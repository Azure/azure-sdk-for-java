// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.accesshelpers.ClassificationPolicyConstructorProxy;
import com.azure.communication.jobrouter.implementation.models.ClassificationPolicyInternal;
import com.azure.communication.jobrouter.implementation.models.ClassificationPolicyItemInternal;
import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.ClassificationPolicyItem;
import com.azure.communication.jobrouter.models.UpdateClassificationPolicyOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.ETag;
import reactor.core.publisher.Flux;

import java.util.function.Function;
import java.util.stream.Collectors;

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

    /**
     * Converts {@link UpdateClassificationPolicyOptions} to {@link ClassificationPolicy}.
     * @param options Container with options to update a distribution policy.
     * @return classification policy.
     */
    public static ClassificationPolicy convertUpdateOptionsToClassificationPolicy(UpdateClassificationPolicyOptions options) {
        return new ClassificationPolicy()
            .setName(options.getName())
            .setPrioritizationRule(options.getPrioritizationRule())
            .setFallbackQueueId(options.getFallbackQueueId())
            .setQueueSelectors(options.getQueueSelectors())
            .setWorkerSelectors(options.getWorkerSelectors());
    }
}
