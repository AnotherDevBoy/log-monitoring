package com.datadog.di;

import com.datadog.reporting.alerting.AlertReporter;
import com.datadog.domain.Event;
import com.datadog.domain.EventListener;
import com.datadog.domain.EventRepository;
import com.datadog.domain.InfluxDbEventRepository;
import com.datadog.ingestion.EventConsumer;
import com.datadog.ingestion.EventProducer;
import com.datadog.reporting.statistics.StatisticsReporter;
import com.datadog.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.influxdb.dto.Query;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogMonitoringModule extends AbstractModule {

  @Singleton
  @Provides
  public EventRepository eventRepositoryProvider() {
    // return new InMemoryEventRepository();
    var container = new InfluxDBContainer<>(DockerImageName.parse("influxdb").withTag("1.8.10"));
    container.setEnv(List.of("INFLUXDB_RETENTION_ENABLED=false"));

    System.out.println("Starting InfluxDB");
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

    System.out.println("Creating database");
    influxDB.query(
        new Query(String.format("CREATE DATABASE %s", InfluxDbEventRepository.DATABASE_NAME)));

    return repository;
  }

  @Singleton
  @Provides
  @Named("stats_queue")
  public BlockingQueue<Long> statisticsQueueProvider() {
    return new LinkedBlockingQueue<>(1);
  }

  @Singleton
  @Provides
  public StatisticsReporter statisticsReporterProvider(@Named("stats_queue") BlockingQueue<Long> statisticsQueue, EventRepository repository) {
    return new StatisticsReporter(statisticsQueue, repository, 2);
  }


  @Singleton
  @Provides
  @Named("alert_queue")
  public BlockingQueue<Long> alertQueueProvider() {
    return new LinkedBlockingQueue<>(1);
  }

  @Singleton
  @Provides
  public AlertReporter alertReporterProvider(@Named("alert_queue") BlockingQueue<Long> alertQueue, EventRepository repository) {
    return new AlertReporter(alertQueue, repository, 2);
  }

  @Singleton
  @Provides
  public Clock clockProvider(@Named("stats_queue") BlockingQueue<Long> statisticsQueue, @Named("alert_queue") BlockingQueue<Long> alertQueue) {
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
  public EventConsumer eventConsumerProvider(@Named("event_queue") BlockingQueue<Event> eventQueue, EventRepository eventRepository) {
    return new EventConsumer(eventQueue, eventRepository);
  }

  @Singleton
  @Provides
  public EventProducer eventProducerProvider(@Named("event_queue") BlockingQueue<Event> eventQueue) {
    return new EventProducer(eventQueue);
  }

  @Singleton
  @Provides
  public List<EventListener> eventListenersProvider(EventProducer eventProducer, Clock clock) {
    return List.of(eventProducer, clock);
  }
}
