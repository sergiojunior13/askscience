package com.sergiojr.askscience.adapters.output.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.sergiojr.askscience.domain.model.Answer;
import com.sergiojr.askscience.domain.model.Article;
import com.sergiojr.askscience.domain.model.Question;
import com.sergiojr.askscience.domain.ports.output.AIRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GeminiAIRepository implements AIRepository {

  @Override
  public Question improveQuestion(Question rawQuestion, String promptInstrucionsForAI) {
    String prompt = String.format(
        """
            Analyze the following user question and suggest an improved scientific search query. Please:
            1. Extract the key scientific concepts from the question
            2. Create a comprehensive query that captures all relevant aspects
            3. Use boolean operators (AND, OR) and quotation marks effectively
            4. Provide only the new query, nothing else
            5. Provide the query in English for better results

            User question: '%s'

            Example:
            - Question: "What is artificial intelligence?"
            - Expected response: '"artificial intelligence" AND (overview OR review OR introduction OR fundamentals) AND ("machine learning" OR "neural networks" OR "deep learning" OR algorithms) AND (history OR development OR applications)'
            """,
        rawQuestion.getContent());

    String improvedQuestion = GeminiAPI.prompt(prompt);

    return new Question(improvedQuestion);
  }

  @Override
  public Answer answerQuestion(Question question, List<Article> articles) {
    try {
      String articlesWithUrlString = articles.stream()
          .map(article -> String.format("{title: '%s', pdfUrl: '%s'}", article.getTitle(), article.getUrl()))
          .collect(Collectors.joining(","));

      String prompt = String.format(
          "Answer the following question based on the provided articles. Please:\n" +
              "1. DETECT THE LANGUAGE OF THE QUESTION AND RESPOND IN THE SAME LANGUAGE.\n" +
              "2. If the articles don't provide the information needed, answer based on your own knowledge, and inform the user that you are doing so.\n"
              +
              "3. The PDFs URLs are these, if you need: [%s]\n\n" +
              "Question: '%s'",
          articlesWithUrlString,
          question.getContent());

      String answerText = GeminiAPI.prompt(prompt, Optional.of(articles), Optional.of(GeminiAnswerSchema.build()));

      GeminiAnswerMapper geminiAnswer = new ObjectMapper().readValue(answerText, GeminiAnswerMapper.class);

      List<Answer.Reference> references = geminiAnswer.references().stream().map(reference -> {
        Article referenceArticle = articles.stream()
            .filter(article -> article.getUrl().equalsIgnoreCase(reference.url()))
            .findFirst().orElse(null);

        if (referenceArticle == null)
          return null;

        return new Answer.Reference(reference.excerpt(), referenceArticle);
      }).filter(reference -> reference != null).toList();

      return new Answer(geminiAnswer.summary(), references);

    } catch (JsonProcessingException e) {
      throw new RuntimeJsonMappingException("Error parsing Gemini answer to question JSON: " + e.getMessage());
    }
  }

  @Override
  public List<Article> reorderArticlesByRelevance(Question originalQuestion, List<Article> articles) {
    List<ScoredArticle> scoredArticles = new ArrayList<>();

    String articlesString = articles.stream()
        .map(article -> String.format("{title: '%s', abstract: '%s'}", article.getTitle(), article.getAbstract()))
        .collect(Collectors.joining(","));

    String evaluationPrompt = String.format(
        """
            Evaluate the relevance of these scientific articles to the user's question.

            User question: "%s"

            Articles: [%s]

            For each article, on a scale from 0 to 10, how relevant is this article for answering the question?
            Expected response:
            [3, 5, 9, ...] /* An array of integers, equivalent to each article */
            """,
        originalQuestion.getContent(),
        articlesString);

    Schema responseSchema = new Schema.Builder(Schema.Type.ARRAY)
        .items(new Schema.Builder(Schema.Type.INTEGER).description("Equivalent article relevance from 0 to 10").build())
        .build();

    String scoresResponse = GeminiAPI.prompt(evaluationPrompt, Optional.empty(), Optional.of(responseSchema));

    int[] scores = extractScores(scoresResponse);

    for (int i = 0; i < scores.length; i++) {
      scoredArticles.add(new ScoredArticle(articles.get(i), scores[i]));
    }

    scoredArticles.sort(Comparator.comparing(ScoredArticle::score).reversed());

    scoredArticles.forEach(scoredArticle -> {
      System.out.println("Article: " + scoredArticle.article().getTitle() + ", Score: " + scoredArticle.score());
    });

    return scoredArticles.stream()
        .map(ScoredArticle::article)
        .toList();
  }

  private int[] extractScores(String scoresResponse) {
    try {
      int[] scores = new ObjectMapper().readValue(scoresResponse, int[].class);

      return scores;
    } catch (JsonProcessingException e) {
      return new int[0];
    }
  }
}

record ScoredArticle(Article article, double score) {
}