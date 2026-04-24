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
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.CosmosRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * E2E tests for ReadConsistencyStrategy propagation through thin client (Gateway V2) mode.
 * Verifies that the RNTBD header 0x00F0 (ReadConsistencyStrategy) is correctly
 * sent to the proxy and that the proxy applies the strategy server-side.
 *
 * Covers all request option types that expose ReadConsistencyStrategy:
 * - CosmosItemRequestOptions (point reads)
 * - CosmosQueryRequestOptions (queries)
 * - CosmosChangeFeedRequestOptions (change feed)
 * - CosmosReadManyRequestOptions (read many)
 * - CosmosClientBuilder (client-level default)
 *
 * Requires a thin-client-enabled account configured via TestConfigurations.
 * Run with test group "thinclient".
 */
public class ThinClientReadConsistencyStrategyE2ETest {
    private static final Logger logger = LoggerFactory.getLogger(ThinClientReadConsistencyStrategyE2ETest.class);
    private static final String THIN_CLIENT_ENDPOINT_INDICATOR = ":10250/";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private String databaseId;
    private String containerId;

    @BeforeClass(groups = {"thinclient"})
    public void beforeClass() {
        client = createThinClientBuilder().buildAsyncClient();

        databaseId = "readConsistencyStrategy-tc-" + UUID.randomUUID().toString().substring(0, 8);
        containerId = "testcontainer";

        client.createDatabaseIfNotExists(databaseId).block();
        database = client.getDatabase(databaseId);

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/pk");
        database.createContainerIfNotExists(containerProperties).block();
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

    // region ItemRequestOptions

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_withLatestCommitted() {
        String id = UUID.randomUUID().toString();
        createAndInsertDocument(id);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThinClientEndpointUsed(response.getDiagnostics());
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_withEventual() {
        String id = UUID.randomUUID().toString();
        createAndInsertDocument(id);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.EVENTUAL);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.EVENTUAL);
        assertThinClientEndpointUsed(response.getDiagnostics());
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readItem_withSession() {
        String id = UUID.randomUUID().toString();
        createAndInsertDocument(id);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.SESSION);
        assertThinClientEndpointUsed(response.getDiagnostics());
    }

    // endregion

    // region QueryRequestOptions

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_queryItems_withLatestCommitted() {
        String id = UUID.randomUUID().toString();
        createAndInsertDocument(id);

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
        assertEffectiveReadConsistencyStrategy(response.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThinClientEndpointUsed(response.getCosmosDiagnostics());
    }

    // endregion

    // region ChangeFeedRequestOptions

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_changeFeed_withLatestCommitted() {
        String pkValue = UUID.randomUUID().toString();
        createAndInsertDocument(pkValue);

        CosmosChangeFeedRequestOptions cfOptions = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(
                FeedRange.forLogicalPartition(new PartitionKey(pkValue)))
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        List<FeedResponse<ObjectNode>> pages = container
            .queryChangeFeed(cfOptions, ObjectNode.class)
            .byPage()
            .collectList()
            .block();

        assertThat(pages).isNotNull();
        assertThat(pages.isEmpty()).isFalse();

        FeedResponse<ObjectNode> firstPage = pages.get(0);
        assertEffectiveReadConsistencyStrategy(firstPage.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThinClientEndpointUsed(firstPage.getCosmosDiagnostics());
    }

    // endregion

    // region ReadManyRequestOptions

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_readMany_withLatestCommitted() {
        String pkValue = UUID.randomUUID().toString();
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        createAndInsertDocument(id1, pkValue);
        createAndInsertDocument(id2, pkValue);

        List<CosmosItemIdentity> identities = Arrays.asList(
            new CosmosItemIdentity(new PartitionKey(pkValue), id1),
            new CosmosItemIdentity(new PartitionKey(pkValue), id2));

        CosmosReadManyRequestOptions readManyOptions = new CosmosReadManyRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        FeedResponse<ObjectNode> response =
            container.readMany(identities, readManyOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getResults()).isNotNull();
        assertThat(response.getResults().size()).isEqualTo(2);
        assertEffectiveReadConsistencyStrategy(response.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThinClientEndpointUsed(response.getCosmosDiagnostics());
    }

    // endregion

    // region Client-level readConsistencyStrategy

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_clientLevel_latestCommitted_readItem() {
        CosmosAsyncClient clientWithReadConsistencyStrategy = null;
        try {
            clientWithReadConsistencyStrategy = createThinClientBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer containerWithReadConsistencyStrategy = clientWithReadConsistencyStrategy.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(containerWithReadConsistencyStrategy, id);

            CosmosItemResponse<ObjectNode> response =
                containerWithReadConsistencyStrategy.readItem(id, new PartitionKey(id), ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
            assertThinClientEndpointUsed(response.getDiagnostics());
        } finally {
            safeClose(clientWithReadConsistencyStrategy);
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_clientLevel_latestCommitted_query() {
        CosmosAsyncClient clientWithReadConsistencyStrategy = null;
        try {
            clientWithReadConsistencyStrategy = createThinClientBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer containerWithReadConsistencyStrategy = clientWithReadConsistencyStrategy.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(containerWithReadConsistencyStrategy, id);

            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
                .setPartitionKey(new PartitionKey(id));

            SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c WHERE c.id=@id");
            querySpec.setParameters(Arrays.asList(new SqlParameter("@id", id)));

            FeedResponse<ObjectNode> response = containerWithReadConsistencyStrategy
                .queryItems(querySpec, queryOptions, ObjectNode.class)
                .byPage()
                .blockFirst();

            assertThat(response).isNotNull();
            assertThat(response.getResults()).isNotNull();
            assertEffectiveReadConsistencyStrategy(response.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
            assertThinClientEndpointUsed(response.getCosmosDiagnostics());
        } finally {
            safeClose(clientWithReadConsistencyStrategy);
        }
    }

    // endregion

    // region Write operations — readConsistencyStrategy forced to DEFAULT

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_writeItem_readConsistencyStrategyIgnored() {
        CosmosAsyncClient clientWithReadConsistencyStrategy = null;
        try {
            clientWithReadConsistencyStrategy = createThinClientBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer containerWithReadConsistencyStrategy = clientWithReadConsistencyStrategy.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);

            CosmosItemResponse<ObjectNode> response =
                containerWithReadConsistencyStrategy.createItem(doc, new PartitionKey(id), null).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(201);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.DEFAULT);
        } finally {
            safeClose(clientWithReadConsistencyStrategy);
        }
    }

    // endregion

    // region Both ConsistencyLevel and readConsistencyStrategy — readConsistencyStrategy wins

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_bothConsistencyLevelAndReadConsistencyStrategy_readConsistencyStrategyWins() {
        CosmosAsyncClient clientWithBoth = null;
        try {
            clientWithBoth = createThinClientBuilder()
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer containerWithBoth = clientWithBoth.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(containerWithBoth, id);

            CosmosItemResponse<ObjectNode> response =
                containerWithBoth.readItem(id, new PartitionKey(id), ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
            assertThinClientEndpointUsed(response.getDiagnostics());
        } finally {
            safeClose(clientWithBoth);
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_requestLevel_bothClAndReadConsistencyStrategy_readConsistencyStrategyWins() {
        // Request-level contention: options set both ConsistencyLevel and readConsistencyStrategy.
        // readConsistencyStrategy should win — proxy must not reject with dual-header error.
        String id = UUID.randomUUID().toString();
        createAndInsertDocument(id);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThinClientEndpointUsed(response.getDiagnostics());
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_requestLevelReadConsistencyStrategy_overridesClientLevelReadConsistencyStrategy() {
        // Request-level readConsistencyStrategy should override client-level readConsistencyStrategy.
        CosmosAsyncClient clientWithClientReadConsistencyStrategy = null;
        try {
            clientWithClientReadConsistencyStrategy = createThinClientBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.EVENTUAL)
                .buildAsyncClient();
            CosmosAsyncContainer containerWithReadConsistencyStrategy = clientWithClientReadConsistencyStrategy.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(containerWithReadConsistencyStrategy, id);

            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

            CosmosItemResponse<ObjectNode> response =
                containerWithReadConsistencyStrategy.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
            assertThinClientEndpointUsed(response.getDiagnostics());
        } finally {
            safeClose(clientWithClientReadConsistencyStrategy);
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_globalStrong_onSessionAccount_throwsBadRequest() {
        String id = UUID.randomUUID().toString();
        createAndInsertDocument(id);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.GLOBAL_STRONG);

        Throwable caughtError = null;
        try {
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();
        } catch (Throwable t) {
            caughtError = t;
        }

        assertThat(caughtError)
            .as("Expected BadRequestException for GLOBAL_STRONG on Session account")
            .isNotNull()
            .isInstanceOf(BadRequestException.class);
        assertThat(caughtError.getMessage()).contains("read-consistency-strategy");
        logger.info("Expected BadRequestException for GLOBAL_STRONG: {}", caughtError.getMessage());
    }

    // endregion

    // region Operation policy (dynamic request options) — readConsistencyStrategy set via CosmosOperationPolicy

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_operationPolicy_setsReadConsistencyStrategy() {
        CosmosAsyncClient policyClient = null;
        try {
            policyClient = createThinClientBuilder()
                .addOperationPolicy(cosmosOperationDetails -> {
                    CosmosRequestOptions overrides = new CosmosRequestOptions()
                        .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);
                    cosmosOperationDetails.setRequestOptions(overrides);
                })
                .buildAsyncClient();
            CosmosAsyncContainer policyContainer = policyClient.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(policyContainer, id);

            CosmosItemResponse<ObjectNode> response =
                policyContainer.readItem(id, new PartitionKey(id), ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
            assertThinClientEndpointUsed(response.getDiagnostics());
        } finally {
            safeClose(policyClient);
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void thinClient_operationPolicy_readConsistencyStrategyOverridesRequestLevel() {
        CosmosAsyncClient policyClient = null;
        try {
            policyClient = createThinClientBuilder()
                .addOperationPolicy(cosmosOperationDetails -> {
                    CosmosRequestOptions overrides = new CosmosRequestOptions()
                        .setReadConsistencyStrategy(ReadConsistencyStrategy.EVENTUAL);
                    cosmosOperationDetails.setRequestOptions(overrides);
                })
                .buildAsyncClient();
            CosmosAsyncContainer policyContainer = policyClient.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(policyContainer, id);

            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

            CosmosItemResponse<ObjectNode> response =
                policyContainer.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.EVENTUAL);
            assertThinClientEndpointUsed(response.getDiagnostics());
        } finally {
            safeClose(policyClient);
        }
    }

    // endregion

    // region Helpers

    private CosmosClientBuilder createThinClientBuilder() {
        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode()
            .consistencyLevel(ConsistencyLevel.SESSION);
    }

    private ObjectNode createDocument(String id) {
        return createDocument(id, id);
    }

    private ObjectNode createDocument(String id, String pk) {
        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        doc.put("id", id);
        doc.put("pk", pk);
        return doc;
    }

    private void createAndInsertDocument(String id) {
        createAndInsertDocument(id, id);
    }

    private void createAndInsertDocument(String id, String pk) {
        ObjectNode doc = createDocument(id, pk);
        container.createItem(doc, new PartitionKey(pk), null).block();
    }

    private void createAndInsertDocument(CosmosAsyncContainer targetContainer, String id) {
        ObjectNode doc = createDocument(id);
        targetContainer.createItem(doc, new PartitionKey(id), null).block();
    }

    private static void assertEffectiveReadConsistencyStrategy(CosmosDiagnostics diagnostics, ReadConsistencyStrategy expected) {
        assertThat(diagnostics).isNotNull();
        assertThat(diagnostics.getDiagnosticsContext()).isNotNull();
        assertThat(diagnostics.getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(expected);
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
            if (requestInfo.getEndpoint().contains(THIN_CLIENT_ENDPOINT_INDICATOR)) {
                return;
            }
        }
        org.assertj.core.api.Assertions.fail("Expected at least one request to use thin client endpoint (:10250/)");
    }

    private static void safeClose(CosmosAsyncClient clientToClose) {
        if (clientToClose != null) {
            try {
                clientToClose.close();
            } catch (Exception e) {
                logger.warn("Failed to close client", e);
            }
        }
    }

    // endregion
}
