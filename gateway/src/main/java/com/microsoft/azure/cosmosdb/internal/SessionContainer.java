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

package com.microsoft.azure.cosmosdb.internal;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.ISessionContainer;
import com.microsoft.azure.cosmosdb.rx.internal.ReplicatedResourceClientUtils;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static com.microsoft.azure.cosmosdb.rx.internal.Utils.ValueHolder;

/**
 * Used internally to cache the collections' session tokens in the Azure Cosmos DB database service.
 */
public final class SessionContainer implements ISessionContainer {
    private final Logger logger = LoggerFactory.getLogger(SessionContainer.class);

    /**
     * Session token cache that maps collection ResourceID to session tokens
     */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> collectionResourceIdToSessionTokens;
    /**
     * Collection ResourceID cache that maps collection name to collection ResourceID
     * When collection name is provided instead of self-link, this is used in combination with
     * collectionResourceIdToSessionTokens to retrieve the session token for the collection by name
     */
    private final ConcurrentHashMap<String, Long> collectionNameToCollectionResourceId;
    private final String hostName;

    public SessionContainer(final String hostName) {
        this(hostName,
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>());
    }

    public SessionContainer(final String hostName,
                            ConcurrentHashMap<String, Long> nameToRidMap,
                            ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> ridToTokensMap) {
        this.hostName = hostName;
        this.collectionResourceIdToSessionTokens = ridToTokensMap;
        this.collectionNameToCollectionResourceId = nameToRidMap;
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
    public void clearToken(String collectionRid, String collectionFullname, Map<String, String> responseHeader) {
        if (!Strings.isNullOrEmpty(collectionFullname)) {
            String collectionName = PathsHelper.getCollectionPath(collectionFullname);
            Long collectionIdAsLong = this.collectionNameToCollectionResourceId.remove(collectionName);
            if (collectionIdAsLong != null) {
                this.collectionResourceIdToSessionTokens.remove(collectionIdAsLong);
            }
        }

        if (!Strings.isNullOrEmpty(collectionRid)) {
            ResourceId resourceId = ResourceId.parse(collectionRid);
            long uniqueCollectionId = resourceId.getUniqueDocumentCollectionId();
            this.collectionResourceIdToSessionTokens.remove(uniqueCollectionId);

            collectionFullname = this.collectionNameToCollectionResourceId.entrySet().stream()
                    .filter(entry -> entry.getValue() == uniqueCollectionId).map(Entry::getKey)
                    .findFirst()
                    .orElse(null);

            if (collectionFullname != null) {
                this.collectionNameToCollectionResourceId.remove(collectionFullname);
            }
        }
    }

    @Override
    public void clearToken(final RxDocumentServiceRequest request, Map<String, String> responseHeaders) {
        if (request.getResourceType() == ResourceType.DocumentCollection
                && request.getOperationType() == OperationType.Delete) {
            // NOTE: this is additional to .Net and handles collection delete
            if (request.getIsNameBased()) {
                clearToken(null, request.getResourceAddress(), responseHeaders);
            } else {
                clearToken(request.getResourceId(), request.getResourceAddress(), responseHeaders);
            }
            return;
        }

        String ownerFullName = responseHeaders.get(HttpConstants.HttpHeaders.OWNER_FULL_NAME);
        String collectionName = PathsHelper.getCollectionPath(ownerFullName);
        String resourceIdString;

        if (!request.getIsNameBased()) {
            resourceIdString = request.getResourceId();
        } else {
            resourceIdString = responseHeaders.get(HttpConstants.HttpHeaders.OWNER_ID);
        }

        if (!Strings.isNullOrEmpty(resourceIdString)) {
            ResourceId resourceId = ResourceId.parse(resourceIdString);
            if (resourceId.getDocumentCollection() != 0) {
                this.collectionResourceIdToSessionTokens.remove(resourceId.getUniqueDocumentCollectionId());
            }
        }

        if (!StringUtils.isEmpty(collectionName)) {
            Long collectionResourceId = this.collectionNameToCollectionResourceId.get(collectionName);
            this.collectionNameToCollectionResourceId.remove(collectionName);
            if (collectionResourceId != null) {
                this.collectionResourceIdToSessionTokens.remove(collectionResourceId);
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
    public void setSessionToken(String collectionRid, String collectionFullname, Map<String, String> responseHeaders) {
        ResourceId resourceId = ResourceId.parse(collectionRid);
        String collectionName = PathsHelper.getCollectionPath(collectionFullname);
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

        logger.trace("Update Session token {} {} {}", resourceId.getUniqueDocumentCollectionId(), collectionName, parsedSessionToken);

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
                        } catch (DocumentClientException e) {
                            throw new IllegalStateException(e);
                        }
                    });

                    return existingTokens;
                }
        );

        if (collectionName != null && resourceId != null && resourceId.getUniqueDocumentCollectionId() != 0) {
            this.collectionNameToCollectionResourceId.compute(collectionName, (k, v) -> resourceId.getUniqueDocumentCollectionId());
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

            if (resourceId.v.getDocumentCollection() != 0 &&
                    collectionName != null &&
                    !ReplicatedResourceClientUtils.isReadingFromMaster(request.getResourceType(), request.getOperationType())) {
                return true;
            }
        }

        return false;
    }
}
