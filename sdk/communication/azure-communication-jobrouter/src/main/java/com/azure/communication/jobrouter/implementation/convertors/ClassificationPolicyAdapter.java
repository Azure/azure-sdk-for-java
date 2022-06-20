// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.convertors;

import com.azure.communication.jobrouter.implementation.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;

/**
 * Converts request options for create and update Classification Policy to {@link ClassificationPolicy}.
 */
public class ClassificationPolicyAdapter {

    /**
     * Converts {@link CreateClassificationPolicyOptions} to {@link ClassificationPolicy}.
     * @param createClassificationPolicyOptions
     * @return classification policy.
     */
    public static ClassificationPolicy convertCreateOptionsToClassificationPolicy(CreateClassificationPolicyOptions createClassificationPolicyOptions) {
        return new ClassificationPolicy()
            .setName(createClassificationPolicyOptions.getName())
            .setPrioritizationRule(createClassificationPolicyOptions.getPrioritizationRule())
            .setFallbackQueueId(createClassificationPolicyOptions.getFallbackQueueId())
            .setQueueSelectors(createClassificationPolicyOptions.getQueueSelectors())
            .setWorkerSelectors(createClassificationPolicyOptions.getWorkerSelectors());
    }
}
