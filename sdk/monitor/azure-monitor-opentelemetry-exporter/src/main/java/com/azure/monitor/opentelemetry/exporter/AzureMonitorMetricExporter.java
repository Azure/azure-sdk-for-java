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
    private static final OperationLogger exportingMetricLogger =
        new OperationLogger(AzureMonitorMetricExporter.class, "Exporting metric");
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
        return AggregationTemporalitySelector.deltaPreferred()
            .getAggregationTemporality(instrumentType);
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
                exportingMetricLogger.recordSuccess();
            } catch (Throwable t) {
                exportingMetricLogger.recordFailure(t.getMessage(), t, EXPORTER_MAPPING_ERROR);
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
