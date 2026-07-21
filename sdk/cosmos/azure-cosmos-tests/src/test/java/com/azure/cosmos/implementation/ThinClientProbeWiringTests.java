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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
            Set<URI> thinclientEndpoints = locationCache.getThinClientRegionalEndpointsEligibleForProbe();

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
            Set<URI> thinclientEndpoints = locationCache.getThinClientRegionalEndpointsEligibleForProbe();

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

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void thinClientDisabledAtRuntime_stopsProbeTraffic() throws Exception {
        // Dynamic disable: a wired-and-probing orchestrator must stop issuing probe traffic once
        // COSMOS.THINCLIENT_ENABLED is set to an explicit false at runtime, and the probe cycle
        // must be skipped on subsequent forced refreshes (routing separately short-circuits to V1).
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

        String previous = System.getProperty(THINCLIENT_ENABLED_PROPERTY);
        GlobalEndpointManager gem = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        try {
            // Phase 1: default (unset) -> probes fire, gate goes healthy.
            System.clearProperty(THINCLIENT_ENABLED_PROPERTY);
            gem.setThinClientHttpClient(httpClient);
            gem.init();
            waitForProxyDecision(gem, Boolean.TRUE, Duration.ofSeconds(5));
            int callsBeforeDisable = probeCallCount.get();
            assertThat(callsBeforeDisable).as("probes fired while enabled").isGreaterThanOrEqualTo(2);

            // Phase 2: explicit runtime opt-out -> forced refresh must NOT issue any new probe traffic.
            System.setProperty(THINCLIENT_ENABLED_PROPERTY, "false");
            gem.refreshLocationAsync(null, true).block();
            Thread.sleep(500); // allow any (incorrectly) scheduled probe to run

            assertThat(probeCallCount.get())
                .as("no new probe traffic is issued while COSMOS.THINCLIENT_ENABLED=false")
                .isEqualTo(callsBeforeDisable);
        } finally {
            restoreProperty(THINCLIENT_ENABLED_PROPERTY, previous);
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void thinClientOptedOutAtInit_probeResumesAfterOptOutDropped() throws Exception {
        // Lazy-wiring: an orchestrator wired while COSMOS.THINCLIENT_ENABLED=false must issue NO probe
        // traffic, yet must NOT be permanently disabled -- dropping the opt-out (unset) at runtime and
        // forcing a refresh must resume probing and let the gate flip healthy.
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

        String previous = System.getProperty(THINCLIENT_ENABLED_PROPERTY);
        GlobalEndpointManager gem = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        try {
            // Phase 1: opt out BEFORE wiring/init -> probe is wired but never fires while disabled.
            System.setProperty(THINCLIENT_ENABLED_PROPERTY, "false");
            gem.setThinClientHttpClient(httpClient);
            gem.init();
            Thread.sleep(500); // give any (incorrectly) scheduled probe a chance to run
            assertThat(probeCallCount.get())
                .as("no probe traffic is issued while opted out at init")
                .isEqualTo(0);

            // Phase 2: drop the opt-out and force a refresh -> probing resumes and the gate flips healthy.
            System.clearProperty(THINCLIENT_ENABLED_PROPERTY);
            gem.refreshLocationAsync(null, true).block();
            waitForProxyDecision(gem, Boolean.TRUE, Duration.ofSeconds(5));

            assertThat(probeCallCount.get())
                .as("probes resume for each thin-client region after the opt-out is dropped")
                .isGreaterThanOrEqualTo(2);
            assertThat(gem.getProxyProbeDecision())
                .as("gate flips healthy once probing resumes and all regions are proven")
                .isEqualTo(Boolean.TRUE);
        } finally {
            restoreProperty(THINCLIENT_ENABLED_PROPERTY, previous);
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void overlappingProbeFire_doesNotCancelActiveCycle() throws Exception {
        // New-A regression: a second (overlapping) probe fire must NOT cancel the sole in-flight cycle.
        // Pre-fix, fireThinClientProbeCycle() did getAndSet(newDisposable) + dispose(old) on the CALLING
        // thread, so a rapid overlapping fire disposed the only cycle doing real HTTP probes -> the gate
        // stayed FALSE forever. The fix never disposes on fire (close() owns cancellation; overlapping
        // fires are deduped by the probe client's single-flight CAS), so the in-flight cycle survives and
        // the gate still flips healthy.
        AtomicInteger probeCallCount = new AtomicInteger(0);
        CountDownLatch probeEntered = new CountDownLatch(2); // both regions probed concurrently (flatMap)
        CountDownLatch releaseProbe = new CountDownLatch(1); // holds both region sends in-flight
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(URI.create("https://testaccount-eastus.documents.azure.com:10250/connectivity-probe"), 200);
        statusByEndpoint.put(URI.create("https://testaccount-eastasia.documents.azure.com:10250/connectivity-probe"), 200);

        // A blocking stub: each probe registers itself and then parks until releaseProbe is counted down,
        // simulating a real in-flight HTTP probe. subscribeOn(boundedElastic) gives each concurrent probe
        // its own thread so both can be in-flight at once (mirrors non-blocking Netty sends under flatMap).
        HttpClient blockingClient = Mockito.mock(HttpClient.class);
        org.mockito.stubbing.Answer<Mono<HttpResponse>> blockingAnswer = invocation -> {
            HttpRequest req = invocation.getArgument(0);
            Integer status = statusByEndpoint.get(req.uri());
            if (status == null) {
                return Mono.error(new RuntimeException("Unexpected probe URI: " + req.uri()));
            }
            return Mono.fromCallable(() -> {
                    probeCallCount.incrementAndGet();
                    probeEntered.countDown();
                    releaseProbe.await();
                    return stubResponse(req, status);
                })
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
        };
        Mockito.when(blockingClient.send(any(HttpRequest.class), any(Duration.class))).thenAnswer(blockingAnswer);
        Mockito.when(blockingClient.send(any(HttpRequest.class))).thenAnswer(blockingAnswer);

        DatabaseAccount databaseAccount = new DatabaseAccount(DB_ACCOUNT_WITH_THINCLIENT_LOCATIONS);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(any())).thenReturn(Flux.just(databaseAccount));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI("https://testaccount.documents.azure.com:443"));

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);

        String previous = System.getProperty(THINCLIENT_ENABLED_PROPERTY);
        GlobalEndpointManager gem = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        try {
            System.clearProperty(THINCLIENT_ENABLED_PROPERTY); // tri-state unset -> probe-gated
            gem.setThinClientHttpClient(blockingClient);
            gem.init(); // cycle D1 fires; both region sends park on releaseProbe

            // D1 has won single-flight and both regions are in-flight concurrently.
            assertThat(probeEntered.await(5, TimeUnit.SECONDS))
                .as("the initial probe cycle reached in-flight HTTP for both thin-client regions")
                .isTrue();
            int callsInFlight = probeCallCount.get();

            // Fire an overlapping cycle while D1 is still in-flight. Pre-fix, this disposed D1 here.
            gem.refreshLocationAsync(null, true).block();

            // Single-flight: the overlapping fire is deduped and issues NO new probe traffic.
            assertThat(probeCallCount.get())
                .as("overlapping fire is deduped by single-flight and issues no new probe traffic")
                .isEqualTo(callsInFlight);

            // Let the still-alive cycle complete. On the fixed code D1 was never disposed.
            releaseProbe.countDown();

            waitForProxyDecision(gem, Boolean.TRUE, Duration.ofSeconds(5));
            assertThat(gem.getProxyProbeDecision())
                .as("the in-flight cycle survives the overlapping fire and flips the gate healthy")
                .isEqualTo(Boolean.TRUE);
        } finally {
            releaseProbe.countDown(); // never leave a probe thread parked
            restoreProperty(THINCLIENT_ENABLED_PROPERTY, previous);
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void overlappingProbeFire_closeStillCancelsTheLiveCycle() throws Exception {
        // New-A residual regression (PR #49796 comment r3575154107): an overlapping (no-op) probe fire
        // must NOT replace the tracked disposable that points at the sole in-flight cycle. close() owns
        // cancellation and disposes whatever handle is tracked, so if a no-op fire clobbers the handle,
        // close() cancels the no-op and the real in-flight probe I/O keeps running until its per-probe
        // timeout -- i.e. close() can no longer promptly stop the live cycle.
        //
        // Pre-fix, fireThinClientProbeCycle() did set(newDisposable) on EVERY fire, so an overlapping
        // fire (single-flight loser -> a no-op that completes immediately) overwrote the tracked handle:
        // the tracked disposable was then the disposed no-op, not the live cycle. The fix uses
        // getAndUpdate(keep-live-predecessor): while a live cycle is tracked, a no-op fire is discarded
        // and the handle keeps pointing at the real cycle, so close() promptly cancels it.
        AtomicInteger probeCallCount = new AtomicInteger(0);
        CountDownLatch probeEntered = new CountDownLatch(2); // both regions probed concurrently (flatMap)
        CountDownLatch releaseProbe = new CountDownLatch(1); // holds both region sends in-flight
        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(URI.create("https://testaccount-eastus.documents.azure.com:10250/connectivity-probe"), 200);
        statusByEndpoint.put(URI.create("https://testaccount-eastasia.documents.azure.com:10250/connectivity-probe"), 200);

        HttpClient blockingClient = Mockito.mock(HttpClient.class);
        org.mockito.stubbing.Answer<Mono<HttpResponse>> blockingAnswer = invocation -> {
            HttpRequest req = invocation.getArgument(0);
            Integer status = statusByEndpoint.get(req.uri());
            if (status == null) {
                return Mono.error(new RuntimeException("Unexpected probe URI: " + req.uri()));
            }
            return Mono.fromCallable(() -> {
                    probeCallCount.incrementAndGet();
                    probeEntered.countDown();
                    releaseProbe.await();
                    return stubResponse(req, status);
                })
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
        };
        Mockito.when(blockingClient.send(any(HttpRequest.class), any(Duration.class))).thenAnswer(blockingAnswer);
        Mockito.when(blockingClient.send(any(HttpRequest.class))).thenAnswer(blockingAnswer);

        DatabaseAccount databaseAccount = new DatabaseAccount(DB_ACCOUNT_WITH_THINCLIENT_LOCATIONS);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(any())).thenReturn(Flux.just(databaseAccount));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI("https://testaccount.documents.azure.com:443"));

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);

        String previous = System.getProperty(THINCLIENT_ENABLED_PROPERTY);
        GlobalEndpointManager gem = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        try {
            System.clearProperty(THINCLIENT_ENABLED_PROPERTY); // tri-state unset -> probe-gated
            gem.setThinClientHttpClient(blockingClient);
            gem.init(); // cycle D1 fires; both region sends park on releaseProbe

            assertThat(probeEntered.await(5, TimeUnit.SECONDS))
                .as("the initial probe cycle reached in-flight HTTP for both thin-client regions")
                .isTrue();

            // D1 is the tracked, live cycle.
            Disposable liveCycle = getTrackedProbeDisposable(gem);
            assertThat(liveCycle).as("the initial in-flight cycle is tracked").isNotNull();
            assertThat(liveCycle.isDisposed()).as("the tracked cycle is still live").isFalse();

            // Fire an overlapping cycle while D1 is still in-flight (single-flight -> a no-op loser).
            gem.refreshLocationAsync(null, true).block();

            // The crux: the tracked handle must STILL be the live cycle, not the no-op fire.
            // Pre-fix (set() on every fire) this is the disposed no-op, so both assertions go red.
            assertThat(getTrackedProbeDisposable(gem))
                .as("overlapping no-op fire must not replace the handle to the live cycle")
                .isSameAs(liveCycle);
            assertThat(liveCycle.isDisposed())
                .as("the live cycle is untouched by the overlapping fire")
                .isFalse();

            // close() disposes the tracked handle: it must promptly cancel the REAL in-flight cycle.
            gem.close();
            assertThat(liveCycle.isDisposed())
                .as("close() promptly cancels the live in-flight cycle")
                .isTrue();
        } finally {
            releaseProbe.countDown(); // never leave a probe thread parked
            restoreProperty(THINCLIENT_ENABLED_PROPERTY, previous);
            LifeCycleUtils.closeQuietly(gem);
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void staleTopologyGrowth_doesNotFlipGateForUnprobedRegion() throws Exception {
        // Issue #1 regression (stale-green): if the topology GROWS while a cycle is in flight, the
        // completing cycle must recompute the gate against the LATEST topology, not the snapshot it
        // captured at subscribe time.
        //
        // Pre-fix, applyCycleResult(endpoints, results) recomputed against the winning cycle's stale
        // snapshot T1={E1,E2}: after proving E1 and E2 it computed computeGate(T1)=TRUE, clobbering the
        // correct FALSE that the overlapping T2={E1,E2,E3} trigger had already published (E3 unproven).
        // That globally enabled thin-client routing for the still-unproven E3 region until the next
        // refresh -> misrouting. The fix recomputes against latestTopology (T2) so the gate stays FALSE.
        URI e1 = URI.create("https://region1.example.com:10250/");
        URI e2 = URI.create("https://region2.example.com:10250/");
        URI e3 = URI.create("https://region3.example.com:10250/");

        Map<URI, Integer> statusByEndpoint = new HashMap<>();
        statusByEndpoint.put(URI.create("https://region1.example.com:10250/connectivity-probe"), 200);
        statusByEndpoint.put(URI.create("https://region2.example.com:10250/connectivity-probe"), 200);
        statusByEndpoint.put(URI.create("https://region3.example.com:10250/connectivity-probe"), 200);

        CountDownLatch probeEntered = new CountDownLatch(2); // E1 + E2 probed by the winning cycle C1
        CountDownLatch releaseProbe = new CountDownLatch(1); // holds C1's two region sends in-flight

        // Blocking stub: each probe registers itself then parks until releaseProbe fires, simulating a
        // real in-flight HTTP probe. subscribeOn(boundedElastic) gives each concurrent probe its own
        // thread so both can be in-flight at once (mirrors non-blocking Netty sends under flatMap).
        HttpClient blockingClient = Mockito.mock(HttpClient.class);
        org.mockito.stubbing.Answer<Mono<HttpResponse>> blockingAnswer = invocation -> {
            HttpRequest req = invocation.getArgument(0);
            Integer status = statusByEndpoint.get(req.uri());
            if (status == null) {
                return Mono.error(new RuntimeException("Unexpected probe URI: " + req.uri()));
            }
            return Mono.fromCallable(() -> {
                    probeEntered.countDown();
                    releaseProbe.await();
                    return stubResponse(req, status);
                })
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
        };
        Mockito.when(blockingClient.send(any(HttpRequest.class), any(Duration.class))).thenAnswer(blockingAnswer);
        Mockito.when(blockingClient.send(any(HttpRequest.class))).thenAnswer(blockingAnswer);

        EndpointProbeClient client = new EndpointProbeClient(blockingClient);
        try {
            Set<URI> t1 = new LinkedHashSet<>(Arrays.asList(e1, e2));
            Set<URI> t2 = new LinkedHashSet<>(Arrays.asList(e1, e2, e3));

            // C1: the winning cycle probes T1 and parks both regions in-flight (async subscribe).
            CountDownLatch c1Done = new CountDownLatch(1);
            client.runProbeCycle(t1).subscribe(v -> { }, e -> c1Done.countDown(), c1Done::countDown);

            assertThat(probeEntered.await(5, TimeUnit.SECONDS))
                .as("C1 reached in-flight HTTP for both T1 regions and holds the single-flight slot")
                .isTrue();

            // C2: overlapping trigger publishing the grown topology T2. Deduped by single-flight, it
            // issues NO new probe traffic (E3 is never sent here) and returns the freshly recomputed
            // gate = FALSE because E3 is unproven. This also records latestTopology = T2.
            Boolean c2Gate = client.runProbeCycle(t2).block();
            assertThat(c2Gate)
                .as("overlapping trigger recomputes the gate against the grown topology (E3 unproven)")
                .isEqualTo(Boolean.FALSE);

            // Release C1; it proves E1 and E2 and then completes.
            releaseProbe.countDown();
            assertThat(c1Done.await(5, TimeUnit.SECONDS))
                .as("C1 completes after release")
                .isTrue();

            // Core assertion: the completing cycle recomputed the gate against the LATEST topology (T2),
            // so it stays FALSE because E3 was never probed. Pre-fix this clobbered to a stale-green TRUE.
            assertThat(client.isThinClientRoutable())
                .as("gate reflects the latest topology (E3 unproven) and does NOT flip to a stale green")
                .isFalse();
        } finally {
            releaseProbe.countDown(); // never leave a probe thread parked
            client.close();
        }
    }

    // ---- helpers ----

    private static final String THINCLIENT_ENABLED_PROPERTY = "COSMOS.THINCLIENT_ENABLED";

    private static void restoreProperty(String key, String previous) {
        if (previous == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previous);
        }
    }

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

    @SuppressWarnings("unchecked")
    private static Disposable getTrackedProbeDisposable(GlobalEndpointManager gem) throws Exception {
        Field f = GlobalEndpointManager.class.getDeclaredField("thinClientProbeCycleDisposable");
        f.setAccessible(true);
        return ((AtomicReference<Disposable>) f.get(gem)).get();
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
