// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.OperationCancelledException;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.util.logging.ClientLogger;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

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

import static com.azure.core.amqp.implementation.ClientConstants.MAX_AMQP_HEADER_SIZE_BYTES;
import static com.azure.core.amqp.implementation.ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Handles scheduling and transmitting events through proton-j to Event Hubs service.
 */
class ReactorSender implements AmqpSendLink {
    private final String entityPath;
    private final Sender sender;
    private final SendLinkHandler handler;
    private final ReactorProvider reactorProvider;
    private final Disposable.Composite subscriptions;

    private final AtomicBoolean hasConnected = new AtomicBoolean();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicBoolean hasAuthorized = new AtomicBoolean(true);
    private final AtomicInteger retryAttempts = new AtomicInteger();

    private final Object pendingSendLock = new Object();
    private final ConcurrentHashMap<String, RetriableWorkItem> pendingSendsMap = new ConcurrentHashMap<>();
    private final PriorityQueue<WeightedDeliveryTag> pendingSendsQueue =
        new PriorityQueue<>(1000, new DeliveryTagComparator());
    private final ClientLogger logger = new ClientLogger(ReactorSender.class);
    private final ReplayProcessor<AmqpEndpointState> endpointStates =
        ReplayProcessor.cacheLastOrDefault(AmqpEndpointState.UNINITIALIZED);
    private FluxSink<AmqpEndpointState> endpointStateSink = endpointStates.sink(FluxSink.OverflowStrategy.BUFFER);

    private final TokenManager tokenManager;
    private final MessageSerializer messageSerializer;
    private final AmqpRetryPolicy retry;
    private final Duration timeout;
    private final Timer sendTimeoutTimer = new Timer("SendTimeout-timer");

    private final Object errorConditionLock = new Object();

    private volatile Exception lastKnownLinkError;
    private volatile Instant lastKnownErrorReportedAt;
    private volatile int linkSize;

    ReactorSender(String entityPath, Sender sender, SendLinkHandler handler, ReactorProvider reactorProvider,
        TokenManager tokenManager, MessageSerializer messageSerializer, Duration timeout, AmqpRetryPolicy retry) {
        this.entityPath = entityPath;
        this.sender = sender;
        this.handler = handler;
        this.reactorProvider = reactorProvider;
        this.tokenManager = tokenManager;
        this.messageSerializer = messageSerializer;
        this.retry = retry;
        this.timeout = timeout;

        this.subscriptions = Disposables.composite(
            this.handler.getDeliveredMessages().subscribe(this::processDeliveredMessage),

            this.handler.getLinkCredits().subscribe(credit -> {
                logger.verbose("Credits on link: {}", credit);
                this.scheduleWorkOnDispatcher();
            }),

            this.handler.getEndpointStates().subscribe(
                state -> {
                    logger.verbose("[{}] Connection state: {}", entityPath, state);
                    this.hasConnected.set(state == EndpointState.ACTIVE);
                    endpointStateSink.next(AmqpEndpointStateUtil.getConnectionState(state));
                }, error -> {
                    logger.error("[{}] Error occurred in sender endpoint handler.", entityPath, error);
                    endpointStateSink.error(error);
                }, () -> {
                    endpointStateSink.next(AmqpEndpointState.CLOSED);
                    endpointStateSink.complete();
                    hasConnected.set(false);
                }),

            this.handler.getErrors().subscribe(error -> {
                logger.error("[{}] Error occurred in sender error handler.", entityPath, error);
                endpointStateSink.error(error);
            }),

            this.tokenManager.getAuthorizationResults().subscribe(
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
    public Flux<AmqpEndpointState> getEndpointStates() {
        return endpointStates;
    }

    @Override
    public Mono<Void> send(Message message) {
        return getLinkSize()
            .flatMap(maxMessageSize -> {
                final int payloadSize = messageSerializer.getSize(message);
                final int allocationSize =
                    Math.min(payloadSize + MAX_AMQP_HEADER_SIZE_BYTES, maxMessageSize);
                final byte[] bytes = new byte[allocationSize];

                int encodedSize;
                try {
                    encodedSize = message.encode(bytes, 0, allocationSize);
                } catch (BufferOverflowException exception) {
                    final String errorMessage =
                        String.format(Locale.US,
                            "Error sending. Size of the payload exceeded maximum message size: %s kb",
                            maxMessageSize / 1024);
                    final Throwable error = new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
                        errorMessage, exception, handler.getErrorContext(sender));
                    return Mono.error(error);
                }
                return send(bytes, encodedSize, DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
            });
    }

    @Override
    public Mono<Void> send(List<Message> messageBatch) {
        if (messageBatch.size() == 1) {
            return send(messageBatch.get(0));
        }

        return getLinkSize()
            .flatMap(maxMessageSize -> {
                final Message firstMessage = messageBatch.get(0);

                // proton-j doesn't support multiple dataSections to be part of AmqpMessage
                // here's the alternate approach provided by them: https://github.com/apache/qpid-proton/pull/54
                final Message batchMessage = Proton.message();
                batchMessage.setMessageAnnotations(firstMessage.getMessageAnnotations());

                final int maxMessageSizeTemp = maxMessageSize;

                final byte[] bytes = new byte[maxMessageSizeTemp];
                int encodedSize = batchMessage.encode(bytes, 0, maxMessageSizeTemp);
                int byteArrayOffset = encodedSize;

                for (final Message amqpMessage : messageBatch) {
                    final Message messageWrappedByData = Proton.message();

                    int payloadSize = messageSerializer.getSize(amqpMessage);
                    int allocationSize =
                        Math.min(payloadSize + MAX_AMQP_HEADER_SIZE_BYTES, maxMessageSizeTemp);

                    byte[] messageBytes = new byte[allocationSize];
                    int messageSizeBytes = amqpMessage.encode(messageBytes, 0, allocationSize);
                    messageWrappedByData.setBody(new Data(new Binary(messageBytes, 0, messageSizeBytes)));

                    try {
                        encodedSize =
                            messageWrappedByData
                                .encode(bytes, byteArrayOffset, maxMessageSizeTemp - byteArrayOffset - 1);
                    } catch (BufferOverflowException exception) {
                        final String message =
                            String.format(Locale.US,
                                "Size of the payload exceeded maximum message size: %s kb",
                                maxMessageSizeTemp / 1024);
                        final AmqpException error = new AmqpException(false,
                            AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, message, exception,
                            handler.getErrorContext(sender));

                        return Mono.error(error);
                    }

                    byteArrayOffset = byteArrayOffset + encodedSize;
                }

                return send(bytes, byteArrayOffset, AmqpConstants.AMQP_BATCH_MESSAGE_FORMAT);
            });

    }

    @Override
    public AmqpErrorContext getErrorContext() {
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
    public String getHostname() {
        return handler.getHostname();
    }

    @Override
    public Mono<Integer> getLinkSize() {
        if (linkSize > 0) {
            return Mono.just(this.linkSize);
        }

        synchronized (this) {
            if (linkSize > 0) {
                return Mono.just(this.linkSize);
            }

            return RetryUtil.withRetry(
                getEndpointStates()
                    .takeUntil(state -> state == AmqpEndpointState.ACTIVE)
                    .then(Mono.fromCallable(() -> {
                        final UnsignedLong remoteMaxMessageSize = sender.getRemoteMaxMessageSize();

                        if (remoteMaxMessageSize != null) {
                            this.linkSize = remoteMaxMessageSize.intValue();
                        }

                        return this.linkSize;
                    })),
                timeout, retry);
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void dispose() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        subscriptions.dispose();
        endpointStateSink.complete();
        tokenManager.close();
    }

    Mono<Void> send(byte[] bytes, int arrayOffset, int messageFormat) {
        if (hasConnected.get()) {
            return Mono.create(sink -> send(new RetriableWorkItem(bytes, arrayOffset, messageFormat, sink, timeout)));
        } else {
            return RetryUtil.withRetry(
                handler.getEndpointStates().takeUntil(state -> state == EndpointState.ACTIVE),
                timeout, retry)
                .then(Mono.create(sink ->
                    send(new RetriableWorkItem(bytes, arrayOffset, messageFormat, sink, timeout))));
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
                    logger.verbose(
                        "clientId[{}]. path[{}], linkName[{}], deliveryTag[{}]: sendData not found for this delivery.",
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
                delivery.setMessageFormat(workItem.getMessageFormat());

                sentMsgSize = sender.send(workItem.getMessage(), 0, workItem.getEncodedMessageSize());
                assert sentMsgSize == workItem.getEncodedMessageSize()
                    : "Contract of the ProtonJ library for Sender. Send API changed";

                linkAdvance = sender.advance();
            } catch (Exception exception) {
                sendException = exception;
            }

            if (linkAdvance) {
                logger.verbose("entityPath[{}], linkName[{}], deliveryTag[{}]: Sent message", entityPath,
                    getLinkName(), deliveryTag);

                workItem.setWaitingForAck();
                sendTimeoutTimer.schedule(new SendTimeout(deliveryTag), timeout.toMillis());
            } else {
                logger.verbose(
                    "clientId[{}]. path[{}], linkName[{}], deliveryTag[{}], sentMessageSize[{}], "
                        + "payloadActualSize[{}]: sendlink advance failed",
                    handler.getConnectionId(), entityPath, getLinkName(), deliveryTag, sentMsgSize,
                    workItem.getEncodedMessageSize());

                if (delivery != null) {
                    delivery.free();
                }

                final AmqpErrorContext context = handler.getErrorContext(sender);
                final Throwable exception = sendException != null
                    ? new OperationCancelledException(String.format(Locale.US,
                    "Entity(%s): send operation failed. Please see cause for more details", entityPath),
                    sendException, context)
                    : new OperationCancelledException(String.format(Locale.US,
                        "Entity(%s): send operation failed while advancing delivery(tag: %s).",
                        entityPath, deliveryTag), context);

                workItem.getSink().error(exception);
            }
        }
    }

    private void processDeliveredMessage(Delivery delivery) {
        final DeliveryState outcome = delivery.getRemoteState();
        final String deliveryTag = new String(delivery.getTag(), UTF_8);

        logger.verbose("entityPath[{}], linkName[{}], deliveryTag[{}]: process delivered message",
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
                lastKnownErrorReportedAt = null;
                retryAttempts.set(0);
            }

            workItem.getSink().success();
        } else if (outcome instanceof Rejected) {
            final Rejected rejected = (Rejected) outcome;
            final org.apache.qpid.proton.amqp.transport.ErrorCondition error = rejected.getError();
            final Exception exception = ExceptionUtil.toException(error.getCondition().toString(),
                error.getDescription(), handler.getErrorContext(sender));

            logger.warning("entityPath[{}], linkName[{}], deliveryTag[{}]: Delivery rejected. [{}]",
                entityPath, getLinkName(), deliveryTag, rejected);

            final int retryAttempt;
            if (isGeneralSendError(error.getCondition())) {
                synchronized (errorConditionLock) {
                    lastKnownLinkError = exception;
                    lastKnownErrorReportedAt = Instant.now();
                    retryAttempt = retryAttempts.incrementAndGet();
                }
            } else {
                retryAttempt = retryAttempts.get();
            }

            final Duration retryInterval = retry.calculateRetryDelay(exception, retryAttempt);

            if (retryInterval == null || retryInterval.compareTo(workItem.getTimeoutTracker().remaining()) > 0) {
                cleanupFailedSend(workItem, exception);
            } else {
                workItem.setLastKnownException(exception);
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
        }
    }

    private void cleanupFailedSend(final RetriableWorkItem workItem, final Exception exception) {
        //TODO (conniey): is there some timeout task I should handle?
        workItem.getSink().error(exception);
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

            Exception cause = lastKnownLinkError;
            final Exception lastError;
            final Instant lastErrorTime;

            synchronized (errorConditionLock) {
                lastError = lastKnownLinkError;
                lastErrorTime = lastKnownErrorReportedAt;
            }

            // Means that there was a timeout error on the send link before. So we check if the last time we got an
            // error it is after the sleep time buffer we allowed. Or if it is after the operation timeout we allotted.
            if (lastError != null && lastErrorTime != null) {
                final Instant now = Instant.now();
                final boolean isLastErrorAfterSleepTime =
                    lastErrorTime.isAfter(now.minusSeconds(SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS));
                final boolean isServerBusy = lastError instanceof AmqpException && isLastErrorAfterSleepTime;
                final boolean isLastErrorAfterOperationTimeout = lastErrorTime.isAfter(now.minus(timeout));

                cause = isServerBusy || isLastErrorAfterOperationTimeout
                    ? lastError
                    : null;
            }

            // If it is a type of AmqpException, we received this error from the service, otherwise, it is a client-side
            // error.
            final AmqpException exception;
            if (cause instanceof AmqpException) {
                exception = (AmqpException) cause;
            } else {
                exception = new AmqpException(true, AmqpErrorCondition.TIMEOUT_ERROR,
                    String.format(Locale.US, "Entity(%s): Send operation timed out", entityPath),
                    handler.getErrorContext(sender));
            }

            workItem.getSink().error(exception);
        }
    }
}
