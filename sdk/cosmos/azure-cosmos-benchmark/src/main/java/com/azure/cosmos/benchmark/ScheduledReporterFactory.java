// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import java.io.File;
import java.util.concurrent.TimeUnit;


public class ScheduledReporterFactory {

    private ScheduledReporterFactory() {
    }

    /**
     * @param benchConfig benchmark-level configuration (reporting directory)
     * @param metricsRegistry MetricRegistry instance for tracking various execution metrics
     * @return ScheduledReporter for reporting the captured metrics
     */
    public static ScheduledReporter create(final BenchmarkConfig benchConfig,
        final MetricRegistry metricsRegistry) {
        if (benchConfig.getReportingDirectory() != null) {
            return CsvReporter.forRegistry(metricsRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build(new File(benchConfig.getReportingDirectory()));
        } else {
            return ConsoleReporter.forRegistry(metricsRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build();
        }
    }
}
