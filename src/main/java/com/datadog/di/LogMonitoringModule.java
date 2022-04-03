package com.datadog.di;

import com.datadog.alerting.AlertReporter;
import com.datadog.clock.Clock;
import com.datadog.domain.Event;
import com.datadog.domain.EventRepository;
import com.datadog.domain.InfluxDbEventRepository;
import com.datadog.ingestion.EventConsumer;
import com.datadog.ingestion.EventProducer;
import com.datadog.statistics.StatisticsReporter;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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
  public StatisticsReporter statisticsReporterProvider(EventRepository repository) {
    return new StatisticsReporter(repository, 2);
  }

  @Singleton
  @Provides
  public AlertReporter alertReporterProvider(EventRepository repository) {
    return new AlertReporter(repository, 2);
  }

  @Singleton
  @Provides
  public Clock clockProvider(StatisticsReporter statisticsReporter, AlertReporter alertReporter) {
    return new Clock(statisticsReporter, alertReporter);
  }

  @Singleton
  @Provides
  public EventProducer eventProducerProvider(EventRepository eventRepository) {
    BlockingQueue<Event> queue = new LinkedBlockingQueue<>(1);
    var consumer =  new EventConsumer(queue, eventRepository);

    var thread = new Thread(consumer);
    thread.start();

    return new EventProducer(queue);
  }
}
