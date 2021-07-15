// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.ReactorReceiver;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Outcome;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.MessageUtils.LOCK_TOKEN_SIZE;
import static com.azure.messaging.servicebus.implementation.ServiceBusReactorSession.LOCKED_UNTIL_UTC;
import static com.azure.messaging.servicebus.implementation.ServiceBusReactorSession.SESSION_FILTER;

/**
 * A proton-j receiver for Service Bus.
 */
public class ServiceBusReactorReceiver extends ReactorReceiver implements ServiceBusReceiveLink {
    private static final Message EMPTY_MESSAGE = Proton.message();

    private final ClientLogger logger = new ClientLogger(ServiceBusReactorReceiver.class);
    private final ConcurrentHashMap<String, Delivery> unsettledDeliveries = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UpdateDispositionWorkItem> pendingUpdates = new ConcurrentHashMap<>();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Disposable subscription;
    private final Receiver receiver;

    /**
     * Indicates whether the message has already been settled from the sender side. This is the case when {@link
     * ServiceBusReceiveMode#RECEIVE_AND_DELETE} is used.
     */
    private final boolean isSettled;
    private final Duration timeout;
    private final AmqpRetryPolicy retryPolicy;
    private final ReceiveLinkHandler handler;
    private final ReactorProvider provider;
    private final Mono<String> sessionIdMono;
    private final Mono<OffsetDateTime> sessionLockedUntil;

    public ServiceBusReactorReceiver(AmqpConnection connection, String entityPath, Receiver receiver,
        ReceiveLinkHandler handler, TokenManager tokenManager, ReactorProvider provider, Duration timeout,
        AmqpRetryPolicy retryPolicy) {
        super(connection, entityPath, receiver, handler, tokenManager, provider.getReactorDispatcher(),
            retryPolicy.getRetryOptions());
        this.receiver = receiver;
        this.handler = handler;
        this.provider = provider;
        this.isSettled = receiver.getSenderSettleMode() == SenderSettleMode.SETTLED;
        this.timeout = timeout;
        this.retryPolicy = retryPolicy;
        this.subscription = Flux.interval(timeout).subscribe(i -> cleanupWorkItems());
        this.sessionIdMono = getEndpointStates().filter(x -> x == AmqpEndpointState.ACTIVE)
            .next()
            .flatMap(state -> {
                @SuppressWarnings("unchecked") final Map<Symbol, Object> remoteSource =
                    ((Source) receiver.getRemoteSource()).getFilter();
                final Object value = remoteSource.get(SESSION_FILTER);
                if (value == null) {
                    logger.info("entityPath[{}], linkName[{}]. There is no session id.", entityPath, getLinkName());
                    return Mono.empty();
                }

                final String actualSessionId = String.valueOf(value);
                return Mono.just(actualSessionId);
            })
            .cache(value -> Duration.ofMillis(Long.MAX_VALUE), error -> Duration.ZERO, () -> Duration.ZERO);

        this.sessionLockedUntil = getEndpointStates().filter(x -> x == AmqpEndpointState.ACTIVE)
            .next()
            .map(state -> {
                if (receiver.getRemoteProperties() != null
                    && receiver.getRemoteProperties().containsKey(LOCKED_UNTIL_UTC)) {
                    final long ticks = (long) receiver.getRemoteProperties().get(LOCKED_UNTIL_UTC);
                    return MessageUtils.convertDotNetTicksToOffsetDateTime(ticks);
                } else {
                    logger.info("entityPath[{}], linkName[{}]. Locked until not set.", entityPath, getLinkName());

                    return Instant.EPOCH.atOffset(ZoneOffset.UTC);
                }
            })
            .cache(value -> Duration.ofMillis(Long.MAX_VALUE), error -> Duration.ZERO, () -> Duration.ZERO);
    }

    @Override
    public Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException("Cannot perform operations on a disposed receiver."));
        }
        return updateDispositionInternal(lockToken, deliveryState);
    }

    @Override
    public Flux<Message> receive() {
        // Remove empty update disposition messages. The deliveries themselves are ACKs with no message.
        return super.receive()
            .filter(message -> message != EMPTY_MESSAGE)
            .publishOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> getSessionId() {
        return sessionIdMono;
    }

    @Override
    public Mono<OffsetDateTime> getSessionLockedUntil() {
        return sessionLockedUntil;
    }

    @Override
    public Mono<Void> closeAsync() {
        if (isDisposed.getAndSet(true)) {
            return super.closeAsync();
        }

        cleanupWorkItems();

        final Mono<Void> disposeMono;
        if (!pendingUpdates.isEmpty()) {
            final List<Mono<Void>> pending = new ArrayList<>();
            final StringJoiner builder = new StringJoiner(", ");
            for (UpdateDispositionWorkItem workItem : pendingUpdates.values()) {

                if (workItem.hasTimedout()) {
                    continue;
                }

                if (workItem.getDeliveryState() instanceof TransactionalState) {
                    pending.add(updateDispositionInternal(workItem.getLockToken(), Released.getInstance()));
                } else {
                    pending.add(workItem.getMono());
                }
                builder.add(workItem.getLockToken());
            }

            logger.info("Waiting for pending updates to complete. Locks: {}", builder.toString());
            disposeMono = Mono.when(pending);
        } else {
            disposeMono = Mono.empty();
        }

        return disposeMono.onErrorResume(error -> {
            logger.info("There was an exception while disposing of all links.", error);
            return Mono.empty();
        }).doFinally(signal -> subscription.dispose()).then(super.closeAsync());
    }

    @Override
    protected Message decodeDelivery(Delivery delivery) {
        final byte[] deliveryTag = delivery.getTag();
        final UUID lockToken;
        if (deliveryTag != null && deliveryTag.length == LOCK_TOKEN_SIZE) {
            lockToken = MessageUtils.convertDotNetBytesToUUID(deliveryTag);
        } else {
            lockToken = MessageUtils.ZERO_LOCK_TOKEN;
        }

        final String lockTokenString = lockToken.toString();

        // There is no lock token associated with this delivery, or the lock token is not in the unsettledDeliveries.
        if (lockToken == MessageUtils.ZERO_LOCK_TOKEN || !unsettledDeliveries.containsKey(lockTokenString)) {
            final int messageSize = delivery.pending();
            final byte[] buffer = new byte[messageSize];
            final int read = receiver.recv(buffer, 0, messageSize);
            final Message message = Proton.message();
            message.decode(buffer, 0, read);

            // The delivery was already settled from the message broker.
            // This occurs in the case of receive and delete.
            if (isSettled) {
                delivery.disposition(Accepted.getInstance());
                delivery.settle();
            } else {
                unsettledDeliveries.putIfAbsent(lockToken.toString(), delivery);
                receiver.advance();
            }
            return new MessageWithLockToken(message, lockToken);
        } else {
            updateOutcome(lockTokenString, delivery);

            // Return empty update disposition messages. The deliveries themselves are ACKs. There is no actual message
            // to propagate.
            return EMPTY_MESSAGE;
        }
    }

    private Mono<Void> updateDispositionInternal(String lockToken, DeliveryState deliveryState) {
        final Delivery unsettled = unsettledDeliveries.get(lockToken);
        if (unsettled == null) {
            logger.warning("entityPath[{}], linkName[{}], deliveryTag[{}]. Delivery not found to update disposition.",
                getEntityPath(), getLinkName(), lockToken);

            return monoError(logger, Exceptions.propagate(new IllegalArgumentException(
                "Delivery not on receive link.")));
        }

        final UpdateDispositionWorkItem workItem = new UpdateDispositionWorkItem(lockToken, deliveryState, timeout);
        final Mono<Void> result = Mono.<Void>create(sink -> {
            workItem.start(sink);
            try {
                provider.getReactorDispatcher().invoke(() -> {
                    unsettled.disposition(deliveryState);
                    pendingUpdates.put(lockToken, workItem);
                });
            } catch (IOException error) {
                sink.error(new AmqpException(false, "updateDisposition failed while dispatching to Reactor.",
                    error, handler.getErrorContext(receiver)));
            }
        }).cache();  // cache because closeAsync use `when` to subscribe this Mono again.

        workItem.setMono(result);

        return result;
    }

    /**
     * Updates the outcome of a delivery. This occurs when a message is being settled from the receiver side.
     * @param delivery Delivery to update.
     */
    private void updateOutcome(String lockToken, Delivery delivery) {
        final DeliveryState remoteState = delivery.getRemoteState();

        logger.verbose("entityPath[{}], linkName[{}], deliveryTag[{}], state[{}] Received update disposition delivery.",
            getEntityPath(), getLinkName(), lockToken, remoteState);

        final Outcome remoteOutcome;
        if (remoteState instanceof Outcome) {
            remoteOutcome = (Outcome) remoteState;
        } else if (remoteState instanceof TransactionalState) {
            remoteOutcome = ((TransactionalState) remoteState).getOutcome();
        } else {
            remoteOutcome = null;
        }

        if (remoteOutcome == null) {
            logger.warning("linkName[{}], deliveryTag[{}]. No outcome associated with delivery. Delivery: {}",
                getLinkName(), lockToken, delivery);
            return;
        }

        final UpdateDispositionWorkItem workItem = pendingUpdates.get(lockToken);
        if (workItem == null) {
            logger.warning("linkName[{}], deliveryTag[{}]. No pending update for delivery. Delivery: {}",
                getLinkName(), lockToken, delivery);
            return;
        }

        // If the statuses match, then we settle the delivery and move on.
        if (remoteState.getType() == workItem.getDeliveryState().getType()) {
            completeWorkItem(lockToken, delivery, workItem.getSink(), null);
            return;
        }

        logger.info("Received delivery '{}' state '{}' doesn't match expected state '{}'",
            lockToken, remoteState, workItem.getDeliveryState());

        switch (remoteState.getType()) {
            case Rejected:
                final Rejected rejected = (Rejected) remoteOutcome;
                final ErrorCondition errorCondition = rejected.getError();
                final Throwable exception = ExceptionUtil.toException(errorCondition.getCondition().toString(),
                    errorCondition.getDescription(), handler.getErrorContext(receiver));

                final Duration retry = retryPolicy.calculateRetryDelay(exception, workItem.incrementRetry());
                if (retry == null) {
                    logger.info("deliveryTag[{}], state[{}]. Retry attempts exhausted.", lockToken, exception);
                    completeWorkItem(lockToken, delivery, workItem.getSink(), exception);
                } else {
                    workItem.setLastException(exception);
                    workItem.resetStartTime();
                    try {
                        provider.getReactorDispatcher().invoke(() -> delivery.disposition(workItem.getDeliveryState()));
                    } catch (IOException error) {
                        final Throwable amqpException = logger.logExceptionAsError(new AmqpException(false,
                            "linkName[%s], deliveryTag[%s]. Retrying updateDisposition failed to dispatch to Reactor.",
                            error, handler.getErrorContext(receiver)));

                        completeWorkItem(lockToken, delivery, workItem.getSink(), amqpException);
                    }
                }

                break;
            case Released:
                final Throwable cancelled = new AmqpException(false, AmqpErrorCondition.OPERATION_CANCELLED,
                    "AMQP layer unexpectedly aborted or disconnected.", handler.getErrorContext(receiver));

                logger.info("deliveryTag[{}], state[{}]. Completing pending updateState operation with exception.",
                    lockToken, remoteState.getType(), cancelled);

                completeWorkItem(lockToken, delivery, workItem.getSink(), cancelled);
                break;
            default:
                final AmqpException error = new AmqpException(false, remoteOutcome.toString(),
                    handler.getErrorContext(receiver));

                logger.info("deliveryTag[{}], state[{}] Completing pending updateState operation with exception.",
                    lockToken, remoteState.getType(), error);

                completeWorkItem(lockToken, delivery, workItem.getSink(), error);
                break;
        }
    }

    private void cleanupWorkItems() {
        logger.verbose("linkName[{}]: Cleaning timed out update work tasks.", getLinkName());
        pendingUpdates.forEach((key, value) -> {
            if (value == null || !value.hasTimedout()) {
                return;
            }

            pendingUpdates.remove(key);
            final Throwable error = value.getLastException() != null
                ? value.getLastException()
                : new AmqpException(true, AmqpErrorCondition.TIMEOUT_ERROR, "Update disposition request timed out.",
                handler.getErrorContext(receiver));

            completeWorkItem(key, null, value.getSink(), error);
        });
    }

    private void completeWorkItem(String lockToken, Delivery delivery, MonoSink<Void> sink, Throwable error) {
        final boolean isSettled = delivery != null && delivery.remotelySettled();
        if (isSettled) {
            delivery.settle();
        }

        if (error != null) {
            final Throwable loggedError = error instanceof RuntimeException
                ? logger.logExceptionAsError((RuntimeException) error)
                : error;
            sink.error(loggedError);
        } else {
            sink.success();
        }

        if (isSettled) {
            pendingUpdates.remove(lockToken);
            unsettledDeliveries.remove(lockToken);
        }
    }

    private static final class UpdateDispositionWorkItem {
        private final String lockToken;
        private final DeliveryState state;
        private final Duration timeout;
        private final AtomicInteger retryAttempts = new AtomicInteger();
        private final AtomicBoolean isDisposed = new AtomicBoolean();

        private Mono<Void> mono;
        private Instant expirationTime;
        private MonoSink<Void> sink;
        private Throwable throwable;

        private UpdateDispositionWorkItem(String lockToken, DeliveryState state, Duration timeout) {
            this.lockToken = lockToken;
            this.state = state;
            this.timeout = timeout;
        }

        private boolean hasTimedout() {
            return expirationTime.isBefore(Instant.now());
        }

        private void resetStartTime() {
            this.expirationTime = Instant.now().plus(timeout);
        }

        private int incrementRetry() {
            return retryAttempts.incrementAndGet();
        }

        private Throwable getLastException() {
            return throwable;
        }

        private void setLastException(Throwable throwable) {
            this.throwable = throwable;
        }

        private void setMono(Mono<Void> mono) {
            this.mono = mono;
        }

        private Mono<Void> getMono() {
            return mono;
        }

        private MonoSink<Void> getSink() {
            return sink;
        }

        private void start(MonoSink<Void> sink) {
            Objects.requireNonNull(sink, "'sink' cannot be null.");
            this.sink = sink;
            this.sink.onDispose(() -> isDisposed.set(true));
            this.sink.onCancel(() -> isDisposed.set(true));
            resetStartTime();
        }

        private DeliveryState getDeliveryState() {
            return state;
        }

        public String getLockToken() {
            return lockToken;
        }
    }
}
