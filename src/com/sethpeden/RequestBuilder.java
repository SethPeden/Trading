package com.sethpeden;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequestBuilder {
  private static final ObjectMapper mapper = new ObjectMapper();
  enum Method {
    GET,
    POST,
    PUT,
    DELETE
  }

  enum Protocol {
    HTTPS("https");

    private String value;
    private Protocol(String value) {
      this.value = value;
    }

    public String getValue() {
      return this.value;
    }
  }

  private Method method;
  private Protocol protocol;
  private String hostname;
  private Integer port;
  private String path;
  private Map<String, Object> parameters = new HashMap<>();
  private Map<String, String> headers = new HashMap<>();
  private Object body;

  public RequestBuilder method(Method method) {
    this.method = method;
    return this;
  }

  public RequestBuilder protocol(Protocol protocol) {
    this.protocol = protocol;
    return this;
  }

  public RequestBuilder hostname(String hostname) {
    this.hostname = hostname;
    return this;
  }

  public RequestBuilder port(int port) {
    this.port = port;
    return this;
  }

  public RequestBuilder path(String path) {
    this.path = path;
    return this;
  }

  public RequestBuilder parameter(String key, Object value) {
    this.parameters.put(key, value);
    return this;
  }

  public RequestBuilder header(String key, String value) {
    this.headers.put(key, value);
    return this;
  }

  public RequestBuilder body(Object body) {
    this.body = body;
    return this;
  }

  public HttpRequest build() throws JsonProcessingException {
    Objects.requireNonNull(this.method);
    Objects.requireNonNull(this.hostname);

    StringBuilder sb = new StringBuilder();
    if (this.protocol != null)
      sb.append(this.protocol.value).append("://");
    if (this.hostname != null)
      sb.append(this.hostname);
    if (this.port != null)
      sb.append(":").append(this.port);
    while (sb.toString().endsWith("/"))
      sb.replace(sb.length() - 1, sb.length(), "");
    if (this.path != null && !this.path.isEmpty()) {
      while (this.path.startsWith("/"))
        this.path = this.path.substring(1);
      sb.append("/").append(this.path);
    }
    if (this.parameters.size() > 0) {
      sb.append("?").append(this.parameters.entrySet().stream().map(entry -> String.format("%s=%s", URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8), URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8))).reduce((a, b) -> String.format("%s&%s", a, b)).orElse(""));
    }
    HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(sb.toString()));
    this.headers.forEach(builder::header);
    switch (this.method) {
      case GET:
        builder.GET();
        break;
      case POST:
        builder.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(this.body)));
        break;
      case PUT:
        builder.PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(this.body)));
        break;
      case DELETE:
        builder.DELETE();
        break;
    }
    return builder.build();
  }

  public static RequestBuilder builder() {
    return new RequestBuilder();
  }

}
