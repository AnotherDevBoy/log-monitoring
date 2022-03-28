package com.datadog.statistics;

import com.datadog.clock.TickListener;
import com.datadog.domain.Event;
import com.datadog.domain.EventRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StatisticsReporter implements TickListener {
  private static final int STATS_WINDOW = 10;
  private final EventRepository eventRepository;
  private final int delay;
  private int counter;

  public StatisticsReporter(EventRepository eventRepository, int delay) {
    this.eventRepository = eventRepository;
    this.delay = delay;

    this.counter = 0;
  }

  @Override
  public void tick(long timestamp) {
    if (this.counter == 10) {
      var events = this.eventRepository.getEvents(timestamp-this.delay-STATS_WINDOW, timestamp-this.delay);

      double averageRps = this.getAverageRps(events);
      Map<Integer, Integer> statusCodes = this.getStatusCodesCount(events);
      System.out.printf("[%d, %d] Average RPS=%f. Status codes: 2xx=%d 3xx=%d 4xx=%d 5xx=%d\n", timestamp-STATS_WINDOW-this.delay, timestamp-this.delay, averageRps, statusCodes.get(200), statusCodes.get(300), statusCodes.get(400), statusCodes.get(500));

      this.counter = 0;
    }

    this.counter++;
  }

  private double getAverageRps(Map<Long, List<Event>> events) {
    double totalEvents = events.values().stream().mapToInt(List::size).reduce(0, Integer::sum);
    double timeStamps = events.keySet().size();
    return totalEvents / timeStamps;
  }

  private Map<Integer, Integer> getStatusCodesCount(Map<Long, List<Event>> events) {
    Map<Integer, Integer> statusCodes = new HashMap<>(Map.of(200, 0, 300, 0, 400, 0, 500, 0));

    events.values().forEach(eventsInTimeStamp -> eventsInTimeStamp.forEach(e -> {
      int statusCode = e.getStatusCode();

      if (statusCode >= 200 && statusCode < 300) {
        statusCodes.put(200, statusCodes.get(200)+1);
      } else if (statusCode >= 300 && statusCode < 400) {
        statusCodes.put(300, statusCodes.get(300)+1);
      } else if (statusCode >= 400 && statusCode < 500) {
        statusCodes.put(400, statusCodes.get(400)+1);
      } else if (statusCode >= 500 && statusCode < 600) {
        statusCodes.put(500, statusCodes.get(500)+1);
      }
    }));

    return statusCodes;
  }
}
