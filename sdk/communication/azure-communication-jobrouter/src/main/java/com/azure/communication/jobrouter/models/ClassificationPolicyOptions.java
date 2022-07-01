// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.implementation.models.WorkerSelectorAttachment;

import java.util.List;

/**
 * Abstract class for Create and Update ClassificationPolicyOptions.
 */
public abstract class ClassificationPolicyOptions {
    /**
     * Unique identifier of this policy.
     */
    protected String id;

    /**
     * Friendly name of this policy.
     */
    protected String name;

    /**
     * The fallback queue to select if the queue selector doesn't find a match.
     */
    protected String fallbackQueueId;

    /**
     * The queue selectors to resolve a queue for a given job.
     */
    protected List<QueueSelectorAttachment> queueSelectors;

    /**
     * A rule of one of the following types:
     *
     *  StaticRule:  A rule providing static rules that always return the same result, regardless of input.
     *  DirectMapRule:  A rule that return the same labels as the input labels.
     *  ExpressionRule: A rule providing inline expression rules.
     *  AzureFunctionRule: A rule providing a binding to an HTTP Triggered Azure Function.
     */
    protected RouterRule prioritizationRule;

    /**
     * The worker label selectors to attach to a given job.
     */
    protected List<WorkerSelectorAttachment> workerSelectors;

    /**
     * Returns classification policy id.
     * @return id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns classification policy name.
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns fallback queue id.
     * @return fallbackQueueId
     */
    public String getFallbackQueueId() {
        return this.fallbackQueueId;
    }

    /**
     * Returns queueSelectors.
     * @return queueSelectors
     */
    public List<QueueSelectorAttachment> getQueueSelectors() {
        return this.queueSelectors;
    }

    /**
     * Returns prioritizationRule.
     * @return prioritizationRule
     */
    public RouterRule getPrioritizationRule() {
        return this.prioritizationRule;
    }

    /**
     * Returns workerSelectors.
     * @return workerSelectors
     */
    public List<WorkerSelectorAttachment> getWorkerSelectors() {
        return this.workerSelectors;
    }
}
