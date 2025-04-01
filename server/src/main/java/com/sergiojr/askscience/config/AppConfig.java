package com.sergiojr.askscience.config;

import com.sergiojr.askscience.adapters.output.gemini.GeminiAIRepository;
import com.sergiojr.askscience.adapters.output.openalex.OpenAlexArticlesRepository;
import com.sergiojr.askscience.domain.ports.output.AIRepository;
import com.sergiojr.askscience.domain.ports.output.ArticlesRepository;

import com.sergiojr.askscience.domain.service.QuestionService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class AppConfig {

  @Bean
  public AIRepository aiRepository() {
    return new GeminiAIRepository();
  }

  @Bean
  public ArticlesRepository articlesRepository() {
    return new OpenAlexArticlesRepository();
  }

  @Bean
  public QuestionService questionService(AIRepository aiRepository, ArticlesRepository articlesRepository) {
    return new QuestionService(aiRepository, articlesRepository);
  }

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(64000);
    return loggingFilter;
  }
}