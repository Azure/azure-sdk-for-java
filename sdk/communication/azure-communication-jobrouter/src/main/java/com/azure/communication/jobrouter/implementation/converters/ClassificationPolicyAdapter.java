// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.models.ClassificationPolicyInternal;
import com.azure.communication.jobrouter.implementation.models.RouterRuleInternal;
import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;

import java.util.stream.Collectors;

import static com.azure.communication.jobrouter.implementation.converters.RouterRuleAdapter.getRouterRuleInternal;

/**
 * Converts request options for create and update Classification Policy to {@link ClassificationPolicy}.
 */
public class ClassificationPolicyAdapter {

    /**
     * Converts {@link CreateClassificationPolicyOptions} to {@link ClassificationPolicy}.
     * @param options Container with options to create a classification policy.
     * @return classification policy.
     */
    public static ClassificationPolicyInternal convertCreateOptionsToClassificationPolicyInternal(CreateClassificationPolicyOptions options) {
        RouterRuleInternal prioritizationRuleInternal = getRouterRuleInternal(options.getPrioritizationRule());

        return new ClassificationPolicyInternal()
            .setName(options.getName())
            .setPrioritizationRule(prioritizationRuleInternal)
            .setFallbackQueueId(options.getFallbackQueueId())
            .setQueueSelectorAttachments(options.getQueueSelectors().stream()
                .map(LabelSelectorAdapter::convertQueueSelectorAttachmentToInternal).collect(Collectors.toList()))
            .setWorkerSelectorAttachments(options.getWorkerSelectors().stream()
                .map(LabelSelectorAdapter::convertWorkerSelectorAttachmentToInternal).collect(Collectors.toList()));
    }

    public static ClassificationPolicyInternal convertClassificationPolicyToClassificationPolicyInternal(ClassificationPolicy classificationPolicy) {
        return new ClassificationPolicyInternal()
            .setEtag(classificationPolicy.getEtag())
            .setId(classificationPolicy.getId())
            .setName(classificationPolicy.getName())
            .setWorkerSelectorAttachments(classificationPolicy.getWorkerSelectorAttachments().stream()
                .map(LabelSelectorAdapter::convertWorkerSelectorAttachmentToInternal).collect(Collectors.toList()))
            .setQueueSelectorAttachments(classificationPolicy.getQueueSelectorAttachments().stream()
                .map(LabelSelectorAdapter::convertQueueSelectorAttachmentToInternal).collect(Collectors.toList()))
            .setFallbackQueueId(classificationPolicy.getFallbackQueueId())
            .setPrioritizationRule(getRouterRuleInternal(classificationPolicy.getPrioritizationRule()));
    }
}
