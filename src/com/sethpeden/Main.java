package com.sethpeden;

import java.io.IOException;
import java.time.Duration;

public class Main {
  public static void main(String[] args) throws IOException, InterruptedException {
    JSON response = StocksAPI.getTickers(1000);
    System.out.println(response);
    long tickerCount = response.get("count").asLong();
    while (response.containsKey("next_url")) {
      Thread.sleep(Duration.ofSeconds(20).toMillis());
      response = StocksAPI.get(response.get("next_url").asString());
      System.out.println(response);
      tickerCount += response.get("count").asLong();
    }
    System.out.println(String.format("Total Ticker Count: %d", tickerCount));
  }
}