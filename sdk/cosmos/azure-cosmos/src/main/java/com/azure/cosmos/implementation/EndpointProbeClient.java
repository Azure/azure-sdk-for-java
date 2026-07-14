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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Drives the thin-client HTTP/2 connectivity probe lifecycle with a per-region, one-shot
 * probe-and-cache model.
 *
 * <p>For every thin-client regional endpoint from {@code GlobalEndpointManager} topology refresh,
 * issues a {@code POST /connectivity-probe} over the thin-client HTTP/2 {@link HttpClient}. Only
 * HTTP 200 counts as success; any other status (e.g. 503 when {@code enableConnectivityProbe} is
 * off) or a connection/TLS/HTTP2/timeout failure counts as a failure.
 *
 * <p><b>Add-only proven cache (delta probing).</b> A 200 records the region in an add-only set that
 * only grows: a proven region is never re-probed for the client's lifetime, even across vanish/
 * re-appear (matching .NET permanent-cache semantics). Each cycle probes only the delta (known
 * endpoints not yet proven); a failed region stays out and is re-probed next refresh, which is the
 * only retry (one attempt per region per cycle).
 *
 * <p><b>Routing gate.</b> {@link #isThinClientRoutable()} returns a cached boolean recomputed each
 * cycle from the endpoints passed to {@link #runProbeCycle(Collection)}: {@code true} only when that
 * topology is non-empty and every region in it is proven. A vanished region (absent from the supplied
 * set) stops gating. Conservative at startup: {@code false} (route to Gateway V1) until a non-empty
 * topology is fully proven. Whether a probe client exists at all is a wiring decision
 * (see {@link ThinClientConnectivityConfig#canThinClientBeUsed()}): the probe is wired whenever
 * thin-client is usable (GATEWAY mode + HTTP/2 and {@code COSMOS.THINCLIENT_ENABLED} not an explicit
 * {@code false}). An explicit opt-in still wires the probe, but the routing site bypasses it; only a
 * hard opt-out ({@code false}) skips the probe entirely.
 *
 * <p>Decisions are made only at refresh boundaries (no per-request circuit-breaker). The routing
 * site {@link ThinClientConnectivityConfig#shouldUseThinClientStoreModel(boolean, boolean, Boolean, Boolean, RxDocumentServiceRequest)}
 * ANDs capability, topology availability, and request eligibility with this gate (via
 * {@code GlobalEndpointManager.getProxyProbeDecision()}).
 */
public class EndpointProbeClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(EndpointProbeClient.class);

    private static final String PROBE_PATH = "/connectivity-probe";

    private final HttpClient httpClient;
    private final Duration perProbeTimeout;
    private final Set<URI> provenHealthyEndpoints = ConcurrentHashMap.newKeySet();

    private volatile boolean thinClientRoutable = false;

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
     * Runs one probe cycle against the supplied endpoints, probing only regions not yet proven, and
     * emits the post-cycle {@link #isThinClientRoutable()} value.
     *
     * <p>A {@code null}/empty collection sets the gate {@code false} (route to Gateway V1) and probes
     * nothing; the add-only proven set is never pruned. When all endpoints are already proven the
     * cycle is a no-op re-emitting the current gate. The returned Mono never errors —
     * internal failures are absorbed and logged so they don't fail topology refresh.
     */
    public Mono<Boolean> runProbeCycle(Collection<URI> regionalEndpoints) {
        // Preconditions re-evaluated at subscription time so an upstream cancel
        // (GlobalEndpointManager.close()) is honored before any HTTP I/O.
        return Mono.defer(() -> {
            // The caller passes an immutable, null-free set; a null topology normalizes to empty
            // (gate false, Gateway V1).
            final Collection<URI> endpoints =
                regionalEndpoints == null ? Collections.emptySet() : regionalEndpoints;

            if (this.closed.get()) {
                return Mono.just(this.thinClientRoutable);
            }

            // Recompute the cached gate against the latest topology using the current proven set, so
            // it reflects the newest endpoints even if this cycle probes nothing or is skipped as an
            // overlapping trigger.
            this.thinClientRoutable = computeGate(endpoints);

            if (!this.cycleInProgress.compareAndSet(false, true)) {
                logger.debug("Thin-client probe cycle already in progress; skipping overlapping trigger.");
                return Mono.just(this.thinClientRoutable);
            }

            // Probe only the not-yet-proven delta; an empty/all-proven filter is a cheap no-op.
            return Flux
                .fromIterable(endpoints)
                .filter(endpoint -> !this.provenHealthyEndpoints.contains(endpoint))
                .flatMap(this::probeEndpointOnce)
                .collectList()
                .map(results -> applyCycleResult(endpoints, results))
                .onErrorResume(t -> {
                    logger.warn(
                        "Thin-client probe cycle threw an unexpected error; leaving failed regions un-cached.", t);
                    return Mono.just(this.thinClientRoutable);
                })
                .doFinally(s -> this.cycleInProgress.set(false));
        });
    }

    /**
     * @return the cached routing gate from the most recent probe cycle: {@code true} only when the
     * supplied topology was non-empty and every region in it is proven.
     */
    public boolean isThinClientRoutable() {
        return this.thinClientRoutable;
    }

    private boolean computeGate(Collection<URI> endpoints) {
        // True only with at least one known region, all proven; vanished regions (absent from the
        // supplied set) stop gating even though they linger in the add-only proven set.
        return !endpoints.isEmpty() && this.provenHealthyEndpoints.containsAll(endpoints);
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
        // Mirror data-plane traffic so proxy-side routing/diagnostics treat it identically.
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
                final EndpointProbeResult result = new EndpointProbeResult(regionalEndpoint, ok);
                return response.body()
                    .doOnNext(buf -> {
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

    private Boolean applyCycleResult(Collection<URI> endpoints, List<EndpointProbeResult> results) {

        // Dropped if the client closed mid-cycle so we don't mutate a dead client.
        if (this.closed.get()) {
            logger.debug("Thin-client probe cycle completed after close; dropping result.");
            return this.thinClientRoutable;
        }

        for (EndpointProbeResult r : results) {
            // Add-only: record each newly proven region (no-op if already proven). Failed regions
            // stay out of the proven set and are re-probed on the next refresh's delta.
            if (r.success && r.endpoint != null) {
                this.provenHealthyEndpoints.add(r.endpoint);
            }
        }
        this.thinClientRoutable = computeGate(endpoints);
        return this.thinClientRoutable;
    }

    /**
     * Marks the client closed; subsequent {@link #runProbeCycle(Collection)} calls short-circuit. The
     * shared {@link HttpClient} is owned by {@code RxDocumentClientImpl}, not closed here. In-flight
     * probes self-terminate via the per-probe timeout; their results are applied but unobserved since
     * the host {@code GlobalEndpointManager} is also closing.
     */
    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)) {
            logger.debug("EndpointProbeClient closed; no further thin-client probes will be issued.");
        }
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
