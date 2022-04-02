package com.datadog.domain;

import java.util.Map;

public interface EventRepository {
  void insertEvent(Event event);

  double getAverageRps(long start, long end);

  Map<Integer, Integer> getStatusCodesCount(long start, long end);
}
