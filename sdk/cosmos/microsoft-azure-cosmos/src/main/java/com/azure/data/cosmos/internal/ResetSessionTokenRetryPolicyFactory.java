// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.caches.RxClientCollectionCache;

public class ResetSessionTokenRetryPolicyFactory implements IRetryPolicyFactory {

    private final IRetryPolicyFactory retryPolicy;
    private final ISessionContainer sessionContainer;
    private final RxClientCollectionCache collectionCache;

    public ResetSessionTokenRetryPolicyFactory(ISessionContainer sessionContainer, RxClientCollectionCache collectionCache, IRetryPolicyFactory retryPolicy) {
        this.retryPolicy = retryPolicy;
        this.sessionContainer = sessionContainer;
        this.collectionCache = collectionCache;
    }

    @Override
    public IDocumentClientRetryPolicy getRequestPolicy() {
        return new RenameCollectionAwareClientRetryPolicy(this.sessionContainer, this.collectionCache, retryPolicy.getRequestPolicy());
    }
}
