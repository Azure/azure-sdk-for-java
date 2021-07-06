// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.caches.RxClientCollectionCache;

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
    public DocumentClientRetryPolicy getRequestPolicy() {
        return new RenameCollectionAwareClientRetryPolicy(this.sessionContainer, this.collectionCache, retryPolicy.getRequestPolicy());
    }

    @Override
    public RetryContext getRetryContext() {
        if (this.retryPolicy != null) {
            return this.retryPolicy.getRetryContext();
        } else {
            return null;
        }
    }

}
