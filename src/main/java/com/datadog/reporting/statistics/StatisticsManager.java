package com.datadog.reporting.statistics;

import com.datadog.domain.EventRepository;
import com.datadog.reporting.EventAggregator;
import com.datadog.reporting.report.Reporter;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class StatisticsManager extends EventAggregator {
  private static final int STATS_WINDOW = 10;

  private Reporter reporter;
  private final EventRepository eventRepository;
  private final int delay;
  private int counter;

  public StatisticsManager(
      BlockingQueue<Long> queue, Reporter reporter, EventRepository eventRepository, int delay) {
    super(queue);

    this.reporter = reporter;
    this.eventRepository = eventRepository;
    this.delay = delay;
    this.counter = -delay;
  }

  @Override
  public void tick(long timestamp) {
    if (this.counter == STATS_WINDOW) {
      long start = timestamp - STATS_WINDOW - this.delay;
      long end = timestamp - this.delay;

      double averageRps = this.eventRepository.getAverageRps(start, end);
      Map<Integer, Integer> statusCodes = this.eventRepository.getStatusCodesCount(start, end);

      this.reporter.report(
          String.format(
              "Average RPS=%f. Status codes: 1xx=%d 2xx=%d 3xx=%d 4xx=%d 5xx=%d. Interval: [%d, %d]",
              averageRps,
              statusCodes.get(100),
              statusCodes.get(200),
              statusCodes.get(300),
              statusCodes.get(400),
              statusCodes.get(500),
              start,
              end));

      var maybeSection = this.eventRepository.getSectionWithMostHits(start, end);

      if (maybeSection.isPresent()) {
        var section = maybeSection.get();
        this.reporter.report(
            String.format(
                "Section %s received the most hits with a total of %d. Interval: [%d, %d]",
                section.getKey(), section.getValue(), start, end));
      }

      this.counter = 0;
    }

    this.counter++;
  }
}
