package com.datadog.reporting.alerting;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datadog.domain.EventRepository;
import com.datadog.reporting.report.Reporter;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AlertManagerTests {

  @Mock private Reporter mockReporter;
  @Mock private EventRepository mockEventRepository;
  private AlertManager sut;

  @BeforeEach
  public void beforeEach() {
    MockitoAnnotations.initMocks(this);
    this.sut =
        new AlertManager(
            new LinkedBlockingQueue<>(), this.mockReporter, this.mockEventRepository, 0);
  }

  @Test
  public void tick_givenAnAverageRpsBelowDefaultThreshold_whenTheAlertIsNotActive_reportsNothing() {
    givenAverageRpsOf(0.0);

    this.sut.tick(System.currentTimeMillis());

    assertReportsNothing();
  }

  @Test
  public void
      tick_givenAnAverageRpsAboveDefaultThreshold_whenTheAlertIsNotActive_reportsHighTraffic() {
    givenAverageRpsOf(11.0);

    this.sut.tick(System.currentTimeMillis());

    assertReportsHighTraffic();
  }

  @Test
  public void
      tick_givenAnAverageRpsEqualsToDefaultThreshold_whenTheAlertIsNotActive_reportsHighTraffic() {
    givenAverageRpsOf(10.0);

    this.sut.tick(System.currentTimeMillis());

    assertReportsHighTraffic();
  }

  @Test
  public void
      tick_givenAnAverageRpsBelowDefaultThreshold_whenTheAlertIsActive_reportsAlertMitigated() {
    givenAverageRpsOf(11.0, 0.0);

    this.sut.tick(System.currentTimeMillis());
    this.sut.tick(System.currentTimeMillis());

    assertReportAlertsMitigatedAfterHighTraffic();
  }

  @Test
  public void tick_givenAnAverageRpsBelowCustomThreshold_whenTheAlertIsNotActive_reportsNothing() {
    givenAverageRpsOf(2.0);

    this.sut.setThreshold(3);
    this.sut.tick(System.currentTimeMillis());

    assertReportsNothing();
  }

  @Test
  public void
      tick_givenAnAverageRpsAboveCustomThreshold_whenTheAlertIsNotActive_reportsHighTraffic() {
    givenAverageRpsOf(4.0);

    this.sut.setThreshold(3);
    this.sut.tick(System.currentTimeMillis());

    assertReportsHighTraffic();
  }

  @Test
  public void
      tick_givenAnAverageRpsEqualsToCustomThreshold_whenTheAlertIsNotActive_reportsHighTraffic() {
    givenAverageRpsOf(3.0);

    this.sut.setThreshold(3);
    this.sut.tick(System.currentTimeMillis());

    assertReportsHighTraffic();
  }

  @Test
  public void
      tick_givenAnAverageRpsBelowCustomThreshold_whenTheAlertIsActive_reportsAlertMitigated() {
    givenAverageRpsOf(3.0, 2.0);

    this.sut.setThreshold(3);
    this.sut.tick(System.currentTimeMillis());
    this.sut.tick(System.currentTimeMillis());

    assertReportAlertsMitigatedAfterHighTraffic();
  }

  private void givenAverageRpsOf(Double... thresholds) {
    if (thresholds.length == 1) {
      when(this.mockEventRepository.getAverageRps(anyLong(), anyLong())).thenReturn(thresholds[0]);
    } else {
      when(this.mockEventRepository.getAverageRps(anyLong(), anyLong()))
          .thenReturn(thresholds[0], Arrays.copyOfRange(thresholds, 1, thresholds.length));
    }
  }

  private void assertReportsNothing() {
    verify(this.mockReporter, times(0)).report(anyString());
  }

  private void assertReportsHighTraffic() {
    var argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.mockReporter, times(1)).report(argumentCaptor.capture());

    assertTrue(argumentCaptor.getValue().contains("High traffic generated an alert"));
  }

  private void assertReportAlertsMitigatedAfterHighTraffic() {
    var argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.mockReporter, times(2)).report(argumentCaptor.capture());

    assertTrue(argumentCaptor.getValue().contains("High traffic alert mitigated"));
  }
}
