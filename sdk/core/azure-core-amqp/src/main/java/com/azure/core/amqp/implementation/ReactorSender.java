// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.OperationCancelledException;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transaction.Declared;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.amqp.exception.AmqpErrorCondition.NOT_ALLOWED;
import static com.azure.core.amqp.implementation.ClientConstants.MAX_AMQP_HEADER_SIZE_BYTES;
import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;
import static com.azure.core.amqp.implementation.ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Handles scheduling and transmitting events through proton-j to Event Hubs service.
 */
class ReactorSender implements AmqpSendLink, AsyncCloseable, AutoCloseable {
    private final String entityPath;
    private final Sender sender;
    private final SendLinkHandler handler;
    private final ReactorProvider reactorProvider;
    private final Disposable.Composite subscriptions;

    private final AtomicBoolean hasConnected = new AtomicBoolean();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicInteger retryAttempts = new AtomicInteger();
    private final Sinks.Empty<Void> isClosedMono = Sinks.empty();

    private final Object pendingSendLock = new Object();
    private final ConcurrentHashMap<String, RetriableWorkItem> pendingSendsMap = new ConcurrentHashMap<>();
    private final PriorityQueue<WeightedDeliveryTag> pendingSendsQueue =
        new PriorityQueue<>(1000, new DeliveryTagComparator());
    private final ClientLogger logger = new ClientLogger(ReactorSender.class);
    private final Flux<AmqpEndpointState> endpointStates;

    private final TokenManager tokenManager;
    private final MessageSerializer messageSerializer;
    private final AmqpRetryPolicy retry;
    private final AmqpRetryOptions retryOptions;
    private final String activeTimeoutMessage;
    private final Scheduler scheduler;

    private final Object errorConditionLock = new Object();

    private volatile Exception lastKnownLinkError;
    private volatile Instant lastKnownErrorReportedAt;
    private volatile int linkSize;

    /**
     * Creates an instance of {@link ReactorSender}.
     *
     * @param amqpConnection The parent {@link AmqpConnection} that this sender lives in.
     * @param entityPath The message broker address for the sender.
     * @param sender The underlying proton-j sender.
     * @param handler The proton-j handler associated with the sender.
     * @param reactorProvider Provider to schedule work on the proton-j reactor.
     * @param tokenManager Token manager for authorising with the CBS node. Can be {@code null} if it is part of the
     *     transaction manager.
     * @param messageSerializer Serializer to deserialise and serialize AMQP messages.
     * @param retryOptions Retry options.
     * @param scheduler Scheduler to schedule send timeout.
     */
    ReactorSender(AmqpConnection amqpConnection, String entityPath, Sender sender, SendLinkHandler handler,
        ReactorProvider reactorProvider, TokenManager tokenManager, MessageSerializer messageSerializer,
        AmqpRetryOptions retryOptions, Scheduler scheduler) {
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.sender = Objects.requireNonNull(sender, "'sender' cannot be null.");
        this.handler = Objects.requireNonNull(handler, "'handler' cannot be null.");
        this.reactorProvider = Objects.requireNonNull(reactorProvider, "'reactorProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' cannot be null.");
        this.retry = RetryUtil.getRetryPolicy(retryOptions);
        this.tokenManager = tokenManager;

        this.activeTimeoutMessage = String.format(
            "ReactorSender connectionId[%s] linkName[%s]: Waiting for send and receive handler to be ACTIVE",
            handler.getConnectionId(), handler.getLinkName());

        this.endpointStates = this.handler.getEndpointStates()
            .map(state -> {
                logger.verbose("connectionId[{}] entityPath[{}] linkName[{}]: State {}", handler.getConnectionId(),
                    entityPath, getLinkName(), state);
                this.hasConnected.set(state == EndpointState.ACTIVE);
                return AmqpEndpointStateUtil.getConnectionState(state);
            })
            .doOnError(error -> {
                hasConnected.set(false);
                handleError(error);
            })
            .doOnComplete(() -> {
                hasConnected.set(false);
                handleClose();
            })
            .cache(1);

        this.subscriptions = Disposables.composite(
            this.endpointStates.subscribe(),

            this.handler.getDeliveredMessages().subscribe(this::processDeliveredMessage),

            this.handler.getLinkCredits().subscribe(credit -> {
                logger.verbose("connectionId[{}] entityPath[{}] linkName[{}] credits[{}] Credits on link.",
                    handler.getConnectionId(), entityPath, getLinkName(), credit);
                this.scheduleWorkOnDispatcher();
            }),

            amqpConnection.getShutdownSignals().flatMap(signal -> {
                logger.verbose("connectionId[{}] linkName[{}]: Shutdown signal received.", handler.getConnectionId(),
                    getLinkName());

                hasConnected.set(false);
                return closeAsync("Connection shutdown.", null);
            }).subscribe()
        );

        if (tokenManager != null) {
            this.subscriptions.add(tokenManager.getAuthorizationResults().onErrorResume(error -> {
                // When we encounter an error refreshing authorization results, close the send link.
                final Mono<Void> operation =
                    closeAsync(String.format("connectionId[%s] linkName[%s] Token renewal failure. Disposing send "
                            + "link.", amqpConnection.getId(), getLinkName()),
                        new ErrorCondition(Symbol.getSymbol(NOT_ALLOWED.getErrorCondition()),
                            error.getMessage()));

                return operation.then(Mono.empty());
            }).subscribe(response -> {
                logger.verbose("connectionId[{}] linkName[{}] response[{}] Token refreshed.",
                    handler.getConnectionId(), getLinkName(), response);
            }, error -> {
            }, () -> {
                logger.verbose("connectionId[{}] entityPath[{}] linkName[{}] Authorization completed. Disposing.",
                    handler.getConnectionId(), entityPath, getLinkName());

                closeAsync("Authorization completed. Disposing.", null).subscribe();
            }));
        }
    }

    @Override
    public Flux<AmqpEndpointState> getEndpointStates() {
        return endpointStates;
    }

    @Override
    public Mono<Void> send(Message message) {
        return send(message, null);
    }

    @Override
    public Mono<Void> send(Message message, DeliveryState deliveryState) {
        if (isDisposed.get()) {
            return Mono.error(new IllegalStateException(String.format(
                "connectionId[%s] linkName[%s] Cannot publish message when disposed.", handler.getConnectionId(),
                getLinkName())));
        }

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
                return send(bytes, encodedSize, DeliveryImpl.DEFAULT_MESSAGE_FORMAT, deliveryState);
            }).then();
    }

    @Override
    public Mono<Void> send(List<Message> messageBatch) {
        return send(messageBatch, null);
    }

    @Override
    public Mono<Void> send(List<Message> messageBatch, DeliveryState deliveryState) {
        if (isDisposed.get()) {
            return Mono.error(new IllegalStateException(String.format(
                "connectionId[%s] linkName[%s] Cannot publish data batch when disposed.", handler.getConnectionId(),
                getLinkName())));
        }

        if (messageBatch.size() == 1) {
            return send(messageBatch.get(0), deliveryState);
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

                return send(bytes, byteArrayOffset, AmqpConstants.AMQP_BATCH_MESSAGE_FORMAT, deliveryState);
            }).then();
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
                return Mono.just(linkSize);
            }

            return RetryUtil.withRetry(getEndpointStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE),
                retryOptions, activeTimeoutMessage)
                .then(Mono.fromCallable(() -> {
                    final UnsignedLong remoteMaxMessageSize = sender.getRemoteMaxMessageSize();
                    if (remoteMaxMessageSize != null) {
                        linkSize = remoteMaxMessageSize.intValue();
                    } else {
                        logger.warning("connectionId[{}], linkName[{}]: Could not get the getRemoteMaxMessageSize."
                                + " Returning current link size: {}", handler.getConnectionId(), handler.getLinkName(),
                            linkSize);
                    }

                    return linkSize;
                }));
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed.get();
    }

    /**
     * Blocking call that disposes of the sender.
     *
     * @see #close()
     */
    @Override
    public void dispose() {
        close();
    }

    /**
     * Blocking call that disposes of the sender.
     *
     * @see #closeAsync()
     */
    @Override
    public void close() {
        closeAsync().block(retryOptions.getTryTimeout());
    }

    @Override
    public Mono<Void> closeAsync() {
        return closeAsync("User invoked close operation.", null);
    }

    /**
     * Disposes of the sender.
     *
     * @param errorCondition Error condition associated with close operation.
     * @param message Message associated with why the sender was closed.
     *
     * @return A mono that completes when the send link has closed.
     */
    Mono<Void> closeAsync(String message, ErrorCondition errorCondition) {
        if (isDisposed.getAndSet(true)) {
            return isClosedMono.asMono();
        }

        final String condition = errorCondition != null ? errorCondition.toString() : NOT_APPLICABLE;
        logger.verbose("connectionId[{}], path[{}], linkName[{}] errorCondition[{}]. Setting error condition and "
                + "disposing. {}",
            handler.getConnectionId(), entityPath, getLinkName(), condition, message);

        final Runnable closeWork = () -> {
            if (errorCondition != null && sender.getCondition() == null) {
                sender.setCondition(errorCondition);
            }

            // Sets local state to close. When onLinkRemoteClose is called from the service,
            // handler.getEndpointStates() will complete its Flux.
            sender.close();
        };

        return Mono.fromRunnable(() -> {
            try {
                reactorProvider.getReactorDispatcher().invoke(closeWork);
            } catch (IOException e) {
                logger.warning("connectionId[{}] entityPath[{}] linkName[{}]: Could not schedule close work. Running"
                    + " manually. And completing close.", handler.getConnectionId(), entityPath, getLinkName(), e);

                closeWork.run();
                handleClose();
            } catch (RejectedExecutionException e) {
                logger.info("connectionId[{}] entityPath[{}] linkName[{}]: RejectedExecutionException scheduling close"
                    + " work. And completing close.", handler.getConnectionId(), entityPath, getLinkName());

                closeWork.run();
                handleClose();
            }
        }).then(isClosedMono.asMono())
            .publishOn(Schedulers.boundedElastic());
    }

    /**
     * A mono that completes when the sender has completely closed.
     *
     * @return mono that completes when the sender has completely closed.
     */
    Mono<Void> isClosed() {
        return isClosedMono.asMono();
    }

    @Override
    public Mono<DeliveryState> send(byte[] bytes, int arrayOffset, int messageFormat, DeliveryState deliveryState) {
        final Flux<EndpointState> activeEndpointFlux = RetryUtil.withRetry(
            handler.getEndpointStates().takeUntil(state -> state == EndpointState.ACTIVE), retryOptions,
            activeTimeoutMessage);

        return activeEndpointFlux.then(Mono.create(sink -> {
            sendWork(new RetriableWorkItem(bytes, arrayOffset, messageFormat, sink, retryOptions.getTryTimeout(),
                deliveryState));
        }));
    }

    /**
     * Add the work item in pending send to be processed on {@link ReactorDispatcher} thread.
     *
     * @param workItem to be processed.
     */
    private void sendWork(RetriableWorkItem workItem) {
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

        if (isDisposed.get()) {
            logger.info("Sender is closed. Not executing work.");
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

                if (workItem.isDeliveryStateProvided()) {
                    delivery.disposition(workItem.getDeliveryState());
                }
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
                scheduler.schedule(new SendTimeout(deliveryTag), retryOptions.getTryTimeout().toMillis(),
                    TimeUnit.MILLISECONDS);
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

                workItem.error(exception);
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
        } else if (workItem.isDeliveryStateProvided()) {
            workItem.success(outcome);
            return;
        }

        if (outcome instanceof Accepted) {
            synchronized (errorConditionLock) {
                lastKnownLinkError = null;
                lastKnownErrorReportedAt = null;
                retryAttempts.set(0);
            }

            workItem.success(outcome);
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
                    reactorProvider.getReactorDispatcher().invoke(() -> sendWork(workItem), retryInterval);
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
        } else if (outcome instanceof Declared) {
            final Declared declared = (Declared) outcome;
            workItem.success(declared);
        } else {
            cleanupFailedSend(workItem, new AmqpException(false, outcome.toString(),
                handler.getErrorContext(sender)));
        }
    }

    private void scheduleWorkOnDispatcher() {
        try {
            reactorProvider.getReactorDispatcher().invoke(this::processSendWork);
        } catch (IOException e) {
            logger.warning("connectionId[{}] linkName[{}]: Error scheduling work on reactor.",
                handler.getConnectionId(), getLinkName(), e);
        } catch (RejectedExecutionException e) {
            logger.info("connectionId[{}] linkName[{}]: Error scheduling work on reactor because of"
                + " RejectedExecutionException.", handler.getConnectionId(), getLinkName());
        }
    }

    private void cleanupFailedSend(final RetriableWorkItem workItem, final Exception exception) {
        //TODO (conniey): is there some timeout task I should handle?
        workItem.error(exception);
    }

    private void completeClose() {
        isClosedMono.emitEmpty((signalType, result) -> {
            logger.warning("connectionId[{}], signal[{}], result[{}]. Unable to emit shutdown signal.",
                handler.getConnectionId(), signalType, result);
            return false;
        });

        subscriptions.dispose();

        if (tokenManager != null) {
            tokenManager.close();
        }
    }

    /**
     * Clears pending sends and puts an error in there.
     *
     * @param error Error to pass to pending sends.
     */
    private void handleError(Throwable error) {
        synchronized (pendingSendLock) {
            final String logMessage = isDisposed.getAndSet(true)
                ? "This was already disposed. Dropping error."
                : String.format("Disposing of '%d' pending sends with error.", pendingSendsMap.size());
            logger.verbose("connectionId[{}] entityPath[{}] linkName[{}] {}", handler.getConnectionId(), entityPath,
                getLinkName(), logMessage);

            pendingSendsMap.forEach((key, value) -> value.error(error));
            pendingSendsMap.clear();
            pendingSendsQueue.clear();
        }

        completeClose();
    }

    private void handleClose() {
        final String message = String.format("Could not complete sends because link '%s' for '%s' is closed.",
            getLinkName(), entityPath);
        final AmqpErrorContext context = handler.getErrorContext(sender);

        synchronized (pendingSendLock) {
            final String logMessage = isDisposed.getAndSet(true)
                ? "This was already disposed."
                : String.format("Disposing of '%d' pending sends.", pendingSendsMap.size());

            logger.verbose("connectionId[{}] entityPath[{}] linkName[{}] {}", handler.getConnectionId(), entityPath,
                getLinkName(), logMessage);

            pendingSendsMap.forEach((key, value) -> value.error(new AmqpException(true, message, context)));
            pendingSendsMap.clear();
            pendingSendsQueue.clear();
        }

        completeClose();
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
     * Keeps track of messages that have been sent, but may not have been acknowledged by the service.
     */
    private class SendTimeout implements Runnable {
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
                final boolean isLastErrorAfterOperationTimeout =
                    lastErrorTime.isAfter(now.minus(retryOptions.getTryTimeout()));

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

            workItem.error(exception);
        }
    }
}
