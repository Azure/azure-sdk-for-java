// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.ChangeFeedProcessorContext;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.models.FeedResponse;

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

    public double getRequestCharge() {

        if (this.changeFeedObserverContext == null) {
            throw new IllegalStateException("changeFeedObserverContext cannot be null!");
        }

        FeedResponse<T> feedResponse = changeFeedObserverContext.getFeedResponse();

        if (feedResponse == null) {
            return 0;
        }

        return changeFeedObserverContext.getFeedResponse().getRequestCharge();
    }

    public String getSessionToken() {

        if (this.changeFeedObserverContext == null) {
            throw new IllegalStateException("changeFeedObserverContext cannot be null!");
        }

        FeedResponse<T> feedResponse = changeFeedObserverContext.getFeedResponse();

        if (feedResponse == null) {
            return StringUtils.EMPTY;
        }

        return feedResponse.getSessionToken();
    }

    public CosmosDiagnostics getDiagnostics() {

        if (this.changeFeedObserverContext == null) {
            throw new IllegalStateException("changeFeedObserverContext cannot be null!");
        }

        FeedResponse<T> feedResponse = changeFeedObserverContext.getFeedResponse();

        if (feedResponse == null) {
            return null;
        }

        return feedResponse.getCosmosDiagnostics();
    }
}
