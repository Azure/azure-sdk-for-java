// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import com.azure.spring.messaging.servicebus.support.ServiceBusMessageHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

/**
 * Azure Service Bus template to support send {@link Message} asynchronously.
 *
 * <p>
 * A {@link #defaultEntityType} is required when no entity type is specified in {@link ServiceBusProducerFactory}
 * via related {@link NamespaceProperties} or producer {@link PropertiesSupplier}.
 * </p>
 */
public class ServiceBusTemplate implements SendOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusTemplate.class);
    private static final ServiceBusMessageConverter DEFAULT_CONVERTER = new ServiceBusMessageConverter();
    private static final Duration DEFAULT_PRC_SEND_TIMEOUT = Duration.ofSeconds(30);
    private final ServiceBusProducerFactory producerFactory;
    private final ServiceBusConsumerFactory consumerFactory;
    private AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter = DEFAULT_CONVERTER;
    private ServiceBusEntityType defaultEntityType;
    private Duration rpcSendTimeout = DEFAULT_PRC_SEND_TIMEOUT;

    /**
     * Create an instance using the supplied producer factory.
     * @param producerFactory the producer factory.
     */
    public ServiceBusTemplate(ServiceBusProducerFactory producerFactory) {
        this(producerFactory, null);
    }

    /**
     * Create an instance using the supplied producer factory, processor factory, it supports request-reply pattern.
     * @param producerFactory the producer factory.
     * @param consumerFactory the consumer factory.
     * @since 5.22.0
     */
    public ServiceBusTemplate(ServiceBusProducerFactory producerFactory,
                              ServiceBusConsumerFactory consumerFactory) {
        this.producerFactory = producerFactory;
        this.consumerFactory = consumerFactory;
    }

    @Override
    public <U> Mono<Void> sendAsync(String destination, Message<U> message) {
        Assert.hasText(destination, "destination can't be null or empty");
        ServiceBusSenderAsyncClient senderAsyncClient =
                     this.producerFactory.createProducer(destination, defaultEntityType);
        ServiceBusMessage serviceBusMessage = messageConverter.fromMessage(message, ServiceBusMessage.class);
        return senderAsyncClient.sendMessage(serviceBusMessage);
    }

    /**
     * Basic RPC pattern usage. Send a message to the destination and wait for a reply message from the replay channel,
     * which must be specified by the message header {@link MessageHeaders#REPLY_CHANNEL} and the session must be enabled;
     * the message header {@link ServiceBusMessageHeaders#REPLY_TO_SESSION_ID} is used to specify a unique reply session ID,
     * if not set, a random {@link UUID} will be used. Use the default entity type and PRC send timeout.
     *
     * @param <U> The type of the message payload.
     * @param destination topic or queue name.
     * @param message Message to be sent to the Service Bus entity.
     *
     * @return the reply message of the response. If the reply message fails to be obtained, null is returned.
     *
     * @throws IllegalStateException if sender or receiver is already disposed.
     * @since 5.22.0
     */
    public <U> ServiceBusReceivedMessage sendAndReceive(String destination,
                                                        Message<U> message) {
        return sendAndReceive(destination, defaultEntityType, message, rpcSendTimeout);
    }

    /**
     * Basic RPC pattern usage. Send a message to the destination and wait for a reply message from the replay channel,
     * which must be specified by the message header {@link MessageHeaders#REPLY_CHANNEL} and the session must be enabled;
     * the message header {@link ServiceBusMessageHeaders#REPLY_TO_SESSION_ID} is used to specify a unique reply session ID,
     * if not set, a random {@link UUID} will be used. Use the default PRC send timeout.
     *
     * @param <U> The type of the message payload.
     * @param destination topic or queue name.
     * @param entityType type of Service Bus entity.
     * @param message Message to be sent to the Service Bus entity.
     *
     * @return the reply message of the response. If the reply message fails to be obtained, null is returned.
     *
     * @throws IllegalStateException if sender or receiver is already disposed.
     * @since 5.22.0
     */
    public <U> ServiceBusReceivedMessage sendAndReceive(String destination,
                                    ServiceBusEntityType entityType,
                                    Message<U> message) {
        return sendAndReceive(destination, entityType, message, rpcSendTimeout);
    }

    /**
     * Basic RPC pattern usage. Send a message to the destination and wait for a reply message from the replay channel,
     * which must be specified by the message header {@link MessageHeaders#REPLY_CHANNEL} and the session must be enabled;
     * the message header {@link ServiceBusMessageHeaders#REPLY_TO_SESSION_ID} is used to specify a unique reply session ID,
     * if not set, a random {@link UUID} will be used. The 'defaultEntityType' will be used if 'entityType' is not specified.
     *
     * @param <U> The type of the message payload.
     * @param destination topic or queue name.
     * @param entityType type of Service Bus entity.
     * @param message the message to be sent to the Service Bus entity.
     * @param sendTimeout timeout for sending the message.
     *
     * @return the reply message of the response. If the reply message fails to be obtained, null is returned.
     *
     * @throws IllegalStateException if sender or receiver is already disposed.
     * @since 5.22.0
     */
    public <U> ServiceBusReceivedMessage sendAndReceive(String destination,
                                    ServiceBusEntityType entityType,
                                    Message<U> message,
                                    Duration sendTimeout) {
        Assert.hasText(destination, "'destination' can't be null or empty");
        Assert.notNull(consumerFactory, "'consumerFactory' can't be null, please enable 'session-enabled' and 'rpc-enabled' for consumer.");

        ServiceBusEntityType currentEntityType = entityType;
        if (entityType == null && defaultEntityType != null) {
            currentEntityType = defaultEntityType;
        }

        MessageHeaders headers = message.getHeaders();
        String replyDestination = headers.get(MessageHeaders.REPLY_CHANNEL, String.class);
        Assert.hasText(replyDestination, "Message header '" + MessageHeaders.REPLY_CHANNEL + "' can't be null or empty.");
        ServiceBusMessage serviceBusMessage = messageConverter.fromMessage(message, ServiceBusMessage.class);
        String replyToSessionId = serviceBusMessage.getReplyToSessionId();
        if (!StringUtils.hasText(replyToSessionId)) {
            replyToSessionId = UUID.randomUUID().toString();
            serviceBusMessage.setReplyToSessionId(replyToSessionId);
            LOGGER.debug("Generated unique reply-to session-id '{}' for entity '{}'.", replyToSessionId, destination);
        } else {
            LOGGER.debug("Provided reply-to session id ‘{}’ for entity '{}', it should be unique.", replyToSessionId, destination);
        }

        ServiceBusSenderAsyncClient senderAsyncClient = this.producerFactory.createProducer(destination, currentEntityType);
        senderAsyncClient.sendMessage(serviceBusMessage).block(sendTimeout);

        ServiceBusSessionReceiverClient sessionReceiver = consumerFactory.createReceiver(replyDestination, currentEntityType);
        Assert.notNull(sessionReceiver, "'sessionReceiver' can't be null, please enable 'session-enabled' for consumer.");
        return sessionReceiver.acceptSession(replyToSessionId)
                              .receiveMessages(1)
                              .stream()
                              .findFirst()
                              .orElse(null);
    }

    /**
     * Set the message converter to use.
     * @param messageConverter the message converter.
     */
    public void setMessageConverter(AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Get the message converter.
     * @return the message converter.
     */
    public AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> getMessageConverter() {
        return messageConverter;
    }

    /**
     * Set the default entity type of the destination to be sent messages to. Required when no entity type is specified
     * in {@link ServiceBusProducerFactory} via related the {@link NamespaceProperties} or producer {@link PropertiesSupplier}.
     * @param entityType the entity type.
     */
    public void setDefaultEntityType(ServiceBusEntityType entityType) {
        defaultEntityType = entityType;
    }

    /**
     * Set the timeout for sending message for request-reply pattern. If not set, the default {@link #DEFAULT_PRC_SEND_TIMEOUT} will be used.
     * @param rpcSendTimeout the timeout of sending messages.
     * @since 5.22.0
     */
    public void setRpcSendTimeout(Duration rpcSendTimeout) {
        if (rpcSendTimeout != null) {
            this.rpcSendTimeout = rpcSendTimeout;
        }
    }
}
