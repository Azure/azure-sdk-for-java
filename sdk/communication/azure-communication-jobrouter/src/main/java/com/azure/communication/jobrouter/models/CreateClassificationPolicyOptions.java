// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.QueueSelectorAttachment;
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
    private final String name;

    /**
     * The fallback queue to select if the queue selector doesn't find a match.
     */
    private final String fallbackQueueId;

    /**
     * The queue selectors to resolve a queue for a given job.
     */
    private final List<QueueSelectorAttachment> queueSelectors;

    /**
     * A rule of one of the following types:
     *
     *  StaticRule:  A rule providing static rules that always return the same result, regardless of input.
     *  DirectMapRule:  A rule that return the same labels as the input labels.
     *  ExpressionRule: A rule providing inline expression rules.
     *  AzureFunctionRule: A rule providing a binding to an HTTP Triggered Azure Function.
     */
    private final RouterRule prioritizationRule;

    /**
     * The worker label selectors to attach to a given job.
     */
    private final List<WorkerSelectorAttachment> workerSelectors;


    /**
     * Constructor for CreateClassificationPolicyOptions
     * @param id ClassificationPolicy id
     * @param name ClassificationPolicy name
     * @param prioritizationRule One of {@link RouterRule}s
     * @param workerSelectors List of {@link WorkerSelectorAttachment}s
     * @param queueSelectors List of {@link QueueSelectorAttachment}s
     * @param fallbackQueueId fallback queueId if queue selectors don't work.
     */
    public CreateClassificationPolicyOptions(String id, String name, RouterRule prioritizationRule, List<WorkerSelectorAttachment> workerSelectors,
                                             List<QueueSelectorAttachment> queueSelectors, String fallbackQueueId) {
        this.id = id;
        this.name = name;
        this.prioritizationRule = prioritizationRule;
        this.workerSelectors = workerSelectors;
        this.queueSelectors = queueSelectors;
        this.fallbackQueueId = fallbackQueueId;
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
