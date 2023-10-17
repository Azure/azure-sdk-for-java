// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.RouterQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts request options for create and update Queue to {@link RouterQueue}.
 */
public class QueueAdapter {

    /**
     * Converts {@link CreateQueueOptions} to {@link RouterQueue}.
     * @param createQueueOptions Container with options to create {@link RouterQueue}
     * @return JobQueue
     */
    public static RouterQueue convertCreateQueueOptionsToRouterQueue(CreateQueueOptions createQueueOptions) {
        Map<String, LabelValue> labelValueMap = createQueueOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : new HashMap<>();

        return new RouterQueue()
            .setName(createQueueOptions.getName())
            .setLabels(labels)
            .setDistributionPolicyId(createQueueOptions.getDistributionPolicyId())
            .setExceptionPolicyId(createQueueOptions.getExceptionPolicyId());
    }
}
