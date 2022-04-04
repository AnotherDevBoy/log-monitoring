package com.datadog.domain;

import java.util.Map;
import java.util.Optional;

public interface EventRepository {
  void insertEvent(Event event);

  double getAverageRps(long start, long end);

  Map<Integer, Integer> getStatusCodesCount(long start, long end);

  Optional<Map.Entry<String, Integer>> getSectionWithMostHits(long start, long end);
}
