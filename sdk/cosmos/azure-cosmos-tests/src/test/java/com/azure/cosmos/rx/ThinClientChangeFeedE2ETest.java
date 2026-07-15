// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Thin client E2E tests for change feed operations.
 * Container is truncated in {@code @BeforeClass} — no per-test cleanup needed.
 */
public class ThinClientChangeFeedE2ETest extends ThinClientTestBase {

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2")
    public ThinClientChangeFeedE2ETest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = TIMEOUT)
    public void testThinClientIncrementalChangeFeed() {
        String pkValue = UUID.randomUUID().toString();
        ObjectNode doc1 = createTestDocument(UUID.randomUUID().toString(), pkValue);
        ObjectNode doc2 = createTestDocument(UUID.randomUUID().toString(), pkValue);

        CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(pkValue));
        batch.createItemOperation(doc1);
        batch.createItemOperation(doc2);
        container.executeCosmosBatch(batch).block();

        // Scope change feed to the specific logical partition to avoid
        // consuming changes from other tests or partitions.
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(pkValue)));

        List<ObjectNode> changeFeedResults = new ArrayList<>();
        List<CosmosDiagnostics> allDiag = new ArrayList<>();
        for (FeedResponse<ObjectNode> page : container.queryChangeFeed(options, ObjectNode.class).byPage().toIterable()) {
            changeFeedResults.addAll(page.getResults());
            allDiag.add(page.getCosmosDiagnostics());
            if (page.getResults().isEmpty()) {
                break;
            }
        }

        assertThat(changeFeedResults.size()).isGreaterThanOrEqualTo(2);
        for (CosmosDiagnostics d : allDiag) {
            assertThinClientEndpointUsed(d);
        }
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = TIMEOUT)
    public void testThinClientChangeFeedFullRange() {
        // Insert docs across two different partition keys so the full-range feed spans multiple partitions.
        String pk1 = "cfFullRange1_" + UUID.randomUUID().toString().substring(0, 8);
        String pk2 = "cfFullRange2_" + UUID.randomUUID().toString().substring(0, 8);
        container.createItem(createTestDocument(UUID.randomUUID().toString(), pk1)).block();
        container.createItem(createTestDocument(UUID.randomUUID().toString(), pk2)).block();

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange());

        List<ObjectNode> changeFeedResults = new ArrayList<>();
        List<CosmosDiagnostics> allDiag = new ArrayList<>();
        for (FeedResponse<ObjectNode> page : container.queryChangeFeed(options, ObjectNode.class).byPage().toIterable()) {
            changeFeedResults.addAll(page.getResults());
            allDiag.add(page.getCosmosDiagnostics());
            if (page.getResults().isEmpty()) {
                break;
            }
        }

        assertThat(changeFeedResults.size()).isGreaterThanOrEqualTo(2);
        for (CosmosDiagnostics d : allDiag) {
            assertThinClientEndpointUsed(d);
        }
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = TIMEOUT)
    public void testThinClientChangeFeedPartitionKey() {
        String pkValue = "cfPk_" + UUID.randomUUID().toString().substring(0, 8);
        container.createItem(createTestDocument(UUID.randomUUID().toString(), pkValue)).block();
        container.createItem(createTestDocument(UUID.randomUUID().toString(), pkValue)).block();

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(pkValue)));

        List<ObjectNode> changeFeedResults = new ArrayList<>();
        List<CosmosDiagnostics> allDiag = new ArrayList<>();
        for (FeedResponse<ObjectNode> page : container.queryChangeFeed(options, ObjectNode.class).byPage().toIterable()) {
            changeFeedResults.addAll(page.getResults());
            allDiag.add(page.getCosmosDiagnostics());
            if (page.getResults().isEmpty()) {
                break;
            }
        }

        // Should only see the 2 docs from this partition key
        assertThat(changeFeedResults.size()).isEqualTo(2);
        for (ObjectNode result : changeFeedResults) {
            assertThat(result.get(PARTITION_KEY_FIELD).asText()).isEqualTo(pkValue);
        }
        for (CosmosDiagnostics d : allDiag) {
            assertThinClientEndpointUsed(d);
        }
    }
}
