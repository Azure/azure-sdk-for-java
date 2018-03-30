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
package com.microsoft.azure.cosmosdb.rx.internal.caches;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.PathsHelper;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.Utils;
import com.microsoft.azure.cosmosdb.rx.internal.AuthorizationTokenType;
import com.microsoft.azure.cosmosdb.rx.internal.IAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.rx.internal.IDocumentClientRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.ObservableHelper;
import com.microsoft.azure.cosmosdb.rx.internal.RetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceResponse;
import com.microsoft.azure.cosmosdb.rx.internal.RxStoreModel;

import rx.Observable;
import rx.Single;

/**
 * Caches collection information.
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class RxClientCollectionCache extends RxCollectionCache {

    private RxStoreModel storeModel;
    private final IAuthorizationTokenProvider tokenProvider;
    private final RetryPolicy retryPolicy;

    public RxClientCollectionCache(RxStoreModel storeModel, 
            IAuthorizationTokenProvider tokenProvider, 
            RetryPolicy retryPolicy) {
        this.storeModel = storeModel;
        this.tokenProvider = tokenProvider;
        this.retryPolicy = retryPolicy;
    }

    protected Single<DocumentCollection> getByRidAsync(String collectionRid) {

        IDocumentClientRetryPolicy retryPolicyInstance = this.retryPolicy.getRequestPolicy();

        return ObservableHelper.inlineIfPossible(
                () -> this.readCollectionAsync(PathsHelper.generatePath(ResourceType.DocumentCollection, collectionRid, false), retryPolicyInstance)
                , retryPolicyInstance);
    }

    protected Single<DocumentCollection> getByNameAsync(String resourceAddress) {

        IDocumentClientRetryPolicy retryPolicyInstance = this.retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossible(
                () -> this.readCollectionAsync(resourceAddress, retryPolicyInstance),
                retryPolicyInstance);
    }

    private Single<DocumentCollection> readCollectionAsync(String collectionLink, IDocumentClientRetryPolicy retryPolicyInstance) {
       
        String path = Utils.joinPath(collectionLink, null);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.Read,
                ResourceType.DocumentCollection,
                path,
                // AuthorizationTokenType.PrimaryMasterKey,
                new HashMap<String, String>());

        request.getHeaders().put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());

        String resourceName = request.getResourceFullName();
        String authorizationToken = tokenProvider.getUserAuthorizationToken(
                resourceName,
                request.getResourceType(),
                HttpConstants.HttpMethods.GET,
                request.getHeaders(),
                AuthorizationTokenType.PrimaryMasterKey);

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
