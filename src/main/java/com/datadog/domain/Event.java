package com.datadog.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;

@With
@Getter
@ToString
@RequiredArgsConstructor
public class Event {
  private final String remoteHost;
  private final String rfc931;
  private final String authUser;
  private final long timestamp;
  private final String verb;
  private final String path;
  private final String protocol;
  private final int statusCode;
  private final long bytes;

  public static String getSection(String path) {
    return "/" + path.split("/")[1];
  }
}
