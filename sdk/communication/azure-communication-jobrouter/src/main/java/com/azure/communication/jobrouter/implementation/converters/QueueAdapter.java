// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.models.RouterQueueInternal;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.RouterQueue;

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
    public static RouterQueueInternal convertCreateQueueOptionsToRouterQueueInternal(CreateQueueOptions createQueueOptions) {
        Map<String, Object> labels = createQueueOptions.getLabels() != null ? createQueueOptions.getLabels().entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> entry.getKey(),
                entry -> RouterValueAdapter.getValue(entry.getValue()))) : null;

        return new RouterQueueInternal()
            .setName(createQueueOptions.getName())
            .setLabels(labels)
            .setDistributionPolicyId(createQueueOptions.getDistributionPolicyId())
            .setExceptionPolicyId(createQueueOptions.getExceptionPolicyId());
    }

    public static RouterQueueInternal convertRouterQueueToRouterQueueInternal(RouterQueue routerQueue) {
        Map<String, Object> labels = routerQueue.getLabels() != null ? routerQueue.getLabels()
            .entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> RouterValueAdapter.getValue(entry.getValue()))) : null;
        return new RouterQueueInternal()
            .setEtag(routerQueue.getEtag())
            .setId(routerQueue.getId())
            .setName(routerQueue.getName())
            .setLabels(labels)
            .setExceptionPolicyId(routerQueue.getExceptionPolicyId())
            .setDistributionPolicyId(routerQueue.getDistributionPolicyId());
    }
}
