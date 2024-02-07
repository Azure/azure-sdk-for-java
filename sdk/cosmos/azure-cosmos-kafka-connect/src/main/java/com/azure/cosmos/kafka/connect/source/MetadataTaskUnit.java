// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source;

import com.azure.cosmos.implementation.routing.Range;

import java.util.List;
import java.util.Map;

public class MetadataTaskUnit implements ITaskUnit {
    private final String databaseName;
    private final List<String> containerRids;
    private final Map<String, List<Range<String>>> containersEffectiveRangesMap;
    private final String topic;

    public MetadataTaskUnit(
        String databaseName,
        List<String> containerRids,
        Map<String, List<Range<String>>> containersEffectiveRangesMap,
        String topic) {
        this.databaseName = databaseName;
        this.containerRids = containerRids;
        this.containersEffectiveRangesMap = containersEffectiveRangesMap;
        this.topic = topic;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public List<String> getContainerRids() {
        return containerRids;
    }

    public Map<String, List<Range<String>>> getContainersEffectiveRangesMap() {
        return containersEffectiveRangesMap;
    }

    public String getTopic() {
        return topic;
    }
}
