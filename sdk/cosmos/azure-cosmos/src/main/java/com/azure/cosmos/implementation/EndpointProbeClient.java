// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Drives the thin-client HTTP/2 connectivity probe lifecycle using a per-region,
 * one-shot probe-and-cache model.
 *
 * <p>For every thin-client regional endpoint discovered via {@code GlobalEndpointManager}
 * topology refresh, this client issues a {@code POST /connectivity-probe} (path
 * configurable via {@link Configs#getThinClientProbePath()}) over the thin-client HTTP/2
 * {@link HttpClient}. The probe contract is strict:
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
 * topology refresh; this is the across-refresh retry mechanism. Within a single cycle, each
 * region is retried up to {@link Configs#getThinClientProbeMaxRetries()} times before being
 * treated as failed for that cycle.
 *
 * <p><b>Routing gate.</b> {@link #isProxyHealthy()} returns {@code true} only when every
 * currently-known thin-client endpoint has a cached success. The startup default is
 * <em>conservative</em>: until at least one non-empty topology has been observed and all of
 * its regions have succeeded, the gate is {@code false} and the SDK routes data-plane traffic
 * to Gateway V1. When the kill switch {@link Configs#isThinClientProbeEnabled()} is
 * {@code false}, the gate is bypassed and always reports healthy.
 *
 * <p>Routing decisions are made strictly at refresh boundaries; this class does not implement
 * any per-request circuit-breaker. The data-plane routing site is expected to AND its existing
 * thin-client-eligibility check with {@link #isProxyHealthy()}.
 *
 * <p>This class is internal; it is not part of the published public API.
 */
public class EndpointProbeClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(EndpointProbeClient.class);
    private static final byte[] EMPTY_BODY = new byte[0];

    private final HttpClient httpClient;
    private final int maxRetries;
    private final Duration perProbeTimeout;
    private final String probePath;

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
    private final AtomicLong cycleIdSeq = new AtomicLong(0);
    // Lean diagnostic surface: effective gate health at last state update (true/false/null) and
    // the wall-clock instant at which it was recorded. Anything beyond this is best observed via
    // logs to keep the diagnostic shape stable across releases.
    private final AtomicReference<Boolean> lastCycleSuccess = new AtomicReference<>(null);
    private final AtomicReference<Instant> lastStateUpdatedAt = new AtomicReference<>(null);

    public EndpointProbeClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(
            httpClient,
            "EndpointProbeClient requires a non-null thin-client HttpClient (HTTP/2). "
                + "Wire it via GlobalEndpointManager#setThinClientHttpClient before init().");
        this.maxRetries = Configs.getThinClientProbeMaxRetries();
        this.perProbeTimeout = Duration.ofMillis(Configs.getThinClientConnectionTimeoutInMs());
        this.probePath = Configs.getThinClientProbePath();
    }

    /**
     * Runs one probe cycle against the supplied set of thin-client regional endpoints,
     * probing only the regions that have not yet recorded a cached success, and emits the
     * post-cycle value of {@link #isProxyHealthy()}.
     *
     * <p>When the feature flag {@link Configs#isThinClientProbeEnabled()} is {@code false},
     * this is a no-op that emits the current (bypassed) health value without issuing any HTTP
     * traffic.
     *
     * <p>When the endpoint collection is {@code null} or empty (no thin-client regions
     * discovered), the cycle is a no-op; {@code GlobalEndpointManager} is expected to call
     * {@link #forceUnhealthy(String)} in that scenario.
     *
     * <p>When every currently-known region already has a cached success the cycle is a no-op
     * that re-records diagnostics and returns the current (healthy) gate value &mdash; no HTTP
     * traffic is issued.
     *
     * <p>The returned Mono never errors; internal exceptions are absorbed and recorded so that
     * probe failures do not propagate out and fail topology refresh.
     */
    public Mono<Boolean> runProbeCycle(Collection<URI> regionalEndpoints) {
        // All preconditions are re-evaluated at subscription time so an upstream
        // cancellation (e.g. GlobalEndpointManager.close() disposing the swap-disposable)
        // is honored before any HTTP I/O is initiated.
        return Mono.defer(() -> {
            if (this.closed.get()) {
                return Mono.fromSupplier(this::isProxyHealthy);
            }

            if (!Configs.isThinClientProbeEnabled()) {
                return Mono.fromSupplier(this::isProxyHealthy);
            }

            if (regionalEndpoints == null || regionalEndpoints.isEmpty()) {
                // No thin-client regions in topology -> probe is moot. GEM drives forceUnhealthy().
                return Mono.fromSupplier(this::isProxyHealthy);
            }

            Set<URI> endpoints = new LinkedHashSet<>(regionalEndpoints);
            endpoints.removeIf(Objects::isNull);
            if (endpoints.isEmpty()) {
                return Mono.fromSupplier(this::isProxyHealthy);
            }

            // A valid, non-empty topology was observed: clear any stale force-unhealthy latch and
            // adopt this set as the gate's evaluation basis.
            this.forcedUnhealthy.set(false);
            this.knownEndpoints = Collections.unmodifiableSet(endpoints);

            // Delta: only probe regions without a cached success.
            Set<URI> delta = new LinkedHashSet<>();
            for (URI endpoint : endpoints) {
                if (!Boolean.TRUE.equals(this.probeSucceeded.get(endpoint))) {
                    delta.add(endpoint);
                }
            }

            final Instant stateAt = Instant.now();

            if (delta.isEmpty()) {
                // Every known region already proven; nothing to probe. Refresh diagnostics only.
                recordState(stateAt);
                return Mono.fromSupplier(this::isProxyHealthy);
            }

            // Single-flight: if a cycle is already running, skip this trigger. knownEndpoints has
            // already been refreshed above so the gate reflects the latest topology regardless.
            if (!this.cycleInProgress.compareAndSet(false, true)) {
                logger.debug("Thin-client probe cycle already in progress; skipping overlapping trigger.");
                return Mono.fromSupplier(this::isProxyHealthy);
            }

            final long cycleId = this.cycleIdSeq.incrementAndGet();

            return Flux
                .fromIterable(delta)
                .flatMap(this::probeEndpointWithRetry)
                .collectList()
                .map(results -> applyCycleResult(results, stateAt, cycleId))
                .onErrorResume(t -> {
                    logger.warn(
                        "Thin-client probe cycle threw an unexpected error; leaving failed regions un-cached.", t);
                    return Mono.fromSupplier(() -> {
                        recordState(Instant.now());
                        return isProxyHealthy();
                    });
                })
                .doFinally(s -> this.cycleInProgress.set(false));
        });
    }

    /**
     * @return current routing-gate value. {@code true} means the SDK may route the data plane
     * to the thin-client proxy. The gate is bypassed (always {@code true}) when probing is
     * disabled; otherwise it is {@code true} only when at least one non-empty topology has been
     * observed, the force-unhealthy latch is clear, and every currently-known endpoint has a
     * cached success.
     */
    public boolean isProxyHealthy() {
        if (!Configs.isThinClientProbeEnabled()) {
            // Kill switch: behave exactly as if the probe machinery did not exist.
            return true;
        }
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
        // Honor the COSMOS.THINCLIENT_PROBE_ENABLED kill switch end-to-end: when probing is
        // disabled, no code path (HTTP cycle OR resolution-mismatch safeguard) is allowed to
        // mutate the routing gate. isProxyHealthy() already bypasses to true in that mode.
        if (!Configs.isThinClientProbeEnabled()) {
            logger.debug(
                "forceUnhealthy(reason='{}') skipped because COSMOS.THINCLIENT_PROBE_ENABLED is false.",
                reason);
            return;
        }
        boolean wasHealthy = isProxyHealthy();
        this.forcedUnhealthy.set(true);
        this.lastCycleSuccess.set(Boolean.FALSE);
        this.lastStateUpdatedAt.set(Instant.now());
        if (wasHealthy) {
            logger.warn(
                "Thin-client probe gate forced UNHEALTHY without an HTTP cycle (reason='{}'). "
                    + "SDK will route data plane to Gateway V1 until a subsequent non-empty probe cycle proves all known regions.",
                reason);
        }
    }

    /** @return read-only snapshot of probe state suitable for diagnostics. */
    public EndpointProbeDiagnosticsSnapshot getDiagnosticsSnapshot() {
        Set<URI> known = this.knownEndpoints;
        int knownCount = known.size();
        int succeededCount = 0;
        for (URI endpoint : known) {
            if (Boolean.TRUE.equals(this.probeSucceeded.get(endpoint))) {
                succeededCount++;
            }
        }
        return new EndpointProbeDiagnosticsSnapshot(
            this.lastCycleSuccess.get(),
            this.lastStateUpdatedAt.get(),
            knownCount,
            succeededCount);
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

    /**
     * Probes a single region, retrying up to {@link #maxRetries} times on failure. Total
     * attempts = {@code 1 + maxRetries}. The returned Mono never errors: a region that fails
     * every attempt resolves to its last failed {@link EndpointProbeResult}.
     */
    private Mono<EndpointProbeResult> probeEndpointWithRetry(URI regionalEndpoint) {
        return Mono
            .defer(() -> probeEndpointOnce(regionalEndpoint))
            .flatMap(result -> result.success
                ? Mono.just(result)
                : Mono.error(new ProbeRetryException(result)))
            .retryWhen(Retry.max(this.maxRetries))
            .onErrorResume(t -> {
                Throwable cause = (t instanceof ProbeRetryException) ? t : t.getCause();
                if (cause instanceof ProbeRetryException) {
                    // Retries exhausted: surface the last failed result.
                    return Mono.just(((ProbeRetryException) cause).result);
                }
                // Any other unexpected error: treat the region as failed for this cycle.
                logger.debug(
                    "Thin-client probe to {} failed unexpectedly during retry: {}",
                    regionalEndpoint, t.toString());
                return Mono.just(new EndpointProbeResult(
                    regionalEndpoint, false, "retry-error:" + t.getClass().getSimpleName()));
            });
    }

    private Mono<EndpointProbeResult> probeEndpointOnce(URI regionalEndpoint) {
        URI probeUri;
        try {
            probeUri = buildProbeUri(regionalEndpoint, this.probePath);
        } catch (URISyntaxException e) {
            logger.warn("Failed to build probe URI for {}: {}", regionalEndpoint, e.getMessage());
            return Mono.just(new EndpointProbeResult(regionalEndpoint, false, "bad-uri"));
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
        request.withBody(EMPTY_BODY);

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
                final EndpointProbeResult result = new EndpointProbeResult(regionalEndpoint, ok, "status:" + status);
                return response.body()
                    .doOnNext(buf -> {
                        if (buf != null) {
                            buf.release();
                        }
                    })
                    .then(Mono.just(result))
                    .timeout(this.perProbeTimeout)
                    .doFinally(s -> safeClose(response))
                    .onErrorResume(drainError -> {
                        logger.debug("Thin-client probe body drain to {} failed: {}",
                            regionalEndpoint, drainError.toString());
                        return Mono.just(result);
                    });
            })
            .onErrorResume(t -> {
                logger.debug(
                    "Thin-client probe to {} failed: {}", regionalEndpoint, t.toString());
                return Mono.just(new EndpointProbeResult(regionalEndpoint, false, "transport:" + t.getClass().getSimpleName()));
            });
    }

    private Boolean applyCycleResult(
        List<EndpointProbeResult> results,
        Instant stateAt,
        long cycleId) {

        // If the probe client was closed (e.g. CosmosClient.close()) while this cycle was
        // in flight, drop the result so we don't mutate state on a dead client.
        if (this.closed.get()) {
            logger.debug(
                "Thin-client probe cycle {} completed after close; dropping result.", cycleId);
            return isProxyHealthy();
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

        recordState(stateAt);
        boolean healthy = isProxyHealthy();

        if (failureCount == 0) {
            logger.info(
                "Thin-client probe cycle {} complete: {} new region(s) proven; gate healthy={} "
                    + "(known={} succeeded={}).",
                cycleId, successCount, healthy, this.knownEndpoints.size(), succeededKnownCount());
        } else {
            logger.info(
                "Thin-client probe cycle {} complete: {} new region(s) proven, {} still failing "
                    + "(will re-probe next refresh); gate healthy={} (known={} succeeded={}).",
                cycleId, successCount, failureCount, healthy, this.knownEndpoints.size(), succeededKnownCount());
        }

        return healthy;
    }

    private void recordState(Instant stateAt) {
        this.lastCycleSuccess.set(isProxyHealthy());
        this.lastStateUpdatedAt.set(stateAt);
    }

    private int succeededKnownCount() {
        int count = 0;
        for (URI endpoint : this.knownEndpoints) {
            if (Boolean.TRUE.equals(this.probeSucceeded.get(endpoint))) {
                count++;
            }
        }
        return count;
    }

    private static URI buildProbeUri(URI regionalEndpoint, String probePath) throws URISyntaxException {
        String normalizedPath = probePath.startsWith("/") ? probePath : "/" + probePath;
        return new URI(
            regionalEndpoint.getScheme(),
            null,
            regionalEndpoint.getHost(),
            regionalEndpoint.getPort(),
            normalizedPath,
            null,
            null);
    }

    private static void safeClose(HttpResponse response) {
        try {
            response.close();
        } catch (Exception ignored) {
            // best-effort
        }
    }

    /**
     * Internal signal used to drive {@code retryWhen}. Carries the failed
     * {@link EndpointProbeResult} so the last failure can be surfaced once retries are
     * exhausted. Never escapes the class.
     */
    private static final class ProbeRetryException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final transient EndpointProbeResult result;

        ProbeRetryException(EndpointProbeResult result) {
            super(null, null, false, false);
            this.result = result;
        }
    }
}
