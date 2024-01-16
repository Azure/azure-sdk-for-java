// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.models.RouterWorkerInternal;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.RouterWorker;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts request options for create and update Worker to {@link RouterWorker}.
 */
public class WorkerAdapter {
    /**
     * Converts {@link CreateWorkerOptions} to {@link RouterWorker}.
     * @param createWorkerOptions Container with options to create {@link RouterWorker}
     * @return RouterWorker
     */
    public static RouterWorkerInternal convertCreateWorkerOptionsToRouterWorker(CreateWorkerOptions createWorkerOptions) {
        Map<String, Object> labels = createWorkerOptions.getLabels().entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> entry.getKey(),
                entry -> RouterValueAdapter.getValue(entry.getValue())));

        Map<String, Object> tags = createWorkerOptions.getTags().entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> entry.getKey(),
                entry -> RouterValueAdapter.getValue(entry.getValue())));

        return new RouterWorkerInternal()
            .setLabels(labels)
            .setTags(tags)
            .setAvailableForOffers(createWorkerOptions.isAvailableForOffers())
            .setChannels(createWorkerOptions.getChannels())
            .setCapacity(createWorkerOptions.getCapacity());
    }
}
