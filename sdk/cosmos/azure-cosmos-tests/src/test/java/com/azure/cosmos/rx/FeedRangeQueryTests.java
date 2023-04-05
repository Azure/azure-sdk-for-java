// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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

    @Factory(dataProvider = "clientBuildersWithDirect")
    public FeedRangeQueryTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        random = new Random();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
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

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
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

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
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

    @Test(groups = {"simple"}, timeOut = TIMEOUT, expectedExceptions = IllegalArgumentException.class)
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
        TestSubscriber<T> testSubscriber = new TestSubscriber<>();
        queryPagedFlux.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        return testSubscriber.values();
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = this.getClientBuilder().buildAsyncClient();
        createdContainer = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdContainer);

        createdDocuments.addAll(this.insertDocuments(
            DEFAULT_NUM_DOCUMENTS_PER_PKEY,
            Collections.singletonList(PK_1),
            createdContainer));
        createdDocuments.addAll(this.insertDocuments(
            DEFAULT_NUM_DOCUMENTS_PER_PKEY,
            Collections.singletonList(PK_2),
            createdContainer));
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

        List<JsonNode> documentInserted = bulkInsertBlocking(container, documentsToInsert);

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
