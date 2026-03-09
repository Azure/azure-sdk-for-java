// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

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
 */
public class ThinClientChangeFeedE2ETest extends ThinClientTestBase {

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2")
    public ThinClientChangeFeedE2ETest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testThinClientIncrementalChangeFeed() {
        String pkValue = UUID.randomUUID().toString();
        String idValue1 = UUID.randomUUID().toString();
        String idValue2 = UUID.randomUUID().toString();
        try {
            ObjectNode doc1 = createTestDocument(idValue1, pkValue);
            ObjectNode doc2 = createTestDocument(idValue2, pkValue);

            CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(pkValue));
            batch.createItemOperation(doc1);
            batch.createItemOperation(doc2);
            container.executeCosmosBatch(batch).block();

            // Read change feed scoped to the specific partition key to avoid
            // consuming changes from other partitions/test classes.
            CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(pkValue)));

            // Drain all pages — blockFirst() on full range is fragile when docs span multiple
            // physical partitions.
            List<ObjectNode> changeFeedResults = new ArrayList<>();
            List<CosmosDiagnostics> allDiag = new ArrayList<>();
            Iterable<FeedResponse<ObjectNode>> pages = container
                .queryChangeFeed(options, ObjectNode.class)
                .byPage()
                .toIterable();
            for (FeedResponse<ObjectNode> page : pages) {
                changeFeedResults.addAll(page.getResults());
                allDiag.add(page.getCosmosDiagnostics());
                // Change feed returns empty pages with a continuation when fully drained
                if (page.getResults().isEmpty()) {
                    break;
                }
            }

            assertThat(changeFeedResults.size()).isGreaterThanOrEqualTo(2);
            for (CosmosDiagnostics d : allDiag) {
                assertThinClientEndpointUsed(d);
            }
        } finally {
            try {
                container.deleteItem(idValue1, new PartitionKey(pkValue)).block();
                container.deleteItem(idValue2, new PartitionKey(pkValue)).block();
            } catch (Exception e) {
                logger.warn("Failed to cleanup documents", e);
            }
        }
    }
}
