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
     * @param queueId
     * @return this
     */
    public UpdateQueueOptions setQueueId(String queueId) {
        this.queueId = queueId;
        return this;
    }

    /**
     * Sets distributionPolicyId
     * @param distributionPolicyId
     * @return this
     */
    public UpdateQueueOptions setDistributionPolicyId(String distributionPolicyId) {
        this.distributionPolicyId = distributionPolicyId;
        return this;
    }

    /**
     * Sets name
     * @param name
     * @return this
     */
    public UpdateQueueOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets labels
     * @param labels
     * @return this
     */
    public UpdateQueueOptions setLabels(Map<String, Object> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Sets exceptionPolicyId
     * @param exceptionPolicyId
     * @return this
     */
    public UpdateQueueOptions setExceptionPolicyId(String exceptionPolicyId) {
        this.exceptionPolicyId = exceptionPolicyId;
        return this;
    }
}
