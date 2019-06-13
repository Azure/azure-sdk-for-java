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
package com.azure.data.cosmos.internal.caches;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.ISessionContainer;
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
import rx.Observable;
import rx.Single;

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

    protected Single<DocumentCollection> getByRidAsync(String collectionRid, Map<String, Object> properties) {
        IDocumentClientRetryPolicy retryPolicyInstance = new ClearingSessionContainerClientRetryPolicy(this.sessionContainer, this.retryPolicy.getRequestPolicy());
        return ObservableHelper.inlineIfPossible(
                () -> this.readCollectionAsync(PathsHelper.generatePath(ResourceType.DocumentCollection, collectionRid, false), retryPolicyInstance, properties)
                , retryPolicyInstance);
    }

    protected Single<DocumentCollection> getByNameAsync(String resourceAddress, Map<String, Object> properties) {
        IDocumentClientRetryPolicy retryPolicyInstance = new ClearingSessionContainerClientRetryPolicy(this.sessionContainer, this.retryPolicy.getRequestPolicy());
        return ObservableHelper.inlineIfPossible(
                () -> this.readCollectionAsync(resourceAddress, retryPolicyInstance, properties),
                retryPolicyInstance);
    }

    private Single<DocumentCollection> readCollectionAsync(String collectionLink, IDocumentClientRetryPolicy retryPolicyInstance, Map<String, Object> properties) {
       
        String path = Utils.joinPath(collectionLink, null);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                path,
                new HashMap<String, String>());

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
            return Single.error(new IllegalStateException("Failed to encode authtoken.", e));
        }
        request.getHeaders().put(HttpConstants.HttpHeaders.AUTHORIZATION, authorizationToken);

        if (retryPolicyInstance != null){
            retryPolicyInstance.onBeforeSendRequest(request);
        }

        Observable<RxDocumentServiceResponse> responseObs = this.storeModel.processMessage(request);
        return responseObs.map(response -> BridgeInternal.toResourceResponse(response, DocumentCollection.class)
                .getResource()).toSingle();
    }
}
