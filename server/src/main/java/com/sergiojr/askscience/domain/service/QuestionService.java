package com.sergiojr.askscience.domain.service;

import java.util.List;

import com.sergiojr.askscience.domain.model.Answer;
import com.sergiojr.askscience.domain.model.Article;
import com.sergiojr.askscience.domain.model.Question;
import com.sergiojr.askscience.domain.ports.output.AIRepository;
import com.sergiojr.askscience.domain.ports.output.ArticlesRepository;

public class QuestionService {
  private final AIRepository aiRepository;
  private final ArticlesRepository articlesRepository;

  public QuestionService(AIRepository aiRepository, ArticlesRepository articlesRepository) {
    this.aiRepository = aiRepository;
    this.articlesRepository = articlesRepository;
  }

  public Answer execute(Question question) {
    String instrucions = articlesRepository.getPromptInstructionsForAI();

    Question improvedQuestion = aiRepository.improveQuestion(question, instrucions);

    System.out.println("Searching for articles from search :" + improvedQuestion.getContent());

    long startTime = System.currentTimeMillis();
    System.out.printf("[%s] Started fetching articles data\n", startTime);

    List<Article> articles = articlesRepository.searchArticles(improvedQuestion.getContent());

    var reorderedArticles = aiRepository.reorderArticlesByRelevance(question, articles);
    reorderedArticles = reorderedArticles.stream().limit(5).toList();

    System.out.println("Found " + articles.size() + " articles");

    long endTime = System.currentTimeMillis();
    System.out.printf("[%s] Finished fetching articles data. Time taken: %dms\n", endTime, (endTime - startTime));
    return aiRepository.answerQuestion(question, reorderedArticles);
  }
}
