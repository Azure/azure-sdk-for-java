// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.ChangeFeedProcessorContext;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;

public final class ChangeFeedProcessorContextImpl<T> implements ChangeFeedProcessorContext {

    private final ChangeFeedObserverContext<T> changeFeedObserverContext;

    public ChangeFeedProcessorContextImpl(ChangeFeedObserverContext<T> changeFeedObserverContext) {
        this.changeFeedObserverContext = changeFeedObserverContext;
    }

    @Override
    public String getLeaseToken() {

        if (changeFeedObserverContext == null) {
            throw new IllegalStateException("changeFeedObserverContext cannot be null!");
        }

        return changeFeedObserverContext.getLeaseToken();
    }

    @Override
    public CosmosDiagnostics getDiagnostics() {
        if (changeFeedObserverContext == null) {
            throw new IllegalStateException("changeFeedObserverContext cannot be null!");
        }

        return changeFeedObserverContext.getFeedResponse().getCosmosDiagnostics();
    }
}
