// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.ScheduledReporterFactory;
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

    private final Configuration _configuration;
    private final EntityConfiguration _entityConfiguration;
    private final CosmosAsyncClient _client;
    private final CosmosAsyncClient _bulkLoadClient;
    private final MetricRegistry _metricsRegistry;
    private final ScheduledReporter _reporter;
    private final ResourceManager _resourceManager;
    private final DataLoader _dataLoader;
    private final TestRunner _testRunner;

    public LICtlWorkload(final Configuration configuration) {
        Preconditions.checkNotNull(configuration, "The Workload configuration defining the parameters can not be null");

        _configuration = configuration;
        _entityConfiguration = new InvitationsEntityConfiguration(configuration);
        _client = AsyncClientFactory.buildAsyncClient(configuration);
        _bulkLoadClient = AsyncClientFactory.buildBulkLoadAsyncClient(configuration);
        _metricsRegistry =  new MetricRegistry();
        _reporter = ScheduledReporterFactory.create(_configuration, _metricsRegistry);
        _resourceManager = _configuration.shouldManageDatabase()
            ? new DatabaseResourceManager(_configuration, _entityConfiguration, _client)
            : new CollectionResourceManager(_configuration, _entityConfiguration, _client);
        _dataLoader = new DataLoader(_configuration, _entityConfiguration, _bulkLoadClient);
        _testRunner = createTestRunner(_configuration);
    }

    public void setup() throws CosmosException {
        if (_configuration.isEnableJvmStats()) {
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
        _reporter.start(_configuration.getPrintingInterval(), TimeUnit.SECONDS);

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

    private TestRunner createTestRunner(Configuration configuration) {
        final Scenario scenario = Scenario.valueOf(configuration.getTestScenario());
        switch (scenario) {
            case QUERY:
                return new QueryTestRunner(_configuration, _client, _metricsRegistry, _entityConfiguration);
            case COMPOSITE_READ:
                return new CompositeReadTestRunner(_configuration, _client, _metricsRegistry, _entityConfiguration);
            case GET:
            default:
                return new GetTestRunner(_configuration, _client, _metricsRegistry, _entityConfiguration);
        }
    }
}
