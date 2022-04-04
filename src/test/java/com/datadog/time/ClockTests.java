package com.datadog.time;

import com.datadog.utils.TestDataHelper;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ClockTests {
  @Test
  public void notify_whenEventTimestampIsEqualToCurrentTimestamp_doesNotSendTickToListeners() {
    var queue = new LinkedBlockingQueue<Long>();
    var sut = new Clock(queue);

    sut.notify(TestDataHelper.VALID_EVENT.withTimestamp(0));

    assertNull(queue.poll());
  }

  @Test
  public void notify_whenEventTimestampIsGreaterThanCurrentTimestamp_sendsTickToListeners() {
    var queue = new LinkedBlockingQueue<Long>();
    var sut = new Clock(queue);

    sut.notify(TestDataHelper.VALID_EVENT.withTimestamp(1));

    assertEquals(1, queue.poll());
  }

  @Test
  public void
      notify_whenEventTimestampIsFarGreaterThanCurrentTimestamp_sendsMultipleTicksToListeners() {
    var queue = new LinkedBlockingQueue<Long>();
    var sut = new Clock(queue);

    sut.notify(TestDataHelper.VALID_EVENT.withTimestamp(3));

    assertEquals(1, queue.poll());
    assertEquals(2, queue.poll());
    assertEquals(3, queue.poll());
  }

  @Test
  public void
      notify_whenEventTimestampIsGreaterThanCurrentTimestampAndMultipleListeners_sendsTickToListeners() {
    var queue1 = new LinkedBlockingQueue<Long>();
    var queue2 = new LinkedBlockingQueue<Long>();

    var sut = new Clock(queue1, queue2);

    sut.notify(TestDataHelper.VALID_EVENT.withTimestamp(1));

    assertEquals(1, queue1.poll());
    assertEquals(1, queue2.poll());
  }
}
