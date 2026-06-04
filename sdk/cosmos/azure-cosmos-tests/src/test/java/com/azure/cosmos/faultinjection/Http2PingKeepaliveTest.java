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
import java.net.InetAddress;
import java.net.URI;
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
 * <p>
 * Two transports exercise the same handler -- pick one per pipeline run via system properties:
 * <ul>
 *   <li>{@code COSMOS.THINCLIENT_ENABLED=true} (default): data plane goes to thin-client proxy
 *       on port 10250; iptables drops by port only (no other process uses 10250).</li>
 *   <li>{@code COSMOS.THINCLIENT_ENABLED=false}: data plane goes to the regional Gateway V2
 *       endpoint on port 443; iptables drops by destination IP + port to avoid killing
 *       unrelated TLS traffic in the JVM. Requires an account whose regional gateway
 *       negotiates {@code h2} via ALPN; the warm-up read asserts this and the test
 *       fails fast on a Classic (HTTP/1.1-only) endpoint.</li>
 * </ul>
 * Override the port explicitly with {@code -DHTTP2_PING_TEST_PORT=<n>} if needed.
 * {@code COSMOS.HTTP2_ENABLED=true} is always set by the test.
 */
public class Http2PingKeepaliveTest extends FaultInjectionTestBase {

    private static final Logger logger = LoggerFactory.getLogger(Http2PingKeepaliveTest.class);
    private static final long TEST_TIMEOUT = 120_000; // 2 minutes

    // sudo prefix: empty when running as root (Docker), "sudo " on CI VMs
    private static final String SUDO = "root".equals(System.getProperty("user.name")) ? "" : "sudo ";

    // Transport selection -- defaults to thin-client for back-compat with the existing pipeline.
    private static final boolean THIN_CLIENT_ENABLED =
        Boolean.parseBoolean(System.getProperty("COSMOS.THINCLIENT_ENABLED", "true"));
    private static final int H2_PORT =
        Integer.getInteger("HTTP2_PING_TEST_PORT", THIN_CLIENT_ENABLED ? 10250 : 443);

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
        // HTTP/2 must be enabled before the client is constructed. THINCLIENT_ENABLED is
        // set externally (Maven profile or -D) so a single test class covers both transports
        // across two pipeline runs.
        System.setProperty("COSMOS.HTTP2_ENABLED", "true");
        logger.info("Transport selected: thinClient={}, h2Port={}", THIN_CLIENT_ENABLED, H2_PORT);

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

        // Lifted out of the try so the finally-block cleanup can reach it whether or not
        // the iptables ADD ran -- finally needs the exact -D form of whatever -A we installed.
        String iptablesDelete = null;

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

            // Warm-up read -- establish the H2 connection and capture diagnostics so we can
            // (a) prove H2 was actually negotiated, (b) discover the regional endpoint host
            // for IP-scoped iptables targeting on the Compute / port-443 path.
            CosmosItemResponse<TestObject> warmup = this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), TestObject.class).block();
            assertThat(warmup).isNotNull();
            assertThat(warmup.getStatusCode()).isEqualTo(200);

            CosmosDiagnostics warmupDiag = warmup.getDiagnostics();
            String diagStr = warmupDiag.toString();
            assertThat(diagStr)
                .as("Warm-up connection must negotiate H2 (thinClient=%s). "
                    + "If false, the account does not expose an H2-capable Gateway V2 endpoint "
                    + "and this test cannot exercise the PING handler.", THIN_CLIENT_ENABLED)
                .contains("\"isHttp2\":true");

            String initialChannelId = extractParentChannelId(warmupDiag);
            String regionalHost = extractEndpointHost(warmupDiag);
            logger.info("Initial parentChannelId: {}, regionalHost: {}", initialChannelId, regionalHost);

            // iptables rule: port-only for thin-client (10250 is exclusive to thin-client traffic);
            // destination-IP + port for Compute (port 443 is shared with everything else in the JVM,
            // so we must scope to the regional gateway's resolved address).
            String iptablesAdd;
            if (THIN_CLIENT_ENABLED) {
                iptablesAdd = String.format(
                    "%siptables -A OUTPUT -p tcp --dport %d -j DROP", SUDO, H2_PORT);
                iptablesDelete = String.format(
                    "%siptables -D OUTPUT -p tcp --dport %d -j DROP", SUDO, H2_PORT);
            } else {
                String regionalIp = InetAddress.getByName(regionalHost).getHostAddress();
                logger.info("Resolved {} -> {} (Compute variant uses IP-scoped DROP)",
                    regionalHost, regionalIp);
                iptablesAdd = String.format(
                    "%siptables -A OUTPUT -p tcp -d %s --dport %d -j DROP",
                    SUDO, regionalIp, H2_PORT);
                iptablesDelete = String.format(
                    "%siptables -D OUTPUT -p tcp -d %s --dport %d -j DROP",
                    SUDO, regionalIp, H2_PORT);
            }

            logger.info("Installing iptables DROP rule: {}", iptablesAdd);
            execCommand(iptablesAdd);

            // Wait for PING timeout with consecutive failure threshold=2 (test override):
            // Round 1: 1s interval + 2s timeout = 3s (failure #1)
            // Round 2: ~1s interval + 2s timeout = 3s (failure #2 >= threshold -> close)
            // Total ~6s + buffer = 10s
            logger.info("Waiting 10s for consecutive PING timeouts to close the connection...");
            Thread.sleep(10_000);

            // Remove iptables rule BEFORE attempting recovery read
            logger.info("Removing iptables DROP rule: {}", iptablesDelete);
            execCommand(iptablesDelete);
            // Cleared -- finally-block no longer needs to delete it.
            iptablesDelete = null;

            // Small wait for network to stabilize
            Thread.sleep(1_000);

            // Recovery read -- should succeed on a NEW connection
            String recoveryChannelId = readAndGetParentChannelId();
            logger.info("Recovery parentChannelId: {}", recoveryChannelId);

            logger.info("RESULT: thinClient={}, port={}, initial={}, recovery={}, DIFFERENT_CONNECTION={}",
                THIN_CLIENT_ENABLED, H2_PORT, initialChannelId, recoveryChannelId,
                !initialChannelId.equals(recoveryChannelId));

            // The connection MUST be different -- the old one was closed by PING timeout
            assertThat(recoveryChannelId)
                .as("After PING timeout (thinClient=%s, port=%d), the handler should have "
                    + "closed the connection. The recovery request must use a new connection.",
                    THIN_CLIENT_ENABLED, H2_PORT)
                .isNotEqualTo(initialChannelId);

            logger.info("PING timeout test passed: connection {} was closed, new connection {} established",
                initialChannelId, recoveryChannelId);
        } finally {
            // Safety: if we installed an iptables rule and didn't manage to remove it above
            // (e.g., assertion failed before we reached the eager delete), best-effort
            // remove it now. The exact -D form was captured when we built -A.
            if (iptablesDelete != null) {
                try {
                    execCommand(iptablesDelete);
                } catch (Exception ignored) {}
            }

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
     * Pulls the regional gateway hostname out of the first {@code gatewayStatisticsList[]}
     * entry whose {@code endpoint} URI exposes a host. Used by the Compute / port-443
     * path to scope the iptables DROP rule to a single destination IP -- a port-only
     * rule on :443 would also kill every other outbound TLS connection in the JVM.
     */
    private String extractEndpointHost(CosmosDiagnostics diagnostics) throws JsonProcessingException {
        ObjectNode node = (ObjectNode) Utils.getSimpleObjectMapper().readTree(diagnostics.toString());
        JsonNode gwStats = node.get("gatewayStatisticsList");
        if (gwStats != null && gwStats.isArray()) {
            for (JsonNode stat : gwStats) {
                if (stat.has("endpoint")) {
                    String endpoint = stat.get("endpoint").asText();
                    try {
                        String host = URI.create(endpoint).getHost();
                        if (host != null && !host.isEmpty()) {
                            return host;
                        }
                    } catch (IllegalArgumentException ignored) {
                        // Bad URI -- fall through to the next stat entry.
                    }
                }
            }
        }
        throw new AssertionError("Could not extract endpoint host from diagnostics: " + diagnostics);
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
