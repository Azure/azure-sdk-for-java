// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.NotImplementedException;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.math.util.Pair;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RegionScopedSessionContainer implements ISessionContainer {

    private final Logger logger = LoggerFactory.getLogger(RegionScopedSessionContainer.class);

    // collectionResourceIdToPartitionScopedRegionLevelProgress is a mapping between the collectionRid and partition-specific region level progress
    // what is partition-specific region level progress?
    //  - when a response is received from a replica, it returns what is known as a session token
    //  - a session token is like a progress bookmark for a given replica of a physical partition
    //  - a session token in the vector session token format has the following
    //        - globalLsn: denotes the progress bookmark of a replica in the hub region
    //        - localLsn: denotes the progress bookmark of a replica in a satellite region
    //  - partition scoped region level progress introduces a new level of mapping - the mapping between the region and region specific progress
    //  - such a mapping exists for each physical partition for the collection
    //  - region specific progress has the following:
    //        - max(globalLsn) seen for that region - in other words the max progress of a replica from the hub region seen in the region mapping to region specific progress
    //        - max(localLsn) seen for that region
    //        - merged session token if necessary - session token in its parsed form is only merged again if the region mapping to region specific progress
    //          is also a first preferred readable region [or] the overall max progress of all regions for that partition is to be recorded
    //  - partition scoped region level progress has a structure which can be summarized as below:
    //      - for a given collection rid:
    //          - {"global" -> (-1, -1, <merged session token representing the overall max progress of all regions for that partition is to be recorded>)}
    //          - {"regionX" -> (max(localLsn), max(globalLsn), <merged session token if also first preferred readable region [or] session token as is if otherwise>}
    //          - {"regionY" -> (max(localLsn), max(globalLsn), <merged session token if also first preferred readable region [or] session token as is if otherwise>}...
    //  - why is the "global" mapping needed?
    //        - not all operations can be scoped to a single-logical partition such as cross-partitioned queries or query which have non-point range scope
    //        - for such operations using the merged session token seen across all replicas across all regions is necessary since the bloom filter won't help here
    //          and the progress to follow has to at least be at physical partition level
    private final ConcurrentHashMap<Long, PartitionScopedRegionLevelProgress> collectionResourceIdToPartitionScopedRegionLevelProgress = new ConcurrentHashMap<>();

    // - partitionKeyBasedBloomFilter encapsulates a Guava-based bloom filter which stores an entry which corresponds to a triple of [collectionRid, EPK hash, region]
    private final PartitionKeyBasedBloomFilter partitionKeyBasedBloomFilter;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();

    // 1. a write lock is acquired when the collection has not been cached by the session container yet
    // 2. the write lock is only released when the collection has been recorded along with any session specific
    // information for a partition in that collection
    // 3. once a collection has been recorded, thread-safe access is delegated to individual concurrent hashmaps
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    private final ConcurrentHashMap<String, Long> collectionNameToCollectionResourceId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> collectionResourceIdToCollectionName = new ConcurrentHashMap<>();
    private final String hostName;
    private boolean disableSessionCapturing;
    private final GlobalEndpointManager globalEndpointManager;
    private final AtomicReference<String> firstPreferredReadableRegionCached;
    private final String regionScopedSessionCapturingOptionsAsString;

    public RegionScopedSessionContainer(final String hostName, boolean disableSessionCapturing, GlobalEndpointManager globalEndpointManager) {
        this.hostName = hostName;
        this.disableSessionCapturing = disableSessionCapturing;
        this.globalEndpointManager = globalEndpointManager;
        this.firstPreferredReadableRegionCached = new AtomicReference<>(StringUtils.EMPTY);
        this.partitionKeyBasedBloomFilter = new PartitionKeyBasedBloomFilter();
        this.regionScopedSessionCapturingOptionsAsString = stringifyConfig();
    }

    public RegionScopedSessionContainer(final String hostName, boolean disableSessionCapturing) {
        this(hostName, disableSessionCapturing, null);
    }

    public RegionScopedSessionContainer(final String hostName) {
        this(hostName, false, null);
    }

    public String getHostName() {
        return this.hostName;
    }

    @Override
    public void setDisableSessionCapturing(boolean value) {
        this.disableSessionCapturing = value;
    }

    @Override
    public boolean getDisableSessionCapturing() {
        return this.disableSessionCapturing;
    }

    @Override
    public String getSessionToken(String collectionLink) {

        PathInfo pathInfo = new PathInfo(false, null, null, false);
        PartitionScopedRegionLevelProgress partitionScopedRegionLevelProgress = null;

        if (PathsHelper.tryParsePathSegments(collectionLink, pathInfo, null)) {
            Long uniqueDocumentCollectionId = null;
            if (pathInfo.isNameBased) {
                String collectionName = PathsHelper.getCollectionPath(pathInfo.resourceIdOrFullName);
                uniqueDocumentCollectionId = this.collectionNameToCollectionResourceId.get(collectionName);
            } else {
                ResourceId resourceId = ResourceId.parse(pathInfo.resourceIdOrFullName);
                if (resourceId.getDocumentCollection() != 0) {
                    uniqueDocumentCollectionId = resourceId.getUniqueDocumentCollectionId();
                }
            }

            if (uniqueDocumentCollectionId != null) {
                partitionScopedRegionLevelProgress = this.collectionResourceIdToPartitionScopedRegionLevelProgress.get(uniqueDocumentCollectionId);
            }
        }

        if (partitionScopedRegionLevelProgress == null) {
            return StringUtils.EMPTY;
        }

        return this.getCombinedSessionToken(partitionScopedRegionLevelProgress);
    }

    private Pair<Long, PartitionScopedRegionLevelProgress> getCollectionRidToPartitionScopedRegionLevelProgress(RxDocumentServiceRequest request) {
        return getCollectionRidToPartitionScopedRegionLevelProgress(request.getIsNameBased(), request.getResourceId(), request.getResourceAddress());
    }

    private Pair<Long, PartitionScopedRegionLevelProgress> getCollectionRidToPartitionScopedRegionLevelProgress(boolean isNameBased, String rId, String resourceAddress) {
        PartitionScopedRegionLevelProgress partitionScopedRegionLevelProgress = null;
        Long collectionResourceId = null;

        if (!isNameBased) {
            if (!StringUtils.isEmpty(rId)) {
                ResourceId resourceId = ResourceId.parse(rId);
                if (resourceId.getDocumentCollection() != 0) {
                    collectionResourceId = resourceId.getUniqueDocumentCollectionId();
                    partitionScopedRegionLevelProgress = this.collectionResourceIdToPartitionScopedRegionLevelProgress.get(collectionResourceId);
                }
            }
        } else {
            String collectionName = Utils.getCollectionName(resourceAddress);
            if (!StringUtils.isEmpty(collectionName) && this.collectionNameToCollectionResourceId.containsKey(collectionName)) {
                collectionResourceId = this.collectionNameToCollectionResourceId.get(collectionName);
                partitionScopedRegionLevelProgress = this.collectionResourceIdToPartitionScopedRegionLevelProgress.get(collectionResourceId);
            }
        }

        if (partitionScopedRegionLevelProgress != null && collectionResourceId != null) {
            return new Pair<>(collectionResourceId, partitionScopedRegionLevelProgress);
        }

        return null;
    }

    public String resolveGlobalSessionToken(RxDocumentServiceRequest request) {
        Pair<Long, PartitionScopedRegionLevelProgress> collectionRidToPartitionScopedRegionLevelProgress = this.getCollectionRidToPartitionScopedRegionLevelProgress(request);

        if (collectionRidToPartitionScopedRegionLevelProgress == null) {
            return StringUtils.EMPTY;
        }

        checkNotNull(collectionRidToPartitionScopedRegionLevelProgress.getKey(), "collectionRid cannot be null!");
        checkNotNull(collectionRidToPartitionScopedRegionLevelProgress.getValue(), "partitionScopedRegionLevelProgress cannot be null!");

        return this.getCombinedSessionToken(collectionRidToPartitionScopedRegionLevelProgress.getValue());
    }

    @Override
    public ISessionToken resolvePartitionLocalSessionToken(RxDocumentServiceRequest request,
                                                           String partitionKeyRangeId) {

        Pair<Long, PartitionScopedRegionLevelProgress> collectionRidToPartitionScopedRegionLevelProgress =
            this.getCollectionRidToPartitionScopedRegionLevelProgress(request);

        if (collectionRidToPartitionScopedRegionLevelProgress == null) {
            return null;
        }

        Long collectionRid = collectionRidToPartitionScopedRegionLevelProgress.getKey();
        PartitionScopedRegionLevelProgress partitionScopedRegionLevelProgress
            = collectionRidToPartitionScopedRegionLevelProgress.getValue();

        Utils.ValueHolder<PartitionKeyInternal> partitionKeyInternal = Utils.ValueHolder.initialize(null);
        Utils.ValueHolder<PartitionKeyDefinition> partitionKeyDefinition = Utils.ValueHolder.initialize(null);

        if (this.firstPreferredReadableRegionCached.get().equals(StringUtils.EMPTY)) {
            this.firstPreferredReadableRegionCached.set(extractFirstEffectivePreferredReadableRegion());
        }

        boolean shouldUseBloomFilter = shouldUseBloomFilter(
            request,
            partitionKeyRangeId,
            partitionKeyInternal,
            partitionKeyDefinition,
            partitionScopedRegionLevelProgress);

        return SessionTokenHelper.resolvePartitionLocalSessionToken(
            request,
            this.partitionKeyBasedBloomFilter,
            partitionScopedRegionLevelProgress,
            partitionKeyInternal.v,
            partitionKeyDefinition.v,
            collectionRid,
            partitionKeyRangeId,
            this.firstPreferredReadableRegionCached.get(),
            shouldUseBloomFilter);
    }

    @Override
    public void clearTokenByCollectionFullName(String collectionFullName) {
        if (!Strings.isNullOrEmpty(collectionFullName)) {
            String collectionName = PathsHelper.getCollectionPath(collectionFullName);
            this.writeLock.lock();
            try {
                if (this.collectionNameToCollectionResourceId.containsKey(collectionName)) {
                    Long rid = this.collectionNameToCollectionResourceId.get(collectionName);
                    this.collectionResourceIdToCollectionName.remove(rid);
                    this.collectionNameToCollectionResourceId.remove(collectionName);
                    this.collectionResourceIdToPartitionScopedRegionLevelProgress.remove(rid);
                }
            } finally {
                this.writeLock.unlock();
            }
        }
    }

    @Override
    public void clearTokenByResourceId(String resourceId) {
        if (!StringUtils.isEmpty(resourceId)) {
            ResourceId resource = ResourceId.parse(resourceId);
            if (resource.getDocumentCollection() != 0) {
                Long rid = resource.getUniqueDocumentCollectionId();
                this.writeLock.lock();
                try {
                    if (this.collectionResourceIdToCollectionName.containsKey(rid)) {
                        String collectionName = this.collectionResourceIdToCollectionName.get(rid);
                        this.collectionResourceIdToCollectionName.remove(rid);
                        this.collectionNameToCollectionResourceId.remove(collectionName);
                        this.collectionResourceIdToPartitionScopedRegionLevelProgress.remove(rid);
                    }
                } finally {
                    this.writeLock.unlock();
                }
            }
        }
    }

    @Override
    public void setSessionToken(RxDocumentServiceRequest request, Map<String, String> responseHeaders) {
        if (this.disableSessionCapturing) {
            return;
        }

        String token = responseHeaders.get(HttpConstants.HttpHeaders.SESSION_TOKEN);

        if (!Strings.isNullOrEmpty(token)) {
            Utils.ValueHolder<ResourceId> resourceId = Utils.ValueHolder.initialize(null);
            Utils.ValueHolder<String> collectionName = Utils.ValueHolder.initialize(null);

            if (SessionContainerUtil.shouldUpdateSessionToken(request, responseHeaders, resourceId, collectionName)) {
                this.setSessionToken(request, resourceId.v, collectionName.v, token);
            }
        }
    }

    @Override
    public void setSessionToken(RxDocumentServiceRequest request, String collectionRid, String collectionFullName, Map<String, String> responseHeaders) {
        if (this.disableSessionCapturing) {
            return;
        }

        ResourceId resourceId = ResourceId.parse(collectionRid);
        String collectionName = PathsHelper.getCollectionPath(collectionFullName);
        String token = responseHeaders.get(HttpConstants.HttpHeaders.SESSION_TOKEN);

        if (!Strings.isNullOrEmpty(token)) {
            this.setSessionToken(request, resourceId, collectionName, token);
        }
    }

    @Override
    public void setSessionToken(String collectionRid, String collectionFullName, Map<String, String> responseHeaders) {
        throw new NotImplementedException("setSessionToken(String collectionRid, String collectionFullName, Map<String, String> responseHeaders) not implemented for RegionScopedSessionContainer");
    }

    private void setSessionToken(RxDocumentServiceRequest request, ResourceId resourceId, String collectionName, String token) {
        String partitionKeyRangeId;
        ISessionToken parsedSessionToken;

        String[] tokenParts = StringUtils.split(token, ':');
        partitionKeyRangeId = tokenParts[0];
        parsedSessionToken = SessionTokenHelper.parse(tokenParts[1]);

        if (logger.isTraceEnabled()) {
            logger.trace("UPDATE SESSION token {} {} {}", resourceId.getUniqueDocumentCollectionId(), collectionName, tokenParts[1]);
        }

        boolean isKnownCollection;

        this.readLock.lock();
        try {
            isKnownCollection = collectionName != null &&
                this.collectionNameToCollectionResourceId.containsKey(collectionName) &&
                this.collectionResourceIdToCollectionName.containsKey(resourceId.getUniqueDocumentCollectionId()) &&
                this.collectionNameToCollectionResourceId.get(collectionName) == resourceId.getUniqueDocumentCollectionId() &&
                this.collectionResourceIdToCollectionName.get(resourceId.getUniqueDocumentCollectionId()).equals(collectionName);
            if (isKnownCollection) {
                this.addSessionTokenAndTryRecordEpkInBloomFilter(request, resourceId, partitionKeyRangeId, parsedSessionToken);
            }
        } finally {
            this.readLock.unlock();
        }

        if (!isKnownCollection) {
            this.writeLock.lock();
            try {
                if (resourceId.getUniqueDocumentCollectionId() != 0) {
                    this.collectionNameToCollectionResourceId.compute(collectionName, (k, v) -> resourceId.getUniqueDocumentCollectionId());
                    this.collectionResourceIdToCollectionName.compute(resourceId.getUniqueDocumentCollectionId(), (k, v) -> collectionName);
                }
                this.addSessionTokenAndTryRecordEpkInBloomFilter(request, resourceId, partitionKeyRangeId, parsedSessionToken);
            } finally {
                this.writeLock.unlock();
            }
        }
    }

    private void recordPartitionKeyInBloomFilter(
        RxDocumentServiceRequest request,
        Long collectionRid,
        String regionRoutedTo,
        PartitionKeyInternal partitionKeyInternal,
        PartitionKeyDefinition partitionKeyDefinition) {

        if (Strings.isNullOrEmpty(this.firstPreferredReadableRegionCached.get())) {
            this.firstPreferredReadableRegionCached.set(extractFirstEffectivePreferredReadableRegion());
        }

        this.partitionKeyBasedBloomFilter.tryRecordPartitionKey(
            request,
            collectionRid,
            this.firstPreferredReadableRegionCached.get(),
            regionRoutedTo,
            partitionKeyInternal,
            partitionKeyDefinition);
    }

    private void recordRegionScopedSessionToken(
        RxDocumentServiceRequest request,
        PartitionScopedRegionLevelProgress partitionScopedRegionLevelProgress,
        ISessionToken parsedSessionToken,
        String partitionKeyRangeId,
        String regionRoutedTo) {

        partitionScopedRegionLevelProgress.tryRecordSessionToken(
            request,
            parsedSessionToken,
            partitionKeyRangeId,
            this.firstPreferredReadableRegionCached.get(),
            regionRoutedTo);
    }

    private void addSessionTokenAndTryRecordEpkInBloomFilter(RxDocumentServiceRequest request, ResourceId resourceId, String partitionKeyRangeId, ISessionToken parsedSessionToken) {

        final Long collectionResourceId = resourceId.getUniqueDocumentCollectionId();

        PartitionScopedRegionLevelProgress partitionScopedRegionLevelProgress
            = this.collectionResourceIdToPartitionScopedRegionLevelProgress.get(collectionResourceId);

        if (this.firstPreferredReadableRegionCached.get().equals(StringUtils.EMPTY)) {
            this.firstPreferredReadableRegionCached.set(extractFirstEffectivePreferredReadableRegion());
        }

        String regionRoutedTo = null;

        if (request.requestContext != null) {
            URI regionEndpointRoutedTo = request.requestContext.locationEndpointToRoute;
            regionRoutedTo = this.globalEndpointManager.getRegionName(regionEndpointRoutedTo, request.getOperationType());
        }

        Utils.ValueHolder<PartitionKeyInternal> partitionKeyInternal = Utils.ValueHolder.initialize(null);
        Utils.ValueHolder<PartitionKeyDefinition> partitionKeyDefinition = Utils.ValueHolder.initialize(null);

        if (partitionScopedRegionLevelProgress != null) {

            if (shouldUseBloomFilter(
                request,
                partitionKeyRangeId,
                partitionKeyInternal,
                partitionKeyDefinition,
                partitionScopedRegionLevelProgress)) {

                this.recordPartitionKeyInBloomFilter(
                    request,
                    collectionResourceId,
                    regionRoutedTo,
                    partitionKeyInternal.v,
                    partitionKeyDefinition.v);
            }

            this.recordRegionScopedSessionToken(
                request,
                partitionScopedRegionLevelProgress,
                parsedSessionToken,
                partitionKeyRangeId,
                regionRoutedTo);

        } else {
            this.collectionResourceIdToPartitionScopedRegionLevelProgress.compute(
                resourceId.getUniqueDocumentCollectionId(), (k, partitionScopedRegionLevelProgressAsVal) -> {

                    if (partitionScopedRegionLevelProgressAsVal == null) {
                        logger.info("Registering a new collection resourceId [{}] in "
                            + "RegionScopedSessionContainer", resourceId);
                        partitionScopedRegionLevelProgressAsVal =
                            new PartitionScopedRegionLevelProgress();
                    }

                    return partitionScopedRegionLevelProgressAsVal;
                }
            );

            partitionScopedRegionLevelProgress =
                this.collectionResourceIdToPartitionScopedRegionLevelProgress.get(resourceId.getUniqueDocumentCollectionId());

            if (partitionScopedRegionLevelProgress != null) {
                this.recordRegionScopedSessionToken(
                    request,
                    partitionScopedRegionLevelProgress,
                    parsedSessionToken,
                    partitionKeyRangeId,
                    regionRoutedTo);
            }

            if (shouldUseBloomFilter(
                request,
                partitionKeyRangeId,
                partitionKeyInternal,
                partitionKeyDefinition,
                partitionScopedRegionLevelProgress)) {

                this.recordPartitionKeyInBloomFilter(
                    request,
                    collectionResourceId,
                    regionRoutedTo,
                    partitionKeyInternal.v,
                    partitionKeyDefinition.v);
            }
        }
    }

    private String getCombinedSessionToken(PartitionScopedRegionLevelProgress partitionScopedRegionLevelProgress) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, PartitionScopedRegionLevelProgress.RegionLevelProgress>> tokens
            = partitionScopedRegionLevelProgress.getPartitionKeyRangeIdToRegionLevelProgress();

        StringBuilder result = new StringBuilder();
        if (tokens != null) {
            for (Iterator<Map.Entry<String, ConcurrentHashMap<String, PartitionScopedRegionLevelProgress.RegionLevelProgress>>> iterator = tokens.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String,  ConcurrentHashMap<String, PartitionScopedRegionLevelProgress.RegionLevelProgress>> entry = iterator.next();

                String partitionKeyRangeId = entry.getKey();
                String sessionTokenAsString = entry.getValue().get(PartitionScopedRegionLevelProgress.GLOBAL_PROGRESS_KEY).getSessionToken().convertToString();

                result = result.append(partitionKeyRangeId).append(":").append(sessionTokenAsString);
                if (iterator.hasNext()) {
                    result = result.append(",");
                }
            }
        }

        return result.toString();
    }

    // validate whether the request can be scoped to a logical partition
    // along with whether multi-write is enabled for the request / account
    // multi-write setup needs to be verified since multiple regions can make progress
    // independently as multiple regions can process writes
    private boolean shouldUseBloomFilter(
        RxDocumentServiceRequest request,
        String partitionKeyRangeId,
        Utils.ValueHolder<PartitionKeyInternal> partitionKeyInternal,
        Utils.ValueHolder<PartitionKeyDefinition> partitionKeyDefinition,
        PartitionScopedRegionLevelProgress partitionScopedRegionLevelProgress) {

        checkNotNull(request, "request cannot be null!");
        checkNotNull(this.globalEndpointManager, "globalEndpointManager cannot be nulL!");
        checkNotNull(partitionScopedRegionLevelProgress, "partitionScopedRegionLevelProgress cannot be null!");

        partitionKeyInternal.v = request.getPartitionKeyInternal();

        if (partitionKeyInternal.v == null) {
            return false;
        }

        partitionKeyDefinition.v = request.getPartitionKeyDefinition();

        if (partitionKeyDefinition.v == null) {
            return false;
        }

        if (partitionScopedRegionLevelProgress.getHasPartitionSeenNonPointRequestsForDocuments(partitionKeyRangeId)) {
            return false;
        }

        return globalEndpointManager.canUseMultipleWriteLocations(request);
    }

    private String extractFirstEffectivePreferredReadableRegion() {

        checkNotNull(globalEndpointManager, "globalEndpointManager cannot be null!");

        List<String> regionNamesForRead = globalEndpointManager
            .getReadEndpoints()
            .stream()
            .map(endpoint -> globalEndpointManager.getRegionName(endpoint, OperationType.Read))
            .collect(Collectors.toList());

        checkNotNull(regionNamesForRead, "regionNamesForRead cannot be null!");

        if (!regionNamesForRead.isEmpty()) {
            return regionNamesForRead.get(0).toLowerCase(Locale.ROOT).trim().replace(" ", "");
        }

        throw new IllegalStateException("regionNamesForRead cannot be empty!");
    }

    public boolean isPartitionKeyResolvedToARegion(
        PartitionKeyInternal internalPartitionKey,
        PartitionKeyDefinition partitionKeyDefinition,
        String collectionId,
        String normalizedRegion) {

        String effectivePartitionKeyString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(internalPartitionKey, partitionKeyDefinition);
        Long collectionRid = this.collectionNameToCollectionResourceId.get(collectionId);

        checkNotNull(collectionRid, "collectionRid cannot be null!");

        return this.partitionKeyBasedBloomFilter.isPartitionKeyResolvedToARegion(
            effectivePartitionKeyString, normalizedRegion, collectionRid);
    }

    public String getRegionScopedSessionCapturingOptionsAsString() {
        return this.regionScopedSessionCapturingOptionsAsString;
    }

    private static String stringifyConfig() {
        return "(rssc: true, expins: " + Configs.getPkBasedBloomFilterExpectedInsertionCount() + ", ffprate: " + Configs.getPkBasedBloomFilterExpectedFfpRate() + ")";
    }
}
