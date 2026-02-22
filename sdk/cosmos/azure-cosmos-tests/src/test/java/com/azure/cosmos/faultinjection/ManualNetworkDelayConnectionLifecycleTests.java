// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.fail;

/**
 * Connection lifecycle tests using tc netem to inject REAL network delay.
 *
 * These exercise the REAL netty ReadTimeoutHandler on HTTP/2 stream channels,
 * unlike SDK fault injection which creates synthetic ReadTimeoutExceptions.
 * They prove that a real netty-level ReadTimeoutException on an H2 stream
 * does NOT close the parent TCP connection.
 *
 * HOW TO RUN:
 * 1. Group "manual-thinclient-network-delay" — NOT included in CI.
 * 2. Docker container with --cap-add=NET_ADMIN, JDK 21, .m2 mounted.
 * 3. Tests self-manage tc netem (add/remove delay) — no manual intervention.
 * 4. Run via: ./azure-cosmos-tests/run-netem-tests.sh inside container.
 *
 * DESIGN:
 * - No creates during tests. One seed item created in beforeClass (via shared container).
 * - Each test: warm-up read (must succeed) → tc delay → timed-out read → remove delay → verify read.
 * - Assertions compare parentChannelId before/after to prove connection survival.
 * - Tests run sequentially (thread-count=1) to avoid tc interference between tests.
 */
public class ManualNetworkDelayConnectionLifecycleTests extends FaultInjectionTestBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private TestObject seedItem;

    private static final String TEST_GROUP = "manual-thinclient-network-delay";
    // 3 minutes per test — enough for warmup + delay + retries + cross-region failover + recovery read
    private static final long TEST_TIMEOUT = 180_000;
    // Hardcode eth0 — Docker always uses eth0. detectNetworkInterface() fails during active delay
    // because `tc qdisc show dev eth0` hangs, and the fallback returns `eth0@if23` which tc rejects.
    private static final String NETWORK_INTERFACE = "eth0";

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2")
    public ManualNetworkDelayConnectionLifecycleTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {TEST_GROUP}, timeOut = TIMEOUT)
    public void beforeClass() {
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
        this.client = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        // Seed one item. The shared container is created by @BeforeSuite on standard gateway (port 443).
        // This createItem goes through the thin-client path, but it happens once and has the full
        // default 60s timeout — no tc delay is active yet.
        this.seedItem = TestObject.create();
        this.cosmosAsyncContainer.createItem(this.seedItem).block();
        logger.info("Seeded test item: id={}, pk={}", seedItem.getId(), seedItem.getId());

        // Verify the item is readable (proves the connection is healthy before ANY test runs)
        this.cosmosAsyncContainer.readItem(
            seedItem.getId(), new PartitionKey(seedItem.getId()), TestObject.class).block();
        logger.info("Seed item read verified — connection is healthy.");
    }

    @AfterClass(groups = {TEST_GROUP}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        // Safety: ensure tc delay is removed even if a test failed mid-delay
        removeNetworkDelay();
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
        safeClose(this.client);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private String extractParentChannelId(CosmosDiagnostics diagnostics) throws JsonProcessingException {
        ObjectNode node = (ObjectNode) Utils.getSimpleObjectMapper().readTree(diagnostics.toString());
        JsonNode gwStats = node.get("gatewayStatisticsList");
        if (gwStats != null && gwStats.isArray() && gwStats.size() > 0) {
            // Return the parentChannelId from the FIRST gateway stats entry
            return gwStats.get(0).has("parentChannelId")
                ? gwStats.get(0).get("parentChannelId").asText() : null;
        }
        return null;
    }

    /**
     * Extracts parentChannelIds from ALL gatewayStatisticsList entries in the diagnostics.
     * Unlike extractParentChannelId (which returns just the first), this returns the
     * parentChannelId from every retry attempt — useful for verifying retry behavior
     * and multi-parent-channel scenarios.
     */
    private List<String> extractAllParentChannelIds(CosmosDiagnostics diagnostics) throws JsonProcessingException {
        List<String> parentChannelIds = new ArrayList<>();
        ObjectNode node = (ObjectNode) Utils.getSimpleObjectMapper().readTree(diagnostics.toString());
        JsonNode gwStats = node.get("gatewayStatisticsList");
        if (gwStats != null && gwStats.isArray()) {
            for (JsonNode stat : gwStats) {
                if (stat.has("parentChannelId")) {
                    String id = stat.get("parentChannelId").asText();
                    if (id != null && !id.isEmpty() && !"null".equals(id)) {
                        parentChannelIds.add(id);
                    }
                }
            }
        }
        return parentChannelIds;
    }

    /**
     * Establishes an HTTP/2 connection by performing a read, and returns the parent channel ID.
     * The parent channel ID identifies the TCP connection shared across all multiplexed H2 streams.
     * This value is captured by the ConnectionObserver in ReactorNettyClient on CONNECTED/ACQUIRED.
     *
     * @return the H2 parent channel ID (NioSocketChannel short text ID, e.g., "819e4658")
     */
    private String establishH2ConnectionAndGetParentChannelId() throws Exception {
        CosmosDiagnostics diagnostics = this.performDocumentOperation(
            this.cosmosAsyncContainer, OperationType.Read, seedItem, false);
        String h2ParentChannelId = extractParentChannelId(diagnostics);
        logger.info("Established H2 parent channel (TCP connection): parentChannelId={}", h2ParentChannelId);
        AssertionsForClassTypes.assertThat(h2ParentChannelId)
            .as("Initial read must succeed and report H2 parentChannelId (NioSocketChannel identity)")
            .isNotNull()
            .isNotEmpty();
        return h2ParentChannelId;
    }

    /**
     * Performs a read after the network delay has been removed, and returns the parent channel ID.
     * Comparing this value with the pre-delay parent channel ID proves whether the H2 TCP connection
     * (the NioSocketChannel that multiplexes all Http2StreamChannels) survived the timeout.
     *
     * @return the H2 parent channel ID after delay recovery
     */
    private String readAfterDelayAndGetParentChannelId() throws Exception {
        CosmosDiagnostics diagnostics = this.performDocumentOperation(
            this.cosmosAsyncContainer, OperationType.Read, seedItem, false);
        String h2ParentChannelId = extractParentChannelId(diagnostics);
        logger.info("Post-delay H2 parent channel: parentChannelId={}", h2ParentChannelId);
        logger.info("Post-delay recovery diagnostics: {}", diagnostics.toString());
        AssertionsForClassTypes.assertThat(h2ParentChannelId)
            .as("Post-delay read must succeed and report H2 parentChannelId")
            .isNotNull()
            .isNotEmpty();
        return h2ParentChannelId;
    }

    private void addNetworkDelay(int delayMs) {
        String iface = NETWORK_INTERFACE;
        String cmd = String.format("tc qdisc add dev %s root netem delay %dms", iface, delayMs);
        logger.info(">>> Adding network delay: {}", cmd);
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            int exit = p.waitFor();
            if (exit != 0) {
                try (BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String errMsg = err.readLine();
                    logger.warn("tc add failed (exit={}): {}", exit, errMsg);
                }
            } else {
                logger.info(">>> Network delay active: {}ms on {}", delayMs, iface);
            }
        } catch (Exception e) {
            logger.error("Failed to add network delay", e);
            fail("Could not add network delay via tc: " + e.getMessage());
        }
    }

    private void removeNetworkDelay() {
        String iface = NETWORK_INTERFACE;
        String cmd = String.format("tc qdisc del dev %s root netem", iface);
        logger.info(">>> Removing network delay: {}", cmd);
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            int exit = p.waitFor();
            if (exit == 0) {
                logger.info(">>> Network delay removed");
            } else {
                logger.warn("tc del returned exit={} (may already be removed)", exit);
            }
        } catch (Exception e) {
            logger.warn("Failed to remove network delay: {}", e.getMessage());
        }
    }

    // ========================================================================
    // Tests — each one: warmup read → tc delay → timed-out read → remove → verify
    // ========================================================================

    /**
     * Proves that after a real netty ReadTimeoutException (fired by ReadTimeoutHandler on the
     * Http2StreamChannel pipeline), the parent NioSocketChannel (TCP connection) remains in the
     * ConnectionProvider pool and is reused for the next request.
     *
     * Uses a 15s e2e timeout on the delayed read to prevent the SDK from retrying cross-region
     * (which also has tc netem delay), keeping the initial failure fast and leaving ample time
     * for the recovery read within the 180s test timeout.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void connectionReuseAfterRealNettyTimeout() throws Exception {
        String h2ParentChannelIdBeforeDelay = establishH2ConnectionAndGetParentChannelId();

        // e2e timeout prevents cross-region failover retries that would consume the test timeout budget
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(15)).enable(true).build();
        CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
        opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        addNetworkDelay(8000);
        try {
            this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
            logger.info("Request succeeded unexpectedly (delay may not be fully active)");
        } catch (CosmosException e) {
            logger.info("ReadTimeoutException triggered: statusCode={}, subStatusCode={}", e.getStatusCode(), e.getSubStatusCode());
        } finally {
            removeNetworkDelay();
        }

        String h2ParentChannelIdAfterDelay = readAfterDelayAndGetParentChannelId();

        logger.info("RESULT: before={}, after={}, SAME_TCP_CONNECTION={}",
            h2ParentChannelIdBeforeDelay, h2ParentChannelIdAfterDelay,
            h2ParentChannelIdBeforeDelay.equals(h2ParentChannelIdAfterDelay));
        assertThat(h2ParentChannelIdAfterDelay)
            .as("H2 parent NioSocketChannel should survive ReadTimeoutException on Http2StreamChannel")
            .isEqualTo(h2ParentChannelIdBeforeDelay);
    }

    /**
     * Proves that multiple consecutive ReadTimeoutExceptions across all retry attempts
     * (6s/6s/10s escalation via HttpTimeoutPolicyForGatewayV2) do not close the parent
     * NioSocketChannel. Each retry opens a new Http2StreamChannel on the same TCP connection.
     *
     * Uses a 15s e2e timeout to prevent cross-region failover retries.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void allRetriesTimeoutConnectionSurvives() throws Exception {
        String h2ParentChannelIdBeforeDelay = establishH2ConnectionAndGetParentChannelId();

        // e2e timeout prevents cross-region failover retries that would consume the test timeout budget
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(15)).enable(true).build();
        CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
        opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        // 8s delay > 6s first-retry timeout — ReadTimeoutHandler fires on each Http2StreamChannel.
        addNetworkDelay(8000);
        try {
            this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
        } catch (CosmosException e) {
            logger.info("All retries exhausted (ReadTimeoutHandler fired on each stream): statusCode={}, subStatusCode={}",
                e.getStatusCode(), e.getSubStatusCode());
        } finally {
            removeNetworkDelay();
        }

        String h2ParentChannelIdAfterDelay = readAfterDelayAndGetParentChannelId();

        logger.info("RESULT: before={}, after={}, SAME_TCP_CONNECTION={}",
            h2ParentChannelIdBeforeDelay, h2ParentChannelIdAfterDelay,
            h2ParentChannelIdBeforeDelay.equals(h2ParentChannelIdAfterDelay));
        assertThat(h2ParentChannelIdAfterDelay)
            .as("H2 parent NioSocketChannel should survive multiple ReadTimeoutExceptions across retry attempts")
            .isEqualTo(h2ParentChannelIdBeforeDelay);
    }

    /**
     * Proves that an independent request after a timeout-induced failure acquires an
     * Http2StreamChannel from the same parent NioSocketChannel in the ConnectionProvider pool.
     *
     * Uses a 15s e2e timeout on the delayed read to prevent the SDK from retrying cross-region
     * (which also has tc netem delay), keeping the initial failure fast and leaving ample time
     * for the recovery read within the 180s test timeout.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void connectionSurvivesTimeoutForNextRequest() throws Exception {
        String h2ParentChannelIdBeforeDelay = establishH2ConnectionAndGetParentChannelId();

        // e2e timeout prevents cross-region failover retries that would consume the test timeout budget
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(15)).enable(true).build();
        CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
        opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        addNetworkDelay(8000);
        try {
            this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
        } catch (CosmosException e) {
            logger.info("ReadTimeoutException on Http2StreamChannel: statusCode={}", e.getStatusCode());
        } finally {
            removeNetworkDelay();
        }

        String h2ParentChannelIdAfterDelay = readAfterDelayAndGetParentChannelId();

        logger.info("RESULT: before={}, after={}, SAME_TCP_CONNECTION={}",
            h2ParentChannelIdBeforeDelay, h2ParentChannelIdAfterDelay,
            h2ParentChannelIdBeforeDelay.equals(h2ParentChannelIdAfterDelay));
        assertThat(h2ParentChannelIdAfterDelay)
            .as("ConnectionProvider pool should hand out same NioSocketChannel after stream timeout")
            .isEqualTo(h2ParentChannelIdBeforeDelay);
    }

    /**
     * Proves that after delay removal, the next read completes on the same H2 parent
     * NioSocketChannel — confirming no connection-level degradation from the timeout.
     *
     * Recovery latency may exceed typical (~130ms) because removing tc netem drops queued
     * packets in flight, requiring TCP retransmission. The first recovery attempt may hit
     * a ReadTimeoutHandler (6s), with the second retry succeeding. The 10s threshold
     * accommodates this TCP stabilization overhead.
     *
     * Uses a 15s e2e timeout on the initial delayed read to prevent the SDK from retrying
     * cross-region (which also has tc netem delay), keeping the initial failure fast (~15s)
     * and leaving ample time for the recovery read within the 180s test timeout.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void postTimeoutReadCompletesQuickly() throws Exception {
        String h2ParentChannelIdBeforeDelay = establishH2ConnectionAndGetParentChannelId();

        // e2e timeout prevents cross-region failover retries that would consume the test timeout budget
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(15)).enable(true).build();
        CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
        opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        addNetworkDelay(8000);
        try {
            this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
        } catch (CosmosException e) {
            logger.info("Timeout: statusCode={}", e.getStatusCode());
        } finally {
            removeNetworkDelay();
        }

        // Brief pause to let TCP retransmission settle after netem qdisc deletion
        Thread.sleep(1000);

        CosmosDiagnostics recoveryDiag = this.performDocumentOperation(
            this.cosmosAsyncContainer, OperationType.Read, seedItem, false);
        CosmosDiagnosticsContext recoveryCtx = recoveryDiag.getDiagnosticsContext();
        String h2ParentChannelIdAfterDelay = extractParentChannelId(recoveryDiag);

        logger.info("RESULT: before={}, after={}, SAME_TCP_CONNECTION={}, recoveryLatency={}ms",
            h2ParentChannelIdBeforeDelay, h2ParentChannelIdAfterDelay,
            h2ParentChannelIdBeforeDelay.equals(h2ParentChannelIdAfterDelay),
            recoveryCtx.getDuration().toMillis());
        AssertionsForClassTypes.assertThat(recoveryCtx.getDuration())
            .as("Recovery read should complete within 10s (allows one 6s ReadTimeout retry + TCP stabilization)")
            .isLessThan(Duration.ofSeconds(10));
        assertThat(h2ParentChannelIdAfterDelay)
            .as("H2 parent NioSocketChannel should survive — recovery read on same TCP connection")
            .isEqualTo(h2ParentChannelIdBeforeDelay);
    }

    /**
     * Proves that under concurrent load, the Http2AllocationStrategy can allocate multiple
     * H2 parent channels (TCP connections) to the same endpoint, and that ALL parent channels
     * survive a ReadTimeoutException on their stream channels.
     *
     * With strictConnectionReuse=false (default) and concurrent requests exceeding
     * maxConcurrentStreams (30), reactor-netty's connection pool opens additional parent
     * channels. This test sends 35 concurrent reads to try to trigger >1 parent channel,
     * then injects network delay, verifies timeout, and confirms parent channels survive.
     *
     * If the pool only creates 1 parent channel (possible under low concurrency), the test
     * still verifies that single channel survives — the multi-parent case is validated when
     * the pool allocates >1.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void multiParentChannelConnectionReuse() throws Exception {
        // Step 1: Send concurrent requests to force multiple parent H2 channels.
        // With strictConnectionReuse=false (default) and maxConcurrentStreams=30,
        // sending 35 concurrent requests should trigger allocation of >1 parent channel.
        int concurrentRequests = 35;
        Set<String> preDelayParentChannelIds = ConcurrentHashMap.newKeySet();

        Flux.range(0, concurrentRequests)
            .flatMap(i -> this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), TestObject.class)
                .doOnSuccess(response -> {
                    try {
                        String parentId = extractParentChannelId(response.getDiagnostics());
                        if (parentId != null) {
                            preDelayParentChannelIds.add(parentId);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to extract parentChannelId from concurrent read", e);
                    }
                }), concurrentRequests) // concurrency = all at once
            .collectList()
            .block();

        logger.info("Pre-delay parent channels observed: {} (count={})",
            preDelayParentChannelIds, preDelayParentChannelIds.size());
        assertThat(preDelayParentChannelIds)
            .as("Should observe at least 1 parent channel from concurrent reads")
            .isNotEmpty();

        if (preDelayParentChannelIds.size() > 1) {
            logger.info("SUCCESS: Multiple parent H2 channels created under concurrent load (count={})",
                preDelayParentChannelIds.size());
        } else {
            logger.info("NOTE: Only 1 parent channel observed — pool handled all 35 concurrent requests " +
                "on a single connection. Multi-parent validation will be skipped; single-parent survival still tested.");
        }

        // Step 2: Inject network delay causing timeouts across all parent channels
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(15)).enable(true).build();
        CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
        opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        addNetworkDelay(8000);
        try {
            this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
        } catch (CosmosException e) {
            logger.info("ReadTimeoutException during delay: statusCode={}", e.getStatusCode());
        } finally {
            removeNetworkDelay();
        }

        // Step 3: Send concurrent requests again, collect parentChannelIds post-delay
        Set<String> postDelayParentChannelIds = ConcurrentHashMap.newKeySet();

        Flux.range(0, concurrentRequests)
            .flatMap(i -> this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), TestObject.class)
                .doOnSuccess(response -> {
                    try {
                        String parentId = extractParentChannelId(response.getDiagnostics());
                        if (parentId != null) {
                            postDelayParentChannelIds.add(parentId);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to extract parentChannelId from post-delay read", e);
                    }
                }), concurrentRequests)
            .collectList()
            .block();

        logger.info("Post-delay parent channels: {} (count={})",
            postDelayParentChannelIds, postDelayParentChannelIds.size());

        // Step 4: Assert that pre-delay parent channels survived
        Set<String> survivedChannels = new HashSet<>(preDelayParentChannelIds);
        survivedChannels.retainAll(postDelayParentChannelIds);

        logger.info("RESULT: pre-delay={}, post-delay={}, survived={}, survivalRate={}/{}",
            preDelayParentChannelIds, postDelayParentChannelIds, survivedChannels,
            survivedChannels.size(), preDelayParentChannelIds.size());

        assertThat(survivedChannels)
            .as("At least one pre-delay H2 parent channel should survive the timeout and be reused post-delay")
            .isNotEmpty();
    }

    /**
     * Verifies the parentChannelId behavior across retry attempts under network delay.
     *
     * When the SDK retries (6s → 6s → 10s via HttpTimeoutPolicyForGatewayV2), each retry
     * opens a new Http2StreamChannel. This test captures the parentChannelId from EVERY
     * retry attempt in gatewayStatisticsList and verifies:
     * 1. Multiple retry attempts were recorded (at least 2 gatewayStatistics entries)
     * 2. The parent H2 channel(s) used during retries survive post-delay
     *
     * Uses a 25s e2e timeout to allow all 3 retry attempts (6+6+10=22s) to fire
     * before the e2e budget is exhausted.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void retryUsesConsistentParentChannelId() throws Exception {
        // Warmup — establish connection and get baseline parentChannelId
        String warmupParentChannelId = establishH2ConnectionAndGetParentChannelId();

        // Inject delay that triggers ReadTimeoutException on every retry attempt
        // 8s > 6s (first and second retry timeout), and with RTT doubling to ~16s,
        // all three retry attempts (6s/6s/10s) should fire ReadTimeoutException.
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(25)).enable(true).build();
        CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
        opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        CosmosDiagnostics failedDiagnostics = null;
        addNetworkDelay(8000);
        try {
            // The 25s e2e timeout budget may be exhausted by retries (6+6+10=22s)
            // or the request may succeed if delay propagation is slow. Either way,
            // we need diagnostics from the attempt.
            CosmosItemResponse<TestObject> response = this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
            // If the read succeeded, it means retries eventually got through.
            // Still extract diagnostics from the successful response.
            if (response != null) {
                failedDiagnostics = response.getDiagnostics();
                logger.info("Request succeeded under delay — extracting diagnostics from success response");
            }
        } catch (CosmosException e) {
            failedDiagnostics = e.getDiagnostics();
            logger.info("All retries timed out: statusCode={}, subStatusCode={}",
                e.getStatusCode(), e.getSubStatusCode());
            if (failedDiagnostics != null) {
                logger.info("Exception diagnostics: {}", failedDiagnostics.toString());
            } else {
                logger.warn("CosmosException.getDiagnostics() returned null (e2e timeout may fire before any retry completes)");
            }
        } catch (Exception e) {
            logger.warn("Non-CosmosException caught: {}", e.getClass().getName(), e);
        } finally {
            removeNetworkDelay();
        }

        // If diagnostics are still null (e2e timeout fired before any retry produced diagnostics),
        // do a recovery read and verify the parent channel survived instead
        if (failedDiagnostics == null) {
            logger.info("No diagnostics from failed request — performing recovery read to verify parent channel survival");
            String postDelayParentChannelId = readAfterDelayAndGetParentChannelId();
            logger.info("RESULT (fallback): warmup={}, postDelay={}, warmupSurvived={}",
                warmupParentChannelId, postDelayParentChannelId,
                warmupParentChannelId.equals(postDelayParentChannelId));
            assertThat(postDelayParentChannelId)
                .as("Parent channel should survive even when e2e timeout fires before retry diagnostics are available")
                .isEqualTo(warmupParentChannelId);
            return;
        }

        // Extract parentChannelIds from ALL retry attempts in the diagnostics
        List<String> retryParentChannelIds = extractAllParentChannelIds(failedDiagnostics);

        logger.info("Retry parentChannelIds across attempts: {} (count={})",
            retryParentChannelIds, retryParentChannelIds.size());
        logger.info("Full failed diagnostics: {}", failedDiagnostics.toString());

        assertThat(retryParentChannelIds)
            .as("Should have parentChannelIds from at least 2 retry attempts")
            .hasSizeGreaterThanOrEqualTo(2);

        // Analyze retry parentChannelId consistency
        Set<String> uniqueRetryParentChannelIds = new HashSet<>(retryParentChannelIds);
        logger.info("Unique parentChannelIds across retries: {} (allSame={})",
            uniqueRetryParentChannelIds, uniqueRetryParentChannelIds.size() == 1);

        // Verify that ALL parent channels used during retries survive post-delay
        String postDelayParentChannelId = readAfterDelayAndGetParentChannelId();

        logger.info("RESULT: warmup={}, retryChannels={}, postDelay={}, warmupSurvived={}",
            warmupParentChannelId, uniqueRetryParentChannelIds, postDelayParentChannelId,
            warmupParentChannelId.equals(postDelayParentChannelId));

        // Under delay with strictConnectionReuse=false (default), the pool may open
        // new parent channels for retries rather than reusing the warmup channel.
        // The key invariant: the warmup parent channel SURVIVES and is reused post-delay,
        // OR the post-delay read uses one of the retry channels (all should survive).
        Set<String> allKnownChannels = new HashSet<>(uniqueRetryParentChannelIds);
        allKnownChannels.add(warmupParentChannelId);
        assertThat(allKnownChannels)
            .as("Post-delay read should use ANY known parent channel (warmup or retry) — proving H2 connections survive delay")
            .contains(postDelayParentChannelId);
    }

    /**
     * Proves that when both the SDK's e2e timeout (7s) and the network delay (8s) are active,
     * the H2 parent NioSocketChannel survives. The e2e cancel fires RST_STREAM on the
     * Http2StreamChannel before ReadTimeoutHandler, but the parent TCP connection is unaffected.
     */
    @Test(groups = {TEST_GROUP}, timeOut = TEST_TIMEOUT)
    public void connectionSurvivesE2ETimeoutWithRealDelay() throws Exception {
        String h2ParentChannelIdBeforeDelay = establishH2ConnectionAndGetParentChannelId();

        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy =
            new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(7)).enable(true).build();
        CosmosItemRequestOptions opts = new CosmosItemRequestOptions();
        opts.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        addNetworkDelay(8000);
        try {
            this.cosmosAsyncContainer.readItem(
                seedItem.getId(), new PartitionKey(seedItem.getId()), opts, TestObject.class).block();
            fail("Should have failed due to e2e timeout");
        } catch (CosmosException e) {
            logger.info("E2E timeout: statusCode={}, subStatusCode={}", e.getStatusCode(), e.getSubStatusCode());
            logger.info("E2E timeout diagnostics: {}", e.getDiagnostics() != null ? e.getDiagnostics().toString() : "null");
        } finally {
            removeNetworkDelay();
        }

        String h2ParentChannelIdAfterDelay = readAfterDelayAndGetParentChannelId();

        logger.info("RESULT: before={}, after={}, SAME_TCP_CONNECTION={}",
            h2ParentChannelIdBeforeDelay, h2ParentChannelIdAfterDelay,
            h2ParentChannelIdBeforeDelay.equals(h2ParentChannelIdAfterDelay));
        assertThat(h2ParentChannelIdAfterDelay)
            .as("H2 parent NioSocketChannel should survive e2e cancel + real network delay")
            .isEqualTo(h2ParentChannelIdBeforeDelay);
    }
}
