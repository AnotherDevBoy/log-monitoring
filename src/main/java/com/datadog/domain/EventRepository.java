package com.datadog.domain;

import java.util.List;

public interface EventRepository {
  void insertEvent(Event event);

  List<Event> getEvents();
}
