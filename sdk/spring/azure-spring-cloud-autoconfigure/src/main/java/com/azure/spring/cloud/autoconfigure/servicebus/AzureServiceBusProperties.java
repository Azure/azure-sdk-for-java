// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpTransportType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Warren Zhu
 */
@ConfigurationProperties("spring.cloud.azure.servicebus")
public class AzureServiceBusProperties {

    private String namespace;

    private String connectionString;

    private AmqpTransportType transportType = AmqpTransportType.AMQP;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public AmqpTransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(AmqpTransportType transportType) {
        this.transportType = transportType;
    }
}
