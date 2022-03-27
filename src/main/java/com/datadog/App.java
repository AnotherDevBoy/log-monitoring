package com.datadog;

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

      var listeners = List.of(new EventWriter());

      CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
      csvReader.forEach(
          row -> {
            var event = EventParser.parseEvent(row);

            listeners.forEach(l -> l.receive(event));
          });
    } catch (Exception e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("LogMonitor", options);
    }
  }
}
