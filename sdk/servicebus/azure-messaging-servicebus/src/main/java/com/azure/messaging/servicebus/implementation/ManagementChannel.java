// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.RequestResponseUtils;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.messaging.servicebus.implementation.ManagementConstants.ASSOCIATED_LINK_NAME_KEY;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.DISPOSITION_STATUS_COMPLETED;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.MANAGEMENT_OPERATION_KEY;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.PEEK_OPERATION_VALUE;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.REQUEST_RESPONSE_FROM_SEQUENCE_NUMBER;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.REQUEST_RESPONSE_MESSAGE_COUNT;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.SERVER_TIMEOUT;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.UPDATE_DISPOSITION_OPERATION;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.REQUEST_RESPONSE_LOCKTOKENS;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.REQUEST_RESPONSE_RENEWLOCK_OPERATION;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.REQUEST_RESPONSE_OK_STATUS_CODE;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.REQUEST_RESPONSE_EXPIRATIONS;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.REQUEST_RESPONSE_OPERATION_NAME;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.REQUEST_RESPONSE_TIMEOUT;

/**
 * Channel responsible for Service Bus related metadata, peek  and management plane operations. Management plane
 * operations increasing quotas, etc.
 */
public class ManagementChannel implements ServiceBusManagementNode {
    private final Scheduler scheduler;
    private final MessageSerializer messageSerializer;
    private final TokenManager tokenManager;
    private final Duration operationTimeout;
    private final Mono<RequestResponseChannel> createRequestResponse;
    private final String fullyQualifiedNamespace;
    private final ClientLogger logger;
    private final String entityPath;
    private final AtomicLong lastPeekedSequenceNumber = new AtomicLong();

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
     *
     * @return Mono that completes successfully when the message is completed. Otherwise, returns an error.
     */
    @Override
    public Mono<Void> complete(UUID lockToken) {
        return isAuthorized(UPDATE_DISPOSITION_OPERATION).then(createRequestResponse.flatMap(channel -> {
            final Message message = createDispositionMessage(new UUID[]{lockToken}, DISPOSITION_STATUS_COMPLETED,
                null, null, null, channel.getReceiveLinkName());
            return channel.sendWithAck(message);
        }).flatMap(response -> {
            final int statusCode = RequestResponseUtils.getResponseStatusCode(response);
            final AmqpResponseCode responseCode = AmqpResponseCode.fromValue(statusCode);
            if (responseCode == AmqpResponseCode.OK) {
                return Mono.empty();
            } else {
                return Mono.error(ExceptionUtil.amqpResponseCodeToException(statusCode, "", getErrorContext()));
            }
        }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Instant> renewMessageLock(ServiceBusReceivedMessage messageForLockRenew) {
        return renewMessageLock(new ServiceBusReceivedMessage[]{messageForLockRenew})
            .last()
            .publishOn(scheduler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ServiceBusReceivedMessage> peek(long fromSequenceNumber) {
        return peek(fromSequenceNumber, 1, null)
            .next()
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
        return isAuthorized(PEEK_OPERATION_VALUE).thenMany(createRequestResponse.flatMap(channel -> {
            final Message message = createManagementMessage(PEEK_OPERATION_VALUE, channel.getReceiveLinkName());

            // set mandatory properties on AMQP message body
            HashMap<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put(REQUEST_RESPONSE_FROM_SEQUENCE_NUMBER, fromSequenceNumber);
            requestBodyMap.put(REQUEST_RESPONSE_MESSAGE_COUNT, maxMessages);

            if (!Objects.isNull(sessionId)) {
                requestBodyMap.put(ManagementConstants.REQUEST_RESPONSE_SESSION_ID, sessionId);
            }

            message.setBody(new AmqpValue(requestBodyMap));

            return channel.sendWithAck(message);
        }).flatMapMany(amqpMessage -> {
            final List<ServiceBusReceivedMessage> messageList =
                messageSerializer.deserializeList(amqpMessage, ServiceBusReceivedMessage.class);

            // Assign the last sequence number so that we can peek from next time
            if (messageList.size() > 0) {
                final ServiceBusReceivedMessage receivedMessage = messageList.get(messageList.size() - 1);

                logger.info("Setting last peeked sequence number: {}", receivedMessage.getSequenceNumber());

                if (receivedMessage.getSequenceNumber() > 0) {
                    this.lastPeekedSequenceNumber.set(receivedMessage.getSequenceNumber());
                }
            }

            return Flux.fromIterable(messageList);
        }));
    }

    private Mono<Void> isAuthorized(String operation) {
        return tokenManager.getAuthorizationResults().next().flatMap(response -> {
            if (response != AmqpResponseCode.ACCEPTED) {
                return Mono.error(new AmqpException(false, String.format(
                    "User does not have authorization to perform operation [%s] on entity [%s]", operation, entityPath),
                    getErrorContext()));
            } else {
                return Mono.empty();
            }
        });
    }

    private Message createDispositionMessage(UUID[] lockTokens, String dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify, String linkName) {

        logger.verbose("Update disposition of deliveries '{}' to '{}' on entity '{}', session '{}'",
            Arrays.toString(lockTokens), dispositionStatus, entityPath, "n/a");

        final Message message = createManagementMessage(UPDATE_DISPOSITION_OPERATION, linkName);

        final Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(ManagementConstants.LOCK_TOKENS_KEY, lockTokens);
        requestBody.put(ManagementConstants.DISPOSITION_STATUS_KEY, dispositionStatus);

        if (deadLetterReason != null) {
            requestBody.put(ManagementConstants.DEADLETTER_REASON_KEY, deadLetterReason);
        }

        if (deadLetterErrorDescription != null) {
            requestBody.put(ManagementConstants.DEADLETTER_DESCRIPTION_KEY, deadLetterErrorDescription);
        }

        if (propertiesToModify != null && propertiesToModify.size() > 0) {
            requestBody.put(ManagementConstants.PROPERTIES_TO_MODIFY_KEY, propertiesToModify);
        }

        message.setBody(new AmqpValue(requestBody));

        return message;
    }

    /**
     * Creates an AMQP message with the required application properties.
     *
     * @param operation Management operation to perform (ie. peek, update-disposition, etc.)
     * @param linkName Name of receiver link associated with operation.
     *
     * @return An AMQP message with the required headers.
     */
    private Message createManagementMessage(String operation, String linkName) {
        final Duration serverTimeout = MessageUtils.adjustServerTimeout(operationTimeout);
        final Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(MANAGEMENT_OPERATION_KEY, operation);
        applicationProperties.put(SERVER_TIMEOUT, serverTimeout.toMillis());

        if (linkName != null && !linkName.isEmpty()) {
            applicationProperties.put(ASSOCIATED_LINK_NAME_KEY, linkName);
        }

        final Message message = Proton.message();
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));
        return message;
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(fullyQualifiedNamespace, entityPath);
    }

    private Flux<Instant> renewMessageLock(ServiceBusReceivedMessage[] messagesforRenewLock) {

        return  isAuthorized(PEEK_OPERATION_VALUE).thenMany(createRequestResponse.flatMap(requestResponseChannel -> {
            UUID[] lockTokens = Arrays.stream(messagesforRenewLock)
                .map(ServiceBusReceivedMessage::getLockToken)
                .toArray(UUID[]::new);
            Message requestMessage = createRequestMessageFromValueBody(REQUEST_RESPONSE_RENEWLOCK_OPERATION,
                Collections.singletonMap(REQUEST_RESPONSE_LOCKTOKENS, lockTokens),
                MessageUtils.adjustServerTimeout(Duration.ofSeconds(60)));
            return requestResponseChannel.sendWithAck(requestMessage);
        }).flatMapMany(responseMessage -> {
            int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
            List<Instant> expirationsForLocks = new ArrayList<>();
            if (statusCode ==  REQUEST_RESPONSE_OK_STATUS_CODE) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) ((AmqpValue) responseMessage
                    .getBody()).getValue();
                Object expirationListObj = responseBody.get(REQUEST_RESPONSE_EXPIRATIONS);

                if (expirationListObj instanceof  Date[]) {
                    Date[] expirations = (Date[]) expirationListObj;
                    expirationsForLocks =  Arrays.stream(expirations)
                        .map(Date::toInstant)
                        .collect(Collectors.toList());
                }
            }
            return Flux.fromIterable(expirationsForLocks);
        }));
    }

    private Message createRequestMessageFromValueBody(String operation, Object valueBody, Duration timeout) {
        Message requestMessage = Message.Factory.create();
        requestMessage.setBody(new AmqpValue(valueBody));
        HashMap<String, Object> applicationPropertiesMap = new HashMap<>();
        applicationPropertiesMap.put(REQUEST_RESPONSE_OPERATION_NAME, operation);
        applicationPropertiesMap.put(REQUEST_RESPONSE_TIMEOUT, timeout.toMillis());

        requestMessage.setApplicationProperties(new ApplicationProperties(applicationPropertiesMap));
        return requestMessage;
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
