package com.datadog.input;

import com.datadog.domain.Event;
import com.datadog.domain.EventListener;
import com.google.inject.Inject;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LogFileProcessor {
  private final List<EventListener> eventListeners;

  public void processFile(CliArguments cliArguments) throws FileNotFoundException {
    FileReader filereader = new FileReader(cliArguments.getFilePath());

    CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();

    for (String[] row : csvReader) {
      var event = parseEvent(row);

      for (var listener : this.eventListeners) {
        listener.notify(event);
      }
    }
  }

  private static Event parseEvent(String[] row) {
    if (row.length != 7) {
      throw new IllegalArgumentException();
    }

    String[] tokens = row[4].split(" ");

    if (tokens.length != 3) {
      throw new IllegalArgumentException();
    }

    return new Event(
        row[0],
        row[1],
        row[2],
        Long.parseLong(row[3]),
        tokens[0],
        tokens[1],
        tokens[2],
        Integer.parseInt(row[5]),
        Long.parseLong(row[6]));
  }
}
