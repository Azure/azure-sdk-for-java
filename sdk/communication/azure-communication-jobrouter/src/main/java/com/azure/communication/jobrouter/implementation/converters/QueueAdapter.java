// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.RouterQueue;

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
        return new RouterQueue()
            .setName(createQueueOptions.getName())
            .setLabels(createQueueOptions.getLabels())
            .setDistributionPolicyId(createQueueOptions.getDistributionPolicyId())
            .setExceptionPolicyId(createQueueOptions.getExceptionPolicyId());
    }
}
