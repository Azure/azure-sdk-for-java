// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.resources.fluentcore.rest.ActivationResponse;

/**
 * The accepted LRO (long running operation).
 *
 * @param <T> the type of final result
 */
public interface Accepted<T> {

    /**
     * Gets the activation response of LRO.
     *
     * @return the activation response
     */
    ActivationResponse<T> getActivationResponse();

    /**
     * Gets the {@link SyncPoller} of LRO.
     *
     * @return the sync poller.
     */
    SyncPoller<Void, T> getSyncPoller();

    /**
     * Gets the final result of LRO.
     *
     * @return the final result.
     * @throws com.azure.core.management.exception.ManagementException If polling fails.
     */
    T getFinalResult();
}
