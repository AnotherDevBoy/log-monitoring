package com.datadog.cli;

import java.util.Optional;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class CliArgumentsParser {
  public static Optional<CliArguments> parseArguments(String[] args) {
    Options options = new Options();
    options.addRequiredOption("l", "log", true, "The path to the log file");

    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);

      return Optional.of(new CliArguments(cmd.getOptionValue("log")));
    } catch (Exception e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("LogMonitor", options);
      return Optional.empty();
    }
  }
}
