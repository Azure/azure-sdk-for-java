// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source;

import com.azure.cosmos.implementation.routing.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedRangesMetadataTopicOffset {
    public static final String CONTAINER_FEED_RANGES_KEY = "cosmos.source.container.feedRanges";

    private final List<Range<String>> feedRanges;
    private final Map<String, Object> topicOffsetMap;

    public FeedRangesMetadataTopicOffset(Map<String, Object> topicOffsetMap) {
        this.topicOffsetMap = topicOffsetMap;
        this.feedRanges = topicOffsetMap.get(CONTAINER_FEED_RANGES_KEY) == null ?
            null : (List<Range<String>>) topicOffsetMap.get(CONTAINER_FEED_RANGES_KEY);
    }

    public FeedRangesMetadataTopicOffset(List<Range<String>> normalizedRanges) {
        this.feedRanges = normalizedRanges;
        this.topicOffsetMap = new HashMap<>();
        this.topicOffsetMap.put(CONTAINER_FEED_RANGES_KEY, normalizedRanges);
    }

    public List<Range<String>> getFeedRanges() {
        return feedRanges;
    }

    public Map<String, Object> getTopicOffsetMap() {
        return topicOffsetMap;
    }
}
