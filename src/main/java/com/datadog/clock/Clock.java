package com.datadog.clock;

import com.datadog.domain.Event;
import com.datadog.domain.EventListener;

import java.util.List;

public class Clock implements EventListener {
  private final List<TickListener> listeners;

  private long timestamp;
  public Clock(List<TickListener> listeners) {
    this.listeners = listeners;
    timestamp = 0;
  }

  @Override
  public void notify(Event event) {
    if (event.getTimestamp() > timestamp) {
      long totalTicks = event.getTimestamp() - timestamp;

      if (timestamp > 0) {
        for (int i = 0; i < totalTicks; ++i) {
          for (var listener : listeners) {
            listener.tick(timestamp + i);
          }
        }
      }

      timestamp = event.getTimestamp();
    }
  }
}
