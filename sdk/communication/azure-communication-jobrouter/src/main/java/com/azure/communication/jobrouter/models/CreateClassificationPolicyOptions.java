// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.implementation.models.RouterRule;
import com.azure.communication.jobrouter.implementation.models.WorkerSelectorAttachment;
import com.azure.core.annotation.Fluent;

import java.util.List;

/** Request options for Create ClassificationPolicy. */
@Fluent
public final class CreateClassificationPolicyOptions {
    private String id;
    private String name;

    private String fallbackQueueId;

    private List<QueueSelectorAttachment> queueSelectors;

    private RouterRule prioritizationRule;

    private List<WorkerSelectorAttachment> workerSelectors;

    /**
     * Constructor for CreateClassificationPolicyOptions
     * @param id ClassificationPolicy id
     * @param name ClassificationPolicy name
     * @param prioritizationRule One of {@link com.azure.communication.jobrouter.implementation.models.RouterRule}s
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
