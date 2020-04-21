// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.ReactorReceiver;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Outcome;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.MessageUtils.LOCK_TOKEN_SIZE;

/**
 * A proton-j receiver for Service Bus.
 */
public class ServiceBusReactorReceiver extends ReactorReceiver {
    private static final Message EMPTY_MESSAGE = Proton.message();
    private final ClientLogger logger = new ClientLogger(ServiceBusReactorReceiver.class);
    private final ConcurrentHashMap<String, Delivery> unsettledDeliveries = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UpdateDispositionWorkItem> pendingUpdates = new ConcurrentHashMap<>();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Disposable subscription;
    private final Receiver receiver;

    /**
     * Indicates whether the message has already been settled from the sender side. This is the case when {@link
     * ReceiveMode#RECEIVE_AND_DELETE} is used.
     */
    private final boolean isSettled;
    private final Duration timeout;
    private final AmqpRetryPolicy retryPolicy;
    private final ReceiveLinkHandler handler;
    private final ReactorProvider provider;

    public ServiceBusReactorReceiver(String entityPath, Receiver receiver, ReceiveLinkHandler handler,
        TokenManager tokenManager, ReactorProvider provider, Duration timeout, AmqpRetryPolicy retryPolicy) {
        super(entityPath, receiver, handler, tokenManager, provider.getReactorDispatcher());
        this.receiver = receiver;
        this.handler = handler;
        this.provider = provider;
        this.isSettled = receiver.getSenderSettleMode() == SenderSettleMode.SETTLED;
        this.timeout = timeout;
        this.retryPolicy = retryPolicy;
        this.subscription = Flux.interval(timeout).subscribe(i -> {
            logger.verbose("linkName[{}]: Cleaning timed out update work tasks.", entityPath);

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
        });
    }

    public Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState) {
        final Delivery unsettled = unsettledDeliveries.get(lockToken);
        if (unsettled == null) {
            logger.warning("entityPath[{}], linkName[{}], deliveryTag[{}]. Delivery not found to update disposition.",
                getEntityPath(), getLinkName(), lockToken);

            return monoError(logger, Exceptions.propagate(new IllegalArgumentException(
                "Delivery not on receive link.")));
        }

        return Mono.create(sink -> {
            final UpdateDispositionWorkItem workItem = new UpdateDispositionWorkItem(deliveryState, sink, timeout);
            try {
                provider.getReactorDispatcher().invoke(() -> {
                    unsettled.disposition(deliveryState);
                    pendingUpdates.put(lockToken, workItem);
                });
            } catch (IOException error) {
                sink.error(new AmqpException(false, "updateDisposition failed while dispatching to Reactor.",
                    error, handler.getErrorContext(receiver)));
            }
        });
    }

    @Override
    public Flux<Message> receive() {
        // Remove empty update disposition messages. The deliveries themselves are ACKs with no message.
        return super.receive().filter(message -> message != EMPTY_MESSAGE);
    }

    @Override
    public void dispose() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        subscription.dispose();
        super.dispose();
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
                unsettledDeliveries.put(lockToken.toString(), delivery);
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

    /**
     * Updates the outcome of a delivery. This occurs when a message is being settled from the receiver side.
     *
     * @param delivery Delivery to update.
     */
    private void updateOutcome(String lockToken, Delivery delivery) {
        final DeliveryState remoteState = delivery.getRemoteState();

        logger.verbose("entityPath[{}], linkName[{}], deliveryTag[{}], state[{}]. Received delivery.",
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
                final Throwable exception = MessageUtils.toException(rejected.getError(),
                    handler.getErrorContext(receiver));

                final Duration retry = retryPolicy.calculateRetryDelay(exception, workItem.incrementRetry());
                if (retry == null) {
                    logger.info("deliveryTag[{}], state[{}]. Retry attempts exhausted.", lockToken, exception);
                    workItem.getSink().error(exception);
                } else {
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
                final Throwable cancelled = MessageUtils.toException(ServiceBusErrorCondition.OPERATION_CANCELLED,
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
        private final DeliveryState state;
        private final MonoSink<Void> sink;
        private final Duration timeout;
        private final AtomicInteger retryAttempts = new AtomicInteger();
        private final AtomicBoolean isDisposed = new AtomicBoolean();
        private Instant expirationTime;

        private Throwable throwable;

        private UpdateDispositionWorkItem(DeliveryState state, MonoSink<Void> sink, Duration timeout) {
            this.state = state;
            this.sink = sink;
            this.timeout = timeout;
            this.sink.onDispose(() -> isDisposed.set(true));
            this.sink.onCancel(() -> isDisposed.set(true));

            resetStartTime();
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

        private DeliveryState getDeliveryState() {
            return state;
        }

        public MonoSink<Void> getSink() {
            return sink;
        }
    }
}
