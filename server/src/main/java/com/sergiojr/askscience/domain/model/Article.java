package com.sergiojr.askscience.domain.model;

public class Article {
  private String url;
  private String title;
  private byte[] pdf;
  private String abstrct;

  public Article(String url, String title, byte[] pdf) {
    this.url = url;
    this.title = title;
    this.pdf = pdf;
  }

  public Article(String url, String title, byte[] pdf, String abstrct) {
    this.url = url;
    this.title = title;
    this.pdf = pdf;
    this.abstrct = abstrct;
  }

  public String getUrl() {
    return url;
  }

  public String getTitle() {
    return title;
  }

  public byte[] getPdf() {
    return pdf;
  }

  public String getAbstract() {
    return abstrct;
  }

  public void setAbstract(String abstrct) {
    this.abstrct = abstrct;
  }
}