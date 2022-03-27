package com.datadog;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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

      // var eventListeners = List.of(new EventWriter());
      long timestamp = 0;

      EventRepository repository = new InMemoryEventRepository();
      StatisticsReporter statisticsReporter = new StatisticsReporter(repository);

      CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();

      for (String[] row : csvReader) {
        var event = EventParser.parseEvent(row);
        repository.insertEvent(event);

        if (event.getTimestamp() > timestamp) {
          long totalTicks = event.getTimestamp() - timestamp;

          if (timestamp > 0) {
            for (int i = 0; i < totalTicks; ++i) {
              statisticsReporter.tick(timestamp + i);
            }
          }

          timestamp = event.getTimestamp();
        }
      }
    } catch (Exception e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("LogMonitor", options);
    }
  }
}
