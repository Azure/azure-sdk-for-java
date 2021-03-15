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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Endpoint region cache manager implementation. Supports cross region address routing based on
 * availability and preference list.
 */
public class GlobalEndpointManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(GlobalEndpointManager.class);

    private final int backgroundRefreshLocationTimeIntervalInMS;
    private final LocationCache locationCache;
    private final URI defaultEndpoint;
    private final ConnectionPolicy connectionPolicy;
    private final Duration maxInitializationTime;
    private final DatabaseAccountManagerInternal owner;
    private final AtomicBoolean isRefreshing;
    private final AtomicBoolean refreshInBackground;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Scheduler scheduler = Schedulers.fromExecutor(executor);
    private volatile boolean isClosed;
    private AtomicBoolean firstTimeDatabaseAccountInitialization = new AtomicBoolean(true);
    private volatile DatabaseAccount latestDatabaseAccount;

    public GlobalEndpointManager(DatabaseAccountManagerInternal owner, ConnectionPolicy connectionPolicy, Configs configs)  {
        this.backgroundRefreshLocationTimeIntervalInMS = configs.getUnavailableLocationsExpirationTimeInSeconds() * 1000;
        this.maxInitializationTime = Duration.ofSeconds(configs.getGlobalEndpointManagerMaxInitializationTimeInSeconds());
        try {
            this.locationCache = new LocationCache(
                    new ArrayList<>(connectionPolicy.getPreferredRegions() != null ?
                            connectionPolicy.getPreferredRegions():
                            Collections.emptyList()
                    ),
                    owner.getServiceEndpoint(),
                    connectionPolicy.isEndpointDiscoveryEnabled(),
                    connectionPolicy.isMultipleWriteRegionsEnabled(),
                    configs);

            this.owner = owner;
            this.defaultEndpoint = owner.getServiceEndpoint();
            this.connectionPolicy = connectionPolicy;

            this.isRefreshing = new AtomicBoolean(false);
            this.refreshInBackground = new AtomicBoolean(false);
            this.isClosed = false;
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
        return this.locationCache.resolveServiceEndpoint(request);
    }

    public void markEndpointUnavailableForRead(URI endpoint) {
        logger.debug("Marking endpoint {} unavailable for read",endpoint);
        this.locationCache.markEndpointUnavailableForRead(endpoint);;
    }

    public void markEndpointUnavailableForWrite(URI endpoint) {
        logger.debug("Marking  endpoint {} unavailable for Write",endpoint);
        this.locationCache.markEndpointUnavailableForWrite(endpoint);
    }

    public boolean canUseMultipleWriteLocations(RxDocumentServiceRequest request) {
        return this.locationCache.canUseMultipleWriteLocations(request);
    }

    public void close() {
        this.isClosed = true;
        this.executor.shutdown();
        logger.debug("GlobalEndpointManager closed.");
    }

    public Mono<Void> refreshLocationAsync(DatabaseAccount databaseAccount, boolean forceRefresh) {
        return Mono.defer(() -> {
            logger.debug("refreshLocationAsync() invoked");

            if (forceRefresh) {
                Mono<DatabaseAccount> databaseAccountObs = getDatabaseAccountFromAnyLocationsAsync(
                    this.defaultEndpoint,
                    new ArrayList<>(this.connectionPolicy.getPreferredRegions()),
                    this::getDatabaseAccountAsync);

                return databaseAccountObs.map(dbAccount -> {
                    this.locationCache.onDatabaseAccountRead(dbAccount);
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
        return this.connectionPolicy.getPreferredRegions() != null ? this.connectionPolicy.getPreferredRegions().size() : 0;
    }

    private Mono<Void> refreshLocationPrivateAsync(DatabaseAccount databaseAccount) {
        return Mono.defer(() -> {
            logger.debug("refreshLocationPrivateAsync() refreshing locations");

            if (databaseAccount != null) {
                this.locationCache.onDatabaseAccountRead(databaseAccount);
            }

            Utils.ValueHolder<Boolean> canRefreshInBackground = new Utils.ValueHolder<>();
            if (this.locationCache.shouldRefreshEndpoints(canRefreshInBackground)) {
                logger.debug("shouldRefreshEndpoints: true");

                if (databaseAccount == null && !canRefreshInBackground.v) {
                    logger.debug("shouldRefreshEndpoints: can't be done in background");

                    Mono<DatabaseAccount> databaseAccountObs = getDatabaseAccountFromAnyLocationsAsync(
                            this.defaultEndpoint,
                            new ArrayList<>(this.connectionPolicy.getPreferredRegions()),
                            this::getDatabaseAccountAsync);

                    return databaseAccountObs.map(dbAccount -> {
                        this.locationCache.onDatabaseAccountRead(dbAccount);
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
                                logger.warn("client already closed");
                                // if client is already closed, nothing to be done, just return.
                                return Mono.empty();
                            }

                            logger.debug("startRefreshLocationTimerAsync() - Invoking refresh, I was registered on [{}]", now);
                            Mono<DatabaseAccount> databaseAccountObs = GlobalEndpointManager.getDatabaseAccountFromAnyLocationsAsync(this.defaultEndpoint, new ArrayList<>(this.connectionPolicy.getPreferredRegions()),
                                    this::getDatabaseAccountAsync);

                            return databaseAccountObs.flatMap(dbAccount -> {
                                logger.debug("db account retrieved");
                                this.refreshInBackground.set(false);
                                return this.refreshLocationPrivateAsync(dbAccount);
                            });
                        }).onErrorResume(ex -> {
                    logger.error("startRefreshLocationTimerAsync() - Unable to refresh database account from any location. Exception: {}", ex.toString(), ex);

                    this.startRefreshLocationTimerAsync();
                    return Mono.empty();
                }).subscribeOn(scheduler);
    }

    private Mono<DatabaseAccount> getDatabaseAccountAsync(URI serviceEndpoint) {
        return this.owner.getDatabaseAccountFromEndpoint(serviceEndpoint)
            .doOnNext(databaseAccount -> {
                if(databaseAccount != null) {
                    this.latestDatabaseAccount = databaseAccount;
                }

                logger.debug("account retrieved: {}", databaseAccount);
            }).single();
    }

    public boolean isClosed() {
        return this.isClosed;
    }
}
