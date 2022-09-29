// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.client;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.cloud.core.provider.ClientOptionsProvider;

/**
 * Properties shared by all amqp client builders.
 */
public final class AmqpClientProperties extends ClientProperties implements ClientOptionsProvider.AmqpClientOptions {

    /**
     * Transport type for AMQP-based client.
     */
    private AmqpTransportType transportType;

    /**
     * Get the transport type.
     * @return The transport type.
     */
    public AmqpTransportType getTransportType() {
        return transportType;
    }

    /**
     * Set the transport type.
     * @param transportType The transport type.
     */
    public void setTransportType(AmqpTransportType transportType) {
        this.transportType = transportType;
    }
}
