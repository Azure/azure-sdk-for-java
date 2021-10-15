// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.AadTokenAuthorizationHelper;
import com.azure.cosmos.implementation.AuthorizationTokenType;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PathsHelper;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.ResourceId;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.RequestVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class BarrierRequestHelper {
    private final static Logger logger = LoggerFactory.getLogger(BarrierRequestHelper.class);

    public static Mono<RxDocumentServiceRequest> createAsync(
            DiagnosticsClientContext clientContext,
            RxDocumentServiceRequest request,
            IAuthorizationTokenProvider authorizationTokenProvider,
            Long targetLsn,
            Long targetGlobalCommittedLsn) {

        boolean isCollectionHeadRequest = BarrierRequestHelper.isCollectionHeadBarrierRequest(
                request.getResourceType(),
                request.getOperationType());

        AuthorizationTokenType originalRequestTokenType = request.authorizationTokenType;

        if (authorizationTokenProvider != null && authorizationTokenProvider.getAuthorizationTokenType() != null) {
            originalRequestTokenType = authorizationTokenProvider.getAuthorizationTokenType();
        }

        if (originalRequestTokenType == AuthorizationTokenType.Invalid) {
            String message = "AuthorizationTokenType not set for the read request";
            assert false : message;
            logger.error(message);
        }

        String authorizationToken = Strings.Emtpy;
        RxDocumentServiceRequest barrierLsnRequest = null;
        if (!isCollectionHeadRequest) {
            // DB Feed
            barrierLsnRequest = RxDocumentServiceRequest.create(clientContext,
                OperationType.HeadFeed,
                null,
                ResourceType.Database,
                null,
                originalRequestTokenType);
        } else if (request.getIsNameBased()) {
            // Name based server request

            // get the collection full name
            // dbs/{id}/colls/{collid}/
            String collectionLink = PathsHelper.getCollectionPath(request.getResourceAddress());
            barrierLsnRequest = RxDocumentServiceRequest.createFromName(clientContext,
                    OperationType.Head,
                    collectionLink,
                    ResourceType.DocumentCollection,
                    originalRequestTokenType);
        } else {
            // RID based Server request
            barrierLsnRequest = RxDocumentServiceRequest.create(clientContext,
                    OperationType.Head,
                    ResourceId.parse(request.getResourceId()).getDocumentCollectionId().toString(),
                    ResourceType.DocumentCollection,
                    null,
                    originalRequestTokenType);
        }

        barrierLsnRequest.getHeaders().put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());

        if (targetLsn != null && targetLsn > 0) {
            barrierLsnRequest.getHeaders().put(HttpConstants.HttpHeaders.TARGET_LSN, targetLsn.toString());
        }

        if (targetGlobalCommittedLsn != null && targetGlobalCommittedLsn > 0) {
            barrierLsnRequest.getHeaders().put(HttpConstants.HttpHeaders.TARGET_GLOBAL_COMMITTED_LSN, targetGlobalCommittedLsn.toString());
        }

        boolean hasAadToken = false;
        switch (originalRequestTokenType) {
            case PrimaryMasterKey:
            case PrimaryReadonlyMasterKey:
            case SecondaryMasterKey:
            case SecondaryReadonlyMasterKey:
                authorizationToken = authorizationTokenProvider.getUserAuthorizationToken(
                        barrierLsnRequest.getResourceAddress(),
                        isCollectionHeadRequest ? ResourceType.DocumentCollection : ResourceType.Database,
                        RequestVerb.HEAD,
                        barrierLsnRequest.getHeaders(),
                        originalRequestTokenType,
                        request.properties);
                break;


            case ResourceToken:
                authorizationToken = request.getHeaders().get(HttpConstants.HttpHeaders.AUTHORIZATION);
                break;

            case AadToken:
                hasAadToken = true;
                break;

            default:
                String unknownAuthToken =
                    "Unknown authorization token kind '" + originalRequestTokenType + "' for read request";
                assert false : unknownAuthToken;
                logger.error(unknownAuthToken);
                throw Exceptions.propagate(
                    new InternalServerErrorException(unknownAuthToken + " - " + RMResources.InternalServerError));
        }

        if (!hasAadToken) {
            barrierLsnRequest.getHeaders().put(HttpConstants.HttpHeaders.AUTHORIZATION, authorizationToken);
        }

        barrierLsnRequest.requestContext = request.requestContext.clone();

        if (request.getPartitionKeyRangeIdentity() != null) {
            barrierLsnRequest.routeTo(request.getPartitionKeyRangeIdentity());
        }
        if (request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY) != null) {
            barrierLsnRequest.getHeaders().put(HttpConstants.HttpHeaders.PARTITION_KEY, request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY));
            barrierLsnRequest.setPartitionKeyInternal(request.getPartitionKeyInternal());
        }
        if (request.getHeaders().get(WFConstants.BackendHeaders.COLLECTION_RID) != null) {
            barrierLsnRequest.getHeaders().put(WFConstants.BackendHeaders.COLLECTION_RID, request.getHeaders().get(WFConstants.BackendHeaders.COLLECTION_RID));
        }

        if (hasAadToken) {
            return authorizationTokenProvider.populateAuthorizationHeader(barrierLsnRequest);
        } else {
            return Mono.just(barrierLsnRequest);
        }
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
