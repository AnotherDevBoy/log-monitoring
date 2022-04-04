package com.datadog.time;

import com.datadog.domain.Event;
import com.datadog.domain.EventListener;
import java.util.concurrent.BlockingQueue;

public class Clock implements EventListener {
  private final BlockingQueue<Long>[] tickerListeners;

  private long timestamp;

  public Clock(BlockingQueue<Long>... tickerListeners) {
    this.tickerListeners = tickerListeners;
    timestamp = 0;
  }

  @Override
  public void notify(Event event) {
    if (this.timestamp == 0) {
      this.timestamp = event.getTimestamp() - 1;
    }

    if (event.getTimestamp() > timestamp) {
      long totalTicks = event.getTimestamp() - timestamp;

      for (int i = 1; i < totalTicks + 1; ++i) {
        for (var listener : tickerListeners) {
          while (!listener.offer(timestamp + i)) {}
        }
      }

      timestamp = event.getTimestamp();
    }
  }
}
