package com.datadog.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CliArgumentsParserTest {
  @Test
  public void parseArguments_whenNoArgumentsProvider_returnsEmpty() {
    assertEquals(Optional.empty(), CliArgumentsParser.parseArguments(null));
    assertEquals(Optional.empty(), CliArgumentsParser.parseArguments(new String[0]));
  }

  @Test
  public void parseArguments_whenOnlyLogFilePathProvided_returnsArguments() {
    var arguments = CliArgumentsParser.parseArguments("--log", "\"mypath\"");
    assertTrue(arguments.isPresent());
    assertEquals("mypath", arguments.get().getFilePath());
  }

  @Test
  public void parseArguments_whenAllArgumentsProvided_returnsArguments() {
    var maybeArguments =
        CliArgumentsParser.parseArguments("--log", "\"mypath\"", "--alert_threshold", "5");
    assertTrue(maybeArguments.isPresent());

    var arguments = maybeArguments.get();
    assertEquals("mypath", arguments.getFilePath());
    assertTrue(arguments.getMaybeAlertThreshold().isPresent());
    assertEquals(5, arguments.getMaybeAlertThreshold().get());
  }
}
