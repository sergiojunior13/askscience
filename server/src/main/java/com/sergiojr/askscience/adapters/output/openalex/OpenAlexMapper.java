package com.sergiojr.askscience.adapters.output.openalex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sergiojr.askscience.domain.model.Article;

public class OpenAlexMapper {
  private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

  public static List<Article> mapToArticleList(List<OpenAlexArticle> openAlexArticles) {
    // Criar um pool de threads
    ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

    try {
      List<CompletableFuture<Article>> futures = openAlexArticles.stream()
          .limit(20)
          .map(article -> CompletableFuture.supplyAsync(() -> mapToArticle(article), executorService))
          .toList();

      return futures.stream()
          .map(CompletableFuture::join)
          .filter(article -> article != null)
          .toList();
    } finally {
      executorService.shutdown();
    }
  }

  public static Article mapToArticle(OpenAlexArticle openAlexArticle) {
    try {
      Location articleLocation = openAlexArticle.locations().stream()
          .filter(location -> location.pdfUrl() != null && location.pdfUrl().endsWith(".pdf"))
          .findFirst()
          .orElse(null);

      if (articleLocation == null)
        return null;

      String pdfUrl = articleLocation.pdfUrl();

      long startTime = System.currentTimeMillis();
      byte[] pdfDownloaded = downloadPdf(pdfUrl);
      long endTime = System.currentTimeMillis();
      long downloadTime = endTime - startTime;

      if (pdfDownloaded != null && pdfDownloaded.length > 0) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfDownloaded))) {
          int pageCount = document.getNumberOfPages();

          if (pageCount == 0) {
            System.out.printf("Thread %s: PDF '%s' não possui páginas.%n",
                Thread.currentThread().getName(),
                openAlexArticle.title());
            return null;
          }

          System.out.printf("Thread %s: PDF '%s' baixado em %d ms (tamanho: %.2f MB, páginas: %d)%n",
              Thread.currentThread().getName(),
              openAlexArticle.title(),
              downloadTime,
              pdfDownloaded.length / (1024.0 * 1024.0),
              pageCount);

          return new Article(
              openAlexArticle.openAccess().oaUrl(),
              openAlexArticle.title(),
              pdfDownloaded, getAbstractFromInvertedIndex(openAlexArticle.abstractInvertedIndex()));

        } catch (IOException e) {
          System.out.printf("Thread %s: Erro ao validar PDF '%s': %s%n",
              Thread.currentThread().getName(),
              openAlexArticle.title(),
              e.getMessage());
          return null;
        }
      } else {
        System.out.printf("Thread %s: PDF '%s' está vazio ou não foi baixado corretamente.%n",
            Thread.currentThread().getName(),
            openAlexArticle.title());
        return null;
      }
    } catch (IOException e) {
      System.err.println("Error downloading PDF for article " + openAlexArticle.title() + ": " + e.getMessage());
      return null;
    }
  }

  static private byte[] downloadPdf(String url) throws IOException {
    if (url == null || url.trim().isEmpty() || !url.endsWith(".pdf")) {
      throw new IOException("PDF URL is null or empty");
    }

    try {
      HttpClient client = HttpClient.newBuilder()
          .followRedirects(HttpClient.Redirect.NORMAL)
          .build();

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("User-Agent",
              "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
          .GET()
          .build();

      HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

      if (response.statusCode() == 200) {
        return response.body();
      } else {
        throw new IOException("HTTP error: " + response.statusCode());
      }
    } catch (Exception e) {
      throw new IOException("Error downloading PDF: " + e.getMessage(), e);
    }
  }

  static private String getAbstractFromInvertedIndex(Map<String, List<Integer>> abstractInvertedIndex) {
    AtomicInteger wordsCount = new AtomicInteger(0);

    abstractInvertedIndex.forEach((_, wordIndeces) -> wordsCount.addAndGet(wordIndeces.size()));

    String[] abstractArray = new String[wordsCount.get()];

    abstractInvertedIndex
        .forEach((keyAsWord, wordIndices) -> wordIndices.forEach(index -> abstractArray[index] = keyAsWord));

    return String.join(" ", abstractArray);
  }
}

record OpenAlexResponse(List<OpenAlexArticle> results) {
}

record OpenAlexArticle(String id, String title, @JsonProperty("open_access") OpenAccess openAccess,
    List<Location> locations,
    @JsonProperty("abstract_inverted_index") Map<String, List<Integer>> abstractInvertedIndex) {
}

record Location(
    @JsonProperty("is_oa") boolean isOa,
    @JsonProperty("landing_page_url") String landingPageUrl,
    @JsonProperty("pdf_url") String pdfUrl) {
}

record OpenAccess(

    @JsonProperty("is_oa") boolean isOa,
    @JsonProperty("oa_status") String oaStatus,
    @JsonProperty("oa_url") String oaUrl,
    @JsonProperty("any_repository_has_fulltext") boolean anyRepositoryHasFulltext) {
}