package com.datadog.domain;

import java.util.Map;
import org.influxdb.InfluxDB;
import org.testcontainers.containers.InfluxDBContainer;

public class InfluxDbEventRepository implements EventRepository {
  private InfluxDB influxDB;

  public InfluxDbEventRepository() {
    var container = new InfluxDBContainer<>();
    this.influxDB = container.getNewInfluxDB();
  }

  @Override
  public void insertEvent(Event event) {}

  @Override
  public double getAverageRps(long start, long end) {
    return 0;
  }

  @Override
  public Map<Integer, Integer> getStatusCodesCount(long start, long end) {
    return null;
  }
}
