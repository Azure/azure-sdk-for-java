// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.routing.LocationCache;
import com.azure.cosmos.implementation.routing.LocationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Endpoint region cache manager implementation. Supports cross region address routing based on
 * availability and preference list.
 */
public class GlobalEndpointManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(GlobalEndpointManager.class);

    private static final CosmosDaemonThreadFactory theadFactory = new CosmosDaemonThreadFactory("cosmos-global-endpoint-mgr");

    private final int backgroundRefreshLocationTimeIntervalInMS;
    private final LocationCache locationCache;
    private final URI defaultEndpoint;
    private final ConnectionPolicy connectionPolicy;
    private final Duration maxInitializationTime;
    private final DatabaseAccountManagerInternal owner;
    private final AtomicBoolean isRefreshing;
    private final AtomicBoolean refreshInBackground;
    private final Scheduler scheduler = Schedulers.newSingle(theadFactory);
    private volatile boolean isClosed;
    private AtomicBoolean firstTimeDatabaseAccountInitialization = new AtomicBoolean(true);
    private volatile DatabaseAccount latestDatabaseAccount;

    private final ReentrantReadWriteLock.WriteLock databaseAccountWriteLock;

    private final ReentrantReadWriteLock.ReadLock databaseAccountReadLock;

    private volatile Throwable latestDatabaseRefreshError;

    public void setLatestDatabaseRefreshError(Throwable latestDatabaseRefreshError) {
        this.latestDatabaseRefreshError = latestDatabaseRefreshError;
    }
    public Throwable getLatestDatabaseRefreshError() {
        return latestDatabaseRefreshError;
    }

    public GlobalEndpointManager(DatabaseAccountManagerInternal owner, ConnectionPolicy connectionPolicy, Configs configs)  {
        this.backgroundRefreshLocationTimeIntervalInMS = configs.getUnavailableLocationsExpirationTimeInSeconds() * 1000;
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
        // TODO: add support for openAsync
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/332589
        startRefreshLocationTimerAsync(true).block(maxInitializationTime);
    }

    public UnmodifiableList<URI> getReadEndpoints() {
        // readonly
        return this.locationCache.getReadEndpoints();
    }

    public UnmodifiableList<URI> getWriteEndpoints() {
        //readonly
        return this.locationCache.getWriteEndpoints();
    }

    public UnmodifiableList<URI> getApplicableReadEndpoints(RxDocumentServiceRequest request) {
        // readonly
        return this.locationCache.getApplicableReadEndpoints(request);
    }

    public UnmodifiableList<URI> getApplicableWriteEndpoints(RxDocumentServiceRequest request) {
        //readonly
        return this.locationCache.getApplicableWriteEndpoints(request);
    }

    public UnmodifiableList<URI> getApplicableReadEndpoints(List<String> excludedRegions) {
        // readonly
        return this.locationCache.getApplicableReadEndpoints(excludedRegions, Collections.emptyList());
    }

    public UnmodifiableList<URI> getApplicableWriteEndpoints(List<String> excludedRegions) {
        //readonly
        return this.locationCache.getApplicableWriteEndpoints(excludedRegions, Collections.emptyList());
    }

    public List<URI> getAvailableReadEndpoints() {
        return this.locationCache.getAvailableReadEndpoints();
    }

    public List<URI> getAvailableWriteEndpoints() {
        return this.locationCache.getAvailableWriteEndpoints();
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

    public URI resolveServiceEndpoint(RxDocumentServiceRequest request) {
        URI serviceEndpoint = this.locationCache.resolveServiceEndpoint(request);
        if (request.faultInjectionRequestContext != null) {
            request.faultInjectionRequestContext.setLocationEndpointToRoute(serviceEndpoint);
        }

        return serviceEndpoint;
    }

    public URI resolveFaultInjectionServiceEndpoint(String region, boolean writeOnly) {
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
        this.scheduler.dispose();
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
                    return Mono.empty();
                });
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
            }

            Utils.ValueHolder<Boolean> canRefreshInBackground = new Utils.ValueHolder<>();
            if (this.locationCache.shouldRefreshEndpoints(canRefreshInBackground)) {
                logger.debug("shouldRefreshEndpoints: true");

                if (databaseAccount == null && !canRefreshInBackground.v) {
                    logger.debug("shouldRefreshEndpoints: can't be done in background");

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

                        this.isRefreshing.set(false);
                        return dbAccount;
                    }).flatMap(dbAccount -> {
                        // trigger a startRefreshLocationTimerAsync don't wait on it.
                        if (!this.refreshInBackground.get()) {
                            this.startRefreshLocationTimerAsync();
                        }
                        return Mono.empty();
                    });
                }

                // trigger a startRefreshLocationTimerAsync don't wait on it.
                if (!this.refreshInBackground.get()) {
                    this.startRefreshLocationTimerAsync();
                }

                this.isRefreshing.set(false);
                return Mono.empty();
            } else {
                logger.debug("shouldRefreshEndpoints: false, nothing to do.");
                this.isRefreshing.set(false);
                return Mono.empty();
            }
        });
    }

    private void startRefreshLocationTimerAsync() {
        startRefreshLocationTimerAsync(false).subscribe();
    }

    private Mono<Void> startRefreshLocationTimerAsync(boolean initialization) {

        if (this.isClosed) {
            logger.debug("startRefreshLocationTimerAsync: nothing to do, it is closed");
            // if client is already closed, nothing to be done, just return.
            return Mono.empty();
        }

        logger.debug("registering a refresh in [{}] ms", this.backgroundRefreshLocationTimeIntervalInMS);
        LocalDateTime now = LocalDateTime.now();

        int delayInMillis = initialization ? 0: this.backgroundRefreshLocationTimeIntervalInMS;

        this.refreshInBackground.set(true);

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
                }).subscribeOn(scheduler);
    }

    private Mono<DatabaseAccount> getDatabaseAccountAsync(URI serviceEndpoint) {
        return this.owner.getDatabaseAccountFromEndpoint(serviceEndpoint)
            .doOnNext(databaseAccount -> {
                if(databaseAccount != null) {

                    this.databaseAccountWriteLock.lock();

                    try {
                        this.latestDatabaseAccount = databaseAccount;
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
}
