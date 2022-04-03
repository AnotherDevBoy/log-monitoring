package com.datadog.di;

import com.datadog.alerting.AlertReporter;
import com.datadog.clock.Clock;
import com.datadog.domain.EventRepository;
import com.datadog.domain.InfluxDbEventRepository;
import com.datadog.ingestion.EventIngester;
import com.datadog.statistics.StatisticsReporter;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.List;
import org.influxdb.dto.Query;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.utility.DockerImageName;

public class LogMonitoringModule extends AbstractModule {

  @Provides
  public EventRepository eventRepositoryProvider() {
    // return new InMemoryEventRepository();
    var container = new InfluxDBContainer<>(DockerImageName.parse("influxdb").withTag("1.8.10"));
    container.setEnv(List.of("INFLUXDB_RETENTION_ENABLED=false"));

    var influxDB =
        container
            .withUsername("admin")
            .withPassword("password")
            .withDatabase(InfluxDbEventRepository.DATABASE_NAME)
            .getNewInfluxDB();

    EventRepository repository = new InfluxDbEventRepository(influxDB);

    System.out.println("Starting InfluxDB");
    container.start();

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

  @Provides
  public StatisticsReporter statisticsReporterProvider(EventRepository repository) {
    return new StatisticsReporter(repository, 2);
  }

  @Provides
  public AlertReporter alertReporterProvider(EventRepository repository) {
    return new AlertReporter(repository, 2);
  }

  @Provides
  public Clock clockProvider(StatisticsReporter statisticsReporter, AlertReporter alertReporter) {
    return new Clock(statisticsReporter, alertReporter);
  }

  @Provides
  public EventIngester eventIngesterProvider(EventRepository repository) {
    return new EventIngester(repository);
  }
}
