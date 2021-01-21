// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.ScheduledReporterFactory;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LICtlWorkload {
    private static final Logger LOGGER = LoggerFactory.getLogger(LICtlWorkload.class);

    private final Configuration _configuration;
    private final CosmosAsyncClient _client;
    private final CosmosAsyncClient _bulkLoadClient;
    private final MetricRegistry _metricsRegistry;
    private final ScheduledReporter _reporter;
    private final ResourceManager _resourceManager;
    private final Map<Key, ObjectNode> _testData;
    private final DataLoader _dataLoader;
    private final GetTestRunner _getTestRunner;

    public LICtlWorkload(final Configuration configuration) {
        Preconditions.checkNotNull(configuration, "The Workload configuration defining the parameters can not be null");

        _configuration = configuration;
        _client = AsyncClientFactory.buildAsyncClient(configuration);
        _bulkLoadClient = AsyncClientFactory.buildBulkLoadAsyncClient(configuration);
        _metricsRegistry =  new MetricRegistry();
        _reporter = ScheduledReporterFactory.create(_configuration, _metricsRegistry);
        _resourceManager = new ResourceManager(_configuration, _client);
        _testData = DataGenerator.createInvitationRecords(_configuration.getNumberOfPreCreatedDocuments());
        _dataLoader = new DataLoader(_configuration, _bulkLoadClient);
        _getTestRunner = new GetTestRunner(_configuration, _client, _metricsRegistry);
    }

    public void setup() throws CosmosException {
        LOGGER.info("Initializing Database");
        _resourceManager.initializeDatabase();
        LOGGER.info("Initializing Container");
        _resourceManager.createContainer();
        LOGGER.info("Loading data");
        _dataLoader.loadData(_testData);
        LOGGER.info("Data loading completed");
        _bulkLoadClient.close();
    }

    public void run() {
        LOGGER.info("Executing the Get test");
        _reporter.start(_configuration.getPrintingInterval(), TimeUnit.SECONDS);

        _getTestRunner.run(_testData);

        _reporter.report();
    }

    /**
     * Close all existing resources, from CosmosDB collections to open connections
     */
    public void shutdown() {
        _resourceManager.deleteResources();
        _client.close();
        _reporter.close();
    }
}
