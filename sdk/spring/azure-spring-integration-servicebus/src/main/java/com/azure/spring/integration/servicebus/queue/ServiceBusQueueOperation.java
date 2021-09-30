// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue;

import com.azure.spring.integration.core.api.SendOperation;
import com.azure.spring.integration.core.api.SubscribeOperation;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.health.InstrumentationManager;
import org.springframework.messaging.Message;

import java.util.concurrent.CompletableFuture;

/**
 * Azure service bus queue operation to support send
 * {@link org.springframework.messaging.Message} asynchronously and subscribe and abandon
 *
 *
 * @author Warren Zhu
 */
public interface ServiceBusQueueOperation extends SendOperation, SubscribeOperation {

    InstrumentationManager getInstrumentationManager();

    void setClientConfig(ServiceBusClientConfig clientConfig);

    /**
     * Send a {@link Message} to the given destination deadletterqueue.
     * @param destination destination
     * @param message message
     * @param deadLetterReason deadLetterReason
     * @param deadLetterErrorDescription deadLetterErrorDescription
     * @param <T> Type of the object to serialize into an AMQP message.
     */
    <T> void deadLetter(String destination, Message<T> message, String deadLetterReason,
                        String deadLetterErrorDescription);

    /**
     * Abandon Message with lock token and updated message property. This will make the message available again for
     * processing. Abandoning a message will increase the delivery count on the message
     *
     * @param destination destination
     * @param message message
     * @param <T> Type of the object to serialize into an AMQP message.
     */
    <T> void abandon(String destination, Message<T> message);
}
