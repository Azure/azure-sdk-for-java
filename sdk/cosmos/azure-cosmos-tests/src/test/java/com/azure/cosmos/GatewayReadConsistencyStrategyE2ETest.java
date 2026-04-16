// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.TestConfigurations;
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
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

/**
 * E2E tests for ReadConsistencyStrategy through compute gateway (Gateway V1) mode.
 * These tests verify that the HTTP header x-ms-cosmos-read-consistency-strategy is
 * correctly sent to the compute gateway.
 *
 * Run with test group "fast" — uses standard account (no thin client required).
 */
public class GatewayReadConsistencyStrategyE2ETest {
    private static final Logger logger = LoggerFactory.getLogger(GatewayReadConsistencyStrategyE2ETest.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final long TIMEOUT = 60_000L;

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_withLatestCommitted() {
        CosmosAsyncClient client = null;
        try {
            client = createGatewayBuilder().buildAsyncClient();
            CosmosAsyncContainer container = getTestContainer(client);

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
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_queryItems_withLatestCommitted() {
        CosmosAsyncClient client = null;
        try {
            client = createGatewayBuilder().buildAsyncClient();
            CosmosAsyncContainer container = getTestContainer(client);

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
        } finally {
            safeClose(client);
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_withEventual() {
        CosmosAsyncClient client = null;
        try {
            client = createGatewayBuilder().buildAsyncClient();
            CosmosAsyncContainer container = getTestContainer(client);

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

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_clientLevel_latestCommitted() {
        CosmosAsyncClient client = null;
        try {
            client = createGatewayBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer container = getTestContainer(client);

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

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_writeItem_rcsIgnored() {
        CosmosAsyncClient client = null;
        try {
            client = createGatewayBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer container = getTestContainer(client);

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

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_globalStrong_invalidAccount_throwsBadRequest() {
        // This test assumes the account is NOT Strong consistency (e.g., Session)
        // Setting GLOBAL_STRONG on a non-Strong account should throw BadRequestException
        CosmosAsyncClient client = null;
        try {
            client = createGatewayBuilder().buildAsyncClient();
            CosmosAsyncContainer container = getTestContainer(client);

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);
            container.createItem(doc, new PartitionKey(id), null).block();

            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setReadConsistencyStrategy(ReadConsistencyStrategy.GLOBAL_STRONG);

            Throwable thrown = catchThrowable(() ->
                container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block()
            );

            assertThat(thrown).isNotNull();
            // The BadRequestException may be wrapped in a CosmosException
            assertThat(thrown.getMessage()).contains("ReadConsistencyStrategy");
            logger.info("Expected error for GLOBAL_STRONG on non-Strong account: {}", thrown.getMessage());
        } finally {
            safeClose(client);
        }
    }

    private CosmosClientBuilder createGatewayBuilder() {
        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode()
            .consistencyLevel(ConsistencyLevel.SESSION);
    }

    private CosmosAsyncContainer getTestContainer(CosmosAsyncClient client) {
        // Uses the shared multi-partition container
        return client.getDatabase("db1").getContainer("c2");
    }

    private ObjectNode createDocument(String id) {
        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        doc.put("id", id);
        doc.put("partitionKey", id);
        return doc;
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
