// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;

/**
 * Converts request options for create and update Classification Policy to {@link ClassificationPolicy}.
 */
public class ClassificationPolicyAdapter {

    /**
     * Converts {@link CreateClassificationPolicyOptions} to {@link ClassificationPolicy}.
     * @param options Container with options to create a classification policy.
     * @return classification policy.
     */
    public static ClassificationPolicy convertCreateOptionsToClassificationPolicy(CreateClassificationPolicyOptions options) {
        return new ClassificationPolicy()
            .setName(options.getName())
            .setPrioritizationRule(options.getPrioritizationRule())
            .setFallbackQueueId(options.getFallbackQueueId())
            .setQueueSelectors(options.getQueueSelectors())
            .setWorkerSelectors(options.getWorkerSelectors());
    }
}
