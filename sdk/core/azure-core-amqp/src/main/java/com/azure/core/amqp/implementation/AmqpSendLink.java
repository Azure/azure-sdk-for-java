// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.engine.Delivery;
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
     * @throws AmqpException if the serialized {@code message} exceed the links capacity for a single message.
     */
    Mono<Void> send(Message message);

    /**
     * Batches list of messages given into a single proton-j message that is sent down the wire.
     *
     * @param messages The list of messages to send to the service.
     * @param deliveryState to be sent along with message.
     *
     * @return A Mono that completes when all the batched messages are successfully transmitted to message broker.
     * @throws AmqpException if the serialized contents of {@code messageBatch} exceed the link's capacity for a single
     * message.
     */
    Mono<Void> send(List<Message> messages, DeliveryState deliveryState);

    /**
     * Sends a single message to the remote endpoint.
     *
     * @param message Message to send.
     * @param deliveryState to be sent along with message.
     * @return A Mono that completes when the message has been sent.
     * @throws AmqpException if the serialized {@code message} exceed the links capacity for a single message.
     */
    Mono<Void> send(Message message, DeliveryState deliveryState);

    /**
     * Batches the messages given into a single proton-j message that is sent down the wire.
     *
     * @param messageBatch The batch of messages to send to the service.
     * @return A Mono that completes when all the batched messages are successfully transmitted to the message broker.
     * @throws AmqpException if the serialized contents of {@code messageBatch} exceed the link's capacity for a single
     * message.
     */
    Mono<Void> send(List<Message> messageBatch);

    /**
     * send the message and return {@link DeliveryState} of this delivery in message broker.
     *
     * @param bytes to send to message broker
     * @param arrayOffset offset of the message.
     * @param messageFormat to be set on the message.
     * @param deliveryState to be updated on the {@link Delivery}.
     *
     * @return A completable of {@link DeliveryState} received from Message Broker.
     */
    Mono<DeliveryState> send(byte[] bytes, int arrayOffset, int messageFormat, DeliveryState deliveryState);

    /**
     * Gets the size of the send link. {@link Message Messages} sent on the link cannot exceed the size.
     *
     * @return A Mono that completes and returns the size of the send link.
     */
    Mono<Integer> getLinkSize();

    /**
     * Gets the context for this AMQP send link.
     *
     * @return The context for this AMQP send link.
     */
    AmqpErrorContext getErrorContext();
}
