package com.datadog.ingestion;

import com.datadog.domain.Event;
import com.datadog.domain.EventListener;
import com.datadog.domain.EventRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventWriter implements EventListener {
  private final EventRepository repository;

  @Override
  public void notify(Event event) {
    repository.insertEvent(event);
  }
}
