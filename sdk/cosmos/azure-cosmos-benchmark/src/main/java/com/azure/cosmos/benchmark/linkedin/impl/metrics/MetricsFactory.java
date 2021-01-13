package com.azure.cosmos.benchmark.linkedin.impl.metrics;

import com.azure.cosmos.benchmark.linkedin.impl.Metrics;
import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;
import com.google.common.base.Preconditions;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;


public class MetricsFactory {
    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    private final Clock _clock;
    private final Map<CollectionKey, Map<String, Metrics>> _collectionKeyToMetricsMap;

    public MetricsFactory(final Clock clock) {
        _clock = clock;
        _collectionKeyToMetricsMap = new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY);
    }

    public Metrics getMetrics(@Nonnull final CollectionKey collectionKey, @Nonnull final String operationName) {
        Preconditions.checkNotNull(collectionKey, "The CollectionKey can not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(operationName),
            "The operationName can not be an empty string. Pass a non-empty operation name to fetch the Metrics");

        final Map<String, Metrics> operationMetricsMap = _collectionKeyToMetricsMap.computeIfAbsent(collectionKey,
            key -> new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY));

        return operationMetricsMap.computeIfAbsent(operationName,
            key -> new MetricsImpl(_clock, collectionKey, key));
    }
}
