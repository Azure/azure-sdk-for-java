// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Benchmark;
import com.azure.cosmos.benchmark.TenantWorkloadConfig;
import com.azure.cosmos.benchmark.linkedin.data.EntityConfiguration;
import com.azure.cosmos.benchmark.linkedin.data.InvitationsEntityConfiguration;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LICtlWorkload implements Benchmark {
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
    private final EntityConfiguration _entityConfiguration;
    private final CosmosAsyncClient _client;
    private final CosmosAsyncClient _bulkLoadClient;
    private final ResourceManager _resourceManager;
    private final DataLoader _dataLoader;
    private final TestRunner _testRunner;

    public LICtlWorkload(final TenantWorkloadConfig workloadCfg) {
        Preconditions.checkNotNull(workloadCfg, "The Workload configuration defining the parameters can not be null");

        _workloadConfig = workloadCfg;
        _entityConfiguration = new InvitationsEntityConfiguration(workloadCfg);
        _client = AsyncClientFactory.buildAsyncClient(workloadCfg);
        _bulkLoadClient = AsyncClientFactory.buildBulkLoadAsyncClient(workloadCfg);
        _resourceManager = workloadCfg.shouldManageDatabase()
            ? new DatabaseResourceManager(workloadCfg, _entityConfiguration, _client)
            : new CollectionResourceManager(workloadCfg, _entityConfiguration, _client);
        _dataLoader = new DataLoader(workloadCfg, _entityConfiguration, _bulkLoadClient);
        _testRunner = createTestRunner(workloadCfg);
    }

    public void run() {
        LOGGER.info("Setting up the LinkedIn ctl workload");

        LOGGER.info("Creating resources");
        _resourceManager.createResources();

        LOGGER.info("Loading data");
        _dataLoader.loadData();

        LOGGER.info("Data loading completed");
        _bulkLoadClient.close();

        _testRunner.init();

        LOGGER.info("Executing the CosmosDB test");
        _testRunner.run();
    }

    public void shutdown() {
        _testRunner.cleanup();
        if (_workloadConfig.isSuppressCleanup()) {
            LOGGER.info("Skipping cleanup of resources (suppressCleanup=true)");
        } else {
            _resourceManager.deleteResources();
        }
        _client.close();
    }

    private TestRunner createTestRunner(TenantWorkloadConfig workloadCfg) {
        final Scenario scenario = Scenario.valueOf(workloadCfg.getTestScenario());
        switch (scenario) {
            case QUERY:
                return new QueryTestRunner(workloadCfg, _client, _entityConfiguration);
            case COMPOSITE_READ:
                return new CompositeReadTestRunner(workloadCfg, _client, _entityConfiguration);
            case GET:
            default:
                return new GetTestRunner(workloadCfg, _client, _entityConfiguration);
        }
    }
}
