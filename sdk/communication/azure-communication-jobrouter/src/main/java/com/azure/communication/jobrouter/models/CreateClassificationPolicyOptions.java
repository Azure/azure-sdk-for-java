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

    public CreateClassificationPolicyOptions(String id, String name, RouterRule prioritizationRule, List<WorkerSelectorAttachment> workerSelectors,
                                             List<QueueSelectorAttachment> queueSelectors, String fallbackQueueId) {
        this.id = id;
        this.name = name;
        this.prioritizationRule = prioritizationRule;
        this.workerSelectors = workerSelectors;
        this.queueSelectors = queueSelectors;
        this.fallbackQueueId = fallbackQueueId;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getFallbackQueueId() {
        return this.fallbackQueueId;
    }

    public List<QueueSelectorAttachment> getQueueSelectors() {
        return this.queueSelectors;
    }

    public RouterRule getPrioritizationRule() {
        return this.prioritizationRule;
    }

    public List<WorkerSelectorAttachment> getWorkerSelectors() {
        return this.workerSelectors;
    }
}
