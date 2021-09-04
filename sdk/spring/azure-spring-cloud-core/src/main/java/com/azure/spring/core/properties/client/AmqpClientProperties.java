// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.client;

import com.azure.core.amqp.AmqpTransportType;

/**
 * Properties shared by all amqp client builders.
 */
public class AmqpClientProperties extends ClientProperties {

    // TODO (xiada): should we use enum here?
    private AmqpTransportType transportType;

    public AmqpTransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(AmqpTransportType transportType) {
        this.transportType = transportType;
    }
}
