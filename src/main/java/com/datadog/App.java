package com.datadog;

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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class App {
  public static void main(String[] args) {
    Options options = new Options();
    options.addRequiredOption("l", "log", true, "The path to the log file");

    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);

      var filePath = cmd.getOptionValue("log");

      FileReader filereader = new FileReader(filePath);

      EventRepository repository = new InMemoryEventRepository();
      StatisticsReporter statisticsReporter = new StatisticsReporter(repository);

      List<EventListener> eventListeners = List.of(new EventWriter(repository), new Clock(List.of(statisticsReporter)));

      CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();

      for (String[] row : csvReader) {
        var event = EventParser.parseEvent(row);

        for (var listener : eventListeners) {
          listener.notify(event);
        }
      }
    } catch (Exception e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("LogMonitor", options);
    }
  }
}
