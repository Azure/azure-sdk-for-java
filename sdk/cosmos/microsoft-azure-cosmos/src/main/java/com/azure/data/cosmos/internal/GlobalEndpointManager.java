// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.internal.routing.LocationCache;
import com.azure.data.cosmos.internal.routing.LocationHelper;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URISyntaxException;
import java.net.URL;
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
    private final URL defaultEndpoint;
    private final ConnectionPolicy connectionPolicy;
    private final DatabaseAccountManagerInternal owner;
    private final AtomicBoolean isRefreshing;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Scheduler scheduler = Schedulers.fromExecutor(executor);
    private volatile boolean isClosed;

    public GlobalEndpointManager(DatabaseAccountManagerInternal owner, ConnectionPolicy connectionPolicy, Configs configs)  {
        this.backgroundRefreshLocationTimeIntervalInMS = configs.getUnavailableLocationsExpirationTimeInSeconds() * 1000;
        try {
            this.locationCache = new LocationCache(
                    new ArrayList<>(connectionPolicy.preferredLocations() != null ?
                            connectionPolicy.preferredLocations():
                            Collections.emptyList()
                    ),
                    owner.getServiceEndpoint().toURL(),
                    connectionPolicy.enableEndpointDiscovery(),
                    BridgeInternal.getUseMultipleWriteLocations(connectionPolicy),
                    configs);

            this.owner = owner;
            this.defaultEndpoint = owner.getServiceEndpoint().toURL();
            this.connectionPolicy = connectionPolicy;

            this.isRefreshing = new AtomicBoolean(false);
            this.isClosed = false;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void init() {
        // TODO: add support for openAsync
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/332589
        startRefreshLocationTimerAsync(true).block();
    }

    public UnmodifiableList<URL> getReadEndpoints() {
        // readonly
        return this.locationCache.getReadEndpoints();
    }

    public UnmodifiableList<URL> getWriteEndpoints() {
        //readonly
        return this.locationCache.getWriteEndpoints();
    }

    public static Mono<DatabaseAccount> getDatabaseAccountFromAnyLocationsAsync(
            URL defaultEndpoint, List<String> locations, Function<URL, Mono<DatabaseAccount>> getDatabaseAccountFn) {

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

    public URL resolveServiceEndpoint(RxDocumentServiceRequest request) {
        return this.locationCache.resolveServiceEndpoint(request);
    }

    public void markEndpointUnavailableForRead(URL endpoint) {
        logger.debug("Marking endpoint {} unavailable for read",endpoint);
        this.locationCache.markEndpointUnavailableForRead(endpoint);;
    }

    public void markEndpointUnavailableForWrite(URL endpoint) {
        logger.debug("Marking  endpoint {} unavailable for Write",endpoint);
        this.locationCache.markEndpointUnavailableForWrite(endpoint);
    }

    public boolean CanUseMultipleWriteLocations(RxDocumentServiceRequest request) {
        return this.locationCache.canUseMultipleWriteLocations(request);
    }

    public void close() {
        this.isClosed = true;
        this.executor.shutdown();
        logger.debug("GlobalEndpointManager closed.");
    }

    public Mono<Void> refreshLocationAsync(DatabaseAccount databaseAccount) {
        return Mono.defer(() -> {
            logger.debug("refreshLocationAsync() invoked");
            if (!isRefreshing.compareAndSet(false, true)) {
                logger.debug("in the middle of another refresh. Not invoking a new refresh.");
                return Mono.empty();
            }

            logger.debug("will refresh");
            return this.refreshLocationPrivateAsync(databaseAccount).doOnError(e -> this.isRefreshing.set(false));
        });
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
                            new ArrayList<>(this.connectionPolicy.preferredLocations()),
                            this::getDatabaseAccountAsync);

                    return databaseAccountObs.map(dbAccount -> {
                        this.locationCache.onDatabaseAccountRead(dbAccount);
                        return dbAccount;
                    }).flatMap(dbAccount -> {
                        // trigger a startRefreshLocationTimerAsync don't wait on it.
                        this.startRefreshLocationTimerAsync();
                        return Mono.empty();
                    });
                }

                // trigger a startRefreshLocationTimerAsync don't wait on it.
                this.startRefreshLocationTimerAsync();

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

        return Mono.delay(Duration.ofMillis(delayInMillis))
                .flatMap(
                        t -> {
                            if (this.isClosed) {
                                logger.warn("client already closed");
                                // if client is already closed, nothing to be done, just return.
                                return Mono.empty();
                            }

                            logger.debug("startRefreshLocationTimerAsync() - Invoking refresh, I was registered on [{}]", now);
                            Mono<DatabaseAccount> databaseAccountObs = GlobalEndpointManager.getDatabaseAccountFromAnyLocationsAsync(this.defaultEndpoint, new ArrayList<>(this.connectionPolicy.preferredLocations()),
                                    this::getDatabaseAccountAsync);

                            return databaseAccountObs.flatMap(dbAccount -> {
                                logger.debug("db account retrieved");
                                return this.refreshLocationPrivateAsync(dbAccount);
                            });
                        }).onErrorResume(ex -> {
                    logger.error("startRefreshLocationTimerAsync() - Unable to refresh database account from any location. Exception: {}", ex.toString(), ex);

                    this.startRefreshLocationTimerAsync();
                    return Mono.empty();
                }).subscribeOn(scheduler);
    }

    private Mono<DatabaseAccount> getDatabaseAccountAsync(URL serviceEndpoint) {
        try {
            return this.owner.getDatabaseAccountFromEndpoint(serviceEndpoint.toURI())
                    .doOnNext(i -> logger.debug("account retrieved: {}", i)).single();
        } catch (URISyntaxException e) {
            return Mono.error(e);
        }
    }

    public boolean isClosed() {
        return this.isClosed;
    }
}
