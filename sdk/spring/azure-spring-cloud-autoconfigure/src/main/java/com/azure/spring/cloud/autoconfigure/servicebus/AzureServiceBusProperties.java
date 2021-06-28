// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.stream.Stream;

/**
 * @author Warren Zhu
 */
@ConfigurationProperties("spring.cloud.azure.servicebus")
public class AzureServiceBusProperties {

    private static final String NO_RETRY = "NoRetry";
    private static final String DEFAULT_RETRY = "Default";

    private static final AmqpRetryOptions AMQP_NO_RETRY = new AmqpRetryOptions().setMaxRetries(0);

    private static final AmqpRetryOptions AMQP_DEFAULT_RETRY = new AmqpRetryOptions().setTryTimeout(ServiceBusConstants.OPERATION_TIMEOUT);

    private String namespace;

    private String connectionString;

    private AmqpTransportType transportType = AmqpTransportType.AMQP;

    private AmqpRetryOptions amqpRetryOptions = new AmqpRetryOptions().setTryTimeout(ServiceBusConstants.OPERATION_TIMEOUT);

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

    public void setTransportType(String transportType) {
        this.transportType = Stream.of(transportType)
                                   .map(AmqpTransportType::fromString)
                                   .findFirst()
                                   .orElse(AmqpTransportType.AMQP);
    }

    public AmqpRetryOptions getAmqpRetryOptions() {
        return amqpRetryOptions;
    }

    public void setAmqpRetryOptions(String amqpRetryOptions) {
        this.amqpRetryOptions = NO_RETRY.equals(amqpRetryOptions) ? AMQP_NO_RETRY : AMQP_DEFAULT_RETRY;
    }
}
