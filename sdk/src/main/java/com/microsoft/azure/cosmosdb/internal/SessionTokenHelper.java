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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;

/**
 * Used internally to provides helper functions to work with session tokens in the Azure Cosmos DB database service.
 */
public class SessionTokenHelper {
    public static void setPartitionLocalSessionToken(RxDocumentServiceRequest request, SessionContainer sessionContainer) throws DocumentClientException {
        String originalSessionToken = request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        String partitionKeyRangeId = request.getResolvedPartitionKeyRange().getId();

        // Add support for partitioned collections
        if (StringUtils.isNotEmpty(originalSessionToken)) {
            Long sessionLsn = getLocalSessionToken(request, originalSessionToken, partitionKeyRangeId);
            if (sessionLsn != null) {
                request.setSessionLsn(sessionLsn);
            }
        } else {
            Long sessionLsn = sessionContainer.resolvePartitionLocalSessionToken(request, partitionKeyRangeId);
            if (sessionLsn != null) {
                request.setSessionLsn(sessionLsn);
            }
        }

        if (request.getSessionLsn() == -1) {
            request.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
        } else {
            request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, String.format("%1s:%2d", "0", request.getSessionLsn()));
        }
    }

    private static Long getLocalSessionToken(
            RxDocumentServiceRequest request,
            String sessionToken,
            String partitionKeyRangeId) throws DocumentClientException {

        if (partitionKeyRangeId == null || partitionKeyRangeId.isEmpty()) {
            // AddressCache/address resolution didn't produce partition key range id.
            // In this case it is a bug.
            throw new IllegalStateException("Partition key range Id is absent in the context.");
        }

        String[] localTokens = sessionToken.split(",");
        Set<String> partitionKeyRangeSet = new HashSet<>();
        partitionKeyRangeSet.add(partitionKeyRangeId);
        Long maxLsn = null;

        if (request.getResolvedPartitionKeyRange() != null && request.getResolvedPartitionKeyRange().getParents() != null) {
            partitionKeyRangeSet.addAll(request.getResolvedPartitionKeyRange().getParents());
        }

        for (String localToken : localTokens) {
            String[] items = localToken.split(":");
            Long tokenLsn = null;
            if (items.length == 2) {
                try {
                    tokenLsn = Long.parseLong(items[1]);
                } catch (NumberFormatException exception) {
                    // do nothing and throw after this
                }
            }
            if (tokenLsn == null) {
                throw new DocumentClientException(HttpConstants.StatusCodes.BADREQUEST, "Invalid session token value.");
            }
            if (partitionKeyRangeSet.contains(items[0])) {
                if (maxLsn == null || maxLsn < tokenLsn) {
                    maxLsn = tokenLsn;
                }
            }
        }

        return maxLsn;
    }

    static Long resolvePartitionLocalSessionToken(RxDocumentServiceRequest request,
                                                  String partitionKeyRangeId,
                                                  ConcurrentHashMap<String, Long> rangeIdToTokenMap) {
        if (rangeIdToTokenMap != null) {
            if (rangeIdToTokenMap.containsKey(partitionKeyRangeId)) {
                return rangeIdToTokenMap.get(partitionKeyRangeId);
            }
            else {
                Collection<String> parents = request.getResolvedPartitionKeyRange().getParents();
                if (parents != null) {
                    List<String> parentsList = new ArrayList<>(parents);
                    for (int i = parentsList.size() - 1; i >= 0; i--) {
                        String parentId = parentsList.get(i);
                        if (rangeIdToTokenMap.containsKey(parentId)) {
                            return rangeIdToTokenMap.get(parentId);
                        }
                    }
                }
            }
        }

        return null;
    }

    public static Long resolvePartitionLocalSessionToken(RxDocumentServiceRequest request,
                                                         String partitionKeyRangeId,
                                                         SessionContainer sessionContainer) throws DocumentClientException {
        return sessionContainer.resolvePartitionLocalSessionToken(request, partitionKeyRangeId);
    }
}
