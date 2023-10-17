// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.RouterQueueAssignment;
import com.azure.communication.jobrouter.models.RouterWorker;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts request options for create and update Worker to {@link RouterWorker}.
 */
public class WorkerAdapter {
    /**
     * Converts {@link CreateWorkerOptions} to {@link RouterWorker}.
     * @param createWorkerOptions Container with options to create {@link RouterWorker}
     * @return RouterWorker
     */
    public static RouterWorker convertCreateWorkerOptionsToRouterWorker(CreateWorkerOptions createWorkerOptions) {
        Map<String, LabelValue> labelValueMap = createWorkerOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : new HashMap<>();
        Map<String, LabelValue> tagValueMap = createWorkerOptions.getLabels();
        Map<String, Object> tags = tagValueMap != null ? tagValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : new HashMap<>();
        Map<String, RouterQueueAssignment> queueAssignments = createWorkerOptions.getQueueAssignments();

        return new RouterWorker()
            .setLabels(labels)
            .setTags(tags)
            .setQueueAssignments(queueAssignments)
            .setAvailableForOffers(createWorkerOptions.isAvailableForOffers())
            .setChannelConfigurations(createWorkerOptions.getChannelConfigurations())
            .setTotalCapacity(createWorkerOptions.getTotalCapacity());
    }
}
