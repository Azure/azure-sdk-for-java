// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.DocumentClientTest;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.TestNGLogListener;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.AsyncDocumentClient.Builder;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.azure.cosmos.implementation.guava25.base.CaseFormat;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.CompositePathSortOrder;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.subscribers.TestSubscriber;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;

@Listeners({TestNGLogListener.class})
public class TestSuiteBase extends DocumentClientTest {

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
    protected static Database SHARED_DATABASE;
    protected static DocumentCollection SHARED_MULTI_PARTITION_COLLECTION;
    protected static DocumentCollection SHARED_SINGLE_PARTITION_COLLECTION;
    protected static DocumentCollection SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES;

    private static <T> ImmutableList<T> immutableListOrNull(List<T> list) {
        return list != null ? ImmutableList.copyOf(list) : null;
    }

    static {
        accountConsistency = parseConsistency(TestConfigurations.CONSISTENCY);
        desiredConsistencies = immutableListOrNull(
                ObjectUtils.defaultIfNull(parseDesiredConsistencies(TestConfigurations.DESIRED_CONSISTENCIES),
                                          allEqualOrLowerConsistencies(accountConsistency)));
        preferredLocations = immutableListOrNull(parsePreferredLocation(TestConfigurations.PREFERRED_LOCATIONS));
        protocols = ObjectUtils.defaultIfNull(immutableListOrNull(parseProtocols(TestConfigurations.PROTOCOLS)),
                                              ImmutableList.of(Protocol.TCP));
        //  Object mapper configuration
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
        objectMapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
    }

    protected TestSuiteBase() {
        this(new AsyncDocumentClient.Builder());
    }

    protected TestSuiteBase(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
        logger.debug("Initializing {} ...", this.getClass().getSimpleName());
    }

    private static class DatabaseManagerImpl implements DatabaseForTest.DatabaseManager {
        public static DatabaseManagerImpl getInstance(AsyncDocumentClient client) {
            return new DatabaseManagerImpl(client);
        }

        private final AsyncDocumentClient client;

        private DatabaseManagerImpl(AsyncDocumentClient client) {
            this.client = client;
        }

        @Override
        public Flux<FeedResponse<Database>> queryDatabases(SqlQuerySpec query) {
            return client.queryDatabases(query, null);
        }

        @Override
        public Mono<ResourceResponse<Database>> createDatabase(Database databaseDefinition) {
            return client.createDatabase(databaseDefinition, null);
        }

        @Override
        public Mono<ResourceResponse<Database>> deleteDatabase(String id) {
            return client.deleteDatabase("dbs/" + id, null);
        }
    }

    @BeforeSuite(groups = {"simple", "long", "direct", "multi-region", "multi-master", "emulator", "non-emulator"}, timeOut = SUITE_SETUP_TIMEOUT)
    public static void beforeSuite() {
        logger.info("beforeSuite Started");
        AsyncDocumentClient houseKeepingClient = createGatewayHouseKeepingDocumentClient().build();
        try {
            DatabaseForTest dbForTest = DatabaseForTest.create(DatabaseManagerImpl.getInstance(houseKeepingClient));
            SHARED_DATABASE = dbForTest.createdDatabase;
            RequestOptions options = new RequestOptions();
            options.setOfferThroughput(10100);
            SHARED_MULTI_PARTITION_COLLECTION = createCollection(houseKeepingClient, SHARED_DATABASE.getId(), getCollectionDefinitionWithRangeRangeIndex(), options);
            SHARED_SINGLE_PARTITION_COLLECTION = createCollection(houseKeepingClient, SHARED_DATABASE.getId(), getCollectionDefinition(), null);
            SHARED_MULTI_PARTITION_COLLECTION_WITH_COMPOSITE_AND_SPATIAL_INDEXES = createCollection(houseKeepingClient, SHARED_DATABASE.getId(), getCollectionDefinitionMultiPartitionWithCompositeAndSpatialIndexes(), options);
        } finally {
            houseKeepingClient.close();
        }
    }

    @AfterSuite(groups = {"simple", "long", "direct", "multi-region", "multi-master", "emulator", "non-emulator"}, timeOut = SUITE_SHUTDOWN_TIMEOUT)
    public static void afterSuite() {
        logger.info("afterSuite Started");
        AsyncDocumentClient houseKeepingClient = createGatewayHouseKeepingDocumentClient().build();
        try {
            safeDeleteDatabase(houseKeepingClient, SHARED_DATABASE);
            DatabaseForTest.cleanupStaleTestDatabases(DatabaseManagerImpl.getInstance(houseKeepingClient));
        } finally {
            safeClose(houseKeepingClient);
        }
    }

    protected static void truncateCollection(DocumentCollection collection) {
        logger.info("Truncating collection {} ...", collection.getId());
        AsyncDocumentClient houseKeepingClient = createGatewayHouseKeepingDocumentClient().build();
        try {
            List<String> paths = collection.getPartitionKey().getPaths();

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setMaxDegreeOfParallelism(-1);

            ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options, 100);

            logger.info("Truncating collection {} documents ...", collection.getId());

            houseKeepingClient.queryDocuments(collection.getSelfLink(), "SELECT * FROM root", options)
                              .publishOn(Schedulers.parallel())
                    .flatMap(page -> Flux.fromIterable(page.getResults()))
                    .flatMap(doc -> {
                        RequestOptions requestOptions = new RequestOptions();

                        if (paths != null && !paths.isEmpty()) {
                            List<String> pkPath = PathParser.getPathParts(paths.get(0));
                            Object propertyValue = ModelBridgeInternal.getObjectByPathFromJsonSerializable(doc, pkPath);
                            if (propertyValue == null) {
                                propertyValue = Undefined.value();
                            }

                            requestOptions.setPartitionKey(new PartitionKey(propertyValue));
                        }

                        return houseKeepingClient.deleteDocument(doc.getSelfLink(), requestOptions);
                    }).then().block();

            logger.info("Truncating collection {} triggers ...", collection.getId());

            houseKeepingClient.queryTriggers(collection.getSelfLink(), "SELECT * FROM root", options)
                              .publishOn(Schedulers.parallel())
                    .flatMap(page -> Flux.fromIterable(page.getResults()))
                    .flatMap(trigger -> {
                        RequestOptions requestOptions = new RequestOptions();

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = trigger.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                    }

                        return houseKeepingClient.deleteTrigger(trigger.getSelfLink(), requestOptions);
                    }).then().block();

            logger.info("Truncating collection {} storedProcedures ...", collection.getId());

            houseKeepingClient.queryStoredProcedures(collection.getSelfLink(), "SELECT * FROM root", options)
                              .publishOn(Schedulers.parallel())
                    .flatMap(page -> Flux.fromIterable(page.getResults()))
                    .flatMap(storedProcedure -> {
                        RequestOptions requestOptions = new RequestOptions();

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = storedProcedure.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                    }

                        return houseKeepingClient.deleteStoredProcedure(storedProcedure.getSelfLink(), requestOptions);
                    }).then().block();

            logger.info("Truncating collection {} udfs ...", collection.getId());

            houseKeepingClient.queryUserDefinedFunctions(collection.getSelfLink(), "SELECT * FROM root", options)
                              .publishOn(Schedulers.parallel())
                    .flatMap(page -> Flux.fromIterable(page.getResults()))
                    .flatMap(udf -> {
                        RequestOptions requestOptions = new RequestOptions();

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = udf.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.partitionKey(new PartitionKey(propertyValue));
//                    }

                        return houseKeepingClient.deleteUserDefinedFunction(udf.getSelfLink(), requestOptions);
                    }).then().block();

        } finally {
            houseKeepingClient.close();
        }

        logger.info("Finished truncating collection {}.", collection.getId());
    }

    @SuppressWarnings("fallthrough")
    protected static void waitIfNeededForReplicasToCatchUp(Builder clientBuilder) {
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

    public static DocumentCollection createCollection(String databaseId,
                                                      DocumentCollection collection,
                                                      RequestOptions options) {
        AsyncDocumentClient client = createGatewayHouseKeepingDocumentClient().build();
        try {
            return client.createCollection("dbs/" + databaseId, collection, options).block().getResource();
        } finally {
            client.close();
        }
    }

    public static DocumentCollection createCollection(AsyncDocumentClient client, String databaseId,
                                                      DocumentCollection collection, RequestOptions options) {
        return client.createCollection("dbs/" + databaseId, collection, options).block().getResource();
    }

    public static DocumentCollection createCollection(AsyncDocumentClient client, String databaseId,
                                                      DocumentCollection collection) {
        return client.createCollection("dbs/" + databaseId, collection, null).block().getResource();
    }

    private static DocumentCollection getCollectionDefinitionMultiPartitionWithCompositeAndSpatialIndexes() {
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

        DocumentCollection documentCollection = new DocumentCollection();

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
        documentCollection.setIndexingPolicy(indexingPolicy);

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        ArrayList<String> partitionKeyPaths = new ArrayList<String>();
        partitionKeyPaths.add("/" + PARTITION_KEY);
        partitionKeyDefinition.setPaths(partitionKeyPaths);
        documentCollection.setPartitionKey(partitionKeyDefinition);

        documentCollection.setId(UUID.randomUUID().toString());

        return documentCollection;
    }

    public static Document createDocument(AsyncDocumentClient client, String databaseId, String collectionId, Document document) {
        return createDocument(client, databaseId, collectionId, document, null);
    }

    public static Document createDocument(AsyncDocumentClient client, String databaseId, String collectionId, Document document, RequestOptions options) {
        return client.createDocument(TestUtils.getCollectionNameLink(databaseId, collectionId), document, options, false).block().getResource();
    }

    public Flux<ResourceResponse<Document>> bulkInsert(AsyncDocumentClient client,
                                                             String collectionLink,
                                                             List<Document> documentDefinitionList,
                                                             int concurrencyLevel) {
        ArrayList<Mono<ResourceResponse<Document>>> result = new ArrayList<>(documentDefinitionList.size());
        for (Document docDef : documentDefinitionList) {
            result.add(client.createDocument(collectionLink, docDef, null, false));
        }

        return Flux.merge(Flux.fromIterable(result), concurrencyLevel).publishOn(Schedulers.parallel());
    }

    public Flux<ResourceResponse<Document>> bulkInsert(AsyncDocumentClient client,
                                                             String collectionLink,
                                                             List<Document> documentDefinitionList) {
        return bulkInsert(client, collectionLink, documentDefinitionList, DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL);
    }

    public static User createUser(AsyncDocumentClient client, String databaseId, User user) {
        return client.createUser("dbs/" + databaseId, user, null).block().getResource();
    }

    public static User safeCreateUser(AsyncDocumentClient client, String databaseId, User user) {
        deleteUserIfExists(client, databaseId, user.getId());
        return createUser(client, databaseId, user);
    }

    private static DocumentCollection safeCreateCollection(AsyncDocumentClient client, String databaseId, DocumentCollection collection, RequestOptions options) {
        deleteCollectionIfExists(client, databaseId, collection.getId());
        return createCollection(client, databaseId, collection, options);
    }

    public static String getCollectionLink(DocumentCollection collection) {
        return collection.getSelfLink();
    }

    static protected DocumentCollection getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }

    static protected DocumentCollection getCollectionDefinitionWithRangeRangeIndex() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath("/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setIndexingPolicy(indexingPolicy);
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }

    public static void deleteCollectionIfExists(AsyncDocumentClient client, String databaseId, String collectionId) {
        List<DocumentCollection> res = client.queryCollections("dbs/" + databaseId,
                                                               String.format("SELECT * FROM root r where r.id = '%s'", collectionId), null).single().block()
                .getResults();
        if (!res.isEmpty()) {
            deleteCollection(client, TestUtils.getCollectionNameLink(databaseId, collectionId));
        }
    }

    public static void deleteCollection(AsyncDocumentClient client, String collectionLink) {
        client.deleteCollection(collectionLink, null).block();
    }

    public static void deleteDocumentIfExists(AsyncDocumentClient client, String databaseId, String collectionId, String docId) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        PartitionKey pk = new PartitionKey(docId);
        options.setPartitionKey(pk);
        List<Document> res = client
                .queryDocuments(TestUtils.getCollectionNameLink(databaseId, collectionId), String.format("SELECT * FROM root r where r.id = '%s'", docId), options)
                .single().block().getResults();
        if (!res.isEmpty()) {
            deleteDocument(client, TestUtils.getDocumentNameLink(databaseId, collectionId, docId), pk);
        }
    }

    public static void safeDeleteDocument(AsyncDocumentClient client, String documentLink, RequestOptions options) {
        if (client != null && documentLink != null) {
            try {
                client.deleteDocument(documentLink, options).block();
            } catch (Exception e) {
                CosmosException dce = Utils.as(e, CosmosException.class);
                if (dce == null || dce.getStatusCode() != 404) {
                    throw e;
                }
            }
        }
    }

    public static void deleteDocument(AsyncDocumentClient client, String documentLink, PartitionKey pk) {
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(pk);
        client.deleteDocument(documentLink, options).block();
    }

    public static void deleteUserIfExists(AsyncDocumentClient client, String databaseId, String userId) {
        List<User> res = client
                .queryUsers("dbs/" + databaseId, String.format("SELECT * FROM root r where r.id = '%s'", userId), null)
                .single().block().getResults();
        if (!res.isEmpty()) {
            deleteUser(client, TestUtils.getUserNameLink(databaseId, userId));
        }
    }

    public static void deleteUser(AsyncDocumentClient client, String userLink) {
        client.deleteUser(userLink, null).block();
    }

    public static String getDatabaseLink(Database database) {
        return database.getSelfLink();
    }

    static private Database safeCreateDatabase(AsyncDocumentClient client, Database database) {
        safeDeleteDatabase(client, database.getId());
        return createDatabase(client, database);
    }

    static protected Database createDatabase(AsyncDocumentClient client, Database database) {
        Mono<ResourceResponse<Database>> databaseObservable = client.createDatabase(database, null);
        return databaseObservable.block().getResource();
    }

    static protected Database createDatabase(AsyncDocumentClient client, String databaseId) {
        Database databaseDefinition = new Database();
        databaseDefinition.setId(databaseId);
        return createDatabase(client, databaseDefinition);
    }

    static protected Database createDatabaseIfNotExists(AsyncDocumentClient client, String databaseId) {
        return client.queryDatabases(String.format("SELECT * FROM r where r.id = '%s'", databaseId), null).flatMap(p -> Flux.fromIterable(p.getResults())).switchIfEmpty(
                Flux.defer(() -> {

                    Database databaseDefinition = new Database();
                    databaseDefinition.setId(databaseId);

                    return client.createDatabase(databaseDefinition, null).map(ResourceResponse::getResource);
                })
        ).single().block();
    }

    static protected void safeDeleteDatabase(AsyncDocumentClient client, Database database) {
        if (database != null) {
            safeDeleteDatabase(client, database.getId());
        }
    }

    static protected void safeDeleteDatabase(AsyncDocumentClient client, String databaseId) {
        if (client != null) {
            try {
                client.deleteDatabase(TestUtils.getDatabaseNameLink(databaseId), null).block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeDeleteAllCollections(AsyncDocumentClient client, Database database) {
        if (database != null) {
            List<DocumentCollection> collections = client.readCollections(database.getSelfLink(), null)
                    .flatMap(p -> Flux.fromIterable(p.getResults()))
                    .collectList()
                    .single()
                    .block();

            for (DocumentCollection collection : collections) {
                client.deleteCollection(collection.getSelfLink(), null).block().getResource();
            }
        }
    }

    static protected void safeDeleteCollection(AsyncDocumentClient client, DocumentCollection collection) {
        if (client != null && collection != null) {
            try {
                client.deleteCollection(collection.getSelfLink(), null).block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeDeleteCollection(AsyncDocumentClient client, String databaseId, String collectionId) {
        if (client != null && databaseId != null && collectionId != null) {
            try {
                client.deleteCollection("/dbs/" + databaseId + "/colls/" + collectionId, null).block();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeCloseAsync(AsyncDocumentClient client) {
        if (client != null) {
            new Thread(() -> {
                try {
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    static protected void safeClose(AsyncDocumentClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public <T extends Resource> void validateSuccess(Mono<ResourceResponse<T>> observable,
                                                     ResourceResponseValidator<T> validator) {
        validateSuccess(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateSuccess(Mono<ResourceResponse<T>> observable,
                                                            ResourceResponseValidator<T> validator, long timeout) {

        TestSubscriber<ResourceResponse<T>> testSubscriber = new TestSubscriber<>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }

    public <T extends Resource> void validateFailure(Mono<ResourceResponse<T>> observable,
                                                     FailureValidator validator) {
        validateFailure(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateFailure(Mono<ResourceResponse<T>> observable,
                                                            FailureValidator validator, long timeout) {

        TestSubscriber<ResourceResponse<T>> testSubscriber = new TestSubscriber<>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errorCount()).isEqualTo(1);
        validator.validate((Throwable) testSubscriber.getEvents().get(1).get(0));
    }

    public <T extends Resource> void validateQuerySuccess(Flux<FeedResponse<T>> observable,
                                                          FeedResponseListValidator<T> validator) {
        validateQuerySuccess(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateQuerySuccess(Flux<FeedResponse<T>> observable,
                                                                 FeedResponseListValidator<T> validator, long timeout) {

        TestSubscriber<FeedResponse<T>> testSubscriber = new TestSubscriber<>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        validator.validate(testSubscriber.values());
    }

    public <T extends Resource> void validateQueryFailure(Flux<FeedResponse<T>> observable,
                                                          FailureValidator validator) {
        validateQueryFailure(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateQueryFailure(Flux<FeedResponse<T>> observable,
                                                                 FailureValidator validator, long timeout) {

        TestSubscriber<FeedResponse<T>> testSubscriber = new TestSubscriber<>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errorCount()).isEqualTo(1);
        validator.validate((Throwable) testSubscriber.getEvents().get(1).get(0));
    }

    @DataProvider
    public static Object[][] clientBuilders() {
        return new Object[][]{{createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true)}};
    }

    @DataProvider
    public static Object[][] clientBuildersWithSessionConsistency() {
        return new Object[][]{
                {createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true)},
                {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.HTTPS, false, null, true)},
                {createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null, true)}
        };
    }

    private static ConsistencyLevel parseConsistency(String consistency) {
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
        return simpleClientBuildersWithDirect(true, toArray(protocols));
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithDirectHttps() {
        return simpleClientBuildersWithDirect(true, Protocol.HTTPS);
    }

    private static Object[][] simpleClientBuildersWithDirect(boolean contentResponseOnWriteEnabled, Protocol... protocols) {
        logger.info("Max test consistency to use is [{}]", accountConsistency);
        List<ConsistencyLevel> testConsistencies = ImmutableList.of(ConsistencyLevel.EVENTUAL);

        boolean isMultiMasterEnabled = preferredLocations != null && accountConsistency == ConsistencyLevel.SESSION;

        List<Builder> builders = new ArrayList<>();
        builders.add(createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, contentResponseOnWriteEnabled));

        for (Protocol protocol : protocols) {
            testConsistencies.forEach(consistencyLevel -> builders.add(createDirectRxDocumentClient(consistencyLevel,
                                                                                                    protocol,
                                                                                                    isMultiMasterEnabled,
                                                                                                    preferredLocations, contentResponseOnWriteEnabled)));
        }

        builders.forEach(b -> logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
                                          b.getConnectionPolicy().getConnectionMode(),
                                          b.getDesiredConsistencyLevel(),
                                          b.getConfigs().getProtocol()
        ));

        return builders.stream().map(b -> new Object[]{b}).collect(Collectors.toList()).toArray(new Object[0][]);
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
        return protocols.toArray(new Protocol[0]);
    }

    private static Object[][] clientBuildersWithDirectSession(Protocol... protocols) {
        return clientBuildersWithDirect(new ArrayList<ConsistencyLevel>() {{
            add(ConsistencyLevel.SESSION);
        }}, true, protocols);
    }

    private static Object[][] clientBuildersWithDirectAllConsistencies(Protocol... protocols) {
        logger.info("Max test consistency to use is [{}]", accountConsistency);
        return clientBuildersWithDirect(desiredConsistencies, true, protocols);
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

    @SuppressWarnings("fallthrough")
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

    private static Object[][] clientBuildersWithDirect(List<ConsistencyLevel> testConsistencies, boolean contentResponseOnWriteEnabled, Protocol... protocols) {
        boolean isMultiMasterEnabled = preferredLocations != null && accountConsistency == ConsistencyLevel.SESSION;

        List<Builder> builders = new ArrayList<>();
        builders.add(createGatewayRxDocumentClient(ConsistencyLevel.SESSION, isMultiMasterEnabled, preferredLocations, contentResponseOnWriteEnabled));

        for (Protocol protocol : protocols) {
            testConsistencies.forEach(consistencyLevel -> builders.add(createDirectRxDocumentClient(consistencyLevel,
                                                                                                    protocol,
                                                                                                    isMultiMasterEnabled,
                                                                                                    preferredLocations, contentResponseOnWriteEnabled)));
        }

        builders.forEach(b -> logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
                                          b.getConnectionPolicy().getConnectionMode(),
                                          b.getDesiredConsistencyLevel(),
                                          b.getConfigs().getProtocol()
        ));

        return builders.stream().map(b -> new Object[]{b}).collect(Collectors.toList()).toArray(new Object[0][]);
    }

    static protected Builder createGatewayHouseKeepingDocumentClient() {
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        ThrottlingRetryOptions options = new ThrottlingRetryOptions();
        options.setMaxRetryWaitTime(Duration.ofSeconds(SUITE_SETUP_TIMEOUT));
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(gatewayConnectionConfig);
        connectionPolicy.setThrottlingRetryOptions(options);
        return new Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.SESSION).withContentResponseOnWriteEnabled(true);
    }

    static protected Builder createGatewayRxDocumentClient(ConsistencyLevel consistencyLevel, boolean multiMasterEnabled, List<String> preferredLocations, boolean contentResponseOnWriteEnabled) {
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(gatewayConnectionConfig);
        connectionPolicy.setMultipleWriteRegionsEnabled(multiMasterEnabled);
        connectionPolicy.setPreferredRegions(preferredLocations);
        return new Builder().withServiceEndpoint(TestConfigurations.HOST)
                            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                            .withConnectionPolicy(connectionPolicy)
                            .withConsistencyLevel(consistencyLevel)
                            .withContentResponseOnWriteEnabled(contentResponseOnWriteEnabled);
    }

    static protected Builder createGatewayRxDocumentClient() {
        return createGatewayRxDocumentClient(ConsistencyLevel.SESSION, false, null, true);
    }

    static protected Builder createDirectRxDocumentClient(ConsistencyLevel consistencyLevel,
                                                          Protocol protocol,
                                                          boolean multiMasterEnabled,
                                                          List<String> preferredRegions,
                                                          boolean contentResponseOnWriteEnabled) {
        DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(directConnectionConfig);
        if (preferredRegions != null) {
            connectionPolicy.setPreferredRegions(preferredRegions);
        }

        if (multiMasterEnabled && consistencyLevel == ConsistencyLevel.SESSION) {
            connectionPolicy.setMultipleWriteRegionsEnabled(true);
        }
        Configs configs = Mockito.spy(new Configs());
        doAnswer((Answer<Protocol>)invocation -> protocol).when(configs).getProtocol();

        return new Builder().withServiceEndpoint(TestConfigurations.HOST)
                            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                            .withConnectionPolicy(connectionPolicy)
                            .withConsistencyLevel(consistencyLevel)
                            .withConfigs(configs)
                            .withContentResponseOnWriteEnabled(contentResponseOnWriteEnabled);
    }

    protected int expectedNumberOfPages(int totalExpectedResult, int maxPageSize) {
        return Math.max((totalExpectedResult + maxPageSize - 1 ) / maxPageSize, 1);
    }

    @DataProvider(name = "queryMetricsArgProvider")
    public Object[][] queryMetricsArgProvider() {
        return new Object[][]{
                {true},
                {false},
                {null}
        };
    }
}
