// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.QueueSelectorAttachment;
import com.azure.core.annotation.Fluent;

import java.util.List;

/** Request options for Update ClassificationPolicy.
 * ClassificationPolicy: A container for the rules that govern how jobs are classified.
 */
@Fluent
public class UpdateClassificationPolicyOptions {
    /**
     * Unique identifier of this policy.
     */
    private String id;

    /**
     * Friendly name of this policy.
     */
    private String name;

    /**
     * The fallback queue to select if the queue selector doesn't find a match.
     */
    private String fallbackQueueId;

    /**
     * The queue selectors to resolve a queue for a given job.
     */
    private List<QueueSelectorAttachment> queueSelectors;

    /**
     * A rule of one of the following types:
     *
     *  StaticRule:  A rule providing static rules that always return the same result, regardless of input.
     *  DirectMapRule:  A rule that return the same labels as the input labels.
     *  ExpressionRule: A rule providing inline expression rules.
     *  AzureFunctionRule: A rule providing a binding to an HTTP Triggered Azure Function.
     */
    private RouterRule prioritizationRule;

    /**
     * The worker label selectors to attach to a given job.
     */
    private List<WorkerSelectorAttachment> workerSelectors;

    /**
     * Constructor for UpdateClassificationPolicyOptions
     * @param id Unique identifier of this policy.
     */
    public UpdateClassificationPolicyOptions(String id) {
        this.id = id;
    }

    /**
     * Sets name.
     * @param name Friendly name of this policy.
     * @return this
     */
    public UpdateClassificationPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets fallbackQueueId.
     * @param fallbackQueueId The fallback queue to select if the queue selector doesn't find a match.
     * @return this
     */
    public UpdateClassificationPolicyOptions setFallbackQueueId(String fallbackQueueId) {
        this.fallbackQueueId = fallbackQueueId;
        return this;
    }

    /**
     * Sets queueSelectors.
     * @param queueSelectors The queue selectors to resolve a queue for a given job.
     * @return this
     */
    public UpdateClassificationPolicyOptions setQueueSelectors(List<QueueSelectorAttachment> queueSelectors) {
        this.queueSelectors = queueSelectors;
        return this;
    }

    /**
     * Sets prioritizationRule.
     * @param prioritizationRule A rule of one of the following types:
     *   StaticRule:  A rule providing static rules that always return the same result, regardless of input.
     *   DirectMapRule:  A rule that return the same labels as the input labels.
     *   ExpressionRule: A rule providing inline expression rules.
     *   AzureFunctionRule: A rule providing a binding to an HTTP Triggered Azure Function.
     * @return this
     */
    public UpdateClassificationPolicyOptions setPrioritizationRule(RouterRule prioritizationRule) {
        this.prioritizationRule = prioritizationRule;
        return this;
    }

    /**
     * Sets workerSelectors.
     * @param workerSelectors The worker label selectors to attach to a given job.
     * @return this
     */
    public UpdateClassificationPolicyOptions setWorkerSelectors(List<WorkerSelectorAttachment> workerSelectors) {
        this.workerSelectors = workerSelectors;
        return this;
    }

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
