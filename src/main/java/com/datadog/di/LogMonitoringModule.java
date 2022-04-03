package com.datadog.di;

import com.datadog.domain.Event;
import com.datadog.domain.EventListener;
import com.datadog.domain.EventRepository;
import com.datadog.domain.InfluxDbEventRepository;
import com.datadog.ingestion.EventConsumer;
import com.datadog.ingestion.EventProducer;
import com.datadog.reporting.alerting.AlertManager;
import com.datadog.reporting.report.ConsoleReporter;
import com.datadog.reporting.report.Reporter;
import com.datadog.reporting.statistics.StatisticsManager;
import com.datadog.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.Query;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class LogMonitoringModule extends AbstractModule {

  @Singleton
  @Provides
  public EventRepository eventRepositoryProvider() {
    // return new InMemoryEventRepository(new StatusCodeAggregator());
    var container = new InfluxDBContainer<>(DockerImageName.parse("influxdb").withTag("1.8.10"));
    container.setEnv(List.of("INFLUXDB_RETENTION_ENABLED=false"));

    log.info("Starting InfluxDB");
    container.start();

    var influxDB =
        container
            .withUsername("admin")
            .withPassword("password")
            .withDatabase(InfluxDbEventRepository.DATABASE_NAME)
            .getNewInfluxDB();

    EventRepository repository = new InfluxDbEventRepository(influxDB);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  influxDB.close();
                  container.stop();
                }));

    log.info("Creating database");
    influxDB.query(
        new Query(String.format("CREATE DATABASE %s", InfluxDbEventRepository.DATABASE_NAME)));

    return repository;
  }

  @Singleton
  @Provides
  public Reporter reporterProvider() {
    return new ConsoleReporter();
  }

  @Singleton
  @Provides
  @Named("stats_queue")
  public BlockingQueue<Long> statisticsQueueProvider() {
    return new LinkedBlockingQueue<>(1);
  }

  @Singleton
  @Provides
  public StatisticsManager statisticsReporterProvider(
      @Named("stats_queue") BlockingQueue<Long> statisticsQueue,
      Reporter reporter,
      EventRepository repository) {
    return new StatisticsManager(statisticsQueue, reporter, repository, 2);
  }

  @Singleton
  @Provides
  @Named("alert_queue")
  public BlockingQueue<Long> alertQueueProvider() {
    return new LinkedBlockingQueue<>(1);
  }

  @Singleton
  @Provides
  public AlertManager alertReporterProvider(
      @Named("alert_queue") BlockingQueue<Long> alertQueue,
      Reporter reporter,
      EventRepository repository) {
    return new AlertManager(alertQueue, reporter, repository, 2);
  }

  @Singleton
  @Provides
  public Clock clockProvider(
      @Named("stats_queue") BlockingQueue<Long> statisticsQueue,
      @Named("alert_queue") BlockingQueue<Long> alertQueue) {
    return new Clock(statisticsQueue, alertQueue);
  }

  @Singleton
  @Provides
  @Named("event_queue")
  public BlockingQueue<Event> eventQueueProvider() {
    return new LinkedBlockingQueue<>(1);
  }

  @Singleton
  @Provides
  public EventConsumer eventConsumerProvider(
      @Named("event_queue") BlockingQueue<Event> eventQueue, EventRepository eventRepository) {
    return new EventConsumer(eventQueue, eventRepository);
  }

  @Singleton
  @Provides
  public EventProducer eventProducerProvider(
      @Named("event_queue") BlockingQueue<Event> eventQueue) {
    return new EventProducer(eventQueue);
  }

  @Singleton
  @Provides
  public List<EventListener> eventListenersProvider(EventProducer eventProducer, Clock clock) {
    return List.of(eventProducer, clock);
  }
}
