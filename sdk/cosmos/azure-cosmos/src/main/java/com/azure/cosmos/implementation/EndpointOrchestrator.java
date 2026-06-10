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
    private final Duration perProbeTimeout;
    private final String probePath;

    private final AtomicBoolean proxyHealthy = new AtomicBoolean(true);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicReference<Instant> lastCycleAt = new AtomicReference<>(null);
    private final AtomicReference<Instant> lastFailureAt = new AtomicReference<>(null);
    private final AtomicReference<Set<URI>> lastFailedEndpoints =
        new AtomicReference<>(Collections.emptySet());
    private final AtomicInteger lastSuccessCount = new AtomicInteger(0);
    private final AtomicInteger lastFailureCount = new AtomicInteger(0);

    public EndpointOrchestrator(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.failureThreshold = Configs.getThinClientProbeFailureThreshold();
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
        if (this.closed.get()) {
            // Client is shutting down; do not initiate any further network I/O.
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

        Instant cycleStart = Instant.now();

        return Flux
            .fromIterable(endpoints)
            .flatMap(this::probeEndpoint)
            .collectList()
            .map(results -> applyCycleResult(results, endpoints, cycleStart))
            .onErrorResume(t -> {
                logger.warn(
                    "Thin-client probe cycle threw an unexpected error; counting as RED cycle.", t);
                return Mono.just(applyCycleResult(
                    Collections.singletonList(new ProbeResult(null, false, "exception:" + t.getClass().getSimpleName())),
                    endpoints,
                    cycleStart));
            });
    }

    /** @return current proxy-health flag; {@code true} means SDK may route data plane to proxy. */
    public boolean isProxyHealthy() {
        return this.proxyHealthy.get();
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
            .map(response -> {
                int status = response.statusCode();
                boolean ok = status == 200;
                if (!ok) {
                    logger.debug("Thin-client probe to {} returned status {}", regionalEndpoint, status);
                }
                // Drain body so reactor-netty releases the underlying buffer.
                response.body()
                    .doFinally(s -> safeClose(response))
                    .subscribe(buf -> { if (buf != null) buf.release(); }, t -> { });
                return new ProbeResult(regionalEndpoint, ok, "status:" + status);
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
        Instant cycleStart) {

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
            int prior = this.consecutiveFailures.getAndSet(0);
            if (prior > 0 || !this.proxyHealthy.get()) {
                logger.info(
                    "Thin-client probe cycle GREEN ({} endpoints). Resetting consecutive failures (was {}); proxy marked healthy.",
                    successCount, prior);
            }
            this.proxyHealthy.set(true);
        } else {
            this.lastFailureAt.set(cycleStart);
            int now = this.consecutiveFailures.incrementAndGet();
            if (now >= this.failureThreshold) {
                if (this.proxyHealthy.compareAndSet(true, false)) {
                    logger.warn(
                        "Thin-client probe cycle RED ({} succeeded / {} failed) for {} consecutive cycles (threshold={}). "
                            + "Marking proxy UNHEALTHY; SDK will route data plane to Gateway V1 until next GREEN cycle. "
                            + "Failed endpoints: {}",
                        successCount, failureCount, now, this.failureThreshold, failedEndpoints);
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
