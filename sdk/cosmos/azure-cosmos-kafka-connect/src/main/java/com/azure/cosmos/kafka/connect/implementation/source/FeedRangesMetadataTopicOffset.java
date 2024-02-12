// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.Range;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FeedRangesMetadataTopicOffset {
    public static final String CONTAINER_FEED_RANGES_KEY = "cosmos.source.metadata.container.feedRanges";
    public static final ObjectMapper objectMapper = Utils.getSimpleObjectMapper();

    private final List<Range<String>> feedRanges;

    public FeedRangesMetadataTopicOffset(List<Range<String>> feedRanges) {
        this.feedRanges = feedRanges;
    }

    public List<Range<String>> getFeedRanges() {
        return feedRanges;
    }

    public static Map<String, Object> toMap(FeedRangesMetadataTopicOffset offset) {
        try {
            Map<String, Object> map = new HashMap<>();

            // offset can only contain primitive types
            map.put(
                CONTAINER_FEED_RANGES_KEY,
                objectMapper
                    .writeValueAsString(
                        offset.getFeedRanges().stream().map(range -> range.toJson()).collect(Collectors.toList())));

            return map;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static FeedRangesMetadataTopicOffset fromMap(Map<String, Object> offsetMap) {
        if (offsetMap == null) {
            return null;
        }

        String feedRangesValue = offsetMap.get(CONTAINER_FEED_RANGES_KEY).toString();
        try {
            List<Range<String>> feedRanges =
                objectMapper
                    .readValue(feedRangesValue, new TypeReference<List<String>>() {})
                    .stream()
                    .map(rangeJson -> new Range<String>(rangeJson))
                    .collect(Collectors.toList());

            return new FeedRangesMetadataTopicOffset(feedRanges);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
