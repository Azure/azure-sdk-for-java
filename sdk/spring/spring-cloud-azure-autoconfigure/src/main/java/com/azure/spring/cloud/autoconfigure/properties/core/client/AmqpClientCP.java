// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.client;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.spring.core.aware.ClientAware;

/**
 *
 */
public class AmqpClientCP extends ClientCP implements ClientAware.AmqpClient {

    private AmqpTransportType transportType = AmqpTransportType.AMQP;

    @Override
    public AmqpTransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(AmqpTransportType transportType) {
        this.transportType = transportType;
    }
}
