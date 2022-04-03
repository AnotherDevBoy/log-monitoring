package com.datadog.input;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CliArguments {
  private final String filePath;
  private final Optional<Integer> maybeAlertThreshold;
}
