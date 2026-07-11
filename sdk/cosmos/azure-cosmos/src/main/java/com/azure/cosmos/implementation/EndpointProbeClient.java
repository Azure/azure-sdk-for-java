// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Drives the thin-client HTTP/2 connectivity probe lifecycle using a per-region,
 * one-shot probe-and-cache model.
 *
 * <p>For every thin-client regional endpoint discovered via {@code GlobalEndpointManager}
 * topology refresh, this client issues a {@code POST /connectivity-probe} over the thin-client
 * HTTP/2 {@link HttpClient}. The probe contract is strict:
 * <ul>
 *   <li><b>HTTP 200</b> &rarr; region succeeded.</li>
 *   <li>Any other status (notably 503 when {@code enableConnectivityProbe} is OFF, 400
 *       for wrong path, anything else) &rarr; region failed.</li>
 *   <li>Connection error / TLS failure / HTTP/2 negotiation failure / timeout &rarr; failed.</li>
 * </ul>
 *
 * <p><b>Per-region cache (delta probing).</b> A successful probe is recorded against its region
 * and that region is skipped on subsequent cycles for as long as it remains in the topology. Each
 * cycle only probes the <em>delta</em> &mdash; the currently-known endpoints not yet proven. A
 * failed region is left un-proven and is naturally re-probed on the next topology refresh; this
 * across-refresh re-probing is the only retry mechanism (there is no in-cycle retry &mdash; each
 * region is attempted exactly once per cycle).
 *
 * <p><b>Routing gate.</b> {@link #isThinClientRoutable()} returns {@code true} only when every
 * currently-known thin-client endpoint has a cached success. The startup default is
 * <em>conservative</em>: until at least one non-empty topology has been observed and all of
 * its regions have succeeded, the gate is {@code false} and the SDK routes data-plane traffic
 * to Gateway V1. Whether a probe client exists at all is governed solely by the wiring decision
 * (see {@link ThinClientConnectivityConfig#canThinClientBeImplicitlyEnabled()}): it is wired only
 * when thin-client is enabled by default — neither explicitly opted into nor out of — and GATEWAY
 * mode plus HTTP/2 hold. An explicit opt-in/opt-out skips the probe entirely.
 *
 * <p>Routing decisions are made strictly at refresh boundaries; this class does not implement
 * any per-request circuit-breaker. The data-plane routing site
 * ({@link ThinClientConnectivityConfig#shouldUseThinClientStoreModel(boolean, boolean, Boolean, Boolean, RxDocumentServiceRequest)})
 * ANDs the thin-client capability, topology availability, and request eligibility with this gate
 * (surfaced via {@code GlobalEndpointManager.getProxyProbeDecision()} → {@link #isThinClientRoutable()}).
 *
 * <p>This class is internal; it is not part of the published public API.
 */
public class EndpointProbeClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(EndpointProbeClient.class);

    // Fixed proxy-contract path (CosmosDB PR 2107592); not configurable.
    private static final String PROBE_PATH = "/connectivity-probe";

    private final HttpClient httpClient;
    private final Duration perProbeTimeout;

    // Single source of truth: every currently-known thin-client endpoint mapped to its probe
    // success state (TRUE = proven reachable, FALSE = known but not yet proven). Keys are the
    // current topology — reconciled on each cycle so vanished regions are dropped; values flip to
    // TRUE as probes succeed. The routing gate is simply "non-empty AND every value TRUE". A single
    // ConcurrentHashMap keeps reads lock-free without a second structure to keep in sync.
    private final ConcurrentHashMap<URI, Boolean> probeEndpointToProbeSuccessState = new ConcurrentHashMap<>();

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicBoolean cycleInProgress = new AtomicBoolean(false);

    public EndpointProbeClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(
            httpClient,
            "EndpointProbeClient requires a non-null thin-client HttpClient (HTTP/2). "
                + "Wire it via GlobalEndpointManager#setThinClientHttpClient before init().");
        this.perProbeTimeout = Duration.ofMillis(Configs.getThinClientConnectionTimeoutInMs());
    }

    /**
     * Runs one probe cycle against the supplied set of thin-client regional endpoints,
     * probing only the regions that have not yet recorded a cached success, and emits the
     * post-cycle value of {@link #isThinClientRoutable()}.
     *
     * <p>When the endpoint collection is {@code null} or empty (no thin-client regions resolved),
     * the reconciled map empties and the gate falls to its conservative {@code false} (route to
     * Gateway V1); the probe Flux then iterates nothing, so no HTTP traffic is issued.
     *
     * <p>There are no short-circuit fast paths: every cycle iterates the provided collection and
     * probes only the not-yet-proven endpoints. When all are already proven the filter yields
     * nothing and the cycle is an inexpensive no-op that re-emits the current (healthy) gate.
     *
     * <p>The returned Mono never errors; internal exceptions are absorbed and logged so that
     * probe failures do not propagate out and fail topology refresh.
     */
    public Mono<Boolean> runProbeCycle(Collection<URI> regionalEndpoints) {
        // All preconditions are re-evaluated at subscription time so an upstream
        // cancellation (e.g. GlobalEndpointManager.close() disposing the swap-disposable)
        // is honored before any HTTP I/O is initiated.
        return Mono.defer(() -> {
            // Every no-op fast path below emits this: the current gate value, evaluated lazily.
            final Mono<Boolean> currentGate = Mono.fromSupplier(this::isThinClientRoutable);

            if (this.closed.get()) {
                return currentGate;
            }

            // Normalize a null topology to an empty iteration (reference only — no copy).
            Collection<URI> endpoints = regionalEndpoints == null ? Collections.emptyList() : regionalEndpoints;

            // Reconcile the single source of truth directly against the provided collection — no
            // intermediate copies. Register newly-seen endpoints as not-yet-proven (preserving any
            // existing TRUE), then drop the ones that have vanished. Add-before-remove so a concurrent
            // gate read never transiently observes an over-healthy state. When the collection is
            // null/empty the map empties and the gate is false, so the SDK falls back to Gateway V1 —
            // no separate force-unhealthy latch needed.
            for (URI endpoint : endpoints) {
                if (endpoint != null) {
                    this.probeEndpointToProbeSuccessState.putIfAbsent(endpoint, Boolean.FALSE);
                }
            }
            this.probeEndpointToProbeSuccessState.keySet().retainAll(endpoints);

            // Single-flight: skip if a cycle is already running. The map was reconciled above so the
            // gate already reflects the latest topology regardless of whether we probe this round.
            if (!this.cycleInProgress.compareAndSet(false, true)) {
                logger.debug("Thin-client probe cycle already in progress; skipping overlapping trigger.");
                return currentGate;
            }

            // No fast paths: always iterate the provided collection and probe only the not-yet-proven
            // endpoints. When everything is already proven (or the collection is empty) the filter
            // yields nothing and the cycle is an inexpensive no-op that re-emits the current gate.
            return Flux
                .fromIterable(endpoints)
                .filter(endpoint -> endpoint != null
                    && !Boolean.TRUE.equals(this.probeEndpointToProbeSuccessState.get(endpoint)))
                .flatMap(this::probeEndpointOnce)
                .collectList()
                .map(this::applyCycleResult)
                .onErrorResume(t -> {
                    logger.warn(
                        "Thin-client probe cycle threw an unexpected error; leaving failed regions un-cached.", t);
                    return currentGate;
                })
                .doFinally(s -> this.cycleInProgress.set(false));
        });
    }

    /**
     * @return current routing-gate value. {@code true} means the SDK may route the data plane to the
     * thin-client proxy: {@code true} only when at least one thin-client region is currently known and
     * every currently-known region has a proven (HTTP 200) probe.
     */
    public boolean isThinClientRoutable() {
        if (this.probeEndpointToProbeSuccessState.isEmpty()) {
            // No known thin-client regions (conservative startup, or regions vanished): route to
            // Gateway V1 until a region is proven reachable.
            return false;
        }
        for (Boolean proven : this.probeEndpointToProbeSuccessState.values()) {
            if (!Boolean.TRUE.equals(proven)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Eagerly warms the shared thin-client HTTP/2 connection pool at client-build time.
     *
     * <p>For each supplied regional endpoint this issues {@code warmupRequestsPerEndpoint} concurrent
     * {@code POST /connectivity-probe} requests over the same HTTP/2 {@link HttpClient} that
     * {@code ThinClientStoreModel} uses for data-plane traffic. The intent is to ACTIVATE the Reactor
     * Netty pool and establish the first live connection (and let the pool's {@code minConnections}
     * floor top-fill) BEFORE the first data-plane request, so the cold TLS+HTTP/2 handshake burst is
     * paid off the request critical path.
     *
     * <p>Unlike {@link #runProbeCycle(Collection)}, this NEVER mutates the routing-gate state — it is
     * a pure connection-warming side effect and its per-request outcomes are discarded. All errors are
     * absorbed; the returned Mono completes (never errors) so a warm-up failure cannot fail client
     * initialization. Callers should still bound it with an external timeout.
     *
     * @param regionalEndpoints the thin-client regional endpoints to dial (null/empty is a no-op).
     * @param warmupRequestsPerEndpoint number of concurrent warm-up requests per endpoint (coerced to
     *                                  at least 1).
     * @return a {@link Mono} that completes when the warm-up requests settle; it never emits an error.
     */
    public Mono<Void> warmUpConnections(Collection<URI> regionalEndpoints, int warmupRequestsPerEndpoint) {
        return Mono.defer(() -> {
            if (this.closed.get() || regionalEndpoints == null || regionalEndpoints.isEmpty()) {
                return Mono.empty();
            }
            int perEndpoint = Math.max(1, warmupRequestsPerEndpoint);
            return Flux
                .fromIterable(regionalEndpoints)
                .filter(Objects::nonNull)
                .flatMap(endpoint -> Flux
                    .range(0, perEndpoint)
                    .flatMap(i -> this.probeEndpointOnce(endpoint)))
                .then()
                .onErrorResume(t -> {
                    logger.debug("Thin-client connection warm-up encountered an error; ignoring.", t);
                    return Mono.empty();
                });
        });
    }

    /**
     * Marks the probe client as closed. Subsequent {@link #runProbeCycle(Collection)}
     * invocations short-circuit and issue no further HTTP/2 probes. The shared
     * thin-client {@link HttpClient} is owned by {@code RxDocumentClientImpl} and is NOT
     * closed here — its lifetime is bound to the {@code CosmosClient} itself.
     *
     * <p>In-flight probe Monos are not actively cancelled; they will self-terminate via
     * the per-probe timeout. Their results are still applied to internal state but, since
     * the host {@code GlobalEndpointManager} is also closed, no consumer will observe the
     * flip.
     */
    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)) {
            logger.debug("EndpointProbeClient closed; no further thin-client probes will be issued.");
        }
    }

    private Mono<EndpointProbeResult> probeEndpointOnce(URI regionalEndpoint) {
        URI probeUri;
        try {
            probeUri = buildProbeUri(regionalEndpoint);
        } catch (URISyntaxException e) {
            logger.warn("Failed to build probe URI for {}: {}", regionalEndpoint, e.getMessage());
            return Mono.just(new EndpointProbeResult(regionalEndpoint, false));
        }

        HttpHeaders headers = new HttpHeaders();
        // Mirror thin-client traffic so any proxy-side routing/diagnostics treat this
        // request the same way as a real data-plane request.
        headers.set(HttpConstants.HttpHeaders.THINCLIENT_PROXY_OPERATION_TYPE, "ConnectivityProbe");

        HttpRequest request = new HttpRequest(
            HttpMethod.POST,
            probeUri,
            probeUri.getPort(),
            headers);
        request.withThinClientRequest(true);

        return this.httpClient
            .send(request, this.perProbeTimeout)
            .flatMap(response -> {
                int status = response.statusCode();
                boolean ok = status == 200;
                if (!ok) {
                    logger.debug("Thin-client probe to {} returned status {}", regionalEndpoint, status);
                }
                // Drain the body within the probe Mono lifecycle so reactor-netty releases
                // the underlying buffer and a slow/trickling body cannot leak resources
                // outside `perProbeTimeout`. doFinally + onErrorResume guarantee that
                // status-based success classification still wins regardless of how the
                // drain stream terminates.
                final EndpointProbeResult result = new EndpointProbeResult(regionalEndpoint, ok);
                return response.body()
                    .doOnNext(buf -> {
                        // body() emits a single aggregated ByteBuf that this subscriber owns. The
                        // probe discards the body, so release it exactly once. Mirror reactor-netty's
                        // releaseOnNotSubscribedResponse / RxGatewayStoreModel safe-release idiom:
                        // a refCnt guard + ReferenceCountUtil.safeRelease so a release race cannot
                        // throw IllegalReferenceCountException into the drain stream.
                        if (buf != null && buf.refCnt() > 0) {
                            ReferenceCountUtil.safeRelease(buf);
                        }
                    })
                    .then(Mono.just(result))
                    .timeout(this.perProbeTimeout)
                    .onErrorResume(drainError -> {
                        logger.debug("Thin-client probe body drain to {} failed: {}",
                            regionalEndpoint, drainError.toString());
                        return Mono.just(result);
                    });
            })
            .onErrorResume(t -> {
                logger.debug(
                    "Thin-client probe to {} failed: {}", regionalEndpoint, t.toString());
                return Mono.just(new EndpointProbeResult(regionalEndpoint, false));
            });
    }

    private Boolean applyCycleResult(List<EndpointProbeResult> results) {

        // If the probe client was closed (e.g. CosmosClient.close()) while this cycle was
        // in flight, drop the result so we don't mutate state on a dead client.
        if (this.closed.get()) {
            logger.debug("Thin-client probe cycle completed after close; dropping result.");
            return isThinClientRoutable();
        }

        int successCount = 0;
        for (EndpointProbeResult r : results) {
            if (r.success && r.endpoint != null) {
                // Flip to proven, but only if the region is still current: replace() is a no-op when
                // a concurrent reconcile has already dropped it, so vanished regions aren't resurrected.
                this.probeEndpointToProbeSuccessState.replace(r.endpoint, Boolean.TRUE);
                successCount++;
            }
        }
        int failureCount = results.size() - successCount;
        boolean healthy = isThinClientRoutable();

        if (failureCount > 0) {
            logger.warn(
                "Thin-client probe cycle complete: {} region(s) proven, {} still failing "
                    + "(will re-probe next refresh); gate healthy={}.",
                successCount, failureCount, healthy);
        } else {
            logger.debug(
                "Thin-client probe cycle complete: {} new region(s) proven; gate healthy={}.",
                successCount, healthy);
        }

        return healthy;
    }

    private static URI buildProbeUri(URI regionalEndpoint) throws URISyntaxException {
        return new URI(
            regionalEndpoint.getScheme(),
            null,
            regionalEndpoint.getHost(),
            regionalEndpoint.getPort(),
            PROBE_PATH,
            null,
            null);
    }
}
