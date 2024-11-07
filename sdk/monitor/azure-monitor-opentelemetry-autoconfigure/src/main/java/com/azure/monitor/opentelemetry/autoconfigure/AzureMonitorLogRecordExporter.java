// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.LogDataMapper;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.SemanticAttributes;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.AzureMonitorMsgId;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is an implementation of OpenTelemetry {@link LogRecordExporter} that allows different
 * logging services to export recorded data for sampled logs in their own format.
 */
class AzureMonitorLogRecordExporter implements LogRecordExporter {

    private static final String EXPORTER_LOGGER_PREFIX = "com.azure.monitor.opentelemetry.exporter";
    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorLogRecordExporter.class);
    private static final OperationLogger OPERATION_LOGGER
        = new OperationLogger(AzureMonitorLogRecordExporter.class, "Exporting log");

    private final AtomicBoolean stopped = new AtomicBoolean();
    private final LogDataMapper mapper;
    private final TelemetryItemExporter telemetryItemExporter;

    /**
     * Creates an instance of log exporter that is configured with given exporter client that sends
     * telemetry events to Application Insights resource identified by the instrumentation key.
     */
    AzureMonitorLogRecordExporter(LogDataMapper mapper, TelemetryItemExporter telemetryItemExporter) {
        this.mapper = mapper;
        this.telemetryItemExporter = telemetryItemExporter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableResultCode export(Collection<LogRecordData> logs) {
        if (stopped.get()) {
            return CompletableResultCode.ofFailure();
        }

        List<TelemetryItem> telemetryItems = new ArrayList<>();
        for (LogRecordData log : logs) {
            // TODO (heya) consider using suppress_instrumentation https://github.com/open-telemetry/opentelemetry-java/pull/6546 later when available
            if (log.getInstrumentationScopeInfo().getName().startsWith(EXPORTER_LOGGER_PREFIX)) {
                continue;
            }
            LOGGER.verbose("exporting log: {}", log);
            try {
                String stack = log.getAttributes().get(SemanticAttributes.EXCEPTION_STACKTRACE);
                telemetryItems.add(mapper.map(log, stack, null));
                OPERATION_LOGGER.recordSuccess();
            } catch (Throwable t) {
                OPERATION_LOGGER.recordFailure(t.getMessage(), t, AzureMonitorMsgId.EXPORTER_MAPPING_ERROR);
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
        stopped.set(true);
        return telemetryItemExporter.shutdown();
    }
}
