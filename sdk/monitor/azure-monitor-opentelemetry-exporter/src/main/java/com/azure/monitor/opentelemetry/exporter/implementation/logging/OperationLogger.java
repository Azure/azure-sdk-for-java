/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.logging;

import com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId;
import org.slf4j.MDC;

import javax.annotation.Nullable;

// operation failure stats for a given 5-min window
// each instance represents a logical grouping of errors that a user cares about and can understand,
// e.g. sending telemetry to the portal, storing telemetry to disk, ...
public class OperationLogger {

  public static final OperationLogger NOOP = new OperationLogger(null);

  @Nullable private final AggregatingLogger aggregatingLogger;

  public OperationLogger(Class<?> source, String operation) {
    this(source, operation, 300);
  }

  // visible for testing
  OperationLogger(Class<?> source, String operation, int intervalSeconds) {
    this(new AggregatingLogger(source, operation, true, intervalSeconds));
  }

  private OperationLogger(@Nullable AggregatingLogger aggregatingLogger) {
    this.aggregatingLogger = aggregatingLogger;
  }

  public void recordSuccess() {
    if (aggregatingLogger != null) {
      aggregatingLogger.recordSuccess();
    }
  }

  @SuppressWarnings("try")
  // failureMessage should have low cardinality
  public void recordFailure(String failureMessage, AzureMonitorMsgId msgId) {
    if (aggregatingLogger != null) {
      try (MDC.MDCCloseable ignored = msgId.makeActive()) {
        aggregatingLogger.recordWarning(failureMessage);
      }
    }
  }

  // failureMessage should have low cardinality
  public void recordFailure(String failureMessage) {
    if (aggregatingLogger != null) {
      aggregatingLogger.recordWarning(failureMessage, null);
    }
  }

  public void recordFailure(String failureMessage, Throwable exception) {
    if (aggregatingLogger != null) {
      aggregatingLogger.recordWarning(failureMessage, exception);
    }
  }

    @SuppressWarnings("try")
    // failureMessage should have low cardinality
  public void recordFailure(
      String failureMessage, @Nullable Throwable exception, AzureMonitorMsgId msgId) {
    try (MDC.MDCCloseable ignored = msgId.makeActive()) {
      recordFailure(failureMessage, exception);
    }
  }
}
