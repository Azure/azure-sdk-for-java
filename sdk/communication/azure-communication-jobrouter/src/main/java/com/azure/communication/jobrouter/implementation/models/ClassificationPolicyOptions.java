package com.azure.communication.jobrouter.implementation.models;

import java.util.List;

public abstract class ClassificationPolicyOptions {
    protected String id;
    protected String name;
    protected String fallbackQueueId;
    protected List<QueueSelectorAttachment> queueSelectors;
    protected RouterRule prioritizationRule;
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
