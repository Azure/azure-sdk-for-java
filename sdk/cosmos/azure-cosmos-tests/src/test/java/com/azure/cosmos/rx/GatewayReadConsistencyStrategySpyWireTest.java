// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.ChangeFeedOperationState;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.QueryFeedOperationState;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.SpyClientUnderTestFactory;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequest;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spy-wire tests for ReadConsistencyStrategy header propagation across both transports.
 *
 * <p>Creates two spy clients that intercept at the HTTP client layer:
 * <ul>
 *   <li><b>Gateway V1 (HTTP/1):</b> No thin client enablement. Requests flow through
 *       {@link RxGatewayStoreModel}. Assertions inspect HTTP headers.</li>
 *   <li><b>Gateway V2 (HTTP/2 + thin client):</b> {@code COSMOS.THINCLIENT_ENABLED=true} +
 *       {@link Http2ConnectionConfig} enabled. Requests flow through {@link ThinClientStoreModel}
 *       when thin client read locations are available. Assertions inspect the RNTBD binary frame
 *       in the HTTP body for the ReadConsistencyStrategy token (0x00FE, Byte type).</li>
 * </ul>
 *
 * <p>V2 tests are skipped when the test account does not have thin client read locations
 * (the spy client falls back to V1 silently — we detect this and skip rather than false-pass).
 */
public class GatewayReadConsistencyStrategySpyWireTest {
    private static final Logger logger = LoggerFactory.getLogger(GatewayReadConsistencyStrategySpyWireTest.class);

    private static ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.CosmosItemRequestOptionsAccessor getItemOptionsAccessor() {
        return ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.getCosmosItemRequestOptionsAccessor();
    }

    private static final long TIMEOUT = 60_000L;
    private static final String DOCUMENT_ID = UUID.randomUUID().toString();

    // V1 transport — HTTP/1, gateway mode, no thin client
    private static final String V1 = "GatewayV1";
    // V2 transport — HTTP/2, thin client enabled
    private static final String V2 = "GatewayV2";

    private CosmosAsyncClient cosmosClient;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private String databaseId;
    private String containerId;

    private SpyClientUnderTestFactory.ClientUnderTest v1SpyClient;
    private SpyClientUnderTestFactory.ClientUnderTest v2SpyClient;

    @BeforeClass(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void beforeClass() {
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

        cosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode()
            .buildAsyncClient();

        databaseId = "ReadConsistencyStrategy-spy-" + UUID.randomUUID().toString().substring(0, 8);
        containerId = "testcontainer";

        cosmosClient.createDatabaseIfNotExists(databaseId).block();

        CosmosContainerProperties props = new CosmosContainerProperties(containerId, "/mypk");
        container = TestSuiteBase.createCollection(cosmosClient, databaseId, props);
        database = cosmosClient.getDatabase(databaseId);

        // Seed a document
        ObjectNode doc = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        doc.put("id", DOCUMENT_ID);
        doc.put("mypk", DOCUMENT_ID);
        container.createItem(doc).block();

        // V1 spy — HTTP/1, no Http2ConnectionConfig → useThinClient = false
        v1SpyClient = createSpyClient(null, false);

        // V2 spy — HTTP/2 enabled, thin client JVM flag set
        v2SpyClient = createSpyClient(null, true);

        logger.info("Created spy-wire test resources: db={}, container={}", databaseId, containerId);
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
        if (cosmosClient != null) {
            cosmosClient.close();
        }
        if (v1SpyClient != null) {
            v1SpyClient.close();
        }
        if (v2SpyClient != null) {
            v2SpyClient.close();
        }
    }

    @DataProvider(name = "transportModes")
    public Object[][] transportModes() {
        return new Object[][] { { V1 }, { V2 } };
    }

    // region No contention — single header set

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void requestLevelReadConsistencyStrategy_headerOnWire_consistencyLevelStripped(String mode) {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        assertReadConsistencyStrategyOnWire(mode, spyFor(mode), options, "LatestCommitted", (byte) 0x03, true);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void clientLevelReadConsistencyStrategy_headerOnWire_consistencyLevelStripped(String mode) {
        SpyClientUnderTestFactory.ClientUnderTest readConsistencyStrategyClient = createSpyClient(ReadConsistencyStrategy.LATEST_COMMITTED, isGatewayV2(mode));
        try {
            assertReadConsistencyStrategyOnWire(mode, readConsistencyStrategyClient, new RequestOptions(), "LatestCommitted", (byte) 0x03, true);
        } finally {
            readConsistencyStrategyClient.close();
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void defaultReadConsistencyStrategy_noReadConsistencyStrategyHeaderOnWire(String mode) {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.DEFAULT);

        assertNoReadConsistencyStrategyOnWire(mode, spyFor(mode), options);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void clientLeveldefaultReadConsistencyStrategy_noReadConsistencyStrategyHeaderOnWire(String mode) {
        SpyClientUnderTestFactory.ClientUnderTest defaultReadConsistencyStrategyClient = createSpyClient(ReadConsistencyStrategy.DEFAULT, isGatewayV2(mode));
        try {
            assertNoReadConsistencyStrategyOnWire(mode, defaultReadConsistencyStrategyClient, new RequestOptions());
        } finally {
            defaultReadConsistencyStrategyClient.close();
        }
    }

    // endregion

    // region Contention — both ConsistencyLevel and ReadConsistencyStrategy present, resolution determines the winner

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void bothConsistencyLevelAndReadConsistencyStrategy_readConsistencyStrategyWins_consistencyLevelStripped(String mode) {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);
        options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);

        assertReadConsistencyStrategyOnWire(mode, spyFor(mode), options, "LatestCommitted", (byte) 0x03, true);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void requestLevelReadConsistencyStrategy_overridesClientLevelReadConsistencyStrategy(String mode) {
        // Client-level ReadConsistencyStrategy = EVENTUAL (applied to every request header via builder)
        // Request-level ReadConsistencyStrategy = LATEST_COMMITTED (per-operation override via requestContext)
        // Resolution: request-level wins.
        SpyClientUnderTestFactory.ClientUnderTest eventualReadConsistencyStrategyClient = createSpyClient(ReadConsistencyStrategy.EVENTUAL, isGatewayV2(mode));
        try {
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            options.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

            assertReadConsistencyStrategyOnWire(mode, eventualReadConsistencyStrategyClient, options, "LatestCommitted", (byte) 0x03, true);
        } finally {
            eventualReadConsistencyStrategyClient.close();
        }
    }

    // endregion

    // region Query operations — ReadConsistencyStrategy header propagation

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void query_requestLevelReadConsistencyStrategy_headerOnWire_consistencyLevelStripped(String mode) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);
        options.setPartitionKey(new PartitionKey(DOCUMENT_ID));

        HttpRequest captured = executeQueryAndCapture(mode, spyFor(mode), options);
        assertReadConsistencyStrategyOnCapturedRequest(mode, captured, "LatestCommitted", (byte) 0x03, true);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void query_defaultReadConsistencyStrategy_noReadConsistencyStrategyHeaderOnWire(String mode) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.DEFAULT);
        options.setPartitionKey(new PartitionKey(DOCUMENT_ID));

        HttpRequest captured = executeQueryAndCapture(mode, spyFor(mode), options);
        assertNoReadConsistencyStrategyOnCapturedRequest(mode, captured);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void query_bothConsistencyLevelAndReadConsistencyStrategy_readConsistencyStrategyWins_consistencyLevelStripped(String mode) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);
        options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
        options.setPartitionKey(new PartitionKey(DOCUMENT_ID));

        HttpRequest captured = executeQueryAndCapture(mode, spyFor(mode), options);
        assertReadConsistencyStrategyOnCapturedRequest(mode, captured, "LatestCommitted", (byte) 0x03, true);
    }

    // endregion

    // region Change-feed operations — ReadConsistencyStrategy header propagation
    // CosmosChangeFeedRequestOptions does not expose setConsistencyLevel, so contention is not testable here.

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void changeFeed_requestLevelReadConsistencyStrategy_headerOnWire(String mode) {
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(DOCUMENT_ID)));
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        HttpRequest captured = executeChangeFeedAndCapture(mode, spyFor(mode), options);
        // ConsistencyLevel is not user-settable on change-feed options, so don't assert strip behavior here.
        assertReadConsistencyStrategyOnCapturedRequest(mode, captured, "LatestCommitted", (byte) 0x03, false);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void changeFeed_defaultReadConsistencyStrategy_noReadConsistencyStrategyHeaderOnWire(String mode) {
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(DOCUMENT_ID)));
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.DEFAULT);

        HttpRequest captured = executeChangeFeedAndCapture(mode, spyFor(mode), options);
        assertNoReadConsistencyStrategyOnCapturedRequest(mode, captured);
    }

    // endregion

    // region readManyByPartitionKeys operations — ReadConsistencyStrategy header propagation
    // readManyByPartitionKeys uses Query infrastructure under the hood (CosmosQueryRequestOptions + QueryFeedOperationState).

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void readMany_requestLevelReadConsistencyStrategy_headerOnWire_consistencyLevelStripped(String mode) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        HttpRequest captured = executeReadManyAndCapture(mode, spyFor(mode), options);
        assertReadConsistencyStrategyOnCapturedRequest(mode, captured, "LatestCommitted", (byte) 0x03, true);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void readMany_defaultReadConsistencyStrategy_noReadConsistencyStrategyHeaderOnWire(String mode) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.DEFAULT);

        HttpRequest captured = executeReadManyAndCapture(mode, spyFor(mode), options);
        assertNoReadConsistencyStrategyOnCapturedRequest(mode, captured);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT, dataProvider = "transportModes")
    public void readMany_bothConsistencyLevelAndReadConsistencyStrategy_readConsistencyStrategyWins_consistencyLevelStripped(String mode) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);
        options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);

        HttpRequest captured = executeReadManyAndCapture(mode, spyFor(mode), options);
        assertReadConsistencyStrategyOnCapturedRequest(mode, captured, "LatestCommitted", (byte) 0x03, true);
    }

    // endregion

    // region Write operations — ReadConsistencyStrategy should not appear on writes (V1 only, writes don't route to thin client)

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void writeWithClientLevelReadConsistencyStrategy_noReadConsistencyStrategyHeaderOnWire() {
        SpyClientUnderTestFactory.ClientUnderTest readConsistencyStrategyClient = createSpyClient(ReadConsistencyStrategy.LATEST_COMMITTED, false);
        try {
            String writeDocId = UUID.randomUUID().toString();
            Document writeDoc = new Document(String.format(
                "{ \"id\": \"%s\", \"mypk\": \"%s\" }", writeDocId, writeDocId));

            readConsistencyStrategyClient.clearCapturedRequests();
            readConsistencyStrategyClient.createDocument(getCollectionLink(), writeDoc, null, false).block();

            List<HttpRequest> requests = readConsistencyStrategyClient.getCapturedRequests();
            assertThat(requests).isNotEmpty();

            HttpRequest createRequest = requests.stream()
                .filter(r -> "POST".equalsIgnoreCase(r.httpMethod().toString()))
                .findFirst()
                .orElse(null);
            assertThat(createRequest).as("Expected a document create request").isNotNull();

            Map<String, String> headers = createRequest.headers().toMap();
            assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .as("Write operations should not have ReadConsistencyStrategy header")
                .isFalse();
        } finally {
            readConsistencyStrategyClient.close();
        }
    }

    // endregion

    // region Assertion helpers — branch on transport mode

    private void assertReadConsistencyStrategyOnWire(
        String mode,
        SpyClientUnderTestFactory.ClientUnderTest client,
        CosmosItemRequestOptions cosmosOptions,
        String expectedHeaderValue,
        byte expectedRntbdByte,
        boolean expectClStripped) {

        HttpRequest captured = executeReadAndCapture(mode, client, cosmosOptions);

        if (isGatewayV2(mode)) {
            // Verify the request actually routed through thin client (port 10250)
            assertThat(captured.uri().toString())
                .as("V2 request should target thin client proxy endpoint (port 10250)")
                .contains(":10250");

            // V2: decode RNTBD frame and inspect typed tokens
            RntbdRequest rntbdRequest = decodeRntbdFrame(collectHttpBody(captured));
            Byte readConsistencyStrategyValue = rntbdRequest.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
            assertThat(readConsistencyStrategyValue)
                .as("ReadConsistencyStrategy token value should be 0x%02X", expectedRntbdByte)
                .isNotNull()
                .isEqualTo(expectedRntbdByte);
            if (expectClStripped) {
                Byte clValue = rntbdRequest.getHeader(RntbdConstants.RntbdRequestHeader.ConsistencyLevel);
                assertThat(clValue)
                    .as("ConsistencyLevel RNTBD token should not be set when ReadConsistencyStrategy is active (0 = unset)")
                    .isEqualTo((byte) 0);
            }
        } else {
            // V1: inspect HTTP headers
            Map<String, String> headers = captured.headers().toMap();
            assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .isEqualTo(expectedHeaderValue);
            if (expectClStripped) {
                assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
                    .as("ConsistencyLevel header should be stripped when ReadConsistencyStrategy is set")
                    .isFalse();
            }
        }
    }

    private void assertReadConsistencyStrategyOnWire(
        String mode,
        SpyClientUnderTestFactory.ClientUnderTest client,
        RequestOptions requestOptions,
        String expectedHeaderValue,
        byte expectedRntbdByte,
        boolean expectClStripped) {

        HttpRequest captured = executeReadAndCapture(mode, client, requestOptions);

        if (isGatewayV2(mode)) {
            assertThat(captured.uri().toString())
                .as("V2 request should target thin client proxy endpoint (port 10250)")
                .contains(":10250");

            RntbdRequest rntbdRequest = decodeRntbdFrame(collectHttpBody(captured));
            Byte readConsistencyStrategyValue = rntbdRequest.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
            assertThat(readConsistencyStrategyValue)
                .as("ReadConsistencyStrategy token value should be 0x%02X", expectedRntbdByte)
                .isNotNull()
                .isEqualTo(expectedRntbdByte);
            if (expectClStripped) {
                Byte clValue = rntbdRequest.getHeader(RntbdConstants.RntbdRequestHeader.ConsistencyLevel);
                assertThat(clValue)
                    .as("ConsistencyLevel RNTBD token should not be set when ReadConsistencyStrategy is active (0 = unset)")
                    .isEqualTo((byte) 0);
            }
        } else {
            Map<String, String> headers = captured.headers().toMap();
            assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .isEqualTo(expectedHeaderValue);
            if (expectClStripped) {
                assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
                    .as("ConsistencyLevel header should be stripped when ReadConsistencyStrategy is set")
                    .isFalse();
            }
        }
    }

    private void assertNoReadConsistencyStrategyOnWire(String mode, SpyClientUnderTestFactory.ClientUnderTest client, CosmosItemRequestOptions cosmosOptions) {
        HttpRequest captured = executeReadAndCapture(mode, client, cosmosOptions);

        if (isGatewayV2(mode)) {
            assertThat(captured.uri().toString())
                .as("V2 request should target thin client proxy endpoint (port 10250)")
                .contains(":10250");

            RntbdRequest rntbdRequest = decodeRntbdFrame(collectHttpBody(captured));
            Byte readConsistencyStrategyValue = rntbdRequest.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
            assertThat(readConsistencyStrategyValue)
                .as("ReadConsistencyStrategy RNTBD token should not be set when DEFAULT (0 = unset)")
                .isEqualTo((byte) 0);
        } else {
            Map<String, String> headers = captured.headers().toMap();
            assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .as("DEFAULT ReadConsistencyStrategy should not emit a header")
                .isFalse();
        }
    }

    private void assertNoReadConsistencyStrategyOnWire(String mode, SpyClientUnderTestFactory.ClientUnderTest client, RequestOptions requestOptions) {
        HttpRequest captured = executeReadAndCapture(mode, client, requestOptions);

        if (isGatewayV2(mode)) {
            assertThat(captured.uri().toString())
                .as("V2 request should target thin client proxy endpoint (port 10250)")
                .contains(":10250");

            RntbdRequest rntbdRequest = decodeRntbdFrame(collectHttpBody(captured));
            Byte readConsistencyStrategyValue = rntbdRequest.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
            assertThat(readConsistencyStrategyValue)
                .as("ReadConsistencyStrategy RNTBD token should not be set when DEFAULT (0 = unset)")
                .isEqualTo((byte) 0);
        } else {
            Map<String, String> headers = captured.headers().toMap();
            assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .as("DEFAULT ReadConsistencyStrategy should not emit a header")
                .isFalse();
        }
    }

    // endregion

    // region Execution + capture helpers

    private HttpRequest executeReadAndCapture(String mode, SpyClientUnderTestFactory.ClientUnderTest client, CosmosItemRequestOptions cosmosOptions) {
        client.clearCapturedRequests();
        RequestOptions requestOptions = getItemOptionsAccessor().toRequestOptions(cosmosOptions);
        requestOptions.setPartitionKey(new PartitionKey(DOCUMENT_ID));
        client.readDocument(getDocumentLink(), requestOptions).block();

        List<HttpRequest> requests = client.getCapturedRequests();
        assertThat(requests).isNotEmpty();

        HttpRequest docRequest = findDocumentReadRequest(mode, requests);
        assertThat(docRequest).as("Expected a document read request").isNotNull();
        return docRequest;
    }

    private HttpRequest executeReadAndCapture(String mode, SpyClientUnderTestFactory.ClientUnderTest client, RequestOptions requestOptions) {
        client.clearCapturedRequests();
        requestOptions.setPartitionKey(new PartitionKey(DOCUMENT_ID));
        client.readDocument(getDocumentLink(), requestOptions).block();

        List<HttpRequest> requests = client.getCapturedRequests();
        assertThat(requests).isNotEmpty();

        HttpRequest docRequest = findDocumentReadRequest(mode, requests);
        assertThat(docRequest).as("Expected a document read request").isNotNull();
        return docRequest;
    }

    private HttpRequest executeQueryAndCapture(
        String mode,
        SpyClientUnderTestFactory.ClientUnderTest client,
        CosmosQueryRequestOptions queryOptions) {

        QueryFeedOperationState state = TestUtils.createDummyQueryFeedOperationState(
            ResourceType.Document, OperationType.Query, queryOptions, cosmosClient);
        client.clearCapturedRequests();
        client.queryDocuments(getCollectionLink(), "SELECT * FROM c", state, Document.class)
            .blockFirst();

        List<HttpRequest> requests = client.getCapturedRequests();
        assertThat(requests).isNotEmpty();

        HttpRequest feedRequest = findFeedRequest(mode, requests);
        assertThat(feedRequest).as("Expected a query feed request").isNotNull();
        return feedRequest;
    }

    private HttpRequest executeChangeFeedAndCapture(
        String mode,
        SpyClientUnderTestFactory.ClientUnderTest client,
        CosmosChangeFeedRequestOptions changeFeedOptions) {

        ChangeFeedOperationState state = new ChangeFeedOperationState(
            cosmosClient,
            "spyChangeFeed",
            databaseId,
            containerId,
            ResourceType.Document,
            OperationType.ReadFeed,
            null,
            changeFeedOptions,
            new CosmosPagedFluxOptions());
        client.clearCapturedRequests();
        client.queryDocumentChangeFeedFromPagedFlux(getCollectionLink(), state, Document.class)
            .blockFirst();

        List<HttpRequest> requests = client.getCapturedRequests();
        assertThat(requests).isNotEmpty();

        HttpRequest feedRequest = findFeedRequest(mode, requests);
        assertThat(feedRequest).as("Expected a change-feed request").isNotNull();
        return feedRequest;
    }

    private HttpRequest executeReadManyAndCapture(
        String mode,
        SpyClientUnderTestFactory.ClientUnderTest client,
        CosmosQueryRequestOptions queryOptions) {

        QueryFeedOperationState state = TestUtils.createDummyQueryFeedOperationState(
            ResourceType.Document, OperationType.Query, queryOptions, cosmosClient);
        client.clearCapturedRequests();
        client.readManyByPartitionKeys(
                Collections.singletonList(new PartitionKey(DOCUMENT_ID)),
                null,
                getCollectionLink(),
                state,
                1,
                1,
                Document.class)
            .blockFirst();

        List<HttpRequest> requests = client.getCapturedRequests();
        assertThat(requests).isNotEmpty();

        HttpRequest feedRequest = findFeedRequest(mode, requests);
        assertThat(feedRequest).as("Expected a readMany feed request").isNotNull();
        return feedRequest;
    }

    private void assertReadConsistencyStrategyOnCapturedRequest(
        String mode,
        HttpRequest captured,
        String expectedHeaderValue,
        byte expectedRntbdByte,
        boolean expectClStripped) {

        if (isGatewayV2(mode)) {
            assertThat(captured.uri().toString())
                .as("V2 request should target thin client proxy endpoint (port 10250)")
                .contains(":10250");

            RntbdRequest rntbdRequest = decodeRntbdFrame(collectHttpBody(captured));
            Byte readConsistencyStrategyValue = rntbdRequest.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
            assertThat(readConsistencyStrategyValue)
                .as("ReadConsistencyStrategy token value should be 0x%02X", expectedRntbdByte)
                .isNotNull()
                .isEqualTo(expectedRntbdByte);
            if (expectClStripped) {
                Byte clValue = rntbdRequest.getHeader(RntbdConstants.RntbdRequestHeader.ConsistencyLevel);
                assertThat(clValue)
                    .as("ConsistencyLevel RNTBD token should not be set when ReadConsistencyStrategy is active (0 = unset)")
                    .isEqualTo((byte) 0);
            }
        } else {
            Map<String, String> headers = captured.headers().toMap();
            assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .isEqualTo(expectedHeaderValue);
            if (expectClStripped) {
                assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
                    .as("ConsistencyLevel header should be stripped when ReadConsistencyStrategy is set")
                    .isFalse();
            }
        }
    }

    private void assertNoReadConsistencyStrategyOnCapturedRequest(String mode, HttpRequest captured) {
        if (isGatewayV2(mode)) {
            assertThat(captured.uri().toString())
                .as("V2 request should target thin client proxy endpoint (port 10250)")
                .contains(":10250");

            RntbdRequest rntbdRequest = decodeRntbdFrame(collectHttpBody(captured));
            Byte readConsistencyStrategyValue = rntbdRequest.getHeader(RntbdConstants.RntbdRequestHeader.ReadConsistencyStrategy);
            assertThat(readConsistencyStrategyValue)
                .as("ReadConsistencyStrategy RNTBD token should not be set when DEFAULT (0 = unset)")
                .isEqualTo((byte) 0);
        } else {
            Map<String, String> headers = captured.headers().toMap();
            assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .as("DEFAULT ReadConsistencyStrategy should not emit a header")
                .isFalse();
        }
    }

    // endregion

    // region Factory + utility helpers

    private SpyClientUnderTestFactory.ClientUnderTest spyFor(String mode) {
        return isGatewayV2(mode) ? v2SpyClient : v1SpyClient;
    }

    private static boolean isGatewayV2(String mode) {
        return V2.equals(mode);
    }

    private SpyClientUnderTestFactory.ClientUnderTest createSpyClient(ReadConsistencyStrategy ReadConsistencyStrategy, boolean http2Enabled) {
        ConnectionPolicy gwPolicy = new ConnectionPolicy(GatewayConnectionConfig.getDefaultConfig());
        // Explicitly pin HTTP/2 to the requested state. The default GatewayConnectionConfig
        // installs a non-null Http2ConnectionConfig with enabled=null, which falls back to
        // the global COSMOS.HTTP2_ENABLED system property. CI sets that property to true, so
        // the "V1" spy would otherwise silently route via the thin-client path and the test's
        // V1-shaped request assertions (GET /docs/{id}, POST /docs without id) would never
        // match. Pin enabled=false explicitly to keep V1 deterministic regardless of the JVM
        // flag, and enabled=true for V2 to match the existing intent.
        gwPolicy.setHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled(http2Enabled));
        try {
            SpyClientUnderTestFactory.ClientUnderTest spy = SpyClientUnderTestFactory.createClientUnderTest(
                new URI(TestConfigurations.HOST),
                TestConfigurations.MASTER_KEY,
                gwPolicy,
                ConsistencyLevel.SESSION,
                ReadConsistencyStrategy,
                new Configs(),
                null,
                true,
                new CosmosClientTelemetryConfig());
            // The spy client's super(...) constructor bypasses the Builder path that initializes
            // operationPolicies via Builder.withOperationPolicies(...). Without this field set,
            // change-feed and other code paths that call this.operationPolicies.forEach(...) NPE.
            // Initialize to an empty list so spy clients behave like default-built production clients.
            initOperationPoliciesIfNull(spy);
            return spy;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initOperationPoliciesIfNull(SpyClientUnderTestFactory.ClientUnderTest spy) {
        try {
            Field field = RxDocumentClientImpl.class.getDeclaredField("operationPolicies");
            field.setAccessible(true);
            if (field.get(spy) == null) {
                field.set(spy, new ArrayList<>());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize operationPolicies on spy client", e);
        }
    }

    private String getDocumentLink() {
        return "dbs/" + databaseId + "/colls/" + containerId + "/docs/" + DOCUMENT_ID;
    }

    private String getCollectionLink() {
        return "dbs/" + databaseId + "/colls/" + containerId;
    }

    private HttpRequest findDocumentReadRequest(String mode, List<HttpRequest> requests) {
        for (HttpRequest request : requests) {
            String uri = request.uri().toString();
            if (isGatewayV2(mode)) {
                // Thin client sends all requests as POST to proxy (:10250) with RNTBD frame body
                if ("POST".equalsIgnoreCase(request.httpMethod().toString()) && uri.contains(":10250")) {
                    return request;
                }
            } else {
                // Gateway V1 sends document reads as GET with the document link in the URI
                if ("GET".equalsIgnoreCase(request.httpMethod().toString()) && uri.contains(getDocumentLink())) {
                    return request;
                }
            }
        }
        return null;
    }

    /**
     * Finds the feed request (query / change-feed / readMany) that targets the test collection's docs endpoint.
     * For V2, any request to the thin-client proxy port (:10250) qualifies (writes don't route through V2).
     * For V1, the feed request hits {@code .../colls/{containerId}/docs} (without a trailing {@code /docId}).
     * Query and readMany use POST; change-feed uses GET with {@code A-IM: Incremental feed}.
     */
    private HttpRequest findFeedRequest(String mode, List<HttpRequest> requests) {
        String docsPath = "/colls/" + containerId + "/docs";
        for (HttpRequest request : requests) {
            String uri = request.uri().toString();
            if (isGatewayV2(mode)) {
                if ("POST".equalsIgnoreCase(request.httpMethod().toString()) && uri.contains(":10250")) {
                    return request;
                }
            } else {
                String httpMethod = request.httpMethod().toString();
                boolean isPost = "POST".equalsIgnoreCase(httpMethod);
                boolean isGet = "GET".equalsIgnoreCase(httpMethod);
                if (!isPost && !isGet) {
                    continue;
                }
                // Skip query-plan precursor requests: queries first POST to /colls/{id}/docs with
                // x-ms-cosmos-is-query-plan-request: True (no ReadConsistencyStrategy header),
                // followed by the actual data POST to the same endpoint (which carries the header).
                String isQueryPlan = request.headers().toMap().get(HttpConstants.HttpHeaders.IS_QUERY_PLAN_REQUEST);
                if ("True".equalsIgnoreCase(isQueryPlan)) {
                    continue;
                }
                int idx = uri.indexOf(docsPath);
                if (idx < 0) {
                    continue;
                }
                // Make sure we matched the collection-docs endpoint, not a single document URI ending in /docs/{id}.
                int tail = idx + docsPath.length();
                if (tail == uri.length() || uri.charAt(tail) == '?' || uri.charAt(tail) == '/' && tail + 1 == uri.length()) {
                    return request;
                }
                // Accept URIs that have the docs path but no further document id segment.
                if (uri.endsWith(docsPath) || uri.endsWith(docsPath + "/")) {
                    return request;
                }
            }
        }
        return null;
    }

    // endregion

    // region RNTBD frame inspection helpers (for V2 assertions)

    private static byte[] collectHttpBody(HttpRequest httpRequest) {
        return httpRequest.body().reduce((a, b) -> {
            byte[] merged = new byte[a.length + b.length];
            System.arraycopy(a, 0, merged, 0, a.length);
            System.arraycopy(b, 0, merged, a.length, b.length);
            return merged;
        }).block();
    }

    /**
     * Decodes the RNTBD binary frame and returns the typed request object.
     * Uses the production decoder (RntbdRequest.decode) so token presence/absence
     * is determined by the actual RNTBD wire format, not brute-force byte scanning.
     *
     * <p>The HTTP body may contain trailing payload bytes (for query / readMany operations:
     * {@code [4-byte LE expectedLength][frame][headers][4-byte payload length][payload]}).
     * {@code expectedLength} encodes the total of {@code length prefix + frame + headers}.
     * {@link RntbdRequest#decode(ByteBuf)} computes payloadBuf size as
     * {@code expectedLength - bytes consumed so far}, which is 0 when no payload is expected —
     * but {@link com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestHeaders#decode}
     * reads tokens until the buffer is exhausted (not until the headers section ends). So we must
     * slice the buffer to exactly {@code expectedLength} bytes to prevent header decoding from
     * walking past the headers boundary and into payload bytes.
     */
    private static RntbdRequest decodeRntbdFrame(byte[] rntbdFrame) {
        ByteBuf buffer = Unpooled.wrappedBuffer(rntbdFrame);
        int expectedLength = buffer.getIntLE(buffer.readerIndex());
        ByteBuf sliced = buffer.slice(buffer.readerIndex(), expectedLength);
        return RntbdRequest.decode(sliced);
    }

    // endregion
}
