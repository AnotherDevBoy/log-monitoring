package com.datadog.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class StatusCodeAggregator {
  @Getter private final Map<Integer, Integer> statusCodes;

  public StatusCodeAggregator() {
    this.statusCodes = new HashMap<>(Map.of(100, 0, 200, 0, 300, 0, 400, 0, 500, 0));
  }

  public void addStatusCode(int statusCode, int amount) {
    if (statusCode >= 100 && statusCode < 200) {
      statusCodes.put(100, statusCodes.get(100) + amount);
    } else if (statusCode >= 200 && statusCode < 300) {
      statusCodes.put(200, statusCodes.get(200) + amount);
    } else if (statusCode >= 300 && statusCode < 400) {
      statusCodes.put(300, statusCodes.get(300) + amount);
    } else if (statusCode >= 400 && statusCode < 500) {
      statusCodes.put(400, statusCodes.get(400) + amount);
    } else if (statusCode >= 500 && statusCode < 600) {
      statusCodes.put(500, statusCodes.get(500) + amount);
    }
  }
}
