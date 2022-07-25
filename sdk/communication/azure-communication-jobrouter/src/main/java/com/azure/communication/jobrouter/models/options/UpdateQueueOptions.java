// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Request options to update a queue.
 * Queue: A queue that can contain jobs to be routed.
 */
@Fluent
public class UpdateQueueOptions {
    /**
     * The Id of this queue
     */
    private String queueId;

    /**
     * The name of this queue.
     */
    private String name;

    /**
     * The ID of the distribution policy that will determine how a job is
     * distributed to workers.
     */
    private String distributionPolicyId;

    /**
     * A set of key/value pairs that are identifying attributes used by the
     * rules engines to make decisions.
     */
    private Map<String, LabelValue> labels;

    /**
     * (Optional) The ID of the exception policy that determines various job
     * escalation rules.
     */
    private String exceptionPolicyId;

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
    public UpdateQueueOptions setLabels(Map<String, LabelValue> labels) {
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

    /**
     * Returns the id of Job Queue.
     * @return id.
     */
    public String getQueueId() {
        return this.queueId;
    }

    /**
     * Returns the name of this queue.
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the id of distribution policy.
     * @return id
     */
    public String getDistributionPolicyId() {
        return this.distributionPolicyId;
    }

    /**
     * Returns labels of Queue.
     * @return labels
     */
    public Map<String, LabelValue> getLabels() {
        return this.labels;
    }

    /**
     * Returns the id of exception policy.
     * @return exceptionPolicyId
     */
    public String getExceptionPolicyId() {
        return this.exceptionPolicyId;
    }
}
