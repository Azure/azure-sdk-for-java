// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.collections.map.UnmodifiableMap;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.RegionNameToRegionIdMap;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PartitionScopedRegionLevelProgress {

    private static final Logger logger = LoggerFactory.getLogger(PartitionScopedRegionLevelProgress.class);

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, RegionLevelProgress>> partitionKeyRangeIdToRegionLevelProgress;

    private final ConcurrentHashMap<String, String> normalizedRegionLookupMap;

    public final static String GLOBAL_PROGRESS_KEY = "global";


    public PartitionScopedRegionLevelProgress() {
        this.partitionKeyRangeIdToRegionLevelProgress = new ConcurrentHashMap<>();
        this.normalizedRegionLookupMap = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, RegionLevelProgress>> getPartitionKeyRangeIdToRegionLevelProgress() {
        return this.partitionKeyRangeIdToRegionLevelProgress;
    }

    public void tryRecordSessionToken(
        RxDocumentServiceRequest request,
        ISessionToken parsedSessionToken,
        Long collectionRid,
        String partitionKeyRangeId,
        String firstEffectivePreferredReadableRegion,
        String regionRoutedTo,
        PartitionKeyBasedBloomFilter partitionKeyBasedBloomFilter,
        GlobalEndpointManager globalEndpointManager) {

        checkNotNull(request, "request cannot be null!");
        checkNotNull(request.requestContext, "requestContext cannot be null!");

        try {
            this.partitionKeyRangeIdToRegionLevelProgress.compute(partitionKeyRangeId, (partitionKeyRangeIdAsKey, regionLevelProgressAsVal) -> {

                Utils.ValueHolder<PartitionKeyInternal> partitionKeyInternal = Utils.ValueHolder.initialize(null);
                Utils.ValueHolder<PartitionKeyDefinition> partitionKeyDefinition = Utils.ValueHolder.initialize(null);

                // bloom filter is used only when the request is targeted to a specific logical partition and
                // multiple write locations are configured
                // and a global session token is not already used for the partition key range
                boolean shouldUseBloomFilter = shouldUseBloomFilter(
                    request,
                    partitionKeyRangeId,
                    partitionKeyInternal,
                    partitionKeyDefinition,
                    this,
                    globalEndpointManager
                );

                if (regionLevelProgressAsVal == null) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding newly encountered partitionKeyRangeId - {}", partitionKeyRangeId);
                    }

                    regionLevelProgressAsVal = new ConcurrentHashMap<>();
                }

                // store the global merged progress of the session token for a given physical partition
                regionLevelProgressAsVal.merge(GLOBAL_PROGRESS_KEY, new RegionLevelProgress(Long.MIN_VALUE, Long.MIN_VALUE, parsedSessionToken, new AtomicBoolean(false), new AtomicBoolean(false)), (regionLevelProgressExisting, regionLevelProgressNew) -> {

                    ISessionToken existingSessionToken = regionLevelProgressExisting.sessionToken;
                    ISessionToken newSessionToken = regionLevelProgressNew.sessionToken;
                    AtomicBoolean hasPartitionSeenNonPointDocumentOperationsInner = regionLevelProgressExisting.hasPartitionSeenNonPointDocumentOperations;
                    AtomicBoolean isGlobalSessionTokenUsedForPartitionKeyRangeInner = regionLevelProgressExisting.isGlobalSessionTokenUsedForPartitionKeyRange;

                    return new RegionLevelProgress(Long.MIN_VALUE, Long.MIN_VALUE, existingSessionToken.merge(newSessionToken), hasPartitionSeenNonPointDocumentOperationsInner, isGlobalSessionTokenUsedForPartitionKeyRangeInner);
                });

                RegionLevelProgress globalLevelProgress = regionLevelProgressAsVal.get(GLOBAL_PROGRESS_KEY);

                checkNotNull(globalLevelProgress, "globalLevelProgress cannot be null!");

                // identify whether regionRoutedTo has a regionId mapping in session token
                Utils.ValueHolder<UnmodifiableMap<Integer, Long>> localLsnByRegion = new Utils.ValueHolder<>();

                // if regionId to localLsn mappings cannot be extracted, the only mapping would be "global" to whatever is the session token corresponding to the globalLsn seen across all regions
                if (!SessionTokenHelper.tryEvaluateLocalLsnByRegionMappingWithNullSafety(parsedSessionToken, localLsnByRegion)) {
                    request.requestContext.getSessionTokenEvaluationResults().add("Recording only the global session token either because session token doesn't have region id to localLsn mappings or is not a vector session token.");
                    globalLevelProgress.isGlobalSessionTokenUsedForPartitionKeyRange.set(true);
                    return regionLevelProgressAsVal;
                }

                if (request.getResourceType() == ResourceType.Document) {
                    // here the request is not pinned to a specific PartitionKey instance (logical partition)
                    if (!this.isRequestScopedToLogicalPartition(request)) {
                        globalLevelProgress.hasPartitionSeenNonPointDocumentOperations.set(true);
                    }
                }

                if (globalLevelProgress.hasPartitionSeenNonPointDocumentOperations.get()) {
                    request.requestContext.getSessionTokenEvaluationResults().add("Recording only the global session token either because the partition has seen non-point operations.");
                    globalLevelProgress.isGlobalSessionTokenUsedForPartitionKeyRange.set(true);
                    return regionLevelProgressAsVal;
                }

                this.normalizedRegionLookupMap.computeIfAbsent(regionRoutedTo, (regionRoutedToAsVal) -> regionRoutedToAsVal.toLowerCase(Locale.ROOT).trim().replace(" ", ""));

                String normalizedRegionRoutedTo = this.normalizedRegionLookupMap.get(regionRoutedTo);

                int regionId = RegionNameToRegionIdMap.getRegionId(normalizedRegionRoutedTo);

                if (regionId != -1) {
                    long localLsn = localLsnByRegion.v.getOrDefault(regionId, Long.MIN_VALUE);

                    // regionId maps to a satellite region
                    if (localLsn != Long.MIN_VALUE) {
                        regionLevelProgressAsVal.compute(normalizedRegionRoutedTo, (normalizedRegionAsKey, regionLevelProgressAsValInner) -> {

                            request.requestContext.getSessionTokenEvaluationResults().add("Recording region specific progress of region : " + normalizedRegionRoutedTo + ".");

                            if (shouldUseBloomFilter) {
                                partitionKeyBasedBloomFilter.tryRecordPartitionKey(
                                    request,
                                    collectionRid,
                                    firstEffectivePreferredReadableRegion,
                                    normalizedRegionRoutedTo,
                                    request.getPartitionKeyInternal(),
                                    request.getPartitionKeyDefinition());
                            }

                            if (regionLevelProgressAsValInner == null) {
                                return new RegionLevelProgress(parsedSessionToken.getLSN(), localLsn, null, globalLevelProgress.hasPartitionSeenNonPointDocumentOperations, globalLevelProgress.isGlobalSessionTokenUsedForPartitionKeyRange);
                            }

                            // regionLevelProgressAsValInner.parsedSessionToken is passed
                            // to have a session token to merge with in case normalizedRegionRoutedTo
                            // is equal to the first preferred region in the subsequent step
                            return new RegionLevelProgress(
                                Math.max(regionLevelProgressAsValInner.getMaxGlobalLsnSeen(), parsedSessionToken.getLSN()),
                                Math.max(regionLevelProgressAsValInner.getMaxLocalLsnSeen(), localLsn),
                                regionLevelProgressAsValInner.sessionToken,
                                globalLevelProgress.hasPartitionSeenNonPointDocumentOperations,
                                globalLevelProgress.isGlobalSessionTokenUsedForPartitionKeyRange);
                        });
                    }
                    // regionId maps to a hub region
                    else {
                        regionLevelProgressAsVal.compute(normalizedRegionRoutedTo, (normalizedRegionAsKey, regionLevelProgressAsValInner) -> {

                            request.requestContext.getSessionTokenEvaluationResults().add("Recording region specific progress of region : " + normalizedRegionRoutedTo + ".");

                            if (shouldUseBloomFilter) {
                                partitionKeyBasedBloomFilter.tryRecordPartitionKey(
                                    request,
                                    collectionRid,
                                    firstEffectivePreferredReadableRegion,
                                    normalizedRegionRoutedTo,
                                    request.getPartitionKeyInternal(),
                                    request.getPartitionKeyDefinition());
                            }

                            if (regionLevelProgressAsValInner == null) {
                                return new RegionLevelProgress(parsedSessionToken.getLSN(), Long.MIN_VALUE, null, globalLevelProgress.hasPartitionSeenNonPointDocumentOperations, globalLevelProgress.isGlobalSessionTokenUsedForPartitionKeyRange);
                            }

                            // regionLevelProgressAsValInner.parsedSessionToken is passed
                            // to have a session token to merge with in case normalizedRegionRoutedTo
                            // is equal to the first preferred region in the subsequent step
                            return new RegionLevelProgress(
                                Math.max(regionLevelProgressAsValInner.getMaxGlobalLsnSeen(), parsedSessionToken.getLSN()),
                                Long.MIN_VALUE,
                                regionLevelProgressAsValInner.sessionToken,
                                globalLevelProgress.hasPartitionSeenNonPointDocumentOperations,
                                globalLevelProgress.isGlobalSessionTokenUsedForPartitionKeyRange);
                        });
                    }

                    // store the session token in parsed form if obtained from the firstEffectivePreferredReadableRegion (a merge is necessary to store latest progress from first preferred region)
                    if (normalizedRegionRoutedTo.equals(firstEffectivePreferredReadableRegion)) {
                        regionLevelProgressAsVal.compute(normalizedRegionRoutedTo, (normalizedRegionAsKey, regionLevelProgressAsValInner) -> {

                            request.requestContext.getSessionTokenEvaluationResults().add("Recording region specific progress of first preferred region : " + regionRoutedTo + ".");

                            if (shouldUseBloomFilter) {
                                partitionKeyBasedBloomFilter.tryRecordPartitionKey(
                                    request,
                                    collectionRid,
                                    firstEffectivePreferredReadableRegion,
                                    normalizedRegionRoutedTo,
                                    request.getPartitionKeyInternal(),
                                    request.getPartitionKeyDefinition());
                            }

                            if (regionLevelProgressAsValInner == null) {
                                return new RegionLevelProgress(parsedSessionToken.getLSN(), Long.MIN_VALUE, parsedSessionToken, globalLevelProgress.hasPartitionSeenNonPointDocumentOperations, globalLevelProgress.isGlobalSessionTokenUsedForPartitionKeyRange);
                            }

                            ISessionToken mergedBasedSessionToken = regionLevelProgressAsValInner.sessionToken == null ? parsedSessionToken : regionLevelProgressAsValInner.sessionToken.merge(parsedSessionToken);

                            return new RegionLevelProgress(
                                Math.max(regionLevelProgressAsValInner.getMaxGlobalLsnSeen(), parsedSessionToken.getLSN()),
                                Long.MIN_VALUE,
                                mergedBasedSessionToken,
                                globalLevelProgress.hasPartitionSeenNonPointDocumentOperations,
                                globalLevelProgress.isGlobalSessionTokenUsedForPartitionKeyRange);
                        });
                    }
                } else {
                    request.requestContext.getSessionTokenEvaluationResults().add("Recording only the global session token since session token provided doesn't have a known region id mapping for region : " + normalizedRegionRoutedTo + ".");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Region with name - {} which has no known regionId has been seen", normalizedRegionRoutedTo);
                    }
                }

                return regionLevelProgressAsVal;
            });
        } catch (Throwable t) {
            if (!(t instanceof CosmosException)) {
                throw constructInternalServerErrorException(request, t.getMessage());
            } else {
                throw t;
            }
        }
    }

    public ISessionToken tryResolveSessionToken(
        RxDocumentServiceRequest request,
        Long collectionRid,
        String partitionKeyRangeId,
        String firstEffectivePreferredReadableRegion,
        PartitionKeyBasedBloomFilter partitionKeyBasedBloomFilter,
        GlobalEndpointManager globalEndpointManager) {

        checkNotNull(request, "request cannot be null!");
        checkNotNull(request.requestContext, "requestContext cannot be null!");

        Utils.ValueHolder<ISessionToken> resultantSessionToken = new Utils.ValueHolder<>(null);

        try {
            this.partitionKeyRangeIdToRegionLevelProgress.compute(partitionKeyRangeId, (partitionKeyRangeIdAsKey, regionLevelProgressAsVal) -> {

                Optional<RegionLevelProgress> globalLevelProgress = resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, GLOBAL_PROGRESS_KEY);
                Utils.ValueHolder<PartitionKeyInternal> partitionKeyInternal = Utils.ValueHolder.initialize(null);
                Utils.ValueHolder<PartitionKeyDefinition> partitionKeyDefinition = Utils.ValueHolder.initialize(null);

                // bloom filter is used only when the request is targeted to a specific logical partition and
                // multiple write locations are configured
                // and a global session token is not alre used for the partition key range
                boolean canUseRegionScopedSessionTokens = shouldUseBloomFilter(
                    request,
                    partitionKeyRangeId,
                    partitionKeyInternal,
                    partitionKeyDefinition,
                    this,
                    globalEndpointManager
                );

                Set<String> lesserPreferredRegionsPkProbablyRequestedFrom = new HashSet<>();

                // obtain the list of regions (excluding the first preferred region)
                // where the partition key of the request has probably seen requests routed to
                if (canUseRegionScopedSessionTokens) {
                    lesserPreferredRegionsPkProbablyRequestedFrom = partitionKeyBasedBloomFilter
                        .tryGetPossibleRegionsLogicalPartitionResolvedTo(
                            request,
                            collectionRid,
                            partitionKeyInternal.v,
                            partitionKeyDefinition.v);
                }

                // globalLevelProgress cannot be null since it is always added irrespective of routing
                if (!globalLevelProgress.isPresent()) {
                    throw constructInternalServerErrorException(request, "globalLevelProgress cannot be null!");
                }

                Optional<ISessionToken> globalSessionToken = globalLevelProgress.get().getSessionToken();

                if (!globalSessionToken.isPresent()) {
                    throw constructInternalServerErrorException(request, "globalSessionToken cannot be null!");
                }

                ISessionToken globalSessionTokenAsVal = globalSessionToken.get();

                // if region level scoping is not allowed, then resolve to the global session token
                // region level scoping is allowed in the following cases:
                //      1. when the request is targeted to a specific logical partition
                //      2. when multiple write locations are configured
                if (!canUseRegionScopedSessionTokens) {
                    resultantSessionToken.v = fallbackToGlobalSessionToken(request, globalLevelProgress);
                    return regionLevelProgressAsVal;
                }

                // baseLevelProgress corresponds to the session token recorded for the first preferred readable region
                // as the first preferred readable region is not recorded in the bloom filter
                // it is necessary to always obtain the session token recorded for the first preferred readable region
                // and use it as the base session token to construct the resultant session token
                Optional<RegionLevelProgress> baseLevelProgress = resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, firstEffectivePreferredReadableRegion);

                if (!baseLevelProgress.isPresent() || !baseLevelProgress.get().getSessionToken().isPresent()) {
                    resultantSessionToken.v = fallbackToGlobalSessionToken(request, globalLevelProgress);
                    return regionLevelProgressAsVal;
                }

                Optional<ISessionToken> baseSessionToken = baseLevelProgress.get().getSessionToken();

                // the partition key of the request has not been requested from lesser preferred regions
                // hence just return the base session token or in other words the session recorded for the first preferred readable region
                if (lesserPreferredRegionsPkProbablyRequestedFrom.isEmpty() && baseSessionToken.isPresent()) {
                    resultantSessionToken.v = baseSessionToken.get();
                    return regionLevelProgressAsVal;
                }

                long globalLsn = -1;

                Utils.ValueHolder<UnmodifiableMap<Integer, Long>> localLsnByRegion = new Utils.ValueHolder<>();

                // return global merged progress of the session token for a given physical partition since regionId to localLsn mappings cannot be resolved
                if (!SessionTokenHelper.tryEvaluateLocalLsnByRegionMappingWithNullSafety(globalSessionTokenAsVal, localLsnByRegion)) {
                    resultantSessionToken.v = fallbackToGlobalSessionToken(request, globalLevelProgress);
                    return regionLevelProgressAsVal;
                }

                Utils.ValueHolder<Long> sessionTokenVersion = new Utils.ValueHolder<>();

                // return global merged progress of the session token for a given physical partition since version cannot be resolved
                if (!SessionTokenHelper.tryEvaluateVersion(globalSessionTokenAsVal, sessionTokenVersion)) {
                    resultantSessionToken.v = fallbackToGlobalSessionToken(request, globalLevelProgress);
                    return regionLevelProgressAsVal;
                }

                StringBuilder sbPartOne = new StringBuilder();
                StringBuilder sbPartTwo = new StringBuilder();

                // iterate through the regionId to localLsn mappings of the session token (satellite regions only)
                // find overlap between regions the partition key has probably seen requests routed to
                for (Map.Entry<Integer, Long> localLsnByRegionEntry : localLsnByRegion.v.entrySet()) {

                    int regionId = localLsnByRegionEntry.getKey();
                    String normalizedRegionName = RegionNameToRegionIdMap.getRegionName(regionId);

                    // the regionId to normalizedRegionName does not exist
                    if (normalizedRegionName.equals(StringUtils.EMPTY)) {

                        if (logger.isDebugEnabled()) {
                            logger.debug("regionId with value - {} which has no known region name has been seen in the vector session token", regionId);
                        }

                        resultantSessionToken.v = fallbackToGlobalSessionToken(request, globalLevelProgress);
                        return regionLevelProgressAsVal;
                    }

                    // start constructing the session token using the localLsn and globalLsn from lesser preferred regions
                    if (lesserPreferredRegionsPkProbablyRequestedFrom.contains(normalizedRegionName)) {
                        request.requestContext.getSessionTokenEvaluationResults().add("Resolving region specific progress from " + normalizedRegionName);
                        Optional<RegionLevelProgress> satelliteRegionProgress = this.resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, normalizedRegionName);

                        if (satelliteRegionProgress.isPresent()) {
                            globalLsn = Math.max(globalLsn, satelliteRegionProgress.get().getMaxGlobalLsnSeen());
                            sbPartTwo.append(VectorSessionToken.SegmentSeparator);
                            sbPartTwo.append(regionId);
                            sbPartTwo.append(VectorSessionToken.RegionProgressSeparator);
                            sbPartTwo.append(satelliteRegionProgress.get().getMaxLocalLsnSeen());
                            lesserPreferredRegionsPkProbablyRequestedFrom.remove(normalizedRegionName);
                        } else {

                            // this condition should ideally be hit probabilistically since the bloom filter
                            // indicated that the partition key has probably seen requests routed to this region but the session container doesn't have any region specific progress recorded
                            // hence resolve to the global session token recorded prior
                            resultantSessionToken.v = fallbackToGlobalSessionToken(request, globalLevelProgress);
                            return regionLevelProgressAsVal;
                        }
                    } else {
                        request.requestContext.getSessionTokenEvaluationResults().add("No region specific progress to resolve from " + normalizedRegionName);
                        sbPartTwo.append(VectorSessionToken.SegmentSeparator);
                        sbPartTwo.append(regionId);
                        sbPartTwo.append(VectorSessionToken.RegionProgressSeparator);
                        sbPartTwo.append(-1);
                    }
                }

                Utils.ValueHolder<ISessionToken> hubSessionToken = new Utils.ValueHolder<>(null);

                // Obtain globalLsn from hub region
                for (String lesserPreferredRegionPkProbablyRequestedFrom : lesserPreferredRegionsPkProbablyRequestedFrom) {
                    int regionId = RegionNameToRegionIdMap.getRegionId(lesserPreferredRegionPkProbablyRequestedFrom);
                    boolean isHubRegion = !localLsnByRegion.v.containsKey(regionId);

                    if (isHubRegion) {
                        Optional<RegionLevelProgress> hubLevelProgress = this.resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, lesserPreferredRegionPkProbablyRequestedFrom);

                        if (hubLevelProgress.isPresent()) {
                            hubSessionToken.v = hubLevelProgress.get().getSessionToken().orElse(null);
                        } else {
                            resultantSessionToken.v = fallbackToGlobalSessionToken(request, globalLevelProgress);
                            return regionLevelProgressAsVal;
                        }
                    }
                }

                sbPartOne.append(sessionTokenVersion.v);
                sbPartOne.append(VectorSessionToken.SegmentSeparator);
                sbPartOne.append(globalLsn);

                Utils.ValueHolder<ISessionToken> resolvedSessionToken = new Utils.ValueHolder<>(null);

                // one additional step of merging base session token / first preferred read region and resolved session token
                if (SessionTokenHelper.tryParse(sbPartOne.append(sbPartTwo).toString(), resolvedSessionToken)) {

                    ISessionToken baseSessionTokenAsVal = baseSessionToken.get();

                    if (hubSessionToken.v != null) {
                        resultantSessionToken.v
                            = baseSessionTokenAsVal.merge(hubSessionToken.v).merge(resolvedSessionToken.v);
                    } else {
                        resultantSessionToken.v
                            = baseSessionTokenAsVal.merge(resolvedSessionToken.v);
                    }

                    return regionLevelProgressAsVal;
                }

                resultantSessionToken.v = fallbackToGlobalSessionToken(request, globalLevelProgress);
                return regionLevelProgressAsVal;
            });

            return resultantSessionToken.v;
        } catch (Throwable t) {
            if (!(t instanceof CosmosException)) {
                throw constructInternalServerErrorException(request, t.getMessage());
            } else {
                throw t;
            }
        }
    }

    public boolean isPartitionKeyRangeIdPresent(String partitionKeyRangeId) {
        return this.partitionKeyRangeIdToRegionLevelProgress.containsKey(partitionKeyRangeId);
    }

    private Optional<RegionLevelProgress> resolvePartitionKeyRangeIdBasedProgress(String partitionKeyRangeId, String progressScope) {

        ConcurrentHashMap<String, RegionLevelProgress> regionToRegionLevelProgress
            = this.partitionKeyRangeIdToRegionLevelProgress.get(partitionKeyRangeId);

        checkNotNull(regionToRegionLevelProgress, "regionToRegionLevelProgress cannot be null!");

        return Optional.ofNullable(regionToRegionLevelProgress.get(progressScope));
    }

    private boolean isRequestScopedToLogicalPartition(RxDocumentServiceRequest request) {
        return request.getPartitionKeyInternal() != null;
    }

    public boolean useGlobalSessionTokenForPartitionKeyRangeId(String partitionKeyRangeId) {

        if (this.partitionKeyRangeIdToRegionLevelProgress.containsKey(partitionKeyRangeId)) {
            ConcurrentHashMap<String, RegionLevelProgress> regionToRegionLevelProgress = this.partitionKeyRangeIdToRegionLevelProgress.get(partitionKeyRangeId);
            RegionLevelProgress regionLevelProgress = regionToRegionLevelProgress.get(GLOBAL_PROGRESS_KEY);

            if (regionLevelProgress == null) {
                return false;
            } else {
                return regionLevelProgress.hasPartitionSeenNonPointDocumentOperations.get() || regionLevelProgress.isGlobalSessionTokenUsedForPartitionKeyRange.get();
            }
        }

        return false;
    }

    private static CosmosException constructInternalServerErrorException(RxDocumentServiceRequest request, String message) {
        CosmosException cosmosException = new InternalServerErrorException(
            message,
            HttpConstants.SubStatusCodes.REGION_SCOPED_SESSION_CONTAINER_IN_BAD_STATE);

        BridgeInternal.setCosmosDiagnostics(cosmosException, request.requestContext.cosmosDiagnostics);

        return cosmosException;
    }

    private static boolean shouldUseBloomFilter(
        RxDocumentServiceRequest request,
        String partitionKeyRangeId,
        Utils.ValueHolder<PartitionKeyInternal> partitionKeyInternal,
        Utils.ValueHolder<PartitionKeyDefinition> partitionKeyDefinition,
        PartitionScopedRegionLevelProgress partitionScopedRegionLevelProgress,
        GlobalEndpointManager globalEndpointManager) {

        checkNotNull(request, "request cannot be null!");
        checkNotNull(globalEndpointManager, "globalEndpointManager cannot be null!");
        checkNotNull(partitionScopedRegionLevelProgress, "partitionScopedRegionLevelProgress cannot be null!");

        partitionKeyInternal.v = request.getPartitionKeyInternal();

        if (partitionKeyInternal.v == null) {
            return false;
        }

        partitionKeyDefinition.v = request.getPartitionKeyDefinition();

        if (partitionKeyDefinition.v == null) {
            return false;
        }

        if (partitionScopedRegionLevelProgress.useGlobalSessionTokenForPartitionKeyRangeId(partitionKeyRangeId)) {
            return false;
        }

        return globalEndpointManager.canUseMultipleWriteLocations(request);
    }

    private static ISessionToken fallbackToGlobalSessionToken(
        RxDocumentServiceRequest request,
        Optional<RegionLevelProgress> globalLevelProgress) {

        if (!globalLevelProgress.isPresent()) {
            throw constructInternalServerErrorException(request, "globalSessionToken cannot be null!");
        }

        Optional<ISessionToken> globalSessionToken = globalLevelProgress.get().getSessionToken();

        if (!globalSessionToken.isPresent()) {
            throw constructInternalServerErrorException(request, "globalSessionToken cannot be null!");
        }

        RegionLevelProgress globalLevelProgressAsVal = globalLevelProgress.get();
        ISessionToken globalSessionTokenAsVal = globalSessionToken.get();

        globalLevelProgressAsVal.isGlobalSessionTokenUsedForPartitionKeyRange.set(true);
        request.requestContext.getSessionTokenEvaluationResults().add("Resolving to the global session token recorded prior since region specific progress couldn't be found.");

        return globalSessionTokenAsVal;
    }

    static class RegionLevelProgress {
        private final long maxGlobalLsnSeen;
        private final long maxLocalLsnSeen;
        private final ISessionToken sessionToken;
        private final AtomicBoolean hasPartitionSeenNonPointDocumentOperations;
        private final AtomicBoolean isGlobalSessionTokenUsedForPartitionKeyRange;

        public RegionLevelProgress(long maxGlobalLsnSeen, long maxLocalLsnSeen, ISessionToken sessionToken, AtomicBoolean hasPartitionSeenNonPointDocumentOperations, AtomicBoolean isGlobalSessionTokenUsedForPartitionKeyRange) {
            this.maxGlobalLsnSeen = maxGlobalLsnSeen;
            this.maxLocalLsnSeen = maxLocalLsnSeen;
            this.sessionToken = sessionToken;
            this.hasPartitionSeenNonPointDocumentOperations = hasPartitionSeenNonPointDocumentOperations;
            this.isGlobalSessionTokenUsedForPartitionKeyRange = isGlobalSessionTokenUsedForPartitionKeyRange;
        }

        public long getMaxGlobalLsnSeen() {
            return maxGlobalLsnSeen;
        }

        public long getMaxLocalLsnSeen() {
            return maxLocalLsnSeen;
        }

        public Optional<ISessionToken> getSessionToken() {
            return Optional.ofNullable(sessionToken);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegionLevelProgress that = (RegionLevelProgress) o;
            return maxGlobalLsnSeen == that.maxGlobalLsnSeen &&
                maxLocalLsnSeen == that.maxLocalLsnSeen &&
                Objects.equals(sessionToken, that.sessionToken) &&
                hasPartitionSeenNonPointDocumentOperations.get() == that.hasPartitionSeenNonPointDocumentOperations.get();
        }

        @Override
        public int hashCode() {
            return Objects.hash(maxGlobalLsnSeen, maxLocalLsnSeen, sessionToken, hasPartitionSeenNonPointDocumentOperations);
        }
    }
}
