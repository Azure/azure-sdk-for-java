// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
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
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

/**
 * E2E tests for ReadConsistencyStrategy through compute gateway (Gateway V1) mode.
 * Verifies that the HTTP header x-ms-cosmos-read-consistency-strategy is
 * correctly sent to the compute gateway and processed.
 *
 * Covers all request option types that expose ReadConsistencyStrategy:
 * - CosmosItemRequestOptions (point reads)
 * - CosmosQueryRequestOptions (queries)
 * - CosmosChangeFeedRequestOptions (change feed)
 * - CosmosReadManyRequestOptions (read many)
 * - CosmosClientBuilder (client-level default)
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

        databaseId = "rcs-gw-" + UUID.randomUUID().toString().substring(0, 8);
        containerId = "testcontainer";

        client.createDatabaseIfNotExists(databaseId).block();
        database = client.getDatabase(databaseId);

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/pk");
        database.createContainerIfNotExists(containerProperties).block();
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

    // region ItemRequestOptions

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_withLatestCommitted() {
        String id = UUID.randomUUID().toString();
        createAndInsertDocument(id);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertEffectiveRcs(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_withEventual() {
        String id = UUID.randomUUID().toString();
        createAndInsertDocument(id);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.EVENTUAL);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertEffectiveRcs(response.getDiagnostics(), ReadConsistencyStrategy.EVENTUAL);
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_withSession() {
        String id = UUID.randomUUID().toString();
        createAndInsertDocument(id);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertEffectiveRcs(response.getDiagnostics(), ReadConsistencyStrategy.SESSION);
    }

    // endregion

    // region QueryRequestOptions

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_queryItems_withLatestCommitted() {
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
        assertEffectiveRcs(response.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
    }

    // endregion

    // region ChangeFeedRequestOptions

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_changeFeed_withLatestCommitted() {
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
        assertEffectiveRcs(firstPage.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
    }

    // endregion

    // region ReadManyRequestOptions

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readMany_withLatestCommitted() {
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
        assertEffectiveRcs(response.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
    }

    // endregion

    // region Client-level RCS

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_clientLevel_latestCommitted_readItem() {
        CosmosAsyncClient clientWithRcs = null;
        try {
            clientWithRcs = createGatewayBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer containerWithRcs = clientWithRcs.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(containerWithRcs, id);

            CosmosItemResponse<ObjectNode> response =
                containerWithRcs.readItem(id, new PartitionKey(id), ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertEffectiveRcs(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        } finally {
            safeClose(clientWithRcs);
        }
    }

    // endregion

    // region Write operations — RCS forced to DEFAULT

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
            assertEffectiveRcs(response.getDiagnostics(), ReadConsistencyStrategy.DEFAULT);
        } finally {
            safeClose(clientWithRcs);
        }
    }

    // endregion

    // region Validation — GLOBAL_STRONG on Session account

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_readItem_globalStrong_invalidAccount_throwsBadRequest() {
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

    // region Both ConsistencyLevel and RCS — RCS wins

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_bothConsistencyLevelAndRcs_rcsWins() {
        CosmosAsyncClient clientWithBoth = null;
        try {
            clientWithBoth = createGatewayBuilder()
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
            assertEffectiveRcs(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        } finally {
            safeClose(clientWithBoth);
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_requestLevel_bothClAndRcs_rcsWins() {
        // Request-level contention: options set both ConsistencyLevel and RCS.
        // RCS should win — gateway must not reject with dual-header error.
        String id = UUID.randomUUID().toString();
        createAndInsertDocument(id);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        CosmosItemResponse<ObjectNode> response =
            container.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertEffectiveRcs(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void gateway_requestLevelRcs_overridesClientLevelRcs() {
        // Request-level RCS should override client-level RCS.
        CosmosAsyncClient clientWithClientRcs = null;
        try {
            clientWithClientRcs = createGatewayBuilder()
                .readConsistencyStrategy(ReadConsistencyStrategy.EVENTUAL)
                .buildAsyncClient();
            CosmosAsyncContainer containerWithRcs = clientWithClientRcs.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(containerWithRcs, id);

            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

            CosmosItemResponse<ObjectNode> response =
                containerWithRcs.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertEffectiveRcs(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        } finally {
            safeClose(clientWithClientRcs);
        }
    }

    // endregion

    // region Helpers

    private CosmosClientBuilder createGatewayBuilder() {
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

    private static void assertEffectiveRcs(CosmosDiagnostics diagnostics, ReadConsistencyStrategy expected) {
        assertThat(diagnostics).isNotNull();
        assertThat(diagnostics.getDiagnosticsContext()).isNotNull();
        assertThat(diagnostics.getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(expected);
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
