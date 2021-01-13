package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.azure.cosmos.benchmark.linkedin.impl.Accessor;
import com.azure.cosmos.benchmark.linkedin.impl.CosmosDBDataAccessor;
import com.azure.cosmos.benchmark.linkedin.impl.DocumentTransformer;
import com.azure.cosmos.benchmark.linkedin.impl.IdentityDocumentTransformer;
import com.azure.cosmos.benchmark.linkedin.impl.OperationsLogger;
import com.azure.cosmos.benchmark.linkedin.impl.ResponseHandler;
import com.azure.cosmos.benchmark.linkedin.impl.datalocator.StaticDataLocator;
import com.azure.cosmos.benchmark.linkedin.impl.keyextractor.IdentityKeyExtractor;
import com.azure.cosmos.benchmark.linkedin.impl.keyextractor.KeyExtractor;
import com.azure.cosmos.benchmark.linkedin.impl.metrics.MetricsFactory;
import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
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
    private final Map<Key, ObjectNode> _testDataMap;
    private final DataLoader _dataLoader;
    private final Accessor<String, JsonNode> _accessor;

    public CtlWorkload(final Configuration configuration) {
        Preconditions.checkNotNull(configuration, "The Workload configuration defining the parameters can not be null");

        _configuration = configuration;
        _client = AsyncClientFactory.buildAsyncClient(configuration);
        _resourceManager = new ResourceManager(configuration, _client);
        _testDataMap = DataGenerator.createInvitationRecords(_configuration.getNumberOfPreCreatedDocuments());
        _dataLoader = new DataLoader(configuration, _client);
        _accessor = createAccessor(_configuration, _client);
    }

    public void setup() throws CosmosException {
        LOGGER.info("Initializing Database");
        _resourceManager.initializeDatabase();
        LOGGER.info("Initializing Containers");
        _resourceManager.createContainers();
        LOGGER.info("Loading data");
        _dataLoader.loadData(_testDataMap);
    }

    public void run() {
    }

    public void shutdown() {
        _resourceManager.deleteResources();
    }

    private static Accessor<String, JsonNode> createAccessor(final Configuration configuration,
        final CosmosAsyncClient client) {

        final StaticDataLocator dataLocator = createDataLocator(configuration, client);
        final KeyExtractor<String> keyExtractor = new IdentityKeyExtractor(Function.identity(), Function.identity());
        final DocumentTransformer<JsonNode, JsonNode> documentTransformer = new IdentityDocumentTransformer<>();
        final Clock clock = Clock.systemUTC();
        return new CosmosDBDataAccessor<>(dataLocator,
            keyExtractor,
            new ResponseHandler<>(documentTransformer, keyExtractor),
            new MetricsFactory(clock),
            clock,
            new OperationsLogger(Duration.ofMillis(100)));
    }

    private static StaticDataLocator createDataLocator(Configuration configuration, CosmosAsyncClient client) {
        final CollectionKey collectionKey = new CollectionKey(configuration.getServiceEndpoint(),
            configuration.getDatabaseId(),
            configuration.getCollectionId());
        final CosmosAsyncDatabase database = client.getDatabase(configuration.getDatabaseId());
        final CosmosAsyncContainer container = database.getContainer(configuration.getCollectionId());
        return new StaticDataLocator(collectionKey, container);
    }
}
