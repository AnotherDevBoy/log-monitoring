package com.datadog.cli;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CliArguments {
  private final String filePath;
}
