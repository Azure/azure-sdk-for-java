package com.azure.core.amqp;

import org.apache.qpid.proton.message.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * An AMQP link that sends information to the remote endpoint.
 */
public interface AmqpSendLink extends AmqpLink {
    /**
     * Sends a single message to the remote endpoint.
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

    /**
     * A send operation that returns associated responses from messages that were sent.
     *
     * @param messages Messages to send to the service.
     * @param mappingFunction Function to map response {@link Message} to a result.
     * @return A stream of mapped responses.
     */
    <T> Publisher<T> sendWithResponse(Publisher<Message> messages, Function<Message, T> mappingFunction);
}
