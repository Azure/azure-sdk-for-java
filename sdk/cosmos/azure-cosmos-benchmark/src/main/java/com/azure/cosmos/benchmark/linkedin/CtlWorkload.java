package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODOs
 *  - Initialize the reporter
 */
public class CtlWorkload {
    private static final Logger LOGGER = LoggerFactory.getLogger(CtlWorkload.class);

    private final Configuration _configuration;
    private final CosmosAsyncClient _client;
    private final ResourceManager _resourceManager;
    private final DataLoader _dataLoader;
    private final Semaphore _concurrencyControlSemaphore;

    public CtlWorkload(final Configuration configuration) {
        Preconditions.checkNotNull(configuration, "The Workload configuration defining the parameters can not be null");

        _configuration = configuration;
        _client = AsyncClientFactory.buildAsyncClient(configuration);
        _resourceManager = new ResourceManager(configuration, _client);
        _dataLoader = new DataLoader(configuration, _client);
        _concurrencyControlSemaphore = new Semaphore(configuration.getConcurrency());
    }

    public void setup() throws CosmosException {
        _resourceManager.initializeDatabase();
        _resourceManager.createContainers();
        final Map<Key, ObjectNode> invitationRecords =
            DataGenerator.createInvitationRecords(_configuration.getNumberOfPreCreatedDocuments());
        _dataLoader.loadData(invitationRecords);
    }

    public void run() {
    }

    public void shutdown() {
        _resourceManager.deleteResources();
    }
}
