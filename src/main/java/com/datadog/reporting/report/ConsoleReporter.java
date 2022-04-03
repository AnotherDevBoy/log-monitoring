package com.datadog.reporting.report;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleReporter implements Reporter {
  @Override
  public void report(String message) {
    log.info(message);
  }
}
