package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.ChangeFeedProcessorContext;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.models.FeedResponse;

public final class ChangeFeedProcessorContextImpl<T> extends ChangeFeedProcessorContext<T> {

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
    public FeedResponse<T> getFeedResponse() {

        if (changeFeedObserverContext == null) {
            throw new IllegalStateException("changeFeedObserverContext cannot be null!");
        }

        return changeFeedObserverContext.getFeedResponse();
    }
}
