package com.sergiojr.askscience.adapters.output.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiojr.askscience.domain.model.Article;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;
import java.util.List;

@Service
public class GeminiAPI {

  private static final String API_KEY = System.getenv("GEMINI_API_KEY");

  private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
      + API_KEY;

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final WebClient webClient = WebClient.create();

  static String prompt(String text) {
    return prompt(text, Optional.empty(), Optional.empty());
  }

  static String prompt(String text, List<Article> articles) {
    return prompt(text, Optional.of(articles), Optional.empty());
  }

  static String prompt(String text, Schema responseSchema) {
    return prompt(text, Optional.empty(), Optional.of(responseSchema));
  }

  static String prompt(String text, Optional<List<Article>> articles, Optional<Schema> responseSchema) {
    GenerationConfig generationConfig = null;
    List<Content> contents = new ArrayList<>();
    List<Part> parts = new ArrayList<>();

    if (!text.trim().isEmpty()) {
      parts.add(new Part(text.trim(), null));
    }

    if (articles.isPresent()) {
      articles.get().forEach(article -> {
        // Verifica se o PDF é válido
        if (article.getPdf() != null && article.getPdf().length > 0) {
          String base64Pdf = Base64.getEncoder().encodeToString(article.getPdf());

          InlineData inlineData = new InlineData("application/pdf", base64Pdf);
          parts.add(new Part(null, inlineData));
        }
      });
    }

    if (parts.isEmpty()) {
      throw new IllegalArgumentException("No text or PDF provided");
    }

    contents.add(new Content(parts));

    if (responseSchema.isPresent()) {
      generationConfig = new GenerationConfig("application/json",
          responseSchema.get());
    }

    RequestBody requestBody = new RequestBody(contents, generationConfig);

    try {
      String json = objectMapper.writeValueAsString(requestBody);

      GeminiResponse response = webClient.post()
          .uri(API_URL)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(json)
          .retrieve()
          .bodyToMono(GeminiResponse.class)
          .block();

      return response.extractText();
    } catch (Exception e) {
      throw new RuntimeException("Failed to request Gemini API: " + e.getMessage(), e);
    }
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
record RequestBody(List<Content> contents, GenerationConfig generationConfig) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record GenerationConfig(@JsonProperty("response_mime_type") String responseMimeType,
    @JsonProperty("response_schema") Schema responseSchema) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record Content(List<Part> parts) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record Part(String text, @JsonProperty("inline_data") InlineData inlineData) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record InlineData(@JsonProperty("mime_type") String mimeType, String data) {
}