// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;


public class ScheduledReporterFactory {

    private ScheduledReporterFactory() {
    }

    /**
     * @param configuration CTL workload parameters
     * @param metricsRegistry MetricRegistry instance for tracking various execution metrics
     * @return ScheduledReporter for reporting the captured metrics
     */
    public static ScheduledReporter create(final Configuration configuration,
        final MetricRegistry metricsRegistry) {
        if (configuration.getGraphiteEndpoint() != null) {
            final Graphite graphite = new Graphite(new InetSocketAddress(
                configuration.getGraphiteEndpoint(),
                configuration.getGraphiteEndpointPort()));
            return GraphiteReporter.forRegistry(metricsRegistry)
                .prefixedWith(configuration.getOperationType().name())
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
        } else if (configuration.getReportingDirectory() != null) {
            return CsvReporter.forRegistry(metricsRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build(configuration.getReportingDirectory());
        } else {
            return ConsoleReporter.forRegistry(metricsRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build();
        }
    }
}
