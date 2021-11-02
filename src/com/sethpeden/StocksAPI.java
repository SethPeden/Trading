package com.sethpeden;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class StocksAPI {
  private static final String AUTHORIZATION = "Bearer MWltcjqfHF0au_7acdn0moBPWLUWZUKd";
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
    return CLIENT.send(request, HANDLER).body();
  }

  public static JSON get(String url) throws IOException, InterruptedException {
    return CLIENT.send(HttpRequest.newBuilder().GET().uri(URI.create(url)).header("Authorization", AUTHORIZATION).build(), HANDLER).body();
  }

  public static JSON getTickerInfo(String ticker) throws IOException, InterruptedException {
    HttpRequest request = baseRequest()
            .method(RequestBuilder.Method.GET)
            .path("/v3/reference/tickers")
            .parameter("ticker", "F")
            .build();
    return execute(request);
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
