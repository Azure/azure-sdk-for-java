// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

public class EndpointOrchestratorTests {

    private static final URI REGION_EAST = URI.create("https://probe-east.example.com:10250");
    private static final URI REGION_WEST = URI.create("https://probe-west.example.com:10250");

    @BeforeMethod(groups = { "unit" })
    public void resetSystemProperties() {
        System.clearProperty("COSMOS.THINCLIENT_PROBE_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_PATH");
    }

    @AfterMethod(groups = { "unit" })
    public void clearSystemProperties() {
        System.clearProperty("COSMOS.THINCLIENT_PROBE_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_PATH");
    }

    @Test(groups = { "unit" })
    public void allGreen_cycleIsGreen_andHealthStaysTrue() {
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 200);
        AtomicInteger sendCount = new AtomicInteger(0);
        HttpClient client = mockClient(statusByEndpoint, sendCount, false);

        EndpointOrchestrator orchestrator = new EndpointOrchestrator(client);

        Boolean healthy = orchestrator.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block();

        assertThat(healthy).isTrue();
        assertThat(orchestrator.isProxyHealthy()).isTrue();
        assertThat(sendCount.get()).isEqualTo(2);
        EndpointOrchestrator.DiagnosticsSnapshot snap = orchestrator.getDiagnosticsSnapshot();
        assertThat(snap.getConsecutiveFailures()).isZero();
        assertThat(snap.getLastSuccessCount()).isEqualTo(2);
        assertThat(snap.getLastFailureCount()).isZero();
        assertThat(snap.getLastFailedEndpoints()).isEmpty();
    }

    @Test(groups = { "unit" })
    public void any503_failsTheCycle_andHysteresisDelaysFlip() {
        // Threshold = 2: one RED cycle should NOT flip; two RED cycles SHOULD flip.
        System.setProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD", "2");

        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        statusByEndpoint.put(REGION_WEST, 503);
        HttpClient client = mockClient(statusByEndpoint, new AtomicInteger(), false);

        EndpointOrchestrator orchestrator = new EndpointOrchestrator(client);

        // Cycle 1: RED but below threshold.
        assertThat(orchestrator.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isTrue();
        assertThat(orchestrator.isProxyHealthy()).isTrue();
        assertThat(orchestrator.getDiagnosticsSnapshot().getConsecutiveFailures()).isEqualTo(1);

        // Cycle 2: RED at threshold -> flip.
        assertThat(orchestrator.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block()).isFalse();
        assertThat(orchestrator.isProxyHealthy()).isFalse();
        assertThat(orchestrator.getDiagnosticsSnapshot().getConsecutiveFailures()).isEqualTo(2);
        assertThat(orchestrator.getDiagnosticsSnapshot().getLastFailedEndpoints()).containsExactly(REGION_WEST);
    }

    @Test(groups = { "unit" })
    public void singleGreenCycleRestoresHealthAndResetsCounter() {
        System.setProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD", "1");

        Map<URI, Integer> redByEndpoint = new HashMap<>();
        redByEndpoint.put(REGION_EAST, 503);
        EndpointOrchestrator orchestrator = new EndpointOrchestrator(mockClient(redByEndpoint, new AtomicInteger(), false));

        assertThat(orchestrator.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(orchestrator.isProxyHealthy()).isFalse();

        // Now swap to a green client and run another cycle on a fresh orchestrator that already saw a red.
        Map<URI, Integer> greenByEndpoint = new HashMap<>();
        greenByEndpoint.put(REGION_EAST, 200);
        EndpointOrchestrator greenOrchestrator = new EndpointOrchestrator(mockClient(greenByEndpoint, new AtomicInteger(), false));

        // Drive greenOrchestrator into the unhealthy state manually by replaying a red first.
        Map<URI, Integer> redOnly = new HashMap<>();
        redOnly.put(REGION_EAST, 503);
        EndpointOrchestrator combo = new EndpointOrchestrator(toggleClient(REGION_EAST, 503, 200));

        assertThat(combo.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(combo.isProxyHealthy()).isFalse();

        assertThat(combo.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isTrue();
        assertThat(combo.isProxyHealthy()).isTrue();
        assertThat(combo.getDiagnosticsSnapshot().getConsecutiveFailures()).isZero();
    }

    @Test(groups = { "unit" })
    public void transportErrorIsRed() {
        System.setProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD", "1");

        HttpClient client = Mockito.mock(HttpClient.class);
        Mockito.doAnswer(inv -> Mono.error(new java.net.ConnectException("refused")))
            .when(client).send(any(HttpRequest.class), any(Duration.class));
        Mockito.doAnswer(inv -> Mono.error(new java.net.ConnectException("refused")))
            .when(client).send(any(HttpRequest.class));

        EndpointOrchestrator orchestrator = new EndpointOrchestrator(client);

        assertThat(orchestrator.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
        assertThat(orchestrator.isProxyHealthy()).isFalse();
    }

    @Test(groups = { "unit" })
    public void featureFlagOff_isNoOp() {
        System.setProperty("COSMOS.THINCLIENT_PROBE_ENABLED", "false");

        HttpClient client = Mockito.mock(HttpClient.class);
        EndpointOrchestrator orchestrator = new EndpointOrchestrator(client);

        Boolean healthy = orchestrator.runProbeCycle(Arrays.asList(REGION_EAST, REGION_WEST)).block();
        assertThat(healthy).isTrue();
        assertThat(orchestrator.isProxyHealthy()).isTrue();
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class), any(Duration.class));
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class));
    }

    @Test(groups = { "unit" })
    public void emptyOrNullEndpointSet_isNoOp() {
        HttpClient client = Mockito.mock(HttpClient.class);
        EndpointOrchestrator orchestrator = new EndpointOrchestrator(client);

        assertThat(orchestrator.runProbeCycle(null).block()).isTrue();
        assertThat(orchestrator.runProbeCycle(Collections.emptyList()).block()).isTrue();
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class), any(Duration.class));
        Mockito.verify(client, Mockito.never()).send(any(HttpRequest.class));
    }

    @Test(groups = { "unit" })
    public void wrongPath400_isRed() {
        System.setProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD", "1");
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 400);
        EndpointOrchestrator orchestrator = new EndpointOrchestrator(mockClient(statusByEndpoint, new AtomicInteger(), false));
        assertThat(orchestrator.runProbeCycle(Collections.singletonList(REGION_EAST)).block()).isFalse();
    }

    @Test(groups = { "unit" })
    public void probeRequestTargetsConfiguredPath() {
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(REGION_EAST, 200);
        AtomicInteger sendCount = new AtomicInteger(0);
        HttpClient client = mockClient(statusByEndpoint, sendCount, true);

        EndpointOrchestrator orchestrator = new EndpointOrchestrator(client);
        orchestrator.runProbeCycle(Collections.singletonList(REGION_EAST)).block();

        assertThat(sendCount.get()).isEqualTo(1);
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
        ByteBuf empty = Unpooled.EMPTY_BUFFER;
        return new HttpResponse() {
            @Override public int statusCode() { return status; }
            @Override public String headerValue(String name) { return null; }
            @Override public HttpHeaders headers() { return new HttpHeaders(); }
            @Override public Mono<ByteBuf> body() { return Mono.just(empty); }
            @Override public Mono<String> bodyAsString() { return Mono.just(""); }
            @Override public void close() { }
        };
    }
}
