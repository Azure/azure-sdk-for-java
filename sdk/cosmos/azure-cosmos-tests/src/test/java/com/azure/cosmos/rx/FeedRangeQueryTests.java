// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class FeedRangeQueryTests extends TestSuiteBase {
    private static final int DEFAULT_NUM_DOCUMENTS_PER_PKEY = 100;
    private static final String PK_1 = "pk1";
    private static final String PK_2 = "pk2";
    private final Random random;
    private final List<JsonNode> createdDocuments = new ArrayList<>();
    private CosmosAsyncContainer createdContainer;
    private CosmosAsyncClient client;
    private FeedRangeTestData scopedData;
    private CosmosAsyncContainer hierarchicalContainer;
    private boolean expectThinClient;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public FeedRangeQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        random = new Random();
    }

    @Test(groups = {"query"}, timeOut = TIMEOUT)
    public void queryWithFeedRange() {
        String query = "select * from root";

        // Do a full scan
        List<JsonNode> queryResults = queryAndGetResults(new SqlQuerySpec(query), new CosmosQueryRequestOptions(),
                                                         JsonNode.class);
        List<String> actualIds = queryResults.stream().map(r -> r.get("id").asText()).collect(Collectors.toList());

        // Get feedranges
        List<FeedRange> feedRanges = this.createdContainer.getFeedRanges().block();

        List<JsonNode> resultsFromFeedRanges = new ArrayList<>();
        for (FeedRange feedRange : feedRanges) {
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions().setFeedRange(feedRange);
            List<JsonNode> feedResults = queryAndGetResults(new SqlQuerySpec(query), queryRequestOptions,
                                                            JsonNode.class);
            resultsFromFeedRanges.addAll(feedResults);
        }
        List<String> feedResultIds =
            resultsFromFeedRanges.stream().map(r -> r.get("id").asText()).collect(Collectors.toList());

        assertThat(queryResults.size()).isEqualTo(resultsFromFeedRanges.size());
        assertThat(feedResultIds).containsExactlyElementsOf(actualIds);
    }

    @Test(groups = {"query"}, timeOut = TIMEOUT)
    public void queryWithFeedRangePartitionKey() {
        String query = "select * from root";
        FeedRange feedRange = FeedRange.forLogicalPartition(new PartitionKey(PK_2));
        CosmosQueryRequestOptions queryRequestOptions =
            new CosmosQueryRequestOptions().setFeedRange(feedRange);
        List<JsonNode> feedResults = queryAndGetResults(new SqlQuerySpec(query), queryRequestOptions,
                                                        JsonNode.class);
        List<JsonNode> pk2results =
            createdDocuments.stream().filter(jsonNode -> PK_2.equals(jsonNode.get("mypk").asText()))
                .collect(Collectors.toList());
        List<String> expectedIds = pk2results.stream().map(jsonNode -> jsonNode.get("id").asText())
                                       .collect(Collectors.toList());
        List<String> actualIds = feedResults.stream().map(jsonNode -> jsonNode.get("id").asText())
                                     .collect(Collectors.toList());

        assertThat(actualIds).containsExactlyInAnyOrderElementsOf(expectedIds);
    }

    @Test(groups = {"query"}, timeOut = TIMEOUT)
    public void queryWithFeedRangeFiltering() {

        String query = "select * from root";
        FeedRangePartitionKeyImpl feedRangeForLogicalPartition = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(PK_1)));

        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(this.client);
        Range<String> effectiveRange =
            feedRangeForLogicalPartition.getNormalizedEffectiveRange(
                asyncDocumentClient.getPartitionKeyRangeCache(),
                null,
                Mono.just(new Utils.ValueHolder<>(ModelBridgeInternal
                                                      .getV2Collection(new CosmosContainerProperties(createdContainer
                                                                                                         .getId(),
                                                                                                     "/mypk")))))
                .block();
        assertThat(effectiveRange).isNotNull();
        FeedRange feedRange = new FeedRangeEpkImpl(effectiveRange);
        CosmosQueryRequestOptions queryRequestOptions =
            new CosmosQueryRequestOptions().setFeedRange(feedRange);
        List<JsonNode> feedResults = queryAndGetResults(new SqlQuerySpec(query), queryRequestOptions,
                                                        JsonNode.class);

        // validations
        List<JsonNode> pk1results = createdDocuments.stream()
                                        .filter(jsonNode -> PK_1.equals(jsonNode.get("mypk").asText()))
                                        .collect(Collectors.toList());
        List<String> expectedIds = pk1results.stream().map(jsonNode -> jsonNode.get("id").asText())
                                       .collect(Collectors.toList());
        List<String> actualIds = feedResults.stream().map(jsonNode -> jsonNode.get("id").asText())
                                     .collect(Collectors.toList());

        assertThat(actualIds).containsExactlyInAnyOrderElementsOf(expectedIds);

    }

    @Test(groups = {"query"}, timeOut = TIMEOUT, expectedExceptions = IllegalArgumentException.class)
    public void queryWithPartitionKeyAndFeedRange() {
        String query = "select * from root";
        FeedRange feedRange = FeedRange.forLogicalPartition(new PartitionKey(PK_2));
        CosmosQueryRequestOptions queryRequestOptions =
            new CosmosQueryRequestOptions().setFeedRange(feedRange);
        queryRequestOptions.setPartitionKey(new PartitionKey(PK_1));
        // This should throw an IllegalArgumentException now
        createdContainer.queryItems(query, queryRequestOptions, JsonNode.class);
    }

    private <T> List<T> queryAndGetResults(SqlQuerySpec querySpec, CosmosQueryRequestOptions options, Class<T> type) {
        CosmosPagedFlux<T> queryPagedFlux = createdContainer.queryItems(querySpec, options, type);
        AtomicReference<List<T>> value = new AtomicReference<>();
        StepVerifier.create(queryPagedFlux.collectList())
            .consumeNextWith(value::set)
            .expectComplete()
            .verify(Duration.ofMillis(TIMEOUT));
        return value.get();
    }

    @DataProvider
    public Object[][] scopedQueries() {
        List<Object[]> cases = new ArrayList<>();
        for (Object[] scope : FeedRangeTestData.scopes()) {
            cases.add(new Object[] {scope[0], false});
            cases.add(new Object[] {scope[0], true});
        }
        return cases.toArray(new Object[0][]);
    }

    @Test(groups = {"query", "thinclient", "thinclientEndpointProbe"}, dataProvider = "scopedQueries", timeOut = TIMEOUT)
    public void queryPreservesScopeAcrossContinuation(String scope, boolean ordered) {
        Range<String> range = scopedData.rangeFor(scope);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions().setFeedRange(new FeedRangeEpkImpl(range));
        String query = ordered ? "SELECT * FROM c ORDER BY c.id" : "SELECT * FROM c";
        assertThat(drainScopedQuery(scopedData.getContainer(), options, query))
            .containsExactlyInAnyOrderElementsOf(scopedData.expectedIds(range));
    }

    @Test(groups = {"query", "thinclient", "thinclientEndpointProbe"}, timeOut = TIMEOUT)
    public void hierarchicalPrefixesDoNotLeakAcrossContinuation() {
        for (int tenant = 0; tenant < 9; tenant++) {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions()
                .setPartitionKey(new PartitionKeyBuilder().add("tenant-" + tenant).build());
            List<String> expected = new ArrayList<>();
            if (tenant < 8) {
                for (int user = 0; user < 4; user++) {
                    expected.add("tenant-" + tenant + "-user-" + user);
                }
            }
            assertThat(drainScopedQuery(hierarchicalContainer, options, "SELECT * FROM c"))
                .containsExactlyInAnyOrderElementsOf(expected);
        }
    }

    private List<String> drainScopedQuery(
        CosmosAsyncContainer container, CosmosQueryRequestOptions options, String query) {
        List<String> ids = new ArrayList<>();
        String continuation = null;
        for (int pageNumber = 0; pageNumber < 100; pageNumber++) {
            FeedResponse<ObjectNode> page = container.queryItems(query, options, ObjectNode.class)
                .byPage(continuation, 1).blockFirst(FeedRangeTestData.REQUEST_TIMEOUT);
            assertThat(page).isNotNull();
            if (expectThinClient) {
                assertThinClientEndpointUsed(page.getCosmosDiagnostics());
            }
            page.getResults().forEach(document -> ids.add(document.get("id").asText()));
            continuation = page.getContinuationToken();
            if (continuation == null) {
                assertThat(new HashSet<>(ids)).as("No duplicate IDs after query resumption").hasSize(ids.size());
                if (ids.size() > 1) {
                    assertThat(pageNumber).as("Must exercise resumed query pages").isPositive();
                }
                return ids;
            }
        }
        throw new AssertionError("Query did not drain in 100 resumed pages");
    }

    @BeforeClass(groups = {"query", "thinclient", "thinclientEndpointProbe"}, timeOut = SETUP_TIMEOUT * 2)
    public void beforeClass(ITestContext context) throws Exception {
        client = this.getClientBuilder().buildAsyncClient();
        expectThinClient = getConnectionPolicy().getConnectionMode() == ConnectionMode.GATEWAY
            && (Boolean.TRUE.equals(Configs.isThinClientEnabled())
                || Arrays.asList(context.getIncludedGroups()).contains("thinclientEndpointProbe"));
        createdContainer = getSharedMultiPartitionCosmosContainer(client);
        cleanUpContainer(createdContainer);

        createdDocuments.addAll(this.insertDocuments(
            DEFAULT_NUM_DOCUMENTS_PER_PKEY,
            Collections.singletonList(PK_1),
            createdContainer));
        createdDocuments.addAll(this.insertDocuments(
            DEFAULT_NUM_DOCUMENTS_PER_PKEY,
            Collections.singletonList(PK_2),
            createdContainer));

        scopedData = new FeedRangeTestData(client, "query-range");
        hierarchicalContainer = scopedData.createHierarchicalContainer();
        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());
    }

    @AfterClass(groups = {"query", "thinclient", "thinclientEndpointProbe"}, alwaysRun = true, timeOut = SHUTDOWN_TIMEOUT)
    public void afterClass() {
        try {
            if (scopedData != null) {
                scopedData.close();
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private List<JsonNode> insertDocuments(
        int documentCount, List<String> partitionKeys,
        CosmosAsyncContainer container) {
        List<JsonNode> documentsToInsert = new ArrayList<>();

        for (int i = 0; i < documentCount; i++) {
            documentsToInsert.add(
                getDocumentDefinition(
                    UUID.randomUUID().toString(),
                    partitionKeys == null ? UUID.randomUUID().toString() : partitionKeys.get(random
                                                                                                 .nextInt(partitionKeys
                                                                                                              .size()))));
        }

        List<JsonNode> documentInserted = insertAllItemsBlocking(container, documentsToInsert, true);

        waitIfNeededForReplicasToCatchUp(this.getClientBuilder());

        return documentInserted;
    }

    private JsonNode getDocumentDefinition(String documentId, String partitionKey) {
        // Doing NUM_DOCUMENTS/2 just to ensure there will be good number of repetetions for int value.
        int randInt = random.nextInt(DEFAULT_NUM_DOCUMENTS_PER_PKEY / 2);
        JsonNode jsonNode = null;
        try {
            jsonNode = Utils.getSimpleObjectMapper().readTree(String.format(Locale.ROOT, "{ "
                                                                                             + "\"id\": \"%s\", "
                                                                                             + "\"name\" : \"%s\", "
                                                                                             + "\"prop\" : %d, "
                                                                                             + "\"mypk\" : \"%s\"} ",
                                                                            documentId, "name_" + randInt, randInt,
                                                                            partitionKey));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
        return jsonNode;
    }

    private Range<String> convertToMaxExclusive(Range<String> maxInclusiveRange) {
        assertThat(maxInclusiveRange)
            .isNotNull()
            .matches(r -> r.isMaxInclusive(), "Ensure isMaxInclusive is set");

        String max = maxInclusiveRange.getMax();
        int i = max.length() - 1;

        while (i >= 0) {
            if (max.charAt(i) == 'F') {
                i--;
                continue;
            }

            char newChar = (char) (((int) max.charAt(i)) + 1);

            if (i < max.length() - 1) {
                max = max.substring(0, i) + newChar + max.substring(i + 1);
            } else {
                max = max.substring(0, i) + newChar;
            }

            break;
        }

        return new Range<>(maxInclusiveRange.getMin(), max, true, false);
    }
}
