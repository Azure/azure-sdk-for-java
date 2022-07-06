// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.convertors;

import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.UpdateJobOptions;
import com.azure.communication.jobrouter.models.UpdateWorkerOptions;

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
        Map<String, Object> labels = labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        return new RouterWorker()
            .setLabels(labels)
            .setTags(createWorkerOptions.getTags())
            .setQueueAssignments(createWorkerOptions.getQueueAssignments())
            .setAvailableForOffers(createWorkerOptions.getAvailableForOffers())
            .setChannelConfigurations(createWorkerOptions.getChannelConfigurations())
            .setTotalCapacity(createWorkerOptions.getTotalCapacity());
    }

    /**
     * Converts {@link UpdateJobOptions} to {@link RouterWorker}.
     * @param updateWorkerOptions Container with options to update {@link RouterWorker}
     * @return RouterWorker
     */
    public static RouterWorker convertUpdateWorkerOptionsToRouterWorker(UpdateWorkerOptions updateWorkerOptions) {
        Map<String, LabelValue> labelValueMap = updateWorkerOptions.getLabels();
        Map<String, Object> labels = labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        return new RouterWorker()
            .setLabels(labels)
            .setTags(updateWorkerOptions.getTags())
            .setQueueAssignments(updateWorkerOptions.getQueueAssignments())
            .setAvailableForOffers(updateWorkerOptions.getAvailableForOffers())
            .setChannelConfigurations(updateWorkerOptions.getChannelConfigurations())
            .setTotalCapacity(updateWorkerOptions.getTotalCapacity());
    }
}
