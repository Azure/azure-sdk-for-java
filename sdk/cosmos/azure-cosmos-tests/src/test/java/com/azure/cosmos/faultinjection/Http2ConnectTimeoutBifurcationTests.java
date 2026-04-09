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
 * - Data plane requests → GW V2 endpoint (port 10250) → CONNECT_TIMEOUT_MILLIS = 5s (default, configurable)
 * - Metadata requests → GW V1 endpoint (port 443) → CONNECT_TIMEOUT_MILLIS = 45s (unchanged)
 *
 * HOW TO RUN:
 * 1. Group "manual-http-network-fault" — runs in dedicated CI pipeline stage.
 * 2. Runs natively on Linux VMs (with sudo) or in Docker (with --cap-add=NET_ADMIN).
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

    private static final String TEST_GROUP = "manual-http-network-fault";
    private static final long TEST_TIMEOUT = 180_000;
    // Network interface detected at runtime — eth0 for Docker, or the default route interface on CI VMs.
    private String networkInterface;
    // Prefix for privileged commands: empty string in Docker (runs as root), "sudo " on CI VMs.
    private String sudoPrefix;

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2")
    public Http2ConnectTimeoutBifurcationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {TEST_GROUP}, timeOut = TIMEOUT)
    public void beforeClass() {
        // Detect whether we're running as root (Docker) or need sudo (CI VM)
        this.sudoPrefix = "root".equals(System.getProperty("user.name")) ? "" : "sudo ";
        this.networkInterface = detectNetworkInterface();
        logger.info("Network interface: {}, sudo: {}", this.networkInterface, !this.sudoPrefix.isEmpty());

        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
        // Use the default THINCLIENT_CONNECTION_TIMEOUT_IN_MS (5000ms) — no override.
        // Tests are designed around the 5s default to match production behavior.

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
        if (sudoPrefix != null) {
            removeIptablesDropOnPort(10250);
        }
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
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
        String iface = networkInterface;
        String[] cmds = {
            // Create root prio qdisc with 3 bands
            sudoPrefix + "tc qdisc add dev " + iface + " root handle 1: prio bands 3",
            // Band 1 (handle 1:1): delay for port 443
            String.format("%stc qdisc add dev %s parent 1:1 handle 10: netem delay %dms", sudoPrefix, iface, port443DelayMs),
            // Band 2 (handle 1:2): delay for port 10250
            String.format("%stc qdisc add dev %s parent 1:2 handle 20: netem delay %dms", sudoPrefix, iface, port10250DelayMs),
            // Band 3 (handle 1:3): no delay (default for all other traffic)
            sudoPrefix + "tc qdisc add dev " + iface + " parent 1:3 handle 30: pfifo_fast",
            // Mark port 443 packets with mark 1
            sudoPrefix + "iptables -t mangle -A OUTPUT -p tcp --dport 443 -j MARK --set-mark 1",
            // Mark port 10250 packets with mark 2
            sudoPrefix + "iptables -t mangle -A OUTPUT -p tcp --dport 10250 -j MARK --set-mark 2",
            // Route mark 1 → band 1 (port 443 delay)
            sudoPrefix + "tc filter add dev " + iface + " parent 1:0 protocol ip prio 1 handle 1 fw flowid 1:1",
            // Route mark 2 → band 2 (port 10250 delay)
            sudoPrefix + "tc filter add dev " + iface + " parent 1:0 protocol ip prio 2 handle 2 fw flowid 1:2",
        };

        for (String cmd : cmds) {
            logger.info(">>> Executing: {}", cmd);
            executeShellCommand(cmd);
        }
        logger.info(">>> Per-port delay active: port 443={}ms, port 10250={}ms", port443DelayMs, port10250DelayMs);
    }

    /**
     * Sets up per-port SYN-ONLY delay using tc netem with iptables mangle marks.
     *
     * Unlike {@link #addPerPortDelay} which delays ALL packets, this only delays
     * the initial TCP SYN packet (--tcp-flags SYN,ACK,FIN,RST SYN). This means:
     * - TCP connect phase is delayed (SYN held in kernel → CONNECT_TIMEOUT_MILLIS fires)
     * - TLS handshake, HTTP request/response, and TCP ACKs flow normally (no delay)
     *
     * This is the correct technique for testing CONNECT_TIMEOUT_MILLIS bifurcation:
     * the connect timeout fires during SYN→SYN-ACK, so only the SYN needs delaying.
     * Delaying all packets causes secondary failures (TLS handshake timeout, HTTP
     * response timeout, premature connection close) that are unrelated to connect timeout.
     *
     * @param port443SynDelayMs  SYN delay for port 443 (metadata)
     * @param port10250SynDelayMs SYN delay for port 10250 (thin client data plane)
     */
    private void addPerPortSynDelay(int port443SynDelayMs, int port10250SynDelayMs) {
        String iface = networkInterface;
        String[] cmds = {
            // Create root prio qdisc with 3 bands
            sudoPrefix + "tc qdisc add dev " + iface + " root handle 1: prio bands 3",
            // Band 1 (handle 1:1): delay for port 443 SYN
            String.format("%stc qdisc add dev %s parent 1:1 handle 10: netem delay %dms", sudoPrefix, iface, port443SynDelayMs),
            // Band 2 (handle 1:2): delay for port 10250 SYN
            String.format("%stc qdisc add dev %s parent 1:2 handle 20: netem delay %dms", sudoPrefix, iface, port10250SynDelayMs),
            // Band 3 (handle 1:3): no delay (default for all other traffic including non-SYN)
            sudoPrefix + "tc qdisc add dev " + iface + " parent 1:3 handle 30: pfifo_fast",
            // Mark ONLY SYN packets (initial TCP connect) to port 443 with mark 1
            sudoPrefix + "iptables -t mangle -A OUTPUT -p tcp --dport 443 --tcp-flags SYN,ACK,FIN,RST SYN -j MARK --set-mark 1",
            // Mark ONLY SYN packets to port 10250 with mark 2
            sudoPrefix + "iptables -t mangle -A OUTPUT -p tcp --dport 10250 --tcp-flags SYN,ACK,FIN,RST SYN -j MARK --set-mark 2",
            // Route mark 1 → band 1 (port 443 SYN delay)
            sudoPrefix + "tc filter add dev " + iface + " parent 1:0 protocol ip prio 1 handle 1 fw flowid 1:1",
            // Route mark 2 → band 2 (port 10250 SYN delay)
            sudoPrefix + "tc filter add dev " + iface + " parent 1:0 protocol ip prio 2 handle 2 fw flowid 1:2",
            // CRITICAL: Catch-all filter → band 3 (no delay) for ALL unmarked traffic.
            sudoPrefix + "tc filter add dev " + iface + " parent 1:0 protocol ip prio 99 u32 match u32 0 0 flowid 1:3",
        };

        for (String cmd : cmds) {
            logger.info(">>> Executing: {}", cmd);
            executeShellCommand(cmd);
        }
        logger.info(">>> Per-port SYN-only delay active: port 443={}ms, port 10250={}ms",
            port443SynDelayMs, port10250SynDelayMs);
    }

    /**
     * Removes all per-port delay rules (tc qdisc + iptables mangle marks).
     */
    private void removePerPortDelay() {
        String iface = networkInterface;
        String[] cmds = {
            sudoPrefix + "tc qdisc del dev " + iface + " root 2>/dev/null",
            sudoPrefix + "iptables -t mangle -F OUTPUT 2>/dev/null",
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
                    fail("Command failed (exit=" + exit + "): " + cmd + " — " + errMsg);
                }
            }
        } catch (AssertionError e) {
            throw e;
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
            "%siptables -A OUTPUT -p tcp --dport %d --tcp-flags SYN,ACK,FIN,RST SYN -j DROP", sudoPrefix, port);
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
            "%siptables -D OUTPUT -p tcp --dport %d --tcp-flags SYN,ACK,FIN,RST SYN -j DROP", sudoPrefix, port);
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

    /**
     * Detects the default-route network interface.
     * In Docker this is typically eth0. On CI VMs it may be eth0, ens5, etc.
     * Falls back to "eth0" if detection fails.
     */
    private static String detectNetworkInterface() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c",
                "ip route show default | awk '{print $5}' | head -1"});
            p.waitFor();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String iface = reader.readLine();
                if (iface != null && !iface.isEmpty() && !iface.contains("@")) {
                    return iface.trim();
                }
            }
        } catch (Exception e) {
            // fall through
        }
        return "eth0";
    }

    // ========================================================================
    // Tests
    // ========================================================================

    /**
     * Proves that the GW V2 data plane connect timeout (5s default) fires when TCP SYN is dropped.
     *
     * Flow:
     * 1. Close + recreate client to force a fresh TCP connection on next request
     * 2. Add iptables DROP SYN on port 10250 (GW V2 data plane)
     * 3. Attempt a read → CONNECT_TIMEOUT_MILLIS (5s) fires, request fails
     * 4. Assert failure latency is within e2e budget (30s), NOT 45s per attempt
     * 5. Remove iptables rule
     * 6. Verify recovery read succeeds
     *
     * The 30s e2e timeout allows multiple 5s connect attempts + SDK retry overhead.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void connectTimeout_GwV2_DataPlane_1sFiresOnDroppedSyn() throws Exception {
        // Close and recreate client to ensure no pooled connections exist —
        // we need to force a NEW TCP connection which will hit the iptables DROP.
        safeClose(this.client);
        this.client = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        // e2e timeout caps total wait so the test doesn't hang for 45s on retry.
        // With 5s connect timeout, 30s budget allows ~5-6 connect attempts.
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(30)).enable(true).build();
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

            // The key assertion: failure should happen within ~30s (e2e budget),
            // and each individual connect attempt should be bounded to ~5s.
            // If the per-request CONNECT_TIMEOUT_MILLIS is NOT applied (i.e., falls back to 45s),
            // the first connect attempt alone would take 45s, exceeding the 30s e2e budget.
            assertThat(failureLatency)
                .as("Failure latency should be within e2e budget (30s), proving connect timeout " +
                    "is NOT 45s. With 5s connect timeout, the SDK can attempt multiple retries " +
                    "before the 30s e2e budget expires.")
                .isLessThan(Duration.ofSeconds(35)); // 30s e2e + 5s buffer

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
     * With THINCLIENT_CONNECTION_TIMEOUT_IN_MS=5000 (default), a single TCP connect attempt
     * to a blackholed port 10250 should fail in ~5s. We measure multiple individual
     * attempts by using an e2e timeout of 12s (enough for 2 connect attempts at 5s each).
     *
     * If CONNECT_TIMEOUT_MILLIS were 45s (the gateway default), a single connect attempt would
     * consume the full 12s e2e budget — and the diagnostics would show 0 completed retries.
     * With 5s CONNECT_TIMEOUT_MILLIS, we expect at least 2 retries within the 12s budget.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void connectTimeout_GwV2_PreciseTiming() throws Exception {
        safeClose(this.client);
        this.client = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        // e2e: 12s. With 5s connect timeout, expect 2 connect attempts.
        // With 45s connect timeout, only 1 attempt (which wouldn't even complete).
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(12)).enable(true).build();
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

            // Should complete within ~12-15s (12s e2e + buffer)
            assertThat(failureLatency)
                .as("Should complete within e2e budget + small buffer")
                .isLessThan(Duration.ofSeconds(16));

            // Parse diagnostics to count gatewayStatisticsList entries
            // Each entry = one network attempt. With 5s connect timeout + 12s e2e,
            // expect at least 2 entries (2 connect attempts that timed out at 5s each).
            if (failedDiagnostics != null) {
                ObjectNode diagNode = (ObjectNode) Utils.getSimpleObjectMapper()
                    .readTree(failedDiagnostics.toString());
                JsonNode gwStats = diagNode.get("gatewayStatisticsList");
                if (gwStats != null && gwStats.isArray()) {
                    logger.info("gatewayStatisticsList entries: {} (with 5s connect timeout, " +
                        "expect >= 2 in 12s budget)", gwStats.size());
                    assertThat(gwStats.size())
                        .as("With 5s CONNECT_TIMEOUT_MILLIS and 12s e2e budget, should have " +
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
     * Proves connect timeout bifurcation using per-port SYN-only delay injection.
     *
     * This is the PUREST bifurcation test — same network condition on both ports,
     * different outcomes because of different CONNECT_TIMEOUT_MILLIS values:
     *
     * - Port 443 (metadata):    5s SYN delay → 5s < 45s CONNECT_TIMEOUT → connect SUCCEEDS
     * - Port 10250 (data plane): 5s SYN delay → 5s > CONNECT_TIMEOUT (default 5s) → connect FAILS
     *
     * SAME delay + different outcomes = the ONLY variable is the timeout configuration.
     * This eliminates "different delays cause different outcomes" as an alternative explanation.
     *
     * Technique: tc netem delays only SYN packets (--tcp-flags SYN,ACK,FIN,RST SYN),
     * not all traffic. This isolates the TCP connect phase — TLS handshake, HTTP request/
     * response, and TCP ACKs flow at normal speed. Unlike delaying all packets (which causes
     * TLS handshake timeout, HTTP response timeout, premature connection close), SYN-only
     * delay ONLY affects CONNECT_TIMEOUT_MILLIS, which is exactly what we're testing.
     *
     * Flow:
     * 1. Apply 5s SYN-only delay on BOTH ports
     * 2. Create a new client → metadata on port 443 succeeds (5s < 45s timeout)
     * 3. Attempt a document read → data plane on port 10250 fails (5s ≥ 5s timeout)
     * 4. Remove delays, verify full recovery
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void connectTimeout_Bifurcation_DelayBased_MetadataSucceeds_DataPlaneFails() throws Exception {
        // Close existing client to force new TCP connections on next use
        safeClose(this.client);

        // Apply SYN-only delay: 7s on BOTH ports.
        // Only the initial TCP SYN packet is delayed — all other traffic flows normally.
        // Port 443:   CONNECT_TIMEOUT = 45s → 7s delay < 45s → connect SUCCEEDS
        // Port 10250: CONNECT_TIMEOUT = 5s  → 7s delay > 5s  → connect FAILS (ConnectTimeoutException at 5s)
        addPerPortSynDelay(7000, 7000);

        try {
            // Step 1: Create a new client — metadata requests go to port 443.
            // The 7s SYN delay means the TCP handshake takes ~7s.
            // Since the gateway CONNECT_TIMEOUT is 45s, the connect SUCCEEDS.
            // If the thin client timeout (5s) were applied, the connect would FAIL at 5s
            // (before the SYN-ACK arrives at 7s). This is the decisive proof.
            Instant metadataStart = Instant.now();
            this.client = getClientBuilder().buildAsyncClient();
            this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);
            Duration metadataLatency = Duration.between(metadataStart, Instant.now());

            logger.info("Client + container setup succeeded with 7s SYN delay on port 443. " +
                "Metadata latency: {}ms (includes 7s SYN delay + TLS + HTTP at normal speed)",
                metadataLatency.toMillis());

            // Metadata latency should be >= 6s (7s SYN delay - jitter) but < 30s
            // (7s for SYN + normal speed TLS/HTTP should complete quickly)
            assertThat(metadataLatency)
                .as("Metadata should succeed despite 7s SYN delay (7s < 45s gateway CONNECT_TIMEOUT). " +
                    "If thin client timeout (5s) were applied, connect would fail at 5s.")
                .isGreaterThanOrEqualTo(Duration.ofSeconds(6))
                .isLessThan(Duration.ofSeconds(30));

            // Step 2: Attempt a document read — this goes to port 10250.
            // The SAME 7s SYN delay is applied, but CONNECT_TIMEOUT_MILLIS is 5s (default).
            // The connect timeout fires at 5s (before the delayed SYN-ACK arrives at 7s).
            // This is the bifurcation: same delay, port 443 succeeded, port 10250 fails.
            CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(30))
                    .enable(true).build();
            CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
            opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

            Instant dataPlaneStart = Instant.now();
            try {
                this.cosmosAsyncContainer.readItem(
                    seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
                fail("Data plane request should have failed — 7s SYN delay exceeds 5s connect timeout");
            } catch (CosmosException e) {
                Duration dataPlaneLatency = Duration.between(dataPlaneStart, Instant.now());
                logger.info("Data plane failed as expected: statusCode={}, latency={}ms",
                    e.getStatusCode(), dataPlaneLatency.toMillis());
                logger.info("Full diagnostics: {}",
                    e.getDiagnostics() != null ? e.getDiagnostics().toString() : "null");

                // Data plane should fail within the e2e budget
                assertThat(dataPlaneLatency)
                    .as("Data plane should fail within e2e budget (30s). " +
                        "Each connect attempt times out at 5s (not 45s), proving 5s CONNECT_TIMEOUT.")
                    .isLessThan(Duration.ofSeconds(35));

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
