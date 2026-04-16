// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsRequestInfo;
import com.azure.cosmos.FlakyTestRetryAnalyzer;
import com.azure.cosmos.ReadConsistencyStrategy;
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
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * E2E tests for ReadConsistencyStrategy propagation through thin client (Gateway V2) mode.
 * Verifies that the RNTBD header 0x00F0 (ReadConsistencyStrategy) is correctly
 * sent to the proxy and that the proxy applies the strategy server-side.
 *
 * Requires a thin-client-enabled account configured via TestConfigurations.
 * Run with test group "thinclient".
 */
public class ThinClientReadConsistencyStrategyE2ETest {
    private static final Logger logger = LoggerFactory.getLogger(ThinClientReadConsistencyStrategyE2ETest.class);
    private static final String thinClientEndpointIndicator = ":10250/";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private String databaseId;
    private String containerId;

    @BeforeClass(groups = {"thinclient"})
    public void beforeClass() {
        client = createThinClientBuilder().buildAsyncClient();

        databaseId = "rcs-thinclient-test-" + UUID.randomUUID().toString().substring(0, 8);
        containerId = "testcontainer";

        client.createDatabaseIfNotExists(databaseId).block();
        database = client.getDatabase(databaseId);

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/pk");
        database.createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(400)).block();
        container = database.getContainer(containerId);

        logger.info("Created test database {} and container {}", databaseId, containerId);
    }

    @AfterClass(groups = {"thinclient"}, alwaysRun = true)
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

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_withLatestCommitted() {
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
        assertThinClientEndpointUsed(response.getDiagnostics());
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_queryItems_withLatestCommitted() {
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
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_withEventual() {
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

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_withSession() {
        String id = UUID.randomUUID().toString();
        ObjectNode doc = createDocument(id);
        container.createItem(doc, new PartitionKey(id), null).block();

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(ReadConsistencyStrategy.SESSION);
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_clientLevel_latestCommitted() {
        CosmosAsyncClient clientWithRcs = null;
        try {
            clientWithRcs = createThinClientBuilder()
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

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_writeItem_rcsIgnored() {
        CosmosAsyncClient clientWithRcs = null;
        try {
            clientWithRcs = createThinClientBuilder()
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
        doc.put("pk", id);
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
