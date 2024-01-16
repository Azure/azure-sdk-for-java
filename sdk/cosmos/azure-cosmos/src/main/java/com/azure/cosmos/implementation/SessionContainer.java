// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.math.util.Pair;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.azure.cosmos.implementation.Utils.ValueHolder;

/**
 * Used internally to cache the collections' session tokens in the Azure Cosmos DB database service.
 */
public final class SessionContainer implements ISessionContainer {

    private static final
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();
    private final Logger logger = LoggerFactory.getLogger(SessionContainer.class);

    private final ConcurrentHashMap<Long, PartitionKeyRangeBasedRegionScopedSessionTokenRegistry> collectionResourceIdToRegionScopedSessionTokens = new ConcurrentHashMap<>();

    private final PartitionKeyBasedBloomFilter partitionKeyBasedBloomFilter;

    /**
     * Collection ResourceID cache that maps collection name to collection ResourceID
     * When collection name is provided instead of self-link, this is used in combination with
     * collectionResourceIdToSessionTokens to retrieve the session token for the collection by name
     */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    private final ConcurrentHashMap<String, Long> collectionNameToCollectionResourceId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> collectionResourceIdToCollectionName = new ConcurrentHashMap<>();
    private final String hostName;
    private boolean disableSessionCapturing;
    private final GlobalEndpointManager globalEndpointManager;
    private final AtomicReference<String> firstPreferredWritableRegionCached;

    public SessionContainer(final String hostName, boolean disableSessionCapturing, GlobalEndpointManager globalEndpointManager) {
        this.hostName = hostName;
        this.disableSessionCapturing = disableSessionCapturing;
        this.globalEndpointManager = globalEndpointManager;
        this.firstPreferredWritableRegionCached = new AtomicReference<>(StringUtils.EMPTY);
        this.partitionKeyBasedBloomFilter = new PartitionKeyBasedBloomFilter();
    }

    public SessionContainer(final String hostName, boolean disableSessionCapturing) {
        this(hostName, disableSessionCapturing, null);
    }

    public SessionContainer(final String hostName) {
        this(hostName, false, null);
    }

    public String getHostName() {
        return this.hostName;
    }

    public void setDisableSessionCapturing(boolean value) {
        this.disableSessionCapturing = value;
    }

    public boolean getDisableSessionCapturing() {
        return this.disableSessionCapturing;
    }

    String getSessionToken(String collectionLink) {

        PathInfo pathInfo = new PathInfo(false, null, null, false);
        PartitionKeyRangeBasedRegionScopedSessionTokenRegistry partitionKeyRangeBasedRegionScopedSessionTokenRegistry = null;
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
                partitionKeyRangeBasedRegionScopedSessionTokenRegistry = this.collectionResourceIdToRegionScopedSessionTokens.get(uniqueDocumentCollectionId);
            }
        }

        if (partitionKeyRangeBasedRegionScopedSessionTokenRegistry == null) {
            return StringUtils.EMPTY;
        }

        return SessionContainer.getCombinedSessionToken(partitionKeyRangeBasedRegionScopedSessionTokenRegistry);
    }

    private PartitionKeyRangeBasedRegionScopedSessionTokenRegistry getPkRangeBasedRegionScopedSessionTokenRegistry(RxDocumentServiceRequest request) {
        return getPkRangeBasedRegionScopedSessionTokenRegistry(request.getIsNameBased(), request.getResourceId(), request.getResourceAddress());
    }

    private PartitionKeyRangeBasedRegionScopedSessionTokenRegistry getPkRangeBasedRegionScopedSessionTokenRegistry(boolean isNameBased, String rId, String resourceAddress) {
        PartitionKeyRangeBasedRegionScopedSessionTokenRegistry partitionKeyRangeBasedRegionScopedSessionTokenRegistry = null;
        if (!isNameBased) {
            if (!StringUtils.isEmpty(rId)) {
                ResourceId resourceId = ResourceId.parse(rId);
                if (resourceId.getDocumentCollection() != 0) {
                    partitionKeyRangeBasedRegionScopedSessionTokenRegistry =
                            this.collectionResourceIdToRegionScopedSessionTokens.get(resourceId.getUniqueDocumentCollectionId());
                }
            }
        } else {
            String collectionName = Utils.getCollectionName(resourceAddress);
            if (!StringUtils.isEmpty(collectionName) && this.collectionNameToCollectionResourceId.containsKey(collectionName)) {
                partitionKeyRangeBasedRegionScopedSessionTokenRegistry = this.collectionResourceIdToRegionScopedSessionTokens.get(
                        this.collectionNameToCollectionResourceId.get(collectionName));
            }
        }
        return partitionKeyRangeBasedRegionScopedSessionTokenRegistry;
    }

    private Pair<Long, PartitionKeyRangeBasedRegionScopedSessionTokenRegistry> getCollectionRidToRegionBasedSessionTokenRegistry(RxDocumentServiceRequest request) {
        return getCollectionRidToRegionBasedSessionTokenRegistry(request.getIsNameBased(), request.getResourceId(), request.getResourceAddress());
    }

    private Pair<Long, PartitionKeyRangeBasedRegionScopedSessionTokenRegistry> getCollectionRidToRegionBasedSessionTokenRegistry(boolean isNameBased, String rId, String resourceAddress) {
        PartitionKeyRangeBasedRegionScopedSessionTokenRegistry partitionKeyRangeBasedRegionScopedSessionTokenRegistry = null;
        Long collectionResourceId = null;

        if (!isNameBased) {
            if (!StringUtils.isEmpty(rId)) {
                ResourceId resourceId = ResourceId.parse(rId);
                if (resourceId.getDocumentCollection() != 0) {
                    collectionResourceId = resourceId.getUniqueDocumentCollectionId();
                    partitionKeyRangeBasedRegionScopedSessionTokenRegistry = this.collectionResourceIdToRegionScopedSessionTokens.get(collectionResourceId);
                }
            }
        } else {
            String collectionName = Utils.getCollectionName(resourceAddress);
            if (!StringUtils.isEmpty(collectionName) && this.collectionNameToCollectionResourceId.containsKey(collectionName)) {
                collectionResourceId = this.collectionNameToCollectionResourceId.get(collectionName);
                partitionKeyRangeBasedRegionScopedSessionTokenRegistry = this.collectionResourceIdToRegionScopedSessionTokens.get(collectionResourceId);
            }
        }

        if (partitionKeyRangeBasedRegionScopedSessionTokenRegistry != null && collectionResourceId != null) {
            return new Pair<>(collectionResourceId, partitionKeyRangeBasedRegionScopedSessionTokenRegistry);
        }

        return null;
    }

    public String resolveGlobalSessionToken(RxDocumentServiceRequest request) {
        PartitionKeyRangeBasedRegionScopedSessionTokenRegistry partitionKeyRangeBasedRegionScopedSessionTokenRegistry = this.getPkRangeBasedRegionScopedSessionTokenRegistry(request);
        if (partitionKeyRangeBasedRegionScopedSessionTokenRegistry != null) {
            return SessionContainer.getCombinedSessionToken(partitionKeyRangeBasedRegionScopedSessionTokenRegistry);
        }

        return StringUtils.EMPTY;
    }

    @Override
    public ISessionToken resolvePartitionLocalSessionToken(RxDocumentServiceRequest request,
                                                           String partitionKeyRangeId) {

        Pair<Long, PartitionKeyRangeBasedRegionScopedSessionTokenRegistry> collectionRidToRegionBasedSessionTokenRegistry =
            this.getCollectionRidToRegionBasedSessionTokenRegistry(request);

        if (collectionRidToRegionBasedSessionTokenRegistry == null) {
            return null;
        }

        Long collectionRid = collectionRidToRegionBasedSessionTokenRegistry.getKey();
        PartitionKeyRangeBasedRegionScopedSessionTokenRegistry partitionKeyRangeBasedRegionScopedSessionTokenRegistry
            = collectionRidToRegionBasedSessionTokenRegistry.getValue();

        ValueHolder<PartitionKeyInternal> partitionKeyInternal = ValueHolder.initialize(null);
        ValueHolder<PartitionKeyDefinition> partitionKeyDefinition = ValueHolder.initialize(null);

        if (this.firstPreferredWritableRegionCached.get().equals(StringUtils.EMPTY)) {
            this.firstPreferredWritableRegionCached.set(extractFirstEffectivePreferredWritableRegion(this.globalEndpointManager));
        }

        if (shouldUseBloomFilter(
            this.globalEndpointManager,
            request,
            partitionKeyInternal,
            partitionKeyDefinition)) {

            return SessionTokenHelper.resolvePartitionLocalSessionToken(request, this.partitionKeyBasedBloomFilter,
                partitionKeyRangeBasedRegionScopedSessionTokenRegistry, partitionKeyInternal.v, partitionKeyDefinition.v,
                collectionRid, partitionKeyRangeId, firstPreferredWritableRegionCached.get(), true);

        }

        return SessionTokenHelper.resolvePartitionLocalSessionToken(request, this.partitionKeyBasedBloomFilter,
            partitionKeyRangeBasedRegionScopedSessionTokenRegistry, partitionKeyInternal.v, partitionKeyDefinition.v,
            collectionRid, partitionKeyRangeId, firstPreferredWritableRegionCached.get(), false);
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
                    this.collectionResourceIdToRegionScopedSessionTokens.remove(rid);
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
                        this.collectionResourceIdToRegionScopedSessionTokens.remove(rid);
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
            ValueHolder<ResourceId> resourceId = ValueHolder.initialize(null);
            ValueHolder<String> collectionName = ValueHolder.initialize(null);

            if (shouldUpdateSessionToken(request, responseHeaders, resourceId, collectionName)) {
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

        // needed to extract pkRangeId information
        String tokenFromResponseHeaderMap = responseHeaders.get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        Map<String, String> sessionTokenToRegionMappings =
            diagnosticsAccessor.getSessionTokenToRegionMappings(request.requestContext.cosmosDiagnostics);

        if (sessionTokenToRegionMappings != null && !sessionTokenToRegionMappings.isEmpty()) {
            this.setSessionToken(request, resourceId, collectionName, tokenFromResponseHeaderMap);
        }
    }

    private void setSessionToken(
        RxDocumentServiceRequest request,
        ResourceId resourceId,
        String collectionName,
        String token) {

        String partitionKeyRangeId;
        String[] tokenParts = StringUtils.split(token, ':');

        assert tokenParts.length > 1;

        partitionKeyRangeId = tokenParts[0];

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
                this.addSessionToken(request, resourceId);
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
                addSessionToken(request, resourceId);
            } finally {
                this.writeLock.unlock();
            }
        }
    }

    private void recordPartitionKeyInBloomFilter(
        Long collectionRid,
        Map<String, String> sessionTokenToRegionMapping,
        PartitionKeyInternal partitionKeyInternal,
        PartitionKeyDefinition partitionKeyDefinition) {

        if (Strings.isNullOrEmpty(this.firstPreferredWritableRegionCached.get())) {
            this.firstPreferredWritableRegionCached.set(extractFirstEffectivePreferredWritableRegion(this.globalEndpointManager));
        }

        this.partitionKeyBasedBloomFilter.tryRecordPartitionKey(
            collectionRid,
            this.firstPreferredWritableRegionCached.get(),
            sessionTokenToRegionMapping,
            partitionKeyInternal,
            partitionKeyDefinition);
    }

    private void recordRegionScopedSessionToken(
        PartitionKeyRangeBasedRegionScopedSessionTokenRegistry partitionKeyRangeBasedRegionScopedSessionTokenRegistry,
        Map<String, String> sessionTokenToRegionMapping) {

        partitionKeyRangeBasedRegionScopedSessionTokenRegistry.tryRecordSessionToken(sessionTokenToRegionMapping);
    }

    private void addSessionToken(RxDocumentServiceRequest request, ResourceId resourceId) {

        final Long collectionResourceId = resourceId.getUniqueDocumentCollectionId();

        PartitionKeyRangeBasedRegionScopedSessionTokenRegistry partitionKeyRangeBasedRegionScopedSessionTokenRegistry =
            this.collectionResourceIdToRegionScopedSessionTokens.get(collectionResourceId);

        Map<String, String> sessionTokenToRegionMapping = null;
        ValueHolder<PartitionKeyInternal> partitionKeyInternal = ValueHolder.initialize(null);
        ValueHolder<PartitionKeyDefinition> partitionKeyDefinition = ValueHolder.initialize(null);

        if (request != null) {
            sessionTokenToRegionMapping = request.requestContext != null ?
                diagnosticsAccessor.getSessionTokenToRegionMappings(request.requestContext.cosmosDiagnostics) : new HashMap<>();
        }

        if (partitionKeyRangeBasedRegionScopedSessionTokenRegistry != null) {

            if (shouldUseBloomFilter(this.globalEndpointManager, request, partitionKeyInternal, partitionKeyDefinition)) {
                this.partitionKeyBasedBloomFilter.tryInitializeBloomFilter();
                this.recordPartitionKeyInBloomFilter(collectionResourceId, sessionTokenToRegionMapping,
                    partitionKeyInternal.v, partitionKeyDefinition.v);
            }

            this.recordRegionScopedSessionToken(
                partitionKeyRangeBasedRegionScopedSessionTokenRegistry,
                sessionTokenToRegionMapping
            );

        } else {
            // populate partitionKeyRangeBasedRegionScopedSessionTokenRegistry
            this.collectionResourceIdToRegionScopedSessionTokens.compute(
                resourceId.getUniqueDocumentCollectionId(), (k, pkRangeBasedRegionScopedSessionTokenRegistryAsVal) -> {

                    if (pkRangeBasedRegionScopedSessionTokenRegistryAsVal == null) {
                        logger.info("Registering a new collection resourceId [{}] in "
                            + "RegionScopedSessionTokenRegistry", resourceId);
                        pkRangeBasedRegionScopedSessionTokenRegistryAsVal =
                            new PartitionKeyRangeBasedRegionScopedSessionTokenRegistry();
                    }

                    return pkRangeBasedRegionScopedSessionTokenRegistryAsVal;
                }
            );

            partitionKeyRangeBasedRegionScopedSessionTokenRegistry =
                this.collectionResourceIdToRegionScopedSessionTokens.get(resourceId.getUniqueDocumentCollectionId());

            if (partitionKeyRangeBasedRegionScopedSessionTokenRegistry != null) {
                this.recordRegionScopedSessionToken(
                    partitionKeyRangeBasedRegionScopedSessionTokenRegistry,
                    sessionTokenToRegionMapping
                );
            }

            if (shouldUseBloomFilter(this.globalEndpointManager, request, partitionKeyInternal, partitionKeyDefinition)) {
                this.partitionKeyBasedBloomFilter.tryInitializeBloomFilter();
                this.recordPartitionKeyInBloomFilter(collectionResourceId, sessionTokenToRegionMapping,
                    partitionKeyInternal.v, partitionKeyDefinition.v);
            }
        }
    }

    private static String getCombinedSessionToken(PartitionKeyRangeBasedRegionScopedSessionTokenRegistry partitionKeyRangeBasedRegionScopedSessionTokenRegistry) {
        StringBuilder result = new StringBuilder();
        ConcurrentHashMap<String, ConcurrentHashMap<String, ISessionToken>> pkRangeIdToSessionTokenMapping
            = partitionKeyRangeBasedRegionScopedSessionTokenRegistry.getPkRangeIdToRegionScopedSessionTokens();

        if (pkRangeIdToSessionTokenMapping != null) {
            for (Iterator<Entry<String, ConcurrentHashMap<String, ISessionToken>>> iterator = pkRangeIdToSessionTokenMapping.entrySet().iterator(); iterator.hasNext(); ) {
                Entry<String, ConcurrentHashMap<String, ISessionToken>> entry = iterator.next();

                String partitionKeyRangeId = entry.getKey();
                ConcurrentHashMap<String, ISessionToken> regionToSessionTokenMappings = entry.getValue();

                ISessionToken mergedSessionTokenForPkRangeId = null;

                for (Iterator<Entry<String, ISessionToken>> regionToSessionTokenIterator = regionToSessionTokenMappings.entrySet().iterator(); regionToSessionTokenIterator.hasNext(); ) {
                    Entry<String, ISessionToken> regionToSessionTokenEntry = regionToSessionTokenIterator.next();
                    ISessionToken sessionTokenEntry = regionToSessionTokenEntry.getValue();

                    if (mergedSessionTokenForPkRangeId == null) {
                        mergedSessionTokenForPkRangeId = sessionTokenEntry;
                    } else {
                        mergedSessionTokenForPkRangeId.merge(sessionTokenEntry);
                    }
                }

                if (mergedSessionTokenForPkRangeId != null) {

                    result.append(partitionKeyRangeId).append(":").append(mergedSessionTokenForPkRangeId.convertToString());

                    if (iterator.hasNext()) {
                        result.append(",");
                    }
                }
            }
        }

        return result.toString();
    }

    private static boolean shouldUpdateSessionToken(
            RxDocumentServiceRequest request,
            Map<String, String> responseHeaders,
            ValueHolder<ResourceId> resourceId,
            ValueHolder<String> collectionName) {
        resourceId.v = null;
        String ownerFullName = responseHeaders.get(HttpConstants.HttpHeaders.OWNER_FULL_NAME);
        if (Strings.isNullOrEmpty(ownerFullName)) ownerFullName = request.getResourceAddress();

        collectionName.v = PathsHelper.getCollectionPath(ownerFullName);
        String resourceIdString;

        if (!request.getIsNameBased()) {
            resourceIdString = request.getResourceId();
        } else {
            resourceIdString = responseHeaders.get(HttpConstants.HttpHeaders.OWNER_ID);
            if (Strings.isNullOrEmpty(resourceIdString)) resourceIdString = request.getResourceId();
        }

        if (!Strings.isNullOrEmpty(resourceIdString)) {
            resourceId.v = ResourceId.parse(resourceIdString);

            if (resourceId.v.getDocumentCollection() != 0
                && !ReplicatedResourceClientUtils.isReadingFromMaster(request.getResourceType(), request.getOperationType())) {
                return true;
            }
        }

        return false;
    }

    private static boolean shouldUseBloomFilter(
        GlobalEndpointManager globalEndpointManager,
        RxDocumentServiceRequest request,
        ValueHolder<PartitionKeyInternal> partitionKeyInternal,
        ValueHolder<PartitionKeyDefinition> partitionKeyDefinition) {

        if (request == null || globalEndpointManager == null) {
            return false;
        }

        partitionKeyInternal.v = request.getPartitionKeyInternal();

        if (partitionKeyInternal.v == null) {
            return false;
        }

        partitionKeyDefinition.v = request.getPartitionKeyDefinition();

        if (partitionKeyDefinition.v == null) {
            return false;
        }

        return globalEndpointManager.canUseMultipleWriteLocations(request);
    }

    // TODO (abhmohanty): cache if possible
    private static String extractFirstEffectivePreferredWritableRegion(GlobalEndpointManager globalEndpointManager) {

        if (globalEndpointManager == null) {
            return StringUtils.EMPTY;
        }

        ConnectionPolicy connectionPolicy = globalEndpointManager.getConnectionPolicy();

        if (connectionPolicy != null) {
            List<String> preferredRegions = connectionPolicy.getPreferredRegions();

            if (preferredRegions != null && !preferredRegions.isEmpty()) {
                return preferredRegions.get(0).toLowerCase(Locale.ROOT).trim().replace(" ", "");
            }
        }

        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        if (databaseAccount != null) {
            Iterator<DatabaseAccountLocation> databaseAccountLocationIterator = databaseAccount.getWritableLocations().iterator();

            if (databaseAccountLocationIterator.hasNext()) {
                DatabaseAccountLocation databaseAccountLocation = databaseAccountLocationIterator.next();
                return databaseAccountLocation.getName().toLowerCase(Locale.ROOT).trim().replace(" ", "");
            }
        }
        return StringUtils.EMPTY;
    }
}
