package com.datadog.reporting.alerting;

import com.datadog.domain.EventRepository;
import com.datadog.reporting.BaseReporter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
public class AlertReporter extends BaseReporter {
  private static final int ALERT_WINDOW = 120;
  private static final int ALERT_THRESHOLD = 10;

  private EventRepository eventRepository;
  private int delay;
  private boolean active;
  @Setter private int threshold;

  public AlertReporter(BlockingQueue<Long> queue, EventRepository eventRepository, int delay) {
    super(queue);

    this.eventRepository = eventRepository;
    this.delay = delay;
    this.active = false;
    this.threshold = ALERT_THRESHOLD;
  }

  @Override
  public void tick(long timestamp) {
    double averageRps =
        this.eventRepository.getAverageRps(
            timestamp - ALERT_WINDOW - this.delay, timestamp - this.delay);

    if (this.active && averageRps < this.threshold) {
      this.active = false;
      System.out.printf("High traffic alert mitigated at %d - hits = %f\n", timestamp, averageRps);
    } else if (!this.active && averageRps >= this.threshold) {
      this.active = true;
      System.out.printf(
          "High traffic generated an alert - hits = %f, triggered at %d.\n", averageRps, timestamp);
    }
  }
}
