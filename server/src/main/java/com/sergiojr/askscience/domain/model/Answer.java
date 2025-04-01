package com.sergiojr.askscience.domain.model;

import java.util.List;

public class Answer {
  private String content;
  private List<Reference> references;

  public Answer(String content, List<Reference> references) {
    this.content = content;
    this.references = references != null ? references : List.of();
  }

  public String getContent() {
    return content;
  }

  public List<Reference> getReferences() {
    return references;
  }

  public static record Reference(String excerpt, Article article) {
  }
}
