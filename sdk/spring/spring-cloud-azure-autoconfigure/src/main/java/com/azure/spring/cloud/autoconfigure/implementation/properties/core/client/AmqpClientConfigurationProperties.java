// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.client;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.cloud.core.provider.ClientOptionsProvider;

/**
 *
 */
public class AmqpClientConfigurationProperties extends ClientConfigurationProperties implements ClientOptionsProvider.AmqpClientOptions {

    /**
     * Transport type for AMQP-based client. Supported types are: AMQP, AMQP_WEB_SOCKETS.
     */
    private AmqpTransportType transportType;

    @Override
    public AmqpTransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(AmqpTransportType transportType) {
        this.transportType = transportType;
    }
}
