package com.sethpeden;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) throws IOException, InterruptedException {
    TreeMap<String, Double> top100MarketCaps = StocksAPI.getTopMarketCaps(100);
    System.out.println(top100MarketCaps);
  }
}