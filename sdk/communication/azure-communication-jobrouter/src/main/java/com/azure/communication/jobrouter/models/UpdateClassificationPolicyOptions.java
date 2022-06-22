package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.ClassificationPolicyOptions;
import com.azure.communication.jobrouter.implementation.models.QueueSelectorAttachment;
import com.azure.communication.jobrouter.implementation.models.RouterRule;
import com.azure.communication.jobrouter.implementation.models.WorkerSelectorAttachment;
import com.azure.core.annotation.Fluent;

import java.util.List;

/** Request options for Update ClassificationPolicy. */
@Fluent
public class UpdateClassificationPolicyOptions extends ClassificationPolicyOptions {
    public UpdateClassificationPolicyOptions(String id) {
        this.id = id;
    }

    public UpdateClassificationPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }

    public UpdateClassificationPolicyOptions setFallbackQueueId(String fallbackQueueId) {
        this.fallbackQueueId = fallbackQueueId;
        return this;
    }

    public UpdateClassificationPolicyOptions setQueueSelectors(List<QueueSelectorAttachment> queueSelectors) {
        this.queueSelectors = queueSelectors;
        return this;
    }

    public UpdateClassificationPolicyOptions setPrioritizationRule(RouterRule prioritizationRule) {
        this.prioritizationRule = prioritizationRule;
        return this;
    }

    public UpdateClassificationPolicyOptions setWorkerSelectors(List<WorkerSelectorAttachment> workerSelectors) {
        this.workerSelectors = workerSelectors;
        return this;
    }
}
