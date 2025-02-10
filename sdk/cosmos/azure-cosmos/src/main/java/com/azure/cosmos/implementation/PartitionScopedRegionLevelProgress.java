// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.collections.map.UnmodifiableMap;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.RegionNameToRegionIdMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

    public void tryRecordSessionToken(RxDocumentServiceRequest request, ISessionToken parsedSessionToken, String partitionKeyRangeId, String firstEffectivePreferredReadableRegion, String regionRoutedTo) {

        checkNotNull(request, "request cannot be null!");
        checkNotNull(request.requestContext, "requestContext cannot be null!");

        this.partitionKeyRangeIdToRegionLevelProgress.compute(partitionKeyRangeId, (partitionKeyRangeIdAsKey, regionLevelProgressAsVal) -> {

            if (regionLevelProgressAsVal == null) {

                if (logger.isDebugEnabled()) {
                    logger.debug("Adding newly encountered partitionKeyRangeId - {}", partitionKeyRangeId);
                }

                regionLevelProgressAsVal = new ConcurrentHashMap<>();
            }

            // store the global merged progress of the session token for a given physical partition
            regionLevelProgressAsVal.merge(GLOBAL_PROGRESS_KEY, new RegionLevelProgress(Long.MIN_VALUE, Long.MIN_VALUE, parsedSessionToken, new AtomicBoolean(false)), (regionLevelProgressExisting, regionLevelProgressNew) -> {

                ISessionToken existingSessionToken = regionLevelProgressExisting.sessionToken;
                ISessionToken newSessionToken = regionLevelProgressNew.sessionToken;
                AtomicBoolean hasPartitionSeenNonPointDocumentOperationsInner = regionLevelProgressExisting.hasPartitionSeenNonPointDocumentOperations;

                return new RegionLevelProgress(Long.MIN_VALUE, Long.MIN_VALUE, existingSessionToken.merge(newSessionToken), hasPartitionSeenNonPointDocumentOperationsInner);
            });

            RegionLevelProgress globalLevelProgress = regionLevelProgressAsVal.get(GLOBAL_PROGRESS_KEY);

            checkNotNull(globalLevelProgress, "globalLevelProgress cannot be null!");

            // identify whether regionRoutedTo has a regionId mapping in session token
            Utils.ValueHolder<UnmodifiableMap<Integer, Long>> localLsnByRegion = new Utils.ValueHolder<>();

            // if regionId to localLsn mappings cannot be extracted, the only mapping would be "global" to whatever is the session token corresponding to the globalLsn seen across all regions
            if (!SessionTokenHelper.tryEvaluateLocalLsnByRegionMappingWithNullSafety(parsedSessionToken, localLsnByRegion)) {
                request.requestContext.getSessionTokenEvaluationResults().add("Recording only the global session token either because session token doesn't have region id to localLsn mappings or is not a vector session token.");
                return regionLevelProgressAsVal;
            }

            if (request.getResourceType() == ResourceType.Document) {
                if (!this.isRequestScopedToLogicalPartition(request)) {
                    globalLevelProgress.hasPartitionSeenNonPointDocumentOperations.set(true);
                }
            }

            if (globalLevelProgress.hasPartitionSeenNonPointDocumentOperations.get()) {
                request.requestContext.getSessionTokenEvaluationResults().add("Recording only the global session token either because the partition has seen non-point operations.");
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

                        if (regionLevelProgressAsValInner == null) {
                            return new RegionLevelProgress(parsedSessionToken.getLSN(), localLsn, null, globalLevelProgress.hasPartitionSeenNonPointDocumentOperations);
                        }

                        // regionLevelProgressAsValInner.parsedSessionToken is passed
                        // to have a session token to merge with in case normalizedRegionRoutedTo
                        // is equal to the first preferred region in the subsequent step
                        return new RegionLevelProgress(
                            Math.max(regionLevelProgressAsValInner.getMaxGlobalLsnSeen(), parsedSessionToken.getLSN()),
                            Math.max(regionLevelProgressAsValInner.getMaxLocalLsnSeen(), localLsn),
                            regionLevelProgressAsValInner.sessionToken,
                            globalLevelProgress.hasPartitionSeenNonPointDocumentOperations);
                    });
                }
                // regionId maps to a hub region
                else {
                    regionLevelProgressAsVal.compute(normalizedRegionRoutedTo, (normalizedRegionAsKey, regionLevelProgressAsValInner) -> {

                        request.requestContext.getSessionTokenEvaluationResults().add("Recording region specific progress of region : " + normalizedRegionRoutedTo + ".");

                        if (regionLevelProgressAsValInner == null) {
                            return new RegionLevelProgress(parsedSessionToken.getLSN(), Long.MIN_VALUE, null, globalLevelProgress.hasPartitionSeenNonPointDocumentOperations);
                        }

                        // regionLevelProgressAsValInner.parsedSessionToken is passed
                        // to have a session token to merge with in case normalizedRegionRoutedTo
                        // is equal to the first preferred region in the subsequent step
                        return new RegionLevelProgress(
                            Math.max(regionLevelProgressAsValInner.getMaxGlobalLsnSeen(), parsedSessionToken.getLSN()),
                            Long.MIN_VALUE,
                            regionLevelProgressAsValInner.sessionToken,
                            globalLevelProgress.hasPartitionSeenNonPointDocumentOperations);
                    });
                }

                // store the session token in parsed form if obtained from the firstEffectivePreferredReadableRegion (a merge is necessary to store latest progress from first preferred region)
                if (normalizedRegionRoutedTo.equals(firstEffectivePreferredReadableRegion)) {
                    regionLevelProgressAsVal.compute(normalizedRegionRoutedTo, (normalizedRegionAsKey, regionLevelProgressAsValInner) -> {

                        request.requestContext.getSessionTokenEvaluationResults().add("Recording region specific progress of first preferred region : " + regionRoutedTo + ".");

                        if (regionLevelProgressAsValInner == null) {
                            return new RegionLevelProgress(parsedSessionToken.getLSN(), Long.MIN_VALUE, parsedSessionToken, globalLevelProgress.hasPartitionSeenNonPointDocumentOperations);
                        }

                        ISessionToken mergedBasedSessionToken = regionLevelProgressAsValInner.sessionToken == null ? parsedSessionToken : regionLevelProgressAsValInner.sessionToken.merge(parsedSessionToken);

                        return new RegionLevelProgress(
                            Math.max(regionLevelProgressAsValInner.getMaxGlobalLsnSeen(), parsedSessionToken.getLSN()),
                            Long.MIN_VALUE,
                            mergedBasedSessionToken,
                            globalLevelProgress.hasPartitionSeenNonPointDocumentOperations);
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
    }

    public ISessionToken tryResolveSessionToken(
        RxDocumentServiceRequest request,
        Set<String> lesserPreferredRegionsPkProbablyRequestedFrom,
        String partitionKeyRangeId,
        String firstEffectivePreferredReadableRegion,
        boolean canUseRegionScopedSessionTokens) {

        checkNotNull(request, "request cannot be null!");
        checkNotNull(request.requestContext, "requestContext cannot be null!");

        RegionLevelProgress globalLevelProgress = resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, GLOBAL_PROGRESS_KEY);

        checkNotNull(globalLevelProgress, "globalLevelProgress cannot be null!");

        ISessionToken globalSessionToken = globalLevelProgress.sessionToken;

        checkNotNull(globalSessionToken, "The session token corresponding to global progress cannot be null!");

        // if region level scoping is not allowed, then resolve to the global session token
        // region level scoping is allowed in the following cases:
        //      1. when the request is targeted to a specific logical partition
        //      2. when multiple write locations are configured
        if (!canUseRegionScopedSessionTokens) {
            return globalSessionToken;
        }

        if (globalLevelProgress.hasPartitionSeenNonPointDocumentOperations.get()) {
            request.requestContext.getSessionTokenEvaluationResults().add("Resolving to the global session token since partition has seen non-point requests.");
            return globalSessionToken;
        }

        RegionLevelProgress baseLevelProgress = resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, firstEffectivePreferredReadableRegion);

        if (baseLevelProgress == null || baseLevelProgress.sessionToken == null) {
            request.requestContext.getSessionTokenEvaluationResults().add("Resolving to the global session token since session token corresponding to first preferred readable region doesn't exist.");
            return globalSessionToken;
        }

        ISessionToken baseSessionToken = baseLevelProgress.sessionToken;

        // the partition key of the request has not been requested from lesser preferred regions
        // hence just return the base session token or in other words the session recorded for the first preferred readable region
        if (lesserPreferredRegionsPkProbablyRequestedFrom.isEmpty()) {
            request.requestContext.getSessionTokenEvaluationResults().add("Resolving to the session token corresponding to the first preferred readable region since the requested logical partition has not been resolved to other regions.");
            return baseSessionToken;
        }

        long globalLsn = -1;

        Utils.ValueHolder<UnmodifiableMap<Integer, Long>> localLsnByRegion = new Utils.ValueHolder<>();

        // return global merged progress of the session token for a given physical partition since regionId to localLsn mappings cannot be resolved
        if (!SessionTokenHelper.tryEvaluateLocalLsnByRegionMappingWithNullSafety(globalSessionToken, localLsnByRegion)) {
            request.requestContext.getSessionTokenEvaluationResults().add("Resolving to the global session token either because session token doesn't have region id to localLsn mappings or is not a vector session token.");
            return globalSessionToken;
        }

        Utils.ValueHolder<Long> sessionTokenVersion = new Utils.ValueHolder<>();

        // return global merged progress of the session token for a given physical partition since version cannot be resolved
        if (!SessionTokenHelper.tryEvaluateVersion(globalSessionToken, sessionTokenVersion)) {
            request.requestContext.getSessionTokenEvaluationResults().add("Resolving to the global session token recorded prior because the version cannot be recorded from the global session token.");

            return globalSessionToken;
        }

        StringBuilder sbPartOne = new StringBuilder();
        StringBuilder sbPartTwo = new StringBuilder();

        for (Map.Entry<Integer, Long> localLsnByRegionEntry : localLsnByRegion.v.entrySet()) {

            int regionId = localLsnByRegionEntry.getKey();
            String normalizedRegionName = RegionNameToRegionIdMap.getRegionName(regionId);

            // the regionId to normalizedRegionName does not exist
            if (normalizedRegionName.equals(StringUtils.EMPTY)) {

                if (logger.isDebugEnabled()) {
                    logger.debug("regionId with value - {} which has no known region name has been seen in the vector session token", regionId);
                }

                request.requestContext.getSessionTokenEvaluationResults().add("Resolving to the global session token since session token provided doesn't have a known region name mapping for region id : " + regionId + ".");
                return globalSessionToken;
            }

            // start constructing the session token using the localLsn and globalLsn from lesser preferred regions
            if (lesserPreferredRegionsPkProbablyRequestedFrom.contains(normalizedRegionName)) {
                request.requestContext.getSessionTokenEvaluationResults().add("Resolving region specific progress from " + normalizedRegionName);
                RegionLevelProgress satelliteRegionProgress = this.resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, normalizedRegionName);
                globalLsn = Math.max(globalLsn, satelliteRegionProgress.maxGlobalLsnSeen);
                sbPartTwo.append(VectorSessionToken.SegmentSeparator);
                sbPartTwo.append(regionId);
                sbPartTwo.append(VectorSessionToken.RegionProgressSeparator);
                sbPartTwo.append(satelliteRegionProgress.maxLocalLsnSeen);
            } else {
                request.requestContext.getSessionTokenEvaluationResults().add("No region specific progress to resolve from " + normalizedRegionName);
                sbPartTwo.append(VectorSessionToken.SegmentSeparator);
                sbPartTwo.append(regionId);
                sbPartTwo.append(VectorSessionToken.RegionProgressSeparator);
                sbPartTwo.append(-1);
            }
        }

        sbPartOne.append(sessionTokenVersion.v);
        sbPartOne.append(VectorSessionToken.SegmentSeparator);
        sbPartOne.append(globalLsn);

        Utils.ValueHolder<ISessionToken> resolvedSessionToken = new Utils.ValueHolder<>(null);

        // one additional step of merging base session token / first preferred read region and resolved session token
        if (SessionTokenHelper.tryParse(sbPartOne.append(sbPartTwo).toString(), resolvedSessionToken)) {
            return baseSessionToken.merge(resolvedSessionToken.v);
        }

        request.requestContext.getSessionTokenEvaluationResults().add("Resolving to the global session token since session token from the first preferred region couldn't be merged with region-resolved session token : " + resolvedSessionToken.v.convertToString() + ".");
        return globalSessionToken;
    }

    public boolean isPartitionKeyRangeIdPresent(String partitionKeyRangeId) {
        return this.partitionKeyRangeIdToRegionLevelProgress.containsKey(partitionKeyRangeId);
    }

    private RegionLevelProgress resolvePartitionKeyRangeIdBasedProgress(String partitionKeyRangeId, String progressScope) {

        ConcurrentHashMap<String, RegionLevelProgress> regionToRegionLevelProgress
            = this.partitionKeyRangeIdToRegionLevelProgress.get(partitionKeyRangeId);

        checkNotNull(regionToRegionLevelProgress, "regionToRegionLevelProgress cannot be null!");

        return regionToRegionLevelProgress.get(progressScope);
    }

    private boolean isRequestScopedToLogicalPartition(RxDocumentServiceRequest request) {
        return request.getPartitionKeyInternal() != null;
    }

    public boolean getHasPartitionSeenNonPointRequestsForDocuments(String partitionKeyRangeId) {

        if (this.partitionKeyRangeIdToRegionLevelProgress.containsKey(partitionKeyRangeId)) {
            ConcurrentHashMap<String, RegionLevelProgress> regionToRegionLevelProgress = this.partitionKeyRangeIdToRegionLevelProgress.get(partitionKeyRangeId);
            RegionLevelProgress regionLevelProgress = regionToRegionLevelProgress.get(GLOBAL_PROGRESS_KEY);

            if (regionLevelProgress == null) {
                return false;
            } else {
                return regionLevelProgress.hasPartitionSeenNonPointDocumentOperations.get();
            }
        }

        return false;
    }

    static class RegionLevelProgress {
        private final long maxGlobalLsnSeen;
        private final long maxLocalLsnSeen;
        private final ISessionToken sessionToken;
        private final AtomicBoolean hasPartitionSeenNonPointDocumentOperations;

        public RegionLevelProgress(long maxGlobalLsnSeen, long maxLocalLsnSeen, ISessionToken sessionToken, AtomicBoolean hasPartitionSeenNonPointDocumentOperations) {
            this.maxGlobalLsnSeen = maxGlobalLsnSeen;
            this.maxLocalLsnSeen = maxLocalLsnSeen;
            this.sessionToken = sessionToken;
            this.hasPartitionSeenNonPointDocumentOperations = hasPartitionSeenNonPointDocumentOperations;
        }

        public long getMaxGlobalLsnSeen() {
            return maxGlobalLsnSeen;
        }

        public long getMaxLocalLsnSeen() {
            return maxLocalLsnSeen;
        }

        public ISessionToken getSessionToken() {
            return sessionToken;
        }

        public void setHasPartitionSeenNonPointDocumentOperations(boolean hasPartitionSeenNonPointDocumentOperations) {
            this.hasPartitionSeenNonPointDocumentOperations.set(hasPartitionSeenNonPointDocumentOperations);
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
