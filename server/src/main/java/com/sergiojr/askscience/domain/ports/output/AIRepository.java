package com.sergiojr.askscience.domain.ports.output;

import java.util.List;

import com.sergiojr.askscience.domain.model.Answer;
import com.sergiojr.askscience.domain.model.Article;
import com.sergiojr.askscience.domain.model.Question;

public interface AIRepository {
  public Question improveQuestion(Question rawQuestion, String promptInstructionsForAI);

  public Answer answerQuestion(Question question, List<Article> articles);

  public List<Article> reorderArticlesByRelevance(Question question, List<Article> articles);
}
