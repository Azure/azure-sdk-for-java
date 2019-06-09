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

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import com.microsoft.azure.cosmosdb.RetryAnalyzer;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosBridgeInternal;
import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosItemRequestOptions;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.internal.query.QueryItem;
import com.microsoft.azure.cosmosdb.internal.routing.Range;
import com.microsoft.azure.cosmosdb.rx.internal.Utils.ValueHolder;
import com.microsoft.azure.cosmosdb.rx.internal.query.CompositeContinuationToken;
import com.microsoft.azure.cosmosdb.rx.internal.query.OrderByContinuationToken;

import io.reactivex.subscribers.TestSubscriber;
import reactor.core.publisher.Flux;
import rx.Observable;

public class OrderbyDocumentQueryTest extends TestSuiteBase {
    private final double minQueryRequestChargePerPartition = 2.0;

    private CosmosClient client;
    private CosmosContainer createdCollection;
    private CosmosDatabase createdDatabase;
    private List<CosmosItemSettings> createdDocuments = new ArrayList<>();

    private int numberOfPartitions;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public OrderbyDocumentQueryTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocumentsValidateContent(boolean qmEnabled) throws Exception {
        CosmosItemSettings expectedDocument = createdDocuments.get(0);

        String query = String.format("SELECT * from root r where r.propStr = '%s'"
            + " ORDER BY r.propInt"
            , expectedDocument.getString("propStr"));

        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setPopulateQueryMetrics(qmEnabled);

        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query, options);

        List<String> expectedResourceIds = new ArrayList<>();
        expectedResourceIds.add(expectedDocument.getResourceId());

        Map<String, ResourceValidator<CosmosItemSettings>> resourceIDToValidator = new HashMap<>();

        resourceIDToValidator.put(expectedDocument.getResourceId(),
            new ResourceValidator.Builder<CosmosItemSettings>().areEqual(expectedDocument).build());

        FeedResponseListValidator<CosmosItemSettings> validator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                .numberOfPages(1)
                .containsExactly(expectedResourceIds)
                .validateAllResources(resourceIDToValidator)
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemSettings>().hasRequestChargeHeader().build())
                .hasValidQueryMetrics(qmEnabled)
                .build();

        try {
            validateQuerySuccess(queryObservable, validator);
        } catch (Throwable error) {
            // TODO: DANOBLE: report this detailed information in all failures produced by TestSuiteBase classes
            // work item: https://msdata.visualstudio.com/CosmosDB/_workitems/edit/370015
            String message = String.format("%s %s mode with %s consistency test failure",
                this.clientBuilder.getConnectionPolicy().getConnectionMode(),
                this.clientBuilder.getConfigs().getProtocol(),
                this.clientBuilder.getDesiredConsistencyLevel());
            throw new AssertionError(message, error);
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments_NoResults() throws Exception {
        String query = "SELECT * from root r where r.id = '2' ORDER BY r.propInt";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query, options);

        FeedResponseListValidator<CosmosItemSettings> validator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
            .containsExactly(new ArrayList<>())
            .numberOfPages(1)
            .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
            .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemSettings>()
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
        options.setEnableCrossPartitionQuery(true);
        int pageSize = 3;
        options.setMaxItemCount(pageSize);
        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query, options);
        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());

        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propInt", d -> d.getInt("propInt"), validatorComparator);
        if ("DESC".equals(sortOrder)) {
            Collections.reverse(expectedResourceIds);
        }

        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<CosmosItemSettings> validator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                .containsExactly(expectedResourceIds)
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemSettings>()
                        .hasRequestChargeHeader().build())
                .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
                .build();

        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryOrderByInt() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propInt";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        int pageSize = 3;
        options.setMaxItemCount(pageSize);
        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query, options);

        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());
        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propInt", d -> d.getInt("propInt"), validatorComparator);
        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<CosmosItemSettings> validator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
            .containsExactly(expectedResourceIds)
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemSettings>()
                .hasRequestChargeHeader().build())
            .totalRequestChargeIsAtLeast(numberOfPartitions * minQueryRequestChargePerPartition)
            .build();

        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryOrderByString() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propStr";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        int pageSize = 3;
        options.setMaxItemCount(pageSize);
        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query, options);

        Comparator<String> validatorComparator = Comparator.nullsFirst(Comparator.<String>naturalOrder());
        List<String> expectedResourceIds = sortDocumentsAndCollectResourceIds("propStr", d -> d.getString("propStr"), validatorComparator);
        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<CosmosItemSettings> validator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
            .containsExactly(expectedResourceIds)
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemSettings>()
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
        options.setEnableCrossPartitionQuery(true);
        int pageSize = 3;
        options.setMaxItemCount(pageSize);
        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query, options);

        Comparator<Integer> validatorComparator = Comparator.nullsFirst(Comparator.<Integer>naturalOrder());

        List<String> expectedResourceIds = 
                sortDocumentsAndCollectResourceIds("propInt", d -> d.getInt("propInt"), validatorComparator)
                .stream().limit(topValue).collect(Collectors.toList());

        int expectedPageSize = expectedNumberOfPages(expectedResourceIds.size(), pageSize);

        FeedResponseListValidator<CosmosItemSettings> validator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
                .containsExactly(expectedResourceIds)
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemSettings>()
                        .hasRequestChargeHeader().build())
                .totalRequestChargeIsAtLeast(numberOfPartitions * (topValue > 0 ? minQueryRequestChargePerPartition : 1))
                .build();

        validateQuerySuccess(queryObservable, validator);
    }

    private <T> List<String> sortDocumentsAndCollectResourceIds(String propName, Function<CosmosItemSettings, T> extractProp, Comparator<T> comparer) {
        return createdDocuments.stream()
                .filter(d -> d.getHashMap().containsKey(propName)) // removes undefined
                .sorted((d1, d2) -> comparer.compare(extractProp.apply(d1), extractProp.apply(d2)))
                .map(d -> d.getResourceId()).collect(Collectors.toList());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void crossPartitionQueryNotEnabled() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propInt";
        FeedOptions options = new FeedOptions();
        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query, options);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(DocumentClientException.class)
                .statusCode(400)
                .build();
        validateQueryFailure(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryScopedToSinglePartition_StartWithContinuationToken() throws Exception {
        String query = "SELECT * FROM r ORDER BY r.propScopedPartitionInt ASC";
        FeedOptions options = new FeedOptions();
        options.setPartitionKey(new PartitionKey("duplicateParitionKeyValue"));
        options.setMaxItemCount(3);
        Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query, options);

        TestSubscriber<FeedResponse<CosmosItemSettings>> subscriber = new TestSubscriber<>();
        queryObservable.take(1).subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertComplete();
        subscriber.assertNoErrors();
        assertThat(subscriber.valueCount()).isEqualTo(1);
        FeedResponse<CosmosItemSettings> page = (FeedResponse<CosmosItemSettings>) subscriber.getEvents().get(0).get(0);
        assertThat(page.getResults()).hasSize(3);

        assertThat(page.getResponseContinuation()).isNotEmpty();


        options.setRequestContinuation(page.getResponseContinuation());
        queryObservable = createdCollection.queryItems(query, options);

        List<CosmosItemSettings> expectedDocs = createdDocuments.stream()
            .filter(d -> (StringUtils.equals("duplicateParitionKeyValue", d.getString("mypk"))))
            .filter(d -> (d.getInt("propScopedPartitionInt") > 2)).collect(Collectors.toList());
        int expectedPageSize = (expectedDocs.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        assertThat(expectedDocs).hasSize(10 - 3);

        FeedResponseListValidator<CosmosItemSettings> validator = null;

        validator = new FeedResponseListValidator.Builder<CosmosItemSettings>()
            .containsExactly(expectedDocs.stream()
                .sorted((e1, e2) -> Integer.compare(e1.getInt("propScopedPartitionInt"), e2.getInt("propScopedPartitionInt")))
                .map(d -> d.getResourceId()).collect(Collectors.toList()))
            .numberOfPages(expectedPageSize)
            .allPagesSatisfy(new FeedResponseValidator.Builder<CosmosItemSettings>()
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

    public CosmosItemSettings createDocument(CosmosContainer cosmosContainer, Map<String, Object> keyValueProps)
            throws DocumentClientException {
        CosmosItemSettings docDefinition = getDocumentDefinition(keyValueProps);
        return cosmosContainer.createItem(docDefinition, new CosmosItemSettings()).block().getCosmosItemSettings();
    }

    public List<CosmosItemSettings> bulkInsert(CosmosContainer cosmosContainer, List<Map<String, Object>> keyValuePropsList) {

        ArrayList<CosmosItemSettings> result = new ArrayList<CosmosItemSettings>();

        for(Map<String, Object> keyValueProps: keyValuePropsList) {
            CosmosItemSettings docDefinition = getDocumentDefinition(keyValueProps);
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
        client = clientBuilder.build();
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
            CosmosItemSettings doc = getDocumentDefinition("duplicateParitionKeyValue", UUID.randomUUID().toString(), p);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            options.setPartitionKey(new PartitionKey(doc.get("mypk")));
            createdDocuments.add(createDocument(createdCollection, doc).read(options).block().getCosmosItemSettings());

        }

        numberOfPartitions = CosmosBridgeInternal.getAsyncDocumentClient(client)
                .readPartitionKeyRanges("dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId(), null)
                .flatMap(p -> Observable.from(p.getResults())).toList().toBlocking().single().size();

        waitIfNeededForReplicasToCatchUp(clientBuilder);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
    
    private void assertInvalidContinuationToken(String query, int[] pageSize, List<String> expectedIds) {
        String requestContinuation = null;
        do {
            FeedOptions options = new FeedOptions();
            options.setMaxItemCount(1);
            options.setEnableCrossPartitionQuery(true);
            options.setMaxDegreeOfParallelism(2);
            OrderByContinuationToken orderByContinuationToken = new OrderByContinuationToken(
                    new CompositeContinuationToken(
                            "asdf",
                            new Range<String>("A", "D", false, true)),
                    new QueryItem[] {new QueryItem("{\"item\" : 42}")},
                    "rid",
                    false);
            options.setRequestContinuation(orderByContinuationToken.toString());
            Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query,
                    options);

            //Observable<FeedResponse<Document>> firstPageObservable = queryObservable.first();
            TestSubscriber<FeedResponse<CosmosItemSettings>> testSubscriber = new TestSubscriber<>();
            queryObservable.subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertError(DocumentClientException.class);
        } while (requestContinuation != null);
    }
    
    private void queryWithContinuationTokensAndPageSizes(String query, int[] pageSizes, List<String> expectedIds) {
        for (int pageSize : pageSizes) {
            List<CosmosItemSettings> receivedDocuments = this.queryWithContinuationTokens(query, pageSize);
            List<String> actualIds = new ArrayList<String>();
            for (CosmosItemSettings document : receivedDocuments) {
                actualIds.add(document.getResourceId());
            }

            assertThat(actualIds).containsExactlyElementsOf(expectedIds);
        }
    }

    private List<CosmosItemSettings> queryWithContinuationTokens(String query, int pageSize) {
        String requestContinuation = null;
        List<String> continuationTokens = new ArrayList<String>();
        List<CosmosItemSettings> receivedDocuments = new ArrayList<CosmosItemSettings>();
        do {
            FeedOptions options = new FeedOptions();
            options.setMaxItemCount(pageSize);
            options.setEnableCrossPartitionQuery(true);
            options.setMaxDegreeOfParallelism(2);
            options.setRequestContinuation(requestContinuation);
            Flux<FeedResponse<CosmosItemSettings>> queryObservable = createdCollection.queryItems(query,
                    options);

            //Observable<FeedResponse<Document>> firstPageObservable = queryObservable.first();
            TestSubscriber<FeedResponse<CosmosItemSettings>> testSubscriber = new TestSubscriber<>();
            queryObservable.subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertNoErrors();
            testSubscriber.assertComplete();

            FeedResponse<CosmosItemSettings> firstPage = (FeedResponse<CosmosItemSettings>) testSubscriber.getEvents().get(0).get(0);
            requestContinuation = firstPage.getResponseContinuation();
            receivedDocuments.addAll(firstPage.getResults());
            continuationTokens.add(requestContinuation);
        } while (requestContinuation != null);

        return receivedDocuments;
    }

    private static CosmosItemSettings getDocumentDefinition(String partitionKey, String id, Map<String, Object> keyValuePair) {
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

        return new CosmosItemSettings(sb.toString());
    }

    private static CosmosItemSettings getDocumentDefinition(Map<String, Object> keyValuePair) {
        String uuid = UUID.randomUUID().toString();
        return getDocumentDefinition(uuid, uuid, keyValuePair);
    }

    private static String toJson(Object object){
        try {
            return com.microsoft.azure.cosmosdb.internal.Utils.getSimpleObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
		}
	}
}
