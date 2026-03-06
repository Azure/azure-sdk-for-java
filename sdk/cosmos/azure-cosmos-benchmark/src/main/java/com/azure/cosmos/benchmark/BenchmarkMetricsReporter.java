// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * A Micrometer {@link io.micrometer.core.instrument.MeterRegistry} backed by a Dropwizard
 * {@link MetricRegistry} with a {@link CsvReporter} or {@link ConsoleReporter}.
 *
 * <p>SDK-emitted Micrometer meters are automatically bridged to the Dropwizard registry,
 * which then leverages the battle-tested CSV/Console reporter for periodic output.
 * This gives us per-metric CSV files (one file per meter) with no custom reporting code.</p>
 */
public class BenchmarkMetricsReporter extends DropwizardMeterRegistry {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkMetricsReporter.class);

    private final ScheduledReporter reporter;

    /**
     * Create a benchmark metrics reporter.
     *
     * @param csvOutputDir directory for CSV output; if null, uses console reporter
     */
    public BenchmarkMetricsReporter(Path csvOutputDir) {
        super(createConfig(), new MetricRegistry(), HierarchicalNameMapper.DEFAULT, Clock.SYSTEM);

        MetricRegistry dropwizardRegistry = getDropwizardRegistry();

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

    @Override
    protected Double nullGaugeValue() {
        return Double.NaN;
    }

    @Override
    public void close() {
        stop();
        super.close();
    }

    private static DropwizardConfig createConfig() {
        return new DropwizardConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String prefix() {
                return "benchmark";
            }
        };
    }
}
