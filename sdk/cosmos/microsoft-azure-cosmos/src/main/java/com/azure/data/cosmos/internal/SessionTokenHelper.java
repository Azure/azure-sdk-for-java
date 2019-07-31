// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BadRequestException;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.InternalServerErrorException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.data.cosmos.internal.Utils.ValueHolder;

/**
 * Used internally to provides helper functions to work with session tokens in the Azure Cosmos DB database service.
 */
public class SessionTokenHelper {

    public static void setOriginalSessionToken(RxDocumentServiceRequest request, String originalSessionToken) {
        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }

        if (originalSessionToken == null) {
            request.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
        } else {
            request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, originalSessionToken);
        }
    }

    public static void setPartitionLocalSessionToken(RxDocumentServiceRequest request, ISessionContainer sessionContainer) throws CosmosClientException {
        String originalSessionToken = request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        String partitionKeyRangeId = request.requestContext.resolvedPartitionKeyRange.id();


        if (Strings.isNullOrEmpty(partitionKeyRangeId)) {
            // AddressCache/address resolution didn't produce partition key range id.
            // In this case it is a bug.
            throw new InternalServerErrorException(RMResources.PartitionKeyRangeIdAbsentInContext);
        }

        if (StringUtils.isNotEmpty(originalSessionToken)) {
            ISessionToken sessionToken = getLocalSessionToken(request, originalSessionToken, partitionKeyRangeId);
            request.requestContext.sessionToken = sessionToken;
        } else {
            // use ambient session token.
            ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, partitionKeyRangeId);
            request.requestContext.sessionToken = sessionToken;
        }

        if (request.requestContext.sessionToken == null) {
            request.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
        } else {
            request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN,
                                     String.format("%1s:%2s", partitionKeyRangeId, request.requestContext.sessionToken.convertToString()));
        }
    }

    private static ISessionToken getLocalSessionToken(
            RxDocumentServiceRequest request,
            String globalSessionToken,
            String partitionKeyRangeId) throws CosmosClientException {

        if (partitionKeyRangeId == null || partitionKeyRangeId.isEmpty()) {
            // AddressCache/address resolution didn't produce partition key range id.
            // In this case it is a bug.
            throw new IllegalStateException("Partition key range Id is absent in the context.");
        }

        // Convert global session token to local - there's no point in sending global token over the wire to the backend.
        // Global session token is comma separated array of <partitionkeyrangeid>:<lsn> pairs. For example:
        //          2:425344,748:2341234,99:42344
        // Local session token is single <partitionkeyrangeid>:<lsn> pair.
        // Backend only cares about pair which relates to the range owned by the partition.
        String[] localTokens = StringUtils.split(globalSessionToken, ",");
        Set<String> partitionKeyRangeSet = new HashSet<>();
        partitionKeyRangeSet.add(partitionKeyRangeId);

        ISessionToken highestSessionToken = null;

        if (request.requestContext.resolvedPartitionKeyRange != null && request.requestContext.resolvedPartitionKeyRange.getParents() != null) {
            partitionKeyRangeSet.addAll(request.requestContext.resolvedPartitionKeyRange.getParents());
        }

        for (String localToken : localTokens) {
            String[] items = StringUtils.split(localToken, ":");
            if (items.length != 2) {
                throw new BadRequestException(String.format(RMResources.InvalidSessionToken, partitionKeyRangeId));
            }

            ISessionToken parsedSessionToken = SessionTokenHelper.parse(items[1]);

            if (partitionKeyRangeSet.contains(items[0])) {

                if (highestSessionToken == null) {
                    highestSessionToken = parsedSessionToken;
                } else {
                    highestSessionToken = highestSessionToken.merge(parsedSessionToken);
                }

            }
        }

        return highestSessionToken;
    }

    static ISessionToken resolvePartitionLocalSessionToken(RxDocumentServiceRequest request,
                                                           String partitionKeyRangeId,
                                                           ConcurrentHashMap<String, ISessionToken> rangeIdToTokenMap) {
        if (rangeIdToTokenMap != null) {
            if (rangeIdToTokenMap.containsKey(partitionKeyRangeId)) {
                return rangeIdToTokenMap.get(partitionKeyRangeId);
            } else {
                Collection<String> parents = request.requestContext.resolvedPartitionKeyRange.getParents();
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

    public static ISessionToken parse(String sessionToken) {
        ValueHolder<ISessionToken> partitionKeyRangeSessionToken = ValueHolder.initialize(null);

        if (SessionTokenHelper.tryParse(sessionToken, partitionKeyRangeSessionToken)) {
            return partitionKeyRangeSessionToken.v;
        } else {
            throw new  RuntimeException(new BadRequestException(String.format(RMResources.InvalidSessionToken, sessionToken)));
        }
    }

    static boolean tryParse(String sessionToken, ValueHolder<ISessionToken> parsedSessionToken) {
        parsedSessionToken.v = null;
        if (!Strings.isNullOrEmpty(sessionToken)) {
            String[] sessionTokenSegments = StringUtils.split(sessionToken,":");
            return VectorSessionToken.tryCreate(sessionTokenSegments[sessionTokenSegments.length - 1], parsedSessionToken);
        } else {
            return false;
        }
    }

    public static void validateAndRemoveSessionToken(RxDocumentServiceRequest request) throws CosmosClientException {
        String sessionToken = request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        if (!Strings.isNullOrEmpty(sessionToken)) {
            getLocalSessionToken(request, sessionToken, StringUtils.EMPTY);
            request.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
        }
    }
}
