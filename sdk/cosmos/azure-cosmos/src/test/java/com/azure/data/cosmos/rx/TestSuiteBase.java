// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncClient;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.PathParser;
import com.azure.data.cosmos.internal.TestConfigurations;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.directconnectivity.Protocol;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosDatabase;
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

public class TestSuiteBase extends CosmosAsyncClientTest {

    private static final int DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL = 500;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected static Logger logger = LoggerFactory.getLogger(TestSuiteBase.class.getSimpleName());
    protected static final int TIMEOUT = 40000;
    protected static final int FEED_TIMEOUT = 40000;
    protected static final int SETUP_TIMEOUT = 60000;
    protected static final int SHUTDOWN_TIMEOUT = 24000;

    protected static final int SUITE_SETUP_TIMEOUT = 120000;
    protected static final int SUITE_SHUTDOWN_TIMEOUT = 60000;

    protected static final int WAIT_REPLICA_CATCH_UP_IN_MILLIS = 4000;

    protected final static ConsistencyLevel accountConsistency;
    protected static final ImmutableList<String> preferredLocations;
    private static final ImmutableList<ConsistencyLevel> desiredConsistencies;
    private static final ImmutableList<Protocol> protocols;

    protected static final CosmosKeyCredential cosmosKeyCredential;

    protected int subscriberValidationTimeout = TIMEOUT;

    private static CosmosAsyncDatabase SHARED_DATABASE;
    private static CosmosAsyncContainer SHARED_MULTI_PARTITION_COLLECTION;
    private static CosmosAsyncContainer SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES;
    private static CosmosAsyncContainer SHARED_SINGLE_PARTITION_COLLECTION;

    public TestSuiteBase(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    protected static CosmosAsyncDatabase getSharedCosmosDatabase(CosmosAsyncClient client) {
        return CosmosBridgeInternal.getCosmosDatabaseWithNewClient(SHARED_DATABASE, client);
    }

    protected static CosmosAsyncContainer getSharedMultiPartitionCosmosContainer(CosmosAsyncClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_MULTI_PARTITION_COLLECTION, SHARED_DATABASE, client);
    }

    protected static CosmosAsyncContainer getSharedMultiPartitionCosmosContainerWithCompositeAndSpatialIndexes(CosmosAsyncClient client) {
        return CosmosBridgeInternal.getCosmosContainerWithNewClient(SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES, SHARED_DATABASE, client);
    }

    protected static CosmosAsyncContainer getSharedSinglePartitionCosmosContainer(CosmosAsyncClient client) {
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

        cosmosKeyCredential = new CosmosKeyCredential(TestConfigurations.MASTER_KEY);
    }

    protected TestSuiteBase() {
        logger.debug("Initializing {} ...", this.getClass().getSimpleName());
    }

    private static <T> ImmutableList<T> immutableListOrNull(List<T> list) {
        return list != null ? ImmutableList.copyOf(list) : null;
    }

    private static class DatabaseManagerImpl implements CosmosDatabaseForTest.DatabaseManager {
        public static DatabaseManagerImpl getInstance(CosmosAsyncClient client) {
            return new DatabaseManagerImpl(client);
        }

        private final CosmosAsyncClient client;

        private DatabaseManagerImpl(CosmosAsyncClient client) {
            this.client = client;
        }

        @Override
        public Flux<FeedResponse<CosmosDatabaseProperties>> queryDatabases(SqlQuerySpec query) {
            return client.queryDatabases(query, null);
        }

        @Override
        public Mono<CosmosAsyncDatabaseResponse> createDatabase(CosmosDatabaseProperties databaseDefinition) {
            return client.createDatabase(databaseDefinition);
        }

        @Override
        public CosmosAsyncDatabase getDatabase(String id) {
            return client.getDatabase(id);
        }
    }

    @BeforeSuite(groups = {"simple", "long", "direct", "multi-master", "emulator", "non-emulator"}, timeOut = SUITE_SETUP_TIMEOUT)
    public static void beforeSuite() {

        logger.info("beforeSuite Started");

        try (CosmosAsyncClient houseKeepingClient = createGatewayHouseKeepingDocumentClient().buildAsyncClient()) {
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

        try (CosmosAsyncClient houseKeepingClient = createGatewayHouseKeepingDocumentClient().buildAsyncClient()) {
            safeDeleteDatabase(SHARED_DATABASE);
            CosmosDatabaseForTest.cleanupStaleTestDatabases(DatabaseManagerImpl.getInstance(houseKeepingClient));
        }
    }

    protected static void truncateCollection(CosmosAsyncContainer cosmosContainer) {
        CosmosContainerProperties cosmosContainerProperties = cosmosContainer.read().block().getProperties();
        String cosmosContainerId = cosmosContainerProperties.getId();
        logger.info("Truncating collection {} ...", cosmosContainerId);
        List<String> paths = cosmosContainerProperties.getPartitionKeyDefinition().getPaths();
        FeedOptions options = new FeedOptions();
        options.setMaxDegreeOfParallelism(-1);
        options.setEnableCrossPartitionQuery(true);
        options.maxItemCount(100);

        logger.info("Truncating collection {} documents ...", cosmosContainer.getId());

        cosmosContainer.queryItems("SELECT * FROM root", options)
                       .publishOn(Schedulers.parallel())
                    .flatMap(page -> Flux.fromIterable(page.getResults()))
                        .flatMap(doc -> {

                            Object propertyValue = null;
                            if (paths != null && !paths.isEmpty()) {
                                List<String> pkPath = PathParser.getPathParts(paths.get(0));
                                propertyValue = doc.getObjectByPath(pkPath);
                                if (propertyValue == null) {
                                    propertyValue = PartitionKey.None;
                                }

                            }
                            return cosmosContainer.getItem(doc.getId(), propertyValue).delete();
                    }).then().block();
        logger.info("Truncating collection {} triggers ...", cosmosContainerId);

        cosmosContainer.getScripts().queryTriggers("SELECT * FROM root", options)
                       .publishOn(Schedulers.parallel())
                .flatMap(page -> Flux.fromIterable(page.getResults()))
                .flatMap(trigger -> {
//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = trigger.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                        Object propertyValue = getTrigger.getObjectByPath(PathParser.getPathParts(getPaths.get(0)));
//                        requestOptions.getPartitionKey(new PartitionKey(propertyValue));
//                    }

                        return cosmosContainer.getScripts().getTrigger(trigger.getId()).delete();
                    }).then().block();

        logger.info("Truncating collection {} storedProcedures ...", cosmosContainerId);

        cosmosContainer.getScripts().queryStoredProcedures("SELECT * FROM root", options)
                       .publishOn(Schedulers.parallel())
                .flatMap(page -> Flux.fromIterable(page.getResults()))
                .flatMap(storedProcedure -> {

//                    if (getPaths != null && !getPaths.isEmpty()) {
//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = storedProcedure.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                        requestOptions.getPartitionKey(new PartitionKey(propertyValue));
//                    }

                    return cosmosContainer.getScripts().getStoredProcedure(storedProcedure.getId()).delete(new CosmosStoredProcedureRequestOptions());
                    }).then().block();

        logger.info("Truncating collection {} udfs ...", cosmosContainerId);

        cosmosContainer.getScripts().queryUserDefinedFunctions("SELECT * FROM root", options)
                       .publishOn(Schedulers.parallel())
                .flatMap(page -> Flux.fromIterable(page.getResults()))
                .flatMap(udf -> {

//                    if (getPaths != null && !getPaths.isEmpty()) {
//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = udf.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                        requestOptions.getPartitionKey(new PartitionKey(propertyValue));
//                    }

                    return cosmosContainer.getScripts().getUserDefinedFunction(udf.getId()).delete();
                    }).then().block();

        logger.info("Finished truncating collection {}.", cosmosContainerId);
    }

    protected static void waitIfNeededForReplicasToCatchUp(CosmosClientBuilder clientBuilder) {
        switch (clientBuilder.getConsistencyLevel()) {
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

    public static CosmosAsyncContainer createCollection(CosmosAsyncDatabase database, CosmosContainerProperties cosmosContainerProperties,
                                                        CosmosContainerRequestOptions options, int throughput) {
        return database.createContainer(cosmosContainerProperties, throughput, options).block().getContainer();
    }

    public static CosmosAsyncContainer createCollection(CosmosAsyncDatabase database, CosmosContainerProperties cosmosContainerProperties,
                                                        CosmosContainerRequestOptions options) {
        return database.createContainer(cosmosContainerProperties, options).block().getContainer();
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
        partitionKeyDefinition.setPaths(partitionKeyPaths);

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDefinition);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<List<CompositePath>> compositeIndexes = new ArrayList<>();

        //Simple
        ArrayList<CompositePath> compositeIndexSimple = new ArrayList<CompositePath>();
        CompositePath compositePath1 = new CompositePath();
        compositePath1.setPath("/" + NUMBER_FIELD);
        compositePath1.setOrder(CompositePathSortOrder.ASCENDING);

        CompositePath compositePath2 = new CompositePath();
        compositePath2.setPath("/" + STRING_FIELD);
        compositePath2.setOrder(CompositePathSortOrder.DESCENDING);

        compositeIndexSimple.add(compositePath1);
        compositeIndexSimple.add(compositePath2);

        //Max Columns
        ArrayList<CompositePath> compositeIndexMaxColumns = new ArrayList<CompositePath>();
        CompositePath compositePath3 = new CompositePath();
        compositePath3.setPath("/" + NUMBER_FIELD);
        compositePath3.setOrder(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath4 = new CompositePath();
        compositePath4.setPath("/" + STRING_FIELD);
        compositePath4.setOrder(CompositePathSortOrder.ASCENDING);

        CompositePath compositePath5 = new CompositePath();
        compositePath5.setPath("/" + NUMBER_FIELD_2);
        compositePath5.setOrder(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath6 = new CompositePath();
        compositePath6.setPath("/" + STRING_FIELD_2);
        compositePath6.setOrder(CompositePathSortOrder.ASCENDING);

        compositeIndexMaxColumns.add(compositePath3);
        compositeIndexMaxColumns.add(compositePath4);
        compositeIndexMaxColumns.add(compositePath5);
        compositeIndexMaxColumns.add(compositePath6);

        //Primitive Values
        ArrayList<CompositePath> compositeIndexPrimitiveValues = new ArrayList<CompositePath>();
        CompositePath compositePath7 = new CompositePath();
        compositePath7.setPath("/" + NUMBER_FIELD);
        compositePath7.setOrder(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath8 = new CompositePath();
        compositePath8.setPath("/" + STRING_FIELD);
        compositePath8.setOrder(CompositePathSortOrder.ASCENDING);

        CompositePath compositePath9 = new CompositePath();
        compositePath9.setPath("/" + BOOL_FIELD);
        compositePath9.setOrder(CompositePathSortOrder.DESCENDING);

        CompositePath compositePath10 = new CompositePath();
        compositePath10.setPath("/" + NULL_FIELD);
        compositePath10.setOrder(CompositePathSortOrder.ASCENDING);

        compositeIndexPrimitiveValues.add(compositePath7);
        compositeIndexPrimitiveValues.add(compositePath8);
        compositeIndexPrimitiveValues.add(compositePath9);
        compositeIndexPrimitiveValues.add(compositePath10);

        //Long Strings
        ArrayList<CompositePath> compositeIndexLongStrings = new ArrayList<CompositePath>();
        CompositePath compositePath11 = new CompositePath();
        compositePath11.setPath("/" + STRING_FIELD);

        CompositePath compositePath12 = new CompositePath();
        compositePath12.setPath("/" + SHORT_STRING_FIELD);

        CompositePath compositePath13 = new CompositePath();
        compositePath13.setPath("/" + MEDIUM_STRING_FIELD);

        CompositePath compositePath14 = new CompositePath();
        compositePath14.setPath("/" + LONG_STRING_FIELD);

        compositeIndexLongStrings.add(compositePath11);
        compositeIndexLongStrings.add(compositePath12);
        compositeIndexLongStrings.add(compositePath13);
        compositeIndexLongStrings.add(compositePath14);

        compositeIndexes.add(compositeIndexSimple);
        compositeIndexes.add(compositeIndexMaxColumns);
        compositeIndexes.add(compositeIndexPrimitiveValues);
        compositeIndexes.add(compositeIndexLongStrings);

        indexingPolicy.setCompositeIndexes(compositeIndexes);
        cosmosContainerProperties.setIndexingPolicy(indexingPolicy);

        return cosmosContainerProperties;
    }

    public static CosmosAsyncContainer createCollection(CosmosAsyncClient client, String dbId, CosmosContainerProperties collectionDefinition) {
        return client.getDatabase(dbId).createContainer(collectionDefinition).block().getContainer();
    }

    public static void deleteCollection(CosmosAsyncClient client, String dbId, String collectionId) {
        client.getDatabase(dbId).getContainer(collectionId).delete().block();
    }

    public static CosmosAsyncItem createDocument(CosmosAsyncContainer cosmosContainer, CosmosItemProperties item) {
        return cosmosContainer.createItem(item).block().getItem();
    }

    public Flux<CosmosAsyncItemResponse> bulkInsert(CosmosAsyncContainer cosmosContainer,
                                                    List<CosmosItemProperties> documentDefinitionList,
                                                    int concurrencyLevel) {
        List<Mono<CosmosAsyncItemResponse>> result = new ArrayList<>(documentDefinitionList.size());
        for (CosmosItemProperties docDef : documentDefinitionList) {
            result.add(cosmosContainer.createItem(docDef));
        }

        return Flux.merge(Flux.fromIterable(result), concurrencyLevel);
    }
    public List<CosmosItemProperties> bulkInsertBlocking(CosmosAsyncContainer cosmosContainer,
                                                         List<CosmosItemProperties> documentDefinitionList) {
        return bulkInsert(cosmosContainer, documentDefinitionList, DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL)
                .publishOn(Schedulers.parallel())
                .map(CosmosAsyncItemResponse::getProperties)
                .collectList()
                .block();
    }

    public void voidBulkInsertBlocking(CosmosAsyncContainer cosmosContainer,
                                       List<CosmosItemProperties> documentDefinitionList) {
        bulkInsert(cosmosContainer, documentDefinitionList, DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL)
            .publishOn(Schedulers.parallel())
            .map(CosmosAsyncItemResponse::getProperties)
            .then()
            .block();
    }

    public static CosmosAsyncUser createUser(CosmosAsyncClient client, String databaseId, CosmosUserProperties userSettings) {
        return client.getDatabase(databaseId).read().block().getDatabase().createUser(userSettings).block().getUser();
    }

    public static CosmosAsyncUser safeCreateUser(CosmosAsyncClient client, String databaseId, CosmosUserProperties user) {
        deleteUserIfExists(client, databaseId, user.getId());
        return createUser(client, databaseId, user);
    }

    private static CosmosAsyncContainer safeCreateCollection(CosmosAsyncClient client, String databaseId, CosmosContainerProperties collection, CosmosContainerRequestOptions options) {
        deleteCollectionIfExists(client, databaseId, collection.getId());
        return createCollection(client.getDatabase(databaseId), collection, options);
    }

    static protected CosmosContainerProperties getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);

        return collectionDefinition;
    }

    static protected CosmosContainerProperties getCollectionDefinitionWithRangeRangeIndex() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.STRING);
        BridgeInternal.setProperty(stringIndex, "getPrecision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.NUMBER);
        BridgeInternal.setProperty(numberIndex, "precision", -1);
        indexes.add(numberIndex);
        includedPath.setIndexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        cosmosContainerProperties.setIndexingPolicy(indexingPolicy);

        return cosmosContainerProperties;
    }

    public static void deleteCollectionIfExists(CosmosAsyncClient client, String databaseId, String collectionId) {
        CosmosAsyncDatabase database = client.getDatabase(databaseId).read().block().getDatabase();
        List<CosmosContainerProperties> res = database.queryContainers(String.format("SELECT * FROM root r where r.id = '%s'", collectionId), null)
                                                      .flatMap(page -> Flux.fromIterable(page.getResults()))
                                                      .collectList()
                                                      .block();

        if (!res.isEmpty()) {
            deleteCollection(database, collectionId);
        }
    }

    public static void deleteCollection(CosmosAsyncDatabase cosmosDatabase, String collectionId) {
        cosmosDatabase.getContainer(collectionId).delete().block();
    }

    public static void deleteCollection(CosmosAsyncContainer cosmosContainer) {
        cosmosContainer.delete().block();
    }

    public static void deleteDocumentIfExists(CosmosAsyncClient client, String databaseId, String collectionId, String docId) {
        FeedOptions options = new FeedOptions();
        options.partitionKey(new PartitionKey(docId));
        CosmosAsyncContainer cosmosContainer = client.getDatabase(databaseId).read().block().getDatabase().getContainer(collectionId).read().block().getContainer();
        List<CosmosItemProperties> res = cosmosContainer
                .queryItems(String.format("SELECT * FROM root r where r.id = '%s'", docId), options)
                .flatMap(page -> Flux.fromIterable(page.getResults()))
                .collectList().block();

        if (!res.isEmpty()) {
            deleteDocument(cosmosContainer, docId);
        }
    }

    public static void safeDeleteDocument(CosmosAsyncContainer cosmosContainer, String documentId, Object partitionKey) {
        if (cosmosContainer != null && documentId != null) {
            try {
                cosmosContainer.getItem(documentId, partitionKey).read().block().getItem().delete().block();
            } catch (Exception e) {
                CosmosClientException dce = Utils.as(e, CosmosClientException.class);
                if (dce == null || dce.getStatusCode() != 404) {
                    throw e;
                }
            }
        }
    }

    public static void deleteDocument(CosmosAsyncContainer cosmosContainer, String documentId) {
        cosmosContainer.getItem(documentId, PartitionKey.None).read().block().getItem().delete();
    }

    public static void deleteUserIfExists(CosmosAsyncClient client, String databaseId, String userId) {
        CosmosAsyncDatabase database = client.getDatabase(databaseId).read().block().getDatabase();
        List<CosmosUserProperties> res = database
                .queryUsers(String.format("SELECT * FROM root r where r.id = '%s'", userId), null)
                .flatMap(page -> Flux.fromIterable(page.getResults()))
                .collectList().block();
        if (!res.isEmpty()) {
            deleteUser(database, userId);
        }
    }

    public static void deleteUser(CosmosAsyncDatabase database, String userId) {
        database.getUser(userId).read().block().getUser().delete().block();
    }

    static private CosmosAsyncDatabase safeCreateDatabase(CosmosAsyncClient client, CosmosDatabaseProperties databaseSettings) {
        safeDeleteDatabase(client.getDatabase(databaseSettings.getId()));
        return client.createDatabase(databaseSettings).block().getDatabase();
    }

    static protected CosmosAsyncDatabase createDatabase(CosmosAsyncClient client, String databaseId) {
        CosmosDatabaseProperties databaseSettings = new CosmosDatabaseProperties(databaseId);
        return client.createDatabase(databaseSettings).block().getDatabase();
    }

    static protected CosmosDatabase createSyncDatabase(CosmosClient client, String databaseId) {
        CosmosDatabaseProperties databaseSettings = new CosmosDatabaseProperties(databaseId);
        try {
            return client.createDatabase(databaseSettings).getDatabase();
        } catch (CosmosClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    static protected CosmosAsyncDatabase createDatabaseIfNotExists(CosmosAsyncClient client, String databaseId) {
        List<CosmosDatabaseProperties> res = client.queryDatabases(String.format("SELECT * FROM r where r.id = '%s'", databaseId), null)
                                                   .flatMap(p -> Flux.fromIterable(p.getResults()))
                                                   .collectList()
                                                   .block();
        if (res.size() != 0) {
            return client.getDatabase(databaseId).read().block().getDatabase();
        } else {
            CosmosDatabaseProperties databaseSettings = new CosmosDatabaseProperties(databaseId);
            return client.createDatabase(databaseSettings).block().getDatabase();
        }
    }

    static protected void safeDeleteDatabase(CosmosAsyncDatabase database) {
        if (database != null) {
            try {
                database.delete().block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeDeleteSyncDatabase(CosmosDatabase database) {
        if (database != null) {
            try {
                database.delete();
            } catch (Exception e) {
                logger.error("failed to delete sync database", e);
            }
        }
    }
    
    static protected void safeDeleteAllCollections(CosmosAsyncDatabase database) {
        if (database != null) {
            List<CosmosContainerProperties> collections = database.readAllContainers()
                                                                  .flatMap(p -> Flux.fromIterable(p.getResults()))
                                                                  .collectList()
                                                                  .block();

            for(CosmosContainerProperties collection: collections) {
                database.getContainer(collection.getId()).delete().block();
            }
        }
    }

    static protected void safeDeleteCollection(CosmosAsyncContainer collection) {
        if (collection != null) {
            try {
                collection.delete().block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeDeleteCollection(CosmosAsyncDatabase database, String collectionId) {
        if (database != null && collectionId != null) {
            try {
                database.getContainer(collectionId).delete().block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeCloseAsync(CosmosAsyncClient client) {
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

    static protected void safeClose(CosmosAsyncClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                logger.error("failed to close client", e);
            }
        }
    }

    static protected void safeCloseSyncClient(CosmosClient client) {
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
                                          c.getConnectionPolicy().getConnectionMode(),
                                          c.getConsistencyLevel(),
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
                                          c.getConnectionPolicy().getConnectionMode(),
                                          c.getConsistencyLevel(),
                                          extractConfigs(c).getProtocol()
        ));

        cosmosConfigurations.add(createGatewayRxDocumentClient(ConsistencyLevel.SESSION, isMultiMasterEnabled, preferredLocations));

        return cosmosConfigurations.stream().map(c -> new Object[]{c}).collect(Collectors.toList()).toArray(new Object[0][]);
    }

    static protected CosmosClientBuilder createGatewayHouseKeepingDocumentClient() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.GATEWAY);
        RetryOptions options = new RetryOptions();
        options.setMaxRetryWaitTimeInSeconds(SUITE_SETUP_TIMEOUT);
        connectionPolicy.setRetryOptions(options);
        return CosmosAsyncClient.builder().setEndpoint(TestConfigurations.HOST)
                .setCosmosKeyCredential(cosmosKeyCredential)
                .setConnectionPolicy(connectionPolicy)
                .setConsistencyLevel(ConsistencyLevel.SESSION);
    }

    static protected CosmosClientBuilder createGatewayRxDocumentClient(ConsistencyLevel consistencyLevel, boolean multiMasterEnabled, List<String> preferredLocations) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.GATEWAY);
        connectionPolicy.setUsingMultipleWriteLocations(multiMasterEnabled);
        connectionPolicy.setPreferredLocations(preferredLocations);
        return CosmosAsyncClient.builder().setEndpoint(TestConfigurations.HOST)
                .setCosmosKeyCredential(cosmosKeyCredential)
                .setConnectionPolicy(connectionPolicy)
                .setConsistencyLevel(consistencyLevel);
    }

    static protected CosmosClientBuilder createGatewayRxDocumentClient() {
        return createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null);
    }

    static protected CosmosClientBuilder createDirectRxDocumentClient(ConsistencyLevel consistencyLevel,
                                                                              Protocol protocol,
                                                                              boolean multiMasterEnabled,
                                                                              List<String> preferredLocations) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.DIRECT);

        if (preferredLocations != null) {
            connectionPolicy.setPreferredLocations(preferredLocations);
        }

        if (multiMasterEnabled && consistencyLevel == ConsistencyLevel.SESSION) {
            connectionPolicy.setUsingMultipleWriteLocations(true);
        }

        Configs configs = spy(new Configs());
        doAnswer((Answer<Protocol>)invocation -> protocol).when(configs).getProtocol();

        CosmosClientBuilder builder = CosmosAsyncClient.builder().setEndpoint(TestConfigurations.HOST)
                .setCosmosKeyCredential(cosmosKeyCredential)
                .setConnectionPolicy(connectionPolicy)
                .setConsistencyLevel(consistencyLevel);

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
