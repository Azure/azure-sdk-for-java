// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.ChangeFeedProcessorContext;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.models.FeedResponse;

public final class ChangeFeedProcessorContextImpl<T> implements ChangeFeedProcessorContext {

    private final ChangeFeedObserverContext<T> changeFeedObserverContext;
    private final FeedResponse<T> feedResponse;

    public ChangeFeedProcessorContextImpl(ChangeFeedObserverContext<T> changeFeedObserverContext) {
        this.changeFeedObserverContext = changeFeedObserverContext;
        this.feedResponse = changeFeedObserverContext.getFeedResponse();
    }

    @Override
    public String getLeaseToken() {

        if (changeFeedObserverContext == null) {
            throw new IllegalStateException("changeFeedObserverContext cannot be null!");
        }

        return changeFeedObserverContext.getLeaseToken();
    }

    @Override
    public CosmosDiagnosticsContext getDiagnostics() {
        if (changeFeedObserverContext == null) {
            throw new IllegalStateException("changeFeedObserverContext cannot be null!");
        }

        if (changeFeedObserverContext.getFeedResponse() == null) {
            throw new IllegalStateException("feed response cannot be null!");
        }

        if (changeFeedObserverContext.getFeedResponse().getCosmosDiagnostics() == null) {
            throw new IllegalStateException("cosmosDiagnostics cannot be null!");
        }

        return changeFeedObserverContext.getFeedResponse().getCosmosDiagnostics().getDiagnosticsContext();
    }
}
