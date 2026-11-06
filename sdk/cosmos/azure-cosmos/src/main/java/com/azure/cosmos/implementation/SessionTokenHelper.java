// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.collections.map.UnmodifiableMap;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.Utils.ValueHolder;

/**
 * Used internally to provides helper functions to work with session tokens in the Azure Cosmos DB database service.
 */
public class SessionTokenHelper {

    private static final Logger logger = LoggerFactory.getLogger(SessionTokenHelper.class);

    public static boolean isCompoundSessionToken(String sessionToken) {
        // Classify without parsing; validation happens when selecting partition-local progress.
        return sessionToken != null && sessionToken.indexOf(',') >= 0;
    }

    public static PartitionKeyRange resolveSessionTokenRange(
        RxDocumentServiceRequest request, DocumentCollection collection, CollectionRoutingMap routingMap,
        boolean explicitCompound) {
        String rangeHeader = request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID);
        PartitionKeyRange target = null;
        if (explicitCompound) {
            if (!collection.getResourceId().equals(routingMap.getCollectionUniqueId())) {
                return null;
            }
            PartitionKeyRangeIdentity identity = request.getPartitionKeyRangeIdentity();
            if (identity != null && identity.getCollectionRid() != null
                && !identity.getCollectionRid().equals(collection.getResourceId())) {
                return null;
            }
            String rangeId = identity == null ? null : identity.getPartitionKeyRangeId();
            if (!Strings.isNullOrEmpty(rangeHeader)) {
                String[] parts = rangeHeader.split(",", -1);
                if (parts.length > 2 || parts[parts.length - 1].isEmpty()
                    || (parts.length == 2 && !parts[0].equals(collection.getResourceId()))
                    || (rangeId != null && !rangeId.equals(parts[parts.length - 1]))) {
                    return null;
                }
                rangeId = parts[parts.length - 1];
            }
            target = rangeId == null ? null : routingMap.tryGetRangeByPartitionKeyRangeId(rangeId);
            if (rangeId != null && target == null) {
                throw new PartitionKeyRangeGoneException();
            }
        } else if (StringUtils.isNotEmpty(rangeHeader)) {
            return routingMap.getRangeByPartitionKeyRangeId(rangeHeader);
        }

        String start = request.getHeaders().get(HttpConstants.HttpHeaders.START_EPK);
        String end = request.getHeaders().get(HttpConstants.HttpHeaders.END_EPK);
        PartitionKeyInternal key = request.getPartitionKeyInternal();
        PartitionKeyRange scopeTarget = null;
        if (explicitCompound && (start != null || end != null)) {
            if (start == null || end == null || start.compareTo(end) >= 0
                || !ReadFeedKeyType.EffectivePartitionKeyRange.name().equalsIgnoreCase(
                    request.getHeaders().get(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE))) {
                return null;
            }
            List<PartitionKeyRange> overlapping =
                routingMap.getOverlappingRanges(new Range<>(start, end, true, false));
            if (overlapping.size() != 1) {
                return null;
            }
            scopeTarget = overlapping.get(0);
        } else if (key != null) {
            if (explicitCompound && collection.getPartitionKey().getKind() == PartitionKind.MULTI_HASH
                && key.getComponents().size() < collection.getPartitionKey().getPaths().size()) {
                return null;
            }
            String epk = explicitCompound ? null : request.getEffectivePartitionKey();
            if (StringUtils.isEmpty(epk)) {
                epk = PartitionKeyInternalHelper.getEffectivePartitionKeyString(key, collection.getPartitionKey());
            }
            if (!explicitCompound) {
                request.setEffectivePartitionKey(epk);
            }
            scopeTarget = routingMap.getRangeByEffectivePartitionKey(epk);
        } else if (explicitCompound && target == null && request.requestContext.resolvedPartitionKeyRange != null) {
            scopeTarget = routingMap.tryGetRangeByPartitionKeyRangeId(
                request.requestContext.resolvedPartitionKeyRange.getId());
        }
        if (target != null && scopeTarget != null && !target.getId().equals(scopeTarget.getId())) {
            return null;
        }
        return scopeTarget == null ? target : scopeTarget;
    }

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

    public static void setPartitionLocalSessionToken(RxDocumentServiceRequest request, ISessionContainer sessionContainer) {
        setPartitionLocalSessionToken(request, request.requestContext.resolvedPartitionKeyRange.getId(), sessionContainer);
    }

    public static void setPartitionLocalSessionToken(RxDocumentServiceRequest request, String partitionKeyRangeId, ISessionContainer sessionContainer) {
        String originalSessionToken = request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);

        if (Strings.isNullOrEmpty(partitionKeyRangeId)) {
            // AddressCache/address resolution didn't produce partition key range id.
            // In this case it is a bug.
            throw new InternalServerErrorException(
                Exceptions.getInternalServerErrorMessage(RMResources.PartitionKeyRangeIdAbsentInContext),
                HttpConstants.SubStatusCodes.MISSING_PARTITION_KEY_RANGE_ID_IN_CONTEXT);
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
                                     concatPartitionKeyRangeIdWithSessionToken(partitionKeyRangeId, request.requestContext.sessionToken.convertToString()));
        }
    }

    private static ISessionToken getLocalSessionToken(
            RxDocumentServiceRequest request,
            String globalSessionToken,
            String partitionKeyRangeId) {

        if (partitionKeyRangeId == null) {
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

        if (highestSessionToken == null) {
            if (StringUtils.isNotEmpty(globalSessionToken)) {
                Set<String> sessionTokenEvaluationResults = request.requestContext.getSessionTokenEvaluationResults();

                String evaluationResult = "The session token : " + globalSessionToken + " for pkRangeId : " + partitionKeyRangeId +
                    " and collectionRid : " + request.requestContext.resolvedCollectionRid + " could not be evaluated, " +
                    "the request will fallback " +
                    "to eventual consistency.";

                sessionTokenEvaluationResults.add(evaluationResult);

                if (Configs.shouldLogIncorrectlyMappedSessionToken()) {
                    logger.warn(evaluationResult);
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
                ISessionToken parentSessionToken = null;

                Collection<String> parents = request.requestContext.resolvedPartitionKeyRange != null
                    ? request.requestContext.resolvedPartitionKeyRange.getParents()
                    : null;
                if (parents != null && !parents.isEmpty()) {
                    List<String> parentsList = new ArrayList<>(parents);
                    for (int i = parentsList.size() - 1; i >= 0; i--) {
                        String parentId = parentsList.get(i);
                        if (rangeIdToTokenMap.containsKey(parentId)) {
                            // A partition can have more than 1 parent (merge). In that case, we apply Merge to generate a token with both parent's max LSNs
                            parentSessionToken =
                                parentSessionToken != null
                                    ? parentSessionToken.merge(rangeIdToTokenMap.get(parentId)) : rangeIdToTokenMap.get(parentId);
                        }
                    }

                    return parentSessionToken;
                }
            }
        }

        return null;
    }

    static ISessionToken resolvePartitionLocalSessionToken(RxDocumentServiceRequest request,
                                                           PartitionKeyBasedBloomFilter partitionKeyBasedBloomFilter,
                                                           PartitionScopedRegionLevelProgress partitionScopedRegionLevelProgress,
                                                           PartitionKeyInternal partitionKey,
                                                           PartitionKeyDefinition partitionKeyDefinition,
                                                           Long collectionRid,
                                                           String partitionKeyRangeId,
                                                           String firstEffectivePreferredReadableRegion,
                                                           GlobalEndpointManager globalEndpointManager) {

        if (partitionScopedRegionLevelProgress != null) {

            if (partitionScopedRegionLevelProgress.isPartitionKeyRangeIdPresent(partitionKeyRangeId)) {
                return partitionScopedRegionLevelProgress
                    .tryResolveSessionToken(
                        request,
                        collectionRid,
                        partitionKeyRangeId,
                        firstEffectivePreferredReadableRegion,
                        partitionKeyBasedBloomFilter,
                        globalEndpointManager);

            } else {
                ISessionToken parentSessionToken = null;

                Collection<String> parents = request.requestContext.resolvedPartitionKeyRange != null
                    ? request.requestContext.resolvedPartitionKeyRange.getParents()
                    : null;
                if (parents != null && !parents.isEmpty()) {
                    List<String> parentsList = new ArrayList<>(parents);
                    for (int i = parentsList.size() - 1; i >= 0; i--) {
                        String parentPkRangeId = parentsList.get(i);
                        if (partitionScopedRegionLevelProgress.isPartitionKeyRangeIdPresent(parentPkRangeId)) {
                            // A partition can have more than 1 parent (merge). In that case, we apply Merge to
                            // generate a token with both parent's max LSNs
                            ISessionToken resolvedSessionTokenForParentPkRangeId = null;

                            resolvedSessionTokenForParentPkRangeId = partitionScopedRegionLevelProgress
                                .tryResolveSessionToken(
                                    request,
                                    collectionRid,
                                    parentPkRangeId,
                                    firstEffectivePreferredReadableRegion,
                                    partitionKeyBasedBloomFilter,
                                    globalEndpointManager);

                            if (resolvedSessionTokenForParentPkRangeId != null) {
                                parentSessionToken = parentSessionToken != null ?
                                    parentSessionToken.merge(resolvedSessionTokenForParentPkRangeId) :
                                    resolvedSessionTokenForParentPkRangeId;
                            }
                        }
                    }

                    return parentSessionToken;
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
            throw new RuntimeException(new BadRequestException(String.format(RMResources.InvalidSessionToken, sessionToken)));
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

    public static void validateAndRemoveSessionToken(RxDocumentServiceRequest request) {
        String sessionToken = request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        if (!Strings.isNullOrEmpty(sessionToken)) {
            getLocalSessionToken(request, sessionToken, StringUtils.EMPTY);
            request.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
        }
    }

    public static  String concatPartitionKeyRangeIdWithSessionToken(String partitionKeyRangeRid, String sessionToken) {
        // e.g., "1:xyz"
        return partitionKeyRangeRid + ":" + sessionToken;
    }

    public static boolean tryEvaluateLocalLsnByRegionMappingWithNullSafety(ISessionToken sessionToken, Utils.ValueHolder<UnmodifiableMap<Integer, Long>> localLsnByRegion) {

        if (sessionToken instanceof VectorSessionToken) {
            VectorSessionToken castVectorSessionToken = Utils.as(sessionToken, VectorSessionToken.class);
            localLsnByRegion.v = castVectorSessionToken.getLocalLsnByRegion();

            return localLsnByRegion.v != null;
        }

        return false;
    }

    public static boolean tryEvaluateVersion(ISessionToken sessionToken, Utils.ValueHolder<Long> version) {

        if (sessionToken instanceof VectorSessionToken) {
            VectorSessionToken castVectorSessionToken = Utils.as(sessionToken, VectorSessionToken.class);
            version.v = castVectorSessionToken.getVersion();
            return true;
        }

        return false;
    }
}
