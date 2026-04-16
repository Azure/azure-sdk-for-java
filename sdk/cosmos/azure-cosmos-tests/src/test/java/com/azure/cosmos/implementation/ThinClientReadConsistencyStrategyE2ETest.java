// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsRequestInfo;
import com.azure.cosmos.FlakyTestRetryAnalyzer;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * E2E tests for ReadConsistencyStrategy propagation through thin client (Gateway V2) mode.
 * These tests verify that the RNTBD header 0x00F0 (ReadConsistencyStrategy) is correctly
 * sent to the proxy and that the proxy applies the strategy server-side.
 *
 * Requires a thin-client-enabled account. Run with test group "thinclient".
 */
public class ThinClientReadConsistencyStrategyE2ETest {
    private static final Logger logger = LoggerFactory.getLogger(ThinClientReadConsistencyStrategyE2ETest.class);
    private static final String thinClientEndpointIndicator = ":10250/";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_withLatestCommitted() {
        CosmosAsyncClient client = null;
        try {
            client = createThinClientBuilder().buildAsyncClient();
            CosmosAsyncContainer container = client.getDatabase("db1").getContainer("c2");

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);
            container.createItem(doc, new PartitionKey(id), null).block();

            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

            CosmosItemResponse<ObjectNode> response =
                container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);

            CosmosDiagnosticsContext ctx = response.getDiagnostics().getDiagnosticsContext();
            assertThat(ctx.getEffectiveReadConsistencyStrategy())
                .isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
            assertThinClientEndpointUsed(response.getDiagnostics());
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_queryItems_withLatestCommitted() {
        CosmosAsyncClient client = null;
        try {
            client = createThinClientBuilder().buildAsyncClient();
            CosmosAsyncContainer container = client.getDatabase("db1").getContainer("c2");

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);
            container.createItem(doc, new PartitionKey(id), null).block();

            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
                .setPartitionKey(new PartitionKey(id))
                .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

            SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c WHERE c.id=@id");
            querySpec.setParameters(Arrays.asList(new SqlParameter("@id", id)));

            FeedResponse<ObjectNode> response = container
                .queryItems(querySpec, queryOptions, ObjectNode.class)
                .byPage()
                .blockFirst();

            assertThat(response).isNotNull();
            assertThat(response.getResults()).isNotNull();
            assertThinClientEndpointUsed(response.getCosmosDiagnostics());
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_withEventual() {
        CosmosAsyncClient client = null;
        try {
            client = createThinClientBuilder().buildAsyncClient();
            CosmosAsyncContainer container = client.getDatabase("db1").getContainer("c2");

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);
            container.createItem(doc, new PartitionKey(id), null).block();

            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setReadConsistencyStrategy(ReadConsistencyStrategy.EVENTUAL);

            CosmosItemResponse<ObjectNode> response =
                container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);

            CosmosDiagnosticsContext ctx = response.getDiagnostics().getDiagnosticsContext();
            assertThat(ctx.getEffectiveReadConsistencyStrategy())
                .isEqualTo(ReadConsistencyStrategy.EVENTUAL);
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_withSession() {
        CosmosAsyncClient client = null;
        try {
            client = createThinClientBuilder().buildAsyncClient();
            CosmosAsyncContainer container = client.getDatabase("db1").getContainer("c2");

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);
            container.createItem(doc, new PartitionKey(id), null).block();

            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);

            CosmosItemResponse<ObjectNode> response =
                container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);

            CosmosDiagnosticsContext ctx = response.getDiagnostics().getDiagnosticsContext();
            assertThat(ctx.getEffectiveReadConsistencyStrategy())
                .isEqualTo(ReadConsistencyStrategy.SESSION);
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_clientLevel_latestCommitted() {
        CosmosAsyncClient client = null;
        try {
            client = createThinClientBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer container = client.getDatabase("db1").getContainer("c2");

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);
            container.createItem(doc, new PartitionKey(id), null).block();

            // No request-level RCS — should inherit client-level LATEST_COMMITTED
            CosmosItemResponse<ObjectNode> response =
                container.readItem(id, new PartitionKey(id), ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);

            CosmosDiagnosticsContext ctx = response.getDiagnostics().getDiagnosticsContext();
            assertThat(ctx.getEffectiveReadConsistencyStrategy())
                .isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_writeItem_rcsIgnored() {
        CosmosAsyncClient client = null;
        try {
            client = createThinClientBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer container = client.getDatabase("db1").getContainer("c2");

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);

            // RCS is set at client level but should be forced to DEFAULT for writes
            CosmosItemResponse<ObjectNode> response =
                container.createItem(doc, new PartitionKey(id), null).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(201);

            CosmosDiagnosticsContext ctx = response.getDiagnostics().getDiagnosticsContext();
            assertThat(ctx.getEffectiveReadConsistencyStrategy())
                .isEqualTo(ReadConsistencyStrategy.DEFAULT);
        } finally {
            safeClose(client);
        }
    }

    private CosmosClientBuilder createThinClientBuilder() {
        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode()
            .consistencyLevel(ConsistencyLevel.SESSION);
    }

    private ObjectNode createDocument(String id) {
        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        doc.put("id", id);
        doc.put("partitionKey", id);
        return doc;
    }

    private static void assertThinClientEndpointUsed(CosmosDiagnostics diagnostics) {
        assertThat(diagnostics).isNotNull();
        CosmosDiagnosticsContext ctx = diagnostics.getDiagnosticsContext();
        assertThat(ctx).isNotNull();

        Collection<CosmosDiagnosticsRequestInfo> requests = ctx.getRequestInfo();
        assertThat(requests).isNotNull();
        assertThat(requests.size()).isPositive();

        for (CosmosDiagnosticsRequestInfo requestInfo : requests) {
            logger.info("Endpoint: {}, RequestType: {}", requestInfo.getEndpoint(), requestInfo.getRequestType());
            if (requestInfo.getEndpoint().contains(thinClientEndpointIndicator)) {
                return;
            }
        }
        org.assertj.core.api.Assertions.fail("Expected at least one request to use thin client endpoint (:10250/)");
    }

    private static void safeClose(CosmosAsyncClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                logger.warn("Failed to close client", e);
            }
        }
    }
}
