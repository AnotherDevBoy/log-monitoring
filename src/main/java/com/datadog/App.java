package com.datadog;

import com.datadog.cli.CliArgumentsParser;
import com.datadog.clock.Clock;
import com.datadog.di.LogMonitoringModule;
import com.datadog.domain.EventListener;
import com.datadog.domain.EventParser;
import com.datadog.ingestion.EventIngester;
import com.google.inject.Guice;
import com.google.inject.Injector;
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

      Injector injector = Guice.createInjector(new LogMonitoringModule());

      var eventIngester = injector.getInstance(EventIngester.class);
      var clock = injector.getInstance(Clock.class);

      List<EventListener> eventListeners = List.of(eventIngester, clock);

      CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();

      System.out.println("Start sending events");
      for (String[] row : csvReader) {
        var event = EventParser.parseEvent(row);

        for (var listener : eventListeners) {
          listener.notify(event);
        }
      }
    } catch (Exception e) {
      System.err.println("An unrecoverable error occurred");
      e.printStackTrace();
    }
  }
}
