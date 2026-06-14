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

public class EndpointProbeClientTests {

    private static final URI REGION_EAST = URI.create("https://probe-east.example.com:10250");
    private static final URI REGION_WEST = URI.create("https://probe-west.example.com:10250");

    @BeforeMethod(groups = { "unit" })
    public void resetSystemProperties() {
        System.clearProperty("COSMOS.THINCLIENT_PROBE_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_RECOVERY_THRESHOLD");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_PATH");
    }

    @AfterMethod(groups = { "unit" })
    public void clearSystemProperties() {
        System.clearProperty("COSMOS.THINCLIENT_PROBE_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_RECOVERY_THRESHOLD");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_PATH");
    }

    @Test(groups = { "unit" })
    public void allGreen_cycleIsGreen_andHealthStaysTrue() {
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
        assertThat(sendCount.get()).isEqualTo(2);
        EndpointProbeDiagnosticsSnapshot snap = probeClient.getDiagnosticsSnapshot();
        assertThat(snap.getLastCycleSuccess()).isEqualTo(Boolean.TRUE);
        assertThat(snap.getLastStateUpdatedAt()).isNotNull();
        assertThat(snap.getLastStateUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test(groups = { "unit" })
    public void any503_failsTheCycle_andHysteresisDelaysFlip() {
        // Threshold = 2: one RED cycle should NOT flip; two RED cycles SHOULD flip.
        System.setProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD", "2");

        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 503);
        HttpClient client = mockClient(statusByEndpoint, new AtomicInteger(), false);

        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        // Cycle 1: RED but below threshold — gate stays HEALTHY.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(probeClient.isProxyHealthy()).isTrue();
        assertThat(probeClient.getDiagnosticsSnapshot().getLastCycleSuccess()).isEqualTo(Boolean.FALSE);

        // Cycle 2: RED at threshold -> flip.
        assertThat(probeClient.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isFalse();
        assertThat(probeClient.isProxyHealthy()).isFalse();
        assertThat(probeClient.getDiagnosticsSnapshot().getLastCycleSuccess()).isEqualTo(Boolean.FALSE);
    }

    @Test(groups = { "unit" })
    public void singleGreenCycleRestoresHealthAndResetsCounter() {
        System.setProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD", "1");

        Map<URI, Integer> redByEndpoint = new HashMap<>();
        redByEndpoint.put(REGION_EAST, 503);
        EndpointProbeClient probeClient = new EndpointProbeClient(mockClient(redByEndpoint, new AtomicInteger(), false));

        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(probeClient.isProxyHealthy()).isFalse();

        // Toggling client returns red on first call and green on subsequent calls; drive the
        // probe client to RED on cycle 1 then GREEN on cycle 2 and assert hysteresis recovery.
        EndpointProbeClient combo = new EndpointProbeClient(toggleClient(REGION_EAST, 503, 200));

        assertThat(combo.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(combo.isProxyHealthy()).isFalse();

        assertThat(combo.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(combo.isProxyHealthy()).isTrue();
        assertThat(combo.getDiagnosticsSnapshot().getLastCycleSuccess()).isEqualTo(Boolean.TRUE);
    }

    @Test(groups = { "unit" })
    public void transportErrorIsRed() {
        System.setProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD", "1");

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
    public void featureFlagOff_isNoOp() {
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
    public void emptyOrNullEndpointSet_isNoOp() {
        HttpClient client = Mockito.mock(HttpClient.class);
        EndpointProbeClient probeClient = new EndpointProbeClient(client);

        assertThat(probeClient.runProbeCycle(null).block()).isTrue();
        assertThat(probeClient.runProbeCycle(Collections.emptyList()).block()).isTrue();
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class), any(Duration.class));
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class));
    }

    @Test(groups = { "unit" })
    public void wrongPath400_isRed() {
        System.setProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD", "1");
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 400);
        EndpointProbeClient probeClient = new EndpointProbeClient(mockClient(statusByEndpoint, new AtomicInteger(), false));
        assertThat(probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
    }

    @Test(groups = { "unit" })
    public void probeRequestTargetsConfiguredPath() {
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        AtomicInteger sendCount = new AtomicInteger(0);
        HttpClient client = mockClient(statusByEndpoint, sendCount, true);

        EndpointProbeClient probeClient = new EndpointProbeClient(client);
        probeClient.runProbeCycle(Collections.singletonList(REGION_EAST)).block();

        assertThat(sendCount.get()).isEqualTo(1);
    }

    @Test(groups = { "unit" })
    public void recoveryThresholdRequiresMultipleGreenCycles() {
        // Operator opts into more conservative recovery: require two consecutive GREEN cycles
        // before flipping back to healthy. With default failureThreshold=1 the probe client
        // becomes UNHEALTHY after one RED. A single GREEN must NOT restore traffic.
        System.setProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD", "1");
        System.setProperty("COSMOS.THINCLIENT_PROBE_RECOVERY_THRESHOLD", "2");

        // Sequenced client returns RED, GREEN, GREEN across successive probe calls
        // against the single regional endpoint.
        HttpClient sequencedClient = sequencedClient(REGION_EAST, 503, 200, 200);
        EndpointProbeClient e = new EndpointProbeClient(sequencedClient);

        // RED #1 — hits failure threshold of 1, gate flips UNHEALTHY.
        e.runProbeCycle(Collections.singletonList(REGION_EAST)).block();
        assertThat(e.isProxyHealthy()).isFalse();

        // GREEN #1 — under recovery threshold of 2, gate STAYS UNHEALTHY.
        e.runProbeCycle(Collections.singletonList(REGION_EAST)).block();
        assertThat(e.isProxyHealthy()).isFalse();

        // GREEN #2 — second consecutive GREEN restores healthy.
        e.runProbeCycle(Collections.singletonList(REGION_EAST)).block();
        assertThat(e.isProxyHealthy()).isTrue();
    }

    @Test(groups = { "unit" })
    public void forceUnhealthy_flipsGateToRedWithoutRunningProbe() {
        // Safeguard path used by GlobalEndpointManager when account topology says thin-client
        // is eligible but LocationCache cannot resolve a single thin-client regional endpoint.
        // Without a fan-out, this method must still flip the gate so the optimistic-startup
        // default does not pin traffic to an unreachable thin-client store model.
        Map<URI, Integer> greenByEndpoint = new HashMap<>();
        greenByEndpoint.put(REGION_EAST, 200);
        EndpointProbeClient probeClient = new EndpointProbeClient(mockClient(greenByEndpoint, new AtomicInteger(), false));
        assertThat(probeClient.isProxyHealthy()).isTrue();

        probeClient.forceUnhealthy("test: endpoint resolution mismatch");
        assertThat(probeClient.isProxyHealthy()).isFalse();
        assertThat(probeClient.getDiagnosticsSnapshot().getLastCycleSuccess()).isEqualTo(Boolean.FALSE);
        assertThat(probeClient.getDiagnosticsSnapshot().getLastStateUpdatedAt()).isNotNull();
    }

    @Test(groups = { "unit" })
    public void forceUnhealthy_onClosedProbeClient_isNoOp() {
        Map<URI, Integer> greenByEndpoint = new HashMap<>();
        greenByEndpoint.put(REGION_EAST, 200);
        EndpointProbeClient probeClient = new EndpointProbeClient(mockClient(greenByEndpoint, new AtomicInteger(), false));
        probeClient.close();

        // Closed probe clients must not mutate any state — otherwise diagnostics from a
        // shutting-down client would show spurious failures.
        probeClient.forceUnhealthy("test");
        // Snapshot should remain at its pre-close state (no cycle ever ran).
        assertThat(probeClient.getDiagnosticsSnapshot().getLastCycleSuccess()).isNull();
    }

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
