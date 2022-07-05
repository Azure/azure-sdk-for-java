// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Request options to create a queue.
 * Queue: A queue that can contain jobs to be routed.
 */
@Fluent
public class CreateQueueOptions extends QueueOptions {

    /**
     * Constructor for CreateQueueOptions.
     * @param queueId The id of queue.
     * @param distributionPolicyId The id of distribution policy.
     */
    public CreateQueueOptions(String queueId, String distributionPolicyId) {
        this.queueId = queueId;
        this.distributionPolicyId = distributionPolicyId;
    }

    /**
     * Sets name
     * @param name
     * @return this
     */
    public CreateQueueOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets labels
     * @param labels
     * @return this
     */
    public CreateQueueOptions setLabels(Map<String, Object> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Sets exceptionPolicyId
     * @param exceptionPolicyId
     * @return this
     */
    public CreateQueueOptions setExceptionPolicyId(String exceptionPolicyId) {
        this.exceptionPolicyId = exceptionPolicyId;
        return this;
    }
}
