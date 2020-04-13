// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.RequestResponseUtils;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.BufferOverflowException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Channel responsible for Service Bus related metadata, peek  and management plane operations. Management plane
 * operations increasing quotas, etc.
 */
public class ManagementChannel implements ServiceBusManagementNode {
    private final MessageSerializer messageSerializer;
    private final TokenManager tokenManager;
    private final Duration operationTimeout;
    private final Mono<RequestResponseChannel> createRequestResponse;
    private final String fullyQualifiedNamespace;
    private final ClientLogger logger;
    private final String entityPath;
    private final AtomicLong lastPeekedSequenceNumber = new AtomicLong();
    private final String sessionId;
    private final boolean isSessionEnabled;

    private volatile boolean isDisposed;

    ManagementChannel(Mono<RequestResponseChannel> createRequestResponse, String fullyQualifiedNamespace,
        String entityPath, String sessionId, TokenManager tokenManager, MessageSerializer messageSerializer,
        Duration operationTimeout) {
        this.createRequestResponse = createRequestResponse;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.logger = new ClientLogger(String.format("%s<%s>", ManagementChannel.class, entityPath));
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.tokenManager = Objects.requireNonNull(tokenManager, "'tokenManager' cannot be null.");
        this.operationTimeout = Objects.requireNonNull(operationTimeout, "'operationTimeout' cannot be null.");
        this.sessionId = Objects.requireNonNull(sessionId, "'sessionId' cannot be null.");
        this.isSessionEnabled = !Objects.isNull(sessionId);
    }

    @Override
    public Mono<Void> updateDisposition(String lockToken, DispositionStatus dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {

        final UUID token = UUID.fromString(lockToken);
        return isAuthorized(ManagementConstants.UPDATE_DISPOSITION_OPERATION).then(createRequestResponse.flatMap(channel -> {
            final Message message = createDispositionMessage(new UUID[]{token}, dispositionStatus,
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
    public Mono<ServiceBusReceivedMessage> peek(long fromSequenceNumber) {
        return peek(fromSequenceNumber, 1)
            .last();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages) {

        return peek(this.lastPeekedSequenceNumber.get() + 1, maxMessages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages, long fromSequenceNumber) {

        return peek(fromSequenceNumber, maxMessages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ServiceBusReceivedMessage> receiveDeferredMessage(ReceiveMode receiveMode, long sequenceNumber) {
        return receiveDeferredMessageBatch(receiveMode, sequenceNumber)
            .next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<ServiceBusReceivedMessage> receiveDeferredMessageBatch(ReceiveMode receiveMode,
        long... sequenceNumbers) {

        return isAuthorized(ManagementConstants.RECEIVE_BY_SEQUENCE_NUMBER_OPERATION)
            .thenMany(createRequestResponse.flatMap(channel -> {
                final Message message = createManagementMessage(
                    ManagementConstants.RECEIVE_BY_SEQUENCE_NUMBER_OPERATION, channel.getReceiveLinkName());

                // set mandatory properties on AMQP message body
                final HashMap<String, Object> requestBodyMap = new HashMap<>();

                requestBodyMap.put(ManagementConstants.SEQUENCE_NUMBERS, Arrays.stream(sequenceNumbers)
                    .boxed().toArray(Long[]::new));

                requestBodyMap.put(ManagementConstants.RECEIVER_SETTLE_MODE,
                    UnsignedInteger.valueOf(receiveMode == ReceiveMode.RECEIVE_AND_DELETE ? 0 : 1));

                if (isSessionEnabled) {
                    requestBodyMap.put(ManagementConstants.SESSION_ID, sessionId);
                }

                message.setBody(new AmqpValue(requestBodyMap));

                return channel.sendWithAck(message);
            }).flatMapMany(amqpMessage -> {
                final List<ServiceBusReceivedMessage> messageList =
                    messageSerializer.deserializeList(amqpMessage, ServiceBusReceivedMessage.class);

                return Flux.fromIterable(messageList);
            }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ServiceBusReceivedMessage> peek() {
        return peek(lastPeekedSequenceNumber.get() + 1);
    }

    private Flux<ServiceBusReceivedMessage> peek(long fromSequenceNumber, int maxMessages) {
        return isAuthorized(ManagementConstants.PEEK_OPERATION).thenMany(createRequestResponse.flatMap(channel -> {
            final Message message = createManagementMessage(ManagementConstants.PEEK_OPERATION, channel.getReceiveLinkName());

            // set mandatory properties on AMQP message body
            final HashMap<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put(ManagementConstants.FROM_SEQUENCE_NUMBER, fromSequenceNumber);
            requestBodyMap.put(ManagementConstants.MESSAGE_COUNT_KEY, maxMessages);

            if (isSessionEnabled) {
                requestBodyMap.put(ManagementConstants.SESSION_ID, sessionId);
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

    private Message createDispositionMessage(UUID[] lockTokens, DispositionStatus dispositionStatus,
        String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify,
        String linkName) {

        logger.verbose("Update disposition of deliveries '{}' to '{}' on entity '{}', session '{}'",
            Arrays.toString(lockTokens), dispositionStatus, entityPath, "n/a");

        final Message message = createManagementMessage(ManagementConstants.UPDATE_DISPOSITION_OPERATION, linkName);

        final Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(ManagementConstants.LOCK_TOKENS_KEY, lockTokens);
        requestBody.put(ManagementConstants.DISPOSITION_STATUS_KEY, dispositionStatus.getValue());

        if (deadLetterReason != null) {
            requestBody.put(ManagementConstants.DEADLETTER_REASON_KEY, deadLetterReason);
        }

        if (deadLetterErrorDescription != null) {
            requestBody.put(ManagementConstants.DEADLETTER_DESCRIPTION_KEY, deadLetterErrorDescription);
        }

        if (propertiesToModify != null && propertiesToModify.size() > 0) {
            requestBody.put(ManagementConstants.PROPERTIES_TO_MODIFY_KEY, propertiesToModify);
        }

        if (isSessionEnabled) {
            requestBody.put(ManagementConstants.SESSION_ID, sessionId);
        }

        message.setBody(new AmqpValue(requestBody));

        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Instant> renewMessageLock(UUID lockToken) {
        return isAuthorized(ManagementConstants.PEEK_OPERATION).then(createRequestResponse.flatMap(channel -> {

            Message requestMessage = createManagementMessage(ManagementConstants.RENEW_LOCK_OPERATION,
                channel.getReceiveLinkName());

            requestMessage.setBody(new AmqpValue(Collections.singletonMap(ManagementConstants.LOCK_TOKENS_KEY, new UUID[]{lockToken})));
            return channel.sendWithAck(requestMessage);
        }).map(responseMessage -> {
            int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
            if (statusCode != AmqpResponseCode.OK.getValue()) {

                throw logger.logExceptionAsError(new AmqpException(false,
                    String.format("Could not renew the lock for lock token: '%s'.", lockToken.toString()),
                    getErrorContext()));
            }
            List<Instant> renewTimeList = messageSerializer.deserializeList(responseMessage, Instant.class);
            if (CoreUtils.isNullOrEmpty(renewTimeList)) {
                throw logger.logExceptionAsError(new AmqpException(false,
                    String.format("Service bus response empty. "
                        + "Could not renew message with lock token: '%s'.", lockToken.toString()),
                    getErrorContext()));
            }
            return renewTimeList.get(0);
        }));
    }

    /**
     * Creates an AMQP message with the required application properties.
     *
     * @param operation Management operation to perform (ie. peek, update-disposition, etc.)
     * @param linkName Name of receiver link associated with operation.
     * @return An AMQP message with the required headers.
     */
    private Message createManagementMessage(String operation, String linkName) {
        final Duration serverTimeout = MessageUtils.adjustServerTimeout(operationTimeout);
        final Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(ManagementConstants.MANAGEMENT_OPERATION_KEY, operation);
        applicationProperties.put(ManagementConstants.SERVER_TIMEOUT, serverTimeout.toMillis());

        if (linkName != null && !linkName.isEmpty()) {
            applicationProperties.put(ManagementConstants.ASSOCIATED_LINK_NAME_KEY, linkName);
        }

        final Message message = Proton.message();
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));

        return message;
    }

    /**
     * Create a Amqp key, value map to be used to create Amqp mesage for scheduling purpose.
     *
     * @param messageToSchedule The message which needs to be scheduled.
     * @param maxMessageSize The maximum size allowed on send link.
     * @return Map of key and value in Amqp format.
     * @throws AmqpException When payload exceeded maximum message allowed size.
     */
    private Map<String, Object> createScheduleMessgeAmqpValue(ServiceBusMessage messageToSchedule, int maxMessageSize) {

        Message message = messageSerializer.serialize(messageToSchedule);

        // The maxsize allowed logic is from ReactorSender, this logic should be kept in sync.
        final int payloadSize = messageSerializer.getSize(message);
        final int allocationSize =
            Math.min(payloadSize + ManagementConstants.MAX_MESSAGING_AMQP_HEADER_SIZE_BYTES, maxMessageSize);
        final byte[] bytes = new byte[allocationSize];

        int encodedSize;
        try {
            encodedSize = message.encode(bytes, 0, allocationSize);
        } catch (BufferOverflowException exception) {
            final String errorMessage =
                String.format(Locale.US,
                    "Error sending. Size of the payload exceeded maximum message size: %s kb",
                    maxMessageSize / 1024);
            throw logger.logExceptionAsWarning(new AmqpException(false,
                AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, errorMessage, exception, getErrorContext()));
        }
        HashMap<String, Object> messageEntry = new HashMap<>();
        messageEntry.put(ManagementConstants.MESSAGE, new Binary(bytes, 0, encodedSize));
        messageEntry.put(ManagementConstants.MESSAGE_ID, message.getMessageId());

        Collection<HashMap<String, Object>> messageList = new LinkedList<>();
        messageList.add(messageEntry);

        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put(ManagementConstants.MESSAGES, messageList);
        return requestBodyMap;
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(fullyQualifiedNamespace, entityPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> cancelScheduledMessage(long sequenceNumber) {
        return isAuthorized(ManagementConstants.CANCEL_SCHEDULED_MESSAGE_OPERATION).then(createRequestResponse.flatMap(channel -> {

            Message requestMessage = createManagementMessage(ManagementConstants.CANCEL_SCHEDULED_MESSAGE_OPERATION,
                channel.getReceiveLinkName());

            requestMessage.setBody(new AmqpValue(Collections.singletonMap(ManagementConstants.SEQUENCE_NUMBERS,
                new Long[]{sequenceNumber})));
            return channel.sendWithAck(requestMessage);
        }).map(responseMessage -> {
            int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);

            if (statusCode == AmqpResponseCode.OK.getValue()) {
                return Mono.empty();
            }
            return Mono.error(new AmqpException(false, "Could not cancel scheduled message with sequence number "
                + sequenceNumber, getErrorContext()));
        })).then();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Long> schedule(ServiceBusMessage messageToSchedule, Instant scheduledEnqueueTime, int maxSendLinkSize) {
        messageToSchedule.setScheduledEnqueueTime(scheduledEnqueueTime);
        return isAuthorized(ManagementConstants.SCHEDULE_MESSAGE_OPERATION).then(createRequestResponse.flatMap(channel -> {

            Message requestMessage = createManagementMessage(ManagementConstants.SCHEDULE_MESSAGE_OPERATION, channel.getReceiveLinkName());
            Map<String, Object> requestBodyMap = createScheduleMessgeAmqpValue(messageToSchedule, maxSendLinkSize);

            requestMessage.setBody(new AmqpValue(requestBodyMap));
            return channel.sendWithAck(requestMessage);
        }).map(responseMessage -> {
            int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);

            if (statusCode != AmqpResponseCode.OK.getValue()) {
                throw logger.logExceptionAsError(new AmqpException(false,
                    String.format("Could not schedule message with message id: '%s'.",
                        messageToSchedule.getMessageId()), getErrorContext()));
            }

            List<Long> sequenceNumberList = messageSerializer.deserializeList(responseMessage, Long.class);
            if (CoreUtils.isNullOrEmpty(sequenceNumberList)) {
                throw logger.logExceptionAsError(new AmqpException(false,
                    String.format("Service bus response empty. Could not schedule message with message id: '%s'.",
                        messageToSchedule.getMessageId()), getErrorContext()));
            }
            return sequenceNumberList.get(0);
        }));
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
