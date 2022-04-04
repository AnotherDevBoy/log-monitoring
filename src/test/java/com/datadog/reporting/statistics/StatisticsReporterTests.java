package com.datadog.reporting.statistics;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datadog.domain.EventRepository;
import com.datadog.reporting.report.Reporter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StatisticsReporterTests {

  @Mock private Reporter mockReporter;
  @Mock private EventRepository mockEventRepository;
  private StatisticsManager sut;

  @BeforeEach
  public void beforeEach() {
    MockitoAnnotations.initMocks(this);
    this.sut =
        new StatisticsManager(
            new LinkedBlockingQueue<>(), this.mockReporter, this.mockEventRepository, 0);
  }

  @Test
  public void tick_whenReceivedFewerTicksThanTheStatsWindow_doesNotReportStatistics() {
    whenAllStatisticsAvailable();

    this.sut.tick(0);

    assertNotStatisticsReported();
  }

  @Test
  public void tick_whenReceivedAsManyTicksAsTheStatsWindow_reportStatistics() {
    whenAllStatisticsAvailable();

    for (long l = 0; l <= 10; l++) {
      this.sut.tick(l);
    }

    assertAllStatisticsReported();
  }

  @Test
  public void tick_whenReceivedAsManyTicksAsTheStatsWindowWithDelay_reportStatisticsWithDelay() {
    int delay = 2;

    this.sut =
        new StatisticsManager(
            new LinkedBlockingQueue<>(), this.mockReporter, this.mockEventRepository, delay);

    whenAllStatisticsAvailable();

    for (long l = 0; l <= 10 + delay; l++) {
      this.sut.tick(l);
    }

    assertAllStatisticsReported();
  }

  @Test
  public void
      tick_whenReceivedAsManyTicksAsTheStatsWindow_AndSectionIsEmpty_reportStatisticsAndSkipsSectionStatistics() {
    whenAllStatisticsButSectionAvailable();

    for (long l = 0; l <= 10; l++) {
      this.sut.tick(l);
    }

    assertAverageAndStatusStatisticsReported();
  }

  private void whenAllStatisticsAvailable() {
    whenAllStatisticsButSectionAvailable();
    when(this.mockEventRepository.getSectionWithMostHits(anyLong(), anyLong()))
        .thenReturn(Optional.of(Pair.of("api", 1)));
  }

  private void whenAllStatisticsButSectionAvailable() {
    when(this.mockEventRepository.getAverageRps(anyLong(), anyLong())).thenReturn(10.0);
    when(this.mockEventRepository.getStatusCodesCount(anyLong(), anyLong())).thenReturn(Map.of());
  }

  private void assertNotStatisticsReported() {
    verify(this.mockReporter, times(0)).report(anyString());
  }

  private void assertAllStatisticsReported() {
    var argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.mockReporter, times(2)).report(argumentCaptor.capture());

    assertTrue(argumentCaptor.getAllValues().get(0).contains("Average RPS"));
    assertTrue(argumentCaptor.getAllValues().get(0).contains("Status codes: 1xx="));
    assertTrue(
        argumentCaptor.getAllValues().get(1).contains("received the most hits with a total of"));
  }

  private void assertAverageAndStatusStatisticsReported() {
    var argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.mockReporter, times(1)).report(argumentCaptor.capture());

    assertTrue(argumentCaptor.getAllValues().get(0).contains("Average RPS"));
    assertTrue(argumentCaptor.getAllValues().get(0).contains("Status codes: 1xx="));
  }
}
