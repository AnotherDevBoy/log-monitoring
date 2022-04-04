package com.datadog.domain;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;

@Slf4j
@RequiredArgsConstructor
public class InfluxDbEventRepository implements EventRepository {
  public static final String DATABASE_NAME = "monitoring";
  private static final String METRIC_NAME = "http-requests";

  private final InfluxDB influxDB;

  @SneakyThrows
  @Override
  public void insertEvent(Event event) {
    // The ID fields is added to avoid InfluxDB from considering these data points as duplicate
    // https://docs.influxdata.com/influxdb/v2.0/write-data/best-practices/duplicate-points/#preserve-duplicate-points
    Point point =
        Point.measurement(METRIC_NAME)
            .time(event.getTimestamp(), TimeUnit.SECONDS)
            .tag("path", event.getPath())
            .tag("status_code", String.valueOf(event.getStatusCode()))
            .tag("http_method", event.getVerb())
            .tag("id", UUID.randomUUID().toString())
            .fields(
                Map.of(
                    "auth",
                    event.getAuthUser(),
                    "remote_host",
                    event.getRemoteHost(),
                    "rfc931",
                    event.getRfc931(),
                    "protocol",
                    event.getProtocol(),
                    "bytes",
                    event.getBytes()))
            .build();

    this.influxDB.write(point);
  }

  @Override
  public double getAverageRps(long start, long end) {
    Query query =
        new Query(
            String.format(
                "SELECT * from \"%s\" where time >= %ds and time < %ds", METRIC_NAME, start, end));
    var result = influxDB.query(query);

    try {
      return (double) result.getResults().get(0).getSeries().get(0).getValues().size()
          / (end - start);
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      return 0;
    }
  }

  @Override
  public Map<Integer, Integer> getStatusCodesCount(long start, long end) {
    StatusCodeAggregator statusCodeAggregator = new StatusCodeAggregator();

    try {
      Query query =
          new Query(
              String.format(
                  "SELECT * from \"%s\" where time >= %ds and time < %ds GROUP BY \"status_code\"",
                  METRIC_NAME, start, end));
      var queryResult = influxDB.query(query);

      for (var s : queryResult.getResults().get(0).getSeries()) {
        statusCodeAggregator.addStatusCode(
            Integer.parseInt(s.getTags().get("status_code")), s.getValues().size());
      }
    } catch (Exception e) {
      log.warn("Unexpected response from InfluxDB", e);
    }

    return statusCodeAggregator.getStatusCodes();
  }

  @Override
  public Optional<Map.Entry<String, Integer>> getSectionWithMostHits(long start, long end) {
    SectionHitsAggregator sectionHitsAggregator = new SectionHitsAggregator();

    try {
      Query query =
          new Query(
              String.format(
                  "SELECT * from \"%s\" where time >= %ds and time < %ds GROUP BY \"path\"",
                  METRIC_NAME, start, end));
      var queryResult = influxDB.query(query);

      for (var s : queryResult.getResults().get(0).getSeries()) {
        sectionHitsAggregator.addHitToSection(
            Event.getSection(s.getTags().get("path")), s.getValues().size());
      }
    } catch (Exception e) {
      log.warn("Unexpected response from InfluxDB", e);
    }

    return sectionHitsAggregator.getSectionWithMostHits();
  }
}
