package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;

import java.util.Collection;

public class QuickPulseMetricReader implements MetricReader {

    private volatile CollectionRegistration collectionRegistration = CollectionRegistration.noop();

    @Override
    public void register(CollectionRegistration registration) {
        // this should get (once) called when the OpenTelemetry SDK is created
        collectionRegistration = registration;
    }

    @Override
    public CompletableResultCode forceFlush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    // this will be called on-demand from Quick Pulse code
    Collection<MetricData> collectAllMetrics() {
        return collectionRegistration.collectAllMetrics();
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return AggregationTemporality.DELTA;
    }
}
