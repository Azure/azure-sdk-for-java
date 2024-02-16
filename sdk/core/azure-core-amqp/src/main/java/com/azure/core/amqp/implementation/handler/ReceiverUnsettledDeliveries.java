// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.amqp.messaging.Outcome;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.DeliveryState.DeliveryStateType;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Delivery;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.amqp.implementation.ClientConstants.CALL_SITE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.DELIVERY_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.DELIVERY_STATE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * Manages the received deliveries which are not settled on the broker. The client can later request settlement of each
 * delivery by sending a disposition frame with a state representing the desired-outcome, which the application wishes
 * to occur at the broker. The broker acknowledges this with a disposition frame with a state (a.k.a. remote-state)
 * representing the actual outcome (a.k.a. remote-outcome) of any work the broker performed upon processing the
 * settlement request and a flag (a.k.a. remotely-settled) indicating whether the broker settled the delivery.
 */
public final class ReceiverUnsettledDeliveries implements AutoCloseable {
    // Ideally value of this const should be 'deliveryTag' but given the only use case today is as Service Bus
    // LockToken, while logging, use the value 'lockToken' to ease log parsing.
    // (TODO: anuchan; consider parametrizing the value of deliveryTag?).
    private static final String DELIVERY_TAG_KEY = "lockToken";
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final String hostname;
    private final String entityPath;
    private final String receiveLinkName;
    private final ReactorDispatcher dispatcher;
    private final AmqpRetryPolicy retryPolicy;
    private final Duration timeout;
    private final UUID deliveryEmptyTag;
    private final ClientLogger logger;
    // The timer to timeout in progress but expired dispositions.
    private final Disposable timoutTimer;
    private final boolean settleOnClose;

    // The deliveries received, for those the application haven't sent disposition frame to the broker requesting
    // settlement or disposition frame is sent, but yet to receive acknowledgment disposition frame from
    // the broker indicating the outcome (a.k.a. remote-outcome).
    private final ConcurrentHashMap<String, Delivery> deliveries = new ConcurrentHashMap<>();
    // A collection of work, where each work representing the disposition frame that the application sent,
    // waiting to receive an acknowledgment disposition frame from the broker indicating the outcome
    // (a.k.a. remote-outcome).
    private final ConcurrentHashMap<String, DispositionWork> pendingDispositions = new ConcurrentHashMap<>();

    /**
     * Creates ReceiverUnsettledDeliveries.
     *
     * @param hostname the name of the host hosting the messaging entity identified by {@code entityPath}.
     * @param entityPath the relative path identifying the messaging entity from which the deliveries are received from,
     * the application can later disposition these deliveries by sending disposition frames to the broker.
     * @param receiveLinkName the name of the amqp receive-link 'Attach'-ed to the messaging entity from which the
     * deliveries are received from.
     * @param dispatcher the dispatcher to invoke the ProtonJ library API to send disposition frame.
     * @param retryOptions the retry configuration to use when resending a disposition frame that the broker
     * 'Rejected'.
     * @param deliveryEmptyTag reference to static UUID indicating absence of delivery tag in deliveries.
     * @param logger the logger. Note: This Ctr and settleOnClose will be removed once the v1 receiver is removed.
     */
    public ReceiverUnsettledDeliveries(String hostname, String entityPath, String receiveLinkName,
        ReactorDispatcher dispatcher, AmqpRetryOptions retryOptions, UUID deliveryEmptyTag, ClientLogger logger) {
        this.hostname = hostname;
        this.entityPath = entityPath;
        this.receiveLinkName = receiveLinkName;
        this.dispatcher = dispatcher;
        this.retryPolicy = RetryUtil.getRetryPolicy(retryOptions);
        this.timeout = retryOptions.getTryTimeout();
        this.deliveryEmptyTag = deliveryEmptyTag;
        this.logger = logger;
        this.timoutTimer = Flux.interval(timeout).subscribe(__ -> completeDispositionWorksOnTimeout("timer"));
        this.settleOnClose = false;
    }

    /**
     * Creates ReceiverUnsettledDeliveries.
     *
     * @param hostname the name of the host hosting the messaging entity identified by {@code entityPath}.
     * @param entityPath the relative path identifying the messaging entity from which the deliveries are received from,
     * the application can later disposition these deliveries by sending disposition frames to the broker.
     * @param receiveLinkName the name of the amqp receive-link 'Attach'-ed to the messaging entity from which the
     * deliveries are received from.
     * @param dispatcher the dispatcher to invoke the ProtonJ library API to send disposition frame.
     * @param retryOptions the retry configuration to use when resending a disposition frame that the broker
     * 'Rejected'.
     * @param logger the logger.
     */
    ReceiverUnsettledDeliveries(String hostname, String entityPath, String receiveLinkName,
        ReactorDispatcher dispatcher, AmqpRetryOptions retryOptions, ClientLogger logger) {
        this.hostname = hostname;
        this.entityPath = entityPath;
        this.receiveLinkName = receiveLinkName;
        this.dispatcher = dispatcher;
        this.retryPolicy = RetryUtil.getRetryPolicy(retryOptions);
        this.timeout = retryOptions.getTryTimeout();
        this.deliveryEmptyTag = com.azure.core.amqp.implementation.handler.ReceiverDeliveryHandler.DELIVERY_EMPTY_TAG;
        this.logger = logger;
        this.timoutTimer = Flux.interval(timeout).subscribe(__ -> completeDispositionWorksOnTimeout("timer"));
        this.settleOnClose = true;
    }

    /**
     * Function to notify a received delivery that is unsettled on the broker side; the application can later use
     * {@link ReceiverUnsettledDeliveries#sendDisposition(String, DeliveryState)} to send a disposition frame requesting
     * settlement of this delivery at the broker.
     *
     * @param deliveryTag the unique delivery tag associated with the {@code delivery}.
     * @param delivery the delivery.
     * @return {@code false} if the instance was closed upon notifying the delivery, {@code true} otherwise.
     */
    public boolean onDelivery(UUID deliveryTag, Delivery delivery) {
        if (isTerminated.get()) {
            return false;
        } else {
            // Continue using putIfAbsent as legacy T1 library.
            deliveries.putIfAbsent(deliveryTag.toString(), delivery);
            return true;
        }
    }

    /**
     * Check if a delivery with the given delivery tag was received.
     *
     * @param deliveryTag the delivery tag.
     * @return {@code true} if delivery with the given delivery tag exists {@code false} otherwise.
     */
    public boolean containsDelivery(UUID deliveryTag) {
        // Note: This method, by design, does not check 'isTerminated' flag since 'onDispositionAck' needs to
        // stay open during termination.
        return deliveryTag != deliveryEmptyTag && deliveries.containsKey(deliveryTag.toString());
    }

    /**
     * Request settlement of delivery (with the unique {@code deliveryTag}) by sending a disposition frame with a state
     * representing the desired-outcome, which the application wishes to occur at the broker. Disposition frame is sent
     * via the same amqp receive-link that delivered the delivery, which was notified through
     * {@link ReceiverUnsettledDeliveries#onDelivery(UUID, Delivery)}.
     *
     * @param deliveryTag the unique delivery tag identifying the delivery.
     * @param desiredState The state to include in the disposition frame indicating the desired-outcome that the
     * application wish to occur at the broker.
     * @return the {@link Mono} upon subscription starts the work by requesting ProtonJ library to send disposition
     * frame to settle the delivery on the broker, and this Mono terminates once the broker acknowledges with
     * disposition frame indicating outcome (a.ka. remote-outcome). The Mono can terminate if the configured timeout
     * elapses or cannot initiate the request to ProtonJ library.
     */
    public Mono<Void> sendDisposition(String deliveryTag, DeliveryState desiredState) {
        if (isTerminated.get()) {
            return monoError(logger, DeliveryNotOnLinkException.linkClosed(deliveryTag, desiredState));
        } else {
            return sendDispositionImpl(deliveryTag, desiredState);
        }
    }

    /**
     * The function to notify the broker's acknowledgment in response to a disposition frame sent to the broker via
     * {@link ReceiverUnsettledDeliveries#sendDisposition(String, DeliveryState)}. The broker acknowledgment is also a
     * disposition frame; the ProtonJ library will map this disposition frame to the same Delivery in-memory object for
     * which the application requested disposition. As part of mapping, the remote-state (representing remote-outcome)
     * and is-remotely-settled (boolean) property of the Delivery object is updated from the disposition frame ack.
     *
     * @param deliveryTag the unique delivery tag of the delivery that application requested disposition.
     * @param delivery the delivery object updated from the broker's transfer frame ack.
     */
    public void onDispositionAck(UUID deliveryTag, Delivery delivery) {
        // Note: It's by design that this method doesn't check for the 'isTerminated' flag. This ack route needs to
        // stay open for potential concurrent termination-route awaiting for in-progress dispositions
        // completion/timeout.
        // termination-route == 'terminateAndAwaitForDispositionsInProgressToComplete'

        final DeliveryState remoteState = delivery.getRemoteState();

        logger.atVerbose()
            .addKeyValue(DELIVERY_TAG_KEY, deliveryTag)
            .addKeyValue(DELIVERY_STATE_KEY, remoteState)
            .log("onDispositionAck");

        final Outcome remoteOutcome;
        if (remoteState instanceof Outcome) {
            remoteOutcome = (Outcome) remoteState;
        } else if (remoteState instanceof TransactionalState) {
            remoteOutcome = ((TransactionalState) remoteState).getOutcome();
        } else {
            remoteOutcome = null;
        }

        if (remoteOutcome == null) {
            logger.atWarning()
                .addKeyValue(DELIVERY_TAG_KEY, deliveryTag)
                .addKeyValue(DELIVERY_KEY, delivery)
                .log("No outcome associated with delivery.");

            return;
        }

        final DispositionWork work = pendingDispositions.get(deliveryTag.toString());
        if (work == null) {
            logger.atWarning()
                .addKeyValue(DELIVERY_TAG_KEY, deliveryTag)
                .addKeyValue(DELIVERY_KEY, delivery)
                .log("No pending update for delivery.");
            return;
        }

        // the outcome that application desired.
        final DeliveryStateType desiredOutcomeType = work.getDesiredState().getType();
        // the outcome that broker actually attained.
        final DeliveryStateType remoteOutcomeType = remoteState.getType();

        if (desiredOutcomeType == remoteOutcomeType) {
            completeDispositionWorkWithSettle(work, delivery, null);
        } else {
            logger.atInfo()
                .addKeyValue(DELIVERY_TAG_KEY, deliveryTag)
                .addKeyValue("receivedDeliveryState", remoteState)
                .addKeyValue(DELIVERY_STATE_KEY, work.getDesiredState())
                .log("Received delivery state doesn't match expected state.");

            if (remoteOutcomeType == DeliveryStateType.Rejected) {
                handleRetriableRejectedRemoteOutcome(work, delivery, (Rejected) remoteOutcome);
            } else {
                handleReleasedOrUnknownRemoteOutcome(work, delivery, remoteOutcome);
            }
        }
    }

    /**
     * Terminate this {@link ReceiverUnsettledDeliveries} including already expired disposition works, and await to
     * complete all disposition work in progress, with AmqpRetryOptions_tryTimeout as the upper bound for the wait time.
     * Future attempts to notify unsettled deliveries or send delivery dispositions will be rejected.
     * <p>
     * From the point of view of this function's call site, it is still possible that the receive-link and dispatcher
     * may healthy, but not guaranteed. If healthy, send-receive of disposition frames are possible, enabling 'graceful'
     * completion of works.
     * <p>
     * e.g., if the user proactively initiates the closing of client, it is likely that the receive-link may be healthy.
     * On the other hand, if the broker initiates the closing of the link, further frame transfer may not be possible.
     *
     * @return a {@link Mono} that await to complete disposition work in progress, the wait has an upper bound of
     * AmqpRetryOptions_tryTimeout.
     */
    public Mono<Void> terminateAndAwaitForDispositionsInProgressToComplete() {
        // 1. Mark this ReceiverUnsettledDeliveries as terminated, so it no longer accept unsettled deliveries
        // or disposition requests
        isTerminated.getAndSet(true);

        // 2. then complete already expired (timed-out) works,
        completeDispositionWorksOnTimeout("terminateAndAwaitForDispositionsInProgressToComplete");

        // 3. then obtain a Mono that wait, with AmqpRetryOptions_tryTimeout as the upper bound for the maximum
        // wait, for the completion of all disposition work in progress, including committing open transactions.
        // The AmqpRetryOptions_tryTimeout is applied implicitly through timeoutTimer.
        final List<Mono<Void>> workMonoList = new ArrayList<>();
        final StringJoiner deliveryTags = new StringJoiner(", ");
        for (DispositionWork work : pendingDispositions.values()) {
            if (work == null || work.hasTimedout()) {
                continue;
            }
            if (work.getDesiredState() instanceof TransactionalState) {
                final Mono<Void> workMono = sendDispositionImpl(work.getDeliveryTag(), Released.getInstance());
                workMonoList.add(workMono);
            } else {
                workMonoList.add(work.getMono());
            }
            deliveryTags.add(work.getDeliveryTag());
        }

        final Mono<Void> workMonoListMerged;
        if (!workMonoList.isEmpty()) {
            logger.info("Waiting for pending updates to complete. Locks: {}", deliveryTags.toString());
            workMonoListMerged = Mono.whenDelayError(workMonoList).onErrorResume(error -> {
                logger.info("There was exception(s) while disposing of all disposition work.", error);
                return Mono.empty();
            });
        } else {
            workMonoListMerged = Mono.empty();
        }
        // 4. finally, Given this is a terminal API in which the timeoutTimer will be used last time,
        // termination also disposes of the timer.
        return workMonoListMerged.doFinally(__ -> timoutTimer.dispose());
    }

    /**
     * Closes this {@link ReceiverUnsettledDeliveries} and force complete any uncompleted work. Future attempts to
     * notify unsettled deliveries or send delivery dispositions will be rejected.
     */
    @Override
    public void close() {
        isTerminated.getAndSet(true);

        if (settleOnClose) {
            // Settle unsettled deliveries to remove them from receive-link's parent ProtonJ TransportSession.
            //
            if (!deliveries.isEmpty()) {
                final Runnable localSettlement = () -> {
                    for (Delivery delivery : deliveries.values()) {
                        delivery.disposition(new Modified());
                        delivery.settle();
                    }
                };

                try {
                    dispatcher.invoke(localSettlement);
                } catch (IOException e) {
                    logger.info("IO sink was closed when scheduling local settlement. Manually settling.", e);
                    localSettlement.run();
                } catch (RejectedExecutionException e) {
                    logger.info("RejectedExecutionException when scheduling local settlement. Manually settling.", e);
                    localSettlement.run();
                }
            }
        }

        // Disposes of subscription to the global interval timer.
        timoutTimer.dispose();

        // Force complete all uncompleted works.
        completeDispositionWorksOnClose();
    }

    /**
     * See the doc for {@link ReceiverUnsettledDeliveries#sendDisposition(String, DeliveryState)}.
     *
     * @param deliveryTag the unique delivery tag identifying the delivery.
     * @param desiredState The state to include in the disposition frame indicating the desired-outcome that the
     * application wish to occur at the broker.
     * @return the {@link Mono} representing disposition work.
     */
    private Mono<Void> sendDispositionImpl(String deliveryTag, DeliveryState desiredState) {
        final Delivery delivery = deliveries.get(deliveryTag);
        if (delivery == null) {
            return monoError(logger, DeliveryNotOnLinkException.noMatchingDelivery(deliveryTag, desiredState));
        }

        final DispositionWork work = new DispositionWork(deliveryTag, desiredState, timeout);

        final Mono<Void> mono = Mono.<Void>create(sink -> {
            work.onStart(sink);
            try {
                dispatcher.invoke(() -> {
                    delivery.disposition(desiredState);
                    if (pendingDispositions.putIfAbsent(deliveryTag, work) != null) {
                        work.onComplete(
                            new AmqpException(false, "A disposition requested earlier is waiting for the broker's ack; "
                                + "a new disposition request is not allowed.", null));
                    }
                });
            } catch (IOException | RejectedExecutionException dispatchError) {
                work.onComplete(new AmqpException(false, "updateDisposition failed while dispatching to Reactor.",
                    dispatchError, getErrorContext(delivery)));
            }
        });

        work.setMono(mono);

        return work.getMono();
    }

    /**
     * Handles the 'Rejected' outcome (in a disposition ack) from the broker in-response to a disposition frame
     * application sent.
     *
     * @param work the work that sent the disposition frame with a desired-outcome which broker 'Rejected'.
     * @param delivery the Delivery in-memory object for which the application had sent the disposition frame; the
     * ProtonJ library updates the remote-state (representing remote-outcome) and is-remotely-settled (boolean) property
     * of the Delivery object from the disposition frame ack.
     * @param remoteOutcome the 'Rejected' remote-outcome describing the rejection reason, this is derived from the
     * remote-state.
     */
    private void handleRetriableRejectedRemoteOutcome(DispositionWork work, Delivery delivery, Rejected remoteOutcome) {
        final AmqpErrorContext amqpErrorContext = getErrorContext(delivery);
        final ErrorCondition errorCondition = remoteOutcome.getError();
        final Throwable error = ExceptionUtil.toException(errorCondition.getCondition().toString(),
            errorCondition.getDescription(), amqpErrorContext);

        final Duration retry = retryPolicy.calculateRetryDelay(error, work.getTryCount());
        if (retry != null) {
            work.onRetriableRejectedOutcome(error);
            try {
                dispatcher.invoke(() -> delivery.disposition(work.getDesiredState()));
            } catch (IOException | RejectedExecutionException dispatchError) {
                final Throwable amqpException = logger.atError()
                    .addKeyValue(DELIVERY_TAG_KEY, work.getDeliveryTag())
                    .addKeyValue(LINK_NAME_KEY, receiveLinkName)
                    .log(new AmqpException(false, "Retrying updateDisposition failed to dispatch to Reactor.",
                        dispatchError, getErrorContext(delivery)));

                completeDispositionWorkWithSettle(work, delivery, amqpException);
            }
        } else {
            logger.atInfo()
                .addKeyValue(DELIVERY_TAG_KEY, work.getDeliveryTag())
                .addKeyValue(DELIVERY_STATE_KEY, delivery.getRemoteState())
                .log("Retry attempts exhausted.", error);

            completeDispositionWorkWithSettle(work, delivery, error);
        }
    }

    /**
     * Handles the 'Released' or unknown outcome (in a disposition ack) from the broker in-response to a disposition
     * frame application sent.
     *
     * @param work the work that sent the disposition frame with a desired-outcome.
     * @param delivery the Delivery in-memory object for which the application had sent the disposition frame; the
     * ProtonJ library updates the remote-state (representing remote-outcome) and is-remotely-settled (boolean) property
     * of the Delivery object from the disposition frame ack.
     * @param remoteOutcome the remote-outcome from the broker describing the reason for broker choosing an outcome
     * different from requested desired-outcome, this is derived from the remote-state.
     */
    private void handleReleasedOrUnknownRemoteOutcome(DispositionWork work, Delivery delivery, Outcome remoteOutcome) {
        final AmqpErrorContext amqpErrorContext = getErrorContext(delivery);
        final AmqpException completionError;

        final DeliveryStateType remoteOutcomeType = delivery.getRemoteState().getType();
        if (remoteOutcomeType == DeliveryStateType.Released) {
            completionError = new AmqpException(false, AmqpErrorCondition.OPERATION_CANCELLED,
                "AMQP layer unexpectedly aborted or disconnected.", amqpErrorContext);
        } else {
            completionError = new AmqpException(false, remoteOutcome.toString(), amqpErrorContext);
        }

        logger.atInfo()
            .addKeyValue(DELIVERY_TAG_KEY, work.getDeliveryTag())
            .addKeyValue(DELIVERY_STATE_KEY, delivery.getRemoteState())
            .log("Completing pending updateState operation with exception.", completionError);

        completeDispositionWorkWithSettle(work, delivery, completionError);
    }

    /**
     * Iterate through all the current {@link DispositionWork} and complete the work those are timed out.
     */
    private void completeDispositionWorksOnTimeout(String callSite) {
        if (pendingDispositions.isEmpty()) {
            return;
        }

        final int[] completionCount = new int[1];
        final StringJoiner deliveryTags = new StringJoiner(", ");

        pendingDispositions.forEach((deliveryTag, work) -> {
            if (work == null || !work.hasTimedout()) {
                return;
            }

            if (completionCount[0] == 0) {
                logger.atInfo()
                    .addKeyValue(CALL_SITE_KEY, callSite)
                    .log("Starting completion of timed out disposition works.");
            }

            final Throwable completionError;
            if (work.getRejectedOutcomeError() != null) {
                completionError = work.getRejectedOutcomeError();
            } else {
                completionError = new AmqpException(true, AmqpErrorCondition.TIMEOUT_ERROR,
                    "Update disposition request timed out.", getErrorContext(deliveries.get(work.getDeliveryTag())));
            }
            deliveryTags.add(work.getDeliveryTag());
            completeDispositionWork(work, completionError);
            completionCount[0]++;
        });

        if (completionCount[0] > 0) {
            // The log help debug if the user code chained to the work-mono (DispositionWork::getMono()) never returns.
            logger.atInfo()
                .addKeyValue(CALL_SITE_KEY, callSite)
                .addKeyValue("locks", deliveryTags.toString())
                .log("Completed {} timed-out disposition works.", completionCount[0]);
        }
    }

    /**
     * Iterate through all the {@link DispositionWork}, and 'force' to complete the uncompleted works because this
     * {@link ReceiverUnsettledDeliveries} is closed.
     */
    private void completeDispositionWorksOnClose() {
        // Note: Possible to have one function for cleaning both timeout and incomplete works, but readability
        // seems to be affected, hence separate functions.

        if (pendingDispositions.isEmpty()) {
            return;
        }

        final int[] completionCount = new int[1];
        final StringJoiner deliveryTags = new StringJoiner(", ");

        final AmqpException completionError = new AmqpException(false,
            "The receiver didn't receive the disposition acknowledgment due to receive link closure.", null);

        pendingDispositions.forEach((deliveryTag, work) -> {
            if (work == null || work.isCompleted()) {
                return;
            }

            if (completionCount[0] == 0) {
                logger.info("Starting completion of disposition works as part of receive link closure.");
            }

            deliveryTags.add(work.getDeliveryTag());
            completeDispositionWork(work, completionError);
            completionCount[0]++;
        });

        if (completionCount[0] > 0) {
            // The log help debug if the user code chained to the work-mono (DispositionWork::getMono()) never returns.
            logger.info("Completed {} disposition works as part of receive link closure. Locks {}", completionCount[0],
                deliveryTags.toString());
        }
    }

    /**
     * Completes the given {@link DispositionWork}, which results in termination of the {@link Mono} returned from the
     * {@link DispositionWork#getMono()} API. If the broker settled the {@link Delivery} associated with the work, it
     * would also be locally settled.
     * <p>
     * Invocations of this function are guaranteed to be serial, as all call sites originate from
     * {@link ReceiverUnsettledDeliveries#onDispositionAck(UUID, Delivery)} running on the ProtonJ Reactor event-loop
     * thread.
     *
     * @param work the work to complete.
     * @param delivery the delivery that the work attempted the disposition, to be locally settled if the broker settled
     * it on the remote end.
     * @param completionError a null value indicates that the work has to complete successfully, otherwise complete the
     * work with the error value.
     */
    private void completeDispositionWorkWithSettle(DispositionWork work, Delivery delivery, Throwable completionError) {
        // The operation ordering same as the T1 Lib: "delivery-settling -> work-completion -> work-delivery-removal".

        final boolean isRemotelySettled = delivery.remotelySettled();
        if (isRemotelySettled) {
            delivery.settle();
        }

        if (completionError != null) {
            final Throwable loggedError = completionError instanceof RuntimeException
                ? logger.logExceptionAsError((RuntimeException) completionError)
                : completionError;
            work.onComplete(loggedError);
        } else {
            work.onComplete();
        }

        if (isRemotelySettled) {
            final String deliveryTag = work.getDeliveryTag();
            pendingDispositions.remove(deliveryTag);
            deliveries.remove(deliveryTag);
        }
    }

    /**
     * Completes the given {@link DispositionWork} with error, which results in termination of the {@link Mono} returned
     * from the {@link DispositionWork#getMono()} API.
     *
     * @param work the work to complete with error.
     * @param completionError the non-null error value.
     */
    private void completeDispositionWork(DispositionWork work, Throwable completionError) {
        // The operation ordering same as the T1 Lib: "work-removal -> work-completion".

        pendingDispositions.remove(work.getDeliveryTag());

        final Throwable loggedError = completionError instanceof RuntimeException
            ? logger.logExceptionAsError((RuntimeException) completionError)
            : completionError;
        work.onComplete(loggedError);
    }

    /**
     * Gets the error context from the receive-link associated with the delivery.
     *
     * @param delivery the delivery.
     * @return the error context from delivery's receive-link, {@code null} if the delivery or receive-link is
     * {@code null}.
     */
    private AmqpErrorContext getErrorContext(Delivery delivery) {
        if (delivery == null || delivery.getLink() == null) {
            return null;
        }
        return LinkHandler.getErrorContext(hostname, entityPath, delivery.getLink());
    }

    /**
     * Represents a work that, upon starting, requests ProtonJ library to send a disposition frame to settle a delivery
     * on the broker and the work completes when the broker acknowledges with a disposition frame indicating the
     * outcome. The work can complete with an error if it cannot initiate the request to the ProtonJ library or the
     * configured timeout elapses.
     * <p>
     * The work is started once the application is subscribed to the {@link Mono} returned by
     * {@link DispositionWork#getMono()}; the Mono is terminated upon the work completion.
     * </p>
     */
    private static final class DispositionWork extends AtomicBoolean {
        private final AtomicInteger tryCount = new AtomicInteger(1);
        private final String deliveryTag;
        private final DeliveryState desiredState;
        private final Duration timeout;
        private Mono<Void> mono;
        private MonoSink<Void> monoSink;
        private Instant expirationTime;
        private Throwable rejectedOutcomeError;

        /**
         * Create a DispositionWork.
         *
         * @param deliveryTag The delivery tag of the Delivery for which to send the disposition frame requesting
         * delivery settlement on the broker.
         * @param desiredState The state to include in the disposition frame indicating the desired-outcome the
         * application wish to occur at the broker.
         * @param timeout after requesting the ProtonJ library to send the disposition frame, how long to wait for an
         * acknowledgment disposition frame to arrive from the broker.
         */
        DispositionWork(String deliveryTag, DeliveryState desiredState, Duration timeout) {
            this.deliveryTag = deliveryTag;
            this.desiredState = desiredState;
            this.timeout = timeout;
            this.monoSink = null;
        }

        /**
         * Gets the delivery tag.
         *
         * @return the delivery tag.
         */
        String getDeliveryTag() {
            return deliveryTag;
        }

        /**
         * Gets the state indicating the desired-outcome which the application wishes to occur at the broker. The
         * disposition frame send to the broker includes this desired state.
         *
         * @return the desired state.
         */
        DeliveryState getDesiredState() {
            return desiredState;
        }

        /**
         * Gets the number of times the work was tried.
         *
         * @return the try count.
         */
        int getTryCount() {
            return tryCount.get();
        }

        /**
         * Gets the error received from the broker when the outcome of the last disposition attempt (by sending a
         * disposition frame) happened to be 'Rejected'.
         *
         * @return the error in the disposition ack frame from the broker with 'Rejected' outcome, null if no such
         * disposition ack frame received.
         */
        Throwable getRejectedOutcomeError() {
            return rejectedOutcomeError;
        }

        /**
         * Check if the work has timed out.
         *
         * @return {@code true} if the work has timed out, {@code false} otherwise.
         */
        boolean hasTimedout() {
            return expirationTime != null && expirationTime.isBefore(Instant.now());
        }

        /**
         * Gets the {@link Mono} upon subscription starts the work by requesting ProtonJ library to send disposition
         * frame to settle a delivery on the broker, and this Mono terminates once the broker acknowledges with
         * disposition frame indicating settlement outcome (a.k.a. remote-outcome) The Mono can terminate if the
         * configured timeout elapses or cannot initiate the request to ProtonJ library.
         *
         * @return the mono
         */
        Mono<Void> getMono() {
            return mono;
        }

        /**
         * Sets the {@link Mono}, where the application can obtain cached version of it from
         * {@link DispositionWork#getMono()} and subscribe to start the work, the mono terminates upon the successful or
         * unsuccessful completion of the work.
         *
         * @param mono the mono
         */
        void setMono(Mono<Void> mono) {
            // cache() the mono to replay the result when subscribed more than once, avoid multiple
            // disposition placement (and enables a possible second subscription to be safe when closing
            // the UnsettledDeliveries type).
            this.mono = mono.cache();
        }

        /**
         * Check if this work is already completed.
         *
         * @return {@code true} if the work is completed, {@code true} otherwise.
         */
        boolean isCompleted() {
            return this.get();
        }

        /**
         * The function invoked once the application start the work by subscribing to the {@link Mono} obtained from
         * {@link DispositionWork#getMono()}.
         *
         * @param monoSink the {@link MonoSink} to notify the completion of the work, which triggers termination of the
         * same {@link Mono} that started the work.
         */
        void onStart(MonoSink<Void> monoSink) {
            this.monoSink = monoSink;
            expirationTime = Instant.now().plus(timeout);
        }

        /**
         * The function invoked when the work is about to be restarted/retried. The broker may return an outcome named
         * 'Rejected' if it is unable to attain the desired-outcome that the application specified in the disposition
         * frame; in this case, the work is retried based on the configured retry settings.
         *
         * @param error the error that the broker returned upon Reject-ing the last work execution attempting the
         * disposition.
         */
        void onRetriableRejectedOutcome(Throwable error) {
            this.rejectedOutcomeError = error;
            expirationTime = Instant.now().plus(timeout);
            tryCount.incrementAndGet();
        }

        /**
         * the function invoked upon the successful completion of the work.
         */
        void onComplete() {
            this.set(true);
            Objects.requireNonNull(monoSink);
            monoSink.success();
        }

        /**
         * the function invoked when the work is completed with an error.
         *
         * @param error the error reason.
         */
        void onComplete(Throwable error) {
            this.set(true);
            Objects.requireNonNull(monoSink);
            monoSink.error(error);
        }
    }
}
