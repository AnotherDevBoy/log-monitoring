package com.datadog.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Optional;

public class CliArgumentsParser {
  public static Optional<CliArguments> parseArguments(String[] args) {
    Options options = new Options();

    var logPathOption = Option
            .builder()
            .hasArgs()
            .longOpt("log")
            .desc("The path to the log file")
            .required(false).build();
    var alertThresholdOption = Option
            .builder()
            .longOpt("alert_threshold")
            .desc("The total traffic threshold to alert on")
            .required(false)
            .build();

    options.addOption(logPathOption);
    options.addOption(alertThresholdOption);

    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);

      String alertThresholdValue = cmd.getOptionValue("alert_threshold");

      return Optional.of(new CliArguments(cmd.getOptionValue("log"), alertThresholdValue != null ? Integer.parseInt(alertThresholdValue) : 10));
    } catch (Exception e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("LogMonitor", options);
      return Optional.empty();
    }
  }
}
