package com.datadog;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

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
}
