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
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.azure.cosmos.implementation.ResourceValidator;
import com.azure.cosmos.implementation.RetryAnalyzer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.query.OrderByContinuationToken;
import com.azure.cosmos.implementation.query.QueryItem;
import com.azure.cosmos.implementation.routing.Range;
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

    private CosmosAsyncClient client;
    private CosmosAsyncContainer createdCollection;
    private CosmosAsyncDatabase createdDatabase;
    private List<InternalObjectNode> createdDocuments = new ArrayList<>();

    private int numberOfPartitions;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public OrderbyDocumentQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocumentsValidateContent(Boolean qmEnabled) throws Exception {
        InternalObjectNode expectedDocument = createdDocuments.get(0);

        String query = String.format("SELECT * from root r where r.propStr = '%s'"
            + " ORDER BY r.propInt"
            , ModelBridgeInternal.getStringFromJsonSerializable(expectedDocument,"propStr"));

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        List<String> expectedResourceIds = new ArrayList<>();
        expectedResourceIds.add(expectedDocument.getResourceId());

        Map<String, ResourceValidator<InternalObjectNode>> resourceIDToValidator = new HashMap<>();

        resourceIDToValidator.put(expectedDocument.getResourceId(),
            new ResourceValidator.Builder<InternalObjectNode>().areEqual(expectedDocument).build());

        FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
                .numberOfPages(1)
                .containsExactly(expectedResourceIds)
                .validateAllResources(resourceIDToValidator)
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>().hasRequestChargeHeader().build())
                .hasValidQueryMetrics(qmEnabled)
                .build();

        validateQuerySuccess(queryObservable.byPage(), validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments_NoResults() throws Exception {
        String query = "SELECT * from root r where r.id = '2' ORDER BY r.propInt";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
            .containsExactly(new ArrayList<>())
            .numberOfPages(1)
            .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
            .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                .hasRequestChargeHeader().build())
            .build();

        validateQuerySuccess(queryObservable.byPage(), validator);
    }

    @DataProvider(name = "sortOrder")
    public Object[][] sortOrder() {
        return new Object[][] { { "ASC" }, {"DESC"} };
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sortOrder")
    public void queryOrderBy(String sortOrder) throws Exception {
        String query = String.format("SELECT * FROM r ORDER BY r.propInt %s", sortOrder);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        int pageSize = 3;
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);
        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());

        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propInt", d -> ModelBridgeInternal.getIntFromJsonSerializable(d,"propInt"), validatorComparator);
        if ("DESC".equals(sortOrder)) {
            Collections.reverse(expectedResourceIds);
        }

        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
                .containsExactly(expectedResourceIds)
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                        .hasRequestChargeHeader().build())
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .build();

        validateQuerySuccess(queryObservable.byPage(pageSize), validator);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT, dataProvider = "sortOrder")
    public void queryOrderByWithValue(String sortOrder) throws Exception {
        String query = String.format("SELECT value r.propInt FROM r ORDER BY r.propInt %s", sortOrder);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        int pageSize = 3;
        CosmosPagedFlux<Integer> queryObservable = createdCollection.queryItems(query, options,
                                                                                Integer.class);
        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());

        List<Integer> expectedValues =
            sortDocumentsAndCollectValues("propInt",
                                               d -> ModelBridgeInternal
                                                        .getIntFromJsonSerializable(d, "propInt"),
                                               validatorComparator);
        if ("DESC".equals(sortOrder)) {
            Collections.reverse(expectedValues);
        }

        int expectedPageSize = expectedNumberOfPages(expectedValues.size(), pageSize);

        FeedResponseListValidator<Integer> validator = new FeedResponseListValidator.Builder<Integer>()
                                                           .containsExactlyValues(expectedValues)
                                                           .numberOfPages(expectedPageSize)
                                                           .allPagesSatisfy(new FeedResponseValidator.Builder<Integer>()
                                                                                .hasRequestChargeHeader().build())
                                                           .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                                                           .build();

        validateQuerySuccess(queryObservable.byPage(pageSize), validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryOrderByInt() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propInt";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        int pageSize = 3;
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());
        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propInt", d -> ModelBridgeInternal.getIntFromJsonSerializable(d,"propInt"), validatorComparator);
        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
            .containsExactly(expectedResourceIds)
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                .hasRequestChargeHeader().build())
            .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
            .build();

        validateQuerySuccess(queryObservable.byPage(pageSize), validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryOrderByString() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propStr";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        int pageSize = 3;
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        Comparator<String> validatorComparator = Comparator.nullsFirst(Comparator.<String>naturalOrder());
        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propStr", d -> ModelBridgeInternal.getStringFromJsonSerializable(d,"propStr"), validatorComparator);
        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
            .containsExactly(expectedResourceIds)
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                .hasRequestChargeHeader().build())
            .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
            .build();

        validateQuerySuccess(queryObservable.byPage(pageSize), validator);
    }

    @DataProvider(name = "topValue")
    public Object[][] topValueParameter() {
        return new Object[][] { { 0 }, { 1 }, { 5 }, { createdDocuments.size() - 1 }, { createdDocuments.size() },
            { createdDocuments.size() + 1 }, { 2 * createdDocuments.size() } };
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider =  "topValue")
    public void queryOrderWithTop(int topValue) throws Exception {
        String query = String.format("SELECT TOP %d * FROM r ORDER BY r.propInt", topValue);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        int pageSize = 3;
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());

        List<String> expectedResourceIds =
                sortDocumentsAndCollectResourceIds("propInt", d -> ModelBridgeInternal.getIntFromJsonSerializable(d,"propInt"), validatorComparator)
                .stream().limit(topValue).collect(Collectors.toList());

        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
                .containsExactly(expectedResourceIds)
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                        .hasRequestChargeHeader().build())
                .totalRequestChargeIsAtLeast(numberOfPartitions * (topValue > 0 ? minQueryRequestChargePerPartition : 1))
                .build();

        validateQuerySuccess(queryObservable.byPage(pageSize), validator);
    }

    private <T> List<String> sortDocumentsAndCollectResourceIds(String propName, Function<InternalObjectNode, T> extractProp, Comparator<T> comparer) {
        return createdDocuments.stream()
                               .filter(d -> ModelBridgeInternal.getMapFromJsonSerializable(d).containsKey(propName)) // removes undefined
                               .sorted((d1, d2) -> comparer.compare(extractProp.apply(d1), extractProp.apply(d2)))
                               .map(Resource::getResourceId).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> sortDocumentsAndCollectValues(String propName,
                                                      Function<InternalObjectNode, T> extractProp, Comparator<T> comparer) {
        return createdDocuments.stream()
                   .filter(d -> ModelBridgeInternal.getMapFromJsonSerializable(d).containsKey(propName)) // removes undefined
                   .sorted((d1, d2) -> comparer.compare(extractProp.apply(d1), extractProp.apply(d2)))
                   .map(d -> (T)ModelBridgeInternal.getMapFromJsonSerializable(d).get(propName))
                   .collect(Collectors.toList());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryScopedToSinglePartition_StartWithContinuationToken() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propScopedPartitionInt ASC";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setPartitionKey(new PartitionKey("duplicateParitionKeyValue"));
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        TestSubscriber<FeedResponse<InternalObjectNode>> subscriber = new TestSubscriber<>();
        queryObservable.byPage(3).take(1).subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertComplete();
        subscriber.assertNoErrors();
        assertThat(subscriber.valueCount()).isEqualTo(1);

        @SuppressWarnings("unchecked")
        FeedResponse<InternalObjectNode> page = (FeedResponse<InternalObjectNode>) subscriber.getEvents().get(0).get(0);
        assertThat(page.getResults()).hasSize(3);

        assertThat(page.getContinuationToken()).isNotEmpty();

        queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        List<InternalObjectNode> expectedDocs = createdDocuments.stream()
                                                                .filter(d -> (StringUtils.equals("duplicateParitionKeyValue", ModelBridgeInternal.getStringFromJsonSerializable(d,"mypk"))))
                                                                .filter(d -> (ModelBridgeInternal.getIntFromJsonSerializable(d,"propScopedPartitionInt") > 2)).collect(Collectors.toList());
        Integer maxItemCount = ModelBridgeInternal.getMaxItemCountFromQueryRequestOptions(options);
        int expectedPageSize = (expectedDocs.size() + maxItemCount - 1) / maxItemCount;

        assertThat(expectedDocs).hasSize(10 - 3);

        FeedResponseListValidator<InternalObjectNode> validator = null;

        validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
            .containsExactly(expectedDocs.stream()
                .sorted((e1, e2) -> Integer.compare(ModelBridgeInternal.getIntFromJsonSerializable(e1,"propScopedPartitionInt"),
                    ModelBridgeInternal.getIntFromJsonSerializable(e2,"propScopedPartitionInt")))
                .map(d -> d.getResourceId()).collect(Collectors.toList()))
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                .requestChargeGreaterThanOrEqualTo(1.0).build())
            .build();

        validateQuerySuccess(queryObservable.byPage(page.getContinuationToken()), validator);
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

        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propInt", d -> ModelBridgeInternal.getIntFromJsonSerializable(d,"propInt"), validatorComparator);
        this.queryWithContinuationTokensAndPageSizes(query, new int[] { 1, 5, 10, 100}, expectedResourceIds);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT * 10, dataProvider = "sortOrder")
    public void queryDocumentsWithOrderByContinuationTokensString(String sortOrder) throws Exception {
        // Get Actual
        String query = String.format("SELECT * FROM c ORDER BY c.id %s", sortOrder);

        // Get Expected
        Comparator<String> order = sortOrder.equals("ASC")?Comparator.naturalOrder():Comparator.reverseOrder();
        Comparator<String> validatorComparator = Comparator.nullsFirst(order);

        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("id", d -> ModelBridgeInternal.getStringFromJsonSerializable(d,"id"), validatorComparator);
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
        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("id", d -> ModelBridgeInternal.getStringFromJsonSerializable(d,"id"), validatorComparator);
        this.assertInvalidContinuationToken(query, new int[] { 1, 5, 10, 100 }, expectedResourceIds);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT, dataProvider = "sortOrder")
    public void queryOrderByArray(String sortOrder) throws Exception {
        String query = String.format("SELECT * FROM r ORDER BY r.propArray %s", sortOrder);

        int pageSize = 3;
        List<InternalObjectNode> results1 = this.queryWithContinuationTokens(query, pageSize);
        List<InternalObjectNode> results2 = this.queryWithContinuationTokens(query, this.createdDocuments.size());

        // If order by item type is array (for exampel [1, 2]), we can not get a predictable ordering, only check it always return consistent order
        // and also continuation token is not supported
        assertThat(results1.stream().map(r -> r.getResourceId()).collect(Collectors.toList()))
            .containsExactlyElementsOf(results2.stream().limit(pageSize).map(r -> r.getResourceId()).collect(Collectors.toList()));
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT, dataProvider = "sortOrder")
    public void queryOrderByObject(String sortOrder) throws Exception {
        String query = String.format("SELECT * FROM r ORDER BY r.propObject %s", sortOrder);

        int pageSize = 3;
        List<InternalObjectNode> results1 = this.queryWithContinuationTokens(query, pageSize);
        List<InternalObjectNode> results2 = this.queryWithContinuationTokens(query, this.createdDocuments.size());

        // If order by item type is object (for example {"prop": "value"}, we can not get a predictable ordering, only check it always return with consistent order
        // and also continuation token is not supported
        assertThat(results1.stream().map(r -> r.getResourceId()).collect(Collectors.toList()))
            .containsExactlyElementsOf(results2.stream().limit(pageSize).map(r -> r.getResourceId()).collect(Collectors.toList()));
    }

    public InternalObjectNode createDocument(CosmosAsyncContainer cosmosContainer, Map<String, Object> keyValueProps) {
        InternalObjectNode docDefinition = getDocumentDefinition(keyValueProps);
        return BridgeInternal.getProperties(cosmosContainer.createItem(docDefinition).block());
    }

    public List<InternalObjectNode> bulkInsert(CosmosAsyncContainer cosmosContainer, List<Map<String, Object>> keyValuePropsList) {

        ArrayList<InternalObjectNode> result = new ArrayList<InternalObjectNode>();

        for(Map<String, Object> keyValueProps: keyValuePropsList) {
            InternalObjectNode docDefinition = getDocumentDefinition(keyValueProps);
            result.add(docDefinition);
        }

        return bulkInsertBlocking(cosmosContainer, result);
    }

    @BeforeMethod(groups = { "simple" })
    public void beforeMethod() throws Exception {
        // add a cool off time
        TimeUnit.SECONDS.sleep(10);
    }

    @BeforeClass(groups = { "simple" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_OrderbyDocumentQueryTest() throws Exception {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);

        List<Map<String, Object>> keyValuePropsList = new ArrayList<>();
        Map<String, Object> props;

        for(int i = 0; i < 30; i++) {
            props = new HashMap<>();
            props.put("propInt", i);
            props.put("propStr", String.valueOf(i));

            List<Integer> orderByArray = new ArrayList<Integer>();
            Map<String, String> orderByObject = new HashMap<>();
            for (int k = 0; k < 3; k++) {
                orderByArray.add(k + i);
                orderByObject.put("key1", String.valueOf(i));
                orderByObject.put("key2", String.valueOf(orderByArray.get(k)));
            }
            props.put("propArray", orderByArray);
            props.put("propObject", orderByObject);
            keyValuePropsList.add(props);
        }

        //undefined values
        props = new HashMap<>();
        keyValuePropsList.add(props);

        createdDocuments = bulkInsert(createdCollection, keyValuePropsList);

        for(int i = 0; i < 10; i++) {
            Map<String, Object> p = new HashMap<>();
            p.put("propScopedPartitionInt", i);
            InternalObjectNode doc = getDocumentDefinition("duplicateParitionKeyValue", UUID.randomUUID().toString(), p);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            createdDocuments.add(createDocument(createdCollection, doc));

        }

        numberOfPartitions = CosmosBridgeInternal.getAsyncDocumentClient(client)
                .readPartitionKeyRanges("dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId(), null)
                .flatMap(p -> Flux.fromIterable(p.getResults())).collectList().single().block().size();

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private void assertInvalidContinuationToken(String query, int[] pageSize, List<String> expectedIds) {
        String requestContinuation = null;
        do {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            options.setMaxDegreeOfParallelism(2);
            OrderByContinuationToken orderByContinuationToken = new OrderByContinuationToken(
                    new CompositeContinuationToken(
                            "asdf",
                            new Range<String>("A", "D", false, true)),
                    new QueryItem[] {new QueryItem("{\"item\" : 42}")},
                    "rid",
                    false);
            CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query,
                    options, InternalObjectNode.class);

            //Observable<FeedResponse<Document>> firstPageObservable = queryObservable.first();
            TestSubscriber<FeedResponse<InternalObjectNode>> testSubscriber = new TestSubscriber<>();
            queryObservable.byPage(orderByContinuationToken.toString(),1).subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertError(CosmosException.class);
        } while (requestContinuation != null);
    }

    private void queryWithContinuationTokensAndPageSizes(String query, int[] pageSizes, List<String> expectedIds) {
        for (int pageSize : pageSizes) {
            List<InternalObjectNode> receivedDocuments = this.queryWithContinuationTokens(query, pageSize);
            List<String> actualIds = new ArrayList<String>();
            for (InternalObjectNode document : receivedDocuments) {
                actualIds.add(document.getResourceId());
            }

            assertThat(actualIds).containsExactlyElementsOf(expectedIds);
        }
    }

    private List<InternalObjectNode> queryWithContinuationTokens(String query, int pageSize) {
        String requestContinuation = null;
        List<String> continuationTokens = new ArrayList<String>();
        List<InternalObjectNode> receivedDocuments = new ArrayList<InternalObjectNode>();
        do {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            options.setMaxDegreeOfParallelism(2);
            CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query,
                    options, InternalObjectNode.class);

            //Observable<FeedResponse<Document>> firstPageObservable = queryObservable.byPage().first();
            TestSubscriber<FeedResponse<InternalObjectNode>> testSubscriber = new TestSubscriber<>();
            queryObservable.byPage(requestContinuation, pageSize).subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertNoErrors();
            testSubscriber.assertComplete();

            @SuppressWarnings("unchecked")
            FeedResponse<InternalObjectNode> firstPage = (FeedResponse<InternalObjectNode>) testSubscriber.getEvents().get(0).get(0);
            requestContinuation = firstPage.getContinuationToken();
            receivedDocuments.addAll(firstPage.getResults());
            continuationTokens.add(requestContinuation);
        } while (requestContinuation != null);

        return receivedDocuments;
    }

    private static InternalObjectNode getDocumentDefinition(String partitionKey, String id, Map<String, Object> keyValuePair) {
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

        return new InternalObjectNode(sb.toString());
    }

    private static InternalObjectNode getDocumentDefinition(Map<String, Object> keyValuePair) {
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
