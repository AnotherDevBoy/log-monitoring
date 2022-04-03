package com.datadog.input;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class CliArguments {
  private final String filePath;
  private final Optional<Integer> maybeAlertThreshold;
}
