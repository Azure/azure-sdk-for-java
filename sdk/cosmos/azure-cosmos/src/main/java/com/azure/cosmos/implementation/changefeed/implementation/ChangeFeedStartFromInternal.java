// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class ChangeFeedStartFromInternal extends JsonSerializable {
    ChangeFeedStartFromInternal() {
    }

    abstract void accept(ChangeFeedStartFromVisitor visitor);

    public static ChangeFeedStartFromInternal createFromBeginning() {
        return InstanceHolder.FROM_BEGINNING_SINGLETON;
    }

    public static ChangeFeedStartFromInternal createFromNow() {
        return InstanceHolder.FROM_NOW_SINGLETON;
    }

    public static ChangeFeedStartFromInternal createFromPointInTime(Instant pointInTime) {
        return new ChangeFeedStartFromPointInTimeImpl(pointInTime);
    }

    public static ChangeFeedStartFromInternal createFromEtagAndFeedRange(String etag, FeedRangeInternal feedRange) {
        return new ChangeFeedStartFromEtagAndFeedRangeImpl(etag, feedRange);
    }

    private static final class InstanceHolder {
        static final ChangeFeedStartFromBeginningImpl FROM_BEGINNING_SINGLETON =
            new ChangeFeedStartFromBeginningImpl();

        static final ChangeFeedStartFromNowImpl FROM_NOW_SINGLETON =
            new ChangeFeedStartFromNowImpl();
    }

    @Override
    public String toString() {
        return this.toJson();
    }

    public static ChangeFeedState fomJson(String json)  throws IOException {
        checkNotNull(json, "Argument 'json' must not be null");
        final ObjectMapper mapper = Utils.getSimpleObjectMapper();
        return mapper.readValue(json, ChangeFeedState.class);
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();
    }
}