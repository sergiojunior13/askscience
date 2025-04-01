package com.sergiojr.askscience.adapters.output.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponse(List<Candidate> candidates) {
  public String extractText() {
    if (candidates != null && !candidates.isEmpty()) {
      Candidate firstCandidate = candidates.get(0);
      if (firstCandidate.content() != null &&
          firstCandidate.content().parts() != null &&
          !firstCandidate.content().parts().isEmpty()) {
        Part firstPart = firstCandidate.content().parts().get(0);
        return firstPart.text();
      }
    }
    return "";
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
record Candidate(Content content) {
}
