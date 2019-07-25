// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.ISessionToken;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;

import java.util.Map;

public interface ISessionContainer {
    /**
     * Returns a serialized map of partitionKeyRangeId to session token. If a entity is name based then the method extracts name from
     * ResourceAddress and use it to identify collection otherwise it uses ResourceId. Returns empty string if collection is unknown
     * @param entity {@link RxDocumentServiceRequest entity}
     * @return serialzed map of partitionKeyRangeId to session token or empty string is collection is unknown
     */
    String resolveGlobalSessionToken(RxDocumentServiceRequest entity);

    /**
     * Returns a session token identified by partitionKeyRangeId(*) from a collection identified either by ResourceAddress
     * (in case of name based entity) or either by ResourceId.
     *
     * If partitionKeyRangeId is not in the collection's partitionKeyRangeId token map then method
     * iterates through request.RequestContext.ResolvedPartitionKeyRange.Parents starting from tail and
     * returns a corresponding token if there is a match.
     * @param entity {@link RxDocumentServiceRequest}
     * @param partitionKeyRangeId partitionKeyRangeId
     * @return Returns a session token identified by partitionKeyRangeId(*) from a collection identified either by ResourceAddress
     * (in case of name based entity) or either by ResourceId.
     */
    ISessionToken resolvePartitionLocalSessionToken(RxDocumentServiceRequest entity, String partitionKeyRangeId);

    /**
     * Atomically: removes partitionKeyRangeId token map associated with resourceId,
     * maps resourceId to collectionFullName and removes its map as well
     * @param resourceId resourceId
     */
    void clearTokenByResourceId(String resourceId);

    /**
     * Atomically: removes partitionKeyRangeId token map associated with collectionFullName, maps collectionFullName to resourceId and
     * removes its map as well.
     * @param collectionFullName collectionFullName
     */
    void clearTokenByCollectionFullName(String collectionFullName);

    /**
     * Infers collectionFullName using responseHeaders[HttpConstants.HttpHeaders.OwnerFullName] or request.ResourceAddress,
     * infers resourceId using responseHeaders[HttpConstants.HttpHeaders.OwnerId] or request.ResourceId,
     * and adds responseHeaders[HttpConstants.HttpHeaders.SessionToken] session token to the (collectionFullName, resourceId)'s
     * partitionKeyRangeId token map.
     *
     * NB: Silently does nothing for master queries, or when it's impossible to infer collectionRid and collectionFullName
     * from the request, or then SessionToken is missing in responseHeader.
     *
     * @param request {@link RxDocumentServiceRequest}
     * @param responseHeaders responseHeaders
     */
    void setSessionToken(RxDocumentServiceRequest request, Map<String, String> responseHeaders);

    /**
     * Adds responseHeaders[HttpConstants.HttpHeaders.SessionToken] session token to the (collectionFullName, collectionRid)'s partitionKeyRangeId  token map.
     * @param collectionRid collectionRid
     * @param collectionFullName collectionFullName
     * @param responseHeaders responseHeaders
     */
    void setSessionToken(String collectionRid, String collectionFullName, Map<String, String> responseHeaders);
}
