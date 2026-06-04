// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HTTP/2 PING keepalive handler test.
 * <p>
 * Uses {@code iptables DROP} to blackhole PING ACKs, verifying the handler closes
 * the broken connection after consecutive PING timeouts and a subsequent request
 * uses a new connection.
 * <p>
 * Run in Docker with {@code --cap-add=NET_ADMIN} (group: {@code manual-http-network-fault}).
 * Requires an HTTP/2-capable Cosmos DB Gateway endpoint; both {@code COSMOS.HTTP2_ENABLED=true}
 * and {@code COSMOS.THINCLIENT_ENABLED=true} are set before client construction because
 * H2 is currently negotiated only on the thin-client proxy path.
 */
public class Http2PingKeepaliveTest extends FaultInjectionTestBase {

    private static final Logger logger = LoggerFactory.getLogger(Http2PingKeepaliveTest.class);
    private static final long TEST_TIMEOUT = 120_000; // 2 minutes

    // sudo prefix: empty when running as root (Docker), "sudo " on CI VMs
    private static final String SUDO = "root".equals(System.getProperty("user.name")) ? "" : "sudo ";

    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private TestObject seedItem;

    public Http2PingKeepaliveTest() {
        super(new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .gatewayMode());
    }

    @BeforeClass(groups = {"manual-http-network-fault"}, timeOut = 120_000)
    public void beforeClass() {
        // Enable HTTP/2 and thin client BEFORE client construction.
        // H2 is currently negotiated on the thin-client proxy path; both flags are required.
        System.setProperty("COSMOS.HTTP2_ENABLED", "true");
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

        this.client = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        // Seed item for reads
        this.seedItem = TestObject.create();
        this.cosmosAsyncContainer.createItem(this.seedItem).block();
        logger.info("Seed item created: {}", this.seedItem.getId());
    }

    @AfterClass(groups = {"manual-http-network-fault"}, timeOut = 60_000, alwaysRun = true)
    public void afterClass() {
        safeClose(this.client);
        System.clearProperty("COSMOS.HTTP2_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
    }

    @BeforeMethod(groups = {"manual-http-network-fault"})
    public void beforeMethod(Method method) {
        logger.info("=== START: {} ===", method.getName());
    }

    @AfterMethod(groups = {"manual-http-network-fault"})
    public void afterMethod(Method method) {
        logger.info("=== END: {} ===", method.getName());
    }

    /**
     * Uses iptables to silently discard all traffic to the Cosmos DB gateway port,
     * preventing PING ACKs from arriving. Verifies the handler detects the broken
     * connection after consecutive PING failures and closes it. A subsequent request
     * (after removing the iptables rule) uses a new connection.
     * <p>
     * This test proves PINGs are actively flowing on idle connections -- without PINGs,
     * iptables DROP would go undetected (channel.isActive() stays true, no GOAWAY arrives)
     * and the test would time out.
     * <p>
     * Requires Docker with --cap-add=NET_ADMIN or Linux with sudo.
     */
    @Test(groups = {"manual-http-network-fault"}, timeOut = TEST_TIMEOUT)
    public void connectionClosedOnPingTimeout() throws Exception {
        // Short interval + timeout for fast detection
        System.setProperty("COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS", "1");
        System.setProperty("COSMOS.HTTP2_PING_TIMEOUT_IN_SECONDS", "2");
        System.setProperty("COSMOS.HTTP2_PING_HEALTH_ENABLED", "true");
        // Override threshold to 2 for faster test (default=5 aligned with Rust SDK)
        System.setProperty("COSMOS.HTTP2_PING_FAILURE_THRESHOLD", "2");

        try {
            safeClose(this.client);

            // Single-connection pool via Http2ConnectionConfig API
            GatewayConnectionConfig gwConfig = new GatewayConnectionConfig();
            gwConfig.getHttp2ConnectionConfig()
                .setEnabled(true)
                .setMaxConnectionPoolSize(1)
                .setMinConnectionPoolSize(1);

            this.client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .consistencyLevel(ConsistencyLevel.SESSION)
                .gatewayMode(gwConfig)
                .buildAsyncClient();
            this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

            // Warm-up read -- establish H2 connection (over thin-client port 10250)
            String initialChannelId = readAndGetParentChannelId();
            logger.info("Initial parentChannelId: {}", initialChannelId);

            // Thin-client traffic goes to port 10250 on regional thin-client endpoints
            // (e.g., thin-client-mr-bs-ci-westcentralus.documents.azure.com:10250).
            // The regional hostname differs from the account-level endpoint, so we
            // blackhole by port only -- catching all thin-client traffic regardless of region.
            int thinClientPort = 10250;
            logger.info("Will blackhole port: {} (thin-client H2)", thinClientPort);

            // Blackhole all traffic to thin-client port -- prevents PING ACKs from arriving
            String iptablesRule = String.format(
                "%siptables -A OUTPUT -p tcp --dport %d -j DROP", SUDO, thinClientPort);
            logger.info("Installing iptables DROP rule: {}", iptablesRule);
            execCommand(iptablesRule);

            // Wait for PING timeout with consecutive failure threshold=2 (test override):
            // Round 1: 1s interval + 2s timeout = 3s (failure #1)
            // Round 2: ~1s interval + 2s timeout = 3s (failure #2 ≥ threshold → close)
            // Total ~6s + buffer = 10s
            logger.info("Waiting 10s for consecutive PING timeouts to close the connection...");
            Thread.sleep(10_000);

            // Remove iptables rule BEFORE attempting recovery read
            String iptablesRemove = String.format(
                "%siptables -D OUTPUT -p tcp --dport %d -j DROP", SUDO, thinClientPort);
            logger.info("Removing iptables DROP rule: {}", iptablesRemove);
            execCommand(iptablesRemove);

            // Small wait for network to stabilize
            Thread.sleep(1_000);

            // Recovery read -- should succeed on a NEW connection
            String recoveryChannelId = readAndGetParentChannelId();
            logger.info("Recovery parentChannelId: {}", recoveryChannelId);

            logger.info("RESULT: initial={}, recovery={}, DIFFERENT_CONNECTION={}",
                initialChannelId, recoveryChannelId,
                !initialChannelId.equals(recoveryChannelId));

            // The connection MUST be different -- the old one was closed by PING timeout
            assertThat(recoveryChannelId)
                .as("After PING timeout, the handler should have closed the connection. "
                    + "The recovery request must use a new connection.")
                .isNotEqualTo(initialChannelId);

            logger.info("PING timeout test passed: connection {} was closed, new connection {} established",
                initialChannelId, recoveryChannelId);
        } finally {
            // Safety: remove any leftover iptables rules
            try {
                String cleanup = String.format(
                    "%siptables -D OUTPUT -p tcp --dport 10250 -j DROP 2>/dev/null", SUDO);
                execCommand(cleanup);
            } catch (Exception ignored) {}

            System.clearProperty("COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS");
            System.clearProperty("COSMOS.HTTP2_PING_TIMEOUT_IN_SECONDS");
            System.clearProperty("COSMOS.HTTP2_PING_HEALTH_ENABLED");
            System.clearProperty("COSMOS.HTTP2_PING_FAILURE_THRESHOLD");
        }
    }

    private String readAndGetParentChannelId() throws JsonProcessingException {
        CosmosItemResponse<TestObject> response = this.cosmosAsyncContainer.readItem(
            seedItem.getId(), new PartitionKey(seedItem.getId()), TestObject.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);

        return extractParentChannelId(response.getDiagnostics());
    }

    /**
     * Mirrors {@code Http2ConnectionLifecycleTests#extractParentChannelId} -- parse
     * the diagnostics JSON rather than substring-scan the toString() so a future change
     * to JSON formatting can't silently break the test.
     */
    private String extractParentChannelId(CosmosDiagnostics diagnostics) throws JsonProcessingException {
        ObjectNode node = (ObjectNode) Utils.getSimpleObjectMapper().readTree(diagnostics.toString());
        JsonNode gwStats = node.get("gatewayStatisticsList");
        if (gwStats != null && gwStats.isArray()) {
            for (JsonNode stat : gwStats) {
                if (stat.has("parentChannelId")) {
                    String id = stat.get("parentChannelId").asText();
                    if (id != null && !id.isEmpty() && !"null".equals(id)) {
                        return id;
                    }
                }
            }
        }
        throw new AssertionError("Could not extract parentChannelId from diagnostics: " + diagnostics);
    }

    /**
     * Runs a shell command and throws on non-zero exit. The test must fail loudly
     * if iptables setup fails (e.g., missing NET_ADMIN capability), rather than
     * silently continuing without network fault injection and reporting a
     * misleading assertion failure downstream. Cleanup-only callers in
     * {@code finally} blocks can swallow the exception locally.
     */
    private static void execCommand(String command) throws Exception {
        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        int exit = p.waitFor();
        if (exit != 0) {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(p.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String stderr = sb.toString().trim();
            logger.warn("Command '{}' exited with code {}: {}", command, exit, stderr);
            throw new RuntimeException("Command failed (exit=" + exit + "): " + command + " -- " + stderr);
        }
    }
}
