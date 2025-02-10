// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class FeedRangeContinuationTopicOffset {
    private static final String ITEM_LSN_KEY = "cosmos.source.feedRange.item.lsn";
    private static final String CONTINUATION_KEY = "cosmos.source.feedRange.responseContinuation";

    private final String responseContinuation;
    private final String itemLsn;

    public FeedRangeContinuationTopicOffset(
        String responseContinuation,
        String itemLsn) {
        checkArgument(StringUtils.isNotEmpty(responseContinuation), "Argument 'responseContinuation' should not be null");
        checkArgument(StringUtils.isNotEmpty(itemLsn), "Argument 'itemLsn' should not be null");

        this.itemLsn = itemLsn;
        this.responseContinuation = responseContinuation;
    }

    public String getResponseContinuation() {
        return responseContinuation;
    }

    public String getItemLsn() {
        return itemLsn;
    }

    public static Map<String, Object> toMap(FeedRangeContinuationTopicOffset offset) {
        Map<String, Object> map = new HashMap<>();
        map.put(CONTINUATION_KEY, offset.getResponseContinuation());
        map.put(ITEM_LSN_KEY, offset.getItemLsn());

        return map;
    }

    public static FeedRangeContinuationTopicOffset fromMap(Map<String, Object> offsetMap) {
        if (offsetMap == null) {
            return null;
        }

        String continuationState = offsetMap.get(CONTINUATION_KEY).toString();
        String itemLsn = offsetMap.get(ITEM_LSN_KEY).toString();
        return new FeedRangeContinuationTopicOffset(continuationState, itemLsn);
    }
}
