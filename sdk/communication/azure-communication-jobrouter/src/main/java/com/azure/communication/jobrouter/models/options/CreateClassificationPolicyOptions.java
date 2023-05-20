// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

import com.azure.communication.jobrouter.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.models.RouterRule;
import com.azure.communication.jobrouter.models.WorkerSelectorAttachment;
import com.azure.core.annotation.Fluent;

import java.util.List;

/** Request options for Create ClassificationPolicy.
 * ClassificationPolicy: A container for the rules that govern how jobs are classified.
 */
@Fluent
public final class CreateClassificationPolicyOptions {
    /**
     * Unique identifier of this policy.
     */
    private final String id;

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
     * Constructor for CreateClassificationPolicyOptions
     * @param id ClassificationPolicy id
     */
    public CreateClassificationPolicyOptions(String id) {
        this.id = id;
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
     * Sets ClassificationPolicy name.
     * @param name CreateClassificationPolicyOptions name
     * @return this
     */
    public CreateClassificationPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns fallback queue id.
     * @return fallbackQueueId
     */
    public String getFallbackQueueId() {
        return this.fallbackQueueId;
    }

    /**
     * Sets ClassificationPolicy fallbackQueueId.
     * @param fallbackQueueId CreateClassificationPolicyOptions fallbackQueueId
     * @return this
     */
    public CreateClassificationPolicyOptions setFallbackQueueId(String fallbackQueueId) {
        this.fallbackQueueId = fallbackQueueId;
        return this;
    }

    /**
     * Returns list of {@link QueueSelectorAttachment}s
     * @return queueSelectors
     */
    public List<QueueSelectorAttachment> getQueueSelectors() {
        return this.queueSelectors;
    }

    /**
     * Sets ClassificationPolicy queueSelectors.
     * @param queueSelectors CreateClassificationPolicyOptions list of {@link WorkerSelectorAttachment}s
     * @return this
     */
    public CreateClassificationPolicyOptions setQueueSelectors(List<QueueSelectorAttachment> queueSelectors) {
        this.queueSelectors = queueSelectors;
        return this;
    }

    /**
     * Returns prioritizationRule.
     * @return prioritizationRule
     */
    public RouterRule getPrioritizationRule() {
        return this.prioritizationRule;
    }

    /**
     * Sets ClassificationPolicy prioritizationRule.
     * @param prioritizationRule CreateClassificationPolicyOptions prioritizationRule
     * @return this
     */
    public CreateClassificationPolicyOptions setPrioritizationRule(RouterRule prioritizationRule) {
        this.prioritizationRule = prioritizationRule;
        return this;
    }

    /**
     * Returns list of {@link WorkerSelectorAttachment}s
     * @return workerSelectors
     */
    public List<WorkerSelectorAttachment> getWorkerSelectors() {
        return this.workerSelectors;
    }

    /**
     * Sets ClassificationPolicy workerSelectors.
     * @param workerSelectors CreateClassificationPolicyOptions list of {@link WorkerSelectorAttachment}s
     * @return this
     */
    public CreateClassificationPolicyOptions setWorkerSelectors(List<WorkerSelectorAttachment> workerSelectors) {
        this.workerSelectors = workerSelectors;
        return this;
    }
}
