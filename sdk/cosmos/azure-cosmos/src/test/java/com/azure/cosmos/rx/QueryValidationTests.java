// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.JsonNode;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryValidationTests extends TestSuiteBase {
    private static final int DEFAULT_NUM_DOCUMENTS = 1000;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdContainer;
    private Random random;

    private CosmosAsyncClient client;
    private List<TestObject> createdDocuments = new ArrayList<>();

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public QueryValidationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        random = new Random();
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        System.setProperty("COSMOS.QUERYPLAN_CACHING_ENABLED", "true");
        client = this.getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
        createdContainer = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdContainer);

        createdDocuments.addAll(this.insertDocuments(DEFAULT_NUM_DOCUMENTS, null, createdContainer));
    }

    @Test(groups = {"unit"}, priority = 1)
    public void queryPlanCacheEnabledFlag() {
        System.setProperty("COSMOS.QUERYPLAN_CACHING_ENABLED", "false");
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder();
        assertThat(Configs.isQueryPlanCachingEnabled()).isFalse();
        System.setProperty("COSMOS.QUERYPLAN_CACHING_ENABLED", "true");
        assertThat(Configs.isQueryPlanCachingEnabled()).isTrue();
        System.setProperty("COSMOS.QUERYPLAN_CACHING_ENABLED", "false");
        assertThat(Configs.isQueryPlanCachingEnabled()).isFalse();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void orderByQuery() {
        /*
        The idea here is to query documents in pages, query all the documents(with pagesize as num_documents and compare
         the results.
         */
        String query = "select * from c order by c.prop ASC";
        queryWithOrderByAndAssert(
            DEFAULT_PAGE_SIZE,
            DEFAULT_NUM_DOCUMENTS,
            query,
            createdContainer,
            d -> d.getProp(),
            createdDocuments);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT * 2)
    public void orderByQueryForLargeCollection() {
        CosmosContainerProperties containerProperties = getCollectionDefinition();
        createdDatabase.createContainer(
            containerProperties,
            ThroughputProperties.createManualThroughput(100000), // Create container with large number physical partitions
            new CosmosContainerRequestOptions()
        ).block();

        CosmosAsyncContainer container = createdDatabase.getContainer(containerProperties.getId());

        int partitionDocCount = 5;
        int pageSize = partitionDocCount + 1;

        String partition1Key = UUID.randomUUID().toString();
        String partition2Key = UUID.randomUUID().toString();

        List<TestObject> documentsInserted = new ArrayList<>();
        documentsInserted.addAll(this.insertDocuments(
            partitionDocCount,
            Collections.singletonList(partition1Key),
            container));
        documentsInserted.addAll(this.insertDocuments(
            partitionDocCount,
            Collections.singletonList(partition2Key),
            container));

        String query = String.format(
            "select * from c where c.mypk in ('%s', '%s') order by c.constantProp DESC",
            partition1Key,
            partition2Key);

        queryWithOrderByAndAssert(
            pageSize,
            partitionDocCount * 2,
            query,
            container,
            d -> d.getConstantProp(),
            documentsInserted);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryOptionNullValidation() {
        String query = "Select top 1 * from c";

        //Database null query option check
        FeedResponse<CosmosDatabaseProperties> databases = client.queryDatabases(query, null).byPage(1).blockFirst();
        assertThat(databases.getResults().size()).isEqualTo(1);
        databases = client.queryDatabases(new SqlQuerySpec(query), null).byPage(1).blockFirst();
        assertThat(databases.getResults().size()).isEqualTo(1);

        //Container null query check
        FeedResponse<CosmosContainerProperties> containers =
            createdDatabase.readAllContainers(null).byPage(1).blockFirst();
        assertThat(containers.getResults().size()).isGreaterThanOrEqualTo(1);
        containers = createdDatabase.queryContainers(query, null).byPage(1).blockFirst();
        assertThat(containers.getResults().size()).isEqualTo(1);
        containers = createdDatabase.queryContainers(new SqlQuerySpec(query), null).byPage(1).blockFirst();
        assertThat(containers.getResults().size()).isEqualTo(1);

        //User null query check
        CosmosUserProperties userProperties = new CosmosUserProperties();
        userProperties.setId(UUID.randomUUID().toString());
        createdDatabase.createUser(userProperties).block();
        FeedResponse<CosmosUserProperties> users = createdDatabase.queryUsers(query, null).byPage(1).blockFirst();
        assertThat(users.getResults().size()).isEqualTo(1);
        users = createdDatabase.queryUsers(new SqlQuerySpec(query), null).byPage(1).blockFirst();
        assertThat(users.getResults().size()).isEqualTo(1);

        //Permission null query check
        CosmosPermissionProperties cosmosPermissionProperties = new CosmosPermissionProperties();
        cosmosPermissionProperties.setContainerName(createdContainer.getId());
        cosmosPermissionProperties.setPermissionMode(PermissionMode.READ);
        cosmosPermissionProperties.setId(UUID.randomUUID().toString());
        createdDatabase.getUser(userProperties.getId()).createPermission(cosmosPermissionProperties, null).block();

        FeedResponse<CosmosPermissionProperties> permissions =
            createdDatabase.getUser(userProperties.getId()).queryPermissions(query, null).byPage(1).blockFirst();
        assertThat(permissions.getResults().size()).isEqualTo(1);

        //Item null query check
        FeedResponse<TestObject> items =
            createdContainer.queryItems(query, null, TestObject.class).byPage(1).blockFirst();
        assertThat(items.getResults().size()).isEqualTo(1);
        items = createdContainer.queryItems(new SqlQuerySpec(query), null, TestObject.class).byPage(1).blockFirst();
        assertThat(items.getResults().size()).isEqualTo(1);

        createdContainer.getScripts().createStoredProcedure(getCosmosStoredProcedureProperties()).block();
        createdContainer.getScripts().createTrigger(getCosmosTriggerProperties()).block();
        createdContainer.getScripts().createUserDefinedFunction(getCosmosUserDefinedFunctionProperties()).block();

        //Sproc null query check
        FeedResponse<CosmosStoredProcedureProperties> sprocs =
            createdContainer.getScripts().queryStoredProcedures(query, null).byPage(1).blockFirst();
        assertThat(sprocs.getResults().size()).isEqualTo(1);
        sprocs =
            createdContainer.getScripts().queryStoredProcedures(new SqlQuerySpec(query), null).byPage(1).blockFirst();
        assertThat(sprocs.getResults().size()).isEqualTo(1);

        //Trigger null query check
        FeedResponse<CosmosTriggerProperties> triggers =
            createdContainer.getScripts().queryTriggers(query, null).byPage(1).blockFirst();
        assertThat(triggers.getResults().size()).isEqualTo(1);
        triggers = createdContainer.getScripts().queryTriggers(new SqlQuerySpec(query), null).byPage(1).blockFirst();
        assertThat(triggers.getResults().size()).isEqualTo(1);

        //Udf null query check
        FeedResponse<CosmosUserDefinedFunctionProperties> udfs =
            createdContainer.getScripts().queryUserDefinedFunctions(query, null).byPage(1).blockFirst();
        assertThat(udfs.getResults().size()).isEqualTo(1);
        udfs =
            createdContainer.getScripts().queryUserDefinedFunctions(new SqlQuerySpec(query), null).byPage(1).blockFirst();
        assertThat(udfs.getResults().size()).isEqualTo(1);

        //Conflict null query check
        try {
            createdContainer.queryConflicts(query, null).byPage(1).blockFirst();
        } catch (CosmosException exception) {
            // It should give bad request exception for not having partition key but not a null pointer.
            assertThat(exception.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.BADREQUEST);
        }
        createdContainer.readAllConflicts(null).byPage(1).blockFirst();
    }

    @DataProvider(name = "query")
    private Object[][] query() {
        return new Object[][]{
            new Object[] { "Select * from c "},
            new Object[] { "select * from c order by c.prop ASC"},
        };
    }

    @Test(groups = {"simple"}, dataProvider = "query", timeOut = TIMEOUT)
    public void queryPlanCacheSinglePartitionCorrectness(String query) {

        String pk1 = "pk1";
        String pk2 = "pk2";
        int partitionDocCount = 5;

        List<TestObject> documentsInserted = new ArrayList<>();
        documentsInserted.addAll(this.insertDocuments(
            partitionDocCount,
            Collections.singletonList(pk1),
            createdContainer));
        AsyncDocumentClient contextClient = CosmosBridgeInternal.getContextClient(createdContainer);
        documentsInserted.addAll(this.insertDocuments(
            partitionDocCount,
            Collections.singletonList(pk2),
            createdContainer));

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setPartitionKey(new PartitionKey(pk2));

        // As we are enabling caching on this client, the query might be cached on other tests using same client so
        // disabling below check
        // assertThat(contextClient.getQueryPlanCache().containsKey(query)).isFalse();
        List<TestObject> values1 = queryAndGetResults(new SqlQuerySpec(query), options, TestObject.class);
        List<String> ids1 = values1.stream().map(TestObject::getId).collect(Collectors.toList());
        // Second time the query plan has to be fetched from the query plan cache and complete the query.
        assertThat(contextClient.getQueryPlanCache().containsKey(query)).isTrue();
        List<TestObject> values2 = queryAndGetResults(new SqlQuerySpec(query), options, TestObject.class);
        List<String> ids2 = values2.stream().map(TestObject::getId).collect(Collectors.toList());

        assertThat(ids1).containsExactlyElementsOf(ids2);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryPlanCacheSinglePartitionParameterizedQueriesCorrectness() {
        SqlQuerySpec sqlQuerySpec = new SqlQuerySpec();
        sqlQuerySpec.setQueryText("select * from c where c.id = @id");

        String pk1 = "pk1";
        String pk2 = "pk2";
        int partitionDocCount = 5;

        List<TestObject> documentsInserted = new ArrayList<>();
        documentsInserted.addAll(this.insertDocuments(
            partitionDocCount,
            Collections.singletonList(pk1),
            createdContainer));
        AsyncDocumentClient contextClient = CosmosBridgeInternal.getContextClient(createdContainer);
        List<TestObject> pk2Docs = this.insertDocuments(
            partitionDocCount,
            Collections.singletonList(pk2),
            createdContainer);
        documentsInserted.addAll(pk2Docs);


        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setPartitionKey(new PartitionKey(pk2));

        sqlQuerySpec.setParameters(Collections.singletonList(new SqlParameter("@id", pk2Docs.get(0).getId())));
        assertThat(contextClient.getQueryPlanCache().containsKey(sqlQuerySpec.getQueryText())).isFalse();
        List<TestObject> values1 = queryAndGetResults(sqlQuerySpec, options, TestObject.class);
        List<String> ids1 = values1.stream().map(TestObject::getId).collect(Collectors.toList());

        // Second time the query plan has to be fetched from the query plan cache and complete the query.
        sqlQuerySpec.setParameters(Collections.singletonList(new SqlParameter("@id", pk2Docs.get(1).getId())));
        assertThat(contextClient.getQueryPlanCache().containsKey(sqlQuerySpec.getQueryText())).isTrue();
        List<TestObject> values2 = queryAndGetResults(sqlQuerySpec, options, TestObject.class);
        List<String> ids2 = values2.stream().map(TestObject::getId).collect(Collectors.toList());

        // Since the value of parameters changed, query results should not match
        assertThat(ids1).doesNotContainAnyElementsOf(ids2);

        sqlQuerySpec.setQueryText("select top @top * from c");
        int topValue = 2;
        sqlQuerySpec.setParameters(Collections.singletonList(new SqlParameter("@top", 2)));
        assertThat(contextClient.getQueryPlanCache().containsKey(sqlQuerySpec.getQueryText())).isFalse();
        values1 = queryAndGetResults(sqlQuerySpec, options, TestObject.class);
        // Top query should not be cached
        assertThat(contextClient.getQueryPlanCache().containsKey(sqlQuerySpec.getQueryText())).isFalse();

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT * 20)
    public void splitQueryContinuationToken() throws Exception {
        String containerId = "splittestcontainer_" + UUID.randomUUID();
        int itemCount = 20;

        //Create container
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/mypk");
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties).block();
        CosmosAsyncContainer container = createdDatabase.getContainer(containerId);
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(this.client);

        //Insert some documents
        List<TestObject> testObjects = insertDocuments(itemCount, Arrays.asList("CA", "US"), container);

        String query = "Select * from c";
        String orderByQuery = "select * from c order by c.prop";

        List<PartitionKeyRange> partitionKeyRanges = getPartitionKeyRanges(containerId, asyncDocumentClient);
        String requestContinuation = null;
        String orderByRequestContinuation = null;
        int preferredPageSize = 15;
        ArrayList<TestObject> resultList = new ArrayList<>();
        ArrayList<TestObject> orderByResultList = new ArrayList<>();
        ArrayList<TestObject> initialOrderByResultList = new ArrayList<>();

        // Query
        FeedResponse<TestObject> jsonNodeFeedResponse = container
                                                            .queryItems(query, new CosmosQueryRequestOptions(), TestObject.class)
                                                            .byPage(preferredPageSize).blockFirst();
        assert jsonNodeFeedResponse != null;
        resultList.addAll(jsonNodeFeedResponse.getResults());
        requestContinuation = jsonNodeFeedResponse.getContinuationToken();

        // Initial OrderBy query
        Flux<FeedResponse<TestObject>> orderByResponseFlux = container
            .queryItems(orderByQuery, new CosmosQueryRequestOptions(),
                TestObject.class)
            .byPage(preferredPageSize);

        for (FeedResponse<TestObject> nodeFeedResponse : orderByResponseFlux.toIterable()) {
            initialOrderByResultList.addAll(nodeFeedResponse.getResults());
        }

        // Orderby query
        FeedResponse<TestObject> orderByFeedResponse = container
                                                           .queryItems(orderByQuery, new CosmosQueryRequestOptions(),
                                                                       TestObject.class)
                                                           .byPage(preferredPageSize).blockFirst();
        assert orderByFeedResponse != null;
        orderByResultList.addAll(orderByFeedResponse.getResults());
        orderByRequestContinuation = orderByFeedResponse.getContinuationToken();

        // Scale up the throughput for a split
        logger.info("Scaling up throughput for split");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(16000);
        ThroughputResponse throughputResponse = container.replaceThroughput(throughputProperties).block();
        logger.info("Throughput replace request submitted for {} ",
                    throughputResponse.getProperties().getManualThroughput());
        throughputResponse = container.readThroughput().block();


        // Wait for the throughput update to complete so that we get the partition split
        while (true) {
            assert throughputResponse != null;
            if (!throughputResponse.isReplacePending()) {
                break;
            }
            logger.info("Waiting for split to complete");
            Thread.sleep(10 * 1000);
            throughputResponse = container.readThroughput().block();
        }

        logger.info("Resuming query from the continuation");
        // Read number of partitions. Should be greater than one
        List<PartitionKeyRange> partitionKeyRangesAfterSplit = getPartitionKeyRanges(containerId, asyncDocumentClient);
        assertThat(partitionKeyRangesAfterSplit.size()).isGreaterThan(partitionKeyRanges.size())
            .as("Partition ranges should increase after split");
        logger.info("After split num partitions = {}", partitionKeyRangesAfterSplit.size());

        // Reading item to refresh cache
        container.readItem(testObjects.get(0).getId(), new PartitionKey(testObjects.get(0).getMypk()),
                           JsonNode.class).block();

        // Resume the query with continuation token saved above and make sure you get all the documents
        Flux<FeedResponse<TestObject>> feedResponseFlux = container
                                                              .queryItems(query, new CosmosQueryRequestOptions(),
                                                                          TestObject.class)
                                                              .byPage(requestContinuation, preferredPageSize);

        for (FeedResponse<TestObject> nodeFeedResponse : feedResponseFlux.toIterable()) {
            resultList.addAll(nodeFeedResponse.getResults());
        }

        // Resume the orderby query with continuation token saved above and make sure you get all the documents
        Flux<FeedResponse<TestObject>> orderfeedResponseFlux = container
                                                                   .queryItems(orderByQuery, new CosmosQueryRequestOptions(),
                                                                               TestObject.class)
                                                                   .byPage(orderByRequestContinuation, preferredPageSize);

        for (FeedResponse<TestObject> nodeFeedResponse : orderfeedResponseFlux.toIterable()) {
            orderByResultList.addAll(nodeFeedResponse.getResults());
        }

        List<String> sourceIds = testObjects.stream().map(TestObject::getId).collect(Collectors.toList());
        List<String> resultIds = resultList.stream().map(TestObject::getId).collect(Collectors.toList());
        List<String> orderResultIds = orderByResultList.stream().map(TestObject::getId).collect(Collectors.toList());
        List<String> initialOrderedResultIds = initialOrderByResultList.stream().map(TestObject::getId).collect(Collectors.toList());

        assertThat(resultIds).containsExactlyInAnyOrderElementsOf(sourceIds)
            .as("Resuming query from continuation token after split validated");

        assertThat(orderResultIds).containsExactlyElementsOf(initialOrderedResultIds)
            .as("Resuming orderby query from continuation token after split validated");

        container.delete().block();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void queryLargePartitionKeyOn100BPKCollection() throws Exception {
        String containerId = "testContainer_" + UUID.randomUUID();
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/id");
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties).block();
        CosmosAsyncContainer container = createdDatabase.getContainer(containerId);
        //id as partitionkey > 100bytes
        String itemID1 = "cosmosdb" +
                             "-drWarm4Z60GkknMfHLo5BwuiH7w6AffzSb9jKbvwAQwaRZd10oxnLeCueuyZ5gbm9dwVVAqJLdzrB38Dk73Q6xMErv-0";
        String itemID2 = "cosmosdb" +
                             "-drWarm4Z60GkknMfHLo5BwuiH7w6AffzSb9jKbvwAQwaRZd10oxnLeCueuyZ5gbm9dwVVAqJLdzrB38Dk73Q6xMErv-1";
        TestObject obj = new TestObject();
        obj.setId(itemID1);
        container.createItem(obj).block();
        obj.setId(itemID2);
        container.createItem(obj).block();

        String query = "select * from c where c.id IN ( \"" + itemID1 + "\" , \"" + itemID2 + "\")";
        List<JsonNode> results = container.queryItems(query, JsonNode.class).collectList().block();
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
        container.delete().block();
    }

    private List<PartitionKeyRange> getPartitionKeyRanges(
        String containerId, AsyncDocumentClient asyncDocumentClient) {
        List<PartitionKeyRange> partitionKeyRanges = new ArrayList<>();
        List<FeedResponse<PartitionKeyRange>> partitionFeedResponseList = asyncDocumentClient
                                                                              .readPartitionKeyRanges("/dbs/" + createdDatabase.getId()
                                                                                                          + "/colls/" + containerId,
                                                                                                      new CosmosQueryRequestOptions())
                                                                              .collectList().block();
        partitionFeedResponseList.forEach(f -> partitionKeyRanges.addAll(f.getResults()));
        return partitionKeyRanges;
    }

    private <T> List<T> queryAndGetResults(SqlQuerySpec querySpec, CosmosQueryRequestOptions options, Class<T> type) {
        CosmosPagedFlux<T> queryPagedFlux = createdContainer.queryItems(querySpec, options, type);
        TestSubscriber<T> testSubscriber = new TestSubscriber<>();
        queryPagedFlux.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
        return testSubscriber.values();
    }

    private <T> List<T> queryWithContinuationTokens(String query, int pageSize, CosmosAsyncContainer container, Class<T> klass) {
        logger.info("querying: " + query);
        String requestContinuation = null;

        List<T> receivedDocuments = new ArrayList<>();
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);

        do {
            CosmosPagedFlux<T> queryPagedFlux = container.queryItems(query, options, klass);
            FeedResponse<T> firstPage = queryPagedFlux.byPage(requestContinuation, pageSize).blockFirst();
            assert firstPage != null;
            requestContinuation = firstPage.getContinuationToken();
            receivedDocuments.addAll(firstPage.getResults());
        } while (requestContinuation != null);

        return receivedDocuments;
    }

    private TestObject getDocumentDefinition(String documentId, String partitionKey) {
        // Doing NUM_DOCUMENTS/2 just to ensure there will be good number of repetitions for int value.
        int randInt = random.nextInt(DEFAULT_NUM_DOCUMENTS / 2);

        return new TestObject(documentId, "name" + randInt, randInt, partitionKey);
    }

    private <T> List<String> sortTestObjectsAndCollectIds(
        List<TestObject> createdDocuments, Function<TestObject, T> extractProp, Comparator<T> comparer) {
        return createdDocuments.stream()
            .sorted((d1, d2) -> comparer.compare(extractProp.apply(d1), extractProp.apply(d2)))
            .map(d -> d.getId()).collect(Collectors.toList());
    }

    private List<TestObject> insertDocuments(int documentCount, List<String> partitionKeys, CosmosAsyncContainer container) {
        List<TestObject> documentsToInsert = new ArrayList<>();

        for (int i = 0; i < documentCount; i++) {
            documentsToInsert.add(
                getDocumentDefinition(
                    UUID.randomUUID().toString(),
                    partitionKeys == null ? UUID.randomUUID().toString() : partitionKeys.get(random.nextInt(partitionKeys.size()))));
        }

        List<TestObject> documentInserted = bulkInsertBlocking(container, documentsToInsert);

        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());

        return documentInserted;
    }

    private <T extends Comparable<T>> void queryWithOrderByAndAssert(
        int pageSize,
        int documentCount,
        String query,
        CosmosAsyncContainer container,
        Function<TestObject, T> extractProp,
        List<TestObject> documentsInserted) {

        List<TestObject> documentsPaged = queryWithContinuationTokens(query, pageSize, container, TestObject.class);

        List<TestObject> allDocuments = queryWithContinuationTokens(query, documentCount, container, TestObject.class);

        Comparator<T> validatorComparator = Comparator.nullsFirst(Comparator.<T>naturalOrder());
        List<String> expectedResourceIds = sortTestObjectsAndCollectIds(documentsInserted,
            extractProp,
            validatorComparator);

        List<String> docIds1 = documentsPaged.stream().map(TestObject::getId).collect(Collectors.toList());
        List<String> docIds2 = allDocuments.stream().map(TestObject::getId).collect(Collectors.toList());

        assertThat(docIds2).containsExactlyInAnyOrderElementsOf(expectedResourceIds);
        assertThat(docIds1).containsExactlyElementsOf(docIds2);
    }

    private static CosmosUserDefinedFunctionProperties getCosmosUserDefinedFunctionProperties() {
        CosmosUserDefinedFunctionProperties udf =
            new CosmosUserDefinedFunctionProperties(UUID.randomUUID().toString(), "function() {var x = 10;}");
        return udf;
    }

    private static CosmosTriggerProperties getCosmosTriggerProperties() {
        CosmosTriggerProperties trigger = new CosmosTriggerProperties(UUID.randomUUID().toString(), "function() {var " +
            "x = 10;}");
        trigger.setTriggerOperation(TriggerOperation.CREATE);
        trigger.setTriggerType(TriggerType.PRE);
        return trigger;
    }

    private static CosmosStoredProcedureProperties getCosmosStoredProcedureProperties() {
        CosmosStoredProcedureProperties storedProcedureDef =
            new CosmosStoredProcedureProperties(UUID.randomUUID().toString(), "function() {var x = 10;}");
        return storedProcedureDef;
    }

    static class TestObject {
        String id;
        String name;
        int prop;
        String mypk;
        String constantProp = "constantProp";

        public TestObject() {
        }

        public TestObject(String id, String name, int prop, String mypk) {
            this.id = id;
            this.name = name;
            this.prop = prop;
            this.mypk = mypk;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getProp() {
            return prop;
        }

        public void setProp(final int prop) {
            this.prop = prop;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public String getConstantProp() {
            return constantProp;
        }
    }
}
