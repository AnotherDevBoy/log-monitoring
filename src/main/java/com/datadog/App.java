package com.datadog;

import com.datadog.cli.CliArguments;
import com.datadog.cli.CliArgumentsParser;
import com.datadog.clock.Clock;
import com.datadog.di.LogMonitoringModule;
import com.datadog.domain.EventListener;
import com.datadog.domain.EventParser;
import com.datadog.ingestion.EventProducer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class App {
  public static void main(String[] args) {
    var maybeArguments = CliArgumentsParser.parseArguments(args);

    if (maybeArguments.isEmpty()) {
      return;
    }

    try {
      Injector injector = Guice.createInjector(new LogMonitoringModule());

      var eventProducer = injector.getInstance(EventProducer.class);
      var clock = injector.getInstance(Clock.class);

      processFile(maybeArguments.get(), List.of(eventProducer, clock));
    } catch (Exception e) {
      System.err.println("An unrecoverable error occurred");
      e.printStackTrace();
    } finally {
      System.exit(0);
    }
  }

  private static void processFile(CliArguments cliArguments, List<EventListener> eventListeners) throws FileNotFoundException {
    FileReader filereader = new FileReader(cliArguments.getFilePath());

    CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();

    System.out.println("Start sending events");
    for (String[] row : csvReader) {
      var event = EventParser.parseEvent(row);

      for (var listener : eventListeners) {
        listener.notify(event);
      }
    }
  }
}
