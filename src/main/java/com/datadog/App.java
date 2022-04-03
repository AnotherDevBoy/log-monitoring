package com.datadog;

import com.datadog.alerting.AlertReporter;
import com.datadog.cli.CliArgumentsParser;
import com.datadog.clock.Clock;
import com.datadog.domain.EventListener;
import com.datadog.domain.EventParser;
import com.datadog.domain.EventRepository;
import com.datadog.domain.InfluxDbEventRepository;
import com.datadog.ingestion.EventIngester;
import com.datadog.statistics.StatisticsReporter;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.FileReader;
import java.util.List;

public class App {
  public static void main(String[] args) {
    var maybeArguments = CliArgumentsParser.parseArguments(args);

    if (maybeArguments.isEmpty()) {
      return;
    }

    // TODO: Instructions - Docker pull is required
    var container = new InfluxDBContainer<>(DockerImageName.parse("influxdb").withTag("1.8.10"));
    container.setEnv(List.of("INFLUXDB_RETENTION_ENABLED=false"));

    try {
      FileReader filereader = new FileReader(maybeArguments.get().getFilePath());

      System.out.println("Starting InfluxDB");
      container.start();

      // Make this configurable
      EventRepository repository = new InfluxDbEventRepository(container);
      //EventRepository repository = new InMemoryEventRepository();
      StatisticsReporter statisticsReporter = new StatisticsReporter(repository, 2);
      AlertReporter alertReporter = new AlertReporter(repository, 2);

      List<EventListener> eventListeners =
          List.of(
              new EventIngester(repository), new Clock(List.of(statisticsReporter, alertReporter)));

      CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();

      System.out.println("Start sending events");
      for (String[] row : csvReader) {
        var event = EventParser.parseEvent(row);

        for (var listener : eventListeners) {
          listener.notify(event);
        }
      }
    } catch (Exception e) {
      // TODO: Error handling
      e.printStackTrace();
    } finally {
      container.stop();
    }
  }
}
