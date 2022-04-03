package com.datadog.ingestion;

import com.datadog.domain.Event;
import com.datadog.domain.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class EventConsumer implements Runnable {
  private final BlockingQueue<Event> queue;
  private final EventRepository repository;

  @SneakyThrows
  @Override
  public void run() {
    while (true) {
      var event = this.queue.poll(100, TimeUnit.MILLISECONDS);
      if (event != null) {
        this.repository.insertEvent(event);
      }
    }
  }
}
