package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.models.EventPosition;
import com.azure.core.amqp.models.ReceiveOptions;
import com.azure.messaging.servicebus.ReceiveMode;
import reactor.core.publisher.Mono;

public interface ServiceBusAmqpConnection extends AmqpConnection {

    /**
     * Creates or gets a send link. The same link is returned if there is an existing send link with the same {@code
     * linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param retryOptions Options to use when creating the link.
     * @return A new or existing send link that is connected to the given {@code entityPath}.
     */
    Mono<AmqpSendLink> createSendLink(String linkName, String entityPath, AmqpRetryOptions retryOptions);

    /**
     * Creates or gets an existing receive link. The same link is returned if there is an existing receive link with the
     * same {@code linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param receiveMode {@link ReceiveMode} to use when creating the link.
     * @return A new or existing receive link that is connected to the given {@code entityPath}.
     */
    Mono<AmqpReceiveLink> createReceiveLink(String linkName, String entityPath, ReceiveMode receiveMode);
}
