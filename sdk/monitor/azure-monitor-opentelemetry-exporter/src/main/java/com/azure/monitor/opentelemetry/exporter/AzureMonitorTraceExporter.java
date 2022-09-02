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
import com.azure.monitor.opentelemetry.exporter.implementation.SpanDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.EXPORTER_MAPPING_ERROR;

/**
 * This class is an implementation of OpenTelemetry {@link SpanExporter} that allows different
 * tracing services to export recorded data for sampled spans in their own format.
 */
// TODO (trask) move this class into internal package
final class AzureMonitorTraceExporter implements SpanExporter {

    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorTraceExporter.class);

    private static final OperationLogger exportingSpanLogger =
        new OperationLogger(AzureMonitorTraceExporter.class, "Exporting span");

    private final TelemetryItemExporter telemetryItemExporter;
    private final SpanDataMapper mapper;

    /**
     * Creates an instance of exporter that is configured with given exporter client that sends
     * telemetry events to Application Insights resource identified by the instrumentation key.
     */
    AzureMonitorTraceExporter(SpanDataMapper mapper, TelemetryItemExporter telemetryItemExporter) {

        this.mapper = mapper;
        this.telemetryItemExporter = telemetryItemExporter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        List<TelemetryItem> telemetryItems = new ArrayList<>();

        for (SpanData span : spans) {
            LOGGER.verbose("exporting span: {}", span);
            try {
                mapper.map(span, telemetryItems::add);
                exportingSpanLogger.recordSuccess();
            } catch (Throwable t) {
                exportingSpanLogger.recordFailure(t.getMessage(), t, EXPORTER_MAPPING_ERROR);
                return CompletableResultCode.ofFailure();
            }
        }

        return telemetryItemExporter.send(telemetryItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableResultCode flush() {
        return telemetryItemExporter.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableResultCode shutdown() {
        return telemetryItemExporter.shutdown();
    }
}
