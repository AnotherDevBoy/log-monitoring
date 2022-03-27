package com.datadog;

public class EventWriter implements EventListener {
  @Override
  public void receive(Event event) {
    System.out.println(event);
  }
}
