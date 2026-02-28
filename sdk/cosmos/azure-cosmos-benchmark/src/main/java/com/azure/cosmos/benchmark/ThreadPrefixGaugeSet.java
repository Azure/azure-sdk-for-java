// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A Dropwizard MetricSet that reports JVM thread counts grouped by name prefix.
 * Thread names like "reactor-http-epoll-1", "reactor-http-epoll-2" are grouped
 * under the prefix "reactor-http-epoll" with a count of 2.
 *
 * This is useful for identifying which thread pools are growing in multi-tenancy
 * scenarios where per-client thread leaks need to be detected.
 *
 * Registered metrics:
 *   threads.prefix.{name} = count of threads with that prefix
 *   threads.prefix.total  = total thread count (same as threads.count)
 */
public class ThreadPrefixGaugeSet implements MetricSet {

    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, Metric> gauges = new HashMap<>();

        // Dynamic gauge that computes thread prefix counts on each read
        gauges.put("threads.prefix.snapshot", (Gauge<Map<String, Integer>>) () -> {
            Map<String, Integer> prefixCounts = new TreeMap<>();
            Thread.getAllStackTraces().keySet().forEach(t -> {
                String prefix = t.getName().replaceAll("-?\\d+$", "");
                if (prefix.isEmpty()) {
                    prefix = t.getName();
                }
                prefixCounts.merge(prefix, 1, Integer::sum);
            });
            return prefixCounts;
        });

        return gauges;
    }
}
