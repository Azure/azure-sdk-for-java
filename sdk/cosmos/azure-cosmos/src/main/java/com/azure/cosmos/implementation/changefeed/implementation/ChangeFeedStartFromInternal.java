// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.Instant;

@JsonDeserialize(using = ChangeFeedStartFromInternalDeserializer.class)
public abstract class ChangeFeedStartFromInternal extends JsonSerializable {
    protected static final long START_FROM_BEGINNING_EPOCH_SECONDS = -62135596800L;
    protected static final Instant START_FROM_BEGINNING_TIME =
        Instant.ofEpochSecond(START_FROM_BEGINNING_EPOCH_SECONDS);

    ChangeFeedStartFromInternal() {
    }

    public static ChangeFeedStartFromInternal createFromBeginning() {
        return InstanceHolder.FROM_BEGINNING_SINGLETON;
    }

    public static ChangeFeedStartFromInternal createFromETagAndFeedRange(
        String eTag,
        FeedRangeInternal feedRange) {

        return new ChangeFeedStartFromETagAndFeedRangeImpl(eTag, feedRange);
    }

    public static ChangeFeedStartFromInternal createFromNow() {
        return InstanceHolder.FROM_NOW_SINGLETON;
    }

    public static ChangeFeedStartFromInternal createFromPointInTime(Instant pointInTime) {
        return new ChangeFeedStartFromPointInTimeImpl(pointInTime);
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();
    }

    @Override
    public String toString() {
        return this.toJson();
    }

    @Override
    public String toJson() {
        String json = super.toJson();

        if (json.indexOf("\"Type\":") != json.lastIndexOf("\"Type\":")) {
            // TODO @fabianm Remove as soon as root caused - https://github.com/Azure/azure-sdk-for-java/issues/20635
            // "StartFrom":{"Type":"NOW","Type":"NOW"}
            throw new IllegalStateException("There shouldn't be any duplicate json properties!");
        }

        return json;
    }

    public abstract boolean supportsFullFidelityRetention();

    public abstract void populateRequest(RxDocumentServiceRequest request);

    private static final class InstanceHolder {
        static final ChangeFeedStartFromBeginningImpl FROM_BEGINNING_SINGLETON =
            new ChangeFeedStartFromBeginningImpl();

        static final ChangeFeedStartFromNowImpl FROM_NOW_SINGLETON =
            new ChangeFeedStartFromNowImpl();
    }
}