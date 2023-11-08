// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.core.config;

import com.azure.spring.messaging.implementation.config.AbstractAzureListenerEndpoint;

/**
 * Base model for a Azure listener endpoint.
 *
 * @see MethodEventHubsListenerEndpoint
 */
abstract class AbstractEventHubsListenerEndpoint extends AbstractAzureListenerEndpoint {

    private Boolean batchListener;

    public Boolean getBatchListener() {
        return batchListener;
    }

    public void setBatchListener(Boolean batchListener) {
        this.batchListener = batchListener;
    }

    public boolean isBatchListener() {
        return Boolean.TRUE.equals(this.batchListener);
    }
}
