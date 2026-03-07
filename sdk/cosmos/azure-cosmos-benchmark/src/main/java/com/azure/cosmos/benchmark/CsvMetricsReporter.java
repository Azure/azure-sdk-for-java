// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * CSV metrics reporter using the Dropwizard bridge.
 * Only used when reportingDestination = CSV.
 */
public class CsvMetricsReporter {

    private static final Logger logger = LoggerFactory.getLogger(CsvMetricsReporter.class);

    private final CsvReporter reporter;

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
