// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.implementation.cpu.CpuMemoryReader;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.SortedMap;
import java.util.Collections;

public class CosmosTotalResultReporter extends ScheduledReporter {
    private final static Logger LOGGER = LoggerFactory.getLogger(CosmosTotalResultReporter.class);
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final MetricRegistry resultRegistry = new MetricRegistry();
    private final Histogram successRate = resultRegistry.histogram("successRate");
    private final Histogram failureRate = resultRegistry.histogram("failureRate");
    private final Histogram medianLatency = resultRegistry.histogram("medianLatency");
    private final Histogram p99Latency = resultRegistry.histogram("p99Latency");

    private final Histogram cpuUsage = resultRegistry.histogram("cpuUsage");

    private final CpuMemoryReader cpuReader;

    private final CosmosContainer results;

    private final String operation;

    private final String testVariationName;

    private final String branchName;

    private final String commitId;

    private final int concurrency;

    private Instant lastRecorded;
    private long lastRecordedSuccessCount;
    private long lastRecordedFailureCount;

    public CosmosTotalResultReporter(
        MetricRegistry registry,
        TimeUnit rateUnit,
        TimeUnit durationUnit,
        MetricFilter filter,
        ScheduledExecutorService executor,
        boolean shutdownExecutorOnStop,
        Set<MetricAttribute> disabledMetricAttributes,
        CosmosContainer results,
        Configuration config) {
        super(registry, "cosmos-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, disabledMetricAttributes);

        this.lastRecorded = Instant.now();
        this.cpuReader = new CpuMemoryReader();
        this.results = results;
        if (config.isSync()) {
            this.operation = "SYNC_" + config.getOperationType().name();
        } else {
            this.operation = config.getOperationType().name();
        }
        this.testVariationName = config.getTestVariationName();
        this.branchName = config.getBranchName();
        this.commitId = config.getCommitId();
        this.concurrency = config.getConcurrency();
    }

    @Override
    public void stop() {
        super.stop();

        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnn")
            .withZone(ZoneId.from(ZoneOffset.UTC));

        Instant nowSnapshot = Instant.now();
        Snapshot successSnapshot = this.successRate.getSnapshot();
        Snapshot failureSnapshot = this.failureRate.getSnapshot();
        Snapshot medianLatencySnapshot = this.medianLatency.getSnapshot();
        Snapshot p99LatencySnapshot = this.p99Latency.getSnapshot();
        Snapshot cpuUsageSnapshot = this.cpuUsage.getSnapshot();

        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        String id = UUID.randomUUID().toString();
        doc.put("id", id);
        doc.put("TIMESTAMP", formatter.format(nowSnapshot).substring(0, 27));
        doc.put("Operation", this.operation);
        doc.put("TestVariationName", this.testVariationName);
        doc.put("BranchName", this.branchName);
        doc.put("CommitId", this.commitId);
        doc.put("Concurrency", this.concurrency);
        doc.put("CpuUsage", (cpuUsageSnapshot.get75thPercentile())/1000d);
        doc.put("SuccessRate", ((long)successSnapshot.get75thPercentile())/100d);
        doc.put("FailureRate", ((long)failureSnapshot.get75thPercentile())/100d);
        double p99 = new BigDecimal(Double.toString(p99LatencySnapshot.get75thPercentile()/100000000d))
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
        double median = new BigDecimal(Double.toString(medianLatencySnapshot.get75thPercentile()/100000000d))
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();

        doc.put("P99LatencyInMs", p99);
        doc.put("MedianLatencyInMs", median);

        results.createItem(doc, new PartitionKey(id), null);

        LOGGER.info("Final results uploaded to {} - {}", results.getId(), doc.toPrettyString());
    }

    @Override
    public void report(
        SortedMap<String, Gauge> gauges,
        SortedMap<String, Counter> counters,
        SortedMap<String, Histogram> histograms,
        SortedMap<String, Meter> meters,
        SortedMap<String, Timer> timers) {
        // We are only interested in success / failure rate and median and P99 latency for now

        Meter successMeter = meters.get(Configuration.SUCCESS_COUNTER_METER_NAME);
        Meter failureMeter = meters.get(Configuration.FAILURE_COUNTER_METER_NAME);
        Timer latencyTimer = timers.get(Configuration.LATENCY_METER_NAME);

        Instant nowSnapshot = Instant.now();

        double intervalInSeconds = Duration.between(lastRecorded, nowSnapshot).toMillis() / 1000;
        if (intervalInSeconds > 0) {
            this.cpuUsage.update((long)(100000d * cpuReader.getSystemWideCpuUsage()));

            long successSnapshot = successMeter.getCount();
            long failureSnapshot = failureMeter.getCount();
            lastRecorded = nowSnapshot;

            if (successSnapshot == 0 && failureSnapshot == 0) {
                return;
            }

            this.successRate.update((long)(100d * (successSnapshot - lastRecordedSuccessCount) / intervalInSeconds));
            this.failureRate.update((long)(100d * (failureSnapshot - lastRecordedFailureCount) / intervalInSeconds));
            lastRecordedSuccessCount = successSnapshot;
            lastRecordedFailureCount = failureSnapshot;

            Snapshot latencySnapshot = latencyTimer.getSnapshot();
            long medianSnapshot = (long) (100d * latencySnapshot.getMedian());
            if (medianSnapshot == 0L) {
                return;
            }

            this.medianLatency.update(medianSnapshot);
            this.p99Latency.update((long) (100d * latencySnapshot.get99thPercentile()));
        }
    }

    /**
     * Returns a new {@link Builder} for {@link CosmosTotalResultReporter}.
     *
     * @param registry the registry to report
     * @param resultsContainer the Cosmos DB container to write the results into
     * @param config the Configuration for the test run
     * @return a {@link Builder} instance for a {@link CosmosTotalResultReporter}
     */
    public static Builder forRegistry(MetricRegistry registry, CosmosContainer resultsContainer, Configuration config) {
        return new Builder(registry, resultsContainer, config);
    }

    /**
     * A builder for {@link CosmosTotalResultReporter} instances. Defaults to using the default locale and
     * time zone, writing to {@code System.out}, converting rates to events/second, converting
     * durations to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;

        private final CosmosContainer resultsContainer;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private final Configuration config;


        private Builder(MetricRegistry registry, CosmosContainer resultsContainer, Configuration config) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            this.disabledMetricAttributes = Collections.emptySet();
            this.resultsContainer = resultsContainer;
            this.config = config;
        }

        /**
         * Specifies whether the executor (used for reporting) will be stopped with same time with reporter.
         * Default value is true.
         * Setting this parameter to false, has the sense in combining with providing external managed executor via {@link #scheduleOn(ScheduledExecutorService)}.
         *
         * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
         * @return {@code this}
         */
        public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        /**
         * Specifies the executor to use while scheduling reporting of metrics.
         * Default value is null.
         * Null value leads to executor will be auto created on start.
         *
         * @param executor the executor to use while scheduling reporting of metrics.
         * @return {@code this}
         */
        public Builder scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or "m15").
         * See {@link MetricAttribute}.
         *
         * @param disabledMetricAttributes a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }

        /**
         * Builds a {@link CosmosTotalResultReporter} with the given properties.
         *
         * @return a {@link CosmosTotalResultReporter}
         */
        public CosmosTotalResultReporter build() {
            return new CosmosTotalResultReporter(registry,
                rateUnit,
                durationUnit,
                filter,
                executor,
                shutdownExecutorOnStop,
                disabledMetricAttributes,
                resultsContainer,
                config);
        }
    }
}
