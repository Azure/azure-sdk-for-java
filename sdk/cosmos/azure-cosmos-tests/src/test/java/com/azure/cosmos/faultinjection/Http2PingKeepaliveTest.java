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
     * End-to-end H3 verification: a read request issued WHILE the connection is being
     * blackholed (iptables DROP on the thin-client / regional-gateway port) is held in
     * flight while {@link com.azure.cosmos.implementation.http.Http2PingHandler}
     * closes the parent channel after consecutive PING ACK timeouts. The closed
     * channel propagates a typed
     * {@link com.azure.cosmos.implementation.http.Http2PingTimeoutChannelClosedException}
     * to the in-flight child stream, which {@code RxGatewayStoreModel} stamps with
     * {@code SubStatusCodes.GATEWAY_HTTP2_PING_TIMEOUT_CHANNEL_CLOSED (10006)}. The
     * test then asserts that {@code ClientRetryPolicy} retried the request and the
     * eventual success landed on the SAME regional gateway endpoint, proving that
     * the PING-driven close did NOT trigger {@code markEndpointUnavailableForRead}
     * or cross-region failover.
     * <p>
     * Without this fix, the same exception would have been classified as a generic
     * {@code GATEWAY_ENDPOINT_UNAVAILABLE (10001)}, refresh-location would have run,
     * the regional endpoint would have been marked down, and the retry would have
     * either failed or landed on a different region depending on preferred-regions.
     * <p>
     * Requires Docker with {@code --cap-add=NET_ADMIN} or Linux with sudo.
     */
    @Test(groups = {"manual-http-network-fault"}, timeOut = TEST_TIMEOUT)
    public void inFlightReadRetriesInSameRegionAfterPingClose() throws Exception {
        // Short interval + timeout for fast detection
        System.setProperty("COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS", "1");
        System.setProperty("COSMOS.HTTP2_PING_TIMEOUT_IN_SECONDS", "2");
        System.setProperty("COSMOS.HTTP2_PING_HEALTH_ENABLED", "true");
        // Override threshold to 2 for faster test (default=5 aligned with Rust SDK)
        System.setProperty("COSMOS.HTTP2_PING_FAILURE_THRESHOLD", "2");

        // Lifted out of the try so the finally-block cleanup can reach it whether or not
        // the iptables ADD ran -- finally needs the exact -D form of whatever -A we installed.
        String iptablesDelete = null;
        Thread iptablesRemovalThread = null;

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
            // for IP-scoped iptables targeting on the Compute / port-443 path, and
            // (c) capture the initial channel-id and endpoint host to compare against
            // the recovery response.
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
            String warmupEndpointHost = extractEndpointHost(warmupDiag);
            logger.info("Warm-up: parentChannelId={}, endpointHost={}", initialChannelId, warmupEndpointHost);

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
                String regionalIp = InetAddress.getByName(warmupEndpointHost).getHostAddress();
                logger.info("Resolved {} -> {} (Compute variant uses IP-scoped DROP)",
                    warmupEndpointHost, regionalIp);
                iptablesAdd = String.format(
                    "%siptables -A OUTPUT -p tcp -d %s --dport %d -j DROP",
                    SUDO, regionalIp, H2_PORT);
                iptablesDelete = String.format(
                    "%siptables -D OUTPUT -p tcp -d %s --dport %d -j DROP",
                    SUDO, regionalIp, H2_PORT);
            }

            logger.info("Installing iptables DROP rule: {}", iptablesAdd);
            execCommand(iptablesAdd);

            // Schedule iptables removal in a background thread. The in-flight read below
            // will block on this rule lifting; ClientRetryPolicy retries the failed
            // PING-close attempt with bounded backoff (max 120 retries; see
            // Configs.DEFAULT_CLIENT_ENDPOINT_FAILOVER_MAX_RETRY_COUNT), and the first
            // retry attempt that lands AFTER the rule is removed succeeds on a brand
            // new TCP connection to the SAME regional gateway endpoint.
            //
            // 20s window picked to comfortably cover:
            //   * 4s for Http2PingHandler to close channel-1 (interval=1s + timeout=2s
            //     for each of the 2 PING failures)
            //   * 2-3 retry attempts, each capped by netty CONNECT_TIMEOUT_MILLIS
            //     (~5s for thin-client per ReactorNettyClient.send) when SYN-DROPped
            final String iptablesDeleteRef = iptablesDelete;
            iptablesRemovalThread = new Thread(() -> {
                try {
                    Thread.sleep(20_000);
                    logger.info("Background thread removing iptables DROP rule: {}", iptablesDeleteRef);
                    execCommand(iptablesDeleteRef);
                } catch (Exception e) {
                    logger.error("Failed to remove iptables in background thread", e);
                }
            }, "iptables-removal-thread");
            iptablesRemovalThread.setDaemon(true);
            iptablesRemovalThread.start();

            // Issue the read WHILE iptables DROP is active. Timeline:
            //   t=0:    read sent on channel-1, DROPped by iptables on the wire
            //   t=~4s:  Http2PingHandler detects 2 consecutive PING ACK timeouts
            //           (interval=1s, timeout=2s, threshold=2) and closes channel-1
            //   t=~4s:  Http2PingCloseRewrapHandler fires the typed exception on the
            //           in-flight child stream; RxGatewayStoreModel stamps subStatus
            //           10006 (GATEWAY_HTTP2_PING_TIMEOUT_CHANNEL_CLOSED);
            //           ClientRetryPolicy.shouldRetry routes via the H3 branch
            //           (shouldRetryOnGatewayTimeout, NO endpoint mark-down)
            //   t=4-20s: retry attempts open new TCP connections that SYN-DROP and
            //           time out after ~5s each (thin-client CONNECT_TIMEOUT_MILLIS)
            //   t=20s:  background thread removes iptables; next retry's SYN succeeds,
            //           H2 negotiates, the read succeeds on channel-2 against the
            //           SAME regional gateway endpoint
            logger.info("Issuing readItem while iptables DROP is active...");
            long startNanos = System.nanoTime();
            CosmosItemResponse<TestObject> response = this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), TestObject.class).block();
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

            // Wait for the iptables removal thread to finish (it should already be done)
            iptablesRemovalThread.join(5_000);
            // Background thread already removed the rule; clear so the finally-block doesn't retry.
            iptablesDelete = null;

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);

            CosmosDiagnostics recoveryDiag = response.getDiagnostics();
            String recoveryDiagStr = recoveryDiag.toString();
            String recoveryChannelId = extractParentChannelId(recoveryDiag);
            String recoveryEndpointHost = extractEndpointHost(recoveryDiag);

            logger.info("RESULT: elapsedMs={}, thinClient={}, port={}, "
                    + "initialChannel={}, recoveryChannel={}, "
                    + "warmupEndpoint={}, recoveryEndpoint={}, "
                    + "SAME_REGION={}, DIFFERENT_CONNECTION={}",
                elapsedMs, THIN_CLIENT_ENABLED, H2_PORT,
                initialChannelId, recoveryChannelId,
                warmupEndpointHost, recoveryEndpointHost,
                warmupEndpointHost.equals(recoveryEndpointHost),
                !initialChannelId.equals(recoveryChannelId));
            logger.info("Recovery diagnostics: {}", recoveryDiagStr);

            // Assertion 1: channel-1 was actually closed; recovery used a new TCP connection.
            assertThat(recoveryChannelId)
                .as("After PING timeout (thinClient=%s, port=%d), the handler should have "
                    + "closed the connection. The recovery request must use a new connection.",
                    THIN_CLIENT_ENABLED, H2_PORT)
                .isNotEqualTo(initialChannelId);

            // Assertion 2 (the H3 invariant we're proving): recovery landed on the SAME
            // regional gateway. If ClientRetryPolicy had treated the PING-close as a
            // regional outage, refreshLocation + markEndpointUnavailableForRead would
            // have fired and the retry would have either failed (single-region account)
            // or landed on a different region (multi-region account).
            assertThat(recoveryEndpointHost)
                .as("ClientRetryPolicy must NOT trigger a region failover for PING-driven "
                    + "channel close. Recovery endpoint should match warm-up endpoint.")
                .isEqualTo(warmupEndpointHost);

            // Assertion 3: the H3 retry branch was actually exercised. Without this guard,
            // a hypothetical scenario where the warm-up's connection survived and the
            // in-flight read never failed (or fell through some other retry path) would
            // silently pass. We require evidence in diagnostics that the PING-close
            // sub-status code was stamped on at least one failed attempt.
            assertThat(recoveryDiagStr)
                .as("Diagnostics must record sub-status 10006 (GATEWAY_HTTP2_PING_TIMEOUT_"
                    + "CHANNEL_CLOSED) on at least one failed attempt, proving "
                    + "ClientRetryPolicy's H3 branch executed.")
                .containsAnyOf("10006", "GATEWAY_HTTP2_PING_TIMEOUT_CHANNEL_CLOSED",
                    "Http2PingTimeoutChannelClosedException");

            logger.info("H3 verified: PING-driven close was retried in-region without endpoint mark-down");
        } finally {
            // Safety: if the background removal thread is still alive (didn't get to sleep
            // through 20s, or the test interrupted early), interrupt it and let the
            // captured -D form below handle cleanup synchronously.
            if (iptablesRemovalThread != null && iptablesRemovalThread.isAlive()) {
                iptablesRemovalThread.interrupt();
            }
            // Safety: if we installed an iptables rule and didn't manage to remove it above
            // (e.g., assertion failed before the background thread removed it), best-effort
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
     * Returns the parentChannelId of the FINAL (successful) attempt in the
     * gatewayStatisticsList. We iterate from the end because the recovery
     * response's diagnostics records every retry attempt: the PING-closed
     * channel comes first (subStatus 10006), connection-timeout attempts in
     * the middle have no parentChannelId at all (subStatus 10001), and the
     * successful 200 attempt (which is the channel we want to compare against
     * the warm-up channel) is last. For the warm-up response with a single
     * gatewayStatistics entry, this is equivalent to picking the first.
     * <p>
     * Parses the diagnostics JSON rather than substring-scanning toString()
     * so that a future change to JSON formatting can't silently break the
     * test.
     */
    private String extractParentChannelId(CosmosDiagnostics diagnostics) throws JsonProcessingException {
        ObjectNode node = (ObjectNode) Utils.getSimpleObjectMapper().readTree(diagnostics.toString());
        JsonNode gwStats = node.get("gatewayStatisticsList");
        if (gwStats != null && gwStats.isArray()) {
            for (int i = gwStats.size() - 1; i >= 0; i--) {
                JsonNode stat = gwStats.get(i);
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
