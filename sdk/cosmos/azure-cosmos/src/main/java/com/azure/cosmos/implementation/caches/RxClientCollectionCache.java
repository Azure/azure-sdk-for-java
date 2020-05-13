// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.AuthorizationTokenType;
import com.azure.cosmos.implementation.ClearingSessionContainerClientRetryPolicy;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.IRetryPolicyFactory;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.ObservableHelper;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PathsHelper;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.Utils;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches collection information.
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class RxClientCollectionCache extends RxCollectionCache {

    private RxStoreModel storeModel;
    private final IAuthorizationTokenProvider tokenProvider;
    private final IRetryPolicyFactory retryPolicy;
    private final ISessionContainer sessionContainer;

    public RxClientCollectionCache(ISessionContainer sessionContainer,
            RxStoreModel storeModel,
            IAuthorizationTokenProvider tokenProvider,
            IRetryPolicyFactory retryPolicy) {
        this.storeModel = storeModel;
        this.tokenProvider = tokenProvider;
        this.retryPolicy = retryPolicy;
        this.sessionContainer = sessionContainer;
    }

    protected Mono<DocumentCollection> getByRidAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext, String collectionRid, Map<String, Object> properties) {
        DocumentClientRetryPolicy retryPolicyInstance = new ClearingSessionContainerClientRetryPolicy(this.sessionContainer, this.retryPolicy.getRequestPolicy());
        return ObservableHelper.inlineIfPossible(
                () -> this.readCollectionAsync(metaDataDiagnosticsContext, PathsHelper.generatePath(ResourceType.DocumentCollection, collectionRid, false), retryPolicyInstance, properties)
                , retryPolicyInstance);
    }

    protected Mono<DocumentCollection> getByNameAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext, String resourceAddress, Map<String, Object> properties) {
        DocumentClientRetryPolicy retryPolicyInstance = new ClearingSessionContainerClientRetryPolicy(this.sessionContainer, this.retryPolicy.getRequestPolicy());
        return ObservableHelper.inlineIfPossible(
                () -> this.readCollectionAsync(metaDataDiagnosticsContext, resourceAddress, retryPolicyInstance, properties),
                retryPolicyInstance);
    }

    private Mono<DocumentCollection> readCollectionAsync(MetadataDiagnosticsContext metaDataDiagnosticsContext,
                                                         String collectionLink,
                                                         DocumentClientRetryPolicy retryPolicyInstance,
                                                         Map<String, Object> properties) {

        String path = Utils.joinPath(collectionLink, null);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                path,
                new HashMap<>());

        request.getHeaders().put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());

        String resourceName = request.getResourceAddress();
        String authorizationToken = tokenProvider.getUserAuthorizationToken(
                resourceName,
                request.getResourceType(),
                RequestVerb.GET,
                request.getHeaders(),
                AuthorizationTokenType.PrimaryMasterKey,
                properties);

        try {
            authorizationToken = URLEncoder.encode(authorizationToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return Mono.error(new IllegalStateException("Failed to encode authtoken.", e));
        }
        request.getHeaders().put(HttpConstants.HttpHeaders.AUTHORIZATION, authorizationToken);

        if (retryPolicyInstance != null){
            retryPolicyInstance.onBeforeSendRequest(request);
        }
        ZonedDateTime addressCallStartTime = ZonedDateTime.now(ZoneOffset.UTC);
        Mono<RxDocumentServiceResponse> responseObs = this.storeModel.processMessage(request);
        return responseObs.map(response -> {
            if(metaDataDiagnosticsContext != null) {
                ZonedDateTime addressCallEndTime = ZonedDateTime.now(ZoneOffset.UTC);
                MetadataDiagnosticsContext.MetadataDiagnostics metaDataDiagnostic  = new MetadataDiagnosticsContext.MetadataDiagnostics(addressCallStartTime,
                    addressCallEndTime,
                    MetadataDiagnosticsContext.MetadataType.CONTAINER_LOOK_UP);
                metaDataDiagnosticsContext.addMetaDataDiagnostic(metaDataDiagnostic);
            }

            return BridgeInternal.toResourceResponse(response, DocumentCollection.class)
                .getResource();
        }).single();
    }
}
