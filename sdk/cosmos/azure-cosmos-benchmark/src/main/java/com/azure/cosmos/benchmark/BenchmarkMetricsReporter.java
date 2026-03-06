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
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Periodically reports metrics from a {@link DropwizardBridgeMeterRegistry} using
 * Dropwizard's {@link CsvReporter} or {@link ConsoleReporter}.
 *
 * <p>This class is a thin wrapper that creates the appropriate Dropwizard reporter
 * for the underlying {@link MetricRegistry} of a {@link DropwizardBridgeMeterRegistry}.
 * SDK-emitted Micrometer meters are bridged to Dropwizard by the registry; this reporter
 * simply handles the periodic output.</p>
 */
public class BenchmarkMetricsReporter {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkMetricsReporter.class);

    private final ScheduledReporter reporter;

    /**
     * Create a benchmark metrics reporter.
     *
     * @param meterRegistry the Dropwizard bridge registry whose metrics to report
     * @param csvOutputDir  directory for CSV output; if null, uses console reporter
     */
    public BenchmarkMetricsReporter(DropwizardBridgeMeterRegistry meterRegistry, Path csvOutputDir) {
        MetricRegistry dropwizardRegistry = meterRegistry.getDropwizardRegistry();

        if (csvOutputDir != null) {
            File dir = csvOutputDir.toFile();
            dir.mkdirs();
            reporter = CsvReporter.forRegistry(dropwizardRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build(dir);
            logger.info("BenchmarkMetricsReporter started (CSV) -> {}", csvOutputDir);
        } else {
            reporter = ConsoleReporter.forRegistry(dropwizardRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build();
            logger.info("BenchmarkMetricsReporter started (console)");
        }
    }

    /**
     * Start periodic reporting.
     */
    public void start(long interval, TimeUnit unit) {
        reporter.start(interval, unit);
    }

    /**
     * Force a single report snapshot.
     */
    public void report() {
        reporter.report();
    }

    /**
     * Stop the reporter and close resources.
     */
    public void stop() {
        reporter.report();
        reporter.stop();
        reporter.close();
    }
}
