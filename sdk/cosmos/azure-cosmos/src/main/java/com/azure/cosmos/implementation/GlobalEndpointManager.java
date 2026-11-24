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
    private final AtomicReference<Disposable> thinClientProbeCycleDisposable = new AtomicReference<>();

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
        // Cancel any in-flight fire-and-forget thin-client probe cycle subscription.
        Disposable probeCycleDisposable = this.thinClientProbeCycleDisposable.getAndSet(null);
        if (probeCycleDisposable != null && !probeCycleDisposable.isDisposed()) {
            probeCycleDisposable.dispose();
        }
        // Flip the probe client's closed guard so any in-flight cycle drops its result.
        EndpointProbeClient probeClient = this.thinClientProbeClient.getAndSet(null);
        if (probeClient != null) {
            try {
                probeClient.close();
            } catch (Throwable t) {
                // Closing the probe client must never fail GlobalEndpointManager.close().
                logger.debug("Failed to close thin-client connectivity-probe client during close(); ignoring.", t);
            }
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
                })
                // Force-refresh updates topology but bypasses refreshLocationPrivateAsync, so fire
                // the probe here too (fire-and-forget; routing stays on Gateway V1 until proven).
                .doOnNext(ignored -> this.fireThinClientProbeCycle())
                .then();
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
        return Mono.defer(() -> {
            logger.debug("refreshLocationPrivateAsync() refreshing locations");

            if (databaseAccount != null) {
                this.databaseAccountWriteLock.lock();

                try {
                    this.locationCache.onDatabaseAccountRead(databaseAccount);
                } finally {
                    this.databaseAccountWriteLock.unlock();
                }

                // Fire-and-forget the delta-gated thin-client connectivity-probe cycle on every
                // topology refresh so the routing gate (getProxyProbeDecision) can converge to the
                // proxy fleet — without blocking init() (which .block()s this pipeline) or the
                // background refresh loop on the probe. Routing stays on Gateway V1 until proven.
                this.fireThinClientProbeCycle();
            }

            return Mono.defer(() -> {
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
                            this.fireThinClientProbeCycle();
                            return Mono.<Void>empty();
                        }).then(Mono.defer(() -> {
                            // trigger a startRefreshLocationTimerAsync don't wait on it.
                            if (!this.refreshInBackground.get()) {
                                this.startRefreshLocationTimerAsync();
                            }
                            return Mono.empty();
                        }));
                    }

                    // trigger a startRefreshLocationTimerAsync don't wait on it.
                    if (!this.refreshInBackground.get()) {
                        this.startRefreshLocationTimerAsync();
                    }

                    this.isRefreshing.set(false);
                    return Mono.empty();
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
                    return Mono.empty();
                }
            });
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
     * Wires the thin-client HTTP/2 {@link HttpClient} for the connectivity probe. Must be called
     * before {@link #init()} so the first topology refresh can issue probes.
     */
    public void setThinClientHttpClient(HttpClient httpClient) {
        if (httpClient == null) {
            return;
        }
        try {
            this.thinClientProbeClient.compareAndSet(null, new EndpointProbeClient(httpClient));
        } catch (Throwable t) {
            // Probe wiring must never trip init(); leaving it null makes getProxyProbeDecision() null.
            logger.warn("Failed to wire thin-client connectivity-probe client; thin-client routing will proceed without probe gating.", t);
        }
    }

    /**
     * Returns the probe's tri-state routing decision: {@code null} when no probe is wired (leave the
     * decision to other gate inputs), {@code TRUE} when the proxy fleet is routable, {@code FALSE}
     * while gating to Gateway V1 until regions are proven.
     */
    public Boolean getProxyProbeDecision() {
        EndpointProbeClient probeClient = this.thinClientProbeClient.get();
        if (probeClient == null) {
            // No probe wired -> no decision can be rendered.
            return null;
        }
        return probeClient.isThinClientRoutable();
    }

    /**
     * Fires the delta-gated thin-client probe cycle fire-and-forget so the outer refresh/retry/init
     * never blocks on it. Routing stays on Gateway V1 until the probe proves the proxy endpoints
     * (conservative-until-proven), then {@link #getProxyProbeDecision()} flips the gate. The live
     * cycle's subscription is tracked in {@link #thinClientProbeCycleDisposable} for {@link #close()}
     * to cancel; the probe client's single-flight CAS dedups overlapping fires to no-ops, and this
     * method keeps the live predecessor rather than tracking those no-ops.
     */
    private void fireThinClientProbeCycle() {
        Disposable newDisposable = this.runThinClientProbeCycleMono()
            .subscribe(
                ignored -> { },
                t -> logger.warn("Thin-client probe cycle subscription errored unexpectedly; ignoring.", t));
        // close() cancels the tracked subscription to promptly stop the ACTIVE probe I/O (the probe
        // client's own close() only flips a flag; in-flight probes otherwise self-terminate on the
        // per-probe timeout). So the tracked handle must always be the real in-flight cycle, never a
        // no-op trigger: while a cycle is running, EndpointProbeClient's single-flight CAS forces every
        // concurrent fire onto the no-op path, so THIS subscription is the no-op and the already-tracked
        // one is the real cycle. Keep the live predecessor and let this no-op self-complete; only
        // install the new handle when nothing live is tracked (it won the single-flight and is the real
        // cycle). Firing on every refresh still runs runThinClientProbeCycleMono -> runProbeCycle, which
        // republishes the latest topology and recomputes the gate even on the no-op path, so a topology
        // delta arriving mid-cycle is never lost. We never dispose the predecessor here (only close()
        // does), so an active cycle is never aborted.
        this.thinClientProbeCycleDisposable.getAndUpdate(
            previous -> (previous != null && !previous.isDisposed()) ? previous : newDisposable);
    }

    private Mono<Void> runThinClientProbeCycleMono() {
        return Mono.defer(() -> {
            // No-op when COSMOS.THINCLIENT_ENABLED is explicitly set (true or false): the flag is then
            // a hard contract that decides routing directly, so the probe verdict is irrelevant and we
            // skip the traffic. Read live here, so dropping the opt-out/opt-in back to unset resumes
            // probing on the next refresh. Only an unset flag lets the probe gate routing.
            if (Configs.isThinClientEnabled() != null) {
                return Mono.empty();
            }
            EndpointProbeClient probeClient = this.thinClientProbeClient.get();
            if (probeClient == null) {
                return Mono.empty();
            }
            if (!this.hasThinClientReadLocations.get()) {
                return Mono.empty();
            }
            // Empty set (endpoints unresolved) is passed through: the gate goes RED / Gateway V1.
            Set<URI> endpoints = this.locationCache.getThinClientRegionalEndpointsEligibleForProbe();
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
            // Probe issues must never fail topology refresh / init; keep the gate at its current state.
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
