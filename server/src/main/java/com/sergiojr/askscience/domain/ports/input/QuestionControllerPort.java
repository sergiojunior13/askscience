package com.sergiojr.askscience.domain.ports.input;

import com.sergiojr.askscience.domain.model.Answer;

public interface QuestionControllerPort {
  Answer askQuestion(String question);
}
