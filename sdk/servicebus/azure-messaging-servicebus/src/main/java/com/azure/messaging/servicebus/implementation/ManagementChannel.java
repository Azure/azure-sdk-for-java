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
import reactor.core.Exceptions;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Channel responsible for Service Bus related metadata, peek  and management plane operations. Management plane
 * operations increasing quotas, etc.
 */
public class ManagementChannel implements ServiceBusManagementNode {
    private final MessageSerializer messageSerializer;
    private final TokenManager tokenManager;
    private final Duration operationTimeout;
    private final Mono<RequestResponseChannel> createChannel;
    private final String fullyQualifiedNamespace;
    private final ClientLogger logger;
    private final String entityPath;
    private final AtomicLong lastPeekedSequenceNumber = new AtomicLong();
    private final String sessionId;
    private final boolean isSessionEnabled;

    private volatile boolean isDisposed;

    ManagementChannel(Mono<RequestResponseChannel> createChannel, String fullyQualifiedNamespace,
        String entityPath, String sessionId, TokenManager tokenManager, MessageSerializer messageSerializer,
        Duration operationTimeout) {
        this.createChannel = Objects.requireNonNull(createChannel, "'createChannel' cannot be null.");
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.logger = new ClientLogger(String.format("%s<%s>", ManagementChannel.class, entityPath));
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.tokenManager = Objects.requireNonNull(tokenManager, "'tokenManager' cannot be null.");
        this.operationTimeout = Objects.requireNonNull(operationTimeout, "'operationTimeout' cannot be null.");

        this.sessionId = sessionId;
        this.isSessionEnabled = !CoreUtils.isNullOrEmpty(sessionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> cancelScheduledMessage(long sequenceNumber) {
        return isAuthorized(ManagementConstants.OPERATION_CANCEL_SCHEDULED_MESSAGE)
            .then(createChannel.flatMap(channel -> {
                final Message requestMessage = createManagementMessage(
                    ManagementConstants.OPERATION_CANCEL_SCHEDULED_MESSAGE, channel.getReceiveLinkName());

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
    public Mono<ServiceBusReceivedMessage> peek() {
        return peek(lastPeekedSequenceNumber.get() + 1);
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

        return isAuthorized(ManagementConstants.OPERATION_RECEIVE_BY_SEQUENCE_NUMBER)
            .thenMany(createChannel.flatMap(channel -> {
                final Message message = createManagementMessage(
                    ManagementConstants.OPERATION_RECEIVE_BY_SEQUENCE_NUMBER, channel.getReceiveLinkName());

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
    public Mono<Instant> renewMessageLock(UUID lockToken) {
        return isAuthorized(ManagementConstants.OPERATION_PEEK).then(createChannel.flatMap(channel -> {
            final Message requestMessage = createManagementMessage(ManagementConstants.OPERATION_RENEW_LOCK,
                channel.getReceiveLinkName());
            final Map<String, Object> requestBody = new HashMap<>();

            requestBody.put(ManagementConstants.LOCK_TOKENS_KEY, new UUID[]{lockToken});

            if (isSessionEnabled) {
                requestBody.put(ManagementConstants.SESSION_ID, sessionId);
            }

            requestMessage.setBody(new AmqpValue(requestBody));

            return channel.sendWithAck(requestMessage);
        }).map(responseMessage -> {
            int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
            if (statusCode != AmqpResponseCode.OK.getValue()) {

                throw logger.logExceptionAsError(Exceptions.propagate(new AmqpException(false,
                    String.format("Could not renew the lock for lock token: '%s'.", lockToken.toString()),
                    getErrorContext())));
            }
            List<Instant> renewTimeList = messageSerializer.deserializeList(responseMessage, Instant.class);
            if (CoreUtils.isNullOrEmpty(renewTimeList)) {
                throw logger.logExceptionAsError(Exceptions.propagate(new AmqpException(false, String.format(
                    "Service bus response empty. Could not renew message with lock token: '%s'.", lockToken),
                    getErrorContext())));
            }

            return renewTimeList.get(0);
        }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Long> schedule(ServiceBusMessage message, Instant scheduledEnqueueTime, int maxLinkSize) {
        message.setScheduledEnqueueTime(scheduledEnqueueTime);

        return isAuthorized(ManagementConstants.OPERATION_SCHEDULE_MESSAGE).then(createChannel.flatMap(channel -> {
            // Serialize the request.
            final Message amqpMessage = messageSerializer.serialize(message);

            // The maxsize allowed logic is from ReactorSender, this logic should be kept in sync.
            final int payloadSize = messageSerializer.getSize(amqpMessage);
            final int allocationSize =
                Math.min(payloadSize + ManagementConstants.MAX_MESSAGING_AMQP_HEADER_SIZE_BYTES, maxLinkSize);
            final byte[] bytes = new byte[allocationSize];

            int encodedSize;
            try {
                encodedSize = amqpMessage.encode(bytes, 0, allocationSize);
            } catch (BufferOverflowException exception) {
                final String errorMessage = String.format(
                    "Error sending. Size of the payload exceeded maximum message size: %s kb", maxLinkSize / 1024);
                return monoError(logger, Exceptions.propagate(new AmqpException(false,
                    AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, errorMessage, exception, getErrorContext())));
            }

            final HashMap<String, Object> messageEntry = new HashMap<>();
            messageEntry.put(ManagementConstants.MESSAGE, new Binary(bytes, 0, encodedSize));
            messageEntry.put(ManagementConstants.MESSAGE_ID, amqpMessage.getMessageId());

            final String sessionId = amqpMessage.getGroupId();
            if (!CoreUtils.isNullOrEmpty(sessionId)) {
                messageEntry.put(ManagementConstants.SESSION_ID, sessionId);
            }

            final String partitionKey = message.getPartitionKey();
            if (!CoreUtils.isNullOrEmpty(partitionKey)) {
                messageEntry.put(ManagementConstants.PARTITION_KEY, partitionKey);
            }

            final String viaPartitionKey = message.getViaPartitionKey();
            if (!CoreUtils.isNullOrEmpty(viaPartitionKey)) {
                messageEntry.put(ManagementConstants.VIA_PARTITION_KEY, viaPartitionKey);
            }

            final Collection<HashMap<String, Object>> messageList = new LinkedList<>();
            messageList.add(messageEntry);

            final Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put(ManagementConstants.MESSAGES, messageList);

            final Message requestMessage = createManagementMessage(ManagementConstants.OPERATION_SCHEDULE_MESSAGE,
                channel.getReceiveLinkName());

            requestMessage.setBody(new AmqpValue(requestBodyMap));

            return channel.sendWithAck(requestMessage);
        }).map(responseMessage -> {
            final int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);

            if (statusCode != AmqpResponseCode.OK.getValue()) {
                throw logger.logExceptionAsError(Exceptions.propagate(new AmqpException(false, String.format(
                    "Could not schedule message with message id: '%s'.", message.getMessageId()), getErrorContext())));
            }

            final List<Long> sequenceNumbers = messageSerializer.deserializeList(responseMessage, Long.class);
            if (CoreUtils.isNullOrEmpty(sequenceNumbers)) {
                throw logger.logExceptionAsError(Exceptions.propagate(new AmqpException(false, String.format(
                    "Service Bus response was empty. Could not schedule message with message id: '%s'.",
                    message.getMessageId()), getErrorContext())));
            }

            return sequenceNumbers.get(0);
        }));
    }

    @Override
    public Mono<Void> updateDisposition(String lockToken, DispositionStatus dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {

        final UUID token = UUID.fromString(lockToken);
        return isAuthorized(ManagementConstants.OPERATION_UPDATE_DISPOSITION).then(createChannel.flatMap(channel -> {
            final Message message = createDispositionMessage(new UUID[]{token}, dispositionStatus,
                deadLetterReason, deadLetterErrorDescription, propertiesToModify, channel.getReceiveLinkName());

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
    public void close() {
        if (isDisposed) {
            return;
        }

        isDisposed = true;
        tokenManager.close();
    }

    private Flux<ServiceBusReceivedMessage> peek(long fromSequenceNumber, int maxMessages) {
        return isAuthorized(ManagementConstants.OPERATION_PEEK).thenMany(createChannel.flatMap(channel -> {
            final Message message = createManagementMessage(ManagementConstants.OPERATION_PEEK,
                channel.getReceiveLinkName());

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

        final Message message = createManagementMessage(ManagementConstants.OPERATION_UPDATE_DISPOSITION, linkName);

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

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(fullyQualifiedNamespace, entityPath);
    }
}
