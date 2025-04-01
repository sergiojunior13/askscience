package com.sergiojr.askscience.domain.ports.output;

import java.util.List;
import com.sergiojr.askscience.domain.model.Article;

public interface ArticlesRepository {
  String getPromptInstructionsForAI();

  List<Article> searchArticles(String searchText);
}
