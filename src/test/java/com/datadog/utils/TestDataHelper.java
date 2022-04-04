package com.datadog.utils;

import com.datadog.domain.Event;

public class TestDataHelper {
  public static final Event VALID_EVENT =
      new Event(
          "10.0.0.1", "-", "user", System.currentTimeMillis(), "GET", "/api", "HTTP 1.0", 200, 100);
}
