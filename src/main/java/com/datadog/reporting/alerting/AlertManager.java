package com.datadog.reporting.alerting;

import com.datadog.domain.EventRepository;
import com.datadog.reporting.EventAggregator;
import com.datadog.reporting.report.Reporter;
import java.util.concurrent.BlockingQueue;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlertManager extends EventAggregator {
  private static final int ALERT_WINDOW = 120;
  private static final int ALERT_THRESHOLD = 10;

  private Reporter reporter;
  private EventRepository eventRepository;
  private int delay;
  private boolean active;
  @Setter private int threshold;

  public AlertManager(
      BlockingQueue<Long> queue, Reporter reporter, EventRepository eventRepository, int delay) {
    super(queue);

    this.reporter = reporter;
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
      this.reporter.report(
          String.format("High traffic alert mitigated at %d - hits = %f", timestamp, averageRps));
    } else if (!this.active && averageRps >= this.threshold) {
      this.active = true;
      this.reporter.report(
          String.format(
              "High traffic generated an alert - hits = %f, triggered at %d",
              averageRps, timestamp));
    }
  }
}
