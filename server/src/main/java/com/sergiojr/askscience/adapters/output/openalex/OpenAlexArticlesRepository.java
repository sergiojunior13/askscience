package com.sergiojr.askscience.adapters.output.openalex;

import java.net.URI;
import java.util.List;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.sergiojr.askscience.domain.model.Article;
import com.sergiojr.askscience.domain.ports.output.ArticlesRepository;

public class OpenAlexArticlesRepository implements ArticlesRepository {
  private String promptInstructionsForAI = """
      "Given the user's question, translate it to English and fix any grammar error and remove all special characters."
      """;
  private static final String BASE_URL = "https://api.openalex.org/works";

  @Override
  public String getPromptInstructionsForAI() {
    return promptInstructionsForAI;
  }

  @Override
  public List<Article> searchArticles(String searchText) {
    URI uri = UriComponentsBuilder.fromUriString(BASE_URL)
        .queryParam("search", searchText)
        .queryParam("filter", "open_access.is_oa:true,has_abstract:true")
        .build()
        .toUri();

    return fetchArticlesFromUrl(uri);
  }

  private List<Article> fetchArticlesFromUrl(URI uri) {
    WebClient webClient = WebClient.builder()
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer size
        .build();

    var articles = webClient.get()
        .uri(uri)
        .retrieve()
        .bodyToMono(OpenAlexResponse.class)
        .block()
        .results();

    return OpenAlexMapper.mapToArticleList(articles);
  }

}