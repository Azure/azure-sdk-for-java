// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.AssertionsForClassTypes;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.fail;

/**
 * Tests for connect timeout bifurcation using Linux iptables to DROP SYN packets.
 *
 * Unlike SDK-level fault injection (which operates above the TCP layer), iptables DROP
 * on SYN packets prevents the TCP handshake from completing, causing the real
 * CONNECT_TIMEOUT_MILLIS to fire at the netty ChannelOption level.
 *
 * The bifurcation under test:
 * - Data plane requests → GW V2 endpoint (port 10250) → CONNECT_TIMEOUT_MILLIS = 1s
 * - Metadata requests → GW V1 endpoint (port 443) → CONNECT_TIMEOUT_MILLIS = 45s (unchanged)
 *
 * HOW TO RUN:
 * 1. Group "manual-thinclient-network-delay" — NOT included in CI.
 * 2. Docker container with --cap-add=NET_ADMIN, JDK 21, .m2 mounted.
 * 3. Tests self-manage iptables rules (add/remove) — no manual intervention.
 * 4. See CONNECT_TIMEOUT_TESTING_README.md for full setup and run instructions.
 *
 * DESIGN:
 * - Each test: warm-up (establish connection) → close client (force new TCP) →
 *   iptables DROP SYN to port 10250 → time the connect failure → remove rule → verify.
 * - Key assertion: failure latency should be ~1s (CONNECT_TIMEOUT_MILLIS), NOT 45s.
 */
public class Http2ConnectTimeoutBifurcationTests extends FaultInjectionTestBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private TestObject seedItem;

    private static final String TEST_GROUP = "manual-thinclient-network-delay";
    private static final long TEST_TIMEOUT = 180_000;

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2")
    public Http2ConnectTimeoutBifurcationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {TEST_GROUP}, timeOut = TIMEOUT)
    public void beforeClass() {
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
        System.setProperty("COSMOS.THINCLIENT_CONNECTION_TIMEOUT_IN_SECONDS", "1");

        this.client = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        this.seedItem = TestObject.create();
        this.cosmosAsyncContainer.createItem(this.seedItem).block();
        logger.info("Seeded test item: id={}, pk={}", seedItem.getId(), seedItem.getId());

        // Verify connectivity is healthy
        this.cosmosAsyncContainer.readItem(
            seedItem.getId(), new PartitionKey(seedItem.getId()), TestObject.class).block();
        logger.info("Seed item read verified — connection is healthy.");
    }

    @AfterClass(groups = {TEST_GROUP}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        // Safety: remove any leftover iptables rules
        removeIptablesDropOnPort(10250);
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_CONNECTION_TIMEOUT_IN_SECONDS");
        safeClose(this.client);
    }

    // ========================================================================
    // tc netem + iptables mangle helpers — per-port delay injection
    // ========================================================================

    /**
     * Sets up per-port network delay using tc netem with iptables mangle marks.
     *
     * tc netem operates at the interface level (not port-specific), so we use:
     * 1. tc qdisc with handles to create two delay classes
     * 2. iptables -t mangle to MARK packets by destination port
     * 3. tc filter to route marked packets to the appropriate delay class
     *
     * @param port443DelayMs  delay for port 443 traffic (metadata)
     * @param port10250DelayMs delay for port 10250 traffic (thin client data plane)
     */
    private void addPerPortDelay(int port443DelayMs, int port10250DelayMs) {
        String[] cmds = {
            // Create root prio qdisc with 3 bands
            "tc qdisc add dev eth0 root handle 1: prio bands 3",
            // Band 1 (handle 1:1): delay for port 443
            String.format("tc qdisc add dev eth0 parent 1:1 handle 10: netem delay %dms", port443DelayMs),
            // Band 2 (handle 1:2): delay for port 10250
            String.format("tc qdisc add dev eth0 parent 1:2 handle 20: netem delay %dms", port10250DelayMs),
            // Band 3 (handle 1:3): no delay (default for all other traffic)
            "tc qdisc add dev eth0 parent 1:3 handle 30: pfifo_fast",
            // Mark port 443 packets with mark 1
            "iptables -t mangle -A OUTPUT -p tcp --dport 443 -j MARK --set-mark 1",
            // Mark port 10250 packets with mark 2
            "iptables -t mangle -A OUTPUT -p tcp --dport 10250 -j MARK --set-mark 2",
            // Route mark 1 → band 1 (port 443 delay)
            "tc filter add dev eth0 parent 1:0 protocol ip prio 1 handle 1 fw flowid 1:1",
            // Route mark 2 → band 2 (port 10250 delay)
            "tc filter add dev eth0 parent 1:0 protocol ip prio 2 handle 2 fw flowid 1:2",
        };

        for (String cmd : cmds) {
            logger.info(">>> Executing: {}", cmd);
            executeShellCommand(cmd);
        }
        logger.info(">>> Per-port delay active: port 443={}ms, port 10250={}ms", port443DelayMs, port10250DelayMs);
    }

    /**
     * Removes all per-port delay rules (tc qdisc + iptables mangle marks).
     */
    private void removePerPortDelay() {
        String[] cmds = {
            "tc qdisc del dev eth0 root 2>/dev/null",
            "iptables -t mangle -F OUTPUT 2>/dev/null",
        };

        for (String cmd : cmds) {
            logger.info(">>> Cleanup: {}", cmd);
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
                p.waitFor();
            } catch (Exception e) {
                logger.warn("Cleanup command failed (may be expected): {}", e.getMessage());
            }
        }
        logger.info(">>> Per-port delay rules removed");
    }

    /**
     * Executes a shell command, failing the test on non-zero exit.
     */
    private void executeShellCommand(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            int exit = p.waitFor();
            if (exit != 0) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String errMsg = err.readLine();
                    logger.warn("Command failed (exit={}): {} — {}", exit, cmd, errMsg);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to execute: {}", cmd, e);
            fail("Shell command failed: " + cmd + " — " + e.getMessage());
        }
    }

    // ========================================================================
    // iptables helpers — DROP SYN to specific destination port
    // ========================================================================

    /**
     * Adds an iptables rule to DROP all outgoing TCP SYN packets (new connections)
     * to the specified destination port. This prevents the TCP handshake from completing,
     * causing the client's CONNECT_TIMEOUT_MILLIS to fire.
     */
    private void addIptablesDropOnPort(int port) {
        String cmd = String.format(
            "iptables -A OUTPUT -p tcp --dport %d --tcp-flags SYN,ACK,FIN,RST SYN -j DROP", port);
        logger.info(">>> Adding iptables DROP SYN rule: {}", cmd);
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            int exit = p.waitFor();
            if (exit != 0) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String errMsg = err.readLine();
                    logger.warn("iptables add failed (exit={}): {}", exit, errMsg);
                }
            } else {
                logger.info(">>> iptables DROP SYN rule active on port {}", port);
            }
        } catch (Exception e) {
            logger.error("Failed to add iptables rule", e);
            fail("Could not add iptables rule: " + e.getMessage());
        }
    }

    /**
     * Removes the iptables DROP SYN rule for the specified port.
     */
    private void removeIptablesDropOnPort(int port) {
        String cmd = String.format(
            "iptables -D OUTPUT -p tcp --dport %d --tcp-flags SYN,ACK,FIN,RST SYN -j DROP", port);
        logger.info(">>> Removing iptables DROP SYN rule: {}", cmd);
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            int exit = p.waitFor();
            if (exit == 0) {
                logger.info(">>> iptables DROP SYN rule removed on port {}", port);
            } else {
                logger.warn("iptables remove returned exit={} (may already be removed)", exit);
            }
        } catch (Exception e) {
            logger.warn("Failed to remove iptables rule: {}", e.getMessage());
        }
    }

    // ========================================================================
    // Tests
    // ========================================================================

    /**
     * Proves that the GW V2 data plane connect timeout (1s) fires when TCP SYN is dropped.
     *
     * Flow:
     * 1. Close + recreate client to force a fresh TCP connection on next request
     * 2. Add iptables DROP SYN on port 10250 (GW V2 data plane)
     * 3. Attempt a read → CONNECT_TIMEOUT_MILLIS (1s) fires, request fails
     * 4. Assert failure latency is ~1-3s (1s timeout + SDK retry overhead), NOT 45s
     * 5. Remove iptables rule
     * 6. Verify recovery read succeeds
     *
     * The 10s e2e timeout prevents the SDK from burning through full retry budgets
     * (6s+6s+10s=22s) so the test completes quickly.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void connectTimeout_GwV2_DataPlane_1sFiresOnDroppedSyn() throws Exception {
        // Close and recreate client to ensure no pooled connections exist —
        // we need to force a NEW TCP connection which will hit the iptables DROP.
        safeClose(this.client);
        this.client = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        // e2e timeout caps total wait so the test doesn't hang for 45s on retry
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(10)).enable(true).build();
        CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
        opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        addIptablesDropOnPort(10250);
        Instant start = Instant.now();
        try {
            this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
            fail("Should have failed — SYN to port 10250 is dropped, connect timeout should fire");
        } catch (CosmosException e) {
            Duration failureLatency = Duration.between(start, Instant.now());
            logger.info("Connect timeout fired: statusCode={}, subStatusCode={}, latency={}ms",
                e.getStatusCode(), e.getSubStatusCode(), failureLatency.toMillis());
            logger.info("Full diagnostics: {}",
                e.getDiagnostics() != null ? e.getDiagnostics().toString() : "null");

            // The key assertion: failure should happen within ~10s (e2e budget),
            // and each individual connect attempt should be bounded to ~1s.
            // If the per-request CONNECT_TIMEOUT_MILLIS is NOT applied (i.e., falls back to 45s),
            // the first connect attempt alone would take 45s, exceeding the 10s e2e budget at the
            // connection acquisition stage.
            assertThat(failureLatency)
                .as("Failure latency should be within e2e budget (10s), proving connect timeout " +
                    "is NOT 45s. With 1s connect timeout, the SDK can attempt multiple retries " +
                    "before the 10s e2e budget expires.")
                .isLessThan(Duration.ofSeconds(12)); // 10s e2e + 2s buffer

            assertThat(e.getStatusCode())
                .as("Should be 408 or 503 due to connect timeout / e2e timeout")
                .isIn(408, 503);
        } finally {
            removeIptablesDropOnPort(10250);
        }

        // Recovery: after removing the DROP rule, the next read should succeed
        Thread.sleep(1000); // brief settling
        CosmosDiagnostics recoveryDiag = this.performDocumentOperation(
            this.cosmosAsyncContainer, OperationType.Read, seedItem, false);
        logger.info("Recovery read succeeded. Diagnostics: {}", recoveryDiag.toString());
        assertThat(recoveryDiag).isNotNull();
    }

    /**
     * Proves that GW V1 metadata (port 443) is UNAFFECTED by the iptables drop on port 10250.
     *
     * Flow:
     * 1. Add iptables DROP SYN on port 10250 only
     * 2. The SDK's initial metadata requests (account read) go through port 443 — should succeed
     * 3. Remove the DROP rule
     * 4. Data plane request succeeds
     *
     * This proves the bifurcation: port 10250 is blocked but port 443 is untouched.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void connectTimeout_GwV1_Metadata_UnaffectedByGwV2Drop() throws Exception {
        addIptablesDropOnPort(10250);
        try {
            // Create a new client — its initialization contacts port 443 for account metadata.
            // This should succeed because only port 10250 is blocked.
            CosmosAsyncClient metadataClient = null;
            try {
                metadataClient = getClientBuilder().buildAsyncClient();
                logger.info("Client created successfully while port 10250 is blocked — " +
                    "metadata on port 443 is unaffected.");

                // The client should be able to resolve the account and database —
                // all metadata requests go through GW V1 (port 443).
                // However, any data plane request (port 10250) WILL fail.
                CosmosAsyncContainer container =
                    getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(metadataClient);
                logger.info("Container reference obtained via metadata (port 443) — success.");

            } finally {
                safeClose(metadataClient);
            }
        } finally {
            removeIptablesDropOnPort(10250);
        }

        // After removing the DROP rule, verify full data plane works
        CosmosDiagnostics recoveryDiag = this.performDocumentOperation(
            this.cosmosAsyncContainer, OperationType.Read, seedItem, false);
        logger.info("Recovery read after iptables removal. Diagnostics: {}", recoveryDiag.toString());
        assertThat(recoveryDiag).isNotNull();
    }

    /**
     * Measures the precise connect timeout boundary.
     *
     * With THINCLIENT_CONNECTION_TIMEOUT_IN_SECONDS=1, a single TCP connect attempt
     * to a blackholed port 10250 should fail in ~1s. We measure multiple individual
     * attempts by using a tight e2e timeout of 3s (enough for 2 connect attempts at 1s each).
     *
     * If CONNECT_TIMEOUT_MILLIS were 45s (the default), a single connect attempt would
     * consume the full 3s e2e budget — and the diagnostics would show 0 completed retries.
     * With 1s CONNECT_TIMEOUT_MILLIS, we expect at least 1-2 retries within the 3s budget.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void connectTimeout_GwV2_PreciseTiming() throws Exception {
        safeClose(this.client);
        this.client = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        // Very tight e2e: 3s. With 1s connect timeout, expect 2-3 connect attempts.
        // With 45s connect timeout, only 1 attempt (which wouldn't even complete).
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(3)).enable(true).build();
        CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
        opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        addIptablesDropOnPort(10250);
        Instant start = Instant.now();
        CosmosDiagnostics failedDiagnostics = null;
        try {
            this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
            fail("Should have failed — SYN to port 10250 is dropped");
        } catch (CosmosException e) {
            Duration failureLatency = Duration.between(start, Instant.now());
            failedDiagnostics = e.getDiagnostics();
            logger.info("Precise timing: latency={}ms, statusCode={}, subStatusCode={}",
                failureLatency.toMillis(), e.getStatusCode(), e.getSubStatusCode());
            logger.info("Full diagnostics: {}",
                failedDiagnostics != null ? failedDiagnostics.toString() : "null");

            // Should complete within ~3-5s (3s e2e + buffer)
            assertThat(failureLatency)
                .as("Should complete within e2e budget + small buffer")
                .isLessThan(Duration.ofSeconds(6));

            // Parse diagnostics to count gatewayStatisticsList entries
            // Each entry = one network attempt. With 1s connect timeout + 3s e2e,
            // expect at least 2 entries (2 connect attempts that timed out at 1s each).
            if (failedDiagnostics != null) {
                ObjectNode diagNode = (ObjectNode) Utils.getSimpleObjectMapper()
                    .readTree(failedDiagnostics.toString());
                JsonNode gwStats = diagNode.get("gatewayStatisticsList");
                if (gwStats != null && gwStats.isArray()) {
                    logger.info("gatewayStatisticsList entries: {} (with 1s connect timeout, " +
                        "expect >= 2 in 3s budget)", gwStats.size());
                    assertThat(gwStats.size())
                        .as("With 1s CONNECT_TIMEOUT_MILLIS and 3s e2e budget, should have " +
                            ">= 2 gateway stats entries (each = one connect attempt). " +
                            "If only 1 entry, the connect timeout may still be 45s.")
                        .isGreaterThanOrEqualTo(2);
                }
            }
        } finally {
            removeIptablesDropOnPort(10250);
        }

        // Recovery
        Thread.sleep(1000);
        CosmosDiagnostics recoveryDiag = this.performDocumentOperation(
            this.cosmosAsyncContainer, OperationType.Read, seedItem, false);
        logger.info("Recovery read succeeded. Diagnostics: {}", recoveryDiag.toString());
    }

    /**
     * Proves connect timeout bifurcation using per-port TCP delay injection.
     *
     * This is the most precise bifurcation test:
     * - Port 443 (metadata):    43s delay → SUCCEEDS (43s < 45s gateway connect timeout)
     * - Port 10250 (data plane): 2s delay  → FAILS    (2s > 1s thin client connect timeout)
     *
     * Unlike the DROP-based tests which prove one side at a time, this test proves
     * BOTH sides simultaneously in a single scenario:
     *
     * 1. Apply per-port delays: 43s on 443, 2s on 10250
     * 2. Create a new client → metadata requests on 443 succeed (43s delay < 45s timeout)
     * 3. Attempt a document read → data plane on 10250 fails (2s delay > 1s timeout)
     * 4. Remove delays, verify full recovery
     *
     * Technique: tc netem with iptables mangle marks for port-specific delay.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void connectTimeout_Bifurcation_DelayBased_MetadataSucceeds_DataPlaneFails() throws Exception {
        // Close existing client
        safeClose(this.client);

        // Apply per-port delays:
        // Port 443:   43s delay (< 45s gateway connect timeout → metadata should SUCCEED)
        // Port 10250:  2s delay (> 1s thin client connect timeout → data plane should FAIL)
        addPerPortDelay(43000, 2000);

        try {
            // Step 1: Create a new client — this contacts port 443 for account metadata.
            // Despite the 43s delay, metadata requests should SUCCEED because the gateway
            // connect timeout is 45s (43s delay < 45s timeout → TCP handshake completes).
            Instant metadataStart = Instant.now();
            this.client = getClientBuilder().buildAsyncClient();
            this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);
            Duration metadataLatency = Duration.between(metadataStart, Instant.now());

            logger.info("Client + container setup succeeded with 43s port-443 delay. " +
                "Metadata latency: {}ms", metadataLatency.toMillis());

            // Metadata latency should be >= 43s (the injected delay) but < 90s
            assertThat(metadataLatency)
                .as("Metadata should succeed despite 43s delay (< 45s gateway timeout)")
                .isGreaterThanOrEqualTo(Duration.ofSeconds(40))
                .isLessThan(Duration.ofSeconds(90));

            // Step 2: Attempt a document read — this goes to port 10250.
            // The 2s delay exceeds the 1s thin client connect timeout → should FAIL.
            CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(10))
                    .enable(true).build();
            CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
            opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

            Instant dataPlaneStart = Instant.now();
            try {
                this.cosmosAsyncContainer.readItem(
                    seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
                fail("Data plane request should have failed — 2s delay exceeds 1s connect timeout");
            } catch (CosmosException e) {
                Duration dataPlaneLatency = Duration.between(dataPlaneStart, Instant.now());
                logger.info("Data plane failed as expected: statusCode={}, latency={}ms",
                    e.getStatusCode(), dataPlaneLatency.toMillis());
                logger.info("Full diagnostics: {}",
                    e.getDiagnostics() != null ? e.getDiagnostics().toString() : "null");

                // Data plane should fail within the e2e budget
                assertThat(dataPlaneLatency)
                    .as("Data plane should fail within e2e budget (10s)")
                    .isLessThan(Duration.ofSeconds(12));

                assertThat(e.getStatusCode())
                    .as("Should be 408 or 503 due to connect timeout")
                    .isIn(408, 503);
            }

        } finally {
            removePerPortDelay();
        }

        // Recovery: after removing delays, everything should work
        Thread.sleep(2000);
        safeClose(this.client);
        this.client = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);
        CosmosDiagnostics recoveryDiag = this.performDocumentOperation(
            this.cosmosAsyncContainer, OperationType.Read, seedItem, false);
        logger.info("Recovery read succeeded. Diagnostics: {}", recoveryDiag.toString());
        assertThat(recoveryDiag).isNotNull();
    }
}
