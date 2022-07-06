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
public final class CreateClassificationPolicyOptions extends ClassificationPolicyOptions {
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
}
