// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.metrics;

import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.impl.Metrics;
import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;
import java.time.Clock;
import java.util.concurrent.TimeUnit;
import org.mpierce.metrics.reservoir.hdrhistogram.HdrHistogramResetOnSnapshotReservoir;


public class MetricsImpl implements Metrics {

    private final Clock _clock;
    private final Meter _successMeter;
    private final Meter _failureMeter;
    private final Timer _latencyTimer;
    private final Meter _documentsNotFoundMeter;

    public MetricsImpl(final MetricRegistry metricsRegistry,
        final Clock clock,
        final CollectionKey collectionKey,
        final String operationName,
        final Configuration.Environment environment) {
        Preconditions.checkNotNull(metricsRegistry, "The MetricsRegistry can not be null");
        Preconditions.checkNotNull(clock, "Need a non-null Clock instance for latency tracking");
        Preconditions.checkNotNull(operationName, "The operation name can not be null");
        Preconditions.checkNotNull(collectionKey,
            "The CollectionKey can not be null. Gotta have a CollectionKey to perform any DocumentDB operations");

        _clock = clock;

        // The metric prefix is comprised of the environment, Operation type, and the collection type
        //      e.g. Daily GET Invitations
        final String metricPrefix = String.format("%s %s %s", environment.name(),
            operationName.toUpperCase(),
            collectionKey.getCollectionName());
        _successMeter = metricsRegistry.meter(metricPrefix + " Successful Operations");
        _failureMeter = metricsRegistry.meter(metricPrefix + " Unsuccessful Operations");
        _latencyTimer = metricsRegistry.register(metricPrefix + " Latency",
            new Timer(new HdrHistogramResetOnSnapshotReservoir()));
        _documentsNotFoundMeter = metricsRegistry.meter(metricPrefix + " Document NotFound Operations");
    }

    @Override
    public void logCounterMetric(final Type metricType) {
        switch (metricType) {
            case NOT_FOUND:
                _documentsNotFoundMeter.mark();
                break;
            case CALL_COUNT:
            default:
        }
    }

    @Override
    public void completed(long startTimeInMillis) {
        final long requestEndTime = _clock.millis();
        final long requestLatencyMs = requestEndTime - startTimeInMillis;
        _successMeter.mark();
        _latencyTimer.update(requestLatencyMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void error(long startTimeInMillis) {
        final long requestEndTime = _clock.millis();
        final long requestLatencyMs = requestEndTime - startTimeInMillis;
        _failureMeter.mark();
        _latencyTimer.update(requestLatencyMs, TimeUnit.MILLISECONDS);
    }

}
