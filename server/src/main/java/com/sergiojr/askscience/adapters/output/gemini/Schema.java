package com.sergiojr.askscience.adapters.output.gemini;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa um objeto Schema para definição do JSON de saída da resposta do
 * Gemini.
 * Baseado em um subconjunto de um objeto de esquema da OpenAPI 3.0.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record Schema(
    Type type,
    String format,
    String title,
    String description,
    Boolean nullable,
    @JsonProperty("enum") List<String> enumValues,
    String maxItems,
    String minItems,
    Map<String, Schema> properties,
    List<String> required,
    List<Schema> anyOf,
    List<String> propertyOrdering,
    Schema items,
    Number minimum,
    Number maximum) {
  /**
   * Construtor com parâmetros obrigatórios
   */
  public Schema(Type type) {
    this(type, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Enum que representa os tipos de dados suportados
   */
  public enum Type {
    TYPE_UNSPECIFIED,
    STRING,
    NUMBER,
    INTEGER,
    BOOLEAN,
    ARRAY,
    OBJECT
  }

  /**
   * Builder para facilitar a criação de objetos Schema
   */
  public static class Builder {
    private Type type;
    private String format;
    private String title;
    private String description;
    private Boolean nullable;
    @JsonProperty("enum")
    private List<String> enumValues;
    private String maxItems;
    private String minItems;
    private Map<String, Schema> properties;
    private List<String> required;
    private List<Schema> anyOf;
    private List<String> propertyOrdering;
    private Schema items;
    private Number minimum;
    private Number maximum;

    public Builder(Type type) {
      this.type = type;
    }

    public Builder format(String format) {
      this.format = format;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder nullable(Boolean nullable) {
      this.nullable = nullable;
      return this;
    }

    public Builder enumValues(List<String> enumValues) {
      this.enumValues = enumValues;
      return this;
    }

    public Builder maxItems(String maxItems) {
      this.maxItems = maxItems;
      return this;
    }

    public Builder minItems(String minItems) {
      this.minItems = minItems;
      return this;
    }

    public Builder properties(Map<String, Schema> properties) {
      this.properties = properties;
      return this;
    }

    public Builder required(List<String> required) {
      this.required = required;
      return this;
    }

    public Builder anyOf(List<Schema> anyOf) {
      this.anyOf = anyOf;
      return this;
    }

    public Builder propertyOrdering(List<String> propertyOrdering) {
      this.propertyOrdering = propertyOrdering;
      return this;
    }

    public Builder items(Schema items) {
      this.items = items;
      return this;
    }

    public Builder minimum(Number minimum) {
      this.minimum = minimum;
      return this;
    }

    public Builder maximum(Number maximum) {
      this.maximum = maximum;
      return this;
    }

    public Schema build() {
      return new Schema(type, format, title, description, nullable, enumValues, maxItems, minItems,
          properties, required, anyOf, propertyOrdering, items, minimum, maximum);
    }
  }
}