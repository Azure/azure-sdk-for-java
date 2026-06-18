// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.buffer.ByteBuf;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

/**
 * Unit tests for the v2 per-region, one-shot probe-and-cache {@link EndpointProbeClient}.
 *
 * <p>The legacy global circuit-breaker (failure/recovery hysteresis thresholds) has been
 * replaced by a model where:
 * <ul>
 *   <li>each region is probed only until it records a success, then cached forever (delta
 *       probing); a successful region is never re-probed;</li>
 *   <li>a region that fails every in-cycle attempt is left un-cached and naturally re-probed
 *       on the next refresh (across-refresh retry);</li>
 *   <li>within a cycle a region is retried up to {@code COSMOS.THINCLIENT_PROBE_MAX_RETRIES}
 *       times;</li>
 *   <li>the routing gate ({@link EndpointProbeClient#isProxyHealthy()}) is conservative: it is
 *       {@code false} until a non-empty topology is observed and every known region has a cached
 *       success.</li>
 * </ul>
 */
public class EndpointProbeClientTests {

    private static final URI REGION_EAST = URI.create("https://probe-east.example.com:10250");
    private static final URI REGION_WEST = URI.create("https://probe-west.example.com:10250");

    @BeforeMethod(groups = { "unit" })
    public void resetSystemProperties() {
        System.clearProperty("COSMOS.THINCLIENT_PROBE_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_MAX_RETRIES");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_PATH");
    }

    @AfterMethod(groups = { "unit" })
    public void clearSystemProperties() {
        System.clearProperty("COSMOS.THINCLIENT_PROBE_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_MAX_RETRIES");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_PATH");
    }

    @Test(groups = { "unit" })
    public void allGreen_allKnownRegionsProven_gateHealthy() {
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 200);
        AtomicInteger sendCount = new AtomicInteger(0);
        HttpClient client = mockClient(statusByEndpoint, sendCount, false);

        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        Instant before = Instant.now();
        Boolean healthy = probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block();

        assertThat(healthy).isTrue();
        assertThat(probeClient.isProxyHealthy()).isTrue();
        // Each proven region is probed exactly once (success short-circuits retries).
        assertThat(sendCount.get()).isEqualTo(2);
        EndpointProbeDiagnosticsSnapshot snap = probeClient.getDiagnosticsSnapshot();
        assertThat(snap.getLastCycleSuccess()).isEqualTo(Boolean.TRUE);
        assertThat(snap.getLastStateUpdatedAt()).isNotNull();
        assertThat(snap.getLastStateUpdatedAt()).isAfterOrEqualTo(before);
        assertThat(snap.getKnownRegionCount()).isEqualTo(2);
        assertThat(snap.getSucceededRegionCount()).isEqualTo(2);
    }

    @Test(groups = { "unit" })
    public void partialFailure_gateStaysUnhealthy_andFailedRegionIsUncached() {
        // No retries so a single non-200 fails the region for the cycle.
        System.setProperty("COSMOS.THINCLIENT_PROBE_MAX_RETRIES", "0");

        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 503);
        HttpClient client = mockClient(statusByEndpoint, new AtomicInteger(), false);

        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        // East proven, West failed -> not every known region succeeded -> gate UNHEALTHY.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isFalse();
        assertThat(probeClient.isProxyHealthy()).isFalse();

        EndpointProbeDiagnosticsSnapshot snap = probeClient.getDiagnosticsSnapshot();
        assertThat(snap.getLastCycleSuccess()).isEqualTo(Boolean.FALSE);
        assertThat(snap.getKnownRegionCount()).isEqualTo(2);
        assertThat(snap.getSucceededRegionCount()).isEqualTo(1);
    }

    @Test(groups = { "unit" })
    public void failedRegion_isReprobedNextRefresh_thenGateHealthy() {
        // No in-cycle retries so the first RED attempt fails the region; the across-refresh
        // retry then re-probes the still-uncached region on the next cycle.
        System.setProperty("COSMOS.THINCLIENT_PROBE_MAX_RETRIES", "0");

        // Toggling client returns RED (503) on the first send then GREEN (200) thereafter.
        EndpointProbeClient probeClient = new EndpointProbeClient(toggleClient(REGION_EAST, 503, 200));

        // Cycle 1: RED -> uncached -> gate UNHEALTHY.
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(probeClient.isProxyHealthy()).isFalse();

        // Cycle 2: region still in the delta -> re-probed -> GREEN -> cached -> gate HEALTHY.
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(probeClient.isProxyHealthy()).isTrue();
        assertThat(probeClient.getDiagnosticsSnapshot().getLastCycleSuccess()).isEqualTo(Boolean.TRUE);
    }

    @Test(groups = { "unit" })
    public void provenRegion_isNeverReprobed_emptyDeltaIssuesNoTraffic() {
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 200);
        AtomicInteger sendCount = new AtomicInteger(0);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(statusByEndpoint, sendCount, false));

        // Cycle 1 proves both regions.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(sendCount.get()).isEqualTo(2);

        // Cycle 2 over the same topology: delta is empty -> no HTTP traffic, gate stays healthy.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(sendCount.get()).isEqualTo(2);
        assertThat(probeClient.isProxyHealthy()).isTrue();
    }

    @Test(groups = { "unit" })
    public void newRegionInTopology_onlyDeltaIsProbed() {
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 200);
        AtomicInteger sendCount = new AtomicInteger(0);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(statusByEndpoint, sendCount, false));

        // Cycle 1 sees only East.
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(sendCount.get()).isEqualTo(1);
        assertThat(probeClient.isProxyHealthy()).isTrue();

        // Cycle 2 grows the topology to {East, West}: only the new region (West) is probed.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(sendCount.get()).isEqualTo(2);
        assertThat(probeClient.isProxyHealthy()).isTrue();
        assertThat(probeClient.getDiagnosticsSnapshot().getSucceededRegionCount()).isEqualTo(2);
    }

    @Test(groups = { "unit" })
    public void inCycleRetry_succeedsWithinMaxRetries() {
        // Default maxRetries (3) -> up to 4 attempts. Region is RED twice then GREEN.
        HttpClient sequencedClient = sequencedClient(REGION_EAST, 503, 503, 200);
        EndpointProbeClient probeClient = new EndpointProbeClient(sequencedClient);

        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(probeClient.isProxyHealthy()).isTrue();
    }

    @Test(groups = { "unit" })
    public void inCycleRetry_exhausted_marksRegionFailed() {
        // maxRetries=1 -> exactly 2 attempts; both RED -> region failed for the cycle.
        System.setProperty("COSMOS.THINCLIENT_PROBE_MAX_RETRIES", "1");

        Map<URI, Integer> redByEndpoint = new HashMap<>();
        redByEndpoint.put(REGION_EAST, 503);
        AtomicInteger sendCount = new AtomicInteger(0);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(redByEndpoint, sendCount, false));

        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(probeClient.isProxyHealthy()).isFalse();
        // 1 initial attempt + 1 retry.
        assertThat(sendCount.get()).isEqualTo(2);
    }

    @Test(groups = { "unit" })
    public void transportErrorIsRed() {
        System.setProperty("COSMOS.THINCLIENT_PROBE_MAX_RETRIES", "0");

        HttpClient client = Mockito.mock(HttpClient.class);
        Mockito.doAnswer(inv -> Mono.error(new ConnectException("refused")))
            .when(client).send(any(HttpRequest.class), any(Duration.class));
        Mockito.doAnswer(inv -> Mono.error(new ConnectException("refused")))
            .when(client).send(any(HttpRequest.class));

        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(probeClient.isProxyHealthy()).isFalse();
    }

    @Test(groups = { "unit" })
    public void featureFlagOff_isNoOp_andGateBypassesToHealthy() {
        System.setProperty("COSMOS.THINCLIENT_PROBE_ENABLED", "false");

        HttpClient client = Mockito.mock(HttpClient.class);
        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        Boolean healthy = probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block();
        assertThat(healthy).isTrue();
        assertThat(probeClient.isProxyHealthy()).isTrue();
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class), any(Duration.class));
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class));
    }

    @Test(groups = { "unit" })
    public void emptyOrNullEndpointSet_isNoOp_andGateStaysConservative() {
        HttpClient client = Mockito.mock(HttpClient.class);
        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        // No topology observed yet -> conservative gate is UNHEALTHY and no probe traffic fires.
        assertThat(probeClient.runProbeCycle(null).block()).isFalse();
        assertThat(probeClient.runProbeCycle(Collections.emptyList()).block()).isFalse();
        assertThat(probeClient.isProxyHealthy()).isFalse();
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class), any(Duration.class));
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class));
    }

    @Test(groups = { "unit" })
    public void wrongPath400_isRed() {
        System.setProperty("COSMOS.THINCLIENT_PROBE_MAX_RETRIES", "0");
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 400);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(statusByEndpoint, new AtomicInteger(), false));
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(probeClient.isProxyHealthy()).isFalse();
    }

    @Test(groups = { "unit" })
    public void probeRequestTargetsConfiguredPathAndMethod() {
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        AtomicInteger sendCount = new AtomicInteger(0);
        HttpClient client = mockClient(statusByEndpoint, sendCount, true);

        EndpointProbeClient probeClient = new EndpointProbeClient(client);
        probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block();

        assertThat(sendCount.get()).isEqualTo(1);
    }

    @Test(groups = { "unit" })
    public void forceUnhealthy_flipsHealthyGateToRedWithoutRunningProbe() {
        // Safeguard path used by GlobalEndpointManager when account topology says thin-client
        // is eligible but LocationCache cannot resolve a single thin-client regional endpoint.
        // Prove a healthy gate first, then assert the latch flips it to UNHEALTHY without a cycle.
        Map<URI, Integer> greenByEndpoint = new HashMap<>();
        greenByEndpoint.put(REGION_EAST, 200);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(greenByEndpoint, new AtomicInteger(), false));

        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(probeClient.isProxyHealthy()).isTrue();

        probeClient.forceUnhealthy("test: endpoint resolution mismatch");
        assertThat(probeClient.isProxyHealthy()).isFalse();
        assertThat(probeClient.getDiagnosticsSnapshot().getLastCycleSuccess()).isEqualTo(Boolean.FALSE);
        assertThat(probeClient.getDiagnosticsSnapshot().getLastStateUpdatedAt()).isNotNull();
    }

    @Test(groups = { "unit" })
    public void forceUnhealthy_latchIsClearedByNextValidCycle() {
        Map<URI, Integer> greenByEndpoint = new HashMap<>();
        greenByEndpoint.put(REGION_EAST, 200);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(greenByEndpoint, new AtomicInteger(), false));

        // Prove healthy, then force the latch.
        probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block();
        probeClient.forceUnhealthy("test: transient resolution mismatch");
        assertThat(probeClient.isProxyHealthy()).isFalse();

        // A subsequent valid non-empty cycle clears the latch; the already-cached region keeps the
        // gate healthy without re-probing.
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(probeClient.isProxyHealthy()).isTrue();
    }

    @Test(groups = { "unit" })
    public void forceUnhealthy_onClosedProbeClient_isNoOp() {
        Map<URI, Integer> greenByEndpoint = new HashMap<>();
        greenByEndpoint.put(REGION_EAST, 200);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(greenByEndpoint, new AtomicInteger(), false));
        probeClient.close();

        // Closed probe clients must not mutate any state — otherwise diagnostics from a
        // shutting-down client would show spurious failures.
        probeClient.forceUnhealthy("test");
        // Snapshot should remain at its pre-close state (no cycle ever ran).
        assertThat(probeClient.getDiagnosticsSnapshot().getLastCycleSuccess()).isNull();
    }

    // --- Mock helpers ---

    private static HttpClient sequencedClient(URI endpoint, int... statuses) {
        HttpClient client = Mockito.mock(HttpClient.class);
        AtomicInteger callCount = new AtomicInteger(0);
        Mockito.doAnswer(inv -> {
            int n = callCount.getAndIncrement();
            int status = statuses[Math.min(n, statuses.length - 1)];
            return Mono.just(stubResponse(status));
        }).when(client).send(any(HttpRequest.class), any(Duration.class));
        Mockito.doAnswer(inv -> {
            int n = callCount.getAndIncrement();
            int status = statuses[Math.min(n, statuses.length - 1)];
            return Mono.just(stubResponse(status));
        }).when(client).send(any(HttpRequest.class));
        return client;
    }

    private static HttpClient mockClient(
        Map<URI, Integer> statusByHost,
        AtomicInteger sendCount,
        boolean assertPathAndMethod) {

        HttpClient client = Mockito.mock(HttpClient.class);
        Mockito.doAnswer(inv -> {
            HttpRequest req = inv.getArgument(0);
            sendCount.incrementAndGet();
            if (assertPathAndMethod) {
                assertThat(req.httpMethod().name()).isEqualToIgnoringCase("POST");
                assertThat(req.uri().getPath()).isEqualTo("/connectivity-probe");
            }
            int status = lookupStatus(statusByHost, req.uri());
            return Mono.just(stubResponse(status));
        }).when(client).send(any(HttpRequest.class), any(Duration.class));

        Mockito.doAnswer(inv -> {
            HttpRequest req = inv.getArgument(0);
            sendCount.incrementAndGet();
            int status = lookupStatus(statusByHost, req.uri());
            return Mono.just(stubResponse(status));
        }).when(client).send(any(HttpRequest.class));
        return client;
    }

    private static HttpClient toggleClient(URI endpoint, int firstStatus, int subsequentStatus) {
        HttpClient client = Mockito.mock(HttpClient.class);
        AtomicInteger callCount = new AtomicInteger(0);
        Mockito.doAnswer(inv -> {
            int n = callCount.incrementAndGet();
            int status = n == 1 ? firstStatus : subsequentStatus;
            return Mono.just(stubResponse(status));
        }).when(client).send(any(HttpRequest.class), any(Duration.class));
        Mockito.doAnswer(inv -> {
            int n = callCount.incrementAndGet();
            int status = n == 1 ? firstStatus : subsequentStatus;
            return Mono.just(stubResponse(status));
        }).when(client).send(any(HttpRequest.class));
        return client;
    }

    private static int lookupStatus(Map<URI, Integer> statusByHost, URI requestUri) {
        for (Map.Entry<URI, Integer> e : statusByHost.entrySet()) {
            if (requestUri.getHost() != null && requestUri.getHost().equalsIgnoreCase(e.getKey().getHost())) {
                return e.getValue();
            }
        }
        return 500;
    }

    private static HttpResponse stubResponse(int status) {
        // Use Mono.empty() so the production body-drain path is not exercised with a singleton
        // ByteBuf whose refCnt would underflow across multiple probe calls and silently throw
        // IllegalReferenceCountException into the swallowed error handler. This mirrors real
        // ReactorNettyHttpResponse.body() behavior on an empty HTTP/2 response.
        return new HttpResponse() {
            @Override public int statusCode() { return status; }
            @Override public String headerValue(String name) { return null; }
            @Override public HttpHeaders headers() { return new HttpHeaders(); }
            @Override public Mono<ByteBuf> body() { return Mono.empty(); }
            @Override public Mono<String> bodyAsString() { return Mono.just(""); }
            @Override public void close() { }
        };
    }
}
