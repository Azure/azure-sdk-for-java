// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.convertors;

import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.UpdateQueueOptions;

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
        return new JobQueue()
            .setName(createQueueOptions.getName())
            .setLabels(createQueueOptions.getLabels())
            .setDistributionPolicyId(createQueueOptions.getDistributionPolicyId())
            .setExceptionPolicyId(createQueueOptions.getExceptionPolicyId());
    }

    /**
     * Converts {@link UpdateQueueOptions} to {@link JobQueue}.
     * @param updateQueueOptions Container with options to update {@link JobQueue}
     * @return JobQueue
     */
    public static JobQueue convertUpdateQueueOptionsToJobQueue(UpdateQueueOptions updateQueueOptions) {
        return new JobQueue()
            .setName(updateQueueOptions.getName())
            .setLabels(updateQueueOptions.getLabels())
            .setDistributionPolicyId(updateQueueOptions.getDistributionPolicyId())
            .setExceptionPolicyId(updateQueueOptions.getExceptionPolicyId());
    }
}
