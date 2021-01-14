package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODOs
 *  - Initialize the metrics reporter
 */
public class CtlWorkload {
    private static final Logger LOGGER = LoggerFactory.getLogger(CtlWorkload.class);

    private final Configuration _configuration;
    private final CosmosAsyncClient _client;
    private final CosmosAsyncClient _bulkLoadClient;
    private final ResourceManager _resourceManager;
    private final Map<Key, ObjectNode> _testData;
    private final DataLoader _dataLoader;
    private final GetTestRunner _getTestRunner;

    public CtlWorkload(final Configuration configuration) {
        Preconditions.checkNotNull(configuration, "The Workload configuration defining the parameters can not be null");

        _configuration = configuration;
        _client = AsyncClientFactory.buildAsyncClient(configuration);
        _bulkLoadClient = AsyncClientFactory.buildBulkLoadAsyncClient(configuration);
        _resourceManager = new ResourceManager(_configuration, _client);
        _testData = DataGenerator.createInvitationRecords(_configuration.getNumberOfPreCreatedDocuments());
        _dataLoader = new DataLoader(_configuration, _bulkLoadClient);
        _getTestRunner = new GetTestRunner(_configuration, _client);
    }

    public void setup() throws CosmosException {
        LOGGER.info("Initializing Database");
        _resourceManager.initializeDatabase();
        LOGGER.info("Initializing Container");
        _resourceManager.createContainer();
        LOGGER.info("Loading data");
        _dataLoader.loadData(_testData);
    }

    public void run() {
        LOGGER.info("Executing the Get test");
        _getTestRunner.run(_testData);
    }

    public void shutdown() {
        _resourceManager.deleteResources();
    }
}
