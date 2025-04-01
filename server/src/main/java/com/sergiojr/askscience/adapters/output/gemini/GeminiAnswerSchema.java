package com.sergiojr.askscience.adapters.output.gemini;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GeminiAnswerSchema {

  static final String summaryDescriptionForAI = "AI-generated summary based on the articles. If just a text section from the article helps answer the question, use it. When using information from an article into a paragraph, reference the article like this, after the paragraph: [0] // Here, is the index of article from references prop";

  public static Schema build() {
    return build(Optional.of(summaryDescriptionForAI));
  }

  /**
   * Defines the schema for the structured answer to the user's question for
   * Gemini
   */
  public static Schema build(Optional<String> summaryDescriptionForAI) {
    // Schema for a reference item
    Schema referenceSchema = new Schema.Builder(Schema.Type.OBJECT)
        .properties(Map.of(
            "excerpt", new Schema.Builder(Schema.Type.STRING)
                .description("Text section from the original article that was the basis for the summary")
                .build(),
            "url", new Schema.Builder(Schema.Type.STRING)
                .description("Exact url of the referenced article")
                .build()))
        .required(List.of("excerpt", "url"))
        .build();

    // Schema for the array of references
    Schema referencesSchema = new Schema.Builder(Schema.Type.ARRAY)
        .items(referenceSchema)
        .description(
            "A list of references used to create the summary. If there are no references, this will be an empty array.")
        .build();

    // Main schema
    return new Schema.Builder(Schema.Type.OBJECT)
        .properties(Map.of(
            "summary", new Schema.Builder(Schema.Type.STRING)
                .description(summaryDescriptionForAI.get())
                .build(),
            "references", referencesSchema))
        .required(List.of("summary", "references"))
        .build();
  }
}

record GeminiAnswerMapper(String summary, List<Reference> references) {
}

record Reference(String excerpt, String url) {
}