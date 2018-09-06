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

package com.microsoft.azure.cosmosdb.internal.routing;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.DatabaseAccount;
import com.microsoft.azure.cosmosdb.DatabaseAccountLocation;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.rx.internal.Configs;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.apache.commons.collections4.map.UnmodifiableMap;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final URL defaultEndpoint;
    private final boolean useMultipleWriteLocations;
    private final Object lockObject;
    private final Duration unavailableLocationsExpirationTime;
    private final ConcurrentHashMap<URL, LocationUnavailabilityInfo> locationUnavailabilityInfoByEndpoint;

    private UnmodifiableList<String> preferredLocations;
    // lower-case region name to endpoint
    private UnmodifiableMap<String, URL> availableWriteLocations;
    // lower-case region
    private UnmodifiableMap<String, URL> availableReadLocations;
    private UnmodifiableList<URL> writeEndpoints;
    private UnmodifiableList<URL> readEndpoints;
    private Instant lastCacheUpdateTimestamp;
    private boolean enableMultipleWriteLocations;

    public LocationCache(
            List<String> preferredLocations,
            URL defaultEndpoint,
            boolean enableEndpointDiscovery,
            boolean useMultipleWriteLocations,
            Configs configs) {
        this.preferredLocations = new UnmodifiableList<>(preferredLocations.stream().map(loc -> loc.toLowerCase()).collect(Collectors.toList()));
        this.defaultEndpoint = defaultEndpoint;
        this.enableEndpointDiscovery = enableEndpointDiscovery;
        this.useMultipleWriteLocations = useMultipleWriteLocations;

        this.lockObject = new Object();
        this.availableWriteLocations = (UnmodifiableMap) UnmodifiableMap.unmodifiableMap(new CaseInsensitiveHashMap<>());
        this.availableReadLocations = (UnmodifiableMap) UnmodifiableMap.unmodifiableMap(new CaseInsensitiveHashMap<>());
        this.locationUnavailabilityInfoByEndpoint = new ConcurrentHashMap<>();
        this.readEndpoints = new UnmodifiableList<>(Collections.singletonList(this.defaultEndpoint));
        this.writeEndpoints = new UnmodifiableList<>(Collections.singletonList(this.defaultEndpoint));
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
    public UnmodifiableList<URL> getReadEndpoints() {
        if (this.locationUnavailabilityInfoByEndpoint.size() > 0
                && unavailableLocationsExpirationTimePassed()) {
            this.updateLocationCache();
        }

        return this.readEndpoints;
    }

    /**
     * Gets list of write endpoints ordered by
     * 1. Preferred location
     * 2. Endpoint availability
     * @return
     */
    public UnmodifiableList<URL> getWriteEndpoints() {
        if (this.locationUnavailabilityInfoByEndpoint.size() > 0
                && unavailableLocationsExpirationTimePassed()) {
            this.updateLocationCache();
        }

        return this.writeEndpoints;
    }

    /**
     * Marks the current location unavailable for read
     */
    public void markCurrentLocationUnavailableForRead() {
        List<URL> readLocationEndpoints = this.readEndpoints;
        this.markEndpointUnavailable(readLocationEndpoints.get(0), OperationType.Read);
    }

    /**
     * Marks the current location unavailable for read
     */
    public void markCurrentLocationUnavailableForWrite() {
        List<URL> writeLocationEndpoints = this.writeEndpoints;
        this.markEndpointUnavailable(writeLocationEndpoints.get(0), OperationType.Write);
    }

    /**
     * Invoked when {@link DatabaseAccount} is read
     * @param databaseAccount Read DatabaseAccount
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
     *             write endpoint in {@link DatabaseAccount#getWritableLocations()}.
     *             Endpoint of first write location in {@link DatabaseAccount#getWritableLocations()} is the only endpoint that supports
     *             write operation on all resource types (except during that region's failover).
     *             Only during manual failover, client would retry write on second write location in {@link DatabaseAccount#getWritableLocations()}.
     *    (b) Else resolve the request to first write endpoint in {@link DatabaseAccount#getWritableLocations()} OR
     *        second write endpoint in {@link DatabaseAccount#getWritableLocations()} in case of manual failover of that location.
     * 2. Else resolve the request to most preferred available read endpoint (automatic failover for read requests)
     * @param request Request for which endpoint is to be resolved
     * @return Resolved endpoint
     */
    public URL resolveServiceEndpoint(RxDocumentServiceRequest request) {
        List<URL> endpoints;
        int regionIndex = Utils.getValueOrDefault(request.requestContext.regionIndex, 0);

        if (request.useWriteEndpoint || request.getOperationType().isWriteOperation()) {
            endpoints = this.getWriteEndpoints();

            if (!this.canUseMultipleWriteLocations() || request.getResourceType() != ResourceType.Document) {
                // For non-document resource types in case of client can use multiple write locations
                // or when client cannot use multiple write locations, flip-flop between the
                // first and the second writable region in DatabaseAccount (for manual fail-over)
                regionIndex = request.useAlternateWriteEndpoint ? 1 : 0;
                UnmodifiableMap<String, URL> availableWriteEndpointsByLocation = this.availableWriteLocations;
                endpoints = this.getPreferredAvailableEndpoints(availableWriteEndpointsByLocation, OperationType.None, this.defaultEndpoint);
            }
        } else {
            // reads go to the most preferred available endpoint
            endpoints = this.getReadEndpoints();
        }

        return endpoints.get(regionIndex % endpoints.size());
    }

    public boolean shouldRefreshEndpoints(Utils.ValueHolder canRefreshInBackground) {
        canRefreshInBackground.v = true;

        String mostPreferredLocation = Utils.firstOrDefault(this.preferredLocations);

        // we should schedule refresh in background if we are unable to target the user's most preferredLocation.
        if (this.enableEndpointDiscovery) {
            if (!Strings.isNullOrEmpty(mostPreferredLocation)) {
                Utils.ValueHolder<URL> mostPreferredReadEndpointHolder = new Utils.ValueHolder<>();
                List<URL> readLocationEndpoints = this.readEndpoints;
                logger.debug("getReadEndpoints [{}]", readLocationEndpoints);

                if (Utils.tryGetValue(this.availableReadLocations, mostPreferredLocation, mostPreferredReadEndpointHolder)) {
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

            Utils.ValueHolder<URL> mostPreferredWriteEndpointHolder = new Utils.ValueHolder<>();
            List<URL> writeLocationEndpoints = this.writeEndpoints;
            logger.debug("getWriteEndpoints [{}]", writeLocationEndpoints);

            if (!this.canUseMultipleWriteLocations()) {
                if (this.isEndpointUnavailable(writeLocationEndpoints.get(0), OperationType.Write)) {
                    // Since most preferred write endpoint is unavailable, we can only refresh in background if
                    // we have an alternate write endpoint
                    canRefreshInBackground.v = writeLocationEndpoints.size() > 1;
                    logger.debug("shouldRefreshEndpoints = true, most preferred location " +
                                    "[{}] endpoint [{}] is not available for write. canRefreshInBackground = [{}]",
                            mostPreferredLocation,
                            writeLocationEndpoints.get(0),
                            canRefreshInBackground.v);

                    return true;
                } else {
                    logger.debug("shouldRefreshEndpoints: false, [{}] is available for Write", writeLocationEndpoints.get(0));
                    return false;
                }
            } else if (!Strings.isNullOrEmpty(mostPreferredLocation)) {
                if (Utils.tryGetValue(this.availableWriteLocations, mostPreferredLocation, mostPreferredWriteEndpointHolder)) {
                    boolean shouldRefresh = ! areEqual(mostPreferredWriteEndpointHolder.v,writeLocationEndpoints.get(0));

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
                return false;
            }
        } else {
            logger.debug("shouldRefreshEndpoints: false, endpoint discovery not enabled");
            return false;
        }
    }

    private boolean areEqual(URL url1, URL url2) {
        return url1.equals(url2);
    }

    private void clearStaleEndpointUnavailabilityInfo() {
        if (!this.locationUnavailabilityInfoByEndpoint.isEmpty()) {
            List<URL> unavailableEndpoints = new ArrayList<>(this.locationUnavailabilityInfoByEndpoint.keySet());

            for (URL unavailableEndpoint: unavailableEndpoints) {
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

    private boolean isEndpointUnavailable(URL endpoint, OperationType expectedAvailableOperations) {
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

    private void markEndpointUnavailable(
            URL unavailableEndpoint,
            OperationType unavailableOperationType) {
        Instant currentTime = Instant.now();
        LocationUnavailabilityInfo updatedInfo = this.locationUnavailabilityInfoByEndpoint.compute(
                unavailableEndpoint,
                new BiFunction<URL, LocationUnavailabilityInfo, LocationUnavailabilityInfo>() {
                    @Override
                    public LocationUnavailabilityInfo apply(URL url, LocationUnavailabilityInfo info) {

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
            logger.debug("updating location cache ..., current readLocations [{}], current writeLocations [{}]",
                    this.readEndpoints, this.writeEndpoints);

            if (preferenceList != null) {
                this.preferredLocations = preferenceList;
            }

            if (enableMultipleWriteLocations != null) {
                this.enableMultipleWriteLocations = enableMultipleWriteLocations;
            }

            this.clearStaleEndpointUnavailabilityInfo();

            if (readLocations != null) {
                this.availableReadLocations = this.getEndpointByLocation(readLocations);
            }

            if (writeLocations != null) {
                this.availableWriteLocations = this.getEndpointByLocation(writeLocations);
            }

            this.writeEndpoints = this.getPreferredAvailableEndpoints(this.availableWriteLocations, OperationType.Write, this.defaultEndpoint);
            this.readEndpoints = this.getPreferredAvailableEndpoints(this.availableReadLocations, OperationType.Read, this.writeEndpoints.get(0));
            this.lastCacheUpdateTimestamp = Instant.now();

            logger.debug("updating location cache finished, new readLocations [{}], new writeLocations [{}]",
                    this.readEndpoints, this.writeEndpoints);
        }
    }

    private UnmodifiableList<URL> getPreferredAvailableEndpoints(UnmodifiableMap<String, URL> endpointsByLocation, OperationType expectedAvailableOperation, URL fallbackEndpoint) {
        List<URL> endpoints = new ArrayList<>();

        // if enableEndpointDiscovery is false, we always use the defaultEndpoint that user passed in during documentClient init
        if (this.enableEndpointDiscovery) {
            if (this.canUseMultipleWriteLocations() || expectedAvailableOperation.supports(OperationType.Read)) {
                List<URL> unavailableEndpoints = new ArrayList<>();

                // When client can not use multiple write locations, preferred locations list should only be used
                // determining read endpoints order.
                // If client can use multiple write locations, preferred locations list should be used for determining
                // both read and write endpoints order.

                for (String location: this.preferredLocations) {
                    Utils.ValueHolder<URL> endpoint = new Utils.ValueHolder<>();
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
                for (Map.Entry<String, URL> kvp: endpointsByLocation.entrySet()) {
                    String location = kvp.getKey();

                    if (!Strings.isNullOrEmpty(location)) { // location is empty during manual fail-over
                        endpoints.add(kvp.getValue());
                    }
                }
            }
        }

        if (endpoints.isEmpty()) {
            endpoints.add(fallbackEndpoint);
        }

        return new UnmodifiableList(endpoints);
    }

    private UnmodifiableMap<String, URL> getEndpointByLocation(Iterable<DatabaseAccountLocation> locations) {
        Map<String, URL> endpointsByLocation = new CaseInsensitiveHashMap<>();

        for (DatabaseAccountLocation location: locations) {
            if (!Strings.isNullOrEmpty(location.getName())) {
                try {
                    URL endpoint = new URL(location.getEndpoint().toLowerCase());
                    endpointsByLocation.put(location.getName().toLowerCase(), endpoint);
                } catch (Exception e) {
                    logger.warn("GetAvailableEndpointsByLocation() - skipping add for location = [{}] as it is location name is either empty or endpoint is malformed [{}]",
                            location.getName(),
                            location.getEndpoint());
                }
            }
        }

        return (UnmodifiableMap) UnmodifiableMap.unmodifiableMap(endpointsByLocation);
    }

    private boolean canUseMultipleWriteLocations() {
        return this.useMultipleWriteLocations && this.enableMultipleWriteLocations;
    }

    private class LocationUnavailabilityInfo {
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
}