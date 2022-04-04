package com.datadog.domain;

import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public interface EventRepository {
  void insertEvent(Event event);

  double getAverageRps(long start, long end);

  Map<Integer, Integer> getStatusCodesCount(long start, long end);

  Optional<Pair<String, Integer>> getSectionWithMostHits(long start, long end);
}
