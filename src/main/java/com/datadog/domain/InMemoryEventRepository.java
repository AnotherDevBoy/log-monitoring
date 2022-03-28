package com.datadog.domain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InMemoryEventRepository implements EventRepository {
  private final Map<Long, List<Event>> events;

  public InMemoryEventRepository() {
    this.events = new HashMap<>();
  }

  @Override
  public void insertEvent(Event event) {
    if (!events.containsKey(event.getTimestamp())) {
      this.events.put(event.getTimestamp(), new LinkedList<>());
    }

    this.events.get(event.getTimestamp()).add(event);
  }

  @Override
  public Map<Long, List<Event>> getEvents(long start, long end) {
    var result = new HashMap<Long, List<Event>>();

    for (long i = start; i < end; ++i) {
      result.put(i, this.events.getOrDefault(i, new LinkedList<>()));
    }

    return result;
  }
}
