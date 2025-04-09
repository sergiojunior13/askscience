package com.sergiojr.askscience.adapters.input;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sergiojr.askscience.domain.model.Answer;
import com.sergiojr.askscience.domain.model.Question;
import com.sergiojr.askscience.domain.ports.input.QuestionControllerPort;
import com.sergiojr.askscience.domain.service.QuestionService;

@CrossOrigin(origins = "*")
@Controller
public class AskQuestionController implements QuestionControllerPort {
  private final QuestionService questionService;

  public AskQuestionController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @GetMapping("/question")
  @ResponseBody
  public Answer askQuestion(@RequestParam(value = "q") String question) {
    Answer answer = questionService.execute(new Question(question));

    return answer;
  }
}
