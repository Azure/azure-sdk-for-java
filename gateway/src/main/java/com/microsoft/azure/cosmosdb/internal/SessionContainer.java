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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceResponse;

/**
 * Used internally to cache the collections' session tokens in the Azure Cosmos DB database service.
 */
public final class SessionContainer {
    /**
     * Session token cache that maps collection ResourceID to session tokens
     */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, Long>> collectionResourceIdToSessionTokens;
    /**
     * Collection ResourceID cache that maps collection name to collection ResourceID
     * When collection name is provided instead of self-link, this is used in combination with
     * collectionResourceIdToSessionTokens to retrieve the session token for the collection by name
     */
    private final ConcurrentHashMap<String, Long> collectionNameToCollectionResourceId;
    private final String hostName;

    public SessionContainer(final String hostName) {
        this(hostName,
                new ConcurrentHashMap<String, Long>(),
                new ConcurrentHashMap<Long, ConcurrentHashMap<String, Long>>());
    }

    public SessionContainer(final String hostName,
                            ConcurrentHashMap<String, Long> nameToRidMap,
                            ConcurrentHashMap<Long, ConcurrentHashMap<String, Long>> ridToTokensMap) {
        this.hostName = hostName;
        this.collectionResourceIdToSessionTokens = ridToTokensMap;
        this.collectionNameToCollectionResourceId = nameToRidMap;
    }

    public String getHostName() {
        return this.hostName;
    }

    private ConcurrentHashMap<String, Long> getPartitionKeyRangeIdToTokenMap(RxDocumentServiceRequest request) {
        return getPartitionKeyRangeIdToTokenMap(request.getIsNameBased(), request.getResourceId(), request.getResourceAddress());
    }

    private ConcurrentHashMap<String, Long> getPartitionKeyRangeIdToTokenMap(boolean isNameBased, String rId, String resourceAddress) {
        ConcurrentHashMap<String, Long> rangeIdToTokenMap = null;
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
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }

        return resolveGlobalSessionToken(request.getIsNameBased(), request.getResourceId(), request.getResourceAddress());
    }

    private String resolveGlobalSessionToken(boolean isNameBased, String rId, String resourceAddress) {
        ConcurrentHashMap<String, Long> rangeIdToTokenMap = this.getPartitionKeyRangeIdToTokenMap(isNameBased, rId, resourceAddress);
        if (rangeIdToTokenMap != null) {
            return this.getCombinedSessionToken(rangeIdToTokenMap);
        }

        return "";
    }

    public String resolveGlobalSessionToken(String collectionLink) {
        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink cannot be null");
        }

        PathInfo pathInfo = PathsHelper.parsePathSegments(collectionLink);

        if (pathInfo == null) {
            return "";
        }

        return this.resolveGlobalSessionToken(pathInfo.isNameBased, pathInfo.resourceIdOrFullName, pathInfo.resourcePath);
    }

    public void clearToken(final RxDocumentServiceRequest request) {
        Long collectionResourceId = null;
        if (!request.getIsNameBased()) {
            if (!StringUtils.isEmpty(request.getResourceId())) {
                ResourceId resourceId = ResourceId.parse(request.getResourceId());
                if (resourceId.getDocumentCollection() != 0) {
                    collectionResourceId = resourceId.getUniqueDocumentCollectionId();
                }
            }
        } else {
            String collectionName = Utils.getCollectionName(request.getResourceAddress());
            if (!StringUtils.isEmpty(collectionName)) {
                collectionResourceId = this.collectionNameToCollectionResourceId.get(collectionName);
                this.collectionNameToCollectionResourceId.remove(collectionName);
            }
        }
        if (collectionResourceId != null) {
            this.collectionResourceIdToSessionTokens.remove(collectionResourceId);
        }
    }

    public void setSessionToken(RxDocumentServiceRequest request, RxDocumentServiceResponse response) {        
        if (response != null && !request.isReadingFromMaster()) {
            String sessionToken = response.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);

            if (!StringUtils.isEmpty(sessionToken)) {
                
                String ownerFullName = response.getResponseHeaders().get(HttpConstants.HttpHeaders.OWNER_FULL_NAME);
                if (StringUtils.isEmpty(ownerFullName)) ownerFullName = request.getResourceAddress();

                String collectionName = Utils.getCollectionName(ownerFullName);

                String ownerId;
                if (!request.getIsNameBased()) {
                    ownerId = request.getResourceId();

                } else {
                    ownerId = response.getResponseHeaders().get(HttpConstants.HttpHeaders.OWNER_ID);
                    if (StringUtils.isEmpty(ownerId)) ownerId = request.getResourceId();
                }
                
                if (!StringUtils.isEmpty(ownerId)) {
                    ResourceId resourceId = ResourceId.parse(ownerId);

                    if (resourceId.getDocumentCollection() != 0 && !StringUtils.isEmpty(collectionName)) {
                        Long uniqueDocumentCollectionId = resourceId.getUniqueDocumentCollectionId();
                        this.setSessionToken(uniqueDocumentCollectionId, collectionName, sessionToken);
                    }
                }
            }
        }
    }

    private void setSessionToken(long collectionRid, String collectionName, String sessionToken) {
        this.collectionResourceIdToSessionTokens.putIfAbsent(collectionRid, new ConcurrentHashMap<String, Long>());
        this.compareAndSetToken(sessionToken, this.collectionResourceIdToSessionTokens.get(collectionRid));
        this.collectionNameToCollectionResourceId.putIfAbsent(collectionName, collectionRid);
    }

    private String getCombinedSessionToken(ConcurrentHashMap<String, Long> tokens) {
        StringBuilder result = new StringBuilder();
        if (tokens != null) {
            for (Iterator<Entry<String, Long>> iterator = tokens.entrySet().iterator(); iterator.hasNext(); ) {
                Entry<String, Long> entry = iterator.next();
                result = result.append(entry.getKey()).append(":").append(entry.getValue());
                if (iterator.hasNext()) {
                    result = result.append(",");
                }
            }
        }

        return result.toString();
    }

    private void compareAndSetToken(String newToken, ConcurrentHashMap<String, Long> oldTokens) {
        if (StringUtils.isNotEmpty(newToken)) {
            String[] newTokenParts = newToken.split(":");
            if (newTokenParts.length == 2) {
                String range = newTokenParts[0];
                Long newLSN = Long.parseLong(newTokenParts[1]);
                Boolean success;
                do {
                    Long oldLSN = oldTokens.putIfAbsent(range, newLSN);
                    // If there exists no previous value or if the previous value is greater than
                    // the current value, then we're done.
                    success = (oldLSN == null || newLSN < oldLSN);
                    if (!success) {
                        // replace the previous value with the current value.
                        success = oldTokens.replace(range, oldLSN, newLSN);
                    }
                } while (!success);
            }
        }
    }

    Long resolvePartitionLocalSessionToken(RxDocumentServiceRequest request, String partitionKeyRangeId) {
        return SessionTokenHelper.resolvePartitionLocalSessionToken(request, partitionKeyRangeId,
                this.getPartitionKeyRangeIdToTokenMap(request));
    }
}
