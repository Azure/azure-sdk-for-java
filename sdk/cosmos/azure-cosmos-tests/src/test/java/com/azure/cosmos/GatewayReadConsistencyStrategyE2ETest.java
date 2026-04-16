// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

/**
 * E2E tests for ReadConsistencyStrategy through compute gateway (Gateway V1) mode.
 * Verifies that the HTTP header x-ms-cosmos-read-consistency-strategy is
 * correctly sent to the compute gateway and processed.
 *
 * Uses TestConfigurations for account credentials.
 * Run with test group "fast".
 */
public class GatewayReadConsistencyStrategyE2ETest {
    private static final Logger logger = LoggerFactory.getLogger(GatewayReadConsistencyStrategyE2ETest.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final long TIMEOUT = 60_000L;

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private String databaseId;
    private String containerId;

    @BeforeClass(groups = {"fast"})
    public void beforeClass() {
        client = createGatewayBuilder().buildAsyncClient();

        databaseId = "rcs-gateway-test-" + UUID.randomUUID().toString().substring(0, 8);
        containerId = "testcontainer";

        client.createDatabaseIfNotExists(databaseId).block();
        database = client.getDatabase(databaseId);

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/pk");
        database.createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(400)).block();
        container = database.getContainer(containerId);

        logger.info("Created test database {} and container {}", databaseId, containerId);
    }

    @AfterClass(groups = {"fast"}, alwaysRun = true)
    public void afterClass() {
        if (database != null) {
            try {
                database.delete().block();
                logger.info("Deleted test database {}", databaseId);
            } catch (Exception e) {
                logger.warn("Failed to delete test database", e);
            }
        }
        safeClose(client);
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_withLatestCommitted() {
        String id = UUID.randomUUID().toString();
        ObjectNode doc = createDocument(id);
        container.createItem(doc, new PartitionKey(id), null).block();

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_queryItems_withLatestCommitted() {
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
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_withEventual() {
        String id = UUID.randomUUID().toString();
        ObjectNode doc = createDocument(id);
        container.createItem(doc, new PartitionKey(id), null).block();

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.EVENTUAL);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(ReadConsistencyStrategy.EVENTUAL);
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_clientLevel_latestCommitted() {
        CosmosAsyncClient clientWithRcs = null;
        try {
            clientWithRcs = createGatewayBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer containerWithRcs = clientWithRcs.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);
            containerWithRcs.createItem(doc, new PartitionKey(id), null).block();

            CosmosItemResponse<ObjectNode> response =
                containerWithRcs.readItem(id, new PartitionKey(id), ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
                .isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        } finally {
            safeClose(clientWithRcs);
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_writeItem_rcsIgnored() {
        CosmosAsyncClient clientWithRcs = null;
        try {
            clientWithRcs = createGatewayBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer containerWithRcs = clientWithRcs.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);

            CosmosItemResponse<ObjectNode> response =
                containerWithRcs.createItem(doc, new PartitionKey(id), null).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(201);
            assertThat(response.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
                .isEqualTo(ReadConsistencyStrategy.DEFAULT);
        } finally {
            safeClose(clientWithRcs);
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_globalStrong_invalidAccount_throwsBadRequest() {
        String id = UUID.randomUUID().toString();
        ObjectNode doc = createDocument(id);
        container.createItem(doc, new PartitionKey(id), null).block();

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.GLOBAL_STRONG);

        Throwable thrown = catchThrowable(() ->
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block()
        );

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("ReadConsistencyStrategy");
        logger.info("Expected error for GLOBAL_STRONG on non-Strong account: {}", thrown.getMessage());
    }

    private CosmosClientBuilder createGatewayBuilder() {
        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode()
            .consistencyLevel(ConsistencyLevel.SESSION);
    }

    private ObjectNode createDocument(String id) {
        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        doc.put("id", id);
        doc.put("pk", id);
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
