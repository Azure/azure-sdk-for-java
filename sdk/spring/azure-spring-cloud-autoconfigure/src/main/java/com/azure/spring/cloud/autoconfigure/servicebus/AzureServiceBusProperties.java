// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Warren Zhu
 */
@ConfigurationProperties("spring.cloud.azure.servicebus")
public class AzureServiceBusProperties {

    private String namespace;

    private String connectionString;

    private AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(ServiceBusConstants.OPERATION_TIMEOUT);

    private AmqpTransportType transportType = AmqpTransportType.AMQP;

    /**
     *
     * @return The namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     *
     * @param namespace The namespace.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     *
     * @return The connection string.
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     *
     * @param connectionString The connection string.
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     *
     * @return The transport type.
     */
    public AmqpTransportType getTransportType() {
        return transportType;
    }

    /**
     *
     * @param transportType The transport type.
     */
    public void setTransportType(AmqpTransportType transportType) {
        this.transportType = transportType;
    }

    /**
     *
     * @return The AmqpRetryOptions.
     */
    public AmqpRetryOptions getRetryOptions() {
        return retryOptions;
    }

    /**
     *
     * @param retryOptions The retry options.
     */
    public void setRetryOptions(AmqpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
    }
}
