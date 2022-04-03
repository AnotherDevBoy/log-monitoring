package com.datadog.domain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InMemoryEventRepository implements EventRepository {
  private final Map<Long, List<Event>> events;

  public InMemoryEventRepository() {
    this.events = new HashMap<>();
  }

  @Override
  public void insertEvent(Event event) {
    if (!events.containsKey(event.getTimestamp())) {
      this.events.put(event.getTimestamp(), new LinkedList<>());
    }

    this.events.get(event.getTimestamp()).add(event);
  }

  @Override
  public double getAverageRps(long start, long end) {
    var events = this.getEventsInRange(start, end);

    double totalEvents = events.values().stream().mapToLong(List::size).reduce(0, Long::sum);

    long interval = end - start;
    return totalEvents / interval;
  }

  @Override
  public Map<Integer, Integer> getStatusCodesCount(long start, long end) {
    StatusCodeAggregator statusCodeAggregator = new StatusCodeAggregator();

    var events = this.getEventsInRange(start, end);
    events
        .values()
        .forEach(
            eventsInTimeStamp ->
                eventsInTimeStamp.forEach(
                    e -> statusCodeAggregator.addStatusCode(e.getStatusCode(), 1)));

    return statusCodeAggregator.getStatusCodes();
  }

  private Map<Long, List<Event>> getEventsInRange(long start, long end) {
    var result = new HashMap<Long, List<Event>>();

    for (long i = start; i < end; ++i) {
      result.put(i, this.events.getOrDefault(i, new LinkedList<>()));
    }

    return result;
  }
}
