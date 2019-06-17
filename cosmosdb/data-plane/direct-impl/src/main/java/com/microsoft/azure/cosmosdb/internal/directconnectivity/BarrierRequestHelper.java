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

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.InternalServerErrorException;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.PathsHelper;
import com.microsoft.azure.cosmosdb.internal.ResourceId;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.Utils;
import com.microsoft.azure.cosmosdb.rx.internal.AuthorizationTokenType;
import com.microsoft.azure.cosmosdb.rx.internal.IAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;
import rx.exceptions.Exceptions;

import java.util.Map;

public class BarrierRequestHelper {
    private final static Logger logger = LoggerFactory.getLogger(BarrierRequestHelper.class);

    public static Single<RxDocumentServiceRequest> createAsync(
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
                Exceptions.propagate(new InternalServerErrorException(RMResources.InternalServerError));
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

        return Single.just(barrierLsnRequest);
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
