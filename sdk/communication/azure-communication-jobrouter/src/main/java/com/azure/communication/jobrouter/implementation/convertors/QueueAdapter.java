// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.convertors;

import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.UpdateQueueOptions;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts request options for create and update Queue to {@link JobQueue}.
 */
public class QueueAdapter {

    /**
     * Converts {@link CreateQueueOptions} to {@link JobQueue}.
     * @param createQueueOptions Container with options to create {@link JobQueue}
     * @return JobQueue
     */
    public static JobQueue convertCreateQueueOptionsToJobQueue(CreateQueueOptions createQueueOptions) {
        Map<String, LabelValue> labelValueMap = createQueueOptions.getLabels();
        Map<String, Object> labels = labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        return new JobQueue()
            .setName(createQueueOptions.getName())
            .setLabels(labels)
            .setDistributionPolicyId(createQueueOptions.getDistributionPolicyId())
            .setExceptionPolicyId(createQueueOptions.getExceptionPolicyId());
    }

    /**
     * Converts {@link UpdateQueueOptions} to {@link JobQueue}.
     * @param updateQueueOptions Container with options to update {@link JobQueue}
     * @return JobQueue
     */
    public static JobQueue convertUpdateQueueOptionsToJobQueue(UpdateQueueOptions updateQueueOptions) {
        Map<String, LabelValue> labelValueMap = updateQueueOptions.getLabels();
        Map<String, Object> labels = labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        return new JobQueue()
            .setName(updateQueueOptions.getName())
            .setLabels(labels)
            .setDistributionPolicyId(updateQueueOptions.getDistributionPolicyId())
            .setExceptionPolicyId(updateQueueOptions.getExceptionPolicyId());
    }
}
