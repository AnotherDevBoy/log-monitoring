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

    double totalEvents = events.values().stream().mapToInt(List::size).reduce(0, Integer::sum);
    double timeStamps = events.keySet().size();
    return totalEvents / timeStamps;
  }

  @Override
  public Map<Integer, Integer> getStatusCodesCount(long start, long end) {
    var events = this.getEventsInRange(start, end);

    Map<Integer, Integer> statusCodes = new HashMap<>(Map.of(200, 0, 300, 0, 400, 0, 500, 0));

    events
        .values()
        .forEach(
            eventsInTimeStamp ->
                eventsInTimeStamp.forEach(
                    e -> {
                      int statusCode = e.getStatusCode();

                      if (statusCode >= 200 && statusCode < 300) {
                        statusCodes.put(200, statusCodes.get(200) + 1);
                      } else if (statusCode >= 300 && statusCode < 400) {
                        statusCodes.put(300, statusCodes.get(300) + 1);
                      } else if (statusCode >= 400 && statusCode < 500) {
                        statusCodes.put(400, statusCodes.get(400) + 1);
                      } else if (statusCode >= 500 && statusCode < 600) {
                        statusCodes.put(500, statusCodes.get(500) + 1);
                      }
                    }));

    return statusCodes;
  }

  private Map<Long, List<Event>> getEventsInRange(long start, long end) {
    var result = new HashMap<Long, List<Event>>();

    for (long i = start; i < end; ++i) {
      result.put(i, this.events.getOrDefault(i, new LinkedList<>()));
    }

    return result;
  }
}
