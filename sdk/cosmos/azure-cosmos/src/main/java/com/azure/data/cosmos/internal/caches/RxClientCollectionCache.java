// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.caches;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.internal.ISessionContainer;
import com.azure.data.cosmos.internal.AuthorizationTokenType;
import com.azure.data.cosmos.internal.ClearingSessionContainerClientRetryPolicy;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.IDocumentClientRetryPolicy;
import com.azure.data.cosmos.internal.IRetryPolicyFactory;
import com.azure.data.cosmos.internal.ObservableHelper;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.PathsHelper;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.RxDocumentServiceResponse;
import com.azure.data.cosmos.internal.RxStoreModel;
import com.azure.data.cosmos.internal.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    protected Mono<DocumentCollection> getByRidAsync(String collectionRid, Map<String, Object> properties) {
        IDocumentClientRetryPolicy retryPolicyInstance = new ClearingSessionContainerClientRetryPolicy(this.sessionContainer, this.retryPolicy.getRequestPolicy());
        return ObservableHelper.inlineIfPossible(
                () -> this.readCollectionAsync(PathsHelper.generatePath(ResourceType.DocumentCollection, collectionRid, false), retryPolicyInstance, properties)
                , retryPolicyInstance);
    }

    protected Mono<DocumentCollection> getByNameAsync(String resourceAddress, Map<String, Object> properties) {
        IDocumentClientRetryPolicy retryPolicyInstance = new ClearingSessionContainerClientRetryPolicy(this.sessionContainer, this.retryPolicy.getRequestPolicy());
        return ObservableHelper.inlineIfPossible(
                () -> this.readCollectionAsync(resourceAddress, retryPolicyInstance, properties),
                retryPolicyInstance);
    }

    private Mono<DocumentCollection> readCollectionAsync(String collectionLink, IDocumentClientRetryPolicy retryPolicyInstance, Map<String, Object> properties) {
       
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
                HttpConstants.HttpMethods.GET,
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

        Flux<RxDocumentServiceResponse> responseObs = this.storeModel.processMessage(request);
        return responseObs.map(response -> BridgeInternal.toResourceResponse(response, DocumentCollection.class)
                .getResource()).single();
    }
}
