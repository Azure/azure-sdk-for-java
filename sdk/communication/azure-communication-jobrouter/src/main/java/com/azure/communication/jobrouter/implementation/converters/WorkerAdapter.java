// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.accesshelpers.RouterWorkerConstructorProxy;
import com.azure.communication.jobrouter.implementation.models.ChannelConfigurationInternal;
import com.azure.communication.jobrouter.implementation.models.RouterJobOfferInternal;
import com.azure.communication.jobrouter.implementation.models.RouterWorkerAssignmentInternal;
import com.azure.communication.jobrouter.implementation.models.RouterWorkerInternal;
import com.azure.communication.jobrouter.implementation.models.RouterWorkerItemInternal;
import com.azure.communication.jobrouter.models.ChannelConfiguration;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.RouterJobOffer;
import com.azure.communication.jobrouter.models.RouterQueueAssignment;
import com.azure.communication.jobrouter.models.RouterWorkerAssignment;
import com.azure.communication.jobrouter.models.RouterWorkerItem;
import com.azure.communication.jobrouter.models.UpdateJobOptions;
import com.azure.communication.jobrouter.models.UpdateWorkerOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.ETag;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts request options for create and update Worker to {@link RouterWorkerInternal}.
 */
public class WorkerAdapter {
    /**
     * Converts {@link CreateWorkerOptions} to {@link RouterWorkerInternal}.
     * @param createWorkerOptions Container with options to create {@link RouterWorkerInternal}
     * @return RouterWorker
     */
    public static RouterWorkerInternal convertCreateWorkerOptionsToRouterWorker(CreateWorkerOptions createWorkerOptions) {
        Map<String, LabelValue> labelValueMap = createWorkerOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : new HashMap<>();
        Map<String, LabelValue> tagValueMap = createWorkerOptions.getLabels();
        Map<String, Object> tags = tagValueMap != null ? tagValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : new HashMap<>();
        Map<String, RouterQueueAssignment> queueAssignmentsMap = createWorkerOptions.getQueueAssignments();
        Map<String, Object> queueAssignments = queueAssignmentsMap != null ? queueAssignmentsMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new RouterQueueAssignment())) : new HashMap<>();

        return new RouterWorkerInternal()
            .setLabels(labels)
            .setTags(tags)
            .setQueueAssignments(queueAssignments)
            .setAvailableForOffers(createWorkerOptions.isAvailableForOffers())
            .setChannelConfigurations(convertChannelConfigurationsToInternal(createWorkerOptions.getChannelConfigurations()))
            .setTotalCapacity(createWorkerOptions.getTotalCapacity());
    }

    /**
     * Converts {@link UpdateJobOptions} to {@link RouterWorkerInternal}.
     * @param updateWorkerOptions Container with options to update {@link RouterWorkerInternal}
     * @return RouterWorker
     */
    public static RouterWorkerInternal convertUpdateWorkerOptionsToRouterWorker(UpdateWorkerOptions updateWorkerOptions) {
        Map<String, LabelValue> labelValueMap = updateWorkerOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : new HashMap<>();
        Map<String, LabelValue> tagValueMap = updateWorkerOptions.getLabels();
        Map<String, Object> tags = tagValueMap != null ? tagValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())) : new HashMap<>();
        Map<String, RouterQueueAssignment> queueAssignmentsMap = updateWorkerOptions.getQueueAssignments();
        Map<String, Object> queueAssignments = queueAssignmentsMap != null ? queueAssignmentsMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new RouterQueueAssignment())) : new HashMap<>();

        return new RouterWorkerInternal()
            .setLabels(labels)
            .setTags(tags)
            .setQueueAssignments(queueAssignments)
            .setAvailableForOffers(updateWorkerOptions.isAvailableForOffers())
            .setChannelConfigurations(convertChannelConfigurationsToInternal(updateWorkerOptions.getChannelConfigurations()))
            .setTotalCapacity(updateWorkerOptions.getTotalCapacity());
    }

    public static PagedFlux<RouterWorkerItem> convertPagedFluxToPublic(PagedFlux<RouterWorkerItemInternal> internalPagedFlux) {
        final Function<PagedResponse<RouterWorkerItemInternal>, PagedResponse<RouterWorkerItem>> responseMapper
            = internalResponse -> new PagedResponseBase<Void, RouterWorkerItem>(internalResponse.getRequest(),
            internalResponse.getStatusCode(),
            internalResponse.getHeaders(),
            internalResponse.getValue()
                .stream()
                .map(internal -> new RouterWorkerItem()
                    .setWorker(RouterWorkerConstructorProxy.create(internal.getWorker()))
                    .setEtag(new ETag(internal.getEtag())))
                .collect(Collectors.toList()),
            internalResponse.getContinuationToken(),
            null);

        return PagedFlux.create(() -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<RouterWorkerItemInternal>> flux = (continuationToken == null)
                ? internalPagedFlux.byPage()
                : internalPagedFlux.byPage(continuationToken);
            return flux.map(responseMapper);
        });
    }

    public static Map<String, ChannelConfiguration> convertChannelConfigurationsToPublic(Map<String, ChannelConfigurationInternal> internal) {
        return internal != null ? internal.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ChannelConfiguration(entry.getValue().getCapacityCostPerJob())
                .setMaxNumberOfJobs(entry.getValue().getMaxNumberOfJobs()))) : new HashMap<>();
    }

    public static Map<String, ChannelConfigurationInternal> convertChannelConfigurationsToInternal(Map<String, ChannelConfiguration> channelConfigurations) {
        return channelConfigurations != null ? channelConfigurations.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ChannelConfigurationInternal()
                .setCapacityCostPerJob(entry.getValue().getCapacityCostPerJob())
                .setMaxNumberOfJobs(entry.getValue().getMaxNumberOfJobs()))) : new HashMap<>();
    }

    public static List<RouterJobOffer> convertOffersToPublic(List<RouterJobOfferInternal> internal) {
        return internal != null ? internal.stream()
            .map(o -> new RouterJobOffer()
                .setJobId(o.getJobId())
                .setOfferedAt(o.getOfferedAt())
                .setOfferId(o.getOfferId())
                .setCapacityCost(o.getCapacityCost())
                .setExpiresAt(o.getExpiresAt()))
            .collect(Collectors.toList()) : new ArrayList<>();
    }

    public static List<RouterWorkerAssignment> convertAssignmentsToPublic(List<RouterWorkerAssignmentInternal> internal) {
        return internal != null ? internal.stream()
            .map(o -> new RouterWorkerAssignment()
                .setJobId(o.getJobId())
                .setAssignmentId(o.getAssignmentId())
                .setAssignedAt(o.getAssignedAt())
                .setCapacityCost(o.getCapacityCost()))
            .collect(Collectors.toList()) : new ArrayList<>();
    }
}
