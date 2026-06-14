// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsRequestInfo;
import com.azure.cosmos.CosmosClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.Collection;

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
        enableThinClientForTest();
        this.client = getClientBuilder().buildAsyncClient();
        this.container = getSharedMultiPartitionCosmosContainer(this.client);

        // Clean up shared container to prevent cross-test-class pollution.
        cleanUpContainer(this.container);
    }

    @AfterClass(groups = {"thinclient"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        clearThinClientForTest();
        if (this.client != null) {
            this.client.close();
        }
    }

    protected static void enableThinClientForTest() {
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
        // Thin-client routing tests in this hierarchy assert that requests actually went
        // through the proxy on port 10250 via assertThinClientEndpointUsed. The connectivity
        // probe is enabled by default in production, but the proxy-side /connectivity-probe
        // endpoint is not deployed in every CI test account yet. With the default failure
        // threshold of 1, a single failed probe cycle flips routing from the proxy to
        // Gateway V1, breaking these assertions. Disable the probe here so the routing path
        // under test is exercised deterministically; production callers still get the probe
        // ON by default. Cleared in clearThinClientForTest() to avoid leaking into other
        // test classes.
        System.setProperty("COSMOS.THINCLIENT_PROBE_ENABLED", "false");
    }

    protected static void clearThinClientForTest() {
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_ENABLED");
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

    /**
     * Asserts that NO requests in the diagnostics were routed through the thin client endpoint,
     * confirming the gateway client used the standard :443 path.
     */
    protected static void assertGatewayEndpointUsed(CosmosDiagnostics diagnostics) {
        assertThat(diagnostics).isNotNull();
        CosmosDiagnosticsContext ctx = diagnostics.getDiagnosticsContext();
        assertThat(ctx).isNotNull();
        Collection<CosmosDiagnosticsRequestInfo> requests = ctx.getRequestInfo();
        assertThat(requests).isNotNull();
        assertThat(requests.size()).isPositive();
        for (CosmosDiagnosticsRequestInfo requestInfo : requests) {
            assertThat(requestInfo.getEndpoint())
                .as("Gateway client must not route through thin client endpoint, but found: " + requestInfo.getEndpoint())
                .doesNotContain(THIN_CLIENT_ENDPOINT_INDICATOR);
        }
    }
}
