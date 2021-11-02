package com.sethpeden;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JSON {
  private static final ObjectMapper mapper = new ObjectMapper();

  private Object json;

  private JSON(Object obj) {
    this.json = obj;
  }

  private JSON(String str) {
    this.json = JSONValue.parse(str);
  }

  public JSON get(String key) {
    return new JSON(((JSONObject) this.json).get(key));
  }

  public boolean containsKey(String key) {
    return ((JSONObject) this.json).containsKey(key);
  }

  public JSON get(int index) {
    return new JSON(((JSONArray) this.json).get(index));
  }

  public Long asLong() {
    return (Long) this.json;
  }

  public Double asDouble() {
    return (Double) this.json;
  }

  public Boolean asBoolean() {
    return (Boolean) this.json;
  }

  public String asString() {
    return String.valueOf(this.json);
  }

  @Override
  public String toString() {
    return this.asString();
  }

  public static JSON parse(String str) {
    return new JSON(str);
  }

}
