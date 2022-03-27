package com.datadog;

import com.datadog.cli.CliArgumentsParser;
import com.datadog.clock.Clock;
import com.datadog.domain.EventListener;
import com.datadog.domain.EventParser;
import com.datadog.domain.EventRepository;
import com.datadog.domain.InMemoryEventRepository;
import com.datadog.ingestion.EventWriter;
import com.datadog.statistics.StatisticsReporter;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileReader;
import java.util.List;

public class App {
  public static void main(String[] args) {
    var maybeArguments = CliArgumentsParser.parseArguments(args);

    if (maybeArguments.isEmpty()) {
      return;
    }

    try {
      FileReader filereader = new FileReader(maybeArguments.get().getFilePath());

      EventRepository repository = new InMemoryEventRepository();
      StatisticsReporter statisticsReporter = new StatisticsReporter(repository);

      List<EventListener> eventListeners =
          List.of(new EventWriter(repository), new Clock(List.of(statisticsReporter)));

      CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();

      for (String[] row : csvReader) {
        var event = EventParser.parseEvent(row);

        for (var listener : eventListeners) {
          listener.notify(event);
        }
      }
    } catch (Exception e) {
      // TODO: Error handling
      System.err.println("An error occurred");
    }
  }
}
