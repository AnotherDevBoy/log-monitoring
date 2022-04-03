package com.datadog.reporting.statistics;

import com.datadog.domain.EventRepository;
import com.datadog.reporting.BaseReporter;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class StatisticsReporter extends BaseReporter {
  private static final int STATS_WINDOW = 10;

  private final EventRepository eventRepository;
  private final int delay;
  private int counter;

  public StatisticsReporter(BlockingQueue<Long> queue, EventRepository eventRepository, int delay) {
    super(queue);

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
      System.out.printf(
          "[%d, %d] Average RPS=%f. Status codes: 2xx=%d 3xx=%d 4xx=%d 5xx=%d\n",
          start,
          end,
          averageRps,
          statusCodes.get(200),
          statusCodes.get(300),
          statusCodes.get(400),
          statusCodes.get(500));

      this.counter = 0;
    }

    this.counter++;
  }
}
