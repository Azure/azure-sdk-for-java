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
package com.microsoft.azure.cosmosdb.rx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.DatabaseForTest;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.RetryOptions;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.Undefined;
import com.microsoft.azure.cosmosdb.internal.PathParser;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import com.microsoft.azure.cosmosdb.rx.internal.Configs;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.User;

import org.testng.annotations.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

public class TestSuiteBase {
    private static final int DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL = 500;
    protected static Logger logger = LoggerFactory.getLogger(TestSuiteBase.class.getSimpleName());
    protected static final int TIMEOUT = 8000;
    protected static final int FEED_TIMEOUT = 12000;
    protected static final int SETUP_TIMEOUT = 30000;
    protected static final int SHUTDOWN_TIMEOUT = 12000;

    protected static final int SUITE_SETUP_TIMEOUT = 120000;
    protected static final int SUITE_SHUTDOWN_TIMEOUT = 60000;

    protected static final int WAIT_REPLICA_CATCH_UP_IN_MILLIS = 2000;

    protected int subscriberValidationTimeout = TIMEOUT;

    protected static Database SHARED_DATABASE;
    protected static DocumentCollection SHARED_MULTI_PARTITION_COLLECTION;
    protected static DocumentCollection SHARED_SINGLE_PARTITION_COLLECTION;
    protected static DocumentCollection SHARED_SINGLE_PARTITION_COLLECTION_WITHOUT_PARTITION_KEY;

    protected TestSuiteBase() {
        logger.debug("Initializing {} ...", this.getClass().getSimpleName());
    }

    @BeforeMethod(groups = { "simple", "long", "direct", "multi-master", "emulator" })
    public void beforeMethod(Method m) {
        logger.info("Starting {}:{} ...", m.getDeclaringClass().getSimpleName(), m.getName());
    }

    @AfterMethod(groups = { "simple", "long", "direct", "multi-master", "emulator" })
    public void afterMethod(Method m) {
        Test t = m.getAnnotation(Test.class);
        logger.info("Finished {}:{}.", m.getDeclaringClass().getSimpleName(), m.getName());
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
        public Observable<FeedResponse<Database>> queryDatabases(SqlQuerySpec query) {
            return client.queryDatabases(query, null);
        }

        @Override
        public Observable<ResourceResponse<Database>> createDatabase(Database databaseDefinition) {
            return client.createDatabase(databaseDefinition, null);
        }

        @Override
        public Observable<ResourceResponse<Database>> deleteDatabase(String id) {

            return client.deleteDatabase("dbs/" + id, null);
        }
    }

    @BeforeSuite(groups = { "simple", "long", "direct", "multi-master", "emulator" }, timeOut = SUITE_SETUP_TIMEOUT)
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
            SHARED_SINGLE_PARTITION_COLLECTION_WITHOUT_PARTITION_KEY = createCollection(houseKeepingClient, SHARED_DATABASE.getId(), getCollectionDefinitionSinglePartitionWithoutPartitionKey());
        } finally {
            houseKeepingClient.close();
        }
    }

    @AfterSuite(groups = { "simple", "long", "direct", "multi-master", "emulator" }, timeOut = SUITE_SHUTDOWN_TIMEOUT)
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

            FeedOptions options = new FeedOptions();
            options.setMaxDegreeOfParallelism(-1);
            options.setEnableCrossPartitionQuery(true);
            options.setMaxItemCount(100);

            logger.info("Truncating collection {} documents ...", collection.getId());

            houseKeepingClient.queryDocuments(collection.getSelfLink(), "SELECT * FROM root", options)
                    .flatMap(page -> Observable.from(page.getResults()))
                    .flatMap(doc -> {
                        RequestOptions requestOptions = new RequestOptions();

                        if (paths != null && !paths.isEmpty()) {
                            List<String> pkPath = PathParser.getPathParts(paths.get(0));
                            Object propertyValue = doc.getObjectByPath(pkPath);
                            if (propertyValue == null) {
                                propertyValue = Undefined.Value();
                            }

                            requestOptions.setPartitionKey(new PartitionKey(propertyValue));
                        }

                        return houseKeepingClient.deleteDocument(doc.getSelfLink(), requestOptions);
                    }).toCompletable().await();

            logger.info("Truncating collection {} triggers ...", collection.getId());

            houseKeepingClient.queryTriggers(collection.getSelfLink(), "SELECT * FROM root", options)
                    .flatMap(page -> Observable.from(page.getResults()))
                    .flatMap(trigger -> {
                        RequestOptions requestOptions = new RequestOptions();

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = trigger.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.setPartitionKey(new PartitionKey(propertyValue));
//                    }

                        return houseKeepingClient.deleteTrigger(trigger.getSelfLink(), requestOptions);
                    }).toCompletable().await();

            logger.info("Truncating collection {} storedProcedures ...", collection.getId());

            houseKeepingClient.queryStoredProcedures(collection.getSelfLink(), "SELECT * FROM root", options)
                    .flatMap(page -> Observable.from(page.getResults()))
                    .flatMap(storedProcedure -> {
                        RequestOptions requestOptions = new RequestOptions();

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = storedProcedure.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.setPartitionKey(new PartitionKey(propertyValue));
//                    }

                        return houseKeepingClient.deleteStoredProcedure(storedProcedure.getSelfLink(), requestOptions);
                    }).toCompletable().await();

            logger.info("Truncating collection {} udfs ...", collection.getId());

            houseKeepingClient.queryUserDefinedFunctions(collection.getSelfLink(), "SELECT * FROM root", options)
                    .flatMap(page -> Observable.from(page.getResults()))
                    .flatMap(udf -> {
                        RequestOptions requestOptions = new RequestOptions();

//                    if (paths != null && !paths.isEmpty()) {
//                        Object propertyValue = udf.getObjectByPath(PathParser.getPathParts(paths.get(0)));
//                        requestOptions.setPartitionKey(new PartitionKey(propertyValue));
//                    }

                        return houseKeepingClient.deleteUserDefinedFunction(udf.getSelfLink(), requestOptions);
                    }).toCompletable().await();

        } finally {
            houseKeepingClient.close();
        }

        logger.info("Finished truncating collection {}.", collection.getId());
    }

    protected static void waitIfNeededForReplicasToCatchUp(AsyncDocumentClient.Builder clientBuilder) {
        switch (clientBuilder.desiredConsistencyLevel) {
            case Eventual:
            case ConsistentPrefix:
                logger.info(" additional wait in Eventual mode so the replica catch up");
                // give times to replicas to catch up after a write
                try {
                    TimeUnit.MILLISECONDS.sleep(WAIT_REPLICA_CATCH_UP_IN_MILLIS);
                } catch (Exception e) {
                    logger.error("unexpected failure", e);
                }

            case Session:
            case BoundedStaleness:
            case Strong:
            default:
                break;
        }
    }

    private static DocumentCollection getCollectionDefinitionSinglePartitionWithoutPartitionKey() {
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        return collectionDefinition;
    }


    public static DocumentCollection createCollection(String databaseId,
                                                      DocumentCollection collection,
                                                      RequestOptions options) {
        AsyncDocumentClient client = createGatewayHouseKeepingDocumentClient().build();
        try {
            return client.createCollection("dbs/" + databaseId, collection, options).toBlocking().single().getResource();
        } finally {
            client.close();
        }
    }

    public static DocumentCollection createCollection(AsyncDocumentClient client, String databaseId,
                                                      DocumentCollection collection, RequestOptions options) {
        return client.createCollection("dbs/" + databaseId, collection, options).toBlocking().single().getResource();
    }

    public static DocumentCollection createCollection(AsyncDocumentClient client, String databaseId,
                                                      DocumentCollection collection) {
        return client.createCollection("dbs/" + databaseId, collection, null).toBlocking().single().getResource();
    }

    public static Document createDocument(AsyncDocumentClient client, String databaseId, String collectionId, Document document) {
        return createDocument(client, databaseId, collectionId, document, null);
    }

    public static Document createDocument(AsyncDocumentClient client, String databaseId, String collectionId, Document document, RequestOptions options) {
        return client.createDocument(Utils.getCollectionNameLink(databaseId, collectionId), document, options, false).toBlocking().single().getResource();
    }

    public Observable<ResourceResponse<Document>> bulkInsert(AsyncDocumentClient client,
                                                             String collectionLink,
                                                             List<Document> documentDefinitionList,
                                                             int concurrencyLevel) {
        ArrayList<Observable<ResourceResponse<Document>>> result = new ArrayList<Observable<ResourceResponse<Document>>>(documentDefinitionList.size());
        for (Document docDef : documentDefinitionList) {
            result.add(client.createDocument(collectionLink, docDef, null, false));
        }

        return Observable.merge(result, concurrencyLevel);
    }

    public Observable<ResourceResponse<Document>> bulkInsert(AsyncDocumentClient client,
                                                             String collectionLink,
                                                             List<Document> documentDefinitionList) {
        return bulkInsert(client, collectionLink, documentDefinitionList, DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL);
    }

    public List<Document> bulkInsertBlocking(AsyncDocumentClient client,
                                             String collectionLink,
                                             List<Document> documentDefinitionList) {
        return bulkInsert(client, collectionLink, documentDefinitionList, DEFAULT_BULK_INSERT_CONCURRENCY_LEVEL)
                .map(ResourceResponse::getResource)
                .toList()
                .toBlocking()
                .single();
    }

    public static ConsistencyLevel getAccountDefaultConsistencyLevel(AsyncDocumentClient client) {
        return client.getDatabaseAccount().toBlocking().single().getConsistencyPolicy().getDefaultConsistencyLevel();
    }

    public static User createUser(AsyncDocumentClient client, String databaseId, User user) {
        return client.createUser("dbs/" + databaseId, user, null).toBlocking().single().getResource();
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
        Collection<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.String);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.Number);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.setIndexes(indexes);
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
                                                               String.format("SELECT * FROM root r where r.id = '%s'", collectionId), null).toBlocking().single()
                .getResults();
        if (!res.isEmpty()) {
            deleteCollection(client, Utils.getCollectionNameLink(databaseId, collectionId));
        }
    }

    public static void deleteCollection(AsyncDocumentClient client, String collectionLink) {
        client.deleteCollection(collectionLink, null).toBlocking().single();
    }

    public static void deleteDocumentIfExists(AsyncDocumentClient client, String databaseId, String collectionId, String docId) {
        FeedOptions options = new FeedOptions();
        options.setPartitionKey(new PartitionKey(docId));
        List<Document> res = client
                .queryDocuments(Utils.getCollectionNameLink(databaseId, collectionId), String.format("SELECT * FROM root r where r.id = '%s'", docId), options)
                .toBlocking().single().getResults();
        if (!res.isEmpty()) {
            deleteDocument(client, Utils.getDocumentNameLink(databaseId, collectionId, docId));
        }
    }

    public static void safeDeleteDocument(AsyncDocumentClient client, String documentLink, RequestOptions options) {
        if (client != null && documentLink != null) {
            try {
                client.deleteDocument(documentLink, options).toBlocking().single();
            } catch (Exception e) {
                DocumentClientException dce = com.microsoft.azure.cosmosdb.rx.internal.Utils.as(e, DocumentClientException.class);
                if (dce == null || dce.getStatusCode() != 404) {
                    throw e;
                }
            }
        }
    }

    public static void deleteDocument(AsyncDocumentClient client, String documentLink) {
        client.deleteDocument(documentLink, null).toBlocking().single();
    }

    public static void deleteUserIfExists(AsyncDocumentClient client, String databaseId, String userId) {
        List<User> res = client
                .queryUsers("dbs/" + databaseId, String.format("SELECT * FROM root r where r.id = '%s'", userId), null)
                .toBlocking().single().getResults();
        if (!res.isEmpty()) {
            deleteUser(client, Utils.getUserNameLink(databaseId, userId));
        }
    }

    public static void deleteUser(AsyncDocumentClient client, String userLink) {
        client.deleteUser(userLink, null).toBlocking().single();
    }

    public static String getDatabaseLink(Database database) {
        return database.getSelfLink();
    }

    static private Database safeCreateDatabase(AsyncDocumentClient client, Database database) {
        safeDeleteDatabase(client, database.getId());
        return createDatabase(client, database);
    }

    static protected Database createDatabase(AsyncDocumentClient client, Database database) {
        Observable<ResourceResponse<Database>> databaseObservable = client.createDatabase(database, null);
        return databaseObservable.toBlocking().single().getResource();
    }

    static protected Database createDatabase(AsyncDocumentClient client, String databaseId) {
        Database databaseDefinition = new Database();
        databaseDefinition.setId(databaseId);
        return createDatabase(client, databaseDefinition);
    }

    static protected Database createDatabaseIfNotExists(AsyncDocumentClient client, String databaseId) {
        return client.queryDatabases(String.format("SELECT * FROM r where r.id = '%s'", databaseId), null).flatMap(p -> Observable.from(p.getResults())).switchIfEmpty(
                Observable.defer(() -> {

                    Database databaseDefinition = new Database();
                    databaseDefinition.setId(databaseId);

                    return client.createDatabase(databaseDefinition, null).map(ResourceResponse::getResource);
                })
        ).toBlocking().single();
    }

    static protected void safeDeleteDatabase(AsyncDocumentClient client, Database database) {
        if (database != null) {
            safeDeleteDatabase(client, database.getId());
        }
    }

    static protected void safeDeleteDatabase(AsyncDocumentClient client, String databaseId) {
        if (client != null) {
            try {
                client.deleteDatabase(Utils.getDatabaseNameLink(databaseId), null).toBlocking().single();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeDeleteCollection(AsyncDocumentClient client, DocumentCollection collection) {
        if (client != null && collection != null) {
            try {
                client.deleteCollection(collection.getSelfLink(), null).toBlocking().single();
            } catch (Exception e) {
            }
        }
    }

    static protected void safeDeleteCollection(AsyncDocumentClient client, String databaseId, String collectionId) {
        if (client != null && databaseId != null && collectionId != null) {
            try {
                client.deleteCollection("/dbs/" + databaseId + "/colls/" + collectionId, null).toBlocking().single();
            } catch (Exception e) {
            }
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

    public <T extends Resource> void validateSuccess(Observable<ResourceResponse<T>> observable,
                                                     ResourceResponseValidator<T> validator) {
        validateSuccess(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateSuccess(Observable<ResourceResponse<T>> observable,
                                                            ResourceResponseValidator<T> validator, long timeout) {

        TestSubscriber<ResourceResponse<T>> testSubscriber = new TestSubscriber<ResourceResponse<T>>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.getOnNextEvents().get(0));
    }

    public <T extends Resource> void validateFailure(Observable<ResourceResponse<T>> observable,
                                                     FailureValidator validator) {
        validateFailure(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateFailure(Observable<ResourceResponse<T>> observable,
                                                            FailureValidator validator, long timeout) {

        TestSubscriber<ResourceResponse<T>> testSubscriber = new TestSubscriber<>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotCompleted();
        testSubscriber.assertTerminalEvent();
        assertThat(testSubscriber.getOnErrorEvents()).hasSize(1);
        validator.validate(testSubscriber.getOnErrorEvents().get(0));
    }

    public <T extends Resource> void validateQuerySuccess(Observable<FeedResponse<T>> observable,
                                                          FeedResponseListValidator<T> validator) {
        validateQuerySuccess(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateQuerySuccess(Observable<FeedResponse<T>> observable,
                                                                 FeedResponseListValidator<T> validator, long timeout) {

        TestSubscriber<FeedResponse<T>> testSubscriber = new TestSubscriber<>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        validator.validate(testSubscriber.getOnNextEvents());
    }

    public <T extends Resource> void validateQueryFailure(Observable<FeedResponse<T>> observable,
                                                          FailureValidator validator) {
        validateQueryFailure(observable, validator, subscriberValidationTimeout);
    }

    public static <T extends Resource> void validateQueryFailure(Observable<FeedResponse<T>> observable,
                                                                 FailureValidator validator, long timeout) {

        TestSubscriber<FeedResponse<T>> testSubscriber = new TestSubscriber<>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotCompleted();
        testSubscriber.assertTerminalEvent();
        assertThat(testSubscriber.getOnErrorEvents()).hasSize(1);
        validator.validate(testSubscriber.getOnErrorEvents().get(0));
    }

    @DataProvider
    public static Object[][] clientBuilders() {
        return new Object[][] { { createGatewayRxDocumentClient(ConsistencyLevel.Session) } };
    }

    @DataProvider
    public static Object[][] clientBuildersWithSessionConsistency() {
        return new Object[][] {
                { createGatewayRxDocumentClient(ConsistencyLevel.Session) },
                { createDirectRxDocumentClient(ConsistencyLevel.Session, Protocol.Https) },
                { createDirectRxDocumentClient(ConsistencyLevel.Session, Protocol.Tcp) }
        };
    }

    private static ConsistencyLevel parseConsistency(String consistency) {
        if (consistency != null) {
            for (ConsistencyLevel consistencyLevel : ConsistencyLevel.values()) {
                if (consistencyLevel.name().toLowerCase().equals(consistency.toLowerCase())) {
                    return consistencyLevel;
                }
            }
        }

        logger.error("Invalid configured test consistency [{}].", consistency);
        throw new IllegalStateException("Invalid configured test consistency " + consistency);
    }

    @DataProvider
    public static Object[][] simpleClientBuildersWithDirect() {

        ConsistencyLevel accountConsistency = parseConsistency(TestConfigurations.CONSISTENCY);
        logger.info("Max test consistency to use is [{}]", accountConsistency);
        List<ConsistencyLevel> testConsistencies = new ArrayList<>();

        switch (accountConsistency) {
            case Strong:
            case BoundedStaleness:
            case Session:
            case ConsistentPrefix:
            case Eventual:
                testConsistencies.add(ConsistencyLevel.Eventual);
                break;
            default:
                throw new IllegalStateException("Invalid configured test consistency " + accountConsistency);
        }

        List<AsyncDocumentClient.Builder> builders = new ArrayList<>();
        builders.add(createGatewayRxDocumentClient(ConsistencyLevel.Session));

        for (Protocol protocol : Protocol.values()) {
            testConsistencies.forEach(consistencyLevel -> builders.add(createDirectRxDocumentClient(consistencyLevel, protocol)));
        }

        builders.forEach(b -> logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
            b.connectionPolicy.getConnectionMode(),
            b.desiredConsistencyLevel,
            b.configs.getProtocol()
        ));

        return builders.stream().map(b -> new Object[]{b}).collect(Collectors.toList()).toArray(new Object[0][]);
    }

    @DataProvider
    public static Object[][] clientBuildersWithDirect() {

        ConsistencyLevel accountConsistency = parseConsistency(TestConfigurations.CONSISTENCY);
        logger.info("Max test consistency to use is [{}]", accountConsistency);
        List<ConsistencyLevel> testConsistencies = new ArrayList<>();

        switch (accountConsistency) {
            case Strong:
                testConsistencies.add(ConsistencyLevel.Strong);
            case BoundedStaleness:
                testConsistencies.add(ConsistencyLevel.BoundedStaleness);
            case Session:
                testConsistencies.add(ConsistencyLevel.Session);
            case ConsistentPrefix:
                testConsistencies.add(ConsistencyLevel.ConsistentPrefix);
            case Eventual:
                testConsistencies.add(ConsistencyLevel.Eventual);
                break;
            default:
                throw new IllegalStateException("Invalid configured test consistency " + accountConsistency);
        }

        List<AsyncDocumentClient.Builder> builders = new ArrayList<>();
        builders.add(createGatewayRxDocumentClient(ConsistencyLevel.Session));

        for (Protocol protocol : Protocol.values()) {
            testConsistencies.forEach(consistencyLevel -> builders.add(createDirectRxDocumentClient(consistencyLevel, protocol)));
        }

        builders.forEach(b -> logger.info("Will Use ConnectionMode [{}], Consistency [{}], Protocol [{}]",
            b.connectionPolicy.getConnectionMode(),
            b.desiredConsistencyLevel,
            b.configs.getProtocol()
        ));

        return builders.stream().map(b -> new Object[]{b}).collect(Collectors.toList()).toArray(new Object[0][]);
    }

    static protected AsyncDocumentClient.Builder createGatewayHouseKeepingDocumentClient() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Gateway);
        RetryOptions options = new RetryOptions();
        options.setMaxRetryWaitTimeInSeconds(SUITE_SETUP_TIMEOUT);
        connectionPolicy.setRetryOptions(options);
        return new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session);
    }

    static protected AsyncDocumentClient.Builder createGatewayRxDocumentClient(ConsistencyLevel consistencyLevel) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Gateway);
        return new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(consistencyLevel);
    }

    static protected AsyncDocumentClient.Builder createGatewayRxDocumentClient() {
        return createGatewayRxDocumentClient(ConsistencyLevel.Session);
    }

    static protected AsyncDocumentClient.Builder createDirectRxDocumentClient(ConsistencyLevel consistencyLevel, Protocol protocol) {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Direct);

        Configs configs = spy(new Configs());
        doAnswer((Answer<Protocol>)invocation -> protocol).when(configs).getProtocol();

        return new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(consistencyLevel)
                .withConfigs(configs);
    }

    protected int expectedNumberOfPages(int totalExpectedResult, int maxPageSize) {
        return Math.max((totalExpectedResult + maxPageSize - 1 ) / maxPageSize, 1);
    }
}
