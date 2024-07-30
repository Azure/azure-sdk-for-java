// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.FeedRange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FeedRangesMetadataTopicOffset {
    public static final String CONTAINER_FEED_RANGES_KEY = "feedRanges";
    public static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private final List<FeedRange> feedRanges;

    public FeedRangesMetadataTopicOffset(List<FeedRange> feedRanges) {
        checkNotNull(feedRanges, "Argument 'feedRanges' can not be null");

        this.feedRanges = feedRanges;
    }

    public List<FeedRange> getFeedRanges() {
        return feedRanges;
    }

    public static Map<String, Object> toMap(FeedRangesMetadataTopicOffset offset) {
        try {
            Map<String, Object> map = new HashMap<>();

            // offset can only contain primitive types
            map.put(
                CONTAINER_FEED_RANGES_KEY,
                OBJECT_MAPPER
                    .writeValueAsString(
                        offset.getFeedRanges().stream().map(range -> range.toString()).collect(Collectors.toList())));

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
            List<FeedRange> feedRanges =
                OBJECT_MAPPER
                    .readValue(feedRangesValue, new TypeReference<List<String>>() {})
                    .stream()
                    .map(rangeJson -> FeedRange.fromString(rangeJson))
                    .collect(Collectors.toList());

            return new FeedRangesMetadataTopicOffset(feedRanges);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
