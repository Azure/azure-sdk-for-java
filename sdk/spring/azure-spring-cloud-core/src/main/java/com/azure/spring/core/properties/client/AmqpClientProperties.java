package com.azure.spring.core.properties.client;

import com.azure.core.amqp.AmqpTransportType;

public class AmqpClientProperties extends ClientProperties {

    private AmqpTransportType transportType;

    public AmqpTransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(AmqpTransportType transportType) {
        this.transportType = transportType;
    }
}
