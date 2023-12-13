// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.math.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
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

    /**
     * SESSION token cache that maps collection ResourceID to session tokens
     */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> collectionResourceIdToSessionTokens = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, PkRangeBasedRegionScopedSessionTokenRegistry> collectionResourceIdToRegionScopedSessionTokens = new ConcurrentHashMap<>();

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
    private final SessionConsistencyOptions sessionConsistencyOptions;
    private final AtomicReference<String> firstPreferredWritableRegionCached;

    public SessionContainer(final String hostName, boolean disableSessionCapturing, GlobalEndpointManager globalEndpointManager, SessionConsistencyOptions sessionConsistencyOptions) {
        this.hostName = hostName;
        this.disableSessionCapturing = disableSessionCapturing;
        this.globalEndpointManager = globalEndpointManager;
        this.sessionConsistencyOptions = sessionConsistencyOptions;
        this.firstPreferredWritableRegionCached = new AtomicReference<>(StringUtils.EMPTY);
        this.partitionKeyBasedBloomFilter = new PartitionKeyBasedBloomFilter();
    }

    public SessionContainer(final String hostName, boolean disableSessionCapturing) {
        this(hostName, disableSessionCapturing, null, SessionConsistencyOptions.getDefaultOptions());
    }

    public SessionContainer(final String hostName) {
        this(hostName, false, null, SessionConsistencyOptions.getDefaultOptions());
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
        ConcurrentHashMap<String, ISessionToken> partitionKeyRangeIdToTokenMap = null;
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
                partitionKeyRangeIdToTokenMap = this.collectionResourceIdToSessionTokens.get(uniqueDocumentCollectionId);
            }
        }

        if (partitionKeyRangeIdToTokenMap == null) {
            return StringUtils.EMPTY;
        }

        return SessionContainer.getCombinedSessionToken(partitionKeyRangeIdToTokenMap);
    }

    private ConcurrentHashMap<String, ISessionToken> getPartitionKeyRangeIdToTokenMap(RxDocumentServiceRequest request) {
        return getPartitionKeyRangeIdToTokenMap(request.getIsNameBased(), request.getResourceId(), request.getResourceAddress());
    }

    private ConcurrentHashMap<String, ISessionToken> getPartitionKeyRangeIdToTokenMap(boolean isNameBased, String rId, String resourceAddress) {
        ConcurrentHashMap<String, ISessionToken> rangeIdToTokenMap = null;
        if (!isNameBased) {
            if (!StringUtils.isEmpty(rId)) {
                ResourceId resourceId = ResourceId.parse(rId);
                if (resourceId.getDocumentCollection() != 0) {
                    rangeIdToTokenMap =
                            this.collectionResourceIdToSessionTokens.get(resourceId.getUniqueDocumentCollectionId());
                }
            }
        } else {
            String collectionName = Utils.getCollectionName(resourceAddress);
            if (!StringUtils.isEmpty(collectionName) && this.collectionNameToCollectionResourceId.containsKey(collectionName)) {
                rangeIdToTokenMap = this.collectionResourceIdToSessionTokens.get(
                        this.collectionNameToCollectionResourceId.get(collectionName));
            }
        }
        return rangeIdToTokenMap;
    }

    private Pair<Long, PkRangeBasedRegionScopedSessionTokenRegistry> getCollectionRidToRegionBasedSessionTokenRegistry(RxDocumentServiceRequest request) {
        return getCollectionRidToRegionBasedSessionTokenRegistry(request.getIsNameBased(), request.getResourceId(), request.getResourceAddress());
    }

    private Pair<Long, PkRangeBasedRegionScopedSessionTokenRegistry> getCollectionRidToRegionBasedSessionTokenRegistry(boolean isNameBased, String rId, String resourceAddress) {
        PkRangeBasedRegionScopedSessionTokenRegistry pkRangeBasedRegionScopedSessionTokenRegistry = null;
        Long collectionResourceId = null;

        if (!isNameBased) {
            if (!StringUtils.isEmpty(rId)) {
                ResourceId resourceId = ResourceId.parse(rId);
                if (resourceId.getDocumentCollection() != 0) {
                    collectionResourceId = resourceId.getUniqueDocumentCollectionId();
                    pkRangeBasedRegionScopedSessionTokenRegistry = this.collectionResourceIdToRegionScopedSessionTokens.get(collectionResourceId);
                }
            }
        } else {
            String collectionName = Utils.getCollectionName(resourceAddress);
            if (!StringUtils.isEmpty(collectionName) && this.collectionNameToCollectionResourceId.containsKey(collectionName)) {
                collectionResourceId = this.collectionNameToCollectionResourceId.get(collectionName);
                pkRangeBasedRegionScopedSessionTokenRegistry = this.collectionResourceIdToRegionScopedSessionTokens.get(collectionResourceId);
            }
        }

        if (pkRangeBasedRegionScopedSessionTokenRegistry != null && collectionResourceId != null) {
            return new Pair<>(collectionResourceId, pkRangeBasedRegionScopedSessionTokenRegistry);
        }

        return null;
    }

    public String resolveGlobalSessionToken(RxDocumentServiceRequest request) {
        ConcurrentHashMap<String, ISessionToken> partitionKeyRangeIdToTokenMap = this.getPartitionKeyRangeIdToTokenMap(request);
        if (partitionKeyRangeIdToTokenMap != null) {
            return SessionContainer.getCombinedSessionToken(partitionKeyRangeIdToTokenMap);
        }

        return StringUtils.EMPTY;
    }

    @Override
    public ISessionToken resolvePartitionLocalSessionToken(RxDocumentServiceRequest request, String partitionKeyRangeId) {

        ISessionToken resolvedPartitionKeyScopedSessionToken = null;

        if (shouldPartitionKeyBeRecorded(this.globalEndpointManager, request, this.sessionConsistencyOptions)) {
            String partitionKey = request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY);
            if (partitionKey != null) {

                Pair<Long, PkRangeBasedRegionScopedSessionTokenRegistry> collectionRidToRegionBasedSessionTokenRegistry =
                    this.getCollectionRidToRegionBasedSessionTokenRegistry(request);

                if (collectionRidToRegionBasedSessionTokenRegistry != null) {

                    Long collectionRid = collectionRidToRegionBasedSessionTokenRegistry.getKey();
                    PkRangeBasedRegionScopedSessionTokenRegistry pkRangeBasedRegionScopedSessionTokenRegistry = collectionRidToRegionBasedSessionTokenRegistry.getValue();

                    if (this.firstPreferredWritableRegionCached.get().equals(StringUtils.EMPTY)) {
                        this.firstPreferredWritableRegionCached.set(extractFirstEffectivePreferredWritableRegion(this.globalEndpointManager));
                    }

                    resolvedPartitionKeyScopedSessionToken = this.partitionKeyBasedBloomFilter.tryResolveSessionToken(
                        collectionRid, partitionKey, partitionKeyRangeId, this.firstPreferredWritableRegionCached.get(), pkRangeBasedRegionScopedSessionTokenRegistry);
                }

                // TODO: abhmohanty - revert logger or logger.debug here
                if (resolvedPartitionKeyScopedSessionToken != null) {
                    logger.info("Using partition key scoped session token - {}.", resolvedPartitionKeyScopedSessionToken.convertToString());
                } else {
                    logger.info("Partition key scoped session token is null");
                }
            }
        }

        ISessionToken resolvedPkRangeIdScopedSessionToken = SessionTokenHelper.resolvePartitionLocalSessionToken(request,
            partitionKeyRangeId,
            this.getPartitionKeyRangeIdToTokenMap(request));

        if (shouldPartitionKeyBeRecorded(this.globalEndpointManager, request, this.sessionConsistencyOptions)) {
            return (resolvedPartitionKeyScopedSessionToken != null) ? resolvedPartitionKeyScopedSessionToken : resolvedPkRangeIdScopedSessionToken;
        }

        return resolvedPkRangeIdScopedSessionToken;
    }

    @Override
    public void clearTokenByCollectionFullName(String collectionFullName) {
        if (!Strings.isNullOrEmpty(collectionFullName)) {
            String collectionName = PathsHelper.getCollectionPath(collectionFullName);
            this.writeLock.lock();
            try {
                if (this.collectionNameToCollectionResourceId.containsKey(collectionName)) {
                    Long rid = this.collectionNameToCollectionResourceId.get(collectionName);
                    this.collectionResourceIdToSessionTokens.remove(rid);
                    this.collectionResourceIdToCollectionName.remove(rid);
                    this.collectionNameToCollectionResourceId.remove(collectionName);

                    if (this.sessionConsistencyOptions.isPartitionKeyScopedSessionCapturingEnabled()) {
                        this.collectionResourceIdToRegionScopedSessionTokens.remove(rid);
                    }
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
                        this.collectionResourceIdToSessionTokens.remove(rid);
                        this.collectionResourceIdToCollectionName.remove(rid);
                        this.collectionNameToCollectionResourceId.remove(collectionName);

                        if (this.sessionConsistencyOptions.isPartitionKeyScopedSessionCapturingEnabled()) {
                            this.collectionResourceIdToRegionScopedSessionTokens.remove(rid);
                        }
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
        String partitionKey = request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY);
        Map<String, String> sessionTokenToRegionMapping
            = diagnosticsAccessor.getSessionTokenToRegionMappings(request.requestContext.cosmosDiagnostics);

        if (!Strings.isNullOrEmpty(token)) {
            ValueHolder<ResourceId> resourceId = ValueHolder.initialize(null);
            ValueHolder<String> collectionName = ValueHolder.initialize(null);

            if (shouldUpdateSessionToken(request, responseHeaders, resourceId, collectionName)) {
                this.setSessionToken(request, resourceId.v, sessionTokenToRegionMapping, collectionName.v, token, partitionKey);
            }
        }
    }

    @Override
    public void setSessionToken(String collectionRid, String collectionFullName, Map<String, String> responseHeaders) {
        if (this.disableSessionCapturing) {
            return;
        }

        ResourceId resourceId = ResourceId.parse(collectionRid);
        String collectionName = PathsHelper.getCollectionPath(collectionFullName);

        String token = responseHeaders.get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        String partitionKey = responseHeaders.get(HttpConstants.HttpHeaders.PARTITION_KEY);

        if (!Strings.isNullOrEmpty(token)) {
            this.setSessionToken(null, resourceId, new ConcurrentHashMap<>(), collectionName, token, partitionKey);
        }
    }

    @Override
    public void setSessionToken(RxDocumentServiceRequest request, String collectionRid, String collectionFullName, Map<String, String> responseHeaders) {
        if (this.disableSessionCapturing) {
            return;
        }

        ResourceId resourceId = ResourceId.parse(collectionRid);
        String collectionName = PathsHelper.getCollectionPath(collectionFullName);
        Map<String, String> sessionTokenToRegionMapping = diagnosticsAccessor.getSessionTokenToRegionMappings(request.requestContext.cosmosDiagnostics);

        String token = responseHeaders.get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        String partitionKey = responseHeaders.get(HttpConstants.HttpHeaders.PARTITION_KEY);

        if (!Strings.isNullOrEmpty(token)) {
            this.setSessionToken(request, resourceId, sessionTokenToRegionMapping, collectionName, token, partitionKey);
        }
    }

    private void setSessionToken(RxDocumentServiceRequest request, ResourceId resourceId, Map<String, String> sessionTokenToRegionMapping, String collectionName, String token, String partitionKey) {
        String partitionKeyRangeId;
        ISessionToken parsedSessionToken;

        String[] tokenParts = StringUtils.split(token, ':');
        partitionKeyRangeId = tokenParts[0];
        parsedSessionToken = SessionTokenHelper.parse(tokenParts[1]);

        if (logger.isTraceEnabled()) {
            logger.trace("UPDATE SESSION token {} {} {}", resourceId.getUniqueDocumentCollectionId(), collectionName, parsedSessionToken);
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
                this.addSessionToken(request, resourceId, sessionTokenToRegionMapping, partitionKeyRangeId, partitionKey, parsedSessionToken);
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
                addSessionToken(request, resourceId, sessionTokenToRegionMapping, partitionKeyRangeId, partitionKey, parsedSessionToken);
            } finally {
                this.writeLock.unlock();
            }
        }
    }

    private void updateExistingPkRangeIdScopedTokensInternal(ConcurrentHashMap<String, ISessionToken>  existingTokens, String partitionKeyRangeId, ISessionToken parsedSessionToken) {
        existingTokens.merge(partitionKeyRangeId, parsedSessionToken, (existingSessionTokens, newSessionToken) -> {
            try {
                if (existingSessionTokens == null) {
                    return newSessionToken;
                }

                return existingSessionTokens.merge(newSessionToken);
            } catch (CosmosException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void recordPartitionKeyInBloomFilter(
        Long collectionRid,
        Map<String, String> sessionTokenToRegionMapping,
        String partitionKey) {

        if (sessionTokenToRegionMapping != null && !sessionTokenToRegionMapping.isEmpty()) {
            if (!Strings.isNullOrEmpty(partitionKey)) {

                if (Strings.isNullOrEmpty(this.firstPreferredWritableRegionCached.get())) {
                    this.firstPreferredWritableRegionCached.set(extractFirstEffectivePreferredWritableRegion(this.globalEndpointManager));
                }

                this.partitionKeyBasedBloomFilter.tryRecordPartitionKey(
                    collectionRid,
                    this.firstPreferredWritableRegionCached.get(),
                    sessionTokenToRegionMapping,
                    partitionKey);
            }
        }
    }

    private void recordRegionScopedSessionToken(
        PkRangeBasedRegionScopedSessionTokenRegistry pkRangeBasedRegionScopedSessionTokenRegistry,
        Map<String, String> sessionTokenToRegionMapping,
        String pkRangeId,
        ISessionToken parsedSessionToken) {

        if (sessionTokenToRegionMapping != null && !sessionTokenToRegionMapping.isEmpty()) {
            pkRangeBasedRegionScopedSessionTokenRegistry.tryRecordSessionToken(
                sessionTokenToRegionMapping, pkRangeId, parsedSessionToken);
        }
    }

    private void addSessionToken(RxDocumentServiceRequest request, ResourceId resourceId, Map<String, String> sessionTokenToRegionMapping, String partitionKeyRangeId, String partitionKey, ISessionToken parsedSessionToken) {

        final Long collectionResourceId = resourceId.getUniqueDocumentCollectionId();

        ConcurrentHashMap<String, ISessionToken> existingPkRangeIdScopedTokensIfAny = this.collectionResourceIdToSessionTokens.get(collectionResourceId);
        PkRangeBasedRegionScopedSessionTokenRegistry pkRangeBasedRegionScopedSessionTokenRegistry = this.collectionResourceIdToRegionScopedSessionTokens.get(collectionResourceId);

        if (existingPkRangeIdScopedTokensIfAny != null) {
            // if an entry for this collection exists, no need to lock the outer ConcurrentHashMap.
            updateExistingPkRangeIdScopedTokensInternal(existingPkRangeIdScopedTokensIfAny, partitionKeyRangeId, parsedSessionToken);

            if (this.sessionConsistencyOptions.isPartitionKeyScopedSessionCapturingEnabled()) {

                if (shouldPartitionKeyBeRecorded(this.globalEndpointManager, request, this.sessionConsistencyOptions)) {
                    this.partitionKeyBasedBloomFilter.tryInitializeBloomFilter();
                    this.recordPartitionKeyInBloomFilter(collectionResourceId, sessionTokenToRegionMapping, partitionKey);
                }

                if (pkRangeBasedRegionScopedSessionTokenRegistry != null) {
                    this.recordRegionScopedSessionToken(
                        pkRangeBasedRegionScopedSessionTokenRegistry,
                        sessionTokenToRegionMapping,
                        partitionKeyRangeId,
                        parsedSessionToken);
                }
            }
        } else {
            this.collectionResourceIdToSessionTokens.compute(
                resourceId.getUniqueDocumentCollectionId(), (k, existingTokens) -> {
                    if (existingTokens == null) {
                        logger.info("Registering a new collection resourceId [{}] in SessionTokens", resourceId);
                        ConcurrentHashMap<String, ISessionToken> tokens =
                            new ConcurrentHashMap<>(200, 0.75f, 2000);
                        tokens.put(partitionKeyRangeId, parsedSessionToken);
                        return tokens;
                    }

                    updateExistingPkRangeIdScopedTokensInternal(existingTokens, partitionKeyRangeId, parsedSessionToken);
                    return existingTokens;
                });

            if (this.sessionConsistencyOptions.isPartitionKeyScopedSessionCapturingEnabled() && pkRangeBasedRegionScopedSessionTokenRegistry == null) {

                // populate pkRangeBasedRegionScopedSessionTokenRegistry
                this.collectionResourceIdToRegionScopedSessionTokens.compute(
                    resourceId.getUniqueDocumentCollectionId(), (k, pkRangeBasedRegionScopedSessionTokenRegistryAsVal) -> {

                        if (pkRangeBasedRegionScopedSessionTokenRegistryAsVal == null) {
                            logger.info("Registering a new collection resourceId [{}] in RegionScopedSessionTokenRegistry", resourceId);
                            pkRangeBasedRegionScopedSessionTokenRegistryAsVal = new PkRangeBasedRegionScopedSessionTokenRegistry();
                        }

                        return pkRangeBasedRegionScopedSessionTokenRegistryAsVal;
                    }
                );

                pkRangeBasedRegionScopedSessionTokenRegistry = this.collectionResourceIdToRegionScopedSessionTokens.get(resourceId.getUniqueDocumentCollectionId());

                if (pkRangeBasedRegionScopedSessionTokenRegistry != null) {
                    this.recordRegionScopedSessionToken(
                        pkRangeBasedRegionScopedSessionTokenRegistry,
                        sessionTokenToRegionMapping,
                        partitionKeyRangeId,
                        parsedSessionToken);
                }

                if (shouldPartitionKeyBeRecorded(this.globalEndpointManager, request, this.sessionConsistencyOptions)) {
                    this.partitionKeyBasedBloomFilter.tryInitializeBloomFilter();
                    this.recordPartitionKeyInBloomFilter(collectionResourceId, sessionTokenToRegionMapping, partitionKey);
                }
            }
        }
    }

    private static String getCombinedSessionToken(ConcurrentHashMap<String, ISessionToken> tokens) {
        StringBuilder result = new StringBuilder();
        if (tokens != null) {
            for (Iterator<Entry<String, ISessionToken>> iterator = tokens.entrySet().iterator(); iterator.hasNext(); ) {
                Entry<String, ISessionToken> entry = iterator.next();
                result = result.append(entry.getKey()).append(":").append(entry.getValue().convertToString());
                if (iterator.hasNext()) {
                    result = result.append(",");
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

    private static boolean shouldPartitionKeyBeRecorded(
        GlobalEndpointManager globalEndpointManager,
        RxDocumentServiceRequest request,
        SessionConsistencyOptions sessionConsistencyOptions) {

        if (request == null || globalEndpointManager == null || sessionConsistencyOptions == null) {
            return false;
        }

        if (sessionConsistencyOptions.isPartitionKeyScopedSessionCapturingEnabled() && globalEndpointManager.canUseMultipleWriteLocations(request)) {
            return globalEndpointManager.getApplicableWriteEndpoints(Collections.emptyList()).size() > 1;
        }

        return false;
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
