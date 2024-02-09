// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementations.source;

import java.util.HashMap;
import java.util.Map;

public class FeedRangeContinuationTopicOffset {
    private static final String ITEM_LSN_KEY = "cosmos.source.feedRange.item.lsn";
    private static final String CONTINUATION_KEY = "cosmos.source.feedRange.continuation";

    private final String continuationState;
    private final String itemLsn;

    public FeedRangeContinuationTopicOffset(
        String continuationState,
        String itemLsn) {
        this.itemLsn = itemLsn;
        this.continuationState = continuationState;
    }

    public String getContinuationState() {
        return continuationState;
    }

    public String getItemLsn() {
        return itemLsn;
    }

    public static Map<String, Object> toMap(FeedRangeContinuationTopicOffset offset) {
        Map<String, Object> map = new HashMap<>();
        map.put(CONTINUATION_KEY, offset.getContinuationState());
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
