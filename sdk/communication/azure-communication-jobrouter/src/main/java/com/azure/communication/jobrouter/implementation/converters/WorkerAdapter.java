// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.RouterWorker;

/**
 * Converts request options for create and update Worker to {@link RouterWorker}.
 */
public class WorkerAdapter {
    /**
     * Converts {@link CreateWorkerOptions} to {@link RouterWorker}.
     * @param createWorkerOptions Container with options to create {@link RouterWorker}
     * @return RouterWorker
     */
    public static RouterWorker convertCreateWorkerOptionsToRouterWorker(CreateWorkerOptions createWorkerOptions) {
        return new RouterWorker()
            .setLabels(createWorkerOptions.getLabels())
            .setTags(createWorkerOptions.getTags())
            .setAvailableForOffers(createWorkerOptions.isAvailableForOffers())
            .setChannels(createWorkerOptions.getChannels())
            .setCapacity(createWorkerOptions.getCapacity());
    }
}
