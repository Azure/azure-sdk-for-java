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

package com.microsoft.azure.cosmosdb.rx.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.DatabaseAccount;
import com.microsoft.azure.cosmosdb.DatabaseAccountLocation;
import com.microsoft.azure.cosmosdb.internal.EndpointManager;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.Utils;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * This class implements the logic for endpoint management for geo-replicated
 * database accounts.
 * <p>
 * When ConnectionPolicy.getEnableEndpointDiscovery is true,
 * the GlobalEndpointManager will choose the correct endpoint to use for write
 * and read operations based on database account information retrieved from the
 * service in conjunction with user's preference as specified in
 * ConnectionPolicy().getPreferredLocations.
 */
class GlobalEndpointManager implements EndpointManager {
    private static final Logger logger = LoggerFactory.getLogger(GlobalEndpointManager.class);
    private final DatabaseAccountManagerInternal client;
    private final Collection<String> preferredLocations;
    private final boolean enableEndpointDiscovery;
    private final URI defaultEndpoint;
    private Map<String, URI> readableLocations;
    private Map<String, URI> writableLocations;
    private ConcurrentMap<String, Long> unavailableRegions;
    private URI currentWriteLocation;
    private URI currentReadLocation;
    private volatile boolean initialized;
    private volatile boolean refreshing;
    private boolean preferredLocationValid;
    private String mostPreferredRegion;
    private volatile boolean isClosed;
    private Subscription endpointCheckIntervalSubscription;

    private static final long DEFAULT_UNAVAILABLE_LOCATION_EXPIRATION_TIME = 5 * 60 * 1000;
    private static final long DEFAULT_BACKGROUND_REFRESH_LOCATION_TIME_INTERVAL_IN_MS = 5 * 60 * 1000;

    public GlobalEndpointManager(final RxDocumentClientImpl client) {
        this(new DatabaseAccountManagerInternal() {
            
            @Override
            public URI getServiceEndpoint() {
                return client.getServiceEndpoint();
            }
            
            @Override
            public Observable<DatabaseAccount> getDatabaseAccountFromEndpoint(URI endpoint) {
                logger.trace("Getting database account endpoint from {}", endpoint);
                return client.getDatabaseAccountFromEndpoint(endpoint);
            }
            
            @Override
            public ConnectionPolicy getConnectionPolicy() {
                return client.getConnectionPolicy();
            }
        });

        // Check if preferred location is provided
        this.preferredLocationValid = false;
        this.mostPreferredRegion = null;
        if (this.preferredLocations != null && this.preferredLocations.size() > 0) {
            String candidateRegion = this.preferredLocations.iterator().next();
            if (StringUtils.isNotEmpty(candidateRegion)) {
                this.preferredLocationValid = true;
                this.mostPreferredRegion = candidateRegion;
                logger.trace("Most preferred read region is {}", this.mostPreferredRegion);
            }
        }

        if (this.enableEndpointDiscovery) {
            this.setupPeriodicCheckAndRefreshEndpoint();
        }
    }

    public void close() {
        this.isClosed = true;
        if (this.endpointCheckIntervalSubscription != null && !this.endpointCheckIntervalSubscription.isUnsubscribed()) {
            this.endpointCheckIntervalSubscription.unsubscribe();
        }
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    private void setupPeriodicCheckAndRefreshEndpoint() {
        Observable<Long> endpointCheckIntervalOb = Observable.interval(
                DEFAULT_BACKGROUND_REFRESH_LOCATION_TIME_INTERVAL_IN_MS,
                TimeUnit.MILLISECONDS);
        this.endpointCheckIntervalSubscription = endpointCheckIntervalOb.subscribe(tick -> {
            if (this.preferredLocationValid &&
                    this.readableLocations != null &&
                    this.readableLocations.get(this.mostPreferredRegion) == null) {
                logger.trace("Current read region is not the most preferred region {}", this.mostPreferredRegion);
                this.refreshEndpointList();
            } else {
                logger.trace("Most preferred read region is active.");
            }
        });
    }

    public void markEndpointUnavailable() {
        if (!this.getReadEndpoint().equals(this.getWriteEndpoint())) {
            Long currentMs = System.currentTimeMillis();
            if (this.unavailableRegions.putIfAbsent(this.getReadEndpoint().toString(), currentMs) == null) {
                logger.debug("Added endpoint {} to unavailable regions with timestamp {}.", this.getReadEndpoint(), currentMs);
            } else {
                logger.trace("Endpoint {} is already in unavailable regions list.", this.getReadEndpoint());
            }
        } else {
            this.unavailableRegions.remove(this.getReadEndpoint().toString());
            logger.debug("Read endpoint is write endpoint {}, not marking as unavailable.", this.getReadEndpoint());
        }
    }

    private boolean checkAndUpdateIfEndpointIsUnavailable(String endpoint) {
        Long addedTimestamp = unavailableRegions.get(endpoint);
        if (addedTimestamp == null) {
            return false;
        } else if (System.currentTimeMillis() - addedTimestamp > DEFAULT_UNAVAILABLE_LOCATION_EXPIRATION_TIME) {
            if (unavailableRegions.remove(endpoint) != null) {
                logger.debug("Remove endpoint {} from unavailable endpoint", endpoint);
            }
            return false;
        } else {
            logger.debug("Endpoint {} is present in unavailable regions list", endpoint);
            return true;
        }
    }
    
    public GlobalEndpointManager(DatabaseAccountManagerInternal client) {
        this.client = client;
        this.preferredLocations = client.getConnectionPolicy().getPreferredLocations();
        this.enableEndpointDiscovery = client.getConnectionPolicy().getEnableEndpointDiscovery();
        this.defaultEndpoint = client.getServiceEndpoint();
        this.initialized = false;
        this.refreshing = false;
        this.unavailableRegions = new ConcurrentHashMap<>();
    }
    
    public URI getWriteEndpoint() {
        if (!initialized) {
            this.initialize();
        }

        return this.currentWriteLocation;
    }

    public URI getReadEndpoint() {
        if (!initialized) {
            this.initialize();
        }

        return this.currentReadLocation;
    }

    public URI resolveServiceEndpoint(OperationType operationType) {
        URI endpoint = null;

        if (Utils.isWriteOperation(operationType)) {
            endpoint = this.getWriteEndpoint();
        } else {
            endpoint = this.getReadEndpoint();
        }

        if (endpoint == null) {
            // Unable to resolve service endpoint through querying database account info.
            // use the value passed in by the user.
            endpoint = this.defaultEndpoint;
        }
        return endpoint;
    }

    /**
     * Refresh the endpoint list.
     * This method need to be synchronized to make sure we don't send get database account request multiple times
     * at once in fail over scenarios.
     */
    public synchronized void refreshEndpointList() {
        if (this.refreshing) {
            logger.trace("Endpoint list is being refreshed.");
            return;
        }

        this.refreshing = true;
        this.refreshEndpointListInternal().subscribe(new Subscriber<DatabaseAccount>() {
             @Override
             public void onCompleted() {
                logger.debug("Endpoint list has been updated.");
                refreshing = false;
             }

             @Override
             public void onError(Throwable e) {
                 logger.warn("refreshEndpointList has encountered an error: {}", e.getMessage(), e);
                 refreshing = false;
             }

             @Override
             public void onNext(DatabaseAccount o) {
             }
        });
    }

    public Observable<DatabaseAccount> getDatabaseAccountFromAnyEndpoint() {
        return this.client.getDatabaseAccountFromEndpoint(this.defaultEndpoint).flatMap(databaseAccount -> {
            // The global endpoint was not working. Try other endpoints in the preferred read region list.
            if (databaseAccount == null) {
                if (this.preferredLocations != null && this.preferredLocations.size() > 0) {
                    // Try to get the database account from all provided preferred regions
                    // Return the first non-null result if there is any, otherwise raise a failure
                    // This is achieved by concat all observables to get the database endpoints, and then do
                    // a firstOrDefault on the combined observable, with the default value of null, indicating that
                    // all requests to the preferred endpoints don't succeed.
                    Observable<DatabaseAccount> dbObs = Observable.empty();
                    for (String regionName : this.preferredLocations) {
                        URI regionalUri = this.getRegionalEndpoint(regionName);
                        if (regionalUri != null) {
                            dbObs = dbObs.concatWith(this.client.getDatabaseAccountFromEndpoint(regionalUri)
                                    .subscribeOn(Schedulers.io()));
                        }
                    }
                    return dbObs.firstOrDefault(null, Objects::nonNull).flatMap(db -> {
                        if (db != null) {
                            return Observable.just(db);
                        } else {
                            return Observable.error(new IllegalStateException("Attempted reading database account " +
                                    "from the preferred regions list but failed."));
                        }
                    });
                } else {
                    logger.warn("There was an issue with the global endpoint and the preferred locations are not provided.");
                }
            }
            if (databaseAccount != null) {
                logger.trace("Fetched database account: {}", databaseAccount);
                return Observable.just(databaseAccount);
            } else {
                return Observable.error(new IllegalStateException("Failed to read database account from all endpoints."));
            }
        });
    }

    private synchronized void initialize() {
        if (initialized) {
            return;
        }

        this.initialized = true;
        this.refreshEndpointListInternal().toBlocking().firstOrDefault(null);
        logger.trace("initialize has been completed.");
    }

    private Observable<DatabaseAccount> refreshEndpointListInternal() {
        if (!this.enableEndpointDiscovery) {
            logger.warn("Endpoint discovery is disabled. Skipping endpoint refresh.");
            return Observable.empty();
        }

        Map<String, URI> writableLocations = new HashMap<String, URI>();
        Map<String, URI> readableLocations = new HashMap<String, URI>();

        return this.getDatabaseAccountFromAnyEndpoint().flatMap(databaseAccount -> {
            if (databaseAccount != null) {
                if (databaseAccount.getWritableLocations() != null) {
                    for (DatabaseAccountLocation location : databaseAccount.getWritableLocations()) {
                        if (StringUtils.isNotEmpty(location.getName())) {
                            URI regionUri = null;
                            try {
                                regionUri = new URI(location.getEndpoint());
                            } catch (URISyntaxException e) {
                                logger.warn("Unexpected endpoint URI {}", location.getEndpoint());
                            }

                            if (regionUri != null) {
                                writableLocations.put(location.getName(), regionUri);
                            }
                        }
                    }
                }

                if (databaseAccount.getReadableLocations() != null) {
                    for (DatabaseAccountLocation location : databaseAccount.getReadableLocations()) {
                        if (StringUtils.isNotEmpty(location.getName()) &&
                                !this.checkAndUpdateIfEndpointIsUnavailable(location.getEndpoint())) {
                            URI regionUri = null;
                            try {
                                regionUri = new URI(location.getEndpoint());
                            } catch (URISyntaxException e) {
                                logger.warn("Unexpected endpoint URI {}", location.getEndpoint());
                            }

                            if (regionUri != null) {
                                readableLocations.put(location.getName(), regionUri);
                            }
                        }
                    }
                }

                this.updateEndpointsCache(writableLocations, readableLocations);
            }
            return Observable.just(databaseAccount);
        });
    }

    private void updateEndpointsCache(Map<String, URI> writableLocations,
                                                   Map<String, URI> readableLocations) {
        this.writableLocations = writableLocations;
        this.readableLocations = readableLocations;

        // If enableEndpointDiscovery is false, we will always use the default
        // value the user has set when creating the DocumentClient object.
        if (!this.enableEndpointDiscovery) {
            this.currentReadLocation = this.defaultEndpoint;
            this.currentWriteLocation = this.defaultEndpoint;
            return;
        }

        // If enableEndpointDiscovery is true, we will choose the first
        // writable region as the current write region, unless there is 
        // no writable region, in which case we'll use the default value.
        if (this.writableLocations.size() == 0) {
            this.currentWriteLocation = this.defaultEndpoint;
        } else {
            Iterator<Entry<String, URI>> iterator = this.writableLocations.entrySet().iterator();
            this.currentWriteLocation = iterator.next().getValue();
        }

        // If there is no readable region, or if there is no preferred
        // regions, we use the same region to read and write.
        URI newReadRegion = null;
        if (this.readableLocations.size() == 0) {
            // If there is no readable region available, we use the write region
            // for reads.
            newReadRegion = this.currentWriteLocation;
        } else {
            // If no preferred read regions are specified, set the read region the same
            // as the write region.
            if (this.preferredLocations == null || this.preferredLocations.size() == 0) {
                newReadRegion = this.currentWriteLocation;
            } else {
                // Choose the first region from the preferred list that is available.
                for (String regionName : this.preferredLocations) {
                    if (StringUtils.isNotEmpty(regionName)) {
                        newReadRegion = this.readableLocations.get(regionName);
                        if (newReadRegion != null) {
                            break;
                        }

                        newReadRegion = this.writableLocations.get(regionName);
                        if (newReadRegion != null) {
                            break;
                        }
                    }
                }
            }
        }

        if (newReadRegion != null) {
            this.currentReadLocation = newReadRegion;
        } else {
            this.currentReadLocation = this.currentWriteLocation;
        }
        
        logger.debug("Current read location {}, current write location {}",
                this.currentReadLocation,
                this.currentWriteLocation);
    }

    private URI getRegionalEndpoint(String regionName) {
        if (StringUtils.isNotEmpty(regionName)) {
            String databaseAccountName = this.defaultEndpoint.getHost();
            int indexOfDot = this.defaultEndpoint.getHost().indexOf('.');
            if (indexOfDot >= 0) {
                databaseAccountName = databaseAccountName.substring(0, indexOfDot);
            }

            // Add region name suffix to the account name.
            String regionalAccountName = databaseAccountName + "-" + regionName.replace(" ", "");
            String regionalUrl = this.defaultEndpoint.toString().replaceFirst(databaseAccountName, regionalAccountName);

            try {
                return new URI(regionalUrl);
            } catch (URISyntaxException e) {
                return null;
            }
        }

        return null;
    }
}
