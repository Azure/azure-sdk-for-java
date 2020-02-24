// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.CosmosClientException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.azure.data.cosmos.internal.Utils.ValueHolder;

/**
 * Used internally to cache the collections' session tokens in the Azure Cosmos DB database service.
 */
public final class SessionContainer implements ISessionContainer {
    private final Logger logger = LoggerFactory.getLogger(SessionContainer.class);

    /**
     * SESSION token cache that maps collection ResourceID to session tokens
     */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> collectionResourceIdToSessionTokens = new ConcurrentHashMap<>();
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

    public SessionContainer(final String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getSessionToken(String collectionLink) {

        PathInfo pathInfo = new PathInfo(false, null, null, false);
        ConcurrentHashMap<String, ISessionToken> partitionKeyRangeIdToTokenMap = null;
        if (PathsHelper.tryParsePathSegments(collectionLink, pathInfo, null)) {
            Long UniqueDocumentCollectionId = null;
            if (pathInfo.isNameBased) {
                String collectionName = PathsHelper.getCollectionPath(pathInfo.resourceIdOrFullName);
                UniqueDocumentCollectionId = this.collectionNameToCollectionResourceId.get(collectionName);
            } else {
                ResourceId resourceId = ResourceId.parse(pathInfo.resourceIdOrFullName);
                if (resourceId.getDocumentCollection() != 0) {
                    UniqueDocumentCollectionId = resourceId.getUniqueDocumentCollectionId();
                }
            }

            if (UniqueDocumentCollectionId != null) {
                partitionKeyRangeIdToTokenMap = this.collectionResourceIdToSessionTokens.get(UniqueDocumentCollectionId);
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


    public String resolveGlobalSessionToken(RxDocumentServiceRequest request) {
        ConcurrentHashMap<String, ISessionToken> partitionKeyRangeIdToTokenMap = this.getPartitionKeyRangeIdToTokenMap(request);
        if (partitionKeyRangeIdToTokenMap != null) {
            return SessionContainer.getCombinedSessionToken(partitionKeyRangeIdToTokenMap);
        }

        return StringUtils.EMPTY;
    }

    @Override
    public ISessionToken resolvePartitionLocalSessionToken(RxDocumentServiceRequest request, String partitionKeyRangeId) {
        return SessionTokenHelper.resolvePartitionLocalSessionToken(request,
                partitionKeyRangeId,
                this.getPartitionKeyRangeIdToTokenMap(request));
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
                    }
                } finally {
                    this.writeLock.unlock();
                }
            }
        }
    }

    @Override
    public void setSessionToken(RxDocumentServiceRequest request, Map<String, String> responseHeaders) {
        String token = responseHeaders.get(HttpConstants.HttpHeaders.SESSION_TOKEN);

        if (!Strings.isNullOrEmpty(token)) {
            ValueHolder<ResourceId> resourceId = ValueHolder.initialize(null);
            ValueHolder<String> collectionName = ValueHolder.initialize(null);

            if (shouldUpdateSessionToken(request, responseHeaders, resourceId, collectionName)) {
                this.setSessionToken(resourceId.v, collectionName.v, token);
            }
        }
    }

    @Override
    public void setSessionToken(String collectionRid, String collectionFullName, Map<String, String> responseHeaders) {
        ResourceId resourceId = ResourceId.parse(collectionRid);
        String collectionName = PathsHelper.getCollectionPath(collectionFullName);
        String token = responseHeaders.get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        if (!Strings.isNullOrEmpty(token)) {
            this.setSessionToken(resourceId, collectionName, token);
        }
    }

    private void setSessionToken(ResourceId resourceId, String collectionName, String token) {
        String partitionKeyRangeId;
        ISessionToken parsedSessionToken;

        String[] tokenParts = StringUtils.split(token, ':');
        partitionKeyRangeId = tokenParts[0];
        parsedSessionToken = SessionTokenHelper.parse(tokenParts[1]);

        logger.trace("UPDATE SESSION token {} {} {}", resourceId.getUniqueDocumentCollectionId(), collectionName, parsedSessionToken);

        boolean isKnownCollection;

        this.readLock.lock();
        try {
            isKnownCollection = this.collectionNameToCollectionResourceId.containsKey(collectionName) &&
                    this.collectionResourceIdToCollectionName.containsKey(resourceId.getUniqueDocumentCollectionId()) &&
                    this.collectionNameToCollectionResourceId.get(collectionName) == resourceId.getUniqueDocumentCollectionId() &&
                    this.collectionResourceIdToCollectionName.get(resourceId.getUniqueDocumentCollectionId()).equals(collectionName);
            if (isKnownCollection) {
                this.addSessionToken(resourceId, partitionKeyRangeId, parsedSessionToken);
            }
        } finally {
            this.readLock.unlock();
        }

        if (!isKnownCollection) {
            this.writeLock.lock();
            try {
                if (collectionName != null && resourceId.getUniqueDocumentCollectionId() != 0) {
                    this.collectionNameToCollectionResourceId.compute(collectionName, (k, v) -> resourceId.getUniqueDocumentCollectionId());
                    this.collectionResourceIdToCollectionName.compute(resourceId.getUniqueDocumentCollectionId(), (k, v) -> collectionName);
                }
                addSessionToken(resourceId, partitionKeyRangeId, parsedSessionToken);
            } finally {
                this.writeLock.unlock();
            }
        }
    }

    private void addSessionToken(ResourceId resourceId, String partitionKeyRangeId, ISessionToken parsedSessionToken) {
        this.collectionResourceIdToSessionTokens.compute(
                resourceId.getUniqueDocumentCollectionId(), (k, existingTokens) -> {
                    if (existingTokens == null) {
                        ConcurrentHashMap<String, ISessionToken> tokens = new ConcurrentHashMap<>();
                        tokens.put(partitionKeyRangeId, parsedSessionToken);
                        return tokens;
                    }

                    existingTokens.merge(partitionKeyRangeId, parsedSessionToken, (existingSessionTokens, newSessionToken) -> {
                        try {
                            if (existingSessionTokens == null) {
                                return newSessionToken;
                            }

                            return existingSessionTokens.merge(newSessionToken);
                        } catch (CosmosClientException e) {
                            throw new IllegalStateException(e);
                        }
                    });

                    return existingTokens;
                }
        );
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

            if (resourceId.v.getDocumentCollection() != 0 &&
                    collectionName != null &&
                    !ReplicatedResourceClientUtils.isReadingFromMaster(request.getResourceType(), request.getOperationType())) {
                return true;
            }
        }

        return false;
    }
}
