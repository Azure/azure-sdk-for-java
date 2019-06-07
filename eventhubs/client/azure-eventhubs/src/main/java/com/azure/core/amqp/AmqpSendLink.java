// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import org.apache.qpid.proton.message.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * An AMQP link that sends information to the remote endpoint.
 */
public interface AmqpSendLink extends AmqpLink {
    /**
     * Sends a single message to the remote endpoint.
     *
     * @param message Message to send.
     * @return A Mono that completes when the message has been sent.
     */
    Mono<Void> send(Message message);

    /**
     * Sends messages to the service. This completes when all the messages have been pushed to the service.
     *
     * @param messages Messages to send to the service.
     * @return A Mono that completes when all the messages have been sent.
     */
    Mono<Void> send(Publisher<Message> messages);
}
