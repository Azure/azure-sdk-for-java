// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

@JsonDeserialize(using = ChangeFeedStateDeserializer.class)
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

    public static ChangeFeedState fromString(String base64EncodedJson) {
        checkNotNull(base64EncodedJson, "Argument 'base64EncodedJson' must not be null");

        String json = new String(
            Base64.getUrlDecoder().decode(base64EncodedJson),
            StandardCharsets.UTF_8);

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

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();
    }

    @Override
    public String toString() {
        String json = this.toJson();

        if (json == null) {
            return "";
        }

        return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public abstract void populateRequest(RxDocumentServiceRequest request, int maxItemCount);
}
