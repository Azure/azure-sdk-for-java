// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainersMetadataTopicOffset {
    public static final String CONTAINERS_RESOURCE_IDS_NAME_KEY = "cosmos.source.containers.resourceIds";

    private final List<String> containerRids;
    private final Map<String, Object> topicOffsetMap;
    public ContainersMetadataTopicOffset(List<String> containerRids) {
        this.containerRids = containerRids;
        this.topicOffsetMap = new HashMap<>();
        this.topicOffsetMap.put(CONTAINERS_RESOURCE_IDS_NAME_KEY, containerRids);
    }

    public ContainersMetadataTopicOffset(Map<String, Object> topicOffsetMap) {
        this.topicOffsetMap = topicOffsetMap;
        this.containerRids = (List<String>) topicOffsetMap.get(CONTAINERS_RESOURCE_IDS_NAME_KEY);
    }

    public List<String> getContainerRids() {
        return containerRids;
    }

    public Map<String, Object> getTopicOffsetMap() {
        return topicOffsetMap;
    }
}
