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
     * Creates an instance of {@link AmqpClientProperties}.
     */
    public AmqpClientProperties() {
    }

    /**
     * Transport type for AMQP-based client. Supported types are: AMQP, AMQP_WEB_SOCKETS.
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
