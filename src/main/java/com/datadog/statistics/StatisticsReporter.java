package com.datadog.statistics;

import com.datadog.domain.EventRepository;
import com.datadog.clock.TickListener;

public class StatisticsReporter implements TickListener {
  private final EventRepository eventRepository;
  private int counter;

  public StatisticsReporter(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
    this.counter = 0;
  }

  @Override
  public void tick(long timestamp) {
    if (this.counter == 10) {
      System.out.printf("%s - Event count: %s\n", timestamp, this.eventRepository.getEvents().size());
      this.counter = 0;
    }

    this.counter++;
  }
}
