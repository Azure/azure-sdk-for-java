// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.FlakyTestRetryAnalyzer;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.TestConfigurations;
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
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Unified E2E tests for ReadConsistencyStrategy propagation across Gateway V1 and Gateway V2.
 *
 * <p>Creates two {@link CosmosAsyncClient} instances — one for each transport:
 * <ul>
 *   <li><b>Gateway V1 (HTTP/1):</b> Standard gateway mode.</li>
 *   <li><b>Gateway V2 (HTTP/2 + thin client):</b> Gateway mode with thin client JVM flag enabled.
 *       V2 tests additionally assert that requests target the thin client endpoint ({@code :10250}).</li>
 * </ul>
 *
 * <p>Does not extend {@link TestSuiteBase} to avoid {@code @BeforeSuite} shared container
 * initialization which requires provisioned throughput (incompatible with serverless accounts).
 * Uses {@link TestSuiteBase#createCollection(CosmosAsyncClient, String, CosmosContainerProperties)}
 * as a static utility for serverless-safe container creation.
 *
 * <p>Run with test group "thinclient".
 */
public class GatewayReadConsistencyStrategyE2ETest {
    private static final Logger logger = LoggerFactory.getLogger(GatewayReadConsistencyStrategyE2ETest.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final long TIMEOUT = 60_000L;

    private static final String GATEWAY_V1 = "GatewayV1";
    private static final String GATEWAY_V2 = "GatewayV2";
    private static final String DIRECT = "Direct";

    private CosmosAsyncClient gatewayV1Client;
    private CosmosAsyncClient gatewayV2Client;
    private CosmosAsyncClient directClient;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer gatewayV1Container;
    private CosmosAsyncContainer gatewayV2Container;
    private CosmosAsyncContainer directContainer;
    private String databaseId;
    private String containerId;

    @BeforeClass(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void beforeClass() {
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

        databaseId = "readConsistencyStrategy-e2e-" + UUID.randomUUID().toString().substring(0, 8);
        containerId = "testcontainer";

        gatewayV1Client = createGatewayV1Builder().buildAsyncClient();

        gatewayV1Client.createDatabaseIfNotExists(databaseId).block();
        database = gatewayV1Client.getDatabase(databaseId);

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/pk");
        TestSuiteBase.createCollection(gatewayV1Client, databaseId, containerProperties);

        gatewayV1Container = gatewayV1Client.getDatabase(databaseId).getContainer(containerId);

        // Gateway V2 client — HTTP/2 enabled, thin client flag set
        gatewayV2Client = createGatewayV2Builder().buildAsyncClient();
        gatewayV2Container = gatewayV2Client.getDatabase(databaseId).getContainer(containerId);

        // Direct mode client
        directClient = createDirectBuilder().buildAsyncClient();
        directContainer = directClient.getDatabase(databaseId).getContainer(containerId);

        logger.info("Created E2E test resources: db={}, container={}", databaseId, containerId);
    }

    @AfterClass(groups = {"thinclient"}, alwaysRun = true)
    public void afterClass() {
        if (database != null) {
            try {
                database.delete().block();
            } catch (Exception e) {
                logger.warn("Failed to delete test database", e);
            }
        }
        safeClose(gatewayV1Client);
        safeClose(gatewayV2Client);
        safeClose(directClient);
    }

    @DataProvider(name = "transportModes")
    public Object[][] transportModes() {
        return new Object[][] { { GATEWAY_V1 }, { GATEWAY_V2 }, { DIRECT } };
    }

    // region ItemRequestOptions — point reads

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readItem_withLatestCommitted(String mode) {
        String id = seedDocument(mode);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        CosmosItemResponse<ObjectNode> response =
            containerFor(mode).readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertSuccessfulRead(response);
        assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertEndpointForMode(mode, response.getDiagnostics());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readItem_withEventual(String mode) {
        String id = seedDocument(mode);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.EVENTUAL);

        CosmosItemResponse<ObjectNode> response =
            containerFor(mode).readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertSuccessfulRead(response);
        assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.EVENTUAL);
        assertEndpointForMode(mode, response.getDiagnostics());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readItem_withSession(String mode) {
        String id = seedDocument(mode);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);

        CosmosItemResponse<ObjectNode> response =
            containerFor(mode).readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertSuccessfulRead(response);
        assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.SESSION);
        assertEndpointForMode(mode, response.getDiagnostics());
    }

    // endregion

    // region QueryRequestOptions

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void queryItems_withLatestCommitted(String mode) {
        String id = seedDocument(mode);

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
            .setPartitionKey(new PartitionKey(id))
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c WHERE c.id=@id");
        querySpec.setParameters(Arrays.asList(new SqlParameter("@id", id)));

        FeedResponse<ObjectNode> response = containerFor(mode)
            .queryItems(querySpec, queryOptions, ObjectNode.class)
            .byPage()
            .blockFirst();

        assertThat(response).isNotNull();
        assertThat(response.getResults()).isNotNull();
        assertEffectiveReadConsistencyStrategy(response.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertEndpointForMode(mode, response.getCosmosDiagnostics());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void queryItems_withSession(String mode) {
        String id = seedDocument(mode);

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
            .setPartitionKey(new PartitionKey(id))
            .setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);

        SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c WHERE c.id=@id");
        querySpec.setParameters(Arrays.asList(new SqlParameter("@id", id)));

        FeedResponse<ObjectNode> response = containerFor(mode)
            .queryItems(querySpec, queryOptions, ObjectNode.class)
            .byPage()
            .blockFirst();

        assertThat(response).isNotNull();
        assertThat(response.getResults()).isNotNull();
        assertEffectiveReadConsistencyStrategy(response.getCosmosDiagnostics(), ReadConsistencyStrategy.SESSION);
        assertEndpointForMode(mode, response.getCosmosDiagnostics());
    }

    // endregion

    // region ReadAllItems

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readAllItems_withLatestCommitted(String mode) {
        String pk = UUID.randomUUID().toString();
        seedDocument(mode, UUID.randomUUID().toString(), pk);

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        FeedResponse<ObjectNode> response = containerFor(mode)
            .readAllItems(new PartitionKey(pk), queryOptions, ObjectNode.class)
            .byPage()
            .blockFirst();

        assertThat(response).isNotNull();
        assertThat(response.getResults()).isNotNull();
        assertEffectiveReadConsistencyStrategy(response.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertEndpointForMode(mode, response.getCosmosDiagnostics());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readAllItems_withSession(String mode) {
        String pk = UUID.randomUUID().toString();
        seedDocument(mode, UUID.randomUUID().toString(), pk);

        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);

        FeedResponse<ObjectNode> response = containerFor(mode)
            .readAllItems(new PartitionKey(pk), queryOptions, ObjectNode.class)
            .byPage()
            .blockFirst();

        assertThat(response).isNotNull();
        assertThat(response.getResults()).isNotNull();
        assertEffectiveReadConsistencyStrategy(response.getCosmosDiagnostics(), ReadConsistencyStrategy.SESSION);
        assertEndpointForMode(mode, response.getCosmosDiagnostics());
    }

    // endregion

    // region ChangeFeedRequestOptions

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void changeFeed_withLatestCommitted(String mode) {
        String pkValue = UUID.randomUUID().toString();
        seedDocument(mode, pkValue, pkValue);

        CosmosChangeFeedRequestOptions cfOptions = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(
                FeedRange.forLogicalPartition(new PartitionKey(pkValue)))
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        List<FeedResponse<ObjectNode>> pages = containerFor(mode)
            .queryChangeFeed(cfOptions, ObjectNode.class)
            .byPage()
            .collectList()
            .block();

        assertThat(pages).isNotNull();
        assertThat(pages.isEmpty()).isFalse();

        FeedResponse<ObjectNode> firstPage = pages.get(0);
        assertEffectiveReadConsistencyStrategy(firstPage.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertEndpointForMode(mode, firstPage.getCosmosDiagnostics());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void changeFeed_withSession(String mode) {
        String pkValue = UUID.randomUUID().toString();
        seedDocument(mode, pkValue, pkValue);

        CosmosChangeFeedRequestOptions cfOptions = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(
                FeedRange.forLogicalPartition(new PartitionKey(pkValue)))
            .setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);

        List<FeedResponse<ObjectNode>> pages = containerFor(mode)
            .queryChangeFeed(cfOptions, ObjectNode.class)
            .byPage()
            .collectList()
            .block();

        assertThat(pages).isNotNull();
        assertThat(pages.isEmpty()).isFalse();

        FeedResponse<ObjectNode> firstPage = pages.get(0);
        assertEffectiveReadConsistencyStrategy(firstPage.getCosmosDiagnostics(), ReadConsistencyStrategy.SESSION);
        assertEndpointForMode(mode, firstPage.getCosmosDiagnostics());
    }

    // endregion

    // region ReadManyRequestOptions

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readMany_withLatestCommitted(String mode) {
        String pkValue = UUID.randomUUID().toString();
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        seedDocument(mode, id1, pkValue);
        seedDocument(mode, id2, pkValue);

        List<CosmosItemIdentity> identities = Arrays.asList(
            new CosmosItemIdentity(new PartitionKey(pkValue), id1),
            new CosmosItemIdentity(new PartitionKey(pkValue), id2));

        CosmosReadManyRequestOptions readManyOptions = new CosmosReadManyRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        FeedResponse<ObjectNode> response =
            containerFor(mode).readMany(identities, readManyOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getResults()).isNotNull();
        assertThat(response.getResults().size()).isEqualTo(2);
        assertEffectiveReadConsistencyStrategy(response.getCosmosDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertEndpointForMode(mode, response.getCosmosDiagnostics());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readMany_withSession(String mode) {
        String pkValue = UUID.randomUUID().toString();
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        seedDocument(mode, id1, pkValue);
        seedDocument(mode, id2, pkValue);

        List<CosmosItemIdentity> identities = Arrays.asList(
            new CosmosItemIdentity(new PartitionKey(pkValue), id1),
            new CosmosItemIdentity(new PartitionKey(pkValue), id2));

        CosmosReadManyRequestOptions readManyOptions = new CosmosReadManyRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.SESSION);

        FeedResponse<ObjectNode> response =
            containerFor(mode).readMany(identities, readManyOptions, ObjectNode.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getResults()).isNotNull();
        assertThat(response.getResults().size()).isEqualTo(2);
        assertEffectiveReadConsistencyStrategy(response.getCosmosDiagnostics(), ReadConsistencyStrategy.SESSION);
        assertEndpointForMode(mode, response.getCosmosDiagnostics());
    }

    // endregion

    // region Client-level ReadConsistencyStrategy

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void clientLevel_latestCommitted_readItem(String mode) {
        CosmosAsyncClient clientWithReadConsistencyStrategy = null;
        try {
            clientWithReadConsistencyStrategy = builderFor(mode)
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer targetContainer = clientWithReadConsistencyStrategy.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(targetContainer, id);

            CosmosItemResponse<ObjectNode> response =
                targetContainer.readItem(id, new PartitionKey(id), ObjectNode.class).block();

            assertSuccessfulRead(response);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
            assertEndpointForMode(mode, response.getDiagnostics());
        } finally {
            safeClose(clientWithReadConsistencyStrategy);
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void clientLevel_session_readItem(String mode) {
        CosmosAsyncClient clientWithReadConsistencyStrategy = null;
        try {
            clientWithReadConsistencyStrategy = builderFor(mode)
                .readConsistencyStrategy(ReadConsistencyStrategy.SESSION)
                .buildAsyncClient();
            CosmosAsyncContainer targetContainer = clientWithReadConsistencyStrategy.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(targetContainer, id);

            CosmosItemResponse<ObjectNode> response =
                targetContainer.readItem(id, new PartitionKey(id), ObjectNode.class).block();

            assertSuccessfulRead(response);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.SESSION);
            assertEndpointForMode(mode, response.getDiagnostics());
        } finally {
            safeClose(clientWithReadConsistencyStrategy);
        }
    }

    // endregion

    // region Write operations— ReadConsistencyStrategy forced to DEFAULT

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void writeItem_readConsistencyStrategyIgnored(String mode) {
        CosmosAsyncClient clientWithReadConsistencyStrategy = null;
        try {
            clientWithReadConsistencyStrategy = builderFor(mode)
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer targetContainer = clientWithReadConsistencyStrategy.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            ObjectNode doc = createDocument(id);

            CosmosItemResponse<ObjectNode> response =
                targetContainer.createItem(doc, new PartitionKey(id), null).block();

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(201);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.DEFAULT);
        } finally {
            safeClose(clientWithReadConsistencyStrategy);
        }
    }

    // endregion

    // region Validation — GLOBAL_STRONG on Session account

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readItem_globalStrong_invalidAccount_throwsBadRequest(String mode) {
        // Account-level consistency is global; always probe via gatewayV1Client to avoid hitting
        // a Direct-mode DatabaseAccount read path that can NPE on thin-client endpoints
        // (regionalRoutingContextToRoute null in StoreReader).
        ConsistencyLevel accountLevel = CosmosBridgeInternal.getAsyncDocumentClient(gatewayV1Client)
            .getDatabaseAccount()
            .map(account -> ConsistencyLevel.valueOf(account.getConsistencyPolicy().getDefaultConsistencyLevel().name()))
            .block();
        if (accountLevel == ConsistencyLevel.STRONG) {
            throw new SkipException(
                "Skipping " + mode + " — account default consistency is STRONG; GLOBAL_STRONG is valid here so the "
                    + "BadRequest assertion does not apply.");
        }

        String id = seedDocument(mode);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.GLOBAL_STRONG);

        Throwable caughtError = null;
        try {
            containerFor(mode).readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();
        } catch (Throwable t) {
            caughtError = t;
        }

        assertThat(caughtError)
            .as("Expected BadRequestException for GLOBAL_STRONG on Session account")
            .isNotNull()
            .isInstanceOf(BadRequestException.class);
        assertThat(caughtError.getMessage()).contains("read-consistency-strategy");
    }

    // endregion

    // region Contention — both ConsistencyLevel and ReadConsistencyStrategy set

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void clientLevel_bothConsistencyLevelAndReadConsistencyStrategy_readConsistencyStrategyWins(String mode) {
        CosmosAsyncClient clientWithBoth = null;
        try {
            clientWithBoth = builderFor(mode)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED)
                .buildAsyncClient();
            CosmosAsyncContainer targetContainer = clientWithBoth.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(targetContainer, id);

            CosmosItemResponse<ObjectNode> response =
                targetContainer.readItem(id, new PartitionKey(id), ObjectNode.class).block();

            assertSuccessfulRead(response);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
            assertEndpointForMode(mode, response.getDiagnostics());
        } finally {
            safeClose(clientWithBoth);
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void requestLevel_bothConsistencyLevelAndReadConsistencyStrategy_readConsistencyStrategyWins(String mode) {
        String id = seedDocument(mode);

        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        CosmosItemResponse<ObjectNode> response =
            containerFor(mode).readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

        assertSuccessfulRead(response);
        assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
        assertEndpointForMode(mode, response.getDiagnostics());
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void requestLevelReadConsistencyStrategy_overridesClientLevelReadConsistencyStrategy(String mode) {
        CosmosAsyncClient clientWithClientReadConsistencyStrategy = null;
        try {
            clientWithClientReadConsistencyStrategy = builderFor(mode)
                .readConsistencyStrategy(ReadConsistencyStrategy.EVENTUAL)
                .buildAsyncClient();
            CosmosAsyncContainer targetContainer = clientWithClientReadConsistencyStrategy.getDatabase(databaseId).getContainer(containerId);

            String id = UUID.randomUUID().toString();
            createAndInsertDocument(targetContainer, id);

            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

            CosmosItemResponse<ObjectNode> response =
                targetContainer.readItem(id, new PartitionKey(id), readOptions, ObjectNode.class).block();

            assertSuccessfulRead(response);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
            assertEndpointForMode(mode, response.getDiagnostics());
        } finally {
            safeClose(clientWithClientReadConsistencyStrategy);
        }
    }

    // endregion

    // region Operation policy

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void operationPolicy_setsReadConsistencyStrategy(String mode) {
        CosmosAsyncClient policyClient = null;
        try {
            policyClient = builderFor(mode)
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

            assertSuccessfulRead(response);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.LATEST_COMMITTED);
            assertEndpointForMode(mode, response.getDiagnostics());
        } finally {
            safeClose(policyClient);
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void operationPolicy_readConsistencyStrategyOverridesRequestLevel(String mode) {
        CosmosAsyncClient policyClient = null;
        try {
            policyClient = builderFor(mode)
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

            assertSuccessfulRead(response);
            assertEffectiveReadConsistencyStrategy(response.getDiagnostics(), ReadConsistencyStrategy.EVENTUAL);
            assertEndpointForMode(mode, response.getDiagnostics());
        } finally {
            safeClose(policyClient);
        }
    }

    // endregion

    // region Helpers — builders

    private static CosmosClientBuilder createGatewayV1Builder() {
        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode()
            .consistencyLevel(ConsistencyLevel.SESSION);
    }

    private static CosmosClientBuilder createGatewayV2Builder() {
        GatewayConnectionConfig gwConfig = new GatewayConnectionConfig();
        gwConfig.setHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled(true));

        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode(gwConfig)
            .consistencyLevel(ConsistencyLevel.SESSION);
    }

    private static CosmosClientBuilder createDirectBuilder() {
        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode()
            .consistencyLevel(ConsistencyLevel.SESSION);
    }

    private CosmosClientBuilder builderFor(String mode) {
        switch (mode) {
            case GATEWAY_V2: return createGatewayV2Builder();
            case DIRECT: return createDirectBuilder();
            default: return createGatewayV1Builder();
        }
    }

    private CosmosAsyncContainer containerFor(String mode) {
        switch (mode) {
            case GATEWAY_V2: return gatewayV2Container;
            case DIRECT: return directContainer;
            default: return gatewayV1Container;
        }
    }

    private CosmosAsyncClient clientFor(String mode) {
        switch (mode) {
            case GATEWAY_V2: return gatewayV2Client;
            case DIRECT: return directClient;
            default: return gatewayV1Client;
        }
    }

    private static boolean isGatewayV2(String mode) {
        return GATEWAY_V2.equals(mode);
    }

    // endregion

    // region Helpers — documents

    private ObjectNode createDocument(String id) {
        return createDocument(id, id);
    }

    private ObjectNode createDocument(String id, String pk) {
        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        doc.put("id", id);
        doc.put("pk", pk);
        return doc;
    }

    private String seedDocument(String mode) {
        String id = UUID.randomUUID().toString();
        seedDocument(mode, id, id);
        return id;
    }

    private void seedDocument(String mode, String id, String pk) {
        ObjectNode doc = createDocument(id, pk);
        containerFor(mode).createItem(doc, new PartitionKey(pk), null).block();
    }

    private void createAndInsertDocument(CosmosAsyncContainer targetContainer, String id) {
        ObjectNode doc = createDocument(id);
        targetContainer.createItem(doc, new PartitionKey(id), null).block();
    }

    // endregion

    // region Helpers — assertions

    private static void assertSuccessfulRead(CosmosItemResponse<ObjectNode> response) {
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    private static void assertEffectiveReadConsistencyStrategy(CosmosDiagnostics diagnostics, ReadConsistencyStrategy expected) {
        assertThat(diagnostics).isNotNull();
        assertThat(diagnostics.getDiagnosticsContext()).isNotNull();
        assertThat(diagnostics.getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(expected);
    }

    private void assertEndpointForMode(String mode, CosmosDiagnostics diagnostics) {
        if (isGatewayV2(mode)) {
            TestSuiteBase.assertThinClientEndpointUsed(diagnostics);
        }
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
