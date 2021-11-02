package com.sethpeden;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class StocksAPI {
  private static final int REQUEST_PER_MINUTE = 4;
  private static final long MILLIS_PER_REQUEST = Duration.ofMinutes(1).toMillis() / StocksAPI.REQUEST_PER_MINUTE;
  private static Long lastCall = null;
  private static long requestCount = 0;

  private static final File AUTH_FILE = new File("src/main/resources/auth.txt");
  private static String AUTHORIZATION;
  static {
    try {
      AUTHORIZATION = Files.lines(AUTH_FILE.toPath()).reduce((a, b) -> String.format("%s%s", a, b)).orElse("");
    } catch (IOException ignored) {
    }
  }

  private static final String HOST_NAME = "api.polygon.io";

  private static final HttpResponse.BodyHandler<JSON> HANDLER = responseInfo -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), JSON::parse);
  private static final HttpClient CLIENT = HttpClient.newHttpClient();

  private static RequestBuilder baseRequest() {
    return RequestBuilder.builder()
            .protocol(RequestBuilder.Protocol.HTTPS)
            .hostname(HOST_NAME)
            .header("Authorization", AUTHORIZATION);
  }

  private static JSON execute(HttpRequest request) throws IOException, InterruptedException {
    while (StocksAPI.requestCount >= StocksAPI.REQUEST_PER_MINUTE) {
      // Waiting until we can send the request.
      if (StocksAPI.lastCall != null) {
        long millisSinceLastCall = System.currentTimeMillis() - StocksAPI.lastCall;
        StocksAPI.requestCount -= millisSinceLastCall / StocksAPI.MILLIS_PER_REQUEST;
      }
    }
    JSON response = CLIENT.send(request, HANDLER).body();
    StocksAPI.requestCount++;
    StocksAPI.lastCall = System.currentTimeMillis();
    return response.containsKey("error") ? StocksAPI.execute(request) : response;
  }

  public static JSON get(String url) throws IOException, InterruptedException {
    return StocksAPI.execute(HttpRequest.newBuilder().GET().uri(URI.create(url)).header("Authorization", AUTHORIZATION).build());
  }

  public static JSON getTickerInfo(String ticker) throws IOException, InterruptedException {
    HttpRequest request = baseRequest()
            .method(RequestBuilder.Method.GET)
            .path("/v3/reference/tickers")
            .parameter("ticker", "F")
            .build();
    return execute(request);
  }

  public static JSON getTickerDetails(String ticker) throws IOException, InterruptedException {
    HttpRequest request = baseRequest()
            .method(RequestBuilder.Method.GET)
            .path(String.format("/v1/meta/symbols/%s/company", ticker))
            .build();
    return execute(request);
  }

  public static Map<String, Double> getMarketCaps(List<String> tickers) throws IOException, InterruptedException {
    Map<String, Double> marketCaps = new HashMap<>();
    int count = 0;
    for (String ticker : tickers) {
      System.out.println(String.format("Getting Info for Ticker: %d - %s", ++count, ticker));
      JSON tickerInfo = StocksAPI.getTickerDetails(ticker);
      marketCaps.put(ticker, (double) tickerInfo.get("marketcap").asLong());
    }
    return marketCaps;
  }

  public static TreeMap<String, Double> getTopMarketCaps(int num) throws IOException, InterruptedException {
    TreeMap<String, Double> sortedMarketCaps = new TreeMap<>(StocksAPI.getMarketCaps(StocksAPI.getAllTickers()));
    return sortedMarketCaps.entrySet().stream()
            .limit(num)
            .collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
  }

  public static List<String> getAllTickers() throws IOException, InterruptedException {
    List<String> tickers = new ArrayList<>();
    JSON response = StocksAPI.getTickers(1000);
    System.out.println(response);
    long tickerCount = response.get("count").asLong();
    long limit = response.get("count").asLong();
    for (int i = 0; i < limit; i++)
      tickers.add(response.get("results").get(i).get("ticker").asString());
    while (response.containsKey("next_url")) {
      response = StocksAPI.get(response.get("next_url").asString());
      System.out.println(response);
      tickerCount += response.get("count").asLong();
      limit = response.get("count").asLong();
      for (int i = 0; i < limit; i++)
        tickers.add(response.get("results").get(i).get("ticker").asString());
    }
    System.out.println(String.format("Total Ticker Count: %d", tickerCount));
    return tickers;
  }

  public static JSON getTickers(int count) throws IOException, InterruptedException {
    HttpRequest request = baseRequest()
            .method(RequestBuilder.Method.GET)
            .path("/v3/reference/tickers")
            .parameter("limit", count)
            .build();
    return execute(request);
  }
}
