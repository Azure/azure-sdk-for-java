// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.servicebus.implementation.ManagementConstants.MANAGEMENT_OPERATION_KEY;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.MANAGEMENT_SERVER_TIMEOUT;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.PEEK_OPERATION_VALUE;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.REQUEST_RESPONSE_FROM_SEQUENCE_NUMBER;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.REQUEST_RESPONSE_MESSAGE_COUNT;

/**
 * Channel responsible for Service Bus related metadata, peek  and management plane operations.
 * Management plane operations increasing quotas, etc.
 */
public class ManagementChannel implements  ServiceBusManagementNode {

    private final Scheduler scheduler;
    private final MessageSerializer messageSerializer;
    private final TokenManager tokenManager;
    private final Duration operationTimeout;
    private final Mono<RequestResponseChannel> createRequestResponse;
    private final String fullyQualifiedNamespace;
    private final ClientLogger logger;
    private final String entityPath;
    private final AtomicReference<Long> lastPeekedSequenceNumber = new AtomicReference<>(0L);

    private volatile boolean isDisposed;

    ManagementChannel(Mono<RequestResponseChannel> createRequestResponse, String fullyQualifiedNamespace,
        String entityPath, TokenManager tokenManager, MessageSerializer messageSerializer, Scheduler scheduler,
        Duration operationTimeout) {
        this.createRequestResponse = createRequestResponse;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.logger = new ClientLogger(String.format("%s<%s>", ManagementChannel.class, entityPath));
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' cannot be null.");
        this.tokenManager = Objects.requireNonNull(tokenManager, "'tokenManager' cannot be null.");
        this.operationTimeout = operationTimeout;
    }

    /**
     * Completes a message given its lock token.
     *
     * @param lockToken Lock token to complete.
     * @return Mono that completes successfully when the message is completed. Otherwise, returns an error.
     */
    @Override
    public Mono<Void> complete(UUID lockToken) {
        return Mono.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ServiceBusReceivedMessage> peek(long fromSequenceNumber) {
        return peek(fromSequenceNumber, 1, null)
            .last()
            .publishOn(scheduler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ServiceBusReceivedMessage> peek() {
        return peek(lastPeekedSequenceNumber.get() + 1);
    }

    private Flux<ServiceBusReceivedMessage> peek(long fromSequenceNumber, int maxMessages, UUID sessionId) {
        return tokenManager.getAuthorizationResults().switchOnFirst((signal, publisher) -> {
            if (signal.hasValue() && (signal.get() == AmqpResponseCode.ACCEPTED)) {
                return createRequestResponse.flatMap(channel -> {
                    final Map<String, Object> appProperties = new HashMap<>();
                    // set mandatory application properties for AMQP message.
                    appProperties.put(MANAGEMENT_OPERATION_KEY, PEEK_OPERATION_VALUE);
                    appProperties.put(MANAGEMENT_SERVER_TIMEOUT, operationTimeout);

                    final Message request = Proton.message();
                    final ApplicationProperties applicationProperties = new ApplicationProperties(appProperties);
                    request.setApplicationProperties(applicationProperties);

                    // set mandatory properties on AMQP message body
                    HashMap<String, Object> requestBodyMap = new HashMap<>();
                    requestBodyMap.put(REQUEST_RESPONSE_FROM_SEQUENCE_NUMBER, fromSequenceNumber);
                    requestBodyMap.put(REQUEST_RESPONSE_MESSAGE_COUNT, maxMessages);

                    if (!Objects.isNull(sessionId)) {
                        requestBodyMap.put(ManagementConstants.REQUEST_RESPONSE_SESSION_ID, sessionId);
                    }

                    request.setBody(new AmqpValue(requestBodyMap));

                    return channel.sendWithAck(request);
                }).flatMapMany(amqpMessage -> {
                    @SuppressWarnings("unchecked")
                    final List<ServiceBusReceivedMessage> messageList =
                        messageSerializer.deserialize(amqpMessage, List.class);

                    // Assign the last sequence number so that we can peek from next time
                    if (messageList.size() > 0) {
                        final ServiceBusReceivedMessage receivedMessage = messageList.get(messageList.size() - 1);

                        logger.info("Setting last peeked sequence number: {}", receivedMessage.getSequenceNumber());

                        if (receivedMessage.getSequenceNumber() > 0) {
                            this.lastPeekedSequenceNumber.set(receivedMessage.getSequenceNumber());
                        }
                    }

                    return Flux.fromIterable(messageList);
                });
            }

            return Flux.error(new AmqpException(false, "User does not have authorization to entity: " + entityPath,
                new SessionErrorContext(fullyQualifiedNamespace, entityPath)));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (isDisposed) {
            return;
        }

        isDisposed = true;
        tokenManager.close();
    }
}
