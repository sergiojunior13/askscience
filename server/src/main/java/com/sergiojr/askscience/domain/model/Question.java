package com.sergiojr.askscience.domain.model;

import java.security.InvalidParameterException;

public class Question {
  private String content;

  public Question(String content) {
    if (content.trim().isEmpty()) {
      throw new InvalidParameterException("Question content is empty");
    }

    this.content = content;
  }

  public String getContent() {
    return content;
  }
}