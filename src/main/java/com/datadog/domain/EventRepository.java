package com.datadog.domain;

import java.util.List;
import java.util.Map;

public interface EventRepository {
  void insertEvent(Event event);

  Map<Long, List<Event>> getEvents(long start, long end);
}
