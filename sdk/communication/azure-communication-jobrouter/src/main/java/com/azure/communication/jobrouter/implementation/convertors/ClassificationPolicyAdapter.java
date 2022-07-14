// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.convertors;

import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateClassificationPolicyOptions;

/**
 * Converts request options for create and update Classification Policy to {@link ClassificationPolicy}.
 */
public class ClassificationPolicyAdapter {

    /**
     * Converts {@link CreateClassificationPolicyOptions} to {@link ClassificationPolicy}.
     * @param createClassificationPolicyOptions Container with options to create a classification policy.
     * @return classification policy.
     */
    public static ClassificationPolicy convertCreateOptionsToClassificationPolicy(CreateClassificationPolicyOptions createClassificationPolicyOptions) {
        return new ClassificationPolicy()
            .setName(createClassificationPolicyOptions.name())
            .setPrioritizationRule(createClassificationPolicyOptions.prioritizationRule())
            .setFallbackQueueId(createClassificationPolicyOptions.fallbackQueueId())
            .setQueueSelectors(createClassificationPolicyOptions.queueSelectors())
            .setWorkerSelectors(createClassificationPolicyOptions.workerSelectors());
    }

    /**
     * Converts {@link UpdateClassificationPolicyOptions} to {@link ClassificationPolicy}.
     * @param updateClassificationPolicyOptions Container with options to update a distribution policy.
     * @return classification policy.
     */
    public static ClassificationPolicy convertUpdateOptionsToClassificationPolicy(UpdateClassificationPolicyOptions updateClassificationPolicyOptions) {
        return new ClassificationPolicy()
            .setName(updateClassificationPolicyOptions.getName())
            .setFallbackQueueId(updateClassificationPolicyOptions.getFallbackQueueId())
            .setQueueSelectors(updateClassificationPolicyOptions.getQueueSelectors())
            .setPrioritizationRule(updateClassificationPolicyOptions.getPrioritizationRule())
            .setWorkerSelectors(updateClassificationPolicyOptions.getWorkerSelectors());
    }
}
