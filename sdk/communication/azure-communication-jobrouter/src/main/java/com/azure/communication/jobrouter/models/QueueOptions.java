// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import java.util.Map;

/**
 * Abstract class for Create and Update QueueOptions.
 */
public abstract class QueueOptions {
    /**
     * The Id of this queue
     */
    protected String queueId;

    /**
     * The name of this queue.
     */
    protected String name;

    /**
     * The ID of the distribution policy that will determine how a job is
     * distributed to workers.
     */
    protected String distributionPolicyId;

    /**
     * A set of key/value pairs that are identifying attributes used by the
     * rules engines to make decisions.
     */
    protected Map<String, Object> labels;

    /**
     * (Optional) The ID of the exception policy that determines various job
     * escalation rules.
     */
    protected String exceptionPolicyId;

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
    public Map<String, Object> getLabels() {
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
