/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.DatabaseAccount;
import com.azure.data.cosmos.DatabaseAccountManagerInternal;
import com.azure.data.cosmos.internal.routing.LocationCache;
import com.azure.data.cosmos.internal.routing.LocationHelper;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final Scheduler scheduler = Schedulers.from(executor);
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
        startRefreshLocationTimerAsync(true).toCompletable().await();
    }

    public UnmodifiableList<URL> getReadEndpoints() {
        // readonly
        return this.locationCache.getReadEndpoints();
    }

    public UnmodifiableList<URL> getWriteEndpoints() {
        //readonly
        return this.locationCache.getWriteEndpoints();
    }

    public static Single<DatabaseAccount> getDatabaseAccountFromAnyLocationsAsync(
            URL defaultEndpoint, List<String> locations, Func1<URL, Single<DatabaseAccount>> getDatabaseAccountFn) {

        return getDatabaseAccountFn.call(defaultEndpoint).onErrorResumeNext(
                e -> {
                    logger.error("Fail to reach global gateway [{}], [{}]", defaultEndpoint, e.getMessage());
                    if (locations.isEmpty()) {
                        return Single.error(e);
                    }

                    Observable<Observable<DatabaseAccount>> obs = Observable.range(0, locations.size())
                            .map(index -> getDatabaseAccountFn.call(LocationHelper.getLocationEndpoint(defaultEndpoint, locations.get(index))).toObservable());

                    // iterate and get the database account from the first non failure, otherwise get the last error.
                    Observable<DatabaseAccount> res = Observable.concatDelayError(obs).first().single();
                    return res.toSingle().doOnError(
                            innerE -> {
                                logger.error("Fail to reach location any of locations", String.join(",", locations), innerE.getMessage());
                            });
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

    public Completable refreshLocationAsync(DatabaseAccount databaseAccount) {
        return Completable.defer(() -> {
            logger.debug("refreshLocationAsync() invoked");
            if (!isRefreshing.compareAndSet(false, true)) {
                logger.debug("in the middle of another refresh. Not invoking a new refresh.");
                return Completable.complete();
            }

            logger.debug("will refresh");
            return this.refreshLocationPrivateAsync(databaseAccount).doOnError(e -> this.isRefreshing.set(false));
        });
    }

    private Completable refreshLocationPrivateAsync(DatabaseAccount databaseAccount) {
        return Completable.defer(() -> {
            logger.debug("refreshLocationPrivateAsync() refreshing locations");

            if (databaseAccount != null) {
                this.locationCache.onDatabaseAccountRead(databaseAccount);
            }

            Utils.ValueHolder<Boolean> canRefreshInBackground = new Utils.ValueHolder();
            if (this.locationCache.shouldRefreshEndpoints(canRefreshInBackground)) {
                logger.debug("shouldRefreshEndpoints: true");

                if (databaseAccount == null && !canRefreshInBackground.v) {
                    logger.debug("shouldRefreshEndpoints: can't be done in background");

                    Single<DatabaseAccount> databaseAccountObs = getDatabaseAccountFromAnyLocationsAsync(
                            this.defaultEndpoint,
                            new ArrayList<>(this.connectionPolicy.preferredLocations()),
                            url -> this.getDatabaseAccountAsync(url));

                    return databaseAccountObs.map(dbAccount -> {
                        this.locationCache.onDatabaseAccountRead(dbAccount);
                        return dbAccount;
                    }).flatMapCompletable(dbAccount -> {
                        // trigger a startRefreshLocationTimerAsync don't wait on it.
                        this.startRefreshLocationTimerAsync();
                        return Completable.complete();
                    });
                }

                // trigger a startRefreshLocationTimerAsync don't wait on it.
                this.startRefreshLocationTimerAsync();

                return Completable.complete();
            } else {
                logger.debug("shouldRefreshEndpoints: false, nothing to do.");
                this.isRefreshing.set(false);
                return Completable.complete();
            }
        });
    }

    private void startRefreshLocationTimerAsync() {
        startRefreshLocationTimerAsync(false).subscribe();
    }

    private Observable startRefreshLocationTimerAsync(boolean initialization) {

        if (this.isClosed) {
            logger.debug("startRefreshLocationTimerAsync: nothing to do, it is closed");
            // if client is already closed, nothing to be done, just return.
            return Observable.empty();
        }

        logger.debug("registering a refresh in [{}] ms", this.backgroundRefreshLocationTimeIntervalInMS);
        LocalDateTime now = LocalDateTime.now();

        int delayInMillis = initialization ? 0: this.backgroundRefreshLocationTimeIntervalInMS;

        return Observable.timer(delayInMillis, TimeUnit.MILLISECONDS)
                .toSingle().flatMapCompletable(
                        t -> {
                            if (this.isClosed) {
                                logger.warn("client already closed");
                                // if client is already closed, nothing to be done, just return.
                                return Completable.complete();
                            }

                            logger.debug("startRefreshLocationTimerAsync() - Invoking refresh, I was registered on [{}]", now);
                            Single<DatabaseAccount> databaseAccountObs = GlobalEndpointManager.getDatabaseAccountFromAnyLocationsAsync(this.defaultEndpoint, new ArrayList<>(this.connectionPolicy.preferredLocations()),
                                    url -> this.getDatabaseAccountAsync(url)).toObservable().toSingle();

                            return databaseAccountObs.flatMapCompletable(dbAccount -> {
                                logger.debug("db account retrieved");
                                return this.refreshLocationPrivateAsync(dbAccount);
                            });
                        }).onErrorResumeNext(ex -> {
                    logger.error("startRefreshLocationTimerAsync() - Unable to refresh database account from any location. Exception: {}", ex.toString(), ex);

                    this.startRefreshLocationTimerAsync();
                    return Completable.complete();
                }).toObservable().subscribeOn(scheduler);
    }

    private Single<DatabaseAccount> getDatabaseAccountAsync(URL serviceEndpoint) {
        try {
            return this.owner.getDatabaseAccountFromEndpoint(serviceEndpoint.toURI())
                    .doOnNext(i -> logger.debug("account retrieved: {}", i)).toSingle();
        } catch (URISyntaxException e) {
            return Single.error(e);
        }
    }

    public boolean isClosed() {
        return this.isClosed;
    }
}
