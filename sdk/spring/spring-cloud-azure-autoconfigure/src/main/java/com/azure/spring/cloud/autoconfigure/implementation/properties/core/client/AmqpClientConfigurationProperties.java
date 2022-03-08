// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.client;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.cloud.autoconfigure.properties.core.client.ClientConfigurationProperties;
import com.azure.spring.cloud.core.provider.ClientOptionsProvider;

/**
 *
 */
public class AmqpClientConfigurationProperties extends ClientConfigurationProperties implements ClientOptionsProvider.AmqpClientOptions {

    /**
     * Transport type for AMQP-based client.
     */
    private AmqpTransportType transportType = AmqpTransportType.AMQP;

    @Override
    public AmqpTransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(AmqpTransportType transportType) {
        this.transportType = transportType;
    }
}
