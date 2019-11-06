// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosBridgeInternal;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.FeedResponseListValidator;
import com.azure.data.cosmos.internal.FeedResponseValidator;
import com.azure.data.cosmos.internal.ResourceValidator;
import com.azure.data.cosmos.internal.RetryAnalyzer;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.Utils.ValueHolder;
import com.azure.data.cosmos.internal.query.CompositeContinuationToken;
import com.azure.data.cosmos.internal.query.OrderByContinuationToken;
import com.azure.data.cosmos.internal.query.QueryItem;
import com.azure.data.cosmos.internal.routing.Range;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.reactivex.subscribers.TestSubscriber;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderbyDocumentQueryTest extends TestSuiteBase {
    private final double minQueryRequestChargePerPartition = 2.0;

    private CosmosClient client;
    private CosmosContainer createdCollection;
    private CosmosDatabase createdDatabase;
    private List<CosmosItemProperties> createdDocuments = new ArrayList<>();

    private int numberOfPartitions;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public OrderbyDocumentQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocumentsValidateContent(boolean qmEnabled) throws Exception {
        CosmosItemProperties expectedDocument = createdDocuments.get(0);

        String query = String.format("SELECT * from root r where r.propStr = '%s'"
            + " ORDER BY r.propInt"
            , expectedDocument.getString("propStr"));

        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.populateQueryMetrics(qmEnabled);

        Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query, options);

        List<String> expectedResourceIds = new ArrayList<>();
        expectedResourceIds.add(expectedDocument.resourceId());

        Map<String, ResourceValidator<CosmosItemProperties>> resourceIDToValidator = new HashMap<>();

        resourceIDToValidator.put(expectedDocument.resourceId(),
            new ResourceValidator.Builder<CosmosItemProperties>().areEqual(expectedDocument).build());

        FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .numberOfPages(1)
                .containsExactly(expectedResourceIds)
                .validateAllResources(resourceIDToValidator)
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>().hasRequestChargeHeader().build())
                .hasValidQueryMetrics(qmEnabled)
                .build();

        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments_NoResults() throws Exception {
        String query = "SELECT * from root r where r.id = '2' ORDER BY r.propInt";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query, options);

        FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
            .containsExactly(new ArrayList<>())
            .numberOfPages(1)
            .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
            .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                .hasRequestChargeHeader().build())
            .build();

        validateQuerySuccess(queryObservable, validator);
    }

    @DataProvider(name = "sortOrder")
    public Object[][] sortOrder() {
        return new Object[][] { { "ASC" }, {"DESC"} };
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sortOrder")
    public void queryOrderBy(String sortOrder) throws Exception {
        String query = String.format("SELECT * FROM r ORDER BY r.propInt %s", sortOrder);
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        int pageSize = 3;
        options.maxItemCount(pageSize);
        Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query, options);
        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());

        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propInt", d -> d.getInt("propInt"), validatorComparator);
        if ("DESC".equals(sortOrder)) {
            Collections.reverse(expectedResourceIds);
        }

        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .containsExactly(expectedResourceIds)
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                        .hasRequestChargeHeader().build())
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .build();

        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryOrderByInt() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propInt";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        int pageSize = 3;
        options.maxItemCount(pageSize);
        Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query, options);

        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());
        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propInt", d -> d.getInt("propInt"), validatorComparator);
        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
            .containsExactly(expectedResourceIds)
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                .hasRequestChargeHeader().build())
            .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
            .build();

        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryOrderByString() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propStr";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        int pageSize = 3;
        options.maxItemCount(pageSize);
        Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query, options);

        Comparator<String> validatorComparator = Comparator.nullsFirst(Comparator.<String>naturalOrder());
        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propStr", d -> d.getString("propStr"), validatorComparator);
        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
            .containsExactly(expectedResourceIds)
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                .hasRequestChargeHeader().build())
            .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
            .build();

        validateQuerySuccess(queryObservable, validator);
    }

    @DataProvider(name = "topValue")
    public Object[][] topValueParameter() {
        return new Object[][] { { 0 }, { 1 }, { 5 }, { createdDocuments.size() - 1 }, { createdDocuments.size() },
            { createdDocuments.size() + 1 }, { 2 * createdDocuments.size() } };
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider =  "topValue")
    public void queryOrderWithTop(int topValue) throws Exception {
        String query = String.format("SELECT TOP %d * FROM r ORDER BY r.propInt", topValue);
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        int pageSize = 3;
        options.maxItemCount(pageSize);
        Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query, options);

        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());

        List<String> expectedResourceIds =
                sortDocumentsAndCollectResourceIds("propInt", d -> d.getInt("propInt"), validatorComparator)
                .stream().limit(topValue).collect(Collectors.toList());

        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<CosmosItemProperties> validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
                .containsExactly(expectedResourceIds)
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                        .hasRequestChargeHeader().build())
                .totalRequestChargeIsAtLeast(numberOfPartitions * (topValue > 0 ? minQueryRequestChargePerPartition : 1))
                .build();

        validateQuerySuccess(queryObservable, validator);
    }

    private <T> List<String> sortDocumentsAndCollectResourceIds(String propName, Function<CosmosItemProperties, T> extractProp, Comparator<T> comparer) {
        return createdDocuments.stream()
                               .filter(d -> d.getMap().containsKey(propName)) // removes undefined
                               .sorted((d1, d2) -> comparer.compare(extractProp.apply(d1), extractProp.apply(d2)))
                               .map(Resource::resourceId).collect(Collectors.toList());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryScopedToSinglePartition_StartWithContinuationToken() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propScopedPartitionInt ASC";
        FeedOptions options = new FeedOptions();
        options.partitionKey(new PartitionKey("duplicateParitionKeyValue"));
        options.maxItemCount(3);
        Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query, options);

        TestSubscriber<FeedResponse<CosmosItemProperties>> subscriber = new TestSubscriber<>();
        queryObservable.take(1).subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertComplete();
        subscriber.assertNoErrors();
        assertThat(subscriber.valueCount()).isEqualTo(1);
        FeedResponse<CosmosItemProperties> page = (FeedResponse<CosmosItemProperties>) subscriber.getEvents().get(0).get(0);
        assertThat(page.results()).hasSize(3);

        assertThat(page.continuationToken()).isNotEmpty();


        options.requestContinuation(page.continuationToken());
        queryObservable = createdCollection.queryItems(query, options);

        List<CosmosItemProperties> expectedDocs = createdDocuments.stream()
            .filter(d -> (StringUtils.equals("duplicateParitionKeyValue", d.getString("mypk"))))
            .filter(d -> (d.getInt("propScopedPartitionInt") > 2)).collect(Collectors.toList());
        int expectedPageSize = (expectedDocs.size() + options.maxItemCount() - 1) / options.maxItemCount();

        assertThat(expectedDocs).hasSize(10 - 3);

        FeedResponseListValidator<CosmosItemProperties> validator = null;

        validator = new FeedResponseListValidator.Builder<CosmosItemProperties>()
            .containsExactly(expectedDocs.stream()
                .sorted((e1, e2) -> Integer.compare(e1.getInt("propScopedPartitionInt"), e2.getInt("propScopedPartitionInt")))
                .map(d -> d.resourceId()).collect(Collectors.toList()))
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemProperties>()
                .requestChargeGreaterThanOrEqualTo(1.0).build())
            .build();

        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
	public void orderByContinuationTokenRoundTrip() throws Exception {
        {
        	// Positive
            OrderByContinuationToken orderByContinuationToken = new OrderByContinuationToken(
                    new CompositeContinuationToken(
                            "asdf",
                            new Range<String>("A", "D", false, true)),
                    new QueryItem[] {new QueryItem("{\"item\" : 42}")},
                    "rid",
                    false);
            String serialized = orderByContinuationToken.toString();
            ValueHolder<OrderByContinuationToken> outOrderByContinuationToken = new ValueHolder<OrderByContinuationToken>();

            assertThat(OrderByContinuationToken.tryParse(serialized, outOrderByContinuationToken)).isTrue();
            OrderByContinuationToken deserialized = outOrderByContinuationToken.v;
            CompositeContinuationToken compositeContinuationToken = deserialized.getCompositeContinuationToken();
            String token = compositeContinuationToken.getToken();
            Range<String> range = compositeContinuationToken.getRange();
            assertThat(token).isEqualTo("asdf");
            assertThat(range.getMin()).isEqualTo("A");
            assertThat(range.getMax()).isEqualTo("D");
            assertThat(range.isMinInclusive()).isEqualTo(false);
            assertThat(range.isMaxInclusive()).isEqualTo(true);

            QueryItem[] orderByItems = deserialized.getOrderByItems();
            assertThat(orderByItems).isNotNull();
            assertThat(orderByItems.length).isEqualTo(1);
            assertThat(orderByItems[0].getItem()).isEqualTo(42);

            String rid = deserialized.getRid();
            assertThat(rid).isEqualTo("rid");

            boolean inclusive = deserialized.getInclusive();
            assertThat(inclusive).isEqualTo(false);
        }

        {
        	// Negative
        	ValueHolder<OrderByContinuationToken> outOrderByContinuationToken = new ValueHolder<OrderByContinuationToken>();
        	assertThat(OrderByContinuationToken.tryParse("{\"property\" : \"Not a valid Order By Token\"}", outOrderByContinuationToken)).isFalse();
        }
	}
    @Test(groups = { "simple" }, timeOut = TIMEOUT * 10, dataProvider = "sortOrder",
            retryAnalyzer = RetryAnalyzer.class)
    public void queryDocumentsWithOrderByContinuationTokensInteger(String sortOrder) throws Exception {
        // Get Actual
        String query = String.format("SELECT * FROM c ORDER BY c.propInt %s", sortOrder);

        // Get Expected
        Comparator<Integer> order = sortOrder.equals("ASC")?Comparator.naturalOrder():Comparator.reverseOrder();
        Comparator<Integer> validatorComparator = Comparator.nullsFirst(order);

        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propInt", d -> d.getInt("propInt"), validatorComparator);
        this.queryWithContinuationTokensAndPageSizes(query, new int[] { 1, 5, 10, 100}, expectedResourceIds);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT * 10, dataProvider = "sortOrder")
    public void queryDocumentsWithOrderByContinuationTokensString(String sortOrder) throws Exception {
        // Get Actual
        String query = String.format("SELECT * FROM c ORDER BY c.id %s", sortOrder);

        // Get Expected
        Comparator<String> order = sortOrder.equals("ASC")?Comparator.naturalOrder():Comparator.reverseOrder();
        Comparator<String> validatorComparator = Comparator.nullsFirst(order);

        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("id", d -> d.getString("id"), validatorComparator);
        this.queryWithContinuationTokensAndPageSizes(query, new int[] { 1, 5, 10, 100 }, expectedResourceIds);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT * 10, dataProvider = "sortOrder")
    public void queryDocumentsWithInvalidOrderByContinuationTokensString(String sortOrder) throws Exception {
        // Get Actual
        String query = String.format("SELECT * FROM c ORDER BY c.id %s", sortOrder);

        // Get Expected
        Comparator<String> validatorComparator;
        if(sortOrder.equals("ASC")) {
            validatorComparator = Comparator.nullsFirst(Comparator.<String>naturalOrder());
        }else{
            validatorComparator = Comparator.nullsFirst(Comparator.<String>reverseOrder());
        }
        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("id", d -> d.getString("id"), validatorComparator);
        this.assertInvalidContinuationToken(query, new int[] { 1, 5, 10, 100 }, expectedResourceIds);
    }

    public CosmosItemProperties createDocument(CosmosContainer cosmosContainer, Map<String, Object> keyValueProps)
            throws CosmosClientException {
        CosmosItemProperties docDefinition = getDocumentDefinition(keyValueProps);
        return cosmosContainer.createItem(docDefinition).block().properties();
    }

    public List<CosmosItemProperties> bulkInsert(CosmosContainer cosmosContainer, List<Map<String, Object>> keyValuePropsList) {

        ArrayList<CosmosItemProperties> result = new ArrayList<CosmosItemProperties>();

        for(Map<String, Object> keyValueProps: keyValuePropsList) {
            CosmosItemProperties docDefinition = getDocumentDefinition(keyValueProps);
            result.add(docDefinition);
        }

        return bulkInsertBlocking(cosmosContainer, result);
    }

    @BeforeMethod(groups = { "simple" })
    public void beforeMethod() throws Exception {
        // add a cool off time
        TimeUnit.SECONDS.sleep(10);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder().build();
        createdDatabase = getSharedCosmosDatabase(client);
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        List<Map<String, Object>> keyValuePropsList = new ArrayList<>();
        Map<String, Object> props;
        for(int i = 0; i < 30; i++) {
            props = new HashMap<>();
            props.put("propInt", i);
            props.put("propStr", String.valueOf(i));
            keyValuePropsList.add(props);
        }

        //undefined values
        props = new HashMap<>();
        keyValuePropsList.add(props);

        createdDocuments = bulkInsert(createdCollection, keyValuePropsList);

        for(int i = 0; i < 10; i++) {
            Map<String, Object> p = new HashMap<>();
            p.put("propScopedPartitionInt", i);
            CosmosItemProperties doc = getDocumentDefinition("duplicateParitionKeyValue", UUID.randomUUID().toString(), p);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            options.partitionKey(new PartitionKey(doc.get("mypk")));
            createdDocuments.add(createDocument(createdCollection, doc).read(options).block().properties());

        }

        numberOfPartitions = CosmosBridgeInternal.getAsyncDocumentClient(client)
                .readPartitionKeyRanges("dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id(), null)
                .flatMap(p -> Flux.fromIterable(p.results())).collectList().single().block().size();

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private void assertInvalidContinuationToken(String query, int[] pageSize, List<String> expectedIds) {
        String requestContinuation = null;
        do {
            FeedOptions options = new FeedOptions();
            options.maxItemCount(1);
            options.enableCrossPartitionQuery(true);
            options.maxDegreeOfParallelism(2);
            OrderByContinuationToken orderByContinuationToken = new OrderByContinuationToken(
                    new CompositeContinuationToken(
                            "asdf",
                            new Range<String>("A", "D", false, true)),
                    new QueryItem[] {new QueryItem("{\"item\" : 42}")},
                    "rid",
                    false);
            options.requestContinuation(orderByContinuationToken.toString());
            Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query,
                    options);

            //Observable<FeedResponse<Document>> firstPageObservable = queryObservable.first();
            TestSubscriber<FeedResponse<CosmosItemProperties>> testSubscriber = new TestSubscriber<>();
            queryObservable.subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertError(CosmosClientException.class);
        } while (requestContinuation != null);
    }

    private void queryWithContinuationTokensAndPageSizes(String query, int[] pageSizes, List<String> expectedIds) {
        for (int pageSize : pageSizes) {
            List<CosmosItemProperties> receivedDocuments = this.queryWithContinuationTokens(query, pageSize);
            List<String> actualIds = new ArrayList<String>();
            for (CosmosItemProperties document : receivedDocuments) {
                actualIds.add(document.resourceId());
            }

            assertThat(actualIds).containsExactlyElementsOf(expectedIds);
        }
    }

    private List<CosmosItemProperties> queryWithContinuationTokens(String query, int pageSize) {
        String requestContinuation = null;
        List<String> continuationTokens = new ArrayList<String>();
        List<CosmosItemProperties> receivedDocuments = new ArrayList<CosmosItemProperties>();
        do {
            FeedOptions options = new FeedOptions();
            options.maxItemCount(pageSize);
            options.enableCrossPartitionQuery(true);
            options.maxDegreeOfParallelism(2);
            options.requestContinuation(requestContinuation);
            Flux<FeedResponse<CosmosItemProperties>> queryObservable = createdCollection.queryItems(query,
                    options);

            //Observable<FeedResponse<Document>> firstPageObservable = queryObservable.first();
            TestSubscriber<FeedResponse<CosmosItemProperties>> testSubscriber = new TestSubscriber<>();
            queryObservable.subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertNoErrors();
            testSubscriber.assertComplete();

            FeedResponse<CosmosItemProperties> firstPage = (FeedResponse<CosmosItemProperties>) testSubscriber.getEvents().get(0).get(0);
            requestContinuation = firstPage.continuationToken();
            receivedDocuments.addAll(firstPage.results());
            continuationTokens.add(requestContinuation);
        } while (requestContinuation != null);

        return receivedDocuments;
    }

    private static CosmosItemProperties getDocumentDefinition(String partitionKey, String id, Map<String, Object> keyValuePair) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        for(String key: keyValuePair.keySet()) {
            Object val = keyValuePair.get(key);
            sb.append("  ");
            sb.append("\"").append(key).append("\"").append(" :" );
            if (val == null) {
                sb.append("null");
            } else {
                sb.append(toJson(val));
            }
            sb.append(",\n");
        }

        sb.append(String.format("  \"id\": \"%s\",\n", id));
        sb.append(String.format("  \"mypk\": \"%s\"\n", partitionKey));
        sb.append("}");

        return new CosmosItemProperties(sb.toString());
    }

    private static CosmosItemProperties getDocumentDefinition(Map<String, Object> keyValuePair) {
        String uuid = UUID.randomUUID().toString();
        return getDocumentDefinition(uuid, uuid, keyValuePair);
    }

    private static String toJson(Object object){
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
		}
	}
}
