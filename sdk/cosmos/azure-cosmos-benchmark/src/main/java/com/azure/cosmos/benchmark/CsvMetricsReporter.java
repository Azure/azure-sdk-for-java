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
 * CSV metrics reporter that writes per-metric CSV files using the Dropwizard {@link CsvReporter}.
 *
 * <p>Reads from a {@link DropwizardBridgeMeterRegistry} which bridges Micrometer meters
 * (including SDK-emitted {@code cosmos.client.op.*} meters) to a Dropwizard {@link MetricRegistry}.
 * The CsvReporter creates one CSV file per metric in the output directory.</p>
 */
public class CsvMetricsReporter {

    private static final Logger logger = LoggerFactory.getLogger(CsvMetricsReporter.class);

    private final CsvReporter reporter;

    /**
     * Create a CSV metrics reporter.
     *
     * @param meterRegistry      the Dropwizard bridge registry whose metrics to report
     * @param reportingDirectory base reporting directory; CSV files are written
     *                           to a {@code metrics/} subdirectory.
     */
    public CsvMetricsReporter(DropwizardBridgeMeterRegistry meterRegistry, String reportingDirectory) {
        MetricRegistry dropwizardRegistry = meterRegistry.getDropwizardRegistry();
        File dir = Paths.get(reportingDirectory, "metrics").toFile();
        dir.mkdirs();
        reporter = CsvReporter.forRegistry(dropwizardRegistry)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .convertRatesTo(TimeUnit.SECONDS)
            .build(dir);
        logger.info("CsvMetricsReporter started (CSV) -> {}", dir);
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
