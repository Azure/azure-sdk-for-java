// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Containers metadata topic offset.
 */
public class ContainersMetadataTopicOffset {
    public static final String CONTAINERS_RESOURCE_IDS_NAME_KEY = "containerRids";
    public static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private final List<String> containerRids;
    public ContainersMetadataTopicOffset(List<String> containerRids) {
        checkNotNull(containerRids, "Argument 'containerRids' can not be null");
        this.containerRids = containerRids;
    }

    public List<String> getContainerRids() {
        return containerRids;
    }

    public static Map<String, Object> toMap(ContainersMetadataTopicOffset offset) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put(
                CONTAINERS_RESOURCE_IDS_NAME_KEY,
                OBJECT_MAPPER.writeValueAsString(offset.getContainerRids()));
            return map;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static ContainersMetadataTopicOffset fromMap(Map<String, Object> offsetMap) {
        if (offsetMap == null) {
            return null;
        }

        try {
            List<String> containerRids =
                OBJECT_MAPPER
                    .readValue(offsetMap.get(CONTAINERS_RESOURCE_IDS_NAME_KEY).toString(), new TypeReference<List<String>>() {});
            return new ContainersMetadataTopicOffset(containerRids);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
