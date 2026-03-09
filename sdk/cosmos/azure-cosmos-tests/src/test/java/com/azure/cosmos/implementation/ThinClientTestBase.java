// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsRequestInfo;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Base class for thin client E2E tests. Provides shared setup/teardown,
 * constants, and helper methods common to all thin client test classes.
 */
public abstract class ThinClientTestBase extends TestSuiteBase {

    protected static final String THIN_CLIENT_ENDPOINT_INDICATOR = ":10250/";
    protected static final String ID_FIELD = "id";
    protected static final String PARTITION_KEY_FIELD = "mypk";
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected CosmosAsyncClient client;
    protected CosmosAsyncContainer container;

    protected ThinClientTestBase(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"thinclient"}, timeOut = SETUP_TIMEOUT)
    public void before_ThinClientTest() {
        assertThat(this.client).isNull();
        // If running locally, uncomment these lines
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
        this.client = getClientBuilder().buildAsyncClient();
        this.container = getSharedMultiPartitionCosmosContainer(this.client);

        // Truncate shared container to prevent cross-test-class pollution.
        // Each test class starts with a clean container and manages its own data.
        truncateCollection(this.container);
    }

    @AfterClass(groups = {"thinclient"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        if (this.client != null) {
            this.client.close();
        }
    }

    /**
     * Creates a test document with id and mypk fields (matching shared container partition key).
     */
    protected ObjectNode createTestDocument(String id, String mypk) {
        ObjectNode doc = OBJECT_MAPPER.createObjectNode();
        doc.put(ID_FIELD, id);
        doc.put(PARTITION_KEY_FIELD, mypk);
        return doc;
    }

    /**
     * Deletes specific documents by their ids and partition keys. Logs warnings on failure.
     */
    protected void deleteDocuments(List<ObjectNode> documents) {
        for (ObjectNode doc : documents) {
            String id = doc.get(ID_FIELD).asText();
            String pk = doc.get(PARTITION_KEY_FIELD).asText();
            try {
                container.deleteItem(id, new PartitionKey(pk)).block();
            } catch (Exception e) {
                logger.warn("Failed to delete document with id: {}", id, e);
            }
        }
    }

    /**
     * Asserts that all requests in the diagnostics were routed through the thin client endpoint.
     */
    protected static void assertThinClientEndpointUsed(CosmosDiagnostics diagnostics) {
        assertThat(diagnostics).isNotNull();
        CosmosDiagnosticsContext ctx = diagnostics.getDiagnosticsContext();
        assertThat(ctx).isNotNull();
        Collection<CosmosDiagnosticsRequestInfo> requests = ctx.getRequestInfo();
        assertThat(requests).isNotNull();
        assertThat(requests.size()).isPositive();
        int requestCountAgainstThinClientEndpoint = 0;
        for (CosmosDiagnosticsRequestInfo requestInfo : requests) {
            if (requestInfo.getEndpoint().contains(THIN_CLIENT_ENDPOINT_INDICATOR)) {
                requestCountAgainstThinClientEndpoint++;
            }
        }
        assertThat(requestCountAgainstThinClientEndpoint).isEqualTo(requests.size());
    }
}
