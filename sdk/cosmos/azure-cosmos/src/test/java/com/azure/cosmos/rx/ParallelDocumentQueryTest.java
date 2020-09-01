// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.implementation.ItemOperations;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.Range;
import com.fasterxml.jackson.databind.JsonNode;
import com.azure.cosmos.implementation.guava27.Strings;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.groups.Tuple;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.azure.cosmos.models.ModelBridgeInternal.partitionKeyRangeIdInternal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.testng.Assert.fail;

public class ParallelDocumentQueryTest extends TestSuiteBase {
    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdCollection;
    private List<InternalObjectNode> createdDocuments;

    private CosmosAsyncClient client;

    public String getCollectionLink() {
        return TestUtils.getCollectionNameLink(createdDatabase.getId(), createdCollection.getId());
    }

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ParallelDocumentQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "queryMetricsArgProvider")
    public Object[][] queryMetricsArgProvider() {
        return new Object[][]{
            {true},
            {false},
            {null}
        };
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "queryMetricsArgProvider")
    public void queryDocuments(Boolean qmEnabled) {
        String query = "SELECT * from c where c.prop = 99";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        if (qmEnabled != null) {
            options.setQueryMetricsEnabled(qmEnabled);
        }

        options.setMaxDegreeOfParallelism(2);
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        List<InternalObjectNode> expectedDocs = createdDocuments.stream().filter(d -> 99 == ModelBridgeInternal.getIntFromJsonSerializable(d,"prop") ).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
            .totalSize(expectedDocs.size())
            .exactlyContainsInAnyOrder(expectedDocs.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
            .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                .requestChargeGreaterThanOrEqualTo(1.0).build())
            .hasValidQueryMetrics(qmEnabled)
            .build();

        validateQuerySuccess(queryObservable.byPage(5), validator, TIMEOUT);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryMetricEquality() throws Exception {
        String query = "SELECT * from c where c.prop = 99";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setQueryMetricsEnabled(true);
        options.setMaxDegreeOfParallelism(0);

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);
        List<FeedResponse<InternalObjectNode>> resultList1 = queryObservable.byPage(5).collectList().block();

        options.setMaxDegreeOfParallelism(4);
        CosmosPagedFlux<InternalObjectNode> threadedQueryObs = createdCollection.queryItems(query, options, InternalObjectNode.class);
        List<FeedResponse<InternalObjectNode>> resultList2 = threadedQueryObs.byPage().collectList().block();

        assertThat(resultList1.size()).isEqualTo(resultList2.size());
        for(int i = 0; i < resultList1.size(); i++){
            compareQueryMetrics(BridgeInternal.queryMetricsFromFeedResponse(resultList1.get(i)),
                BridgeInternal.queryMetricsFromFeedResponse(resultList2.get(i)));
        }
    }

    private void compareQueryMetrics(Map<String, QueryMetrics> qm1, Map<String, QueryMetrics> qm2) {
        assertThat(qm1.keySet().size()).isEqualTo(qm2.keySet().size());
        QueryMetrics queryMetrics1 = BridgeInternal.createQueryMetricsFromCollection(qm1.values());
        QueryMetrics queryMetrics2 = BridgeInternal.createQueryMetricsFromCollection(qm2.values());
        assertThat(queryMetrics1.getRetrievedDocumentSize()).isEqualTo(queryMetrics2.getRetrievedDocumentSize());
        assertThat(queryMetrics1.getRetrievedDocumentCount()).isEqualTo(queryMetrics2.getRetrievedDocumentCount());
        assertThat(queryMetrics1.getIndexHitDocumentCount()).isEqualTo(queryMetrics2.getIndexHitDocumentCount());
        assertThat(queryMetrics1.getOutputDocumentCount()).isEqualTo(queryMetrics2.getOutputDocumentCount());
        assertThat(queryMetrics1.getOutputDocumentSize()).isEqualTo(queryMetrics2.getOutputDocumentSize());
        assertThat(BridgeInternal.getClientSideMetrics(queryMetrics1).getRequestCharge())
            .isEqualTo(BridgeInternal.getClientSideMetrics(queryMetrics1).getRequestCharge());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryDocuments_NoResults() {
        String query = "SELECT * from root r where r.id = '2'";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator.Builder<InternalObjectNode>()
            .containsExactly(new ArrayList<>())
            .numberOfPagesIsGreaterThanOrEqualTo(1)
            .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                .pageSizeIsLessThanOrEqualTo(0)
                .requestChargeGreaterThanOrEqualTo(1.0).build())
            .build();
        validateQuerySuccess(queryObservable.byPage(), validator);
    }

    @Test(groups = { "simple" }, timeOut = 2 * TIMEOUT)
    public void queryDocumentsWithPageSize() {
        String query = "SELECT * from root";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        int pageSize = 3;
        options.setMaxDegreeOfParallelism(-1);

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        List<InternalObjectNode> expectedDocs = createdDocuments;
        assertThat(expectedDocs).isNotEmpty();

        FeedResponseListValidator<InternalObjectNode> validator = new FeedResponseListValidator
            .Builder<InternalObjectNode>()
            .exactlyContainsInAnyOrder(expectedDocs
                .stream()
                .map(d -> d.getResourceId())
                .collect(Collectors.toList()))
            .numberOfPagesIsGreaterThanOrEqualTo((expectedDocs.size() + 1) / 3)
            .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                .requestChargeGreaterThanOrEqualTo(1.0)
                .pageSizeIsLessThanOrEqualTo(pageSize)
                .build())
            .build();
        validateQuerySuccess(queryObservable.byPage(pageSize), validator, 2 * subscriberValidationTimeout);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void invalidQuerySyntax() {
        String query = "I am an invalid query";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        FailureValidator validator = new FailureValidator.Builder()
            .instanceOf(CosmosException.class)
            .statusCode(400)
            .notNullActivityId()
            .build();
        validateQueryFailure(queryObservable.byPage(), validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void crossPartitionQueryNotEnabled() {
        String query = "SELECT * from root";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        List<InternalObjectNode> expectedDocs = createdDocuments;
        FeedResponseListValidator<InternalObjectNode> validator =
            new FeedResponseListValidator.Builder<InternalObjectNode>()
                .totalSize(expectedDocs.size())
                .exactlyContainsInAnyOrder(expectedDocs.stream().map(Resource::getResourceId).collect(Collectors.toList()))
                .allPagesSatisfy(new FeedResponseValidator.Builder<InternalObjectNode>()
                    .requestChargeGreaterThanOrEqualTo(1.0)
                    .build())
                .build();
        validateQuerySuccess(queryObservable.byPage(), validator);
    }

    @Test(groups = { "simple" }, timeOut = 2 * TIMEOUT)
    public void partitionKeyRangeId() {
        int sum = 0;

        for (String partitionKeyRangeId :
            CosmosBridgeInternal.getAsyncDocumentClient(client).readPartitionKeyRanges(getCollectionLink(), null)
                .flatMap(p -> Flux.fromIterable(p.getResults()))
                .map(Resource::getId).collectList().single().block()) {
            String query = "SELECT * from root";
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            partitionKeyRangeIdInternal(options, partitionKeyRangeId);
            int queryResultCount = createdCollection.queryItems(query, options, InternalObjectNode.class)
                .byPage()
                .flatMap(p -> Flux.fromIterable(p.getResults()))
                .collectList().block().size();

            sum += queryResultCount;
        }

        assertThat(sum).isEqualTo(createdDocuments.size());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void compositeContinuationTokenRoundTrip() throws Exception {
        {
            // Positive
            CompositeContinuationToken compositeContinuationToken = new CompositeContinuationToken("asdf",
                new Range<String>("A", "D", false, true));
            String serialized = compositeContinuationToken.toString();
            ValueHolder<CompositeContinuationToken> outCompositeContinuationToken = new ValueHolder<CompositeContinuationToken>();
            boolean succeeed = CompositeContinuationToken.tryParse(serialized, outCompositeContinuationToken);
            assertThat(succeeed).isTrue();
            CompositeContinuationToken deserialized = outCompositeContinuationToken.v;
            String token = deserialized.getToken();
            Range<String> range = deserialized.getRange();
            assertThat(token).isEqualTo("asdf");
            assertThat(range.getMin()).isEqualTo("A");
            assertThat(range.getMax()).isEqualTo("D");
            assertThat(range.isMinInclusive()).isEqualTo(false);
            assertThat(range.isMaxInclusive()).isEqualTo(true);
        }

        {
            // Negative
            ValueHolder<CompositeContinuationToken> outCompositeContinuationToken = new ValueHolder<CompositeContinuationToken>();
            boolean succeeed = CompositeContinuationToken.tryParse("{\"property\" : \"not a valid composite continuation token\"}", outCompositeContinuationToken);
            assertThat(succeeed).isFalse();
        }

        {
            // Negative - GATEWAY composite continuation token
            ValueHolder<CompositeContinuationToken> outCompositeContinuationToken = new ValueHolder<CompositeContinuationToken>();
            boolean succeeed = CompositeContinuationToken.tryParse("{\"token\":\"-RID:tZFQAImzNLQLAAAAAAAAAA==#RT:1#TRC:10\",\"range\":{\"min\":\"\",\"max\":\"FF\"}}", outCompositeContinuationToken);
            assertThat(succeeed).isFalse();
        }
    }

    @Test(groups = { "non-emulator" }, timeOut = TIMEOUT * 10)
    public void queryDocumentsWithCompositeContinuationTokens() throws Exception {
        String query = "SELECT * FROM c";

        // Get Expected
        List<InternalObjectNode> expectedDocs = new ArrayList<>(createdDocuments);
        assertThat(expectedDocs).isNotEmpty();

        this.queryWithContinuationTokensAndPageSizes(query, new int[] {1, 10, 100}, expectedDocs);
    }

    @Test(groups = { "simple" })
    public void queryDocumentsStringValue(){
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setMaxDegreeOfParallelism(2);

        List<String> expectedValues = createdDocuments.stream().map(d -> d.getId()).collect(Collectors.toList());

        String query = "Select value c.id from c";

        CosmosPagedFlux<String> queryObservable = createdCollection.queryItems(query, options, String.class);

        List<String> fetchedResults = new ArrayList<>();
        queryObservable.byPage().map(stringFeedResponse -> fetchedResults.addAll(stringFeedResponse.getResults())).blockLast();

        assertThat(fetchedResults).containsAll(expectedValues);
    }

    @Test(groups = { "simple" })
    @SuppressWarnings("rawtypes")
    public void queryDocumentsArrayValue(){
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setMaxDegreeOfParallelism(2);

        Collection<List<List<Integer>>> expectedValues = new ArrayList<>();
        List<List<Integer>> lists = new ArrayList<>();
        List<Integer> a1 = new ArrayList<>();
        ArrayList<Integer> a2 = new ArrayList<>();
        a1.add(6519456);
        a1.add(1471916863);
        a2.add(2498434);
        a2.add(1455671440);
        lists.add(a1);
        lists.add(a2);

        expectedValues.add(lists);
        expectedValues.add(lists);

        expectedValues.add(lists);
        expectedValues.add(lists);

        String query = "Select top 2 value c.sgmts from c";

        CosmosPagedFlux<List> queryObservable = createdCollection.queryItems(query, options, List.class);

        List<List> fetchedResults = new ArrayList<>();
        queryObservable.byPage().map(feedResponse -> fetchedResults.addAll(feedResponse.getResults())).blockLast();
        assertThat(fetchedResults).containsAll(expectedValues);
    }

    @Test(groups = { "simple" })
    public void queryDocumentsIntegerValue(){
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setMaxDegreeOfParallelism(2);

        List<Integer> expectedValues = createdDocuments.stream().map(d -> ModelBridgeInternal.getIntFromJsonSerializable(d,"prop")).collect(Collectors.toList());

        String query = "Select value c.prop from c";

        CosmosPagedFlux<Integer> queryObservable = createdCollection.queryItems(query, options, Integer.class);

        List<Integer> fetchedResults = new ArrayList<>();
        queryObservable.byPage().map(feedResponse -> fetchedResults.addAll(feedResponse.getResults())).blockLast();

        assertThat(fetchedResults).containsAll(expectedValues);
    }

    @Test(groups = {"simple"})
    public void queryDocumentsBooleanValue() {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setMaxDegreeOfParallelism(2);

        List<Boolean> expectedValues = createdDocuments
                                           .stream()
                                           .map(d -> ModelBridgeInternal.getBooleanFromJsonSerializable(d, "boolProp"))
                                           .collect(Collectors.toList());

        String query = "Select value c.boolProp from c";

        CosmosPagedFlux<Boolean> queryObservable = createdCollection.queryItems(query, options, Boolean.class);

        List<Boolean> fetchedResults = new ArrayList<>();
        queryObservable.byPage().map(feedResponse -> fetchedResults.addAll(feedResponse.getResults())).blockLast();

        assertThat(fetchedResults).containsAll(expectedValues);
    }

    @Test(groups = {"simple"})
    public void queryDocumentsDoubleValue() {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setMaxDegreeOfParallelism(2);

        List<Double> expectedValues = createdDocuments.stream()
                                           .map(d -> ModelBridgeInternal.getDoubleFromJsonSerializable(d, "_value"))
                                           .collect(Collectors.toList());

        String query = "Select value c._value from c";

        CosmosPagedFlux<Double> queryObservable = createdCollection.queryItems(query, options, Double.class);

        List<Double> fetchedResults = new ArrayList<>();
        queryObservable.byPage().map(feedResponse -> fetchedResults.addAll(feedResponse.getResults())).blockLast();

        assertThat(fetchedResults).containsAll(expectedValues);
    }

    @Test(groups = {"simple"})
    public void queryDocumentsDoubleValueToInt() {
        // When try try to fetch double value using integer class, it should fail
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);
        String query = "Select value c._value from c";
        CosmosPagedFlux<Integer> queryObservable = createdCollection.queryItems(query, options, Integer.class);
        Exception resultException = null;
        List<Integer> fetchedResults = new ArrayList<>();
        try {
            queryObservable.byPage().map(feedResponse -> fetchedResults.addAll(feedResponse.getResults())).blockLast();
        } catch (Exception e) {
            resultException = e;
        }
        assertThat(resultException).isNotNull();
        assertThat(resultException).isInstanceOf(IllegalArgumentException.class);
    }

    @Test(groups = { "simple" })
    public void queryDocumentsPojo(){
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setMaxDegreeOfParallelism(2);
        String query = "Select * from c";
        CosmosPagedFlux<TestObject> queryObservable = createdCollection.queryItems(query, options, TestObject.class);
        List<TestObject> fetchedResults = new ArrayList<>();
        queryObservable.byPage().map(feedResponse -> fetchedResults.addAll(feedResponse.getResults())).blockLast();

        List<Tuple> assertTuples = createdDocuments.stream()
            .map(internalObjectNode -> tuple(internalObjectNode.getId(),
                ModelBridgeInternal.getObjectFromJsonSerializable(internalObjectNode, "mypk"),
                ModelBridgeInternal.getObjectFromJsonSerializable(internalObjectNode, "prop"),
                ModelBridgeInternal.getObjectFromJsonSerializable(internalObjectNode, "boolProp")))
            .collect(Collectors.toList());

        assertThat(fetchedResults).extracting(TestObject::getId,
            TestObject::getMypk,
            TestObject::getProp,
            TestObject::getBoolProp)
            .containsAll(assertTuples);
    }

    @Test(groups = { "simple" })
    public void queryDocumentsNestedPropValue(){
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        options.setMaxDegreeOfParallelism(2);

        List<NestedObject> expectedValues = createdDocuments.stream()
            .map(d -> ModelBridgeInternal.toObjectFromJsonSerializable(d,TestObject.class))
            .map(d -> d.getNestedProp()).collect(Collectors.toList());

        String query = "Select value c.nestedProp from c";

        CosmosPagedFlux<NestedObject> queryObservable = createdCollection.queryItems(query, options, NestedObject.class);

        List<NestedObject> fetchedResults = new ArrayList<>();
        queryObservable.byPage().map(feedResponse -> fetchedResults.addAll(feedResponse.getResults())).blockLast();

        assertThat(fetchedResults.size()).isEqualTo(expectedValues.size());
        assertThat(fetchedResults).containsExactlyInAnyOrderElementsOf(expectedValues);
    }

    @BeforeClass(groups = { "simple", "non-emulator" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_ParallelDocumentQueryTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);

        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        createdDocuments = prepareCosmosContainer(createdCollection);
    }

    private List<InternalObjectNode> prepareCosmosContainer(CosmosAsyncContainer cosmosContainer) {
        try {
            truncateCollection(cosmosContainer);
        } catch (Throwable firstChanceException) {
            try {
                truncateCollection(cosmosContainer);
            } catch (Throwable lastChanceException) {
                String message = Strings.lenientFormat("container %s truncation failed due to first chance %s followed by last chance %s",
                    cosmosContainer,
                    firstChanceException,
                    lastChanceException);
                logger.error(message);
                fail(message, lastChanceException);
            }
        }

        List<InternalObjectNode> docDefList = new ArrayList<>();

        for (int i = 0; i < 13; i++) {
            docDefList.add(getDocumentDefinition(i));
        }

        for (int i = 0; i < 21; i++) {
            docDefList.add(getDocumentDefinition(99));
        }

        List<InternalObjectNode> items = bulkInsertBlocking(cosmosContainer, docDefList);
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        return items;
    }

    @AfterClass(groups = { "simple", "non-emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static InternalObjectNode getDocumentDefinition(int cnt) {
        String uuid = UUID.randomUUID().toString();
        boolean boolVal = cnt % 2 == 0;
        InternalObjectNode doc = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"prop\" : %d, "
                + "\"_value\" : %f, "
                + "\"boolProp\" : %b, "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]], "
                + "\"nestedProp\": { "
                + "\"id\": \"nestedObjectId\", "
                + "\"value\": \"nestedObjectValue\", "
                + "}"
                + "}"
            , uuid, cnt, (double)cnt*2.3, boolVal, uuid)); //2.3 is just a random num chosen
        return doc;
    }

    static class TestObject{
        String id;
        int prop;
        Boolean boolProp;
        String mypk;
        List<List<Integer>> sgmts;
        NestedObject nestedProp;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getProp() {
            return prop;
        }

        public void setProp(Integer prop) {
            this.prop = prop;
        }

        public Boolean getBoolProp() {
            return boolProp;
        }

        public void setBoolProp(Boolean boolProp) {
            this.boolProp = boolProp;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public List<List<Integer>> getSgmts() {
            return sgmts;
        }

        public void setSgmts(List<List<Integer>> sgmts) {
            this.sgmts = sgmts;
        }

        public NestedObject getNestedProp() {
            return nestedProp;
        }

        public void setNestedProp(NestedObject nestedProp) {
            this.nestedProp = nestedProp;
        }
    }

    static class NestedObject {
        String id;
        String value;

        public void setId(String id) { this.id = id; }
        public String getId() { return this.id; }
        public void setValue(String value) { this.value = value; }
        public String getValue() { return this.value; }

        @Override
        public String toString() {
            return "NestedObject{" +
                "id='" + id + '\'' +
                ", value='" + value + '\'' +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NestedObject that = (NestedObject) o;
            return Objects.equals(id, that.id) &&
                Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, value);
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, enabled = false)
    public void invalidQuerySytax() throws Exception {

        String query = "I am an invalid query";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

        FailureValidator validator = new FailureValidator.Builder().instanceOf(CosmosException.class)
            .statusCode(400).notNullActivityId().build();
        validateQueryFailure(queryObservable.byPage(), validator);
    }

    public InternalObjectNode createDocument(CosmosAsyncContainer cosmosContainer, int cnt) {

        InternalObjectNode docDefinition = getDocumentDefinition(cnt);

        return BridgeInternal.getProperties(cosmosContainer.createItem(docDefinition).block());
    }

    private void queryWithContinuationTokensAndPageSizes(String query, int[] pageSizes, List<InternalObjectNode> expectedDocs) {
        for (int pageSize : pageSizes) {
            List<InternalObjectNode> receivedDocuments = this.queryWithContinuationTokens(query, pageSize);
            List<String> actualIds = new ArrayList<String>();
            for (InternalObjectNode document : receivedDocuments) {
                actualIds.add(document.getResourceId());
            }

            List<String> expectedIds = new ArrayList<String>();
            for (InternalObjectNode document : expectedDocs) {
                expectedIds.add(document.getResourceId());
            }

            assertThat(actualIds).containsOnlyElementsOf(expectedIds);
        }
    }

    private List<InternalObjectNode> queryWithContinuationTokens(String query, int pageSize) {
        String requestContinuation = null;
        List<String> continuationTokens = new ArrayList<String>();
        List<InternalObjectNode> receivedDocuments = new ArrayList<InternalObjectNode>();
        do {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            options.setMaxDegreeOfParallelism(2);
            CosmosPagedFlux<InternalObjectNode> queryObservable = createdCollection.queryItems(query, options, InternalObjectNode.class);

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

    //TODO: Fix the test for GW mode
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readMany() throws Exception {
        if (this.getConnectionPolicy().getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("Skipping gateway mode. This needs to be fixed");
        }

        List<Pair<String, PartitionKey>> pairList = new ArrayList<>();
        for (int i = 0; i < createdDocuments.size(); i = i + 3) {
            pairList.add(Pair.of(createdDocuments.get(i).getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(createdDocuments.get(i), "mypk"))));
        }
        FeedResponse<JsonNode> documentFeedResponse =
            createdCollection.readMany(pairList, JsonNode.class).block();
        assertThat(documentFeedResponse.getResults().size()).isEqualTo(pairList.size());
        assertThat(documentFeedResponse.getResults().stream().map(jsonNode -> jsonNode.get("id").textValue()).collect(Collectors.toList()))
            .containsAll(pairList.stream().map(p -> p.getLeft()).collect(Collectors.toList()));
    }

    //TODO: Fix the test for GW mode
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readManyIdSameAsPartitionKey() throws Exception {
        if (this.getConnectionPolicy().getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("Skipping gateway mode. This needs to be fixed");
        }

        CosmosAsyncContainer containerWithIdAsPartitionKey = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(client);
        List<InternalObjectNode> newItems = prepareCosmosContainer(containerWithIdAsPartitionKey);
        List<Pair<String, PartitionKey>> pairList = new ArrayList<>();
        for (int i = 0; i < newItems.size(); i = i + 3) {
            pairList.add(Pair.of(newItems.get(i).getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(newItems.get(i), "id"))));
        }
        FeedResponse<JsonNode> documentFeedResponse =
            containerWithIdAsPartitionKey.readMany(pairList, JsonNode.class).block();
        assertThat(documentFeedResponse.getResults().size()).isEqualTo(pairList.size());
        assertThat(documentFeedResponse.getResults().stream().map(jsonNode -> jsonNode.get("id").textValue()).collect(Collectors.toList()))
            .containsAll(pairList.stream().map(p -> p.getLeft()).collect(Collectors.toList()));
    }
}
