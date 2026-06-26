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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
 * <p><b>Per-region cache (delta probing).</b> A successful probe for a region is cached for
 * the lifetime of this client and that region is never probed again. Each cycle only probes
 * the <em>delta</em> &mdash; the currently-known endpoints that have not yet recorded a
 * success. A failed region is simply left un-cached and is naturally re-probed on the next
 * topology refresh; this across-refresh re-probing is the only retry mechanism (there is no
 * in-cycle retry &mdash; each region is attempted exactly once per cycle).
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
 * ({@link ThinClientConnectivityConfig#shouldUseThinClientStoreModel(boolean, boolean, boolean, Boolean, RxDocumentServiceRequest)})
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

    // Per-region success cache. Only successful probes are ever recorded (endpoint -> TRUE);
    // a region's presence as a key means "this region has been proven reachable and must never
    // be probed again". Failures are intentionally NOT recorded so the region re-enters the
    // delta on the next refresh.
    private final ConcurrentHashMap<URI, Boolean> probeSucceeded = new ConcurrentHashMap<>();
    // Snapshot of the most recently observed non-empty thin-client topology. The routing gate
    // is evaluated against exactly this set. Replaced wholesale (never mutated) on each valid
    // cycle so reads are consistent without locking.
    private volatile Set<URI> knownEndpoints = Collections.emptySet();
    // Latch set by forceUnhealthy(...) when a refresh resolves an empty thin-client endpoint set
    // after a prior successful cycle. Without it, the now-stale knownEndpoints / probeSucceeded
    // would keep the gate healthy even though the account no longer exposes thin-client regions.
    // Cleared automatically by the next valid (non-empty) cycle.
    private final AtomicBoolean forcedUnhealthy = new AtomicBoolean(false);

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
     * <p>When the endpoint collection is {@code null} or empty (no thin-client regions
     * discovered), the cycle is a no-op; {@code GlobalEndpointManager} is expected to call
     * {@link #forceUnhealthy(String)} in that scenario.
     *
     * <p>When every currently-known region already has a cached success the cycle is a no-op
     * that returns the current (healthy) gate value &mdash; no HTTP traffic is issued.
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

            // Nothing to probe when the client is closed or no endpoints were supplied.
            if (this.closed.get() || regionalEndpoints == null) {
                return currentGate;
            }

            Set<URI> endpoints = new LinkedHashSet<>(regionalEndpoints);
            endpoints.removeIf(Objects::isNull);
            if (endpoints.isEmpty()) {
                // No thin-client regions in topology -> probe is moot. GEM drives forceUnhealthy().
                return currentGate;
            }

            // A valid, non-empty topology was observed: clear any stale force-unhealthy latch and
            // adopt this set as the gate's evaluation basis.
            this.forcedUnhealthy.set(false);
            this.knownEndpoints = Collections.unmodifiableSet(endpoints);

            // Delta: only probe regions that have not yet recorded a cached success.
            Set<URI> delta = new LinkedHashSet<>(endpoints);
            delta.removeIf(endpoint -> Boolean.TRUE.equals(this.probeSucceeded.get(endpoint)));
            if (delta.isEmpty()) {
                // Every known region already proven; nothing to probe.
                return currentGate;
            }

            // Single-flight: skip if a cycle is already running. knownEndpoints was refreshed above
            // so the gate still reflects the latest topology regardless.
            if (!this.cycleInProgress.compareAndSet(false, true)) {
                logger.debug("Thin-client probe cycle already in progress; skipping overlapping trigger.");
                return currentGate;
            }

            return Flux
                .fromIterable(delta)
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
     * @return current routing-gate value. {@code true} means the SDK may route the data plane
     * to the thin-client proxy. It is {@code true} only when at least one non-empty topology has
     * been observed, the force-unhealthy latch is clear, and every currently-known endpoint has a
     * cached success.
     */
    public boolean isThinClientRoutable() {
        if (this.forcedUnhealthy.get()) {
            return false;
        }
        Set<URI> known = this.knownEndpoints;
        if (known.isEmpty()) {
            // Conservative startup: route to Gateway V1 until a region is proven reachable.
            return false;
        }
        for (URI endpoint : known) {
            if (!Boolean.TRUE.equals(this.probeSucceeded.get(endpoint))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Forces the routing gate to {@code false} without running a probe cycle. Used by
     * {@code GlobalEndpointManager} as a safeguard when account topology reports thin-client
     * read locations but {@code LocationCache} cannot resolve a single thin-client regional
     * endpoint (e.g. name-normalization mismatch, or the account no longer exposing thin-client
     * regions after previously doing so). In that scenario no probe can fire, so without this
     * latch the stale per-region cache from an earlier successful cycle would keep the gate
     * healthy and silently bypass the safety net.
     *
     * <p>The latch is cleared automatically by the next valid (non-empty) probe cycle.
     *
     * @param reason short human-readable reason captured in the log.
     */
    public void forceUnhealthy(String reason) {
        if (this.closed.get()) {
            return;
        }
        boolean wasHealthy = isThinClientRoutable();
        this.forcedUnhealthy.set(true);
        if (wasHealthy) {
            logger.warn(
                "Thin-client probe gate forced UNHEALTHY without an HTTP cycle (reason='{}'). "
                    + "SDK will route data plane to Gateway V1 until a subsequent non-empty probe cycle proves all known regions.",
                reason);
        }
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
                // Cache the success permanently; this region will be excluded from future deltas.
                this.probeSucceeded.put(r.endpoint, Boolean.TRUE);
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
