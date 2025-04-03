// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosExcludedRegions;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.apachecommons.collections.map.CaseInsensitiveMap;
import com.azure.cosmos.implementation.apachecommons.collections.map.UnmodifiableMap;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Implements the abstraction to resolve target location for geo-replicated DatabaseAccount
 * with multiple writable and readable locations.
 */
public class LocationCache {
    private final static Logger logger = LoggerFactory.getLogger(LocationCache.class);

    private final boolean enableEndpointDiscovery;
    private final RegionalRoutingContext defaultRegionalRoutingContext;
    private final boolean useMultipleWriteLocations;
    private final Object lockObject;
    private final Duration unavailableLocationsExpirationTime;
    private final ConcurrentHashMap<RegionalRoutingContext, LocationUnavailabilityInfo> locationUnavailabilityInfoByEndpoint;
    private final ConnectionPolicy connectionPolicy;

    private DatabaseAccountLocationsInfo locationInfo;

    private Instant lastCacheUpdateTimestamp;
    private boolean enableMultipleWriteLocations;

    public LocationCache(
            ConnectionPolicy connectionPolicy,
            URI defaultEndpoint,
            Configs configs) {

        List<String> preferredLocations = new ArrayList<>(connectionPolicy.getPreferredRegions() != null ?
            connectionPolicy.getPreferredRegions() :
            Collections.emptyList()
        );

        this.locationInfo = new DatabaseAccountLocationsInfo(preferredLocations, defaultEndpoint);
        this.defaultRegionalRoutingContext = new RegionalRoutingContext(defaultEndpoint);
        this.enableEndpointDiscovery = connectionPolicy.isEndpointDiscoveryEnabled();
        this.useMultipleWriteLocations = connectionPolicy.isMultipleWriteRegionsEnabled();

        this.lockObject = new Object();

        this.locationUnavailabilityInfoByEndpoint = new ConcurrentHashMap<>();

        this.lastCacheUpdateTimestamp = Instant.MIN;
        this.enableMultipleWriteLocations = false;
        this.unavailableLocationsExpirationTime = Duration.ofSeconds(configs.getUnavailableLocationsExpirationTimeInSeconds());
        this.connectionPolicy = connectionPolicy;
    }

    /**
     * Gets list of read endpoints ordered by
     *
     * 1. Preferred location
     * 2. Endpoint availability
     * @return
     */
    public UnmodifiableList<RegionalRoutingContext> getReadEndpoints() {
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
    public UnmodifiableList<RegionalRoutingContext> getWriteEndpoints() {
        if (this.locationUnavailabilityInfoByEndpoint.size() > 0
                && unavailableLocationsExpirationTimePassed()) {
            this.updateLocationCache();
        }

        return this.locationInfo.writeEndpoints;
    }


    /***
     * Get the list of available read endpoints.
     * The list will not be filtered by preferred region list.
     *
     * This method is ONLY used for fault injection.
     * @return
     */
    public List<URI> getAvailableReadEndpoints() {
        return this.locationInfo.availableReadEndpointsByLocation.values().stream().map(consolidatedLocationEndpoints -> {
                // TODO (nehrao1): Integrate thinclient endpoints into fault injection
                // TODO (nehrao1): https://github.com/Azure/azure-sdk-for-java/issues/44429
                return consolidatedLocationEndpoints.getGatewayRegionalEndpoint();
            }).collect(Collectors.toList());
    }

    /***
     * Get the list of available write endpoints.
     * The list will not be filtered by preferred region list.
     *
     * This method is ONLY used for fault injection.
     * @return
     */
    public List<URI> getAvailableWriteEndpoints() {
        return this.locationInfo.availableWriteEndpointsByLocation.values().stream().map(consolidatedLocationEndpoints -> {
            // TODO(nehrao1): Integrate thinclient endpoints into fault injection
            // TODO(nehrao1): https://github.com/Azure/azure-sdk-for-java/issues/44429
            return consolidatedLocationEndpoints.getGatewayRegionalEndpoint();
        }).collect(Collectors.toList());
    }

    public List<String> getEffectivePreferredLocations() {
        return this.locationInfo.effectivePreferredLocations;
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
    public RegionalRoutingContext resolveServiceEndpoint(RxDocumentServiceRequest request) {
        Objects.requireNonNull(request.requestContext,
            "RxDocumentServiceRequest.requestContext is required and cannot be null.");

        if (request.requestContext.regionalRoutingContextToRoute != null) {
            return request.requestContext.regionalRoutingContextToRoute;
        }

        int locationIndex = Utils.getValueOrDefault(request.requestContext.locationIndexToRoute, 0);

        boolean usePreferredLocations = request.requestContext.usePreferredLocations != null ? request.requestContext.usePreferredLocations : true;
        if (!usePreferredLocations || (request.getOperationType().isWriteOperation() && !this.canUseMultipleWriteLocations(request))) {
            // For non-document resource types in case of client can use multiple write locations
            // or when client cannot use multiple write locations, flip-flop between the
            // first and the second writable region in DatabaseAccount (for manual failover)
            DatabaseAccountLocationsInfo currentLocationInfo =  this.locationInfo;

            if (this.enableEndpointDiscovery && !currentLocationInfo.availableWriteLocations.isEmpty()) {
                locationIndex =  Math.min(locationIndex%2, currentLocationInfo.availableWriteLocations.size()-1);
                String writeLocation = currentLocationInfo.availableWriteLocations.get(locationIndex);
                return currentLocationInfo.availableWriteEndpointsByLocation.get(writeLocation);
            } else {
                return this.defaultRegionalRoutingContext;
            }
        } else {
            UnmodifiableList<RegionalRoutingContext> endpoints =
                request.getOperationType().isWriteOperation()? this.getApplicableWriteEndpoints(request) : this.getApplicableReadEndpoints(request);
            return endpoints.get(locationIndex % endpoints.size());
        }
    }

    public UnmodifiableList<RegionalRoutingContext> getApplicableWriteEndpoints(RxDocumentServiceRequest request) {
        return this.getApplicableWriteEndpoints(request.requestContext.getExcludeRegions(), request.requestContext.getUnavailableRegionsForPartition());
    }

    public UnmodifiableList<RegionalRoutingContext> getApplicableWriteEndpoints(List<String> excludedRegionsOnRequest, List<String> unavailableRegionsForPartition) {

        UnmodifiableList<RegionalRoutingContext> writeEndpoints = this.getWriteEndpoints();
        Supplier<CosmosExcludedRegions> excludedRegionsSupplier = this.connectionPolicy.getExcludedRegionsSupplier();

        List<String> effectiveExcludedRegions = isExcludedRegionsSupplierConfigured(excludedRegionsSupplier) ?
            new ArrayList<>(excludedRegionsSupplier.get().getExcludedRegions()) : Collections.emptyList();

        if (!isExcludeRegionsConfigured(excludedRegionsOnRequest, effectiveExcludedRegions) && (unavailableRegionsForPartition == null || unavailableRegionsForPartition.isEmpty())) {
            return writeEndpoints;
        }

        if (excludedRegionsOnRequest != null && !excludedRegionsOnRequest.isEmpty()) {
            effectiveExcludedRegions = excludedRegionsOnRequest;
        }

        List<String> effectiveExcludedRegionsWithPartitionUnavailableRegions = new ArrayList<>(effectiveExcludedRegions);

        if (unavailableRegionsForPartition != null) {
            effectiveExcludedRegionsWithPartitionUnavailableRegions.addAll(unavailableRegionsForPartition);
        }

        // filter regions based on the exclude region config
        return this.getApplicableEndpoints(
            writeEndpoints,
            this.locationInfo.regionNameByWriteEndpoint,
            this.defaultRegionalRoutingContext,
            effectiveExcludedRegionsWithPartitionUnavailableRegions);
    }

    public UnmodifiableList<RegionalRoutingContext> getApplicableReadEndpoints(RxDocumentServiceRequest request) {
        return this.getApplicableReadEndpoints(request.requestContext.getExcludeRegions(), request.requestContext.getUnavailableRegionsForPartition());
    }

    public UnmodifiableList<RegionalRoutingContext> getApplicableReadEndpoints(List<String> excludedRegionsOnRequest, List<String> unavailableRegionsForPartition) {
        UnmodifiableList<RegionalRoutingContext> readEndpoints = this.getReadEndpoints();
        Supplier<CosmosExcludedRegions> excludedRegionsSupplier = this.connectionPolicy.getExcludedRegionsSupplier();

        List<String> effectiveExcludedRegions = isExcludedRegionsSupplierConfigured(excludedRegionsSupplier) ?
            new ArrayList<>(excludedRegionsSupplier.get().getExcludedRegions()) : Collections.emptyList();

        if (!isExcludeRegionsConfigured(excludedRegionsOnRequest, effectiveExcludedRegions) && (unavailableRegionsForPartition == null || unavailableRegionsForPartition.isEmpty())) {
            return readEndpoints;
        }

        if (excludedRegionsOnRequest != null && !excludedRegionsOnRequest.isEmpty()) {
            effectiveExcludedRegions = excludedRegionsOnRequest;
        }

        List<String> effectiveExcludedRegionsWithPartitionUnavailableRegions = new ArrayList<>(effectiveExcludedRegions);

        if (unavailableRegionsForPartition != null) {
            effectiveExcludedRegionsWithPartitionUnavailableRegions.addAll(unavailableRegionsForPartition);
        }

        // filter regions based on the exclude region config
        return this.getApplicableEndpoints(
            readEndpoints,
            this.locationInfo.regionNameByReadEndpoint,
            this.locationInfo.writeEndpoints.get(0), // match the fallback region used in getPreferredAvailableEndpoints
            effectiveExcludedRegionsWithPartitionUnavailableRegions);
    }

    private UnmodifiableList<RegionalRoutingContext> getApplicableEndpoints(
        UnmodifiableList<RegionalRoutingContext> endpoints,
        UnmodifiableMap<RegionalRoutingContext, String> regionNameByEndpoint,
        RegionalRoutingContext fallbackRegionalRoutingContext,
        List<String> excludeRegionList) {

        List<RegionalRoutingContext> applicableEndpoints = new ArrayList<>();
        for (RegionalRoutingContext endpoint : endpoints) {
            Utils.ValueHolder<String> regionName = new Utils.ValueHolder<>();
            if (Utils.tryGetValue(regionNameByEndpoint, endpoint, regionName)) {
                if (!excludeRegionList.stream().anyMatch(regionName.v::equalsIgnoreCase)) {
                    applicableEndpoints.add(endpoint);
                }
            }
        }

        if (applicableEndpoints.isEmpty()) {
            applicableEndpoints.add(fallbackRegionalRoutingContext);
        }

        return new UnmodifiableList<>(applicableEndpoints);
    }

    private boolean isExcludeRegionsConfigured(List<String> excludedRegionsOnRequest, List<String> excludedRegionsOnClient) {
        boolean isExcludedRegionsConfiguredOnRequest = !(excludedRegionsOnRequest == null || excludedRegionsOnRequest.isEmpty());
        boolean isExcludedRegionsConfiguredOnClient = !(excludedRegionsOnClient == null || excludedRegionsOnClient.isEmpty());

        return isExcludedRegionsConfiguredOnRequest || isExcludedRegionsConfiguredOnClient;
    }

    public URI resolveFaultInjectionEndpoint(String region, boolean writeOnly) {
        Utils.ValueHolder<RegionalRoutingContext> endpointValueHolder = new Utils.ValueHolder<>();
        if (writeOnly) {
            Utils.tryGetValue(this.locationInfo.availableWriteEndpointsByLocation, region, endpointValueHolder);
        } else {
            Utils.tryGetValue(this.locationInfo.availableReadEndpointsByLocation, region, endpointValueHolder);
        }

        if (endpointValueHolder.v != null) {
            // TODO: Figure out how to integrate thinclient into fault injection
            return endpointValueHolder.v.getGatewayRegionalEndpoint();
        }

        throw new IllegalArgumentException("Can not find service endpoint for region " + region);
    }

    public URI getDefaultEndpoint() {
        return this.defaultRegionalRoutingContext.getGatewayRegionalEndpoint();
    }

    public boolean shouldRefreshEndpoints(Utils.ValueHolder<Boolean> canRefreshInBackground) {
        canRefreshInBackground.v = true;
        DatabaseAccountLocationsInfo currentLocationInfo = this.locationInfo;
        String mostPreferredLocation = Utils.firstOrDefault(currentLocationInfo.preferredLocations);

        if (StringUtils.isEmpty(mostPreferredLocation)) {
            mostPreferredLocation = Utils.firstOrDefault(currentLocationInfo.effectivePreferredLocations);
        }

        // we should schedule refresh in background if we are unable to target the user's most preferredLocation.
        if (this.enableEndpointDiscovery) {

            boolean shouldRefresh = this.useMultipleWriteLocations && !this.enableMultipleWriteLocations;
            List<RegionalRoutingContext> readLocationEndpoints = currentLocationInfo.readEndpoints;
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
                Utils.ValueHolder<RegionalRoutingContext> mostPreferredReadEndpointHolder = new Utils.ValueHolder<>();
                logger.debug("getReadEndpoints [{}]", readLocationEndpoints);

                if (Utils.tryGetValue(currentLocationInfo.availableReadEndpointsByLocation, mostPreferredLocation, mostPreferredReadEndpointHolder)) {
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

            Utils.ValueHolder<RegionalRoutingContext> mostPreferredWriteEndpointHolder = new Utils.ValueHolder<>();
            List<RegionalRoutingContext> writeLocationEndpoints = currentLocationInfo.writeEndpoints;
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
                if (Utils.tryGetValue(currentLocationInfo.availableWriteEndpointsByLocation, mostPreferredLocation, mostPreferredWriteEndpointHolder)) {
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

    public String getRegionName(URI locationEndpoint, com.azure.cosmos.implementation.OperationType operationType) {
        Utils.ValueHolder<String> regionName = new Utils.ValueHolder<>();
        if (operationType.isWriteOperation()) {
            if (Utils.tryGetValue(this.locationInfo.regionNameByWriteEndpoint, new RegionalRoutingContext(locationEndpoint), regionName)) {
                return regionName.v;
            }
        } else {
            if (Utils.tryGetValue(this.locationInfo.regionNameByReadEndpoint, new RegionalRoutingContext(locationEndpoint), regionName)) {
                return regionName.v;
            }
        }

        //If preferred list is not set, locationEndpoint will be default endpoint, so return the hub region
        return this.locationInfo.availableWriteLocations.get(0).toLowerCase(Locale.ROOT);
    }

    private boolean areEqual(RegionalRoutingContext url1, RegionalRoutingContext url2) {
        return url1.equals(url2);
    }

    private void clearStaleEndpointUnavailabilityInfo() {
        if (!this.locationUnavailabilityInfoByEndpoint.isEmpty()) {
            List<RegionalRoutingContext> unavailableEndpoints = new ArrayList<>(this.locationUnavailabilityInfoByEndpoint.keySet());

            for (RegionalRoutingContext unavailableEndpoint: unavailableEndpoints) {
                Utils.ValueHolder<LocationUnavailabilityInfo> unavailabilityInfoHolder = new Utils.ValueHolder<>();
                Utils.ValueHolder<LocationUnavailabilityInfo> removedHolder = new Utils.ValueHolder<>();

                if (Utils.tryGetValue(this.locationUnavailabilityInfoByEndpoint, unavailableEndpoint, unavailabilityInfoHolder)
                        &&
                        durationPassed(Instant.now(), unavailabilityInfoHolder.v.lastUnavailabilityCheckTimeStamp,
                                this.unavailableLocationsExpirationTime)

                        && Utils.tryRemove(this.locationUnavailabilityInfoByEndpoint, unavailableEndpoint, removedHolder)) {
                    logger.debug(
                            "Removed endpoint [{}] unavailable for operations [{}] from unavailableEndpoints",
                            unavailableEndpoint,
                            unavailabilityInfoHolder.v.unavailableOperations);
                }
            }
        }
    }

    private boolean isEndpointUnavailable(RegionalRoutingContext endpoint, OperationType expectedAvailableOperations) {
        Utils.ValueHolder<LocationUnavailabilityInfo> unavailabilityInfoHolder = new Utils.ValueHolder<>();

        if (expectedAvailableOperations == OperationType.None
                || !Utils.tryGetValue(this.locationUnavailabilityInfoByEndpoint, endpoint, unavailabilityInfoHolder)
                || !unavailabilityInfoHolder.v.unavailableOperations.supports(expectedAvailableOperations)) {
            return false;
        } else {
            if (durationPassed(Instant.now(), unavailabilityInfoHolder.v.lastUnavailabilityCheckTimeStamp, this.unavailableLocationsExpirationTime)) {
                return false;
            } else {
                logger.debug(
                        "Endpoint [{}] unavailable for operations [{}] present in unavailableEndpoints",
                        endpoint,
                        unavailabilityInfoHolder.v.unavailableOperations);
                // Unexpired entry present. Endpoint is unavailable
                return true;
            }
        }
    }

    private boolean anyEndpointsAvailable(List<RegionalRoutingContext> endpoints, OperationType expectedAvailableOperations) {
        Utils.ValueHolder<LocationUnavailabilityInfo> unavailabilityInfoHolder = new Utils.ValueHolder<>();
        boolean anyEndpointsAvailable = false;
        for (RegionalRoutingContext endpoint : endpoints) {
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
                new RegionalRoutingContext(unavailableEndpoint),
                new BiFunction<RegionalRoutingContext, LocationUnavailabilityInfo, LocationUnavailabilityInfo>() {
                    @Override
                    public LocationUnavailabilityInfo apply(RegionalRoutingContext url, LocationUnavailabilityInfo info) {
                        if (info == null) {
                            // not already present, add
                            return new LocationUnavailabilityInfo(currentTime, unavailableOperationType);
                        } else {
                            // already present, update
                            info.lastUnavailabilityCheckTimeStamp = currentTime;
                            info.unavailableOperations = OperationType.combine(info.unavailableOperations, unavailableOperationType);
                            return info;
                        }
                    }
                });

        this.updateLocationCache();

        logger.debug(
                "Endpoint [{}] unavailable for [{}] added/updated to unavailableEndpoints with timestamp [{}]",
                unavailableEndpoint,
                unavailableOperationType,
                updatedInfo.lastUnavailabilityCheckTimeStamp);
    }

    private void updateLocationCache(){
        updateLocationCache(null, null, null, null);
    }

    private void updateLocationCache(
            Iterable<DatabaseAccountLocation> gatewayWriteLocations,
            Iterable<DatabaseAccountLocation> gatewayReadLocations,
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

            if (gatewayReadLocations != null) {
                Utils.ValueHolder<UnmodifiableList<String>> readValueHolder = Utils.ValueHolder.initialize(nextLocationInfo.availableReadLocations);
                Utils.ValueHolder<UnmodifiableMap<RegionalRoutingContext, String>> readRegionMapValueHolder = Utils.ValueHolder.initialize(nextLocationInfo.regionNameByReadEndpoint);
                nextLocationInfo.availableReadEndpointsByLocation = this.getEndpointsByLocation(gatewayReadLocations, readValueHolder, readRegionMapValueHolder);
                nextLocationInfo.availableReadLocations =  readValueHolder.v;
                nextLocationInfo.regionNameByReadEndpoint = readRegionMapValueHolder.v;
            }

            if (gatewayWriteLocations != null) {
                Utils.ValueHolder<UnmodifiableList<String>> writeValueHolder = Utils.ValueHolder.initialize(nextLocationInfo.availableWriteLocations);
                Utils.ValueHolder<UnmodifiableMap<RegionalRoutingContext, String>> outWriteRegionMap = Utils.ValueHolder.initialize(nextLocationInfo.regionNameByWriteEndpoint);
                nextLocationInfo.availableWriteEndpointsByLocation = this.getEndpointsByLocation(gatewayWriteLocations, writeValueHolder, outWriteRegionMap);
                nextLocationInfo.availableWriteLocations = writeValueHolder.v;
                nextLocationInfo.regionNameByWriteEndpoint = outWriteRegionMap.v;
            }

            nextLocationInfo.writeEndpoints = this.getPreferredAvailableEndpoints(nextLocationInfo.availableWriteEndpointsByLocation, nextLocationInfo.availableWriteLocations, OperationType.Write, this.defaultRegionalRoutingContext);
            nextLocationInfo.readEndpoints = this.getPreferredAvailableEndpoints(nextLocationInfo.availableReadEndpointsByLocation, nextLocationInfo.availableReadLocations, OperationType.Read, nextLocationInfo.writeEndpoints.get(0));

            if (nextLocationInfo.preferredLocations == null || nextLocationInfo.preferredLocations.isEmpty()) {

                Utils.ValueHolder<String> regionForDefaultEndpoint = new Utils.ValueHolder<>();

                // only set effective preferred locations when default endpoint doesn't map to a regional endpoint
                if (!Utils.tryGetValue(nextLocationInfo.regionNameByReadEndpoint, this.defaultRegionalRoutingContext, regionForDefaultEndpoint)) {
                    nextLocationInfo.effectivePreferredLocations = nextLocationInfo.availableReadLocations;
                }
            }

            this.lastCacheUpdateTimestamp = Instant.now();

            logger.debug("updating location cache finished, new readLocations [{}], new writeLocations [{}]",
                    nextLocationInfo.readEndpoints, nextLocationInfo.writeEndpoints);
            this.locationInfo = nextLocationInfo;
        }
    }

    private UnmodifiableList<RegionalRoutingContext> getPreferredAvailableEndpoints(UnmodifiableMap<String, RegionalRoutingContext> endpointsByLocation,
                                                                                    UnmodifiableList<String> orderedLocations,
                                                                                    OperationType expectedAvailableOperation,
                                                                                    RegionalRoutingContext fallbackRegionalRoutingContext) {
        List<RegionalRoutingContext> endpoints = new ArrayList<>();
        DatabaseAccountLocationsInfo currentLocationInfo = this.locationInfo;
        // if enableEndpointDiscovery is false, we always use the defaultEndpoint that user passed in during documentClient init
        if (this.enableEndpointDiscovery) {
            if (this.canUseMultipleWriteLocations() || expectedAvailableOperation.supports(OperationType.Read)) {
                List<RegionalRoutingContext> unavailableEndpoints = new ArrayList<>();

                // When client can not use multiple write locations, preferred locations list should only be used
                // determining read endpoints order.
                // If client can use multiple write locations, preferred locations list should be used for determining
                // both read and write endpoints order.

                if (currentLocationInfo.preferredLocations != null && !currentLocationInfo.preferredLocations.isEmpty()) {
                    for (String location: currentLocationInfo.preferredLocations) {
                        Utils.ValueHolder<RegionalRoutingContext> endpoint = new Utils.ValueHolder<>();
                        if (Utils.tryGetValue(endpointsByLocation, location, endpoint)) {
                            if (this.isEndpointUnavailable(endpoint.v, expectedAvailableOperation)) {
                                unavailableEndpoints.add(endpoint.v);
                            } else {
                                endpoints.add(endpoint.v);
                            }
                        }
                    }
                } else {
                    for (String location : orderedLocations) {
                        Utils.ValueHolder<RegionalRoutingContext> endpoint = Utils.ValueHolder.initialize(null);
                        if (Utils.tryGetValue(endpointsByLocation, location, endpoint)) {

                            // if defaultEndpoint equals a regional endpoint then use
                            // whatever the fallback endpoint is
                            if (this.defaultRegionalRoutingContext.getGatewayRegionalEndpoint().equals(endpoint.v.getGatewayRegionalEndpoint())) {
                                endpoints = new ArrayList<>();
                                break;
                            }

                            if (this.isEndpointUnavailable(endpoint.v, expectedAvailableOperation)) {
                                unavailableEndpoints.add(endpoint.v);
                            } else {
                                endpoints.add(endpoint.v);
                            }
                        }
                    }
                }

                if (endpoints.isEmpty()) {
                    endpoints.add(fallbackRegionalRoutingContext);
                }

                endpoints.addAll(unavailableEndpoints);
            } else {
                for (String location : orderedLocations) {
                    Utils.ValueHolder<RegionalRoutingContext> endpoint = Utils.ValueHolder.initialize(null);
                    if (!Strings.isNullOrEmpty(location) && // location is empty during manual failover
                        Utils.tryGetValue(endpointsByLocation, location, endpoint)) {
                        endpoints.add(endpoint.v);
                    }
                }
            }
        }

        if (endpoints.isEmpty()) {
            endpoints.add(fallbackRegionalRoutingContext);
        }
        return new UnmodifiableList<>(endpoints);
    }

    private void addEndpoints(
        Iterable<DatabaseAccountLocation> gatewayDbAccountLocations,
        Map<String, RegionalRoutingContext> endpointsByLocation,
        Map<RegionalRoutingContext, String> regionByEndpoint,
        List<String> parsedLocations) {

        if (gatewayDbAccountLocations != null) {
            for (DatabaseAccountLocation gatewayDbAccountLocation : gatewayDbAccountLocations) {
                if (!Strings.isNullOrEmpty(gatewayDbAccountLocation.getName())) {
                    try {

                        String location = gatewayDbAccountLocation.getName().toLowerCase(Locale.ROOT);
                        URI endpoint = new URI(gatewayDbAccountLocation.getEndpoint().toLowerCase(Locale.ROOT));

                        RegionalRoutingContext regionalRoutingContext = new RegionalRoutingContext(endpoint);

                        if (!endpointsByLocation.containsKey(location)) {
                            endpointsByLocation.put(location, regionalRoutingContext);
                        }

                        if (!regionByEndpoint.containsKey(regionalRoutingContext)) {
                            regionByEndpoint.put(regionalRoutingContext, location);
                        }

                        parsedLocations.add(gatewayDbAccountLocation.getName());
                    } catch (Exception e) {

                        logger.warn("Skipping add for location = [{}] and endpoint = [{}] due to exception [{}]",
                            gatewayDbAccountLocation.getName(),
                            gatewayDbAccountLocation.getEndpoint(),
                            e.getMessage());
                    }
                }
            }
        }
    }

    private UnmodifiableMap<String, RegionalRoutingContext> getEndpointsByLocation(Iterable<DatabaseAccountLocation> gatewayLocations,
                                                                                   Utils.ValueHolder<UnmodifiableList<String>> orderedLocations,
                                                                                   Utils.ValueHolder<UnmodifiableMap<RegionalRoutingContext, String>> regionMap) {
        Map<String, RegionalRoutingContext> endpointsByLocation = new CaseInsensitiveMap<>();
        Map<RegionalRoutingContext, String> regionByEndpoint = new CaseInsensitiveMap<>();
        List<String> parsedLocations = new ArrayList<>();

        addEndpoints(gatewayLocations, endpointsByLocation, regionByEndpoint, parsedLocations);

        orderedLocations.v = new UnmodifiableList<>(parsedLocations);
        regionMap.v = (UnmodifiableMap<RegionalRoutingContext, String>) UnmodifiableMap.unmodifiableMap(regionByEndpoint);

        return (UnmodifiableMap<String, RegionalRoutingContext>) UnmodifiableMap.unmodifiableMap(endpointsByLocation);
    }

    public boolean canUseMultipleWriteLocations() {
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
            this.lastUnavailabilityCheckTimeStamp = instant;
            this.unavailableOperations = type;
        }

        public Instant lastUnavailabilityCheckTimeStamp;
        public OperationType unavailableOperations;
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

    private static boolean isExcludedRegionsSupplierConfigured(Supplier<CosmosExcludedRegions> excludedRegionsSupplier) {
        return excludedRegionsSupplier != null && excludedRegionsSupplier.get() != null;
    }

    static class DatabaseAccountLocationsInfo {
        private UnmodifiableList<RegionalRoutingContext> writeEndpoints;
        private UnmodifiableList<RegionalRoutingContext> readEndpoints;
        private UnmodifiableList<String> preferredLocations;
        private UnmodifiableList<String> effectivePreferredLocations;
        // lower-case region
        private UnmodifiableList<String> availableWriteLocations;
        // lower-case region
        private UnmodifiableList<String> availableReadLocations;
        private UnmodifiableMap<String, RegionalRoutingContext> availableWriteEndpointsByLocation;
        private UnmodifiableMap<String, RegionalRoutingContext> availableReadEndpointsByLocation;
        private UnmodifiableMap<RegionalRoutingContext, String> regionNameByWriteEndpoint;
        private UnmodifiableMap<RegionalRoutingContext, String> regionNameByReadEndpoint;

        public DatabaseAccountLocationsInfo(List<String> preferredLocations,
                                            URI defaultEndpoint) {
            this.preferredLocations = new UnmodifiableList<>(preferredLocations.stream().map(loc -> loc.toLowerCase(Locale.ROOT)).collect(Collectors.toList()));
            this.effectivePreferredLocations = new UnmodifiableList<>(Collections.emptyList());
            this.availableWriteEndpointsByLocation
                = (UnmodifiableMap<String, RegionalRoutingContext>) UnmodifiableMap.<String, RegionalRoutingContext>unmodifiableMap(new CaseInsensitiveMap<>());
            this.availableReadEndpointsByLocation
                = (UnmodifiableMap<String, RegionalRoutingContext>) UnmodifiableMap.<String, RegionalRoutingContext>unmodifiableMap(new CaseInsensitiveMap<>());
            this.regionNameByWriteEndpoint
                = (UnmodifiableMap<RegionalRoutingContext, String>) UnmodifiableMap.<RegionalRoutingContext, String>unmodifiableMap(new CaseInsensitiveMap<>());
            this.regionNameByReadEndpoint
                = (UnmodifiableMap<RegionalRoutingContext, String>) UnmodifiableMap.<RegionalRoutingContext, String>unmodifiableMap(new CaseInsensitiveMap<>());
            this.availableReadLocations = new UnmodifiableList<>(Collections.emptyList());
            this.availableWriteLocations = new UnmodifiableList<>(Collections.emptyList());
            this.readEndpoints = new UnmodifiableList<>(Collections.singletonList(new RegionalRoutingContext(defaultEndpoint)));
            this.writeEndpoints = new UnmodifiableList<>(Collections.singletonList(new RegionalRoutingContext(defaultEndpoint)));
        }

        public DatabaseAccountLocationsInfo(DatabaseAccountLocationsInfo other) {
            this.preferredLocations = other.preferredLocations;
            this.effectivePreferredLocations = other.effectivePreferredLocations;
            this.availableWriteLocations = other.availableWriteLocations;
            this.availableReadLocations = other.availableReadLocations;
            this.availableWriteEndpointsByLocation = other.availableWriteEndpointsByLocation;
            this.regionNameByWriteEndpoint = other.regionNameByWriteEndpoint;
            this.regionNameByReadEndpoint = other.regionNameByReadEndpoint;
            this.availableReadEndpointsByLocation = other.availableReadEndpointsByLocation;
            this.writeEndpoints = other.writeEndpoints;
            this.readEndpoints = other.readEndpoints;
        }
    }
}
