package com.datadog.domain;

import com.datadog.domain.Event;

import java.util.List;

public interface EventRepository {
  void insertEvent(Event event);
  List<Event> getEvents();
}
