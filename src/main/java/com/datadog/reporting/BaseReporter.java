package com.datadog.reporting;

import com.datadog.time.TickListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseReporter implements Runnable, TickListener {
  private final BlockingQueue<Long> queue;

  protected BaseReporter(BlockingQueue<Long> queue) {
    this.queue = queue;
  }

  @SneakyThrows
  @Override
  public void run() {
    while (true) {
      try {
        var timestamp = this.queue.poll(100, TimeUnit.MILLISECONDS);

        if (timestamp != null) {
          this.tick(timestamp);
        }
      } catch (InterruptedException interruptedException) {
        throw interruptedException;
      } catch (Exception e) {
        log.warn("An error occurred while processing tick", e);
      }
    }
  }
}
