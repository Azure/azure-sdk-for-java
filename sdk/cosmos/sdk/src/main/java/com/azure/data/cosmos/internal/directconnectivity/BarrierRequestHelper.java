// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.InternalServerErrorException;
import com.azure.data.cosmos.internal.AuthorizationTokenType;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.PathsHelper;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.ResourceId;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Strings;
import com.azure.data.cosmos.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.Map;

public class BarrierRequestHelper {
    private final static Logger logger = LoggerFactory.getLogger(BarrierRequestHelper.class);

    public static Mono<RxDocumentServiceRequest> createAsync(
            RxDocumentServiceRequest request,
            IAuthorizationTokenProvider authorizationTokenProvider,
            Long targetLsn,
            Long targetGlobalCommittedLsn) {

        boolean isCollectionHeadRequest = BarrierRequestHelper.isCollectionHeadBarrierRequest(
                request.getResourceType(),
                request.getOperationType());

        AuthorizationTokenType originalRequestTokenType = request.authorizationTokenType;

        if (originalRequestTokenType == AuthorizationTokenType.Invalid) {
            String message = "AuthorizationTokenType not set for the read request";
            assert false : message;
            logger.error(message);
        }

        String authorizationToken = Strings.Emtpy;
        RxDocumentServiceRequest barrierLsnRequest = null;
        if (!isCollectionHeadRequest) {
            // DB Feed
            barrierLsnRequest = RxDocumentServiceRequest.create(
                    OperationType.HeadFeed,
                    (String) null,
                    (ResourceType) ResourceType.Database,
                    (Map<String, String>) null);
        } else if (request.getIsNameBased()) {
            // Name based server request

            // get the collection full name
            // dbs/{id}/colls/{collid}/
            String collectionLink = PathsHelper.getCollectionPath(request.getResourceAddress());
            barrierLsnRequest = RxDocumentServiceRequest.createFromName(
                    OperationType.Head,
                    collectionLink,
                    ResourceType.DocumentCollection);
        } else {
            // RID based Server request
            barrierLsnRequest = RxDocumentServiceRequest.create(
                    OperationType.Head,
                    ResourceId.parse(request.getResourceId()).getDocumentCollectionId().toString(),
                    ResourceType.DocumentCollection, null);
        }

        barrierLsnRequest.getHeaders().put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());

        if (targetLsn != null && targetLsn > 0) {
            barrierLsnRequest.getHeaders().put(HttpConstants.HttpHeaders.TARGET_LSN, targetLsn.toString());
        }

        if (targetGlobalCommittedLsn != null && targetGlobalCommittedLsn > 0) {
            barrierLsnRequest.getHeaders().put(HttpConstants.HttpHeaders.TARGET_GLOBAL_COMMITTED_LSN, targetGlobalCommittedLsn.toString());
        }

        switch (originalRequestTokenType) {
            case PrimaryMasterKey:
            case PrimaryReadonlyMasterKey:
            case SecondaryMasterKey:
            case SecondaryReadonlyMasterKey:
                authorizationToken = authorizationTokenProvider.getUserAuthorizationToken(
                        barrierLsnRequest.getResourceAddress(),
                        isCollectionHeadRequest ? ResourceType.DocumentCollection : ResourceType.Database,
                        HttpConstants.HttpMethods.HEAD,
                        barrierLsnRequest.getHeaders(),
                        originalRequestTokenType,
                        request.properties);
                break;


            case ResourceToken:
                authorizationToken = request.getHeaders().get(HttpConstants.HttpHeaders.AUTHORIZATION);
                break;

            default:
                String unknownAuthToken = "Unknown authorization token kind for read request";
                assert false : unknownAuthToken;
                logger.error(unknownAuthToken);
                throw Exceptions.propagate(new InternalServerErrorException(RMResources.InternalServerError));
        }

        barrierLsnRequest.getHeaders().put(HttpConstants.HttpHeaders.AUTHORIZATION, authorizationToken);
        barrierLsnRequest.requestContext = request.requestContext.clone();

        if (request.getPartitionKeyRangeIdentity() != null) {
            barrierLsnRequest.routeTo(request.getPartitionKeyRangeIdentity());
        }
        if (request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY) != null) {
            barrierLsnRequest.getHeaders().put(HttpConstants.HttpHeaders.PARTITION_KEY, request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY));
        }
        if (request.getHeaders().get(WFConstants.BackendHeaders.COLLECTION_RID) != null) {
            barrierLsnRequest.getHeaders().put(WFConstants.BackendHeaders.COLLECTION_RID, request.getHeaders().get(WFConstants.BackendHeaders.COLLECTION_RID));
        }

        return Mono.just(barrierLsnRequest);
    }

    static boolean isCollectionHeadBarrierRequest(ResourceType resourceType, OperationType operationType) {
        switch (resourceType) {
            case Attachment:
            case Document:
            case Conflict:
            case StoredProcedure:
            case UserDefinedFunction:
            case Trigger:
                return true;
            case DocumentCollection:
                if (operationType != OperationType.ReadFeed && operationType != OperationType.Query && operationType != OperationType.SqlQuery) {
                    return true;
                } else {
                    return false;
                }
            case PartitionKeyRange:
                // no logic for OperationType.GetSplitPoint and OperationType.AbortSplit
                // as they are not applicable to SDK
                return false;
            default:
                return false;
        }
    }
}
