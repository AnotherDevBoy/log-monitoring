package com.datadog.domain;

import java.util.LinkedList;
import java.util.List;

public class InMemoryEventRepository implements EventRepository {
  private final List<Event> events;

  public InMemoryEventRepository() {
    this.events = new LinkedList<>();
  }

  @Override
  public void insertEvent(Event event) {
    this.events.add(event);
  }

  @Override
  public List<Event> getEvents() {
    return this.events;
  }
}
