// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Standalone HTTP spy-wire tests for ReadConsistencyStrategy header propagation.
 * Does NOT extend TestSuiteBase — creates its own serverless-safe resources (no throughput).
 */
public class ReadConsistencyStrategyHttpSpyWireTest {
    private static final Logger logger = LoggerFactory.getLogger(ReadConsistencyStrategyHttpSpyWireTest.class);

    private static final ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.CosmosItemRequestOptionsAccessor
        itemOptionsAccessor = ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.getCosmosItemRequestOptionsAccessor();

    private static final long TIMEOUT = 60_000L;
    private static final String DOCUMENT_ID = UUID.randomUUID().toString();

    private CosmosAsyncClient cosmosClient;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private String databaseId;
    private String containerId;

    private SpyClientUnderTestFactory.ClientUnderTest spyClient;

    @BeforeClass(groups = {"fast"}, timeOut = TIMEOUT)
    public void beforeClass() {
        String endpoint = System.getProperty("ACCOUNT_HOST", System.getenv("ACCOUNT_HOST"));
        String key = System.getProperty("ACCOUNT_KEY", System.getenv("ACCOUNT_KEY"));

        // Create a high-level client for resource setup (no throughput = serverless-safe)
        cosmosClient = new CosmosClientBuilder()
            .endpoint(endpoint)
            .key(key)
            .gatewayMode()
            .buildAsyncClient();

        databaseId = "rcs-spy-" + UUID.randomUUID().toString().substring(0, 8);
        containerId = "testcontainer";

        cosmosClient.createDatabaseIfNotExists(databaseId).block();
        database = cosmosClient.getDatabase(databaseId);

        CosmosContainerProperties props = new CosmosContainerProperties(containerId, "/mypk");
        database.createContainerIfNotExists(props).block();
        container = database.getContainer(containerId);

        // Seed a document
        ObjectNode doc = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        doc.put("id", DOCUMENT_ID);
        doc.put("mypk", DOCUMENT_ID);
        container.createItem(doc).block();

        // Build the spy client (low-level AsyncDocumentClient with HTTP interceptor)
        ConnectionPolicy gwPolicy = new ConnectionPolicy(com.azure.cosmos.GatewayConnectionConfig.getDefaultConfig());

        try {
            spyClient = SpyClientUnderTestFactory.createClientUnderTest(
                new URI(endpoint),
                key,
                gwPolicy,
                ConsistencyLevel.SESSION,
                null, // readConsistencyStrategy (none at client level for most tests)
                new Configs(),
                null, // credential
                true, // contentResponseOnWriteEnabled
                new CosmosClientTelemetryConfig()  // clientTelemetryConfig
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        logger.info("Created spy-wire test resources: db={}, container={}", databaseId, containerId);
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
        if (cosmosClient != null) {
            cosmosClient.close();
        }
        if (spyClient != null) {
            spyClient.close();
        }
    }

    private String getDocumentLink() {
        return "dbs/" + databaseId + "/colls/" + containerId + "/docs/" + DOCUMENT_ID;
    }

    private String getCollectionLink() {
        return "dbs/" + databaseId + "/colls/" + containerId;
    }

    // region ReadConsistencyStrategy — verify HTTP headers on the wire

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void readItem_withRequestLevelRcs_headerOnWire() {
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        cosmosItemRequestOptions.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);
        cosmosItemRequestOptions.setCustomItemSerializer(CosmosItemSerializer.DEFAULT_SERIALIZER);

        spyClient.clearCapturedRequests();
        RequestOptions requestOptions = itemOptionsAccessor.toRequestOptions(cosmosItemRequestOptions);
        requestOptions.setPartitionKey(new PartitionKey(DOCUMENT_ID));
        spyClient.readDocument(getDocumentLink(), requestOptions).block();

        List<HttpRequest> requests = spyClient.getCapturedRequests();
        assertThat(requests).isNotEmpty();

        HttpRequest docRequest = findDocumentRequest(requests, getDocumentLink());
        assertThat(docRequest).as("Expected a document read request").isNotNull();

        Map<String, String> headers = docRequest.headers().toMap();
        assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
            .isEqualTo("LatestCommitted");
        assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
            .as("ConsistencyLevel header should be stripped when RCS is set")
            .isFalse();
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void readItem_withClientLevelRcs_headerOnWire() {
        String endpoint = System.getProperty("ACCOUNT_HOST", System.getenv("ACCOUNT_HOST"));
        String key = System.getProperty("ACCOUNT_KEY", System.getenv("ACCOUNT_KEY"));

        ConnectionPolicy gwPolicy = new ConnectionPolicy(com.azure.cosmos.GatewayConnectionConfig.getDefaultConfig());

        SpyClientUnderTestFactory.ClientUnderTest rcsClient;
        try {
            rcsClient = SpyClientUnderTestFactory.createClientUnderTest(
                new URI(endpoint),
                key,
                gwPolicy,
                ConsistencyLevel.SESSION,
                ReadConsistencyStrategy.LATEST_COMMITTED,
                new Configs(),
                null, true, new CosmosClientTelemetryConfig());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            rcsClient.clearCapturedRequests();
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setPartitionKey(new PartitionKey(DOCUMENT_ID));
            rcsClient.readDocument(getDocumentLink(), requestOptions).block();

            List<HttpRequest> requests = rcsClient.getCapturedRequests();
            assertThat(requests).isNotEmpty();

            HttpRequest docRequest = findDocumentRequest(requests, getDocumentLink());
            assertThat(docRequest).as("Expected a document read request").isNotNull();

            Map<String, String> headers = docRequest.headers().toMap();
            assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .isEqualTo("LatestCommitted");
            assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
                .as("ConsistencyLevel header should be stripped when client-level RCS is set")
                .isFalse();
        } finally {
            rcsClient.close();
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void readItem_withBothRcsAndCl_onlyRcsOnWire(){
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        cosmosItemRequestOptions.setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);
        cosmosItemRequestOptions.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
        cosmosItemRequestOptions.setCustomItemSerializer(CosmosItemSerializer.DEFAULT_SERIALIZER);

        spyClient.clearCapturedRequests();
        RequestOptions requestOptions = itemOptionsAccessor.toRequestOptions(cosmosItemRequestOptions);
        requestOptions.setPartitionKey(new PartitionKey(DOCUMENT_ID));
        spyClient.readDocument(getDocumentLink(), requestOptions).block();

        List<HttpRequest> requests = spyClient.getCapturedRequests();
        assertThat(requests).isNotEmpty();

        HttpRequest docRequest = findDocumentRequest(requests, getDocumentLink());
        assertThat(docRequest).as("Expected a document read request").isNotNull();

        Map<String, String> headers = docRequest.headers().toMap();
        assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
            .isEqualTo("LatestCommitted");
        assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
            .as("ConsistencyLevel header should be stripped when both CL and RCS are set — RCS wins")
            .isFalse();
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void readItem_withDefaultRcs_noRcsHeaderOnWire() {
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        cosmosItemRequestOptions.setReadConsistencyStrategy(ReadConsistencyStrategy.DEFAULT);
        cosmosItemRequestOptions.setCustomItemSerializer(CosmosItemSerializer.DEFAULT_SERIALIZER);

        spyClient.clearCapturedRequests();
        RequestOptions requestOptions = itemOptionsAccessor.toRequestOptions(cosmosItemRequestOptions);
        requestOptions.setPartitionKey(new PartitionKey(DOCUMENT_ID));
        spyClient.readDocument(getDocumentLink(), requestOptions).block();

        List<HttpRequest> requests = spyClient.getCapturedRequests();
        assertThat(requests).isNotEmpty();

        HttpRequest docRequest = findDocumentRequest(requests, getDocumentLink());
        assertThat(docRequest).as("Expected a document read request").isNotNull();

        Map<String, String> headers = docRequest.headers().toMap();
        assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
            .as("DEFAULT RCS should not emit a header — it is transparent")
            .isFalse();
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void writeItem_withClientLevelRcs_noRcsHeaderOnWire() {
        String endpoint = System.getProperty("ACCOUNT_HOST", System.getenv("ACCOUNT_HOST"));
        String key = System.getProperty("ACCOUNT_KEY", System.getenv("ACCOUNT_KEY"));

        ConnectionPolicy gwPolicy = new ConnectionPolicy(com.azure.cosmos.GatewayConnectionConfig.getDefaultConfig());

        SpyClientUnderTestFactory.ClientUnderTest rcsClient;
        try {
            rcsClient = SpyClientUnderTestFactory.createClientUnderTest(
                new URI(endpoint),
                key,
                gwPolicy,
                ConsistencyLevel.SESSION,
                ReadConsistencyStrategy.LATEST_COMMITTED,
                new Configs(),
                null, true, new CosmosClientTelemetryConfig());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            String writeDocId = UUID.randomUUID().toString();
            Document writeDoc = new Document(String.format(
                "{ \"id\": \"%s\", \"mypk\": \"%s\" }", writeDocId, writeDocId));

            rcsClient.clearCapturedRequests();
            rcsClient.createDocument(getCollectionLink(), writeDoc, null, false).block();

            List<HttpRequest> requests = rcsClient.getCapturedRequests();
            assertThat(requests).isNotEmpty();

            HttpRequest createRequest = requests.stream()
                .filter(r -> "POST".equalsIgnoreCase(r.httpMethod().toString()))
                .findFirst()
                .orElse(null);
            assertThat(createRequest).as("Expected a document create request").isNotNull();

            Map<String, String> headers = createRequest.headers().toMap();
            assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                .as("Write operations should not have RCS header")
                .isFalse();
        } finally {
            rcsClient.close();
        }
    }

    // endregion

    private static HttpRequest findDocumentRequest(List<HttpRequest> requests, String documentLink) {
        for (HttpRequest request : requests) {
            String uri = request.uri().toString();
            if ("GET".equalsIgnoreCase(request.httpMethod().toString()) && uri.contains(documentLink)) {
                return request;
            }
        }
        return null;
    }
}
