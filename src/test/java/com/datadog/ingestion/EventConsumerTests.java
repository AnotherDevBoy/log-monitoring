package com.datadog.ingestion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.datadog.domain.Event;
import com.datadog.domain.EventRepository;
import com.datadog.utils.TestDataHelper;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EventConsumerTests {
  @Mock private EventRepository mockEventRepository;

  @BeforeEach
  public void beforeEach() {
    MockitoAnnotations.initMocks(this);
  }

  @SneakyThrows
  @Test
  public void run_whenEventAvailableInQueue_insertsEvent() {
    var queue = new LinkedBlockingQueue<Event>();

    queue.add(TestDataHelper.VALID_EVENT);
    var sut = new EventConsumer(queue, this.mockEventRepository);

    Thread thread = new Thread(sut);
    thread.start();
    Thread.sleep(150);
    thread.interrupt();

    assertInsertEventCalled(1);
  }

  @SneakyThrows
  @Test
  public void run_whenNoEventAvailableInQueue_doesNotAttemptToInsertEvent() {
    var queue = new LinkedBlockingQueue<Event>();

    var sut = new EventConsumer(queue, this.mockEventRepository);

    Thread thread = new Thread(sut);
    thread.start();
    Thread.sleep(150);
    thread.interrupt();

    assertInsertEventCalled(0);
  }

  private void assertInsertEventCalled(int times) {
    verify(this.mockEventRepository, times(times)).insertEvent(any());
  }
}
