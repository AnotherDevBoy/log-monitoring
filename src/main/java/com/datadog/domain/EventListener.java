package com.datadog.domain;

public interface EventListener {
  void notify(Event event);
}
