// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source;

import java.util.HashMap;
import java.util.Map;

public class FeedRangeContinuationTopicOffset {
    private static final String ITEM_LSN_KEY = "cosmos.source.feedRange.item.lsn";
    private static final String CONTINUATION_KEY = "cosmos.source.feedRange.continuation";

    private final String continuationState;
    private final String itemLsn;
    private final Map<String, Object> topicOffsetMap;

    public FeedRangeContinuationTopicOffset(
        String continuationState,
        String itemLsn) {
        this.itemLsn = itemLsn;
        this.continuationState = continuationState;

        this.topicOffsetMap = new HashMap<>();
        this.topicOffsetMap.put(CONTINUATION_KEY, continuationState);
        this.topicOffsetMap.put(ITEM_LSN_KEY, itemLsn);
    }

    public FeedRangeContinuationTopicOffset(Map<String, Object> topicOffsetMap) {
        this.topicOffsetMap = topicOffsetMap;
        this.continuationState = topicOffsetMap.get(CONTINUATION_KEY).toString();
        this.itemLsn = topicOffsetMap.get(ITEM_LSN_KEY).toString();
    }

    public String getContinuationState() {
        return continuationState;
    }

    public String getItemLsn() {
        return itemLsn;
    }

    public Map<String, Object> getTopicOffsetMap() {
        return topicOffsetMap;
    }
}
