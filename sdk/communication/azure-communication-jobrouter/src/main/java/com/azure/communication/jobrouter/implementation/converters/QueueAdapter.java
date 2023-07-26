// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.accesshelpers.RouterQueueConstructorProxy;
import com.azure.communication.jobrouter.implementation.models.RouterQueueInternal;
import com.azure.communication.jobrouter.implementation.models.RouterQueueItemInternal;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterQueueItem;
import com.azure.communication.jobrouter.models.UpdateQueueOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.ETag;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts request options for create and update Queue to {@link RouterQueue}.
 */
public class QueueAdapter {

    /**
     * Converts {@link CreateQueueOptions} to {@link RouterQueue}.
     * @param createQueueOptions Container with options to create {@link RouterQueue}
     * @return JobQueue
     */
    public static RouterQueueInternal convertCreateQueueOptionsToRouterQueue(CreateQueueOptions createQueueOptions) {
        Map<String, LabelValue> labelValueMap = createQueueOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : new HashMap<>();

        return new RouterQueueInternal()
            .setName(createQueueOptions.getName())
            .setLabels(labels)
            .setDistributionPolicyId(createQueueOptions.getDistributionPolicyId())
            .setExceptionPolicyId(createQueueOptions.getExceptionPolicyId());
    }

    /**
     * Converts {@link UpdateQueueOptions} to {@link RouterQueue}.
     * @param updateQueueOptions Container with options to update {@link RouterQueue}
     * @return RouterQueue.
     */
    public static RouterQueueInternal convertUpdateQueueOptionsToRouterQueue(UpdateQueueOptions updateQueueOptions) {
        Map<String, LabelValue> labelValueMap = updateQueueOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) :  new HashMap<>();

        return new RouterQueueInternal()
            .setName(updateQueueOptions.getName())
            .setLabels(labels)
            .setDistributionPolicyId(updateQueueOptions.getDistributionPolicyId())
            .setExceptionPolicyId(updateQueueOptions.getExceptionPolicyId());
    }

    public static PagedFlux<RouterQueueItem> convertPagedFluxToPublic(PagedFlux<RouterQueueItemInternal> internalPagedFlux) {
        final Function<PagedResponse<RouterQueueItemInternal>, PagedResponse<RouterQueueItem>> responseMapper
            = internalResponse -> new PagedResponseBase<Void, RouterQueueItem>(internalResponse.getRequest(),
            internalResponse.getStatusCode(),
            internalResponse.getHeaders(),
            internalResponse.getValue()
                .stream()
                .map(internal -> new RouterQueueItem()
                    .setQueue(RouterQueueConstructorProxy.create(internal.getQueue()))
                    .setEtag(new ETag(internal.getEtag())))
                .collect(Collectors.toList()),
            internalResponse.getContinuationToken(),
            null);

        return PagedFlux.create(() -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<RouterQueueItemInternal>> flux = (continuationToken == null)
                ? internalPagedFlux.byPage()
                : internalPagedFlux.byPage(continuationToken);
            return flux.map(responseMapper);
        });
    }
}
