package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.ClassificationPolicy;

public class ClassificationPolicyAdapter {

    public static ClassificationPolicy convertCreateOptionsToClassificationPolicy(CreateClassificationPolicyOptions createClassificationPolicyOptions) {
        return new ClassificationPolicy()
            .setName(createClassificationPolicyOptions.getName())
            .setPrioritizationRule(createClassificationPolicyOptions.getPrioritizationRule())
            .setFallbackQueueId(createClassificationPolicyOptions.getFallbackQueueId())
            .setQueueSelectors(createClassificationPolicyOptions.getQueueSelectors())
            .setWorkerSelectors(createClassificationPolicyOptions.getWorkerSelectors());
    }
}
