// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
final class AzureMonitorTraceExporter implements SpanExporter {

    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorTraceExporter.class);
    private static final OperationLogger OPERATION_LOGGER =
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
                OPERATION_LOGGER.recordSuccess();
            } catch (Throwable t) {
                OPERATION_LOGGER.recordFailure(t.getMessage(), t, EXPORTER_MAPPING_ERROR);
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
