// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Request options to update a queue.
 * Queue: A queue that can contain jobs to be routed.
 */
@Fluent
public class UpdateQueueOptions extends QueueOptions {
    /**
     * Sets queueId.
     * @param queueId The Id of this queue
     * @return this
     */
    public UpdateQueueOptions setQueueId(String queueId) {
        this.queueId = queueId;
        return this;
    }

    /**
     * Sets distributionPolicyId.
     * @param distributionPolicyId The ID of the distribution policy that will determine how a job is distributed to workers.
     * @return this
     */
    public UpdateQueueOptions setDistributionPolicyId(String distributionPolicyId) {
        this.distributionPolicyId = distributionPolicyId;
        return this;
    }

    /**
     * Sets name.
     * @param name The name of this queue.
     * @return this
     */
    public UpdateQueueOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets labels.
     * @param labels A set of key/value pairs that are identifying attributes used by the rules engines to make decisions.
     * @return this
     */
    public UpdateQueueOptions setLabels(Map<String, Object> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Sets exceptionPolicyId.
     * @param exceptionPolicyId (Optional) The ID of the exception policy that determines various job escalation rules.
     * @return this
     */
    public UpdateQueueOptions setExceptionPolicyId(String exceptionPolicyId) {
        this.exceptionPolicyId = exceptionPolicyId;
        return this;
    }
}
