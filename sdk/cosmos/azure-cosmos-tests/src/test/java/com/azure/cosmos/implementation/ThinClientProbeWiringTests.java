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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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
 *   <li>Default health is optimistic ({@code true}) before any probes complete.</li>
 *   <li>{@code isProxyProbeHealthy()} remains optimistic when no orchestrator is wired
 *       (preserves pre-existing GW v1 / direct-mode behavior).</li>
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

    private DatabaseAccountManagerInternal databaseAccountManagerInternal;

    @BeforeClass(groups = "unit")
    public void setup() {
        databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);
    }

    @BeforeMethod(groups = { "unit" })
    public void resetSystemProperties() {
        System.clearProperty("COSMOS.THINCLIENT_PROBE_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD");
    }

    @AfterMethod(groups = { "unit" })
    public void clearSystemProperties() {
        System.clearProperty("COSMOS.THINCLIENT_PROBE_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD");
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void isProxyProbeHealthy_returnsTrueWhenNoOrchestratorWired() throws Exception {
        GlobalEndpointManager gem = newGemWithAccount(DB_ACCOUNT_WITH_THINCLIENT_LOCATIONS);
        try {
            // No setThinClientHttpClient() called -> optimistic default keeps existing routing decisions intact.
            assertThat(gem.isProxyProbeHealthy()).isTrue();
            assertThat(gem.getThinClientProbeDiagnostics()).isNull();
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
            assertThat(gem.isProxyProbeHealthy()).as("after all-200 cycle, proxy is healthy").isTrue();
            assertThat(gem.getThinClientProbeDiagnostics()).isNotNull();
            assertThat(gem.getThinClientProbeDiagnostics().getLastCycleSuccess()).isEqualTo(Boolean.TRUE);
            assertThat(gem.getThinClientProbeDiagnostics().getLastStateUpdatedAt()).isNotNull();
        } finally {
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void setThinClientHttpClient_redProbesFlipHealthyAfterThreshold() throws Exception {
        System.setProperty("COSMOS.THINCLIENT_PROBE_FAILURE_THRESHOLD", "1");

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

            // Wait for the first cycle to complete and flip health (threshold=1 -> flips immediately on first RED).
            long deadline = System.currentTimeMillis() + 5_000;
            while (System.currentTimeMillis() < deadline) {
                if (!gem.isProxyProbeHealthy()) {
                    break;
                }
                Thread.sleep(50);
            }

            assertThat(gem.isProxyProbeHealthy())
                .as("after all-RED cycle with threshold=1, proxy should be marked unhealthy")
                .isFalse();
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
}
