package com.datadog.ingestion;


import com.datadog.domain.Event;
import com.datadog.domain.EventListener;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.BlockingQueue;

@RequiredArgsConstructor
public class EventProducer implements EventListener {
  private final BlockingQueue<Event> queue;
  @Override
  public void notify(Event event) {
    while (!this.queue.offer(event)) {

    }
  }
}
