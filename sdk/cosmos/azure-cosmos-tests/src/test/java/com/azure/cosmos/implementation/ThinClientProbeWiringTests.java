// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.routing.LocationCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

/**
 * Verifies the connectivity-probe orchestrator is correctly wired into
 * {@link GlobalEndpointManager} and that {@link com.azure.cosmos.implementation.routing.LocationCache}
 * exposes thin-client regional endpoints discovered during topology refresh.
 *
 * <p>These tests cover the integration boundary that the routing gate
 * {@code RxDocumentClientImpl.useThinClientStoreModel} relies on:
 * <ul>
 *   <li>{@code getProxyProbeDecision()} renders no decision ({@code null}) when no orchestrator is
 *       wired (preserves pre-existing GW v1 / direct-mode behavior).</li>
 *   <li>Once an orchestrator is wired, the gate is conservative: the decision stays {@code FALSE}
 *       until every known thin-client region has a cached successful probe.</li>
 *   <li>Once an HttpClient is wired, the orchestrator probes the regional endpoints
 *       discovered by {@code LocationCache.getThinClientRegionalEndpoints()}.</li>
 *   <li>An empty thin-client region set does not trigger probe traffic.</li>
 * </ul>
 */
public class ThinClientProbeWiringTests {

    private static final int TIMEOUT = 60_000;

    private static final String DB_ACCOUNT_WITH_THINCLIENT_LOCATIONS =
        "{\"_self\":\"\",\"id\":\"testaccount\",\"_rid\":\"testaccount.documents.azure.com\","
            + "\"writableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:443/\"}],"
            + "\"readableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:443/\"},"
            + "{\"name\":\"East Asia\",\"databaseAccountEndpoint\":\"https://testaccount-eastasia.documents.azure.com:443/\"}],"
            + "\"thinClientWritableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:10250/\"}],"
            + "\"thinClientReadableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:10250/\"},"
            + "{\"name\":\"East Asia\",\"databaseAccountEndpoint\":\"https://testaccount-eastasia.documents.azure.com:10250/\"}],"
            + "\"enableMultipleWriteLocations\":false,\"userReplicationPolicy\":{\"asyncReplication\":false,\"minReplicaSetSize\":3,\"maxReplicasetSize\":4},"
            + "\"userConsistencyPolicy\":{\"defaultConsistencyLevel\":\"Session\"},\"systemReplicationPolicy\":{\"minReplicaSetSize\":3,\"maxReplicasetSize\":4},"
            + "\"readPolicy\":{\"primaryReadCoefficient\":1,\"secondaryReadCoefficient\":1}}";

    private static final String DB_ACCOUNT_NO_THINCLIENT_LOCATIONS =
        "{\"_self\":\"\",\"id\":\"testaccount\",\"_rid\":\"testaccount.documents.azure.com\","
            + "\"writableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:443/\"}],"
            + "\"readableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:443/\"}],"
            + "\"enableMultipleWriteLocations\":false,\"userReplicationPolicy\":{\"asyncReplication\":false,\"minReplicaSetSize\":3,\"maxReplicasetSize\":4},"
            + "\"userConsistencyPolicy\":{\"defaultConsistencyLevel\":\"Session\"},\"systemReplicationPolicy\":{\"minReplicaSetSize\":3,\"maxReplicasetSize\":4},"
            + "\"readPolicy\":{\"primaryReadCoefficient\":1,\"secondaryReadCoefficient\":1}}";

    private static final String DB_ACCOUNT_ONE_THINCLIENT_LOCATION =
        "{\"_self\":\"\",\"id\":\"testaccount\",\"_rid\":\"testaccount.documents.azure.com\","
            + "\"writableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:443/\"}],"
            + "\"readableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:443/\"}],"
            + "\"thinClientWritableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:10250/\"}],"
            + "\"thinClientReadableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:10250/\"}],"
            + "\"enableMultipleWriteLocations\":false,\"userReplicationPolicy\":{\"asyncReplication\":false,\"minReplicaSetSize\":3,\"maxReplicasetSize\":4},"
            + "\"userConsistencyPolicy\":{\"defaultConsistencyLevel\":\"Session\"},\"systemReplicationPolicy\":{\"minReplicaSetSize\":3,\"maxReplicasetSize\":4},"
            + "\"readPolicy\":{\"primaryReadCoefficient\":1,\"secondaryReadCoefficient\":1}}";

    private DatabaseAccountManagerInternal databaseAccountManagerInternal;

    @BeforeClass(groups = "unit")
    public void setup() {
        databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void getProxyProbeDecision_returnsNullWhenNoOrchestratorWired() throws Exception {
        GlobalEndpointManager gem = newGemWithAccount(DB_ACCOUNT_WITH_THINCLIENT_LOCATIONS);
        try {
            // No setThinClientHttpClient() called -> no decision can be rendered; routing is left to other gate inputs.
            assertThat(gem.getProxyProbeDecision()).isNull();
        } finally {
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void locationCache_exposesThinClientRegionalEndpoints() throws Exception {
        GlobalEndpointManager gem = newGemWithAccount(DB_ACCOUNT_WITH_THINCLIENT_LOCATIONS);
        try {
            LocationCache locationCache = getLocationCache(gem);
            Set<URI> thinclientEndpoints = locationCache.getThinClientRegionalEndpoints();

            assertThat(thinclientEndpoints).hasSize(2);
            assertThat(thinclientEndpoints).contains(
                URI.create("https://testaccount-eastus.documents.azure.com:10250/"),
                URI.create("https://testaccount-eastasia.documents.azure.com:10250/"));
        } finally {
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void locationCache_returnsEmptySetWhenNoThinClientLocations() throws Exception {
        GlobalEndpointManager gem = newGemWithAccount(DB_ACCOUNT_NO_THINCLIENT_LOCATIONS);
        try {
            LocationCache locationCache = getLocationCache(gem);
            Set<URI> thinclientEndpoints = locationCache.getThinClientRegionalEndpoints();

            assertThat(thinclientEndpoints).isEmpty();
            assertThat(gem.hasThinClientReadLocations()).isFalse();
        } finally {
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void setThinClientHttpClient_triggersProbeOnRefresh() throws Exception {
        AtomicInteger probeCallCount = new AtomicInteger(0);
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(URI.create("https://testaccount-eastus.documents.azure.com:10250/connectivity-probe"), 200);
        statusByEndpoint.put(URI.create("https://testaccount-eastasia.documents.azure.com:10250/connectivity-probe"), 200);
        HttpClient httpClient = stubHttpClient(statusByEndpoint, probeCallCount);

        DatabaseAccount databaseAccount = new DatabaseAccount(DB_ACCOUNT_WITH_THINCLIENT_LOCATIONS);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(any())).thenReturn(Flux.just(databaseAccount));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI("https://testaccount.documents.azure.com:443"));

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);

        GlobalEndpointManager gem = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        try {
            // Wire BEFORE init so the first refresh probes (mirrors RxDocumentClientImpl.init() sequence).
            gem.setThinClientHttpClient(httpClient);
            gem.init();

            // Probe is fire-and-forget on a scheduler -> wait briefly for it to run.
            waitForProbeCallCount(probeCallCount, 2, Duration.ofSeconds(5));

            assertThat(probeCallCount.get()).as("probe was issued for each thin-client region").isGreaterThanOrEqualTo(2);
            assertThat(gem.getProxyProbeDecision()).as("after all-200 cycle, proxy is healthy").isEqualTo(Boolean.TRUE);
        } finally {
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void setThinClientHttpClient_redProbesKeepProxyUnhealthy() throws Exception {
        // Each region is attempted exactly once per cycle (no in-cycle retry).
        AtomicInteger probeCallCount = new AtomicInteger(0);
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(URI.create("https://testaccount-eastus.documents.azure.com:10250/connectivity-probe"), 503);
        statusByEndpoint.put(URI.create("https://testaccount-eastasia.documents.azure.com:10250/connectivity-probe"), 503);
        HttpClient httpClient = stubHttpClient(statusByEndpoint, probeCallCount);

        DatabaseAccount databaseAccount = new DatabaseAccount(DB_ACCOUNT_WITH_THINCLIENT_LOCATIONS);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(any())).thenReturn(Flux.just(databaseAccount));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI("https://testaccount.documents.azure.com:443"));

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);

        GlobalEndpointManager gem = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        try {
            gem.setThinClientHttpClient(httpClient);
            gem.init();

            // Wait for the probe cycle to actually fire against both regions.
            waitForProbeCallCount(probeCallCount, 2, Duration.ofSeconds(5));

            assertThat(probeCallCount.get())
                .as("probe was issued for each thin-client region")
                .isGreaterThanOrEqualTo(2);
            assertThat(gem.getProxyProbeDecision())
                .as("no region recorded a successful probe, so the proxy gate stays unhealthy")
                .isEqualTo(Boolean.FALSE);
        } finally {
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void refreshWithNewRegion_probesOnlyNewlyAddedRegion() throws Exception {
        // Delta-probing across a SUBSEQUENT account refresh: a thin-client region that appears
        // after the initial topology read must be probed, while an already-proven region must
        // NOT be re-probed. This proves the probe cycle (a) runs on every refresh and (b) is
        // delta-gated to newly-added regions.
        AtomicInteger probeCallCount = new AtomicInteger(0);
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(URI.create("https://testaccount-eastus.documents.azure.com:10250/connectivity-probe"), 200);
        statusByEndpoint.put(URI.create("https://testaccount-eastasia.documents.azure.com:10250/connectivity-probe"), 200);
        HttpClient httpClient = stubHttpClient(statusByEndpoint, probeCallCount);

        // Topology can change between refreshes; start with a single thin-client region (East US).
        AtomicReference<DatabaseAccount> currentAccount =
            new AtomicReference<>(new DatabaseAccount(DB_ACCOUNT_ONE_THINCLIENT_LOCATION));
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(any()))
            .thenAnswer(invocation -> Flux.just(currentAccount.get()));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint())
            .thenReturn(new URI("https://testaccount.documents.azure.com:443"));

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);

        GlobalEndpointManager gem = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        try {
            gem.setThinClientHttpClient(httpClient);
            gem.init();

            // Phase 1: only East US is known -> exactly one region is probed and the gate goes healthy.
            waitForProxyDecision(gem, Boolean.TRUE, Duration.ofSeconds(5));
            assertThat(probeCallCount.get())
                .as("only the single known thin-client region (East US) is probed on the initial refresh")
                .isEqualTo(1);
            assertThat(gem.getProxyProbeDecision()).isEqualTo(Boolean.TRUE);

            // Phase 2: East Asia is added to the topology; a forced refresh must probe ONLY the new region.
            currentAccount.set(new DatabaseAccount(DB_ACCOUNT_WITH_THINCLIENT_LOCATIONS));
            gem.refreshLocationAsync(null, true).block();

            waitForProbeCallCount(probeCallCount, 2, Duration.ofSeconds(5));

            assertThat(probeCallCount.get())
                .as("delta refresh probes only the newly-added region (East Asia); East US is not re-probed")
                .isEqualTo(2);
            assertThat(gem.getProxyProbeDecision())
                .as("both thin-client regions are now proven healthy")
                .isEqualTo(Boolean.TRUE);
        } finally {
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    // ---- helpers ----

    private GlobalEndpointManager newGemWithAccount(String accountJson) throws Exception {
        DatabaseAccount databaseAccount = new DatabaseAccount(accountJson);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any()))
            .thenReturn(Flux.just(databaseAccount));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint())
            .thenReturn(new URI("https://testaccount.documents.azure.com:443"));

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);

        GlobalEndpointManager gem = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        gem.init();
        return gem;
    }

    private static LocationCache getLocationCache(GlobalEndpointManager gem) throws Exception {
        Field f = GlobalEndpointManager.class.getDeclaredField("locationCache");
        f.setAccessible(true);
        return (LocationCache) f.get(gem);
    }

    private static HttpClient stubHttpClient(Map<URI, Integer> statusByEndpoint, AtomicInteger callCount) {
        HttpClient mock = Mockito.mock(HttpClient.class);
        Mockito.when(mock.send(any(HttpRequest.class), any(Duration.class)))
            .thenAnswer(invocation -> {
                HttpRequest req = invocation.getArgument(0);
                callCount.incrementAndGet();
                Integer status = statusByEndpoint.get(req.uri());
                if (status == null) {
                    return Mono.error(new RuntimeException("Unexpected probe URI: " + req.uri()));
                }
                return Mono.just(stubResponse(req, status));
            });
        Mockito.when(mock.send(any(HttpRequest.class)))
            .thenAnswer(invocation -> {
                HttpRequest req = invocation.getArgument(0);
                callCount.incrementAndGet();
                Integer status = statusByEndpoint.get(req.uri());
                if (status == null) {
                    return Mono.error(new RuntimeException("Unexpected probe URI: " + req.uri()));
                }
                return Mono.just(stubResponse(req, status));
            });
        return mock;
    }

    private static HttpResponse stubResponse(HttpRequest req, int status) {
        return new HttpResponse() {
            @Override
            public int statusCode() {
                return status;
            }

            @Override
            public String headerValue(String name) {
                return null;
            }

            @Override
            public HttpHeaders headers() {
                return new HttpHeaders();
            }

            @Override
            public Mono<ByteBuf> body() {
                return Mono.just(Unpooled.EMPTY_BUFFER);
            }

            @Override
            public Mono<String> bodyAsString() {
                return Mono.just("");
            }

            @Override
            public void close() { }
        };
    }

    private static void waitForProbeCallCount(AtomicInteger counter, int expected, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline && counter.get() < expected) {
            Thread.sleep(50);
        }
    }

    private static void waitForProxyDecision(GlobalEndpointManager gem, Boolean expected, Duration timeout)
        throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline && !expected.equals(gem.getProxyProbeDecision())) {
            Thread.sleep(50);
        }
    }
}
