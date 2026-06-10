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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Drives the thin-client HTTP/2 connectivity probe lifecycle.
 *
 * <p>For every thin-client regional endpoint discovered via {@code GlobalEndpointManager}
 * topology refresh, this orchestrator issues a {@code POST /connectivity-probe} (path
 * configurable via {@link Configs#getThinClientProbePath()}) over the thin-client HTTP/2
 * {@link HttpClient}. The probe contract (confirmed via CosmosDB PR 2107592) is strict:
 * <ul>
 *   <li><b>HTTP 200</b> &rarr; region is green.</li>
 *   <li>Any other status (notably 503 when {@code enableConnectivityProbe} is OFF, 400
 *       for wrong path, anything else) &rarr; region is red.</li>
 *   <li>Connection error / TLS failure / HTTP/2 negotiation failure / timeout &rarr; red.</li>
 * </ul>
 *
 * <p>A cycle is GREEN only if every supplied regional endpoint returns 200 within the
 * per-probe budget; otherwise the cycle is RED. The orchestrator applies a
 * configurable consecutive-failure threshold
 * ({@link Configs#getThinClientProbeFailureThreshold()}) before flipping
 * {@link #isProxyHealthy()} from {@code true} to {@code false}; a single GREEN cycle
 * resets the counter and restores health.
 *
 * <p>Routing decisions are made strictly at refresh boundaries; this class does not
 * implement any per-request circuit-breaker. The data-plane routing site is expected to
 * AND its existing thin-client-eligibility check with {@link #isProxyHealthy()}.
 *
 * <p>Optimistic startup: {@link #isProxyHealthy()} returns {@code true} until the first
 * probe cycle completes RED enough times to cross the threshold.
 *
 * <p>This class is internal; it is not part of the published public API.
 */
public class EndpointOrchestrator implements java.io.Closeable {

    private static final Logger logger = LoggerFactory.getLogger(EndpointOrchestrator.class);
    private static final byte[] EMPTY_BODY = new byte[0];

    private final HttpClient httpClient;
    private final int failureThreshold;
    private final int recoveryThreshold;
    private final Duration perProbeTimeout;
    private final String probePath;

    private final AtomicBoolean proxyHealthy = new AtomicBoolean(true);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicBoolean cycleInProgress = new AtomicBoolean(false);
    private final AtomicLong cycleIdSeq = new AtomicLong(0);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicInteger consecutiveSuccesses = new AtomicInteger(0);
    private final AtomicReference<Instant> lastCycleAt = new AtomicReference<>(null);
    private final AtomicReference<Instant> lastFailureAt = new AtomicReference<>(null);
    private final AtomicReference<Set<URI>> lastFailedEndpoints =
        new AtomicReference<>(Collections.emptySet());
    private final AtomicInteger lastSuccessCount = new AtomicInteger(0);
    private final AtomicInteger lastFailureCount = new AtomicInteger(0);

    public EndpointOrchestrator(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.failureThreshold = Configs.getThinClientProbeFailureThreshold();
        this.recoveryThreshold = Configs.getThinClientProbeRecoveryThreshold();
        this.perProbeTimeout = Duration.ofMillis(Configs.getThinClientConnectionTimeoutInMs());
        this.probePath = Configs.getThinClientProbePath();
    }

    /**
     * Runs one probe cycle against the supplied set of thin-client regional endpoints,
     * updates internal health state with hysteresis, and emits the post-cycle value of
     * {@link #isProxyHealthy()}.
     *
     * <p>When the feature flag {@link Configs#isThinClientProbeEnabled()} is {@code false},
     * this is a no-op that emits the current health value without issuing any HTTP traffic.
     *
     * <p>When the endpoint collection is {@code null} or empty (no thin-client regions
     * discovered), the cycle is treated as a no-op and health state is left unchanged.
     *
     * <p>The returned Mono never errors; internal exceptions are absorbed and counted as a
     * RED cycle so that probe failures do not propagate out and fail topology refresh.
     */
    public Mono<Boolean> runProbeCycle(Collection<URI> regionalEndpoints) {
        // All preconditions are re-evaluated at subscription time so an upstream
        // cancellation (e.g. GlobalEndpointManager.close() disposing the swap-disposable)
        // is honored before any HTTP I/O is initiated.
        return Mono.defer(() -> {
            if (this.closed.get()) {
                return Mono.fromSupplier(this.proxyHealthy::get);
            }

            if (!Configs.isThinClientProbeEnabled()) {
                return Mono.fromSupplier(this.proxyHealthy::get);
            }

            if (regionalEndpoints == null || regionalEndpoints.isEmpty()) {
                // No thin-client regions in topology -> probe is moot. Leave state unchanged.
                return Mono.fromSupplier(this.proxyHealthy::get);
            }

            Set<URI> endpoints = new HashSet<>(regionalEndpoints);
            endpoints.removeIf(Objects::isNull);
            if (endpoints.isEmpty()) {
                return Mono.fromSupplier(this.proxyHealthy::get);
            }

            // Single-flight: if a cycle is already running, skip this trigger. Combined
            // with the monotonic cycleId below, this guarantees that overlapping refresh
            // calls cannot increment consecutiveFailures faster than once per *completed*
            // cycle (addresses eager failover under refresh storms) and cannot let a stale
            // older cycle clobber a newer one (addresses missed/flapping failover).
            if (!this.cycleInProgress.compareAndSet(false, true)) {
                logger.debug("Thin-client probe cycle already in progress; skipping overlapping trigger.");
                return Mono.fromSupplier(this.proxyHealthy::get);
            }

            final long cycleId = this.cycleIdSeq.incrementAndGet();
            final Instant cycleStart = Instant.now();

            return Flux
                .fromIterable(endpoints)
                .flatMap(this::probeEndpoint)
                .collectList()
                .map(results -> applyCycleResult(results, endpoints, cycleStart, cycleId))
                .onErrorResume(t -> {
                    logger.warn(
                        "Thin-client probe cycle threw an unexpected error; counting as RED cycle.", t);
                    return Mono.just(applyCycleResult(
                        Collections.singletonList(new ProbeResult(null, false, "exception:" + t.getClass().getSimpleName())),
                        endpoints,
                        cycleStart,
                        cycleId));
                })
                .doFinally(s -> this.cycleInProgress.set(false));
        });
    }

    /** @return current proxy-health flag; {@code true} means SDK may route data plane to proxy. */
    public boolean isProxyHealthy() {
        return this.proxyHealthy.get();
    }

    /**
     * Forces the proxy-health flag to {@code false} without running a probe cycle. Used by
     * {@code GlobalEndpointManager} as a safeguard when account topology reports thin-client
     * read locations but {@code LocationCache} cannot resolve a single thin-client regional
     * endpoint (e.g. name-normalization mismatch between the gateway and thin-client region
     * lists). In that scenario no probe can fire, so the optimistic-startup default would
     * silently bypass the safety net and pin traffic to thin-client even when the proxy is
     * effectively unreachable. Calling this method flips the gate to RED and increments
     * {@code consecutiveFailures} so the next genuine cycle's hysteresis behavior is honored.
     *
     * @param reason short human-readable reason captured in the log.
     */
    public void forceUnhealthy(String reason) {
        if (this.closed.get()) {
            return;
        }
        this.consecutiveSuccesses.set(0);
        int now = this.consecutiveFailures.incrementAndGet();
        if (this.proxyHealthy.compareAndSet(true, false)) {
            logger.warn(
                "Thin-client probe gate flipped UNHEALTHY without an HTTP cycle (consecutiveFailures={}, reason='{}'). "
                    + "SDK will route data plane to Gateway V1 until {} consecutive GREEN cycle(s).",
                now, reason, this.recoveryThreshold);
        }
    }

    /** @return read-only snapshot of probe state suitable for diagnostics. */
    public DiagnosticsSnapshot getDiagnosticsSnapshot() {
        return new DiagnosticsSnapshot(
            this.proxyHealthy.get(),
            this.consecutiveFailures.get(),
            this.failureThreshold,
            this.lastCycleAt.get(),
            this.lastFailureAt.get(),
            this.lastFailedEndpoints.get(),
            this.lastSuccessCount.get(),
            this.lastFailureCount.get());
    }

    /**
     * Marks the orchestrator as closed. Subsequent {@link #runProbeCycle(Collection)}
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
            logger.debug("EndpointOrchestrator closed; no further thin-client probes will be issued.");
        }
    }

    private Mono<ProbeResult> probeEndpoint(URI regionalEndpoint) {
        URI probeUri;
        try {
            probeUri = buildProbeUri(regionalEndpoint, this.probePath);
        } catch (URISyntaxException e) {
            logger.warn("Failed to build probe URI for {}: {}", regionalEndpoint, e.getMessage());
            return Mono.just(new ProbeResult(regionalEndpoint, false, "bad-uri"));
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
                // status-based RED/GREEN classification still wins regardless of how the
                // drain stream terminates.
                final ProbeResult result = new ProbeResult(regionalEndpoint, ok, "status:" + status);
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
                return Mono.just(new ProbeResult(regionalEndpoint, false, "transport:" + t.getClass().getSimpleName()));
            });
    }

    private Boolean applyCycleResult(
        java.util.List<ProbeResult> results,
        Set<URI> attemptedEndpoints,
        Instant cycleStart,
        long cycleId) {

        // If the orchestrator was closed (e.g. CosmosClient.close()) while this cycle was
        // in flight, drop the result so we don't mutate health state on a dead client.
        if (this.closed.get()) {
            logger.debug(
                "Thin-client probe cycle {} completed after close; dropping result.", cycleId);
            return this.proxyHealthy.get();
        }

        int successCount = 0;
        Set<URI> failedEndpoints = new HashSet<>();
        for (ProbeResult r : results) {
            if (r.success) {
                successCount++;
            } else if (r.endpoint != null) {
                failedEndpoints.add(r.endpoint);
            }
        }
        // Treat any endpoint that didn't produce a ProbeResult as failed (defensive).
        if (results.size() < attemptedEndpoints.size()) {
            for (URI attempted : attemptedEndpoints) {
                boolean covered = false;
                for (ProbeResult r : results) {
                    if (attempted.equals(r.endpoint)) {
                        covered = true;
                        break;
                    }
                }
                if (!covered) {
                    failedEndpoints.add(attempted);
                }
            }
        }
        int failureCount = attemptedEndpoints.size() - successCount;

        boolean cycleGreen = (successCount == attemptedEndpoints.size()) && failedEndpoints.isEmpty();

        this.lastCycleAt.set(cycleStart);
        this.lastSuccessCount.set(successCount);
        this.lastFailureCount.set(failureCount);
        this.lastFailedEndpoints.set(Collections.unmodifiableSet(failedEndpoints));

        if (cycleGreen) {
            this.consecutiveFailures.set(0);
            int greens = this.consecutiveSuccesses.incrementAndGet();
            if (this.proxyHealthy.get()) {
                if (greens == 1) {
                    // already healthy; suppress noisy log
                    return this.proxyHealthy.get();
                }
                logger.debug(
                    "Thin-client probe cycle GREEN ({} endpoints). Consecutive GREEN={}; proxy remains healthy.",
                    successCount, greens);
            } else if (greens >= this.recoveryThreshold) {
                if (this.proxyHealthy.compareAndSet(false, true)) {
                    logger.info(
                        "Thin-client probe cycle GREEN ({} endpoints). Consecutive GREEN={} (recovery threshold={}); "
                            + "proxy marked HEALTHY; SDK will resume routing data plane to thin-client.",
                        successCount, greens, this.recoveryThreshold);
                }
            } else {
                logger.info(
                    "Thin-client probe cycle GREEN ({} endpoints). Consecutive GREEN={} (recovery threshold={}); "
                        + "proxy remains UNHEALTHY pending further GREEN cycles.",
                    successCount, greens, this.recoveryThreshold);
            }
        } else {
            this.consecutiveSuccesses.set(0);
            this.lastFailureAt.set(cycleStart);
            int now = this.consecutiveFailures.incrementAndGet();
            if (now >= this.failureThreshold) {
                if (this.proxyHealthy.compareAndSet(true, false)) {
                    logger.warn(
                        "Thin-client probe cycle RED ({} succeeded / {} failed) for {} consecutive cycles (threshold={}). "
                            + "Marking proxy UNHEALTHY; SDK will route data plane to Gateway V1 until {} consecutive GREEN cycle(s). "
                            + "Failed endpoints: {}",
                        successCount, failureCount, now, this.failureThreshold, this.recoveryThreshold, failedEndpoints);
                } else {
                    logger.warn(
                        "Thin-client probe cycle RED ({} succeeded / {} failed); consecutive failures={} (threshold={}); proxy remains UNHEALTHY. Failed endpoints: {}",
                        successCount, failureCount, now, this.failureThreshold, failedEndpoints);
                }
            } else {
                logger.info(
                    "Thin-client probe cycle RED ({} succeeded / {} failed); consecutive failures={} (threshold={}); proxy currently healthy={}. Failed endpoints: {}",
                    successCount, failureCount, now, this.failureThreshold, this.proxyHealthy.get(), failedEndpoints);
            }
        }

        return this.proxyHealthy.get();
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

    private static final class ProbeResult {
        final URI endpoint;
        final boolean success;
        @SuppressWarnings("unused")
        final String reason;

        ProbeResult(URI endpoint, boolean success, String reason) {
            this.endpoint = endpoint;
            this.success = success;
            this.reason = reason;
        }
    }

    /** Immutable snapshot of probe state for client diagnostics. */
    public static final class DiagnosticsSnapshot {
        private final boolean proxyHealthy;
        private final int consecutiveFailures;
        private final int failureThreshold;
        private final Instant lastCycleAt;
        private final Instant lastFailureAt;
        private final Set<URI> lastFailedEndpoints;
        private final int lastSuccessCount;
        private final int lastFailureCount;

        DiagnosticsSnapshot(
            boolean proxyHealthy,
            int consecutiveFailures,
            int failureThreshold,
            Instant lastCycleAt,
            Instant lastFailureAt,
            Set<URI> lastFailedEndpoints,
            int lastSuccessCount,
            int lastFailureCount) {
            this.proxyHealthy = proxyHealthy;
            this.consecutiveFailures = consecutiveFailures;
            this.failureThreshold = failureThreshold;
            this.lastCycleAt = lastCycleAt;
            this.lastFailureAt = lastFailureAt;
            this.lastFailedEndpoints = lastFailedEndpoints;
            this.lastSuccessCount = lastSuccessCount;
            this.lastFailureCount = lastFailureCount;
        }

        public boolean isProxyHealthy() { return proxyHealthy; }
        public int getConsecutiveFailures() { return consecutiveFailures; }
        public int getFailureThreshold() { return failureThreshold; }
        public Instant getLastCycleAt() { return lastCycleAt; }
        public Instant getLastFailureAt() { return lastFailureAt; }
        public Set<URI> getLastFailedEndpoints() { return lastFailedEndpoints; }
        public int getLastSuccessCount() { return lastSuccessCount; }
        public int getLastFailureCount() { return lastFailureCount; }
    }
}
