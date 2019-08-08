// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.RetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.amqp.exception.ExceptionUtil;
import com.azure.core.amqp.exception.OperationCancelledException;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.handler.SendLinkHandler;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.messaging.eventhubs.implementation.EventDataUtil.getDataSerializedSize;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Handles scheduling and transmitting events through proton-j to Event Hubs service.
 */
class ReactorSender extends EndpointStateNotifierBase implements AmqpSendLink {
    private final String entityPath;
    private final Sender sender;
    private final SendLinkHandler handler;
    private final ReactorProvider reactorProvider;
    private final Disposable.Composite subscriptions;

    private final AtomicBoolean hasConnected = new AtomicBoolean();
    private final AtomicBoolean hasAuthorized = new AtomicBoolean(true);
    private final AtomicInteger retryAttempts = new AtomicInteger();

    private final Object pendingSendLock = new Object();
    private final ConcurrentHashMap<String, RetriableWorkItem> pendingSendsMap = new ConcurrentHashMap<>();
    private final PriorityQueue<WeightedDeliveryTag> pendingSendsQueue = new PriorityQueue<>(1000, new DeliveryTagComparator());

    private final ActiveClientTokenManager tokenManager;
    private final RetryPolicy retry;
    private final Duration timeout;
    private final Timer sendTimeoutTimer = new Timer("SendTimeout-timer");

    private final Object errorConditionLock = new Object();

    private volatile Exception lastKnownLinkError;
    private volatile Instant lastKnownErrorReportedAt;

    /**
     * Max message size can change from its initial value. When the send link is opened, we query for the remote link
     * capacity.
     */
    private volatile int maxMessageSize;

    ReactorSender(String entityPath, Sender sender, SendLinkHandler handler, ReactorProvider reactorProvider,
                  ActiveClientTokenManager tokenManager, Duration timeout, RetryPolicy retry, int maxMessageSize) {
        super(new ClientLogger(ReactorSender.class));
        this.entityPath = entityPath;
        this.sender = sender;
        this.handler = handler;
        this.reactorProvider = reactorProvider;
        this.tokenManager = tokenManager;
        this.retry = retry;
        this.timeout = timeout;
        this.maxMessageSize = maxMessageSize;

        this.subscriptions = Disposables.composite(
            handler.getDeliveredMessages().subscribe(this::processDeliveredMessage),

            handler.getLinkCredits().subscribe(credit -> {
                logger.verbose("Credits on link: {}", credit);
                this.scheduleWorkOnDispatcher();
            }),

            handler.getEndpointStates().subscribe(
                state -> {
                    this.hasConnected.set(state == EndpointState.ACTIVE);
                    this.notifyEndpointState(state);
                },
                error -> logger.error("Error encountered getting endpointState", error),
                () -> {
                    logger.verbose("getLinkCredits completed.");
                    hasConnected.set(false);
                }),

            tokenManager.getAuthorizationResults().subscribe(
                response -> {
                    logger.verbose("Token refreshed: {}", response);
                    hasAuthorized.set(true);
                },
                error -> {
                    logger.info("clientId[{}], path[{}], linkName[{}] - tokenRenewalFailure[{}]",
                        handler.getConnectionId(), this.entityPath, getLinkName(), error.getMessage());
                    hasAuthorized.set(false);
                }, () -> hasAuthorized.set(false))
        );
    }

    @Override
    public Mono<Void> send(Message message) {
        final int payloadSize = getDataSerializedSize(message);
        final int allocationSize = Math.min(payloadSize + ClientConstants.MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES, maxMessageSize);
        final byte[] bytes = new byte[allocationSize];

        int encodedSize;
        try {
            encodedSize = message.encode(bytes, 0, allocationSize);
        } catch (BufferOverflowException exception) {
            final String errorMessage = String.format(Locale.US, "Error sending. Size of the payload exceeded maximum message size: %s kb", maxMessageSize / 1024);
            final Throwable error = new AmqpException(false, ErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, errorMessage,
                exception, handler.getErrorContext(sender));

            return Mono.error(error);
        }

        return send(bytes, encodedSize, DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
    }

    @Override
    public Mono<Void> send(List<Message> messageBatch) {
        if (messageBatch.size() == 1) {
            return send(messageBatch.get(0));
        }

        final Message firstMessage = messageBatch.get(0);

        // proton-j doesn't support multiple dataSections to be part of AmqpMessage
        // here's the alternate approach provided by them: https://github.com/apache/qpid-proton/pull/54
        final Message batchMessage = Proton.message();
        batchMessage.setMessageAnnotations(firstMessage.getMessageAnnotations());

        final int maxMessageSizeTemp = this.maxMessageSize;

        final byte[] bytes = new byte[maxMessageSizeTemp];
        int encodedSize = batchMessage.encode(bytes, 0, maxMessageSizeTemp);
        int byteArrayOffset = encodedSize;

        for (final Message amqpMessage : messageBatch) {
            final Message messageWrappedByData = Proton.message();

            int payloadSize = getDataSerializedSize(amqpMessage);
            int allocationSize = Math.min(payloadSize + ClientConstants.MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES, maxMessageSizeTemp);

            byte[] messageBytes = new byte[allocationSize];
            int messageSizeBytes = amqpMessage.encode(messageBytes, 0, allocationSize);
            messageWrappedByData.setBody(new Data(new Binary(messageBytes, 0, messageSizeBytes)));

            try {
                encodedSize = messageWrappedByData.encode(bytes, byteArrayOffset, maxMessageSizeTemp - byteArrayOffset - 1);
            } catch (BufferOverflowException exception) {
                final String message = String.format(Locale.US, "Size of the payload exceeded maximum message size: %s kb", maxMessageSizeTemp / 1024);
                final AmqpException error = new AmqpException(false, ErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, message,
                    exception, handler.getErrorContext(sender));

                return Mono.error(error);
            }

            byteArrayOffset = byteArrayOffset + encodedSize;
        }

        return send(bytes, byteArrayOffset, AmqpConstants.AMQP_BATCH_MESSAGE_FORMAT);
    }

    @Override
    public ErrorContext getErrorContext() {
        return handler.getErrorContext(sender);
    }

    @Override
    public String getLinkName() {
        return sender.getName();
    }

    @Override
    public String getEntityPath() {
        return entityPath;
    }

    @Override
    public Mono<Integer> getLinkSize() {
        if (this.hasConnected.get() && this.maxMessageSize > 0) {
            return Mono.just(maxMessageSize);
        }

        return RetryUtil.withRetry(
            handler.getEndpointStates().takeUntil(state -> state == EndpointState.ACTIVE),
            timeout, retry)
            .then(Mono.fromCallable(() -> {
                final UnsignedLong remoteMaxMessageSize = sender.getRemoteMaxMessageSize();

                if (remoteMaxMessageSize != null) {
                    this.maxMessageSize = remoteMaxMessageSize.intValue();
                }

                return this.maxMessageSize;
            }));
    }

    @Override
    public void close() {
        subscriptions.dispose();
        tokenManager.close();
        super.close();
    }

    private Mono<Void> send(byte[] bytes, int arrayOffset, int messageFormat) {
        Mono<Void> sendWorkItem = Mono.create(sink -> {
            send(new RetriableWorkItem(bytes, arrayOffset, messageFormat, sink, timeout));
        });

        if (hasConnected.get()) {
            return sendWorkItem;
        } else {
            return RetryUtil.withRetry(
                handler.getEndpointStates().takeUntil(state -> state == EndpointState.ACTIVE),
                timeout, retry)
                .then(sendWorkItem);
        }
    }

    private void send(RetriableWorkItem workItem) {
        final String deliveryTag = UUID.randomUUID().toString().replace("-", "");

        synchronized (pendingSendLock) {
            this.pendingSendsMap.put(deliveryTag, workItem);
            this.pendingSendsQueue.offer(new WeightedDeliveryTag(deliveryTag, workItem.hasBeenRetried() ? 1 : 0));
        }

        this.scheduleWorkOnDispatcher();
    }

    /**
     * Invokes work on the Reactor. Should only be called from ReactorDispatcher.invoke()
     */
    private void processSendWork() {
        if (!hasConnected.get()) {
            logger.warning("Not connected. Not processing send work.");
            return;
        }

        while (hasConnected.get() && sender.getCredit() > 0) {
            final WeightedDeliveryTag weightedDelivery;
            final RetriableWorkItem workItem;
            final String deliveryTag;
            synchronized (pendingSendLock) {
                weightedDelivery = this.pendingSendsQueue.poll();
                if (weightedDelivery != null) {
                    deliveryTag = weightedDelivery.getDeliveryTag();
                    workItem = this.pendingSendsMap.get(deliveryTag);
                } else {
                    workItem = null;
                    deliveryTag = null;
                }
            }

            if (workItem == null) {
                if (deliveryTag != null) {
                    logger.verbose("clientId[{}]. path[{}], linkName[{}], deliveryTag[{}]: sendData not found for this delivery.",
                        handler.getConnectionId(), entityPath, getLinkName(), deliveryTag);
                }

                //TODO (conniey): Should we update to continue rather than break?
                break;
            }

            Delivery delivery = null;
            boolean linkAdvance = false;
            int sentMsgSize = 0;
            Exception sendException = null;

            try {
                delivery = sender.delivery(deliveryTag.getBytes(UTF_8));
                delivery.setMessageFormat(workItem.messageFormat());

                sentMsgSize = sender.send(workItem.message(), 0, workItem.encodedMessageSize());
                assert sentMsgSize == workItem.encodedMessageSize() : "Contract of the ProtonJ library for Sender.Send API changed";

                linkAdvance = sender.advance();
            } catch (Exception exception) {
                sendException = exception;
            }

            if (linkAdvance) {
                logger.verbose("entityPath[{}], linkName[{}], deliveryTag[{}]: Sent message", entityPath, getLinkName(), deliveryTag);

                workItem.setIsWaitingForAck();
                sendTimeoutTimer.schedule(new SendTimeout(deliveryTag), timeout.toMillis());
            } else {
                logger.verbose(
                    "clientId[{}]. path[{}], linkName[{}], deliveryTag[{}], sentMessageSize[{}], payloadActualSize[{}]: sendlink advance failed",
                    handler.getConnectionId(), entityPath, getLinkName(), deliveryTag, sentMsgSize, workItem.encodedMessageSize());

                if (delivery != null) {
                    delivery.free();
                }

                final ErrorContext context = handler.getErrorContext(sender);
                final Throwable exception = sendException != null
                    ? new OperationCancelledException(String.format(Locale.US, "Entity(%s): send operation failed. Please see cause for more details", entityPath), sendException, context)
                    : new OperationCancelledException(String.format(Locale.US, "Entity(%s): send operation failed while advancing delivery(tag: %s).", entityPath, deliveryTag), context);

                workItem.sink().error(exception);
            }
        }
    }

    private void processDeliveredMessage(Delivery delivery) {
        final DeliveryState outcome = delivery.getRemoteState();
        final String deliveryTag = new String(delivery.getTag(), UTF_8);

        logger.verbose("entityPath[{}], clinkName[{}], deliveryTag[{}]: process delivered message",
            entityPath, getLinkName(), deliveryTag);

        final RetriableWorkItem workItem = pendingSendsMap.remove(deliveryTag);

        if (workItem == null) {
            logger.verbose("clientId[{}]. path[{}], linkName[{}], delivery[{}] - mismatch (or send timed out)",
                handler.getConnectionId(), entityPath, getLinkName(), deliveryTag);
            return;
        }

        if (outcome instanceof Accepted) {
            synchronized (errorConditionLock) {
                lastKnownLinkError = null;
                retryAttempts.set(0);
            }

            workItem.sink().success();
        } else if (outcome instanceof Rejected) {
            final Rejected rejected = (Rejected) outcome;
            final org.apache.qpid.proton.amqp.transport.ErrorCondition error = rejected.getError();
            final Exception exception = ExceptionUtil.toException(error.getCondition().toString(),
                error.getDescription(), handler.getErrorContext(sender));

            final int retryAttempt;
            if (isGeneralSendError(error.getCondition())) {
                synchronized (errorConditionLock) {
                    lastKnownLinkError = exception;
                    retryAttempt = retryAttempts.incrementAndGet();
                }
            } else {
                retryAttempt = retryAttempts.get();
            }

            final Duration retryInterval = retry.calculateRetryDelay(exception, retryAttempt);

            if (retryInterval.compareTo(workItem.timeoutTracker().remaining()) > 0) {
                cleanupFailedSend(workItem, exception);
            } else {
                workItem.lastKnownException(exception);
                try {
                    reactorProvider.getReactorDispatcher().invoke(() -> send(workItem), retryInterval);
                } catch (IOException | RejectedExecutionException schedulerException) {
                    exception.initCause(schedulerException);
                    cleanupFailedSend(
                        workItem,
                        new AmqpException(false,
                            String.format(Locale.US, "Entity(%s): send operation failed while scheduling a"
                                + " retry on Reactor, see cause for more details.", entityPath),
                            schedulerException, handler.getErrorContext(sender)));
                }
            }
        } else if (outcome instanceof Released) {
            cleanupFailedSend(workItem, new OperationCancelledException(outcome.toString(),
                handler.getErrorContext(sender)));
        } else {
            cleanupFailedSend(workItem, new AmqpException(false, outcome.toString(),
                handler.getErrorContext(sender)));
        }
    }

    private void scheduleWorkOnDispatcher() {
        try {
            reactorProvider.getReactorDispatcher().invoke(this::processSendWork);
        } catch (IOException e) {
            logger.error("Error scheduling work on reactor.", e);
            notifyError(e);
        }
    }

    private void cleanupFailedSend(final RetriableWorkItem workItem, final Exception exception) {
        //TODO (conniey): is there some timeout task I should handle?
        workItem.sink().error(exception);
    }

    private static boolean isGeneralSendError(Symbol amqpError) {
        return (amqpError == AmqpErrorCode.SERVER_BUSY_ERROR || amqpError == AmqpErrorCode.TIMEOUT_ERROR
            || amqpError == AmqpErrorCode.RESOURCE_LIMIT_EXCEEDED);
    }

    private static class WeightedDeliveryTag {
        private final String deliveryTag;
        private final int priority;

        WeightedDeliveryTag(final String deliveryTag, final int priority) {
            this.deliveryTag = deliveryTag;
            this.priority = priority;
        }

        private String getDeliveryTag() {
            return this.deliveryTag;
        }

        private int getPriority() {
            return this.priority;
        }
    }

    private static class DeliveryTagComparator implements Comparator<WeightedDeliveryTag>, Serializable {
        private static final long serialVersionUID = -7057500582037295635L;

        @Override
        public int compare(WeightedDeliveryTag deliveryTag0, WeightedDeliveryTag deliveryTag1) {
            return deliveryTag1.getPriority() - deliveryTag0.getPriority();
        }
    }

    /**
     * Keeps track of Messages that have been sent, but may not have been acknowledged by the service.
     */
    private class SendTimeout extends TimerTask {
        private final String deliveryTag;

        SendTimeout(String deliveryTag) {
            this.deliveryTag = deliveryTag;
        }

        @Override
        public void run() {
            final RetriableWorkItem workItem = pendingSendsMap.remove(deliveryTag);
            if (workItem == null) {
                return;
            }

            Exception exceptionUsed = lastKnownLinkError;
            final Exception lastError;
            final Instant lastErrorTime;

            synchronized (errorConditionLock) {
                lastError = lastKnownLinkError;
                lastErrorTime = lastKnownErrorReportedAt;
            }

            if (lastError != null) {
                final Instant now = Instant.now();
                final Instant duration = now.minusSeconds(ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS);
                final boolean isServerBusy = (lastError instanceof AmqpException) && lastErrorTime.isAfter(duration);

                final Instant timedOut = now.minusMillis(timeout.toMillis());
                exceptionUsed = isServerBusy || lastErrorTime.isAfter(timedOut)
                    ? lastError
                    : null;
            }

            // If it is a type of AmqpException, we received this error from the service, otherwise, it is a client-side
            // error.
            final AmqpException exception;
            if (exceptionUsed instanceof AmqpException) {
                exception = (AmqpException) exceptionUsed;
            } else {
                exception = new AmqpException(true, ErrorCondition.TIMEOUT_ERROR,
                    String.format(Locale.US, "Entity(%s): Send operation timed out", entityPath),
                    handler.getErrorContext(sender));
            }

            workItem.sink().error(exception);
        }
    }
}
