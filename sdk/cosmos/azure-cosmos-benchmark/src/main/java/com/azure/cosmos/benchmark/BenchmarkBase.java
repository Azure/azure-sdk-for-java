// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.ThroughputProperties;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BenchmarkBase {
    private final static Logger logger = LoggerFactory.getLogger(BenchmarkBase.class);

    protected final Configuration configuration;
    protected final Semaphore concurrencyControlSemaphore;

    protected final CosmosAsyncClient cosmosClient;
    protected final CosmosAsyncDatabase cosmosAsyncDatabase;
    protected boolean databaseCreated;

    protected final MetricRegistry metricsRegistry;
    protected final ScheduledReporter reporter;
    protected final AtomicBoolean warmupMode = new AtomicBoolean(false);

    public BenchmarkBase(Configuration configuration) {
        this.configuration = configuration;
        this.concurrencyControlSemaphore = new Semaphore(configuration.getConcurrency());
        this.cosmosClient = this.createCosmosClient();
        this.cosmosAsyncDatabase = this.createDatabase();

        this.reporter = this.initializeReporter();
        this.metricsRegistry = this.initializeMetricRegistry();
        this.initializeAzureMonitorMeterRegistry();
    }

    protected abstract void initializeMeters();
    protected abstract void resetMeters();
    protected abstract void run() throws Exception;
    protected abstract void shutdown();

    private CosmosAsyncClient createCosmosClient() {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(this.configuration.getServiceEndpoint())
            .key(this.configuration.getMasterKey())
            .consistencyLevel(this.configuration.getConsistencyLevel())
            .contentResponseOnWriteEnabled(Boolean.parseBoolean(this.configuration.isContentResponseOnWriteEnabled()));
        if (this.configuration.getConnectionMode().equals(ConnectionMode.DIRECT)) {
            cosmosClientBuilder = cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig());
        } else {
            GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
            gatewayConnectionConfig.setMaxConnectionPoolSize(this.configuration.getMaxConnectionPoolSize());
            cosmosClientBuilder = cosmosClientBuilder.gatewayMode(gatewayConnectionConfig);
        }

        return cosmosClientBuilder.buildAsyncClient();
    }

    private CosmosAsyncDatabase createDatabase() {
        CosmosAsyncDatabase database;
        try {
            database = cosmosClient.getDatabase(this.configuration.getDatabaseId());
            database.read().block();
        } catch (CosmosException e) {
            if (e.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND) {
                cosmosClient.createDatabase(this.configuration.getDatabaseId(), ThroughputProperties.createManualThroughput(this.configuration.getThroughput())).block();
                database = cosmosClient.getDatabase(this.configuration.getDatabaseId());
                logger.info("Database {} is created for this test", this.configuration.getDatabaseId());
                databaseCreated = true;
            } else {
                throw e;
            }
        }

        return database;
    }

    private void initializeAzureMonitorMeterRegistry() {
        MeterRegistry registry = configuration.getAzureMonitorMeterRegistry();

        if (registry != null) {
            BridgeInternal.monitorTelemetry(registry);
        }

        registry = configuration.getGraphiteMeterRegistry();

        if (registry != null) {
            BridgeInternal.monitorTelemetry(registry);
        }
    }

    protected void initializeMetersIfSkippedEnoughOperations(AtomicLong count) {
        if (configuration.getSkipWarmUpOperations() > 0) {
            if (count.get() >= configuration.getSkipWarmUpOperations()) {
                if (warmupMode.get()) {
                    synchronized (this) {
                        if (warmupMode.get()) {
                            logger.info("Warmup phase finished. Starting capturing perf numbers ....");
                            resetMeters();
                            initializeMeters();
                            reporter.start(configuration.getPrintingInterval(), TimeUnit.SECONDS);
                            warmupMode.set(false);
                        }
                    }
                }
            }
        }
    }

    public MetricRegistry initializeMetricRegistry() {
        MetricRegistry metricRegistry = new MetricRegistry();
        if (configuration.isEnableJvmStats()) {
            metricsRegistry.register("gc", new GarbageCollectorMetricSet());
            metricsRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
            metricsRegistry.register("memory", new MemoryUsageGaugeSet());
        }

        return metricRegistry;
    }

    private ScheduledReporter initializeReporter() {
        ScheduledReporter reporter;

        if (configuration.getGraphiteEndpoint() != null) {
            final Graphite graphite = new Graphite(new InetSocketAddress(
                configuration.getGraphiteEndpoint(),
                configuration.getGraphiteEndpointPort()));
            reporter = GraphiteReporter.forRegistry(metricsRegistry)
                .prefixedWith(configuration.getOperationType().name())
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
        } else if (configuration.getReportingDirectory() != null) {
            reporter = CsvReporter.forRegistry(metricsRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build(configuration.getReportingDirectory());
        } else {
            reporter = ConsoleReporter.forRegistry(metricsRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .build();
        }

        return reporter;
    }

    protected Mono sparsityMono(long i) {
        Duration duration = configuration.getSparsityWaitTime();
        if (duration != null && !duration.isZero()) {
            if (configuration.getSkipWarmUpOperations() > i) {
                // don't wait on the initial warm up time.
                duration = Duration.ofSeconds(0);
            }
        } else {
            duration = Duration.ofSeconds(0);
        }

        return Mono.delay(duration);
    }
}
