// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.exception.AmqpException;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * An AMQP link that sends information to the remote endpoint.
 */
public interface AmqpSendLink extends AmqpLink {
    /**
     * Sends a single message to the remote endpoint.
     *
     * @param message Message to send.
     * @return A Mono that completes when the message has been sent.
     * @throws AmqpException if the serialised {@code message} exceed the link's capacity for a single message.
     */
    Mono<Void> send(Message message);

    /**
     * Batches the messages given into a single proton-j message that is sent down the wire.
     *
     * @param messageBatch The batch of messages to send to the service.
     * @return A Mono that completes when all the batched message is successfully transmitted to Event Hub.
     * @throws AmqpException if the serialised contents of {@code messageBatch} exceed the link's capacity for a single
     * message.
     */
    Mono<Void> sendBatch(List<Message> messageBatch);
}
