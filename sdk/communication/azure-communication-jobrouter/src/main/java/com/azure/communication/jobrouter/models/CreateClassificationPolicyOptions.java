// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.QueueSelectorAttachment;
import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Request options for Create ClassificationPolicy.
 * ClassificationPolicy: A container for the rules that govern how jobs are classified.
 *
 * @param id Unique identifier of this policy.
 * @param name Friendly name of this policy.
 * @param fallbackQueueId The fallback queue to select if the queue selector doesn't find a match.
 * @param queueSelectors The queue selectors to resolve a queue for a given job.
 * @param prioritizationRule A rule of one of the following types:
 * <p>
 * StaticRule:  A rule providing static rules that always return the same result, regardless of input.
 * DirectMapRule:  A rule that return the same labels as the input labels.
 * ExpressionRule: A rule providing inline expression rules.
 * AzureFunctionRule: A rule providing a binding to an HTTP Triggered Azure Function.
 * @param workerSelectors The worker label selectors to attach to a given job.
 */
@Fluent
public record CreateClassificationPolicyOptions(String id, String name, RouterRule prioritizationRule,
                                                List<WorkerSelectorAttachment> workerSelectors,
                                                List<QueueSelectorAttachment> queueSelectors, String fallbackQueueId) {
    /**
     * Constructor for CreateClassificationPolicyOptions
     *
     * @param id ClassificationPolicy id
     * @param name ClassificationPolicy name
     * @param prioritizationRule One of {@link RouterRule}s
     * @param workerSelectors List of {@link WorkerSelectorAttachment}s
     * @param queueSelectors List of {@link QueueSelectorAttachment}s
     * @param fallbackQueueId fallback queueId if queue selectors don't work.
     */
    public CreateClassificationPolicyOptions {
    }

    /**
     * Returns classification policy id.
     *
     * @return id
     */
    @Override
    public String id() {
        return this.id;
    }

    /**
     * Returns classification policy name.
     *
     * @return name
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * Returns fallback queue id.
     *
     * @return fallbackQueueId
     */
    @Override
    public String fallbackQueueId() {
        return this.fallbackQueueId;
    }

    /**
     * Returns queueSelectors.
     *
     * @return queueSelectors
     */
    @Override
    public List<QueueSelectorAttachment> queueSelectors() {
        return this.queueSelectors;
    }

    /**
     * Returns prioritizationRule.
     *
     * @return prioritizationRule
     */
    @Override
    public RouterRule prioritizationRule() {
        return this.prioritizationRule;
    }

    /**
     * Returns workerSelectors.
     *
     * @return workerSelectors
     */
    @Override
    public List<WorkerSelectorAttachment> workerSelectors() {
        return this.workerSelectors;
    }
}
