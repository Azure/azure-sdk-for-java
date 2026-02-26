// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically samples Reactor Netty connection pool metrics from a Micrometer
 * {@link MeterRegistry} and writes them to a CSV file.
 *
 * <p>Metrics captured (when {@code ConnectionProvider.metrics(true)} is enabled):</p>
 * <ul>
 *   <li>{@code reactor.netty.connection.provider.total.connections}</li>
 *   <li>{@code reactor.netty.connection.provider.active.connections}</li>
 *   <li>{@code reactor.netty.connection.provider.idle.connections}</li>
 *   <li>{@code reactor.netty.connection.provider.pending.connections}</li>
 *   <li>{@code reactor.netty.connection.provider.max.connections}</li>
 *   <li>{@code reactor.netty.connection.provider.max.pending.connections}</li>
 * </ul>
 *
 * <p>CSV columns: timestamp, metric, pool_id, pool_name, remote_address, value</p>
 */
public class NettyHttpMetricsReporter {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpMetricsReporter.class);
    private static final String METRIC_PREFIX = "reactor.netty.connection.provider";
    private static final String CSV_HEADER = "timestamp,metric,pool_id,pool_name,remote_address,value";

    private final MeterRegistry registry;
    private final Path outputFile;
    private final ScheduledExecutorService scheduler;
    private BufferedWriter writer;

    /**
     * @param registry   the Micrometer registry to query (typically {@code Metrics.globalRegistry})
     * @param outputDir  directory to write the CSV file into
     */
    public NettyHttpMetricsReporter(MeterRegistry registry, Path outputDir) {
        this.registry = registry;
        this.outputFile = outputDir.resolve("netty-pool-metrics.csv");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "netty-metrics-reporter");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Start periodic reporting.
     *
     * @param interval reporting interval
     * @param unit     time unit
     */
    public void start(long interval, TimeUnit unit) {
        try {
            Files.createDirectories(outputFile.getParent());
            writer = Files.newBufferedWriter(outputFile,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            writer.write(CSV_HEADER);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            logger.error("Failed to create netty pool metrics CSV: {}", outputFile, e);
            return;
        }

        scheduler.scheduleAtFixedRate(this::report, interval, interval, unit);
        logger.info("NettyHttpMetricsReporter started -> {} (every {}s)", outputFile, unit.toSeconds(interval));
    }

    /**
     * Write a single snapshot of all pool metrics to CSV.
     */
    public void report() {
        if (writer == null) return;

        String timestamp = Instant.now().toString();
        int count = 0;

        try {
            for (Meter meter : registry.getMeters()) {
                String name = meter.getId().getName();
                if (!name.startsWith(METRIC_PREFIX)) continue;

                // Only report gauge-type metrics (connections counts)
                if (!(meter instanceof Gauge)) continue;

                double value = ((Gauge) meter).value();
                String poolId = meter.getId().getTag("id");
                String poolName = meter.getId().getTag("name");
                String remoteAddr = meter.getId().getTag("remote.address");

                // Strip the common prefix for shorter metric names in CSV
                String shortName = name.substring(METRIC_PREFIX.length() + 1);

                writer.write(String.format("%s,%s,%s,%s,%s,%.0f",
                    timestamp, shortName,
                    poolId != null ? poolId : "",
                    poolName != null ? poolName : "",
                    remoteAddr != null ? remoteAddr : "",
                    value));
                writer.newLine();
                count++;
            }
            writer.flush();
        } catch (IOException e) {
            logger.warn("Failed to write netty pool metrics: {}", e.getMessage());
        }

        if (count > 0) {
            logger.debug("NettyHttpMetricsReporter: wrote {} pool metrics", count);
        }
    }

    /**
     * Stop the reporter and close the CSV file.
     */
    public void stop() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Final snapshot
        report();

        if (writer != null) {
            try {
                writer.close();
                logger.info("NettyHttpMetricsReporter stopped. Output: {}", outputFile);
            } catch (IOException e) {
                logger.warn("Failed to close netty pool metrics CSV", e);
            }
        }
    }
}