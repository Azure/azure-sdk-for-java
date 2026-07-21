// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.buffer.ByteBuf;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.ConnectException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

/**
 * Unit tests for the v2 per-region, one-shot probe-and-cache {@link EndpointProbeClient}.
 *
 * <p>The legacy global circuit-breaker (failure/recovery hysteresis thresholds) has been
 * replaced by a model where:
 * <ul>
 *   <li>each region is probed only until it records a success, then cached in an <em>add-only</em>
 *       proven set forever (delta probing); a proven region is never re-probed — not even if it
 *       vanishes from the topology and later re-appears (matching .NET's permanent-cache
 *       semantics);</li>
 *   <li>a region that fails is left un-cached and naturally re-probed on the next refresh
 *       (across-refresh re-probing is the only retry mechanism; each region is attempted exactly
 *       once per cycle);</li>
 *   <li>the routing gate ({@link EndpointProbeClient#isThinClientRoutable()}) is conservative and is
 *       evaluated against the <em>current topology snapshot</em>: it is {@code true} only when that
 *       snapshot is non-empty and every region in it is present in the add-only proven set. A proven
 *       region that has since vanished is not in the snapshot, so it neither helps nor blocks the
 *       gate.</li>
 * </ul>
 */
public class EndpointProbeClientTests {

    private static final URI REGION_EAST = URI.create("https://probe-east.example.com:10250");
    private static final URI REGION_WEST = URI.create("https://probe-west.example.com:10250");
    private static final URI REGION_CENTRAL = URI.create("https://probe-central.example.com:10250");

    @Test(groups = { "unit" })
    public void allGreen_allKnownRegionsProven_gateHealthy() {
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 200);
        AtomicInteger sendCount = new AtomicInteger(0);
        HttpClient client = mockClient(statusByEndpoint, sendCount, false);

        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        Boolean healthy = probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block();

        assertThat(healthy).isTrue();
        assertThat(probeClient.isThinClientRoutable()).isTrue();
        // Each proven region is probed exactly once.
        assertThat(sendCount.get()).isEqualTo(2);
    }

    @Test(groups = { "unit" })
    public void partialFailure_gateStaysUnhealthy_andFailedRegionIsUncached() {
        // A single non-200 fails the region for the cycle.
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 503);
        HttpClient client = mockClient(statusByEndpoint, new AtomicInteger(), false);

        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        // East proven, West failed -> not every known region succeeded -> gate UNHEALTHY.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isFalse();
        assertThat(probeClient.isThinClientRoutable()).isFalse();
    }

    @Test(groups = { "unit" })
    public void failedRegion_isReprobedNextRefresh_thenGateHealthy() {
        // The first RED attempt fails the region; across-refresh re-probing then re-probes the
        // still-uncached region on the next cycle.
        // Toggling client returns RED (503) on the first send then GREEN (200) thereafter.
        EndpointProbeClient probeClient = new EndpointProbeClient(toggleClient(REGION_EAST, 503, 200));

        // Cycle 1: RED -> uncached -> gate UNHEALTHY.
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(probeClient.isThinClientRoutable()).isFalse();

        // Cycle 2: region still in the delta -> re-probed -> GREEN -> cached -> gate HEALTHY.
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(probeClient.isThinClientRoutable()).isTrue();
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
        assertThat(probeClient.isThinClientRoutable()).isTrue();
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
        assertThat(probeClient.isThinClientRoutable()).isTrue();

        // Cycle 2 grows the topology to {East, West}: only the new region (West) is probed.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(sendCount.get()).isEqualTo(2);
        assertThat(probeClient.isThinClientRoutable()).isTrue();
    }

    @Test(groups = { "unit" })
    public void transportErrorIsRed() {
        HttpClient client = Mockito.mock(HttpClient.class);
        Mockito.doAnswer(inv -> Mono.error(new ConnectException("refused")))
            .when(client).send(any(HttpRequest.class), any(Duration.class));
        Mockito.doAnswer(inv -> Mono.error(new ConnectException("refused")))
            .when(client).send(any(HttpRequest.class));

        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(probeClient.isThinClientRoutable()).isFalse();
    }

    @Test(groups = { "unit" })
    public void emptyOrNullEndpointSet_isNoOp_andGateStaysConservative() {
        HttpClient client = Mockito.mock(HttpClient.class);
        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        // No topology observed yet -> conservative gate is UNHEALTHY and no probe traffic fires.
        assertThat(probeClient.runProbeCycle(null).block()).isFalse();
        assertThat(probeClient.runProbeCycle(Collections.emptyList()).block()).isFalse();
        assertThat(probeClient.isThinClientRoutable()).isFalse();
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class), any(Duration.class));
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class));
    }

    @Test(groups = { "unit" })
    public void wrongPath400_isRed() {
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 400);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(statusByEndpoint, new AtomicInteger(), false));
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(probeClient.isThinClientRoutable()).isFalse();
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
    public void emptyEndpoints_flipHealthyGateToRed() {
        // GlobalEndpointManager passes an empty endpoint set when account topology says thin-client
        // is eligible but LocationCache cannot resolve a single thin-client regional endpoint.
        // Prove a healthy gate first, then assert an empty cycle flips it to UNHEALTHY without probing.
        Map<URI, Integer> greenByEndpoint = new HashMap<>();
        greenByEndpoint.put(REGION_EAST, 200);
        AtomicInteger sendCount = new AtomicInteger();
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(greenByEndpoint, sendCount, false));

        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(probeClient.isThinClientRoutable()).isTrue();

        // Regions vanished -> gate goes RED, and no additional probe traffic is issued.
        assertThat(probeClient.runProbeCycle(Collections.emptyList()).block()).isFalse();
        assertThat(probeClient.isThinClientRoutable()).isFalse();
        assertThat(sendCount.get()).isEqualTo(1);
    }

    @Test(groups = { "unit" })
    public void emptyEndpoints_recoverOnNextNonEmptyCycle() {
        Map<URI, Integer> greenByEndpoint = new HashMap<>();
        greenByEndpoint.put(REGION_EAST, 200);
        AtomicInteger sendCount = new AtomicInteger();
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(greenByEndpoint, sendCount, false));

        // Prove healthy, then collapse the topology to empty -> gate RED.
        probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block();
        assertThat(sendCount.get()).isEqualTo(1);
        assertThat(probeClient.runProbeCycle(Collections.emptyList()).block()).isFalse();
        assertThat(probeClient.isThinClientRoutable()).isFalse();

        // A subsequent non-empty cycle restores the gate; the already-cached region keeps it
        // healthy WITHOUT re-probing (add-only cache: the proven entry survived the empty cycle).
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(probeClient.isThinClientRoutable()).isTrue();
        assertThat(sendCount.get()).isEqualTo(1);
    }

    @Test(groups = { "unit" })
    public void vanishedProvenRegion_reAppears_isNotReprobed() {
        // Core add-only semantic: a region proven healthy stays proven for the client's lifetime.
        // When it vanishes from the topology and later re-appears it must be treated as already
        // proven (skipped by the delta filter), so NO additional probe traffic is issued and the
        // gate is healthy immediately on re-appearance.
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 200);
        AtomicInteger sendCount = new AtomicInteger(0);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(statusByEndpoint, sendCount, false));

        // Cycle 1: prove both regions.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(sendCount.get()).isEqualTo(2);

        // Cycle 2: WEST vanishes -> topology is {EAST}. Gate stays healthy (EAST still proven),
        // and no probe fires (EAST already proven).
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(probeClient.isThinClientRoutable()).isTrue();
        assertThat(sendCount.get()).isEqualTo(2);

        // Cycle 3: WEST re-appears -> topology is {EAST, WEST}. WEST is already in the add-only
        // proven set, so it is NOT re-probed and the gate is healthy immediately.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(probeClient.isThinClientRoutable()).isTrue();
        assertThat(sendCount.get()).isEqualTo(2);
    }

    @Test(groups = { "unit" })
    public void vanishedProvenRegion_doesNotGateCurrentTopology() {
        // A retained proven entry for a vanished region must not falsely satisfy the gate for a
        // different, not-yet-proven region that is now current. The gate is evaluated via
        // containsAll(currentSnapshot), so only the CURRENT regions matter.
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 503); // WEST never succeeds
        AtomicInteger sendCount = new AtomicInteger(0);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(statusByEndpoint, sendCount, false));

        // Cycle 1: topology is {EAST} -> proven -> gate healthy.
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(probeClient.isThinClientRoutable()).isTrue();
        assertThat(sendCount.get()).isEqualTo(1);

        // Cycle 2: EAST vanishes and WEST (unhealthy) appears -> topology is {WEST}. EAST's retained
        // proven entry must NOT make the gate healthy; WEST is current and unproven -> gate RED.
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_WEST)).block()).isFalse();
        assertThat(probeClient.isThinClientRoutable()).isFalse();
        assertThat(sendCount.get()).isEqualTo(2); // WEST was probed (delta), EAST was not re-probed
    }

    @Test(groups = { "unit" })
    public void unsupportedNewRegion_blocksGate_removalRestoresGate() {
        // Scenario from the field: two supported regions are proven and the gate is healthy. A new
        // region C is then added whose endpoint does NOT support thin-client (probe never returns
        // 200). Because the gate is evaluated against the CURRENT topology snapshot via
        // containsAll(...), the presence of the unproven C flips the gate to UNHEALTHY even though
        // A and B remain proven. When C is later removed from the topology, the snapshot shrinks
        // back to {A, B} — both still proven (add-only) — so the gate becomes usable again without
        // any re-probing of A or B.
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);     // A: supported
        statusByEndpoint.put(REGION_WEST, 200);     // B: supported
        statusByEndpoint.put(REGION_CENTRAL, 503);  // C: no thin-client support
        AtomicInteger sendCount = new AtomicInteger(0);
        EndpointProbeClient probeClient =
            new EndpointProbeClient(mockClient(statusByEndpoint, sendCount, false));

        // Cycle 1: {A, B} both proven -> gate healthy.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(probeClient.isThinClientRoutable()).isTrue();
        assertThat(sendCount.get()).isEqualTo(2);

        // Cycle 2: unsupported C is added -> {A, B, C}. Only C is in the delta and it fails (503),
        // staying uncached. The current snapshot now includes C, which is not proven -> gate RED.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST, REGION_CENTRAL)).block())
            .isFalse();
        assertThat(probeClient.isThinClientRoutable()).isFalse();
        assertThat(sendCount.get()).isEqualTo(3); // only C probed; A/B already proven

        // Cycle 3: C is removed -> snapshot shrinks back to {A, B}. Both are still proven, so the
        // gate is usable again, and neither A nor B is re-probed.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(probeClient.isThinClientRoutable()).isTrue();
        assertThat(sendCount.get()).isEqualTo(3);
    }

    @Test(groups = { "unit" })
    public void runProbeCycle_overlappingCycle_skippedBySingleFlight() throws Exception {
        // Single-flight invariant: while one probe cycle holds cycleInProgress, a concurrently
        // triggered cycle must short-circuit — re-emitting the CURRENT gate WITHOUT issuing any
        // additional HTTP probe. Mirrors .NET RunProbeCycle_OverlappingCycle_SkippedBySingleFlight.
        AtomicInteger sendCount = new AtomicInteger(0);
        CountDownLatch probeStarted = new CountDownLatch(1); // cycle #1 has entered send()
        CountDownLatch releaseProbe = new CountDownLatch(1); // holds cycle #1 mid-flight

        HttpClient client = Mockito.mock(HttpClient.class);
        Mockito.doAnswer(inv -> {
            sendCount.incrementAndGet();
            probeStarted.countDown();
            return Mono.fromCallable(() -> {
                releaseProbe.await();
                return stubResponse(200);
            }).subscribeOn(Schedulers.boundedElastic());
        }).when(client).send(any(HttpRequest.class), any(Duration.class));

        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        // Cycle #1: fire asynchronously; it wins the single-flight CAS and blocks inside send().
        CompletableFuture<Boolean> cycleOne = probeClient
            .runProbeCycle(Collections.singletonList(REGION_EAST))
            .subscribeOn(Schedulers.boundedElastic())
            .toFuture();

        // Wait until cycle #1 is provably mid-flight (CAS held, one probe in flight).
        assertThat(probeStarted.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(sendCount.get()).isEqualTo(1);

        // Cycle #2: triggered while #1 is in flight -> loses the CAS -> returns the current gate
        // with NO additional probe. Gate is still UNHEALTHY (EAST not yet proven).
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(sendCount.get()).isEqualTo(1); // overlapping trigger issued no extra send

        // Release cycle #1: EAST is now proven -> gate HEALTHY, and exactly one probe was ever issued.
        releaseProbe.countDown();
        assertThat(cycleOne.get(5, TimeUnit.SECONDS)).isTrue();
        assertThat(probeClient.isThinClientRoutable()).isTrue();
        assertThat(sendCount.get()).isEqualTo(1);

        // Cycle #3 (after the CAS was released): a fresh topology with a new region (WEST) must win
        // the single-flight CAS and probe the delta again — proving the guard freed the flag when
        // cycle #1 completed rather than pinning the client to a single lifetime cycle.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(sendCount.get()).isEqualTo(2); // WEST (the delta) was probed
        assertThat(probeClient.isThinClientRoutable()).isTrue();
    }

    @Test(groups = { "unit" })
    public void runProbeCycle_closedMidCycle_resultDropped_gateStaysConservative() throws Exception {
        // If the client is closed while a cycle is in flight, applyCycleResult must DROP the result
        // (not mutate the add-only proven set), so the gate stays conservative (UNHEALTHY) even
        // though the in-flight probe ultimately returned 200.
        AtomicInteger sendCount = new AtomicInteger(0);
        CountDownLatch probeStarted = new CountDownLatch(1);
        CountDownLatch releaseProbe = new CountDownLatch(1);

        HttpClient client = Mockito.mock(HttpClient.class);
        Mockito.doAnswer(inv -> {
            sendCount.incrementAndGet();
            probeStarted.countDown();
            return Mono.fromCallable(() -> {
                releaseProbe.await();
                return stubResponse(200);
            }).subscribeOn(Schedulers.boundedElastic());
        }).when(client).send(any(HttpRequest.class), any(Duration.class));

        EndpointProbeClient probeClient = new EndpointProbeClient(client);
        CompletableFuture<Boolean> cycle = probeClient
            .runProbeCycle(Collections.singletonList(REGION_EAST))
            .subscribeOn(Schedulers.boundedElastic())
            .toFuture();

        assertThat(probeStarted.await(5, TimeUnit.SECONDS)).isTrue();

        // Close while the probe is mid-flight, then let it return 200.
        probeClient.close();
        releaseProbe.countDown();

        // The cycle completes but its successful result is dropped -> EAST never proven -> gate RED.
        cycle.get(5, TimeUnit.SECONDS);
        assertThat(probeClient.isThinClientRoutable()).isFalse();
    }

    // --- Mock helpers ---

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
