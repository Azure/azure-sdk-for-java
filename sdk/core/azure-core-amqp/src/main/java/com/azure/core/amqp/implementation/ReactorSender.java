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
import com.azure.core.util.CoreUtils;
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
import org.apache.qpid.proton.codec.CompositeReadableBuffer;
import org.apache.qpid.proton.codec.ReadableBuffer;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.amqp.exception.AmqpErrorCondition.NOT_ALLOWED;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addErrorCondition;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addSignalTypeAndResult;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.createContextWithConnectionId;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.MAX_AMQP_HEADER_SIZE_BYTES;
import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;
import static com.azure.core.amqp.implementation.ClientConstants.SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS;
import static com.azure.core.util.FluxUtil.monoError;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Handles scheduling and transmitting events through proton-j to Event Hubs service.
 */
class ReactorSender implements AmqpSendLink, AsyncCloseable, AutoCloseable {
    private static final String DELIVERY_TAG_KEY = "deliveryTag";
    private static final String PENDING_SENDS_SIZE_KEY = "pending_sends_size";
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
    private final PriorityQueue<WeightedDeliveryTag> pendingSendsQueue
        = new PriorityQueue<>(1000, new DeliveryTagComparator());
    private final ClientLogger logger;
    private final Flux<AmqpEndpointState> endpointStates;

    private final TokenManager tokenManager;
    private final MessageSerializer messageSerializer;
    private final AmqpRetryPolicy retry;
    private final AmqpRetryOptions retryOptions;
    private final String activeTimeoutMessage;
    private final Scheduler scheduler;

    private final AmqpMetricsProvider metricsProvider;

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
        AmqpRetryOptions retryOptions, Scheduler scheduler, AmqpMetricsProvider metricsProvider) {
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.sender = Objects.requireNonNull(sender, "'sender' cannot be null.");
        this.handler = Objects.requireNonNull(handler, "'handler' cannot be null.");
        this.reactorProvider = Objects.requireNonNull(reactorProvider, "'reactorProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' cannot be null.");
        this.retry = RetryUtil.getRetryPolicy(retryOptions);
        this.tokenManager = tokenManager;

        this.metricsProvider = metricsProvider;

        String connectionId = handler.getConnectionId() == null ? NOT_APPLICABLE : handler.getConnectionId();
        String linkName = getLinkName() == null ? NOT_APPLICABLE : getLinkName();

        Map<String, Object> loggingContext = createContextWithConnectionId(connectionId);
        loggingContext.put(LINK_NAME_KEY, linkName);
        loggingContext.put(ENTITY_PATH_KEY, entityPath);
        this.logger = new ClientLogger(ReactorSender.class, loggingContext);

        this.activeTimeoutMessage = String.format(
            "ReactorSender connectionId[%s] linkName[%s]: Waiting for send and receive handler to be ACTIVE",
            handler.getConnectionId(), handler.getLinkName());

        this.endpointStates = this.handler.getEndpointStates().map(state -> {
            logger.atVerbose().addKeyValue("state", state).log("onEndpointState");
            this.hasConnected.set(state == EndpointState.ACTIVE);
            return AmqpEndpointStateUtil.getConnectionState(state);
        }).doOnError(error -> {
            hasConnected.set(false);
            handleError(error);
        }).doOnComplete(() -> {
            hasConnected.set(false);
            handleClose();
        }).cache(1);

        this.subscriptions = Disposables.composite(this.endpointStates.subscribe(),

            this.handler.getDeliveredMessages().subscribe(this::processDeliveredMessage),

            this.handler.getLinkCredits().subscribe(credit -> {
                logger.atVerbose().addKeyValue("credits", credit).log("Credits on link.");
                this.scheduleWorkOnDispatcher();
            }),

            amqpConnection.getShutdownSignals().flatMap(signal -> {
                logger.verbose("Shutdown signal received.");

                hasConnected.set(false);
                return closeAsync("Connection shutdown.", null);
            }).subscribe());

        if (tokenManager != null) {
            this.subscriptions.add(tokenManager.getAuthorizationResults().onErrorResume(error -> {
                // When we encounter an error refreshing authorization results, close the send link.
                final Mono<Void> operation = closeAsync(
                    String.format("connectionId[%s] linkName[%s] Token renewal failure. Disposing send " + "link.",
                        amqpConnection.getId(), getLinkName()),
                    new ErrorCondition(Symbol.getSymbol(NOT_ALLOWED.getErrorCondition()), error.getMessage()));

                return operation.then(Mono.empty());
            }).subscribe(response -> {
                logger.atVerbose().addKeyValue("response", response).log("Token refreshed.");
            }, error -> {
            }, () -> {
                logger.verbose(" Authorization completed. Disposing.");

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
            return Mono.error(new IllegalStateException(
                String.format("connectionId[%s] linkName[%s] Cannot publish message when disposed.",
                    handler.getConnectionId(), getLinkName())));
        }

        return getLinkSize().flatMap(maxMessageSize -> {
            final int payloadSize = messageSerializer.getSize(message);
            final int allocationSize = Math.min(payloadSize + MAX_AMQP_HEADER_SIZE_BYTES, maxMessageSize);
            final byte[] bytes = new byte[allocationSize];

            int encodedSize;
            try {
                encodedSize = message.encode(bytes, 0, allocationSize);
            } catch (BufferOverflowException exception) {
                final String errorMessage = String.format(Locale.US,
                    "Error sending. Size of the payload exceeded maximum message size: %s kb", maxMessageSize / 1024);
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
            return Mono.error(new IllegalStateException(
                String.format("connectionId[%s] linkName[%s] Cannot publish data batch when disposed.",
                    handler.getConnectionId(), getLinkName())));
        }

        if (messageBatch.size() == 1) {
            return send(messageBatch.get(0), deliveryState);
        }

        return getLinkSize().flatMap(maxMessageSize -> {
            int totalEncodedSize = 0;
            final CompositeReadableBuffer buffer = new CompositeReadableBuffer();

            final byte[] envelopBytes = batchEnvelopBytes(messageBatch.get(0), maxMessageSize);
            if (envelopBytes.length > 0) {
                totalEncodedSize += envelopBytes.length;
                if (totalEncodedSize > maxMessageSize) {
                    return batchBufferOverflowError(maxMessageSize);
                }
                buffer.append(envelopBytes);
            }

            for (final Message message : messageBatch) {
                final byte[] sectionBytes = batchBinaryDataSectionBytes(message, maxMessageSize);
                if (sectionBytes.length > 0) {
                    totalEncodedSize += sectionBytes.length;
                    if (totalEncodedSize > maxMessageSize) {
                        return batchBufferOverflowError(maxMessageSize);
                    }
                    buffer.append(sectionBytes);
                } else {
                    logger.info("Ignoring the empty message org.apache.qpid.proton.message.message@{} in the batch.",
                        Integer.toHexString(System.identityHashCode(message)));
                }
            }

            return send(buffer, AmqpConstants.AMQP_BATCH_MESSAGE_FORMAT, deliveryState);
        }).then();
    }

    private byte[] batchEnvelopBytes(Message envelopMessage, int maxMessageSize) {
        // Proton-j doesn't support multiple dataSections to be part of AmqpMessage.
        // Here's the alternate approach provided: https://github.com/apache/qpid-proton/pull/54
        final Message message = Proton.message();
        message.setMessageAnnotations(envelopMessage.getMessageAnnotations());

        // Set partition identifier properties of the first message on batch message
        if ((envelopMessage.getMessageId() instanceof String)
            && !CoreUtils.isNullOrEmpty((String) envelopMessage.getMessageId())) {

            message.setMessageId(envelopMessage.getMessageId());
        }

        if (!CoreUtils.isNullOrEmpty(envelopMessage.getGroupId())) {
            message.setGroupId(envelopMessage.getGroupId());
        }

        final int size = messageSerializer.getSize(message);
        final int allocationSize = Math.min(size + MAX_AMQP_HEADER_SIZE_BYTES, maxMessageSize);
        final byte[] encodedBytes = new byte[allocationSize];
        final int encodedSize = message.encode(encodedBytes, 0, allocationSize);
        // This copyOf copying is just few bytes for envelop.
        return Arrays.copyOf(encodedBytes, encodedSize);
    }

    private byte[] batchBinaryDataSectionBytes(Message sectionMessage, int maxMessageSize) {
        final int size = messageSerializer.getSize(sectionMessage);
        final int allocationSize = Math.min(size + MAX_AMQP_HEADER_SIZE_BYTES, maxMessageSize);
        final byte[] encodedBytes = new byte[allocationSize];
        final int encodedSize = sectionMessage.encode(encodedBytes, 0, allocationSize);

        final Message message = Proton.message();
        final Data binaryData = new Data(new Binary(encodedBytes, 0, encodedSize));
        message.setBody(binaryData);
        final int binaryRawSize = binaryData.getValue().getLength();
        // Precompute the "amqp:data:binary" encoded size -
        final int binaryEncodedSize = binaryEncodedSize(binaryRawSize);
        // ^ this pre-computation avoids allocating byte[] 'arr1' of estimated encoded size (to pass to
        // message.encode(arr1,))
        // and a second allocation of byte[] 'arr2' with exact encoded size (returned from message.encode(arr1,)) then
        // copying encoded size bytes from 'arr1' to 'arr2'. Skipping extra allocations and CPU cycles for copying.
        final byte[] binaryEncodedBytes = new byte[binaryEncodedSize];
        message.encode(binaryEncodedBytes, 0, binaryEncodedSize);
        return binaryEncodedBytes;
    }

    private Mono<Void> batchBufferOverflowError(int maxMessageSize) {
        return monoError(logger,
            new AmqpException(
                false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, String.format(Locale.US,
                    "Size of the payload exceeded maximum message size: %s kb", maxMessageSize / 1024),
                new BufferOverflowException(), handler.getErrorContext(sender)));
    }

    /**
     * Compute the encoded size when encoding a binary data of given size per Amqp 1.0 spec "amqp:data:binary" format.
     *
     * @param binaryRawSize the length of the binary data.
     * @return the encoded size.
     */
    private int binaryEncodedSize(int binaryRawSize) {
        if (binaryRawSize <= 255) {
            // [0x00,0x53,0x75,0xa0,{byte(Data.Binary.Length)},{Data.Binary.bytes}]
            // The AMQP 1.0 spec format ^ for amqp:data:binary when the raw bytes length is <= 255.
            return 5 + binaryRawSize;
        } else {
            // [0x00,0x53,0x75,0xb0,{int(Data.Binary.Length)},{Data.Binary.bytes}]
            // The AMQP 1.0 spec format ^ for amqp:data:binary when the raw bytes length is > 255.
            return 8 + binaryRawSize;
        }
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
            // Using Mono.defer when returning cached linkSize, refer
            // https://github.com/Azure/azure-sdk-for-java/issues/26372.
            return Mono.defer(() -> Mono.just(this.linkSize));
        }

        synchronized (this) {
            if (linkSize > 0) {
                return Mono.defer(() -> Mono.just(linkSize));
            }

            return RetryUtil
                .withRetry(getEndpointStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE), retryOptions,
                    activeTimeoutMessage)
                .then(Mono.fromCallable(() -> {
                    final UnsignedLong remoteMaxMessageSize = sender.getRemoteMaxMessageSize();
                    if (remoteMaxMessageSize != null) {
                        linkSize = remoteMaxMessageSize.intValue();
                    } else {
                        logger.warning("Could not get the getRemoteMaxMessageSize. Returning current link size: {}",
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

        addErrorCondition(logger.atVerbose(), errorCondition).log("Setting error condition and disposing. {}", message);

        final Runnable closeWork = () -> {
            if (errorCondition != null && sender.getCondition() == null) {
                sender.setCondition(errorCondition);
            }

            // Sets local state to close. When onLinkRemoteClose is called from the service,
            // handler.getEndpointStates() will complete its Flux.
            sender.close();
        };

        // @formatter:off
        return Mono.fromRunnable(() -> {
            try {
                reactorProvider.getReactorDispatcher().invoke(closeWork);
            } catch (IOException e) {
                logger.warning("Could not schedule close work. Running manually. And completing close.", e);

                closeWork.run();
                handleClose();
            } catch (RejectedExecutionException e) {
                logger.info("RejectedExecutionException scheduling close work. And completing close.");

                closeWork.run();
                handleClose();
            }
        }).then(isClosedMono.asMono())
            .publishOn(Schedulers.boundedElastic());
        // @formatter:on
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
        return onEndpointActive().then(Mono.create(sink -> {
            sendWork(new RetriableWorkItem(bytes, arrayOffset, messageFormat, sink, retryOptions.getTryTimeout(),
                deliveryState, metricsProvider));
        }));
    }

    Mono<DeliveryState> send(ReadableBuffer buffer, int messageFormat, DeliveryState deliveryState) {
        return onEndpointActive().then(Mono.create(sink -> {
            sendWork(new RetriableWorkItem(buffer, messageFormat, sink, retryOptions.getTryTimeout(), deliveryState,
                metricsProvider));
        }));
    }

    private Flux<EndpointState> onEndpointActive() {
        return RetryUtil.withRetry(handler.getEndpointStates().takeUntil(state -> state == EndpointState.ACTIVE),
            retryOptions, activeTimeoutMessage);
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
                    logger.atVerbose()
                        .addKeyValue(DELIVERY_TAG_KEY, deliveryTag)
                        .log("sendData not found for this delivery.");
                }

                // TODO (conniey): Should we update to continue rather than break?
                break;
            }

            Delivery delivery = null;
            boolean linkAdvance = false;
            int sentMsgSize = 0;
            Exception sendException = null;

            try {
                workItem.beforeTry();
                delivery = sender.delivery(deliveryTag.getBytes(UTF_8));
                delivery.setMessageFormat(workItem.getMessageFormat());

                if (workItem.isDeliveryStateProvided()) {
                    delivery.disposition(workItem.getDeliveryState());
                }
                workItem.send(sender);
                linkAdvance = sender.advance();
            } catch (Exception exception) {
                sendException = exception;
            }

            if (linkAdvance) {
                logger.atVerbose().addKeyValue(DELIVERY_TAG_KEY, deliveryTag).log("Sent message.");

                workItem.setWaitingForAck();
                scheduler.schedule(new SendTimeout(deliveryTag), retryOptions.getTryTimeout().toMillis(),
                    TimeUnit.MILLISECONDS);
            } else {
                logger.atVerbose()
                    .addKeyValue(DELIVERY_TAG_KEY, deliveryTag)
                    .addKeyValue("sentMessageSize", sentMsgSize)
                    .addKeyValue("payloadActualSize", workItem.getEncodedMessageSize())
                    .log("Sendlink advance failed.");

                DeliveryState outcome = null;
                if (delivery != null) {
                    outcome = delivery.getRemoteState();
                    delivery.free();
                }

                final AmqpErrorContext context = handler.getErrorContext(sender);
                final Throwable exception = sendException != null
                    ? new OperationCancelledException(String.format(Locale.US,
                        "Entity(%s): send operation failed. Please see cause for more details", entityPath),
                        sendException, context)
                    : new OperationCancelledException(
                        String.format(Locale.US, "Entity(%s): send operation failed while advancing delivery(tag: %s).",
                            entityPath, deliveryTag),
                        context);

                workItem.error(exception, outcome);
            }
        }
    }

    private void processDeliveredMessage(Delivery delivery) {
        final DeliveryState outcome = delivery.getRemoteState();
        final String deliveryTag = new String(delivery.getTag(), UTF_8);
        logger.atVerbose().addKeyValue(DELIVERY_TAG_KEY, deliveryTag).log("Process delivered message.");

        final RetriableWorkItem workItem = pendingSendsMap.remove(deliveryTag);
        if (workItem == null) {
            logger.atVerbose().addKeyValue(DELIVERY_TAG_KEY, deliveryTag).log("Mismatch (or send timed out).");

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

            logger.atWarning()
                .addKeyValue(DELIVERY_TAG_KEY, deliveryTag)
                .addKeyValue("rejected", rejected)
                .log("Delivery rejected.");

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
                cleanupFailedSend(workItem, exception, outcome);
            } else {
                workItem.setLastKnownException(exception);
                try {
                    reactorProvider.getReactorDispatcher().invoke(() -> sendWork(workItem), retryInterval);
                } catch (IOException | RejectedExecutionException schedulerException) {
                    exception.initCause(schedulerException);
                    cleanupFailedSend(workItem,
                        new AmqpException(false,
                            String.format(Locale.US,
                                "Entity(%s): send operation failed while scheduling a"
                                    + " retry on Reactor, see cause for more details.",
                                entityPath),
                            schedulerException, handler.getErrorContext(sender)),
                        outcome);
                }
            }
        } else if (outcome instanceof Released) {
            cleanupFailedSend(workItem,
                new OperationCancelledException(outcome.toString(), handler.getErrorContext(sender)), outcome);
        } else if (outcome instanceof Declared) {
            final Declared declared = (Declared) outcome;
            workItem.success(declared);
        } else {
            cleanupFailedSend(workItem, new AmqpException(false, outcome.toString(), handler.getErrorContext(sender)),
                outcome);
        }
    }

    private void scheduleWorkOnDispatcher() {
        try {
            reactorProvider.getReactorDispatcher().invoke(this::processSendWork);
        } catch (IOException e) {
            logger.warning("Error scheduling work on reactor.", e);

        } catch (RejectedExecutionException e) {
            logger.info("Error scheduling work on reactor because of RejectedExecutionException.");
        }
    }

    private void cleanupFailedSend(final RetriableWorkItem workItem, final Exception exception,
        final DeliveryState deliveryState) {
        // TODO (conniey): is there some timeout task I should handle?
        workItem.error(exception, deliveryState);
    }

    private void completeClose() {
        isClosedMono.emitEmpty((signalType, result) -> {
            addSignalTypeAndResult(logger.atWarning(), signalType, result).log("Unable to emit shutdown signal.");
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
            if (isDisposed.getAndSet(true)) {
                logger.verbose("This was already disposed. Dropping error.");
            } else {
                logger.atVerbose()
                    .addKeyValue(PENDING_SENDS_SIZE_KEY, () -> String.valueOf(pendingSendsMap.size()))
                    .log("Disposing pending sends with error.");
            }

            pendingSendsMap.forEach((key, value) -> value.error(error, null));
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
            if (isDisposed.getAndSet(true)) {
                logger.verbose("This was already disposed.");
            } else {
                logger.atVerbose()
                    .addKeyValue(PENDING_SENDS_SIZE_KEY, () -> String.valueOf(pendingSendsMap.size()))
                    .log("Disposing pending sends.");
            }

            pendingSendsMap.forEach((key, value) -> value.error(new AmqpException(true, message, context), null));
            pendingSendsMap.clear();
            pendingSendsQueue.clear();
        }

        completeClose();
    }

    private static boolean isGeneralSendError(Symbol amqpError) {
        return (amqpError == AmqpErrorCode.SERVER_BUSY_ERROR
            || amqpError == AmqpErrorCode.TIMEOUT_ERROR
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
                final boolean isLastErrorAfterSleepTime
                    = lastErrorTime.isAfter(now.minusSeconds(SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS));
                final boolean isServerBusy = lastError instanceof AmqpException && isLastErrorAfterSleepTime;
                final boolean isLastErrorAfterOperationTimeout
                    = lastErrorTime.isAfter(now.minus(retryOptions.getTryTimeout()));

                cause = isServerBusy || isLastErrorAfterOperationTimeout ? lastError : null;
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

            workItem.error(exception, null);
        }
    }
}
