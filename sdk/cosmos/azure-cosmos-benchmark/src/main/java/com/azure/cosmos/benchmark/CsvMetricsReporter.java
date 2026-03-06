// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Metrics reporter using Dropwizard's native {@link ConsoleReporter} or {@link CsvReporter}.
 *
 * <p>Reads from a {@link DropwizardBridgeMeterRegistry} which bridges Micrometer meters
 * (including SDK-emitted {@code cosmos.client.op.*} meters) to a Dropwizard {@link MetricRegistry}.</p>
 */
public class CsvMetricsReporter {

    private static final Logger logger = LoggerFactory.getLogger(CsvMetricsReporter.class);

    private final ScheduledReporter reporter;

    /**
     * Create a CSV metrics reporter that writes per-metric CSV files.
     */
    public CsvMetricsReporter(DropwizardBridgeMeterRegistry meterRegistry, String reportingDirectory) {
        MetricRegistry dropwizardRegistry = meterRegistry.getDropwizardRegistry();
        File dir = Paths.get(reportingDirectory, "metrics").toFile();
        dir.mkdirs();
        reporter = CsvReporter.forRegistry(dropwizardRegistry)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .build(dir);
        logger.info("CsvMetricsReporter started -> {}", dir);
    }

    /**
     * Create a console metrics reporter using Dropwizard's native ConsoleReporter.
     */
    public CsvMetricsReporter(DropwizardBridgeMeterRegistry meterRegistry) {
        MetricRegistry dropwizardRegistry = meterRegistry.getDropwizardRegistry();
        reporter = ConsoleReporter.forRegistry(dropwizardRegistry)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .build();
        logger.info("ConsoleReporter started");
    }

    public void start(long interval, TimeUnit unit) {
        reporter.start(interval, unit);
    }

    public void report() {
        reporter.report();
    }

    public void stop() {
        reporter.report();
        reporter.stop();
        reporter.close();
    }
}
