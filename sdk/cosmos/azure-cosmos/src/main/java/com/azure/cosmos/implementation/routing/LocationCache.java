// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.apachecommons.collections.map.CaseInsensitiveMap;
import com.azure.cosmos.implementation.apachecommons.collections.map.UnmodifiableMap;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Implements the abstraction to resolve target location for geo-replicated DatabaseAccount
 * with multiple writable and readable locations.
 */
public class LocationCache {
    private final static Logger logger = LoggerFactory.getLogger(LocationCache.class);

    private final boolean enableEndpointDiscovery;
    private final URI defaultEndpoint;
    private final boolean useMultipleWriteLocations;
    private final Object lockObject;
    private final Duration unavailableLocationsExpirationTime;
    private final ConcurrentHashMap<URI, LocationUnavailabilityInfo> locationUnavailabilityInfoByEndpoint;

    private DatabaseAccountLocationsInfo locationInfo;

    private Instant lastCacheUpdateTimestamp;
    private boolean enableMultipleWriteLocations;

    public LocationCache(
            List<String> preferredLocations,
            URI defaultEndpoint,
            boolean enableEndpointDiscovery,
            boolean useMultipleWriteLocations,
            Configs configs) {
        this.locationInfo = new DatabaseAccountLocationsInfo(preferredLocations, defaultEndpoint);
        this.defaultEndpoint = defaultEndpoint;
        this.enableEndpointDiscovery = enableEndpointDiscovery;
        this.useMultipleWriteLocations = useMultipleWriteLocations;

        this.lockObject = new Object();


        this.locationUnavailabilityInfoByEndpoint = new ConcurrentHashMap<>();

        this.lastCacheUpdateTimestamp = Instant.MIN;
        this.enableMultipleWriteLocations = false;
        this.unavailableLocationsExpirationTime = Duration.ofSeconds(configs.getUnavailableLocationsExpirationTimeInSeconds());
    }

    /**
     * Gets list of read endpoints ordered by
     *
     * 1. Preferred location
     * 2. Endpoint availability
     * @return
     */
    public UnmodifiableList<URI> getReadEndpoints() {
        if (this.locationUnavailabilityInfoByEndpoint.size() > 0
                && unavailableLocationsExpirationTimePassed()) {
            this.updateLocationCache();
        }

        return this.locationInfo.readEndpoints;
    }

    /**
     * Gets list of write endpoints ordered by
     * 1. Preferred location
     * 2. Endpoint availability
     * @return
     */
    public UnmodifiableList<URI> getWriteEndpoints() {
        if (this.locationUnavailabilityInfoByEndpoint.size() > 0
                && unavailableLocationsExpirationTimePassed()) {
            this.updateLocationCache();
        }

        return this.locationInfo.writeEndpoints;
    }

    /**
     * Marks the current location unavailable for read
     */
    public void markEndpointUnavailableForRead(URI endpoint) {
        this.markEndpointUnavailable(endpoint, OperationType.Read);
    }

    /**
     * Marks the current location unavailable for write
     */
    public void markEndpointUnavailableForWrite(URI endpoint) {
        this.markEndpointUnavailable(endpoint, OperationType.Write);
    }

    /**
     * Invoked when {@link DatabaseAccount} is read
     * @param databaseAccount READ DatabaseAccount
     */
    public void onDatabaseAccountRead(DatabaseAccount databaseAccount) {
        this.updateLocationCache(
                databaseAccount.getWritableLocations(),
                databaseAccount.getReadableLocations(),
                null,
                BridgeInternal.isEnableMultipleWriteLocations(databaseAccount));
    }

    void onLocationPreferenceChanged(UnmodifiableList<String> preferredLocations) {
        this.updateLocationCache(
                null, null , preferredLocations, null);
    }

    /**
     * Resolves request to service endpoint.
     * 1. If this is a write request
     *    (a) If UseMultipleWriteLocations = true
     *        (i) For document writes, resolve to most preferred and available write endpoint.
     *            Once the endpoint is marked unavailable, it is moved to the end of available write endpoint. Current request will
     *            be retried on next preferred available write endpoint.
     *        (ii) For all other resources, always resolve to first/second (regardless of preferred locations)
     *             write getEndpoint in {@link DatabaseAccount#getWritableLocations()}.
     *             Endpoint of first write location in {@link DatabaseAccount#getWritableLocations()} is the only getEndpoint that supports
     *             write operation on all resource types (except during that region's failover).
     *             Only during manual failover, client would retry write on second write location in {@link DatabaseAccount#getWritableLocations()}.
     *    (b) Else resolve the request to first write getEndpoint in {@link DatabaseAccount#getWritableLocations()} OR
     *        second write getEndpoint in {@link DatabaseAccount#getWritableLocations()} in case of manual failover of that location.
     * 2. Else resolve the request to most preferred available read getEndpoint (getAutomatic failover for read requests)
     * @param request Request for which getEndpoint is to be resolved
     * @return Resolved getEndpoint
     */
    public URI resolveServiceEndpoint(RxDocumentServiceRequest request) {
        Objects.requireNonNull(request.requestContext,
            "RxDocumentServiceRequest.requestContext is required and cannot be null.");
        if(request.requestContext.locationEndpointToRoute != null) {
            return request.requestContext.locationEndpointToRoute;
        }

        int locationIndex = Utils.getValueOrDefault(request.requestContext.locationIndexToRoute, 0);

        boolean usePreferredLocations = request.requestContext.usePreferredLocations != null ? request.requestContext.usePreferredLocations : true;
        if(!usePreferredLocations || (request.getOperationType().isWriteOperation() && !this.canUseMultipleWriteLocations(request))) {
            // For non-document resource types in case of client can use multiple write locations
            // or when client cannot use multiple write locations, flip-flop between the
            // first and the second writable region in DatabaseAccount (for manual failover)
            DatabaseAccountLocationsInfo currentLocationInfo =  this.locationInfo;

            if(this.enableEndpointDiscovery && currentLocationInfo.availableWriteLocations.size() > 0) {
                locationIndex =  Math.min(locationIndex%2, currentLocationInfo.availableWriteLocations.size()-1);
                String writeLocation = currentLocationInfo.availableWriteLocations.get(locationIndex);
                return currentLocationInfo.availableWriteEndpointByLocation.get(writeLocation);
            } else {
                return this.defaultEndpoint;
            }
        } else {
            UnmodifiableList<URI> endpoints = request.getOperationType().isWriteOperation()? this.getWriteEndpoints() : this.getReadEndpoints();
            return endpoints.get(locationIndex % endpoints.size());
        }
    }

    public boolean shouldRefreshEndpoints(Utils.ValueHolder<Boolean> canRefreshInBackground) {
        canRefreshInBackground.v = true;
        DatabaseAccountLocationsInfo currentLocationInfo = this.locationInfo;
        String mostPreferredLocation = Utils.firstOrDefault(currentLocationInfo.preferredLocations);

        // we should schedule refresh in background if we are unable to target the user's most preferredLocation.
        if (this.enableEndpointDiscovery) {

            boolean shouldRefresh = this.useMultipleWriteLocations && !this.enableMultipleWriteLocations;
            List<URI> readLocationEndpoints = currentLocationInfo.readEndpoints;
            if (this.isEndpointUnavailable(readLocationEndpoints.get(0), OperationType.Read)) {
                // Since most preferred read endpoint is unavailable, we can only refresh in background if
                // we have an alternate read endpoint
                canRefreshInBackground.v = anyEndpointsAvailable(readLocationEndpoints,OperationType.Read);
                logger.debug("shouldRefreshEndpoints = true,  since the first read endpoint " +
                        "[{}] is not available for read. canRefreshInBackground = [{}]",
                    readLocationEndpoints.get(0),
                    canRefreshInBackground.v);
                return true;
            }

            if (!Strings.isNullOrEmpty(mostPreferredLocation)) {
                Utils.ValueHolder<URI> mostPreferredReadEndpointHolder = new Utils.ValueHolder<>();
                logger.debug("getReadEndpoints [{}]", readLocationEndpoints);

                if (Utils.tryGetValue(currentLocationInfo.availableReadEndpointByLocation, mostPreferredLocation, mostPreferredReadEndpointHolder)) {
                    logger.debug("most preferred is [{}], most preferred available is [{}]",
                            mostPreferredLocation, mostPreferredReadEndpointHolder.v);
                    if (!areEqual(mostPreferredReadEndpointHolder.v, readLocationEndpoints.get(0))) {
                        // For reads, we can always refresh in background as we can alternate to
                        // other available read endpoints
                        logger.debug("shouldRefreshEndpoints = true, most preferred location [{}]" +
                                " is not available for read.", mostPreferredLocation);
                        return true;
                    }

                    logger.debug("most preferred is [{}], and most preferred available [{}] are the same",
                            mostPreferredLocation, mostPreferredReadEndpointHolder.v);
                }
                else {
                    logger.debug("shouldRefreshEndpoints = true, most preferred location [{}] " +
                            "is not in available read locations.", mostPreferredLocation);
                    return true;
                }
            }

            Utils.ValueHolder<URI> mostPreferredWriteEndpointHolder = new Utils.ValueHolder<>();
            List<URI> writeLocationEndpoints = currentLocationInfo.writeEndpoints;
            logger.debug("getWriteEndpoints [{}]", writeLocationEndpoints);

            if (!this.canUseMultipleWriteLocations()) {
                if (this.isEndpointUnavailable(writeLocationEndpoints.get(0), OperationType.Write)) {
                    // Since most preferred write endpoint is unavailable, we can only refresh in background if
                    // we have an alternate write endpoint
                    canRefreshInBackground.v = anyEndpointsAvailable(writeLocationEndpoints,OperationType.Write);
                    logger.debug("shouldRefreshEndpoints = true, most preferred location " +
                                    "[{}] endpoint [{}] is not available for write. canRefreshInBackground = [{}]",
                            mostPreferredLocation,
                            writeLocationEndpoints.get(0),
                            canRefreshInBackground.v);

                    return true;
                } else {
                    logger.debug("shouldRefreshEndpoints: false, [{}] is available for Write", writeLocationEndpoints.get(0));
                    return shouldRefresh;
                }
            } else if (!Strings.isNullOrEmpty(mostPreferredLocation)) {
                if (Utils.tryGetValue(currentLocationInfo.availableWriteEndpointByLocation, mostPreferredLocation, mostPreferredWriteEndpointHolder)) {
                    shouldRefresh = ! areEqual(mostPreferredWriteEndpointHolder.v,writeLocationEndpoints.get(0));

                    if (shouldRefresh) {
                        logger.debug("shouldRefreshEndpoints: true, write endpoint [{}] is not the same as most preferred [{}]",
                                writeLocationEndpoints.get(0), mostPreferredWriteEndpointHolder.v);
                    } else {
                        logger.debug("shouldRefreshEndpoints: false, write endpoint [{}] is the same as most preferred [{}]",
                                writeLocationEndpoints.get(0), mostPreferredWriteEndpointHolder.v);
                    }

                    return shouldRefresh;
                } else {
                    logger.debug("shouldRefreshEndpoints = true, most preferred location [{}] is not in available write locations",
                            mostPreferredLocation);
                    return true;
                }
            } else {
                logger.debug("shouldRefreshEndpoints: false, mostPreferredLocation [{}] is empty", mostPreferredLocation);
                return shouldRefresh;
            }
        } else {
            logger.debug("shouldRefreshEndpoints: false, endpoint discovery not enabled");
            return false;
        }
    }
    private boolean areEqual(URI url1, URI url2) {
        return url1.equals(url2);
    }

    private void clearStaleEndpointUnavailabilityInfo() {
        if (!this.locationUnavailabilityInfoByEndpoint.isEmpty()) {
            List<URI> unavailableEndpoints = new ArrayList<>(this.locationUnavailabilityInfoByEndpoint.keySet());

            for (URI unavailableEndpoint: unavailableEndpoints) {
                Utils.ValueHolder<LocationUnavailabilityInfo> unavailabilityInfoHolder = new Utils.ValueHolder<>();
                Utils.ValueHolder<LocationUnavailabilityInfo> removedHolder = new Utils.ValueHolder<>();

                if (Utils.tryGetValue(this.locationUnavailabilityInfoByEndpoint, unavailableEndpoint, unavailabilityInfoHolder)
                        &&
                        durationPassed(Instant.now(), unavailabilityInfoHolder.v.LastUnavailabilityCheckTimeStamp,
                                this.unavailableLocationsExpirationTime)

                        && Utils.tryRemove(this.locationUnavailabilityInfoByEndpoint, unavailableEndpoint, removedHolder)) {
                    logger.debug(
                            "Removed endpoint [{}] unavailable for operations [{}] from unavailableEndpoints",
                            unavailableEndpoint,
                            unavailabilityInfoHolder.v.UnavailableOperations);
                }
            }
        }
    }

    private boolean isEndpointUnavailable(URI endpoint, OperationType expectedAvailableOperations) {
        Utils.ValueHolder<LocationUnavailabilityInfo> unavailabilityInfoHolder = new Utils.ValueHolder<>();

        if (expectedAvailableOperations == OperationType.None
                || !Utils.tryGetValue(this.locationUnavailabilityInfoByEndpoint, endpoint, unavailabilityInfoHolder)
                || !unavailabilityInfoHolder.v.UnavailableOperations.supports(expectedAvailableOperations)) {
            return false;
        } else {
            if (durationPassed(Instant.now(), unavailabilityInfoHolder.v.LastUnavailabilityCheckTimeStamp, this.unavailableLocationsExpirationTime)) {
                return false;
            } else {
                logger.debug(
                        "Endpoint [{}] unavailable for operations [{}] present in unavailableEndpoints",
                        endpoint,
                        unavailabilityInfoHolder.v.UnavailableOperations);
                // Unexpired entry present. Endpoint is unavailable
                return true;
            }
        }
    }

    private boolean anyEndpointsAvailable(List<URI> endpoints, OperationType expectedAvailableOperations) {
        Utils.ValueHolder<LocationUnavailabilityInfo> unavailabilityInfoHolder = new Utils.ValueHolder<>();
        boolean anyEndpointsAvailable = false;
        for (URI endpoint : endpoints) {
            if (!isEndpointUnavailable(endpoint, expectedAvailableOperations)) {
                anyEndpointsAvailable = true;
                break;
            }
        }
        return anyEndpointsAvailable;
    }

    private void markEndpointUnavailable(
            URI unavailableEndpoint,
            OperationType unavailableOperationType) {
        Instant currentTime = Instant.now();
        LocationUnavailabilityInfo updatedInfo = this.locationUnavailabilityInfoByEndpoint.compute(
                unavailableEndpoint,
                new BiFunction<URI, LocationUnavailabilityInfo, LocationUnavailabilityInfo>() {
                    @Override
                    public LocationUnavailabilityInfo apply(URI url, LocationUnavailabilityInfo info) {
                        if (info == null) {
                            // not already present, add
                            return new LocationUnavailabilityInfo(currentTime, unavailableOperationType);
                        } else {
                            // already present, update
                            info.LastUnavailabilityCheckTimeStamp = currentTime;
                            info.UnavailableOperations = OperationType.combine(info.UnavailableOperations, unavailableOperationType);
                            return info;
                        }

                    }
                });

        this.updateLocationCache();

        logger.debug(
                "Endpoint [{}] unavailable for [{}] added/updated to unavailableEndpoints with timestamp [{}]",
                unavailableEndpoint,
                unavailableOperationType,
                updatedInfo.LastUnavailabilityCheckTimeStamp);
    }

    private void updateLocationCache(){
        updateLocationCache(null, null, null, null);
    }

    private void updateLocationCache(
            Iterable<DatabaseAccountLocation> writeLocations,
            Iterable<DatabaseAccountLocation> readLocations,
            UnmodifiableList<String> preferenceList,
            Boolean enableMultipleWriteLocations) {
        synchronized (this.lockObject) {
            DatabaseAccountLocationsInfo nextLocationInfo = new DatabaseAccountLocationsInfo(this.locationInfo);
            logger.debug("updating location cache ..., current readLocations [{}], current writeLocations [{}]",
                    nextLocationInfo.readEndpoints, nextLocationInfo.writeEndpoints);

            if (preferenceList != null) {
                nextLocationInfo.preferredLocations = preferenceList;
            }

            if (enableMultipleWriteLocations != null) {
                this.enableMultipleWriteLocations = enableMultipleWriteLocations;
            }

            this.clearStaleEndpointUnavailabilityInfo();

            if (readLocations != null) {
                Utils.ValueHolder<UnmodifiableList<String>> out = Utils.ValueHolder.initialize(nextLocationInfo.availableReadLocations);
                nextLocationInfo.availableReadEndpointByLocation = this.getEndpointByLocation(readLocations, out);
                nextLocationInfo.availableReadLocations =  out.v;
            }

            if (writeLocations != null) {
                Utils.ValueHolder<UnmodifiableList<String>> out = Utils.ValueHolder.initialize(nextLocationInfo.availableWriteLocations);
                nextLocationInfo.availableWriteEndpointByLocation = this.getEndpointByLocation(writeLocations, out);
                nextLocationInfo.availableWriteLocations = out.v;
            }

            nextLocationInfo.writeEndpoints = this.getPreferredAvailableEndpoints(nextLocationInfo.availableWriteEndpointByLocation, nextLocationInfo.availableWriteLocations, OperationType.Write, this.defaultEndpoint);
            nextLocationInfo.readEndpoints = this.getPreferredAvailableEndpoints(nextLocationInfo.availableReadEndpointByLocation, nextLocationInfo.availableReadLocations, OperationType.Read, nextLocationInfo.writeEndpoints.get(0));
            this.lastCacheUpdateTimestamp = Instant.now();

            logger.debug("updating location cache finished, new readLocations [{}], new writeLocations [{}]",
                    nextLocationInfo.readEndpoints, nextLocationInfo.writeEndpoints);
            this.locationInfo = nextLocationInfo;
        }
    }

    private UnmodifiableList<URI> getPreferredAvailableEndpoints(UnmodifiableMap<String, URI> endpointsByLocation,
                                                                 UnmodifiableList<String> orderedLocations,
                                                                 OperationType expectedAvailableOperation,
                                                                 URI fallbackEndpoint) {
        List<URI> endpoints = new ArrayList<>();
        DatabaseAccountLocationsInfo currentLocationInfo = this.locationInfo;
        // if enableEndpointDiscovery is false, we always use the defaultEndpoint that user passed in during documentClient init
        if (this.enableEndpointDiscovery) {
            if (this.canUseMultipleWriteLocations() || expectedAvailableOperation.supports(OperationType.Read)) {
                List<URI> unavailableEndpoints = new ArrayList<>();

                // When client can not use multiple write locations, preferred locations list should only be used
                // determining read endpoints order.
                // If client can use multiple write locations, preferred locations list should be used for determining
                // both read and write endpoints order.

                for (String location: currentLocationInfo.preferredLocations) {
                    Utils.ValueHolder<URI> endpoint = new Utils.ValueHolder<>();
                    if (Utils.tryGetValue(endpointsByLocation, location, endpoint)) {
                        if (this.isEndpointUnavailable(endpoint.v, expectedAvailableOperation)) {
                            unavailableEndpoints.add(endpoint.v);
                        } else {
                            endpoints.add(endpoint.v);
                        }
                    }
                }

                if (endpoints.isEmpty()) {
                    endpoints.add(fallbackEndpoint);
                }

                endpoints.addAll(unavailableEndpoints);
            } else {
                for (String location : orderedLocations) {

                    Utils.ValueHolder<URI> endpoint = Utils.ValueHolder.initialize(null);
                    if (!Strings.isNullOrEmpty(location) && // location is empty during manual failover
                        Utils.tryGetValue(endpointsByLocation, location, endpoint)) {
                        endpoints.add(endpoint.v);
                    }
                }
            }
        }

        if (endpoints.isEmpty()) {
            endpoints.add(fallbackEndpoint);
        }

        return new UnmodifiableList<URI>(endpoints);
    }

    private UnmodifiableMap<String, URI> getEndpointByLocation(Iterable<DatabaseAccountLocation> locations,
                                                                  Utils.ValueHolder<UnmodifiableList<String>> orderedLocations) {
        Map<String, URI> endpointsByLocation = new CaseInsensitiveMap<>();
        List<String> parsedLocations = new ArrayList<>();

        for (DatabaseAccountLocation location: locations) {
            if (!Strings.isNullOrEmpty(location.getName())) {
                try {
                    URI endpoint = new URI(location.getEndpoint().toLowerCase(Locale.ROOT));
                    endpointsByLocation.put(location.getName().toLowerCase(Locale.ROOT), endpoint);
                    parsedLocations.add(location.getName());

                } catch (Exception e) {
                    logger.warn("GetAvailableEndpointsByLocation() - skipping add for location = [{}] as it is location name is either empty or endpoint is malformed [{}]",
                            location.getName(),
                            location.getEndpoint());
                }
            }
        }

        orderedLocations.v = new UnmodifiableList<String>(parsedLocations);

        return (UnmodifiableMap<String, URI>) UnmodifiableMap.<String, URI>unmodifiableMap(endpointsByLocation);
    }

    private boolean canUseMultipleWriteLocations() {
        return this.useMultipleWriteLocations && this.enableMultipleWriteLocations;
    }

    public boolean canUseMultipleWriteLocations(RxDocumentServiceRequest request) {
        return this.canUseMultipleWriteLocations() &&
            (request.getResourceType() == ResourceType.Document ||
                (request.getResourceType() == ResourceType.StoredProcedure && request.getOperationType() ==
                    com.azure.cosmos.implementation.OperationType.ExecuteJavaScript));
    }


    private static class LocationUnavailabilityInfo {
        LocationUnavailabilityInfo(Instant instant, OperationType type) {
            this.LastUnavailabilityCheckTimeStamp = instant;
            this.UnavailableOperations = type;
        }

        public Instant LastUnavailabilityCheckTimeStamp;
        public OperationType UnavailableOperations;
    }

    private enum OperationType {
        None(0x0),
        Read(0x1),
        Write(0x2),
        ReadAndWrite(0x3);

        private final int flag;

        public boolean hasReadFlag() {
            return (flag & Read.flag) != 0;
        }

        public boolean hasWriteFlag() {
            return (flag & Write.flag) != 0;
        }

        public static OperationType combine(OperationType t1, OperationType t2) {
            switch (t1.flag | t2.flag) {
                case 0x0:
                    return None;
                case 0x1:
                    return Read;
                case 0x2:
                    return Write;
                default:
                    return ReadAndWrite;
            }
        }

        public boolean supports(OperationType type) {
            return (flag & type.flag) != 0;
        }

        OperationType(int flag) {
            this.flag = flag;
        }
    }

    private boolean durationPassed(Instant end, Instant start, Duration duration) {
        return end.minus(duration).isAfter(start);
    }

    private boolean unavailableLocationsExpirationTimePassed() {
        return durationPassed(Instant.now(), this.lastCacheUpdateTimestamp, this.unavailableLocationsExpirationTime);
    }

    static class DatabaseAccountLocationsInfo {
        private UnmodifiableList<String> preferredLocations;
        // lower-case region
        private UnmodifiableList<String> availableWriteLocations;
        // lower-case region
        private UnmodifiableList<String> availableReadLocations;
        private UnmodifiableMap<String, URI> availableWriteEndpointByLocation;
        private UnmodifiableMap<String, URI> availableReadEndpointByLocation;

        private UnmodifiableList<URI> writeEndpoints;
        private UnmodifiableList<URI> readEndpoints;

        public DatabaseAccountLocationsInfo(List<String> preferredLocations,
                                            URI defaultEndpoint) {
            this.preferredLocations = new UnmodifiableList<>(preferredLocations.stream().map(loc -> loc.toLowerCase(Locale.ROOT)).collect(Collectors.toList()));
            this.availableWriteEndpointByLocation
                = (UnmodifiableMap<String, URI>) UnmodifiableMap.<String, URI>unmodifiableMap(new CaseInsensitiveMap<>());
            this.availableReadEndpointByLocation
                = (UnmodifiableMap<String, URI>) UnmodifiableMap.<String, URI>unmodifiableMap(new CaseInsensitiveMap<>());
            this.availableReadLocations = new UnmodifiableList<>(Collections.emptyList());
            this.availableWriteLocations = new UnmodifiableList<>(Collections.emptyList());
            this.readEndpoints = new UnmodifiableList<>(Collections.singletonList(defaultEndpoint));
            this.writeEndpoints = new UnmodifiableList<>(Collections.singletonList(defaultEndpoint));
        }

        public DatabaseAccountLocationsInfo(DatabaseAccountLocationsInfo other) {
            this.preferredLocations = other.preferredLocations;
            this.availableWriteLocations = other.availableWriteLocations;
            this.availableReadLocations = other.availableReadLocations;
            this.availableWriteEndpointByLocation = other.availableWriteEndpointByLocation;
            this.availableReadEndpointByLocation = other.availableReadEndpointByLocation;
            this.writeEndpoints = other.writeEndpoints;
            this.readEndpoints = other.readEndpoints;
        }
    }
}
