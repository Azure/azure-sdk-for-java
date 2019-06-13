/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.rx;

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
import com.azure.data.cosmos.CosmosContainerSettings;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosDatabaseResponse;
import com.azure.data.cosmos.CosmosDatabaseSettings;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosRequestOptions;
import com.azure.data.cosmos.CosmosResponse;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosUser;
import com.azure.data.cosmos.CosmosUserSettings;
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
import com.azure.data.cosmos.directconnectivity.Protocol;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.PathParser;
import com.azure.data.cosmos.internal.Utils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import rx.Observable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    }

    protected TestSuiteBase() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
        objectMapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
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
        public Flux<FeedResponse<CosmosDatabaseSettings>> queryDatabases(SqlQuerySpec query) {
            return client.queryDatabases(query, null);
        }

        @Override
        public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseSettings databaseDefinition) {
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
        CosmosClient houseKeepingClient = createGatewayHouseKeepingDocumentClient().build();
        CosmosDatabaseForTest dbForTest = CosmosDatabaseForTest.create(DatabaseManagerImpl.getInstance(houseKeepingClient));
        SHARED_DATABASE = dbForTest.createdDatabase;
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        options.offerThroughput(10100);
        SHARED_MULTI_PARTITION_COLLECTION = createCollection(SHARED_DATABASE, getCollectionDefinitionWithRangeRangeIndex(), options);
        SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES = createCollection(SHARED_DATABASE, getCollectionDefinitionMultiPartitionWithCompositeAndSpatialIndexes(), options);
        options.offerThroughput(6000);
        SHARED_SINGLE_PARTITION_COLLECTION = createCollection(SHARED_DATABASE, getCollectionDefinitionWithRangeRangeIndex(), options);
    }

    @AfterSuite(groups = {"simple", "long", "direct", "multi-master", "emulator", "non-emulator"}, timeOut = SUITE_SHUTDOWN_TIMEOUT)
    public static void afterSuite() {
        logger.info("afterSuite Started");
        CosmosClient houseKeepingClient = createGatewayHouseKeepingDocumentClient().build();
        try {
            safeDeleteDatabase(SHARED_DATABASE);
            CosmosDatabaseForTest.cleanupStaleTestDatabases(DatabaseManagerImpl.getInstance(houseKeepingClient));
        } finally {
            safeClose(houseKeepingClient);
        }
    }

    protected static void truncateCollection(CosmosContainer cosmosContainer) {
        CosmosContainerSettings cosmosContainerSettings = cosmosContainer.read().block().settings();
        String cosmosContainerId = cosmosContainerSettings.id();
        logger.info("Truncating collection {} ...", cosmosContainerId);
        CosmosClient houseKeepingClient = createGatewayHouseKeepingDocumentClient().build();
        try {
            List<String> paths = cosmosContainerSettings.partitionKey().paths();
            FeedOptions options = new FeedOptions();
            options.maxDegreeOfParallelism(-1);
            options.enableCrossPartitionQuery(true);
            options.maxItemCount(100);

            logger.info("Truncating collection {} documents ...", cosmosContainer.id());

            cosmosContainer.queryItems("SELECT * FROM root", options)
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
                    }).collectList().block();
            logger.info("Truncating collection {} triggers ...", cosmosContainerId);

            cosmosContainer.queryTriggers("SELECT * FROM root", options)
                    .flatMap(page -> Flux.fromIterable(page.results()))
                    .flatMap(trigger -> {
                        CosmosRequestOptions requestOptions = new CosmosRequestOptions();

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = trigger.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                    }

                        return cosmosContainer.getTrigger(trigger.id()).delete(requestOptions);
                    }).collectList().block();

            logger.info("Truncating collection {} storedProcedures ...", cosmosContainerId);

            cosmosContainer.queryStoredProcedures("SELECT * FROM root", options)
                    .flatMap(page -> Flux.fromIterable(page.results()))
                    .flatMap(storedProcedure -> {
                        CosmosRequestOptions requestOptions = new CosmosRequestOptions();

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = storedProcedure.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                    }

                        return cosmosContainer.getStoredProcedure(storedProcedure.id()).delete(requestOptions);
                    }).collectList().block();

            logger.info("Truncating collection {} udfs ...", cosmosContainerId);

            cosmosContainer.queryUserDefinedFunctions("SELECT * FROM root", options)
                    .flatMap(page -> Flux.fromIterable(page.results()))
                    .flatMap(udf -> {
                        CosmosRequestOptions requestOptions = new CosmosRequestOptions();

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = udf.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                    }

                        return cosmosContainer.getUserDefinedFunction(udf.id()).delete(requestOptions);
                    }).collectList().block();

        } finally {
            houseKeepingClient.close();
        }

        logger.info("Finished truncating collection {}.", cosmosContainerId);
    }

    protected static void waitIfNeededForReplicasToCatchUp(CosmosClientBuilder clientBuilder) {
        switch (clientBuilder.getDesiredConsistencyLevel()) {
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

    public static CosmosContainer createCollection(CosmosDatabase database, CosmosContainerSettings cosmosContainerSettings,
            CosmosContainerRequestOptions options) {
        return database.createContainer(cosmosContainerSettings, options).block().container();
    }

    private static CosmosContainerSettings getCollectionDefinitionMultiPartitionWithCompositeAndSpatialIndexes() {
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

        CosmosContainerSettings cosmosContainerSettings = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDefinition);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<ArrayList<CompositePath>> compositeIndexes = new ArrayList<ArrayList<CompositePath>>();

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
        cosmosContainerSettings.indexingPolicy(indexingPolicy);

        return cosmosContainerSettings;
    }

    public static CosmosContainer createCollection(CosmosClient client, String dbId, CosmosContainerSettings collectionDefinition) {
        return client.getDatabase(dbId).createContainer(collectionDefinition).block().container();
    }

    public static void deleteCollection(CosmosClient client, String dbId, String collectionId) {
        client.getDatabase(dbId).getContainer(collectionId).delete().block();
    }

    public static CosmosItem createDocument(CosmosContainer cosmosContainer, CosmosItemProperties item) {
        return cosmosContainer.createItem(item).block().item();
    }

    /*
    // TODO: respect concurrencyLevel;
    public Flux<CosmosItemResponse> bulkInsert(CosmosContainer cosmosContainer,
                                                             List<CosmosItemProperties> documentDefinitionList,
                                                             int concurrencyLevel) {
        CosmosItemProperties first = documentDefinitionList.remove(0);
        Flux<CosmosItemResponse> result = Flux.from(cosmosContainer.createItem(first));
        for (CosmosItemProperties docDef : documentDefinitionList) {
            result.concatWith(cosmosContainer.createItem(docDef));
        }

        return result;
    }
*/
    public List<CosmosItemProperties> bulkInsertBlocking(CosmosContainer cosmosContainer,
                                                         List<CosmosItemProperties> documentDefinitionList) {
        /*
        return bulkInsert(cosmosContainer, documentDefinitionList, DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL)
                .parallel()
                .runOn(Schedulers.parallel())
                .map(CosmosItemResponse::properties)
                .sequential()
                .collectList()
                .block();
                */
        return Flux.merge(documentDefinitionList.stream()
                .map(d -> cosmosContainer.createItem(d).map(response -> response.properties()))
                .collect(Collectors.toList())).collectList().block();
    }

    public static ConsistencyLevel getAccountDefaultConsistencyLevel(CosmosClient client) {
        return CosmosBridgeInternal.getDatabaseAccount(client).block().getConsistencyPolicy().getDefaultConsistencyLevel();
    }

    public static CosmosUser createUser(CosmosClient client, String databaseId, CosmosUserSettings userSettings) {
        return client.getDatabase(databaseId).read().block().database().createUser(userSettings).block().user();
    }

    public static CosmosUser safeCreateUser(CosmosClient client, String databaseId, CosmosUserSettings user) {
        deleteUserIfExists(client, databaseId, user.id());
        return createUser(client, databaseId, user);
    }

    private static CosmosContainer safeCreateCollection(CosmosClient client, String databaseId, CosmosContainerSettings collection, CosmosContainerRequestOptions options) {
        deleteCollectionIfExists(client, databaseId, collection.id());
        return createCollection(client.getDatabase(databaseId), collection, options);
    }

    static protected CosmosContainerSettings getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);

        CosmosContainerSettings collectionDefinition = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);

        return collectionDefinition;
    }

    static protected CosmosContainerSettings getCollectionDefinitionWithRangeRangeIndex() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.paths(paths);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.path("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.STRING);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.NUMBER);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.indexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        CosmosContainerSettings cosmosContainerSettings = new CosmosContainerSettings(UUID.randomUUID().toString(), partitionKeyDef);
        cosmosContainerSettings.indexingPolicy(indexingPolicy);

        return cosmosContainerSettings;
    }

    public static void deleteCollectionIfExists(CosmosClient client, String databaseId, String collectionId) {
        CosmosDatabase database = client.getDatabase(databaseId).read().block().database();
        List<CosmosContainerSettings> res = database.queryContainers(String.format("SELECT * FROM root r where r.id = '%s'", collectionId), null)
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
        List<CosmosUserSettings> res = database
                .queryUsers(String.format("SELECT * FROM root r where r.id = '%s'", userId), null)
                .flatMap(page -> Flux.fromIterable(page.results()))
                .collectList().block();
        if (!res.isEmpty()) {
            deleteUser(database, userId);
        }
    }

    public static void deleteUser(CosmosDatabase database, String userId) {
        database.getUser(userId).read().block().user().delete(null).block();
    }

    static private CosmosDatabase safeCreateDatabase(CosmosClient client, CosmosDatabaseSettings databaseSettings) {
        safeDeleteDatabase(client.getDatabase(databaseSettings.id()));
        return client.createDatabase(databaseSettings).block().database();
    }

    static protected CosmosDatabase createDatabase(CosmosClient client, String databaseId) {
        CosmosDatabaseSettings databaseSettings = new CosmosDatabaseSettings(databaseId);
        return client.createDatabase(databaseSettings).block().database();
    }

    static protected CosmosDatabase createDatabaseIfNotExists(CosmosClient client, String databaseId) {
        List<CosmosDatabaseSettings> res = client.queryDatabases(String.format("SELECT * FROM r where r.id = '%s'", databaseId), null)
                .flatMap(p -> Flux.fromIterable(p.results()))
                .collectList()
                .block();
        if (res.size() != 0) {
            return client.getDatabase(databaseId).read().block().database();
        } else {
            CosmosDatabaseSettings databaseSettings = new CosmosDatabaseSettings(databaseId);
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
            List<CosmosContainerSettings> collections = database.listContainers()
                    .flatMap(p -> Flux.fromIterable(p.results()))
                    .collectList()
                    .block();

            for(CosmosContainerSettings collection: collections) {
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
                database.getContainer(collectionId).read().block().container().delete().block();
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

    public <T extends Resource> void validateQuerySuccess(Observable<FeedResponse<T>> observable,
            FeedResponseListValidator<T> validator) {
        validateQuerySuccess(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateQuerySuccess(Observable<FeedResponse<T>> observable,
            FeedResponseListValidator<T> validator, long timeout) {

        VerboseTestSubscriber<FeedResponse<T>> testSubscriber = new VerboseTestSubscriber<>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        validator.validate(testSubscriber.getOnNextEvents());
    }

    public <T extends CosmosResponse> void validateSuccess(Mono<T> single, CosmosResponseValidator<T> validator)
            throws InterruptedException {
        validateSuccess(single.flux(), validator, subscriberValidationTimeout);
    }

    public static <T extends CosmosResponse> void validateSuccess(Flux<T> flowable,
            CosmosResponseValidator<T> validator, long timeout) throws InterruptedException {

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
        validator.validate(testSubscriber.getEvents().get(0).stream().map(object -> (FeedResponse<T>) object)
                .collect(Collectors.toList()));
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
                {createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null)},
                {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.HTTPS, false, null)},
                {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null)}
        };
    }

    private static ConsistencyLevel parseConsistency(String consistency) {
        if (consistency != null) {
            for (ConsistencyLevel consistencyLevel : ConsistencyLevel.values()) {
                if (consistencyLevel.toString().toLowerCase().equals(consistency.toLowerCase())) {
                    return consistencyLevel;
                }
            }
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

        try {
            return objectMapper.readValue(protocols, new TypeReference<List<Protocol>>() {
            });
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
        cosmosConfigurations.add(createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null));

        for (Protocol protocol : protocols) {
            testConsistencies.forEach(consistencyLevel -> cosmosConfigurations.add(createDirectRxDocumentClient(consistencyLevel,
                                                                                                    protocol,
                                                                                                    isMultiMasterEnabled,
                                                                                                    preferredLocations)));
        }

        cosmosConfigurations.forEach(c -> logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
                                          c.getConnectionPolicy().connectionMode(),
                                          c.getDesiredConsistencyLevel(),
                                          c.getConfigs().getProtocol()
        ));

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

        try {
            return objectMapper.readValue(consistencies, new TypeReference<List<ConsistencyLevel>>() {
            });
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
        cosmosConfigurations.add(createGatewayRxDocumentClient(ConsistencyLevel.SESSION, isMultiMasterEnabled, preferredLocations));

        for (Protocol protocol : protocols) {
            testConsistencies.forEach(consistencyLevel -> cosmosConfigurations.add(createDirectRxDocumentClient(consistencyLevel,
                                                                                                    protocol,
                                                                                                    isMultiMasterEnabled,
                                                                                                    preferredLocations)));
        }

        cosmosConfigurations.forEach(c -> logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
                                          c.getConnectionPolicy().connectionMode(),
                                          c.getDesiredConsistencyLevel(),
                                          c.getConfigs().getProtocol()
        ));

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

        return CosmosClient.builder().endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .connectionPolicy(connectionPolicy)
                .consistencyLevel(consistencyLevel)
                .configs(configs);
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

    public static class VerboseTestSubscriber<T> extends rx.observers.TestSubscriber<T> {
        @Override
        public void assertNoErrors() {
            List<Throwable> onErrorEvents = getOnErrorEvents();
            StringBuilder errorMessageBuilder = new StringBuilder();
            if (!onErrorEvents.isEmpty()) {
                for(Throwable throwable : onErrorEvents) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    String sStackTrace = sw.toString(); // stack trace as a string
                    errorMessageBuilder.append(sStackTrace);
                }

                AssertionError ae = new AssertionError(errorMessageBuilder.toString());
                throw ae;
            }
        }
    }
}
