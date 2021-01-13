package com.azure.cosmos.benchmark.linkedin.impl.metrics;

import com.azure.cosmos.benchmark.linkedin.impl.Metrics;
import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;
import com.google.common.base.Preconditions;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MetricsImpl implements Metrics {
    public final static String LATENCY_MS = "LatencyMs";
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsImpl.class);
    private static final String SEPARATOR = "-";
    private final Clock _clock;
    private final String _metricNamePrefix;
    private final String _latencyMetricName;

    public MetricsImpl(final Clock clock, final CollectionKey collectionKey, final String operation) {
        Preconditions.checkNotNull(operation, "The operation name can not be null");
        Preconditions.checkNotNull(collectionKey,
            "The CollectionKey can not be null. Gotta have a CollectionKey to perform any DocumentDB operations");

        _clock = Preconditions.checkNotNull(clock, "The Clock object can not be null");
        // All metrics are of the form "database-collection-operation-%s". Pre-creating the
        // prefix to save repeating formatting or concatenation
        _metricNamePrefix = collectionKey.getDatabaseName() + SEPARATOR
            + collectionKey.getCollectionName() + SEPARATOR
            + operation + SEPARATOR;
        _latencyMetricName = createMetricName(LATENCY_MS);
    }

    @Override
    public void logCounterMetric(String metricName) {
        // Intentional no-op for now
    }

    @Override
    public void completed(long startTimeInMillis) {
        final long requestEndTime = _clock.millis();
        final long requestLatencyMs = requestEndTime - startTimeInMillis;
        LOGGER.debug("Latency tracking for {}: Duration: {}ms", _latencyMetricName, requestLatencyMs);
    }

    private String createMetricName(final String metricName) {
        return _metricNamePrefix + metricName;
    }
}
