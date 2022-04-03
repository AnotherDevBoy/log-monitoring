package com.datadog;

import com.datadog.di.LogMonitoringModule;
import com.datadog.ingestion.EventConsumer;
import com.datadog.input.CliArgumentsParser;
import com.datadog.input.LogFileProcessor;
import com.datadog.reporting.alerting.AlertManager;
import com.datadog.reporting.statistics.StatisticsManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
  public static void main(String[] args) {
    var maybeArguments = CliArgumentsParser.parseArguments(args);

    if (maybeArguments.isEmpty()) {
      return;
    }

    try {
      Injector injector = Guice.createInjector(new LogMonitoringModule());

      var arguments = maybeArguments.get();

      if (arguments.getMaybeAlertThreshold().isPresent()) {
        var alertReporter = injector.getInstance(AlertManager.class);
        alertReporter.setThreshold(arguments.getMaybeAlertThreshold().get());
      }

      var logFileProcessor = injector.getInstance(LogFileProcessor.class);

      startBackgroundThreads(injector);

      logFileProcessor.processFile(arguments);
    } catch (Exception e) {
      System.err.println("An unrecoverable error occurred");
      e.printStackTrace();
    } finally {
      System.exit(0);
    }
  }

  private static void startBackgroundThreads(Injector injector) {
    var statisticsReporter = injector.getInstance(StatisticsManager.class);
    var alertReporter = injector.getInstance(AlertManager.class);
    var eventConsumer = injector.getInstance(EventConsumer.class);

    new ThreadFactoryBuilder()
        .setNameFormat("statistics")
        .setDaemon(true)
        .build()
        .newThread(statisticsReporter)
        .start();
    new ThreadFactoryBuilder()
        .setNameFormat("alerts")
        .setDaemon(true)
        .build()
        .newThread(alertReporter)
        .start();
    new ThreadFactoryBuilder()
        .setNameFormat("event-consumer")
        .setDaemon(true)
        .build()
        .newThread(eventConsumer)
        .start();
  }
}
