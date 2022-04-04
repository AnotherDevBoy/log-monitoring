package com.datadog.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.datadog.domain.Event;
import com.datadog.utils.TestDataHelper;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.Test;

public class EventProducerTests {

  @Test
  public void notify_addsEventToQUeue() {
    var queue = new LinkedBlockingQueue<Event>();
    var sut = new EventProducer(queue);
    sut.notify(TestDataHelper.VALID_EVENT);

    var queuedEvent = queue.poll();
    assertEquals(TestDataHelper.VALID_EVENT, queuedEvent);
  }
}
