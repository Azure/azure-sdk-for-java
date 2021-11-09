// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.client;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.core.aware.ClientAware;

/**
 * Properties shared by all amqp client builders.
 */
public final class AmqpClientProperties extends ClientProperties implements ClientAware.AmqpClient {

    private AmqpTransportType transportType = AmqpTransportType.AMQP;

    public AmqpTransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(AmqpTransportType transportType) {
        this.transportType = transportType;
    }
}
