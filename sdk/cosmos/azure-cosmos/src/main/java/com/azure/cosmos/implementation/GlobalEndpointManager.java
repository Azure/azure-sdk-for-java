// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.routing.LocationCache;
import com.azure.cosmos.implementation.routing.LocationHelper;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Endpoint region cache manager implementation. Supports cross region address routing based on
 * availability and preference list.
 */
public class GlobalEndpointManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(GlobalEndpointManager.class);

    private final int backgroundRefreshLocationTimeIntervalInMS;
    private final int backgroundRefreshJitterMaxInSeconds;
    private final LocationCache locationCache;
    private final URI defaultEndpoint;
    private final ConnectionPolicy connectionPolicy;
    private final Duration maxInitializationTime;
    private final DatabaseAccountManagerInternal owner;
    private final AtomicBoolean isRefreshing;
    private final AtomicBoolean refreshInBackground;
    private final AtomicReference<Disposable> backgroundRefreshDisposable = new AtomicReference<>();
    private volatile boolean isClosed;
    private volatile DatabaseAccount latestDatabaseAccount;
    private final AtomicBoolean hasThinClientReadLocations = new AtomicBoolean(false);
    private final AtomicBoolean lastRecordedPerPartitionAutomaticFailoverEnabledOnClient = new AtomicBoolean(false);
    private final AtomicReference<EndpointProbeClient> thinClientProbeClient = new AtomicReference<>(null);

    private final ReentrantReadWriteLock.WriteLock databaseAccountWriteLock;

    private final ReentrantReadWriteLock.ReadLock databaseAccountReadLock;

    private volatile Throwable latestDatabaseRefreshError;

    private volatile Consumer<DatabaseAccount> perPartitionAutomaticFailoverConfigModifier;

    public void setLatestDatabaseRefreshError(Throwable latestDatabaseRefreshError) {
        this.latestDatabaseRefreshError = latestDatabaseRefreshError;
    }
    public Throwable getLatestDatabaseRefreshError() {
        return latestDatabaseRefreshError;
    }

    public GlobalEndpointManager(DatabaseAccountManagerInternal owner, ConnectionPolicy connectionPolicy, Configs configs)  {
        this.backgroundRefreshLocationTimeIntervalInMS = configs.getUnavailableLocationsExpirationTimeInSeconds() * 1000;
        this.backgroundRefreshJitterMaxInSeconds = configs.getBackgroundRefreshLocationJitterMaxInSeconds();
        this.maxInitializationTime = Duration.ofSeconds(configs.getGlobalEndpointManagerMaxInitializationTimeInSeconds());

        try {
            this.locationCache = new LocationCache(
                    connectionPolicy,
                    owner.getServiceEndpoint(),
                    configs);

            this.owner = owner;
            this.defaultEndpoint = owner.getServiceEndpoint();
            this.connectionPolicy = connectionPolicy;

            this.isRefreshing = new AtomicBoolean(false);
            this.refreshInBackground = new AtomicBoolean(false);
            this.isClosed = false;
            ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

            this.databaseAccountWriteLock = reentrantReadWriteLock.writeLock();
            this.databaseAccountReadLock = reentrantReadWriteLock.readLock();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void init() {
        startRefreshLocationTimerAsync(true).block(maxInitializationTime);
    }

    public UnmodifiableList<RegionalRoutingContext> getReadEndpoints() {
        // readonly
        return this.locationCache.getReadEndpoints();
    }

    public UnmodifiableList<RegionalRoutingContext> getWriteEndpoints() {
        //readonly
        return this.locationCache.getWriteEndpoints();
    }

    public UnmodifiableList<RegionalRoutingContext> getApplicableReadRegionalRoutingContexts(RxDocumentServiceRequest request) {
        // readonly
        return this.locationCache.getApplicableReadRegionRoutingContexts(request);
    }

    public UnmodifiableList<RegionalRoutingContext> getApplicableWriteRegionalRoutingContexts(RxDocumentServiceRequest request) {
        //readonly
        return this.locationCache.getApplicableWriteRegionRoutingContexts(request);
    }

    public UnmodifiableList<RegionalRoutingContext> getApplicableReadRegionalRoutingContexts(List<String> excludedRegions) {
        // readonly
        return this.locationCache.getApplicableReadRegionRoutingContexts(excludedRegions, Collections.emptyList());
    }

    public UnmodifiableList<RegionalRoutingContext> getApplicableWriteRegionalRoutingContexts(List<String> excludedRegions) {
        //readonly
        return this.locationCache.getApplicableWriteRegionRoutingContexts(excludedRegions, Collections.emptyList());
    }

    public List<RegionalRoutingContext> getAvailableReadRoutingContexts() {
        return this.locationCache.getAvailableReadRegionalRoutingContexts();
    }

    public List<RegionalRoutingContext> getAvailableWriteRoutingContexts() {
        return this.locationCache.getAvailableWriteRegionalRoutingContexts();
    }

    public static Mono<DatabaseAccount> getDatabaseAccountFromAnyLocationsAsync(
            URI defaultEndpoint, List<String> locations, Function<URI, Mono<DatabaseAccount>> getDatabaseAccountFn) {

        return getDatabaseAccountFn.apply(defaultEndpoint).onErrorResume(
                e -> {
                    logger.error("Fail to reach global gateway [{}], [{}]", defaultEndpoint, e.getMessage());
                    if (locations.isEmpty()) {
                        return Mono.error(e);
                    }

                    Flux<Flux<DatabaseAccount>> obs = Flux.range(0, locations.size())
                            .map(index -> getDatabaseAccountFn.apply(LocationHelper.getLocationEndpoint(defaultEndpoint, locations.get(index))).flux());

                    // iterate and get the database account from the first non failure, otherwise get the last error.
                    Mono<DatabaseAccount> res = Flux.concatDelayError(obs).take(1).single();
                    return res.doOnError(
                            innerE -> logger.error("Fail to reach location any of locations {} {}", String.join(",", locations), innerE.getMessage()));
                });
    }

    public RegionalRoutingContext resolveServiceEndpoint(RxDocumentServiceRequest request) {
        RegionalRoutingContext serviceEndpoints = this.locationCache.resolveServiceEndpoint(request);
        if (request.faultInjectionRequestContext != null) {
            // TODO: integrate thin client into fault injection
            request.faultInjectionRequestContext.setRegionalRoutingContextToRoute(serviceEndpoints);
        }

        return serviceEndpoints;
    }

    public RegionalRoutingContext resolveFaultInjectionServiceEndpoint(String region, boolean writeOnly) {
        return this.locationCache.resolveFaultInjectionEndpoint(region, writeOnly);
    }

    public URI getDefaultEndpoint() {
        return this.locationCache.getDefaultEndpoint();
    }

    public void markEndpointUnavailableForRead(URI endpoint) {
        logger.debug("Marking endpoint {} unavailable for read",endpoint);
        this.locationCache.markEndpointUnavailableForRead(endpoint);;
    }

    public void markEndpointUnavailableForWrite(URI endpoint) {
        logger.debug("Marking  endpoint {} unavailable for Write",endpoint);
        this.locationCache.markEndpointUnavailableForWrite(endpoint);
    }

    public boolean canUseMultipleWriteLocations() {
        return this.locationCache.canUseMultipleWriteLocations();
    }

    public boolean canUseMultipleWriteLocations(RxDocumentServiceRequest request) {
        return this.locationCache.canUseMultipleWriteLocations(request);
    }

    public void close() {
        this.isClosed = true;
        this.perPartitionAutomaticFailoverConfigModifier = null;
        Disposable disposable = this.backgroundRefreshDisposable.getAndSet(null);
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        logger.debug("GlobalEndpointManager closed.");
    }

    public Mono<Void> refreshLocationAsync(DatabaseAccount databaseAccount, boolean forceRefresh) {
        return Mono.defer(() -> {
            logger.debug("refreshLocationAsync() invoked");

            if (forceRefresh) {
                Mono<DatabaseAccount> databaseAccountObs = getDatabaseAccountFromAnyLocationsAsync(
                    this.defaultEndpoint,
                    new ArrayList<>(this.getEffectivePreferredRegions()),
                    this::getDatabaseAccountAsync);

                return databaseAccountObs.map(dbAccount -> {
                    this.databaseAccountWriteLock.lock();

                    try {
                        this.locationCache.onDatabaseAccountRead(dbAccount);
                    } finally {
                        this.databaseAccountWriteLock.unlock();
                    }

                    return dbAccount;
                }).flatMap(dbAccount -> {
                    return Mono.<Void>empty();
                })
                // Force-refresh bypasses refreshLocationPrivateAsync but still updates topology
                // (hasThinClientReadLocations / LocationCache), so drive the delta-gated probe
                // cycle here too to honor "probe on every account refresh".
                .then(runThinClientProbeCycleMono());
            }

            if (!isRefreshing.compareAndSet(false, true)) {
                logger.debug("in the middle of another refresh. Not invoking a new refresh.");
                return Mono.empty();
            }

            logger.debug("will refresh");
            return this.refreshLocationPrivateAsync(databaseAccount).doOnError(e -> this.isRefreshing.set(false));
        });
    }

    /**
     * This will provide the latest databaseAccount.
     * If due to some reason last databaseAccount update was null,
     * this method will return previous valid value
     * @return DatabaseAccount
     */
    public DatabaseAccount getLatestDatabaseAccount() {
        return this.latestDatabaseAccount;
    }

    public int getPreferredLocationCount() {
        List<String> effectivePreferredRegions = this.getEffectivePreferredRegions();

        return effectivePreferredRegions != null ? effectivePreferredRegions.size() : 0;
    }

    private Mono<Void> refreshLocationPrivateAsync(DatabaseAccount databaseAccount) {
        return Mono.<Void>defer(() -> {
            logger.debug("refreshLocationPrivateAsync() refreshing locations");

            Mono<Void> probePrefix = Mono.empty();
            if (databaseAccount != null) {
                this.databaseAccountWriteLock.lock();

                try {
                    this.locationCache.onDatabaseAccountRead(databaseAccount);
                } finally {
                    this.databaseAccountWriteLock.unlock();
                }

                // Run the thin-client connectivity-probe cycle on every topology refresh so
                // the routing gate (getProxyProbeDecision) can converge to the proxy fleet.
                probePrefix = this.runThinClientProbeCycleMono();
            }

            return probePrefix.then(Mono.defer(() -> {
                Utils.ValueHolder<Boolean> canRefreshInBackground = new Utils.ValueHolder<>();
                if (this.locationCache.shouldRefreshEndpoints(canRefreshInBackground)) {
                    logger.debug("shouldRefreshEndpoints: true");

                    if (databaseAccount == null && !canRefreshInBackground.v) {
                        logger.debug("shouldRefreshEndpoints: can't be done in background");

                        Mono<DatabaseAccount> databaseAccountObs = getDatabaseAccountFromAnyLocationsAsync(
                                this.defaultEndpoint,
                                new ArrayList<>(this.getEffectivePreferredRegions()),
                                this::getDatabaseAccountAsync);

                        return databaseAccountObs.flatMap(dbAccount -> {
                            this.databaseAccountWriteLock.lock();

                            try {
                                this.locationCache.onDatabaseAccountRead(dbAccount);
                            } finally {
                                this.databaseAccountWriteLock.unlock();
                            }

                            this.isRefreshing.set(false);
                            return this.runThinClientProbeCycleMono();
                        }).then(Mono.defer(() -> {
                            // trigger a startRefreshLocationTimerAsync don't wait on it.
                            if (!this.refreshInBackground.get()) {
                                this.startRefreshLocationTimerAsync();
                            }
                            return Mono.<Void>empty();
                        }));
                    }

                    // trigger a startRefreshLocationTimerAsync don't wait on it.
                    if (!this.refreshInBackground.get()) {
                        this.startRefreshLocationTimerAsync();
                    }

                    this.isRefreshing.set(false);
                    return Mono.<Void>empty();
                } else {
                    logger.debug("shouldRefreshEndpoints: false, nothing to do.");

                    // Even when no endpoint refresh is needed right now, we must keep the
                    // background refresh timer running so that future database account
                    // topology changes are detected — e.g., multi-write <-> single-write
                    // transitions, failover priority changes, region add/remove.
                    // This aligns with the .NET SDK behavior where the background loop
                    // continues unconditionally as long as the client is alive.
                    if (!this.refreshInBackground.get()) {
                        this.startRefreshLocationTimerAsync();
                    }

                    this.isRefreshing.set(false);
                    return Mono.<Void>empty();
                }
            }));
        });
    }

    private void startRefreshLocationTimerAsync() {
        Disposable newDisposable = startRefreshLocationTimerAsync(false).subscribe();
        Disposable oldDisposable = this.backgroundRefreshDisposable.getAndSet(newDisposable);
        if (oldDisposable != null && !oldDisposable.isDisposed()) {
            oldDisposable.dispose();
        }
    }

    private Mono<Void> startRefreshLocationTimerAsync(boolean initialization) {

        if (this.isClosed) {
            logger.debug("startRefreshLocationTimerAsync: nothing to do, it is closed");
            // if client is already closed, nothing to be done, just return.
            return Mono.empty();
        }

        LocalDateTime now = LocalDateTime.now();

        // Add jitter to the background refresh interval to prevent many CosmosClient
        // instances from refreshing simultaneously and overwhelming the compute gateway.
        int jitterInSeconds = (initialization || this.backgroundRefreshJitterMaxInSeconds <= 0)
            ? 0
            : ThreadLocalRandom.current().nextInt(0, this.backgroundRefreshJitterMaxInSeconds + 1);
        int delayInMillis = initialization ? 0 : this.backgroundRefreshLocationTimeIntervalInMS + (jitterInSeconds * 1000);

        this.refreshInBackground.set(true);

        logger.debug("Background refresh scheduled with delay [{}] ms (base [{}] ms + jitter [{}] s)",
            delayInMillis, this.backgroundRefreshLocationTimeIntervalInMS, jitterInSeconds);

        return Mono.delay(Duration.ofMillis(delayInMillis), CosmosSchedulers.COSMOS_PARALLEL)
                .flatMap(
                        t -> {
                            if (this.isClosed) {
                                logger.info("client already closed");
                                // if client is already closed, nothing to be done, just return.
                                return Mono.empty();
                            }

                            logger.debug("startRefreshLocationTimerAsync() - Invoking refresh, I was registered on [{}]", now);
                            Mono<DatabaseAccount> databaseAccountObs = GlobalEndpointManager.getDatabaseAccountFromAnyLocationsAsync(this.defaultEndpoint, new ArrayList<>(this.getEffectivePreferredRegions()),
                                    this::getDatabaseAccountAsync);

                            return databaseAccountObs.flatMap(dbAccount -> {
                                logger.info("db account retrieved {}", dbAccount);
                                this.refreshInBackground.set(false);
                                return this.refreshLocationPrivateAsync(dbAccount);
                            });
                        }).onErrorResume(ex -> {
                    logger.error("startRefreshLocationTimerAsync() - Unable to refresh database account from any location. Exception: {}", ex.toString(), ex);
                    this.setLatestDatabaseRefreshError(ex);

                    this.startRefreshLocationTimerAsync();
                    return Mono.empty();
                }).subscribeOn(CosmosSchedulers.GLOBAL_ENDPOINT_MANAGER_BOUNDED_ELASTIC);
    }

    public boolean hasThinClientReadLocations() {
        return this.hasThinClientReadLocations.get();
    }

    /**
     * Resolves the current set of thin-client regional endpoints from the location cache. Exposed so
     * the client bootstrap ({@code RxDocumentClientImpl.init()}) can eagerly pre-warm the thin-client
     * HTTP/2 connection pool at client-build time. Returns an empty set when no thin-client region can
     * be resolved.
     */
    public Set<URI> getThinClientRegionalEndpoints() {
        return this.locationCache.getThinClientRegionalEndpoints();
    }

    /**
     * Wires the thin-client HTTP/2 {@link HttpClient} used by the connectivity-probe
     * probeClient. Must be invoked by the client bootstrap before {@link #init()} so
     * that the very first topology refresh can issue probes.
     */
    public void setThinClientHttpClient(HttpClient httpClient) {
        if (httpClient == null) {
            return;
        }
        try {
            this.thinClientProbeClient.compareAndSet(null, new EndpointProbeClient(httpClient));
        } catch (Throwable t) {
            // Probe wiring must never trip CosmosClient initialization. If the probe client
            // can't be constructed for any reason, leave it null — `getProxyProbeDecision()`
            // then renders no decision (null) and routing behaves as if no probe were wired.
            logger.warn("Failed to wire thin-client connectivity-probe client; thin-client routing will proceed without probe gating.", t);
        }
    }

    /**
     * Returns the thin-client connectivity-probe's current routing decision as a tri-state:
     * <ul>
     *   <li>{@code null} &mdash; <b>no decision</b>: no probe client is wired (the client either
     *       explicitly opted into/out of thin-client, or is not a thin-client client, so no probe
     *       runs). The caller must treat this as neither healthy nor unhealthy and leave the routing
     *       decision to the other gate inputs.</li>
     *   <li>{@code TRUE} &mdash; an active probe considers the proxy fleet routable.</li>
     *   <li>{@code FALSE} &mdash; an active probe is gating traffic to Gateway V1 until its regions are proven.</li>
     * </ul>
     * Only an active, wired probe can return a non-null decision.
     */
    public Boolean getProxyProbeDecision() {
        EndpointProbeClient probeClient = this.thinClientProbeClient.get();
        if (probeClient == null) {
            // No probe wired -> no decision can be rendered.
            return null;
        }
        return probeClient.isThinClientRoutable();
    }

    private Mono<Void> runThinClientProbeCycleMono() {
        return Mono.defer(() -> {
            EndpointProbeClient probeClient = this.thinClientProbeClient.get();
            if (probeClient == null) {
                return Mono.empty();
            }
            if (!this.hasThinClientReadLocations.get()) {
                return Mono.empty();
            }
            // Resolve the current thin-client regional endpoints. An empty set (e.g.
            // hasThinClientReadLocations is true but LocationCache cannot normalize-match a single
            // thin-client region) is passed straight through: runProbeCycle adopts it and the routing
            // gate goes RED, so the SDK falls back to Gateway V1 until resolution recovers.
            Set<URI> endpoints = this.locationCache.getThinClientRegionalEndpoints();
            // Chained into the topology-refresh reactor pipeline. Cancellation propagates
            // through the outer subscription (disposed in close() via backgroundRefreshDisposable).
            // The probe client's internal single-flight CAS guarantees only one cycle runs at
            // a time; runProbeCycle absorbs all per-probe errors and never errors the Mono.
            return probeClient
                .runProbeCycle(endpoints)
                .subscribeOn(CosmosSchedulers.GLOBAL_ENDPOINT_MANAGER_BOUNDED_ELASTIC)
                .doOnNext(healthy -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Thin-client probe cycle completed; proxyHealthy={}", healthy);
                    }
                })
                .then();
        }).onErrorResume(t -> {
            // Defensive: probe issues must never bubble out and fail topology refresh or
            // CosmosClient init. Log and move on — the gate stays at its current state.
            logger.warn("Thin-client probe cycle threw; ignoring to protect topology refresh.", t);
            return Mono.empty();
        });
    }

    private Mono<DatabaseAccount> getDatabaseAccountAsync(URI serviceEndpoint) {
        return this.owner.getDatabaseAccountFromEndpoint(serviceEndpoint)
            .doOnNext(databaseAccount -> {
                if(databaseAccount != null) {

                    this.databaseAccountWriteLock.lock();

                    try {
                        this.latestDatabaseAccount = databaseAccount;
                        Collection<DatabaseAccountLocation> thinClientReadLocations =
                                databaseAccount.getThinClientReadableLocations();
                        this.hasThinClientReadLocations.set(thinClientReadLocations != null && !thinClientReadLocations.isEmpty());

                        Boolean currentPerPartitionAutomaticFailoverEnabledFromService =
                            databaseAccount.isPerPartitionFailoverBehaviorEnabled();

                        if (currentPerPartitionAutomaticFailoverEnabledFromService != null) {
                            boolean newVal = currentPerPartitionAutomaticFailoverEnabledFromService;
                            // Attempt to flip only if the value actually changes.
                            if (this.lastRecordedPerPartitionAutomaticFailoverEnabledOnClient
                                .compareAndSet(!newVal, newVal)) {
                                if (this.perPartitionAutomaticFailoverConfigModifier != null) {
                                    logger.info("ATTN: Per partition automatic failover enabled: {}, applying modifier",
                                        currentPerPartitionAutomaticFailoverEnabledFromService);
                                    this.perPartitionAutomaticFailoverConfigModifier.accept(databaseAccount);
                                }
                            }
                        }

                        this.setLatestDatabaseRefreshError(null);
                    } finally {
                        this.databaseAccountWriteLock.unlock();
                    }
                }

                logger.debug("account retrieved: {}", databaseAccount);
            }).single();
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    public String getRegionName(URI locationEndpoint, OperationType operationType) {
        return this.locationCache.getRegionName(locationEndpoint, operationType);
    }

    public String getRegionName(URI locationEndpoint, OperationType operationType, boolean isPerPartitionAutomaticFailoverEnabledAndWriteRequest) {
        return this.locationCache.getRegionName(locationEndpoint, operationType, isPerPartitionAutomaticFailoverEnabledAndWriteRequest);
    }

    public ConnectionPolicy getConnectionPolicy() {
        return this.connectionPolicy;
    }

    private List<String> getEffectivePreferredRegions() {

        if (this.connectionPolicy.getPreferredRegions() != null && !this.connectionPolicy.getPreferredRegions().isEmpty()) {
            return this.connectionPolicy.getPreferredRegions();
        }

        // when latestDatabaseAccount is initialized
        // the locationCache reflects account-level region information

        this.databaseAccountReadLock.lock();

        try {
            if (this.latestDatabaseAccount == null) {
                return Collections.emptyList();
            }
            return this.locationCache.getEffectivePreferredLocations();
        } finally {
            this.databaseAccountReadLock.unlock();
        }
    }

    public void setPerPartitionAutomaticFailoverConfigModifier(Consumer<DatabaseAccount> perPartitionAutomaticFailoverConfigModifier) {
        this.perPartitionAutomaticFailoverConfigModifier = perPartitionAutomaticFailoverConfigModifier;
    }

    public boolean getNRegionSynchronousCommitEnabled() {
        this.databaseAccountReadLock.lock();
        try {
            if (this.latestDatabaseAccount == null) {
                return false;
            }
            return this.latestDatabaseAccount.isNRegionSynchronousCommitEnabled();
        } finally {
            this.databaseAccountReadLock.unlock();
        }
    }
}
