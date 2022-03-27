package com.datadog;

public class EventParser {
  public static Event parseEvent(String[] row) {
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
            Long.parseLong(row[6])
    );
  }
}
