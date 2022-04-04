package com.datadog.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.datadog.utils.TestDataHelper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.utility.DockerImageName;

public class InfluxDbEventRepositoryTests {
  private static final Map<Integer, Integer> EMPTY_STATUS_COUNT =
      Map.of(100, 0, 200, 0, 300, 0, 400, 0, 500, 0);

  private static InfluxDBContainer container;
  private static InfluxDB influxDB;
  private EventRepository sut;

  @BeforeAll
  public static void beforeAll() {
    container = new InfluxDBContainer<>(DockerImageName.parse("influxdb").withTag("1.8.10"));
    container.setEnv(List.of("INFLUXDB_RETENTION_ENABLED=false"));
    container.start();

    influxDB =
        container
            .withUsername("admin")
            .withPassword("password")
            .withDatabase(InfluxDbEventRepository.DATABASE_NAME)
            .getNewInfluxDB();
  }

  @BeforeEach
  public void beforeEach() {
    influxDB.query(
        new Query(String.format("CREATE DATABASE %s", InfluxDbEventRepository.DATABASE_NAME)));

    this.sut = new InfluxDbEventRepository(influxDB);
  }

  @AfterEach
  public void afterEach() {
    influxDB.query(
        new Query(String.format("DROP DATABASE %s", InfluxDbEventRepository.DATABASE_NAME)));
  }

  @AfterAll
  public static void afterAll() {
    influxDB.close();
    container.stop();
  }

  @Test
  public void getAverageRps_whenNoEventsAvailable_returnsZero() {
    long start = System.currentTimeMillis() / 1000;
    assertEquals(0, this.sut.getAverageRps(start, start + 1));
  }

  @Test
  public void getAverageRps_whenNoEventsAvailableWithinRange_returnsZero() {
    long start = System.currentTimeMillis() / 1000;

    var event = TestDataHelper.VALID_EVENT.withTimestamp(start);
    this.sut.insertEvent(event);

    assertEquals(0, this.sut.getAverageRps(start + 1, start + 2));
  }

  @Test
  public void getAverageRps_whenEventsAvailableWithinRange_calculatesAverage() {
    long start = System.currentTimeMillis() / 1000;

    var event1 = TestDataHelper.VALID_EVENT.withTimestamp(start);
    var event2 = TestDataHelper.VALID_EVENT.withTimestamp(start + 1);
    this.sut.insertEvent(event1);
    this.sut.insertEvent(event2);
    this.sut.insertEvent(event2);

    assertEquals(1.5, this.sut.getAverageRps(start, start + 2));
  }

  @Test
  public void getStatusCodesCount_whenNoEventsAvailable_returnsStatusCodesWithZeroCount() {
    long start = System.currentTimeMillis() / 1000;
    assertEquals(EMPTY_STATUS_COUNT, this.sut.getStatusCodesCount(start, start + 1));
  }

  @Test
  public void
      getStatusCodesCount_whenNoEventsAvailableWithinRange_returnsStatusCodesWithZeroCount() {
    long start = System.currentTimeMillis() / 1000;

    var event = TestDataHelper.VALID_EVENT.withTimestamp(start);
    this.sut.insertEvent(event);

    assertEquals(EMPTY_STATUS_COUNT, this.sut.getStatusCodesCount(start + 1, start + 2));
  }

  @Test
  public void getStatusCodesCount_whenEventsAvailable_returnsStatusCodes() {
    long start = System.currentTimeMillis() / 1000;

    var event1 = TestDataHelper.VALID_EVENT.withTimestamp(start).withStatusCode(200);
    var event2 = TestDataHelper.VALID_EVENT.withTimestamp(start + 1).withStatusCode(500);
    this.sut.insertEvent(event1);
    this.sut.insertEvent(event2);

    var statusCodes = this.sut.getStatusCodesCount(start, start + 2);
    assertEquals(0, statusCodes.get(100));
    assertEquals(1, statusCodes.get(200));
    assertEquals(0, statusCodes.get(300));
    assertEquals(0, statusCodes.get(400));
    assertEquals(1, statusCodes.get(500));
  }

  @Test
  public void getSectionWithMostHits_whenNoEventsAvailable_returnsEmpty() {
    long start = System.currentTimeMillis() / 1000;
    assertEquals(Optional.empty(), this.sut.getSectionWithMostHits(start, start + 1));
  }

  @Test
  public void getSectionWithMostHits_whenNoEventsAvailableWithinRange_returnsEmpty() {
    long start = System.currentTimeMillis() / 1000;

    var event = TestDataHelper.VALID_EVENT.withTimestamp(start);
    this.sut.insertEvent(event);

    assertEquals(Optional.empty(), this.sut.getSectionWithMostHits(start + 1, start + 2));
  }

  @Test
  public void getSectionWithMostHits_whenEventsAvailable_returnsTopSection() {
    long start = System.currentTimeMillis() / 1000;

    var event1 = TestDataHelper.VALID_EVENT.withTimestamp(start).withPath("/api");
    var event2 = TestDataHelper.VALID_EVENT.withTimestamp(start + 1).withPath("/api/something");
    var event3 = TestDataHelper.VALID_EVENT.withTimestamp(start + 1).withPath("/other");
    this.sut.insertEvent(event1);
    this.sut.insertEvent(event2);
    this.sut.insertEvent(event3);

    var maybeSection = this.sut.getSectionWithMostHits(start, start + 2);
    assertTrue(maybeSection.isPresent());

    var section = maybeSection.get();
    assertEquals("/api", section.getKey());
    assertEquals(2, section.getValue());
  }
}
