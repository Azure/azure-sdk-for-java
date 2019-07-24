// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CompositePath;
import com.azure.data.cosmos.CompositePathSortOrder;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosBridgeInternal;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosClientTest;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosDatabaseProperties;
import com.azure.data.cosmos.CosmosDatabaseResponse;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.CosmosResponse;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosStoredProcedureRequestOptions;
import com.azure.data.cosmos.CosmosUser;
import com.azure.data.cosmos.CosmosUserProperties;
import com.azure.data.cosmos.DataType;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.IncludedPath;
import com.azure.data.cosmos.Index;
import com.azure.data.cosmos.IndexingPolicy;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.RetryOptions;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.PathParser;
import com.azure.data.cosmos.internal.TestConfigurations;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.directconnectivity.Protocol;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import io.reactivex.subscribers.TestSubscriber;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.azure.data.cosmos.BridgeInternal.extractConfigs;
import static com.azure.data.cosmos.BridgeInternal.injectConfigs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

public class TestSuiteBase extends CosmosClientTest {

    private static final int DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL = 500;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected static Logger logger = LoggerFactory.getLogger(TestSuiteBase.class.getSimpleName());
    protected static final int TIMEOUT = 40000;
    protected static final int FEED_TIMEOUT = 40000;
    protected static final int SETUP_TIMEOUT = 60000;
    protected static final int SHUTDOWN_TIMEOUT = 12000;

    protected static final int SUITE_SETUP_TIMEOUT = 120000;
    protected static final int SUITE_SHUTDOWN_TIMEOUT = 60000;

    protected static final int WAIT_REPLICA_CATCH_UP_IN_MILLIS = 4000;

    protected final static ConsistencyLevel accountConsistency;
    protected static final ImmutableList<String> preferredLocations;
    private static final ImmutableList<ConsistencyLevel> desiredConsistencies;
    private static final ImmutableList<Protocol> protocols;

    protected int subscriberValidationTimeout = TIMEOUT;

    private static CosmosDatabase SHARED_DATABASE;
    private static CosmosContainer SHARED_MULTI_PARTITION_COLLECTION;
    private static CosmosContainer SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES;
    private static CosmosContainer SHARED_SINGLE_PARTITION_COLLECTION;

    public TestSuiteBase(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    protected static CosmosDatabase getSharedCosmosDatabase(CosmosClient client) {
        return CosmosBridgeInternal.getCosmosDatabaseWithNewClient(SHARED_DATABASE, client);
    }
    
    protected static CosmosContainer getSharedMultiPartitionCosmosContainer(CosmosClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_MULTI_PARTITION_COLLECTION, SHARED_DATABASE, client);
    }

    protected static CosmosContainer getSharedMultiPartitionCosmosContainerWithCompositeAndSpatialIndexes(CosmosClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES, SHARED_DATABASE, client);
    }

    protected static CosmosContainer getSharedSinglePartitionCosmosContainer(CosmosClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_SINGLE_PARTITION_COLLECTION, SHARED_DATABASE, client);
    }

    static {
        accountConsistency = parseConsistency(TestConfigurations.CONSISTENCY);
        desiredConsistencies = immutableListOrNull(
                ObjectUtils.defaultIfNull(parseDesiredConsistencies(TestConfigurations.DESIRED_CONSISTENCIES),
                                          allEqualOrLowerConsistencies(accountConsistency)));
        preferredLocations = immutableListOrNull(parsePreferredLocation(TestConfigurations.PREFERRED_LOCATIONS));
        protocols = ObjectUtils.defaultIfNull(immutableListOrNull(parseProtocols(TestConfigurations.PROTOCOLS)),
                                              ImmutableList.of(Protocol.HTTPS, Protocol.TCP));

        //  Object mapper configurations
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
        objectMapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
    }

    protected TestSuiteBase() {
        logger.debug("Initializing {} ...", this.getClass().getSimpleName());
    }

    private static <T> ImmutableList<T> immutableListOrNull(List<T> list) {
        return list != null ? ImmutableList.copyOf(list) : null;
    }

    private static class DatabaseManagerImpl implements CosmosDatabaseForTest.DatabaseManager {
        public static DatabaseManagerImpl getInstance(CosmosClient client) {
            return new DatabaseManagerImpl(client);
        }

        private final CosmosClient client;

        private DatabaseManagerImpl(CosmosClient client) {
            this.client = client;
        }

        @Override
        public Flux<FeedResponse<CosmosDatabaseProperties>> queryDatabases(SqlQuerySpec query) {
            return client.queryDatabases(query, null);
        }

        @Override
        public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseDefinition) {
            return client.createDatabase(databaseDefinition);
        }

        @Override
        public CosmosDatabase getDatabase(String id) {
            return client.getDatabase(id);
        }
    }

    @BeforeSuite(groups = {"simple", "long", "direct", "multi-master", "emulator", "non-emulator"}, timeOut = SUITE_SETUP_TIMEOUT)
    public static void beforeSuite() {

        logger.info("beforeSuite Started");

        try (CosmosClient houseKeepingClient = createGatewayHouseKeepingDocumentClient().build()) {
            CosmosDatabaseForTest dbForTest = CosmosDatabaseForTest.create(DatabaseManagerImpl.getInstance(houseKeepingClient));
            SHARED_DATABASE = dbForTest.createdDatabase;
            CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
            SHARED_MULTI_PARTITION_COLLECTION = createCollection(SHARED_DATABASE, getCollectionDefinitionWithRangeRangeIndex(), options, 10100);
            SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES = createCollection(SHARED_DATABASE, getCollectionDefinitionMultiPartitionWithCompositeAndSpatialIndexes(), options);
            SHARED_SINGLE_PARTITION_COLLECTION = createCollection(SHARED_DATABASE, getCollectionDefinitionWithRangeRangeIndex(), options, 6000);
        }
    }

    @AfterSuite(groups = {"simple", "long", "direct", "multi-master", "emulator", "non-emulator"}, timeOut = SUITE_SHUTDOWN_TIMEOUT)
    public static void afterSuite() {

        logger.info("afterSuite Started");

        try (CosmosClient houseKeepingClient = createGatewayHouseKeepingDocumentClient().build()) {
            safeDeleteDatabase(SHARED_DATABASE);
            CosmosDatabaseForTest.cleanupStaleTestDatabases(DatabaseManagerImpl.getInstance(houseKeepingClient));
        }
    }

    protected static void truncateCollection(CosmosContainer cosmosContainer) {
        CosmosContainerProperties cosmosContainerProperties = cosmosContainer.read().block().properties();
        String cosmosContainerId = cosmosContainerProperties.id();
        logger.info("Truncating collection {} ...", cosmosContainerId);
        List<String> paths = cosmosContainerProperties.partitionKeyDefinition().paths();
        FeedOptions options = new FeedOptions();
        options.maxDegreeOfParallelism(-1);
        options.enableCrossPartitionQuery(true);
        options.maxItemCount(100);

        logger.info("Truncating collection {} documents ...", cosmosContainer.id());

        cosmosContainer.queryItems("SELECT * FROM root", options)
                       .publishOn(Schedulers.parallel())
                    .flatMap(page -> Flux.fromIterable(page.results()))
                        .flatMap(doc -> {

                            Object propertyValue = null;
                            if (paths != null && !paths.isEmpty()) {
                                List<String> pkPath = PathParser.getPathParts(paths.get(0));
                                propertyValue = doc.getObjectByPath(pkPath);
                                if (propertyValue == null) {
                                    propertyValue = PartitionKey.None;
                                }

                            }
                            return cosmosContainer.getItem(doc.id(), propertyValue).delete();
                    }).then().block();
        logger.info("Truncating collection {} triggers ...", cosmosContainerId);

        cosmosContainer.getScripts().queryTriggers("SELECT * FROM root", options)
                       .publishOn(Schedulers.parallel())
                .flatMap(page -> Flux.fromIterable(page.results()))
                .flatMap(trigger -> {
//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = trigger.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                    }

                        return cosmosContainer.getScripts().getTrigger(trigger.id()).delete();
                    }).then().block();

        logger.info("Truncating collection {} storedProcedures ...", cosmosContainerId);

        cosmosContainer.getScripts().queryStoredProcedures("SELECT * FROM root", options)
                       .publishOn(Schedulers.parallel())
                .flatMap(page -> Flux.fromIterable(page.results()))
                .flatMap(storedProcedure -> {

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = storedProcedure.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                    }

                    return cosmosContainer.getScripts().getStoredProcedure(storedProcedure.id()).delete(new CosmosStoredProcedureRequestOptions());
                    }).then().block();

        logger.info("Truncating collection {} udfs ...", cosmosContainerId);

        cosmosContainer.getScripts().queryUserDefinedFunctions("SELECT * FROM root", options)
                       .publishOn(Schedulers.parallel())
                .flatMap(page -> Flux.fromIterable(page.results()))
                .flatMap(udf -> {

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = udf.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                    }

                    return cosmosContainer.getScripts().getUserDefinedFunction(udf.id()).delete();
                    }).then().block();

        logger.info("Finished truncating collection {}.", cosmosContainerId);
    }

    protected static void waitIfNeededForReplicasToCatchUp(CosmosClientBuilder clientBuilder) {
        switch (clientBuilder.consistencyLevel()) {
            case EVENTUAL:
            case CONSISTENT_PREFIX:
                logger.info(" additional wait in EVENTUAL mode so the replica catch up");
                // give times to replicas to catch up after a write
                try {
                    TimeUnit.MILLISECONDS.sleep(WAIT_REPLICA_CATCH_UP_IN_MILLIS);
                } catch (Exception e) {
                    logger.error("unexpected failure", e);
                }

            case SESSION:
            case BOUNDED_STALENESS:
            case STRONG:
            default:
                break;
        }
    }

    public static CosmosContainer createCollection(CosmosDatabase database, CosmosContainerProperties cosmosContainerProperties,
                                                   CosmosContainerRequestOptions options, int throughput) {
        return database.createContainer(cosmosContainerProperties, throughput, options).block().container();
    }

    public static CosmosContainer createCollection(CosmosDatabase database, CosmosContainerProperties cosmosContainerProperties,
            CosmosContainerRequestOptions options) {
        return database.createContainer(cosmosContainerProperties, options).block().container();
    }

    private static CosmosContainerProperties getCollectionDefinitionMultiPartitionWithCompositeAndSpatialIndexes() {
        final String NUMBER_FIELD = "numberField";
        final String STRING_FIELD = "stringField";
        final String NUMBER_FIELD_2 = "numberField2";
        final String STRING_FIELD_2 = "stringField2";
        final String BOOL_FIELD = "boolField";
        final String NULL_FIELD = "nullField";
        final String OBJECT_FIELD = "objectField";
        final String ARRAY_FIELD = "arrayField";
        final String SHORT_STRING_FIELD = "shortStringField";
        final String MEDIUM_STRING_FIELD = "mediumStringField";
        final String LONG_STRING_FIELD = "longStringField";
        final String PARTITION_KEY = "pk";

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        ArrayList<String> partitionKeyPaths = new ArrayList<String>();
        partitionKeyPaths.add("/" + PARTITION_KEY);
        partitionKeyDefinition.paths(partitionKeyPaths);

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDefinition);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<List<CompositePath>> compositeIndexes = new ArrayList<>();

        //Simple
        ArrayList<CompositePath> compositeIndexSimple = new ArrayList<CompositePath>();
        CompositePath compositePath1 = new CompositePath();
        compositePath1.path("/" + NUMBER_FIELD);
        compositePath1.order(CompositePathSortOrder.ASCENDING);

        CompositePath compositePath2 = new CompositePath();
        compositePath2.path("/" + STRING_FIELD);
        compositePath2.order(CompositePathSortOrder.DESCENDING);

        compositeIndexSimple.add(compositePath1);
        compositeIndexSimple.add(compositePath2);

        //Max Columns
        ArrayList<CompositePath> compositeIndexMaxColumns = new ArrayList<CompositePath>();
        CompositePath compositePath3 = new CompositePath();
        compositePath3.path("/" + NUMBER_FIELD);
        compositePath3.order(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath4 = new CompositePath();
        compositePath4.path("/" + STRING_FIELD);
        compositePath4.order(CompositePathSortOrder.ASCENDING);

        CompositePath compositePath5 = new CompositePath();
        compositePath5.path("/" + NUMBER_FIELD_2);
        compositePath5.order(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath6 = new CompositePath();
        compositePath6.path("/" + STRING_FIELD_2);
        compositePath6.order(CompositePathSortOrder.ASCENDING);

        compositeIndexMaxColumns.add(compositePath3);
        compositeIndexMaxColumns.add(compositePath4);
        compositeIndexMaxColumns.add(compositePath5);
        compositeIndexMaxColumns.add(compositePath6);

        //Primitive Values
        ArrayList<CompositePath> compositeIndexPrimitiveValues = new ArrayList<CompositePath>();
        CompositePath compositePath7 = new CompositePath();
        compositePath7.path("/" + NUMBER_FIELD);
        compositePath7.order(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath8 = new CompositePath();
        compositePath8.path("/" + STRING_FIELD);
        compositePath8.order(CompositePathSortOrder.ASCENDING);

        CompositePath compositePath9 = new CompositePath();
        compositePath9.path("/" + BOOL_FIELD);
        compositePath9.order(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath10 = new CompositePath();
        compositePath10.path("/" + NULL_FIELD);
        compositePath10.order(CompositePathSortOrder.ASCENDING);

        compositeIndexPrimitiveValues.add(compositePath7);
        compositeIndexPrimitiveValues.add(compositePath8);
        compositeIndexPrimitiveValues.add(compositePath9);
        compositeIndexPrimitiveValues.add(compositePath10);

        //Long Strings
        ArrayList<CompositePath> compositeIndexLongStrings = new ArrayList<CompositePath>();
        CompositePath compositePath11 = new CompositePath();
        compositePath11.path("/" + STRING_FIELD);

        CompositePath compositePath12 = new CompositePath();
        compositePath12.path("/" + SHORT_STRING_FIELD);

        CompositePath compositePath13 = new CompositePath();
        compositePath13.path("/" + MEDIUM_STRING_FIELD);

        CompositePath compositePath14 = new CompositePath();
        compositePath14.path("/" + LONG_STRING_FIELD);

        compositeIndexLongStrings.add(compositePath11);
        compositeIndexLongStrings.add(compositePath12);
        compositeIndexLongStrings.add(compositePath13);
        compositeIndexLongStrings.add(compositePath14);

        compositeIndexes.add(compositeIndexSimple);
        compositeIndexes.add(compositeIndexMaxColumns);
        compositeIndexes.add(compositeIndexPrimitiveValues);
        compositeIndexes.add(compositeIndexLongStrings);

        indexingPolicy.compositeIndexes(compositeIndexes);
        cosmosContainerProperties.indexingPolicy(indexingPolicy);

        return cosmosContainerProperties;
    }

    public static CosmosContainer createCollection(CosmosClient client, String dbId, CosmosContainerProperties collectionDefinition) {
        return client.getDatabase(dbId).createContainer(collectionDefinition).block().container();
    }

    public static void deleteCollection(CosmosClient client, String dbId, String collectionId) {
        client.getDatabase(dbId).getContainer(collectionId).delete().block();
    }

    public static CosmosItem createDocument(CosmosContainer cosmosContainer, CosmosItemProperties item) {
        return cosmosContainer.createItem(item).block().item();
    }

    private Flux<CosmosItemResponse> bulkInsert(CosmosContainer cosmosContainer,
                                                List<CosmosItemProperties> documentDefinitionList,
                                                int concurrencyLevel) {
        List<Mono<CosmosItemResponse>> result = new ArrayList<>(documentDefinitionList.size());
        for (CosmosItemProperties docDef : documentDefinitionList) {
            result.add(cosmosContainer.createItem(docDef));
        }

        return Flux.merge(Flux.fromIterable(result), concurrencyLevel);
    }
    public List<CosmosItemProperties> bulkInsertBlocking(CosmosContainer cosmosContainer,
                                                         List<CosmosItemProperties> documentDefinitionList) {
        return bulkInsert(cosmosContainer, documentDefinitionList, DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL)
                .publishOn(Schedulers.parallel())
                .map(CosmosItemResponse::properties)
                .collectList()
                .block();
    }

    public void voidBulkInsertBlocking(CosmosContainer cosmosContainer,
                                                         List<CosmosItemProperties> documentDefinitionList) {
        bulkInsert(cosmosContainer, documentDefinitionList, DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL)
            .publishOn(Schedulers.parallel())
            .map(CosmosItemResponse::properties)
            .then()
            .block();
    }

    public static CosmosUser createUser(CosmosClient client, String databaseId, CosmosUserProperties userSettings) {
        return client.getDatabase(databaseId).read().block().database().createUser(userSettings).block().user();
    }

    public static CosmosUser safeCreateUser(CosmosClient client, String databaseId, CosmosUserProperties user) {
        deleteUserIfExists(client, databaseId, user.id());
        return createUser(client, databaseId, user);
    }

    private static CosmosContainer safeCreateCollection(CosmosClient client, String databaseId, CosmosContainerProperties collection, CosmosContainerRequestOptions options) {
        deleteCollectionIfExists(client, databaseId, collection.id());
        return createCollection(client.getDatabase(databaseId), collection, options);
    }

    static protected CosmosContainerProperties getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        return collectionDefinition;
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithRangeRangeIndex() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.path("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.STRING);
        BridgeInternal.setProperty(stringIndex, "precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.NUMBER);
        BridgeInternal.setProperty(numberIndex, "precision", -1);
        indexes.add(numberIndex);
        includedPath.indexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        cosmosContainerProperties.indexingPolicy(indexingPolicy);

        return cosmosContainerProperties;
    }

    public static void deleteCollectionIfExists(CosmosClient client, String databaseId, String collectionId) {
        CosmosDatabase database = client.getDatabase(databaseId).read().block().database();
        List<CosmosContainerProperties> res = database.queryContainers(String.format("SELECT * FROM root r where r.id = '%s'", collectionId), null)
                                                      .flatMap(page -> Flux.fromIterable(page.results()))
                                                      .collectList()
                                                      .block();
        
        if (!res.isEmpty()) {
            deleteCollection(database, collectionId);
        }
    }

    public static void deleteCollection(CosmosDatabase cosmosDatabase, String collectionId) {
        cosmosDatabase.getContainer(collectionId).delete().block();
    }

    public static void deleteCollection(CosmosContainer cosmosContainer) {
        cosmosContainer.delete().block();
    }

    public static void deleteDocumentIfExists(CosmosClient client, String databaseId, String collectionId, String docId) {
        FeedOptions options = new FeedOptions();
        options.partitionKey(new PartitionKey(docId));
        CosmosContainer cosmosContainer = client.getDatabase(databaseId).read().block().database().getContainer(collectionId).read().block().container();
        List<CosmosItemProperties> res = cosmosContainer
                .queryItems(String.format("SELECT * FROM root r where r.id = '%s'", docId), options)
                .flatMap(page -> Flux.fromIterable(page.results()))
                .collectList().block();

        if (!res.isEmpty()) {
            deleteDocument(cosmosContainer, docId);
        }
    }

    public static void safeDeleteDocument(CosmosContainer cosmosContainer, String documentId, Object partitionKey) {
        if (cosmosContainer != null && documentId != null) {
            try {
                cosmosContainer.getItem(documentId, partitionKey).read().block().item().delete().block();
            } catch (Exception e) {
                CosmosClientException dce = Utils.as(e, CosmosClientException.class);
                if (dce == null || dce.statusCode() != 404) {
                    throw e;
                }
            }
        }
    }

    public static void deleteDocument(CosmosContainer cosmosContainer, String documentId) {
        cosmosContainer.getItem(documentId, PartitionKey.None).read().block().item().delete();
    }

    public static void deleteUserIfExists(CosmosClient client, String databaseId, String userId) {
        CosmosDatabase database = client.getDatabase(databaseId).read().block().database();
        List<CosmosUserProperties> res = database
                .queryUsers(String.format("SELECT * FROM root r where r.id = '%s'", userId), null)
                .flatMap(page -> Flux.fromIterable(page.results()))
                .collectList().block();
        if (!res.isEmpty()) {
            deleteUser(database, userId);
        }
    }

    public static void deleteUser(CosmosDatabase database, String userId) {
        database.getUser(userId).read().block().user().delete().block();
    }

    static private CosmosDatabase safeCreateDatabase(CosmosClient client, CosmosDatabaseProperties databaseSettings) {
        safeDeleteDatabase(client.getDatabase(databaseSettings.id()));
        return client.createDatabase(databaseSettings).block().database();
    }

    static protected CosmosDatabase createDatabase(CosmosClient client, String databaseId) {
        CosmosDatabaseProperties databaseSettings = new CosmosDatabaseProperties(databaseId);
        return client.createDatabase(databaseSettings).block().database();
    }

    static protected CosmosDatabase createDatabaseIfNotExists(CosmosClient client, String databaseId) {
        List<CosmosDatabaseProperties> res = client.queryDatabases(String.format("SELECT * FROM r where r.id = '%s'", databaseId), null)
                                                   .flatMap(p -> Flux.fromIterable(p.results()))
                                                   .collectList()
                                                   .block();
        if (res.size() != 0) {
            return client.getDatabase(databaseId).read().block().database();
        } else {
            CosmosDatabaseProperties databaseSettings = new CosmosDatabaseProperties(databaseId);
            return client.createDatabase(databaseSettings).block().database();
        }
    }

    static protected void safeDeleteDatabase(CosmosDatabase database) {
        if (database != null) {
            try {
                database.delete().block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeDeleteAllCollections(CosmosDatabase database) {
        if (database != null) {
            List<CosmosContainerProperties> collections = database.readAllContainers()
                                                                  .flatMap(p -> Flux.fromIterable(p.results()))
                                                                  .collectList()
                                                                  .block();

            for(CosmosContainerProperties collection: collections) {
                database.getContainer(collection.id()).delete().block();
            }
        }
    }

    static protected void safeDeleteCollection(CosmosContainer collection) {
        if (collection != null) {
            try {
                collection.delete().block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeDeleteCollection(CosmosDatabase database, String collectionId) {
        if (database != null && collectionId != null) {
            try {
                database.getContainer(collectionId).delete().block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeCloseAsync(CosmosClient client) {
        if (client != null) {
            new Thread(() -> {
                try {
                    client.close();
                } catch (Exception e) {
                    logger.error("failed to close client", e);
                }
            }).start();
        }
    }

    static protected void safeClose(CosmosClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                logger.error("failed to close client", e);
            }
        }
    }

    public <T extends CosmosResponse> void validateSuccess(Mono<T> single, CosmosResponseValidator<T> validator)
            throws InterruptedException {
        validateSuccess(single.flux(), validator, subscriberValidationTimeout);
    }

    public static <T extends CosmosResponse> void validateSuccess(Flux<T> flowable,
            CosmosResponseValidator<T> validator, long timeout) {

        TestSubscriber<T> testSubscriber = new TestSubscriber<>();

        flowable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }

    public <T extends Resource, U extends CosmosResponse> void validateFailure(Mono<U> mono, FailureValidator validator)
            throws InterruptedException {
        validateFailure(mono.flux(), validator, subscriberValidationTimeout);
    }

    public static <T extends Resource, U extends CosmosResponse> void validateFailure(Flux<U> flowable,
            FailureValidator validator, long timeout) throws InterruptedException {

        TestSubscriber<CosmosResponse> testSubscriber = new TestSubscriber<>();

        flowable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errors()).hasSize(1);
        validator.validate((Throwable) testSubscriber.getEvents().get(1).get(0));
    }

    public <T extends Resource> void validateQuerySuccess(Flux<FeedResponse<T>> flowable,
            FeedResponseListValidator<T> validator) {
        validateQuerySuccess(flowable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateQuerySuccess(Flux<FeedResponse<T>> flowable,
            FeedResponseListValidator<T> validator, long timeout) {

        TestSubscriber<FeedResponse<T>> testSubscriber = new TestSubscriber<>();

        flowable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        validator.validate(testSubscriber.values());
    }

    public <T extends Resource> void validateQueryFailure(Flux<FeedResponse<T>> flowable, FailureValidator validator) {
        validateQueryFailure(flowable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateQueryFailure(Flux<FeedResponse<T>> flowable,
            FailureValidator validator, long timeout) {

        TestSubscriber<FeedResponse<T>> testSubscriber = new TestSubscriber<>();

        flowable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.getEvents().get(1)).hasSize(1);
        validator.validate((Throwable) testSubscriber.getEvents().get(1).get(0));
    }

    @DataProvider
    public static Object[][] clientBuilders() {
        return new Object[][]{{createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null)}};
    }

    @DataProvider
    public static Object[][] clientBuildersWithSessionConsistency() {
        return new Object[][]{
            {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.HTTPS, false, null)},
            {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null)},
            {createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null)}
        };
    }

    static ConsistencyLevel parseConsistency(String consistency) {
        if (consistency != null) {
            consistency = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, consistency).trim();
            return ConsistencyLevel.valueOf(consistency);
        }

        logger.error("INVALID configured test consistency [{}].", consistency);
        throw new IllegalStateException("INVALID configured test consistency " + consistency);
    }

    static List<String> parsePreferredLocation(String preferredLocations) {
        if (StringUtils.isEmpty(preferredLocations)) {
            return null;
        }

        try {
            return objectMapper.readValue(preferredLocations, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            logger.error("INVALID configured test preferredLocations [{}].", preferredLocations);
            throw new IllegalStateException("INVALID configured test preferredLocations " + preferredLocations);
        }
    }

    static List<Protocol> parseProtocols(String protocols) {
        if (StringUtils.isEmpty(protocols)) {
            return null;
        }
        List<Protocol> protocolList = new ArrayList<>();
        try {
            List<String> protocolStrings = objectMapper.readValue(protocols, new TypeReference<List<String>>() {
            });
            for(String protocol : protocolStrings) {
                protocolList.add(Protocol.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, protocol)));
            }
            return protocolList;
        } catch (Exception e) {
            logger.error("INVALID configured test protocols [{}].", protocols);
            throw new IllegalStateException("INVALID configured test protocols " + protocols);
        }
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithDirect() {
        return simpleClientBuildersWithDirect(toArray(protocols));
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithDirectHttps() {
        return simpleClientBuildersWithDirect(Protocol.HTTPS);
    }

    private static Object[][] simpleClientBuildersWithDirect(Protocol... protocols) {
        logger.info("Max test consistency to use is [{}]", accountConsistency);
        List<ConsistencyLevel> testConsistencies = ImmutableList.of(ConsistencyLevel.EVENTUAL);
        
        boolean isMultiMasterEnabled = preferredLocations != null && accountConsistency == ConsistencyLevel.SESSION;

        List<CosmosClientBuilder> cosmosConfigurations = new ArrayList<>();

        for (Protocol protocol : protocols) {
            testConsistencies.forEach(consistencyLevel -> cosmosConfigurations.add(createDirectRxDocumentClient(consistencyLevel,
                                                                                                    protocol,
                                                                                                    isMultiMasterEnabled,
                                                                                                    preferredLocations)));
        }

        cosmosConfigurations.forEach(c -> logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
                                          c.connectionPolicy().connectionMode(),
                                          c.consistencyLevel(),
                                          extractConfigs(c).getProtocol()
        ));

        cosmosConfigurations.add(createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null));

        return cosmosConfigurations.stream().map(b -> new Object[]{b}).collect(Collectors.toList()).toArray(new Object[0][]);
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirect() {
        return clientBuildersWithDirectAllConsistencies(toArray(protocols));
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirectHttps() {
        return clientBuildersWithDirectAllConsistencies(Protocol.HTTPS);
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirectSession() {
        return clientBuildersWithDirectSession(toArray(protocols));
    }

    static Protocol[] toArray(List<Protocol> protocols) {
        return protocols.toArray(new Protocol[protocols.size()]);
    }

    private static Object[][] clientBuildersWithDirectSession(Protocol... protocols) {
        return clientBuildersWithDirect(new ArrayList<ConsistencyLevel>() {{
            add(ConsistencyLevel.SESSION);
        }}, protocols);
    }

    private static Object[][] clientBuildersWithDirectAllConsistencies(Protocol... protocols) {
        logger.info("Max test consistency to use is [{}]", accountConsistency);
        return clientBuildersWithDirect(desiredConsistencies, protocols);
    }

    static List<ConsistencyLevel> parseDesiredConsistencies(String consistencies) {
        if (StringUtils.isEmpty(consistencies)) {
            return null;
        }
        List<ConsistencyLevel> consistencyLevels = new ArrayList<>();
        try {
            List<String> consistencyStrings = objectMapper.readValue(consistencies, new TypeReference<List<String>>() {});
            for(String consistency : consistencyStrings) {
                consistencyLevels.add(ConsistencyLevel.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, consistency)));
            }
            return consistencyLevels;
        } catch (Exception e) {
            logger.error("INVALID consistency test desiredConsistencies [{}].", consistencies);
            throw new IllegalStateException("INVALID configured test desiredConsistencies " + consistencies);
        }
    }

    static List<ConsistencyLevel> allEqualOrLowerConsistencies(ConsistencyLevel accountConsistency) {
        List<ConsistencyLevel> testConsistencies = new ArrayList<>();
        switch (accountConsistency) {
        
            case STRONG:
                testConsistencies.add(ConsistencyLevel.STRONG);
            case BOUNDED_STALENESS:
                testConsistencies.add(ConsistencyLevel.BOUNDED_STALENESS);
            case SESSION:
                testConsistencies.add(ConsistencyLevel.SESSION);
            case CONSISTENT_PREFIX:
                testConsistencies.add(ConsistencyLevel.CONSISTENT_PREFIX);
            case EVENTUAL:
                testConsistencies.add(ConsistencyLevel.EVENTUAL);
                break;
            default:
                throw new IllegalStateException("INVALID configured test consistency " + accountConsistency);
        }
        return testConsistencies;
    }

    private static Object[][] clientBuildersWithDirect(List<ConsistencyLevel> testConsistencies, Protocol... protocols) {
        boolean isMultiMasterEnabled = preferredLocations != null && accountConsistency == ConsistencyLevel.SESSION;

        List<CosmosClientBuilder> cosmosConfigurations = new ArrayList<>();

        for (Protocol protocol : protocols) {
            testConsistencies.forEach(consistencyLevel -> cosmosConfigurations.add(createDirectRxDocumentClient(consistencyLevel,
                                                                                                    protocol,
                                                                                                    isMultiMasterEnabled,
                                                                                                    preferredLocations)));
        }

        cosmosConfigurations.forEach(c -> logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
                                          c.connectionPolicy().connectionMode(),
                                          c.consistencyLevel(),
                                          extractConfigs(c).getProtocol()
        ));

        cosmosConfigurations.add(createGatewayRxDocumentClient(ConsistencyLevel.SESSION, isMultiMasterEnabled, preferredLocations));

        return cosmosConfigurations.stream().map(c -> new Object[]{c}).collect(Collectors.toList()).toArray(new Object[0][]);
    }

    static protected CosmosClientBuilder createGatewayHouseKeepingDocumentClient() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        RetryOptions options = new RetryOptions();
        options.maxRetryWaitTimeInSeconds(SUITE_SETUP_TIMEOUT);
        connectionPolicy.retryOptions(options);
        return CosmosClient.builder().endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .connectionPolicy(connectionPolicy)
                .consistencyLevel(ConsistencyLevel.SESSION);
    }

    static protected CosmosClientBuilder createGatewayRxDocumentClient(ConsistencyLevel consistencyLevel, boolean multiMasterEnabled, List<String> preferredLocations) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.GATEWAY);
        connectionPolicy.usingMultipleWriteLocations(multiMasterEnabled);
        connectionPolicy.preferredLocations(preferredLocations);
        return CosmosClient.builder().endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .connectionPolicy(connectionPolicy)
                .consistencyLevel(consistencyLevel);
    }

    static protected CosmosClientBuilder createGatewayRxDocumentClient() {
        return createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null);
    }

    static protected CosmosClientBuilder createDirectRxDocumentClient(ConsistencyLevel consistencyLevel,
                                                                              Protocol protocol,
                                                                              boolean multiMasterEnabled,
                                                                              List<String> preferredLocations) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(ConnectionMode.DIRECT);

        if (preferredLocations != null) {
            connectionPolicy.preferredLocations(preferredLocations);
        }

        if (multiMasterEnabled && consistencyLevel == ConsistencyLevel.SESSION) {
            connectionPolicy.usingMultipleWriteLocations(true);
        }

        Configs configs = spy(new Configs());
        doAnswer((Answer<Protocol>)invocation -> protocol).when(configs).getProtocol();

        CosmosClientBuilder builder = CosmosClient.builder().endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .connectionPolicy(connectionPolicy)
                .consistencyLevel(consistencyLevel);

        return injectConfigs(builder, configs);
    }

    protected int expectedNumberOfPages(int totalExpectedResult, int maxPageSize) {
        return Math.max((totalExpectedResult + maxPageSize - 1 ) / maxPageSize, 1);
    }

    @DataProvider(name = "queryMetricsArgProvider")
    public Object[][] queryMetricsArgProvider() {
        return new Object[][]{
                {true},
                {false},
        };
    }
}
