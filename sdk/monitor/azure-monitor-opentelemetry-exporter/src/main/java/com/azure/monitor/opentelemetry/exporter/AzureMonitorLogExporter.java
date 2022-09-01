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

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.LogDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.EXPORTER_MAPPING_ERROR;

/**
 * This class is an implementation of OpenTelemetry {@link LogExporter} that allows different
 * logging services to export recorded data for sampled logs in their own format.
 */
class AzureMonitorLogExporter implements LogExporter {

  private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorLogExporter.class);
  private static final OperationLogger exportingLogLogger =
      new OperationLogger(AzureMonitorLogExporter.class, "Exporting log");
  private final AtomicBoolean stopped = new AtomicBoolean();
  private final LogDataMapper mapper;
  private final TelemetryItemExporter telemetryItemExporter;

  /**
   * Creates an instance of log exporter that is configured with given exporter client that sends
   * telemetry events to Application Insights resource identified by the instrumentation key.
   */
  AzureMonitorLogExporter(LogDataMapper mapper, TelemetryItemExporter telemetryItemExporter) {
    this.mapper = mapper;
    this.telemetryItemExporter = telemetryItemExporter;
  }

  /** {@inheritDoc} */
  @Override
  public CompletableResultCode export(Collection<LogData> logs) {
    if (stopped.get()) {
      return CompletableResultCode.ofFailure();
    }

    List<TelemetryItem> telemetryItems = new ArrayList<>();
    for (LogData log : logs) {
      LOGGER.verbose("exporting log: {}", log);
      try {
        String stack = log.getAttributes().get(SemanticAttributes.EXCEPTION_STACKTRACE);
        telemetryItems.add(mapper.map(log, stack, null));
        exportingLogLogger.recordSuccess();
      } catch (Throwable t) {
        exportingLogLogger.recordFailure(t.getMessage(), t, EXPORTER_MAPPING_ERROR);
        return CompletableResultCode.ofFailure();
      }
    }

    return telemetryItemExporter.send(telemetryItems);
  }

  /** {@inheritDoc} */
  @Override
  public CompletableResultCode flush() {
    return telemetryItemExporter.flush();
  }

  /** {@inheritDoc} */
  @Override
  public CompletableResultCode shutdown() {
    stopped.set(true);
    return telemetryItemExporter.shutdown();
  }
}
