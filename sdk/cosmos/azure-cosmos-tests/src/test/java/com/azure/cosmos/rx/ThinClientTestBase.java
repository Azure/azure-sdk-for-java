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
 *
 * <p>The concrete subclasses tag every {@code @Test}/{@code @BeforeClass}/{@code @AfterClass}
 * with BOTH the {@code "thinclient"} and {@code "thinclientEndpointProbe"} groups so the same test
 * bodies run in either CI lane against a different ambient enablement path:</p>
 * <ul>
 *   <li>{@code thinclient} lane -- launched with {@code -DCOSMOS.THINCLIENT_ENABLED=true}
 *       (explicit opt-in path).</li>
 *   <li>{@code thinclient-endpoint-probe} lane -- {@code COSMOS.THINCLIENT_ENABLED} left unset, so
 *       {@code Configs.isThinClientEnabled()} is null and routing is decided by the endpoint
 *       connectivity probe (falling back to Gateway V1 if the probe fails).</li>
 * </ul>
 */
public abstract class ThinClientTestBase extends TestSuiteBase {

    protected static final String ID_FIELD = "id";
    protected static final String PARTITION_KEY_FIELD = "mypk";
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected CosmosAsyncClient client;
    protected CosmosAsyncContainer container;

    protected ThinClientTestBase(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = SETUP_TIMEOUT)
    public void before_ThinClientTest() {
        // Thin client enablement is controlled entirely by the CI lane's ambient JVM config, so this
        // base neither sets nor clears COSMOS.THINCLIENT_ENABLED. The flag is read lazily per client
        // build, and mutating it here would leak across the classes that share this JVM. In the
        // "thinclient" lane the flag is launched as -DCOSMOS.THINCLIENT_ENABLED=true
        // (+ -DCOSMOS.HTTP2_ENABLED=true) for the explicit opt-in path; in the
        // "thinclient-endpoint-probe" lane it is left unset so the endpoint connectivity probe drives
        // routing. Relying on the ambient flag lets the same bodies run correctly in both lanes.
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        this.container = getSharedMultiPartitionCosmosContainer(this.client);

        // Clean up shared container to prevent cross-test-class pollution.
        cleanUpContainer(this.container);
    }

    @AfterClass(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
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
     * Asserts that all data requests in the diagnostics were routed through the thin client endpoint.
     *
     * <p>In the {@code thinclient-endpoint-probe} lane this doubles as a probe-success signal: it
     * only holds when the connectivity probe succeeded and selected Gateway V2 (thin client). A
     * failing probe would fall back to Gateway V1 and this assertion would fail.</p>
     */
    public static void assertThinClientEndpointUsed(CosmosDiagnostics diagnostics) {
        // Delegate to the shared TestSuiteBase implementation so the thin-client routing invariant
        // (every request via the thin-client endpoint -- including QueryPlan, which is routed to
        // Gateway V2 when thin client + HTTP/2 are opted in) and null-endpoint handling are applied
        // consistently across all thin-client tests.
        TestSuiteBase.assertThinClientEndpointUsed(diagnostics);
    }

    /**
     * Asserts that NO requests in the diagnostics were routed through the thin client endpoint,
     * confirming the gateway client used the standard :443 path.
     */
    public static void assertGatewayEndpointUsed(CosmosDiagnostics diagnostics) {
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
