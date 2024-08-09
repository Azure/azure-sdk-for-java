// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.EXPORTER_MAPPING_ERROR;

/**
 * This class is an implementation of OpenTelemetry {@link MetricExporter}
 */
class AzureMonitorMetricExporter implements MetricExporter {

    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorMetricExporter.class);
    private static final OperationLogger OPERATION_LOGGER
        = new OperationLogger(AzureMonitorMetricExporter.class, "Exporting metric");

    private final AtomicBoolean stopped = new AtomicBoolean();
    private final MetricDataMapper mapper;
    private final TelemetryItemExporter telemetryItemExporter;

    /**
     * Creates an instance of metric exporter that is configured with given exporter client that sends
     * metrics to Application Insights resource identified by the instrumentation key.
     */
    AzureMonitorMetricExporter(MetricDataMapper mapper, TelemetryItemExporter telemetryItemExporter) {
        this.mapper = mapper;
        this.telemetryItemExporter = telemetryItemExporter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return AggregationTemporalitySelector.deltaPreferred().getAggregationTemporality(instrumentType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
        if (stopped.get()) {
            return CompletableResultCode.ofFailure();
        }

        List<TelemetryItem> telemetryItems = new ArrayList<>();
        for (MetricData metricData : metrics) {
            LOGGER.verbose("exporting metric: {}", metricData);
            try {
                mapper.map(metricData, telemetryItems::add);
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
        stopped.set(true);
        return telemetryItemExporter.shutdown();
    }
}
