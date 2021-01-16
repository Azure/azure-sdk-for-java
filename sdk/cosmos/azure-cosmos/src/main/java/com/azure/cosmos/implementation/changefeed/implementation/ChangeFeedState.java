// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class ChangeFeedState extends JsonSerializable {
    ChangeFeedState() {
    }

    public abstract FeedRangeContinuation getContinuation();

    public abstract ChangeFeedState setContinuation(FeedRangeContinuation continuation);

    public abstract FeedRangeInternal getFeedRange();

    public abstract ChangeFeedMode getMode();

    public abstract ChangeFeedStartFromInternal getStartFromSettings();

    public abstract String applyServerResponseContinuation(
        String serverContinuationToken,
        RxDocumentServiceRequest request);

    public abstract String getContainerRid();

    public static ChangeFeedState fromJson(String json) {
        checkNotNull(json, "Argument 'json' must not be null");

        final ObjectMapper mapper = Utils.getSimpleObjectMapper();

        try {
            return mapper.readValue(json, ChangeFeedState.class);
        } catch (IOException ioException) {
            throw new IllegalArgumentException(
                String.format("The change feed state continuation contains invalid or unsupported" +
                    " json: %s", json),
                ioException);
        }
    }

    public abstract void populateEffectiveRangeAndStartFromSettingsToRequest(RxDocumentServiceRequest request);

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();
    }

    @Override
    public String toString() {
        return this.toJson();
    }

    public abstract void populateRequest(RxDocumentServiceRequest request, int maxItemCount);
}
