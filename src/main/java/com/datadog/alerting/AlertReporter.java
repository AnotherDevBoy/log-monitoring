package com.datadog.alerting;

import com.datadog.clock.TickListener;
import com.datadog.domain.EventRepository;

public class AlertReporter implements TickListener {
  private static final int ALERT_WINDOW = 120;
  private static final int ALERT_THRESHOLD = 10;

  private EventRepository eventRepository;
  private int delay;
  private boolean active;

  public AlertReporter(EventRepository eventRepository, int delay) {
    this.eventRepository = eventRepository;
    this.delay = delay;
    this.active = false;
  }

  @Override
  public void tick(long timestamp) {
    double averageRps =
        this.eventRepository.getAverageRps(
            timestamp - ALERT_WINDOW - this.delay, timestamp - this.delay);

    if (this.active && averageRps < ALERT_THRESHOLD) {
      this.active = false;
      System.out.printf("High traffic alert mitigated at %d - hits = %f\n", timestamp, averageRps);
    } else if (!this.active && averageRps >= ALERT_THRESHOLD) {
      this.active = true;
      System.out.printf(
          "High traffic generated an alert - hits = %f, triggered at %d.\n", averageRps, timestamp);
    }
  }
}
