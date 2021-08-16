// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.metrics;

import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.impl.Metrics;
import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;


public class MetricsFactory {
    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    private final MetricRegistry _metricsRegistry;
    private final Clock _clock;
    private final Configuration.Environment _environment;

    // Local cache enables reusing the same Metric instance
    //   {CollectionKey -> {OperationName -> Metrics} map}
    private final Map<CollectionKey, Map<String, Metrics>> _collectionKeyToMetricsMap;

    public MetricsFactory(final MetricRegistry metricsRegistry,
        final Clock clock,
        final Configuration.Environment environment) {
        Preconditions.checkNotNull(metricsRegistry, "The MetricsRegistry can not be null");
        Preconditions.checkNotNull(clock, "Need a non-null Clock instance for latency tracking");
        Preconditions.checkNotNull(environment, "Need a valid value for the CTL environment");

        _metricsRegistry = metricsRegistry;
        _clock = clock;
        _environment = environment;
        _collectionKeyToMetricsMap = new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY);
    }

    public Metrics getMetrics(@Nonnull final CollectionKey collectionKey, @Nonnull final String operationName) {
        Preconditions.checkNotNull(collectionKey, "The CollectionKey can not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(operationName),
            "The operationName can not be an empty string. Pass a non-empty operation name to fetch the Metrics");

        final Map<String, Metrics> operationMetricsMap = _collectionKeyToMetricsMap.computeIfAbsent(collectionKey,
            key -> new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY));

        return operationMetricsMap.computeIfAbsent(operationName,
            operation -> new MetricsImpl(_metricsRegistry, _clock, collectionKey, operation, _environment));
    }
}
