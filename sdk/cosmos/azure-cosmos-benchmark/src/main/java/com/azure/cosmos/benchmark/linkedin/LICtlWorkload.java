// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.BenchmarkConfig;
import com.azure.cosmos.benchmark.ScheduledReporterFactory;
import com.azure.cosmos.benchmark.TenantWorkloadConfig;
import com.azure.cosmos.benchmark.linkedin.data.EntityConfiguration;
import com.azure.cosmos.benchmark.linkedin.data.InvitationsEntityConfiguration;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.google.common.base.Preconditions;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LICtlWorkload {
    private static final Logger LOGGER = LoggerFactory.getLogger(LICtlWorkload.class);

    /**
     * The test scenarios supported for the LinkedIn CTL Workload
     */
    public enum Scenario {
        GET,
        QUERY,
        COMPOSITE_READ
    }

    private final TenantWorkloadConfig _workloadConfig;
    private final BenchmarkConfig _benchConfig;
    private final EntityConfiguration _entityConfiguration;
    private final CosmosAsyncClient _client;
    private final CosmosAsyncClient _bulkLoadClient;
    private final MetricRegistry _metricsRegistry;
    private final ScheduledReporter _reporter;
    private final ResourceManager _resourceManager;
    private final DataLoader _dataLoader;
    private final TestRunner _testRunner;

    public LICtlWorkload(final TenantWorkloadConfig workloadCfg, final BenchmarkConfig benchConfig) {
        Preconditions.checkNotNull(workloadCfg, "The Workload configuration defining the parameters can not be null");
        Preconditions.checkNotNull(benchConfig, "The benchmark configuration defining the parameters can not be null");

        _workloadConfig = workloadCfg;
        _benchConfig = benchConfig;
        _entityConfiguration = new InvitationsEntityConfiguration(workloadCfg);
        _client = AsyncClientFactory.buildAsyncClient(workloadCfg);
        _bulkLoadClient = AsyncClientFactory.buildBulkLoadAsyncClient(workloadCfg);
        _metricsRegistry =  new MetricRegistry();
        _reporter = ScheduledReporterFactory.create(_benchConfig, _metricsRegistry);
        _resourceManager = workloadCfg.shouldManageDatabase()
            ? new DatabaseResourceManager(workloadCfg, _entityConfiguration, _client)
            : new CollectionResourceManager(workloadCfg, _entityConfiguration, _client);
        _dataLoader = new DataLoader(workloadCfg, _entityConfiguration, _bulkLoadClient);
        _testRunner = createTestRunner(workloadCfg);
    }

    public void setup() throws CosmosException {
        if (_benchConfig.isEnableJvmStats()) {
            LOGGER.info("Enabling JVM stats collection");
            _metricsRegistry.register("gc", new GarbageCollectorMetricSet());
            _metricsRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
            _metricsRegistry.register("memory", new MemoryUsageGaugeSet());
        }

        LOGGER.info("Creating resources");
        _resourceManager.createResources();

        LOGGER.info("Loading data");
        _dataLoader.loadData();

        LOGGER.info("Data loading completed");
        _bulkLoadClient.close();

        _testRunner.init();
    }

    public void run() {
        LOGGER.info("Executing the CosmosDB test");
        _reporter.start(_benchConfig.getPrintingInterval(), TimeUnit.SECONDS);

        _testRunner.run();

        _reporter.report();
    }

    /**
     * Close all existing resources, from CosmosDB collections to open connections
     */
    public void shutdown() {
        _testRunner.cleanup();
        _resourceManager.deleteResources();
        _client.close();
        _reporter.close();
    }

    private TestRunner createTestRunner(TenantWorkloadConfig workloadCfg) {
        final Scenario scenario = Scenario.valueOf(workloadCfg.getTestScenario());
        switch (scenario) {
            case QUERY:
                return new QueryTestRunner(workloadCfg, _client, _metricsRegistry, _entityConfiguration);
            case COMPOSITE_READ:
                return new CompositeReadTestRunner(workloadCfg, _client, _metricsRegistry, _entityConfiguration);
            case GET:
            default:
                return new GetTestRunner(workloadCfg, _client, _metricsRegistry, _entityConfiguration);
        }
    }
}
