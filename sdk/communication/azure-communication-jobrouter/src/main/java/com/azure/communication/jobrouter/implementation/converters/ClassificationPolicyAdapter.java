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
    public static ClassificationPolicyInternal convertCreateOptionsToClassificationPolicy(CreateClassificationPolicyOptions options) {
        return new ClassificationPolicyInternal()
            .setName(options.getName())
            .setPrioritizationRule(RouterRuleAdapter.convertRouterRuleToInternal(options.getPrioritizationRule()))
            .setFallbackQueueId(options.getFallbackQueueId())
            .setQueueSelectors(options.getQueueSelectors().stream()
                .map(LabelSelectorAdapter::convertQueueSelectorAttachmentToInternal).collect(Collectors.toList()))
            .setWorkerSelectors(options.getWorkerSelectors().stream()
                .map(LabelSelectorAdapter::convertWorkerSelectorAttachmentToInternal).collect(Collectors.toList()));
    }

    /**
     * Converts {@link UpdateClassificationPolicyOptions} to {@link ClassificationPolicy}.
     * @param options Container with options to update a distribution policy.
     * @return classification policy.
     */
    public static ClassificationPolicyInternal convertUpdateOptionsToClassificationPolicy(UpdateClassificationPolicyOptions options) {
        return new ClassificationPolicyInternal()
            .setName(options.getName())
            .setPrioritizationRule(RouterRuleAdapter.convertRouterRuleToInternal(options.getPrioritizationRule()))
            .setFallbackQueueId(options.getFallbackQueueId())
            .setQueueSelectors(options.getQueueSelectors().stream()
                .map(LabelSelectorAdapter::convertQueueSelectorAttachmentToInternal).collect(Collectors.toList()))
            .setWorkerSelectors(options.getWorkerSelectors().stream()
                .map(LabelSelectorAdapter::convertWorkerSelectorAttachmentToInternal).collect(Collectors.toList()));
    }

    public static PagedFlux<ClassificationPolicyItem> convertPagedFluxToPublic(PagedFlux<ClassificationPolicyItemInternal> internalPagedFlux) {
        final Function<PagedResponse<ClassificationPolicyItemInternal>, PagedResponse<ClassificationPolicyItem>> responseMapper
            = internalResponse -> new PagedResponseBase<Void, ClassificationPolicyItem>(internalResponse.getRequest(),
            internalResponse.getStatusCode(),
            internalResponse.getHeaders(),
            internalResponse.getValue()
                .stream()
                .map(internal -> new ClassificationPolicyItem()
                    .setClassificationPolicy(ClassificationPolicyConstructorProxy.create(internal.getClassificationPolicy()))
                    .setEtag(new ETag(internal.getEtag())))
                .collect(Collectors.toList()),
            internalResponse.getContinuationToken(),
            null);

        return PagedFlux.create(() -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<ClassificationPolicyItemInternal>> flux = (continuationToken == null)
                ? internalPagedFlux.byPage()
                : internalPagedFlux.byPage(continuationToken);
            return flux.map(responseMapper);
        });
    }
}
