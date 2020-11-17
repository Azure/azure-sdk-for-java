// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBodyType;

/**
 * java doc sample for  {@link ServiceBusMessage}.
 */
public class ServiceBusMessageJavaDocCodeSamples {
    /**
     * Copy {@link AmqpAnnotatedMessage}.
     */
    public void copyServiceBusReceivedMessage() {
        ServiceBusReceivedMessage sourceServiceBusReceivedMessage = null;
        // BEGIN: com.azure.messaging.servicebus.ServiceBusMessage.copyServiceBusMessage
        AmqpMessageBodyType bodyType = sourceServiceBusReceivedMessage.getAmqpAnnotatedMessage().getBody().getBodyType();
        ServiceBusMessage copyMessage = null;
        switch (bodyType) {
            case DATA:
                copyMessage = new ServiceBusMessage(sourceServiceBusReceivedMessage);
                break;
            case SEQUENCE:
            case VALUE:
                throw new RuntimeException("Body type not supported yet.");
            default:
                throw new RuntimeException("Body type not valid.");
        }
        // END: com.azure.messaging.servicebus.ServiceBusMessage.copyServiceBusMessage
    }
}
