// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A Micrometer {@link MeterBinder} that reports JVM thread counts grouped by name prefix.
 * Thread names like "reactor-http-epoll-1", "reactor-http-epoll-2" are grouped
 * under the prefix "reactor-http-epoll" with a count of 2.
 *
 * <p>This is useful for identifying which thread pools are growing in multi-tenancy
 * scenarios where per-client thread leaks need to be detected.</p>
 *
 * <p>Registered metrics:</p>
 * <ul>
 *   <li>{@code jvm.threads.prefix} — one gauge per thread name prefix, tagged with {@code prefix=...}</li>
 * </ul>
 */
public class ThreadPrefixGaugeSet implements MeterBinder {

    private static final String METRIC_NAME = "jvm.threads.prefix";
    private static final int DEFAULT_REFRESH_INTERVAL_SECONDS = 10;

    private final int refreshIntervalSeconds;
    private ScheduledExecutorService scheduler;

    public ThreadPrefixGaugeSet() {
        this(DEFAULT_REFRESH_INTERVAL_SECONDS);
    }

    public ThreadPrefixGaugeSet(int refreshIntervalSeconds) {
        this.refreshIntervalSeconds = refreshIntervalSeconds;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        MultiGauge multiGauge = MultiGauge.builder(METRIC_NAME)
            .description("Thread count grouped by name prefix")
            .register(registry);

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "thread-prefix-gauge-updater");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            Map<String, Integer> prefixCounts = computePrefixCounts();
            multiGauge.register(
                prefixCounts.entrySet().stream()
                    .map(e -> MultiGauge.Row.of(Tags.of("prefix", e.getKey()), e.getValue()))
                    .collect(Collectors.toList()),
                true // overwrite previous rows
            );
        }, 0, refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    public void close() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private Map<String, Integer> computePrefixCounts() {
        Map<String, Integer> prefixCounts = new TreeMap<>();
        Thread.getAllStackTraces().keySet().forEach(t -> {
            String prefix = t.getName().replaceAll("-?\\d+$", "");
            if (prefix.isEmpty()) {
                prefix = t.getName();
            }
            prefixCounts.merge(prefix, 1, Integer::sum);
        });
        return prefixCounts;
    }
}
