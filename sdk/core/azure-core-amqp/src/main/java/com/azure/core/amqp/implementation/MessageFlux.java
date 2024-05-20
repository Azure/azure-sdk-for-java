// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.implementation.handler.DeliveryNotOnLinkException;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.azure.core.amqp.implementation.ClientConstants.CONNECTION_ID_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.DELIVERY_STATE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.DELIVERY_TAG_KEY;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * The Flux operator to stream messages reliably from a messaging entity (e.g., Event Hub partition,
 * Service Bus Queue, Topic) to downstream message subscriber.
 */
public final class MessageFlux extends FluxOperator<AmqpReceiveLink, Message> {
    /** An AmqpRetryPolicy const indicates that MessageFlux should terminate when the first receiver terminates
     * (i.e., disables the retry action to obtain next receiver from the upstream).
     **/
    public static final AmqpRetryPolicy NULL_RETRY_POLICY = new FixedAmqpRetryPolicy(new AmqpRetryOptions());
    private static final String MESSAGE_FLUX_KEY = "messageFlux";
    private final ClientLogger logger;
    /**
     * The prefetch value used by the credit computation strategy.
     */
    private final int prefetch;
    /**
     * The mode representing the strategy to compute and send the receiver credit.
     * See {@link CreditAccountingStrategy}
     */
    private final CreditFlowMode creditFlowMode;
    /**
     * The retry policy to use to establish a new receiver when the current receiver encounter terminal error.
     */
    private final AmqpRetryPolicy retryPolicy;
    /**
     * The function for updating disposition state of messages using the current receiver.
     */
    private volatile DispositionFunction updateDispositionFunc;

    /**
     * Create a message-flux to stream messages from a messaging entity to downstream subscriber.
     *
     * @param source the upstream source that, upon a request, provide a new receiver connected to the messaging entity.
     * @param prefetch the number of messages that the operator should prefetch from the messaging entity (for a
     *                 less chatty network and faster message processing on the client).
     * @param creditFlowMode the mode indicating how to compute the credit and when to send it to the broker.
     * @param retryPolicy the retry policy to use to obtain a new receiver upon current receiver termination.
     * @throws IllegalArgumentException if the {@code prefetch} is a negative value.
     * @throws NullPointerException if the {@code retryPolicy} is {@code null}.
     */
    public MessageFlux(Flux<? extends AmqpReceiveLink> source, int prefetch, CreditFlowMode creditFlowMode,
        AmqpRetryPolicy retryPolicy) {
        super(source);

        final Map<String, Object> loggingContext = new HashMap<>(1);
        loggingContext.put(MESSAGE_FLUX_KEY, StringUtil.getRandomString("mf"));
        this.logger = new ClientLogger(MessageFlux.class, loggingContext);

        if (prefetch < 0) {
            throw new IllegalArgumentException("prefetch >= 0 required but it was " + prefetch);
        }
        this.prefetch = prefetch;
        this.creditFlowMode = creditFlowMode;
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        this.updateDispositionFunc = DispositionFunction.NO_DISPOSITION;
    }

    /**
     * Register the downstream subscriber.
     *
     * @param actual the downstream subscriber interested in the published messages and termination.
     */
    @Override
    public void subscribe(CoreSubscriber<? super Message> actual) {
        source.subscribe(new RecoverableReactorReceiver(this, actual, prefetch, creditFlowMode, retryPolicy));
    }

    /**
     * Updates the disposition state of a message uniquely identified by the given delivery tag.
     *
     * @param deliveryTag delivery tag of message.
     * @param deliveryState Delivery state of message.
     *
     * @return A Mono that completes when the state is successfully updated and acknowledged by message broker.
     */
    public Mono<Void> updateDisposition(String deliveryTag, DeliveryState deliveryState) {
        final DispositionFunction function = this.updateDispositionFunc;
        return function.updateDisposition(deliveryTag, deliveryState);
    }

    /**
     * The callback invoked when next receiver is attached to the messaging entity from which this message-flux
     * instance stream messages. There will be only one receiver at a time, and this callback delivers the reference
     * to the function to disposition messages that arrives in the new receiver.
     *
     * @param updateDispositionFunc the function to disposition messages delivered by the current backing receiver.
     */
    void onNextUpdateDispositionFunction(DispositionFunction updateDispositionFunc) {
        this.updateDispositionFunc = updateDispositionFunc;
    }

    /**
     * Represents a function that accepts delivery tag and disposition state {@link DeliveryState} to set for the message
     * identified by that delivery tag. The function returns {@link Mono} representing the outcome of the disposition
     * operation attempted.
     */
    @FunctionalInterface
    private interface DispositionFunction {
        /**
         * Indicate that the disposition cannot be attempted as there is no backing receiver link to perform the operation.
         */
        DispositionFunction NO_DISPOSITION = (t, s) -> Mono
            .error(new IllegalStateException("Cannot update disposition as no receive-link is established."));

        /**
         * Updates the disposition state of a message uniquely identified by the given delivery tag.
         *
         * @param deliveryTag delivery tag of message.
         * @param deliveryState Delivery state of message.
         *
         * @return A Mono that completes when the state is successfully updated and acknowledged by message broker.
         */
        Mono<Void> updateDisposition(String deliveryTag, DeliveryState deliveryState);
    }

    /**
     * The underlying consumer and producer extension of the message-flux operator. The consuming side processes events
     * (about new receiver, terminal signals) from the upstream and events (messages, terminal signals) from
     * the current receiver. The producing side publishes the messages to message-flux's downstream. The type has
     * a recovery mechanism to obtain a new receiver from upstream upon the current receiver's termination.
     * Recoveries happen underneath while the messages flow transparently downstream. The type can terminate downstream
     * if the upstream terminates, the recovery path encounters a non-retriable error (i.e., the current receiver
     * terminated with a non-retriable error), or recovery retries exhaust.
     */
    private static final class RecoverableReactorReceiver implements CoreSubscriber<AmqpReceiveLink>, Subscription {
        // holds the current mediator that coordinates between a receiver and the recoverable-receiver.
        private final MediatorHolder mediatorHolder = new MediatorHolder();
        private final MessageFlux parent;
        private final int prefetch;
        private final CreditFlowMode creditFlowMode;
        private final AmqpRetryPolicy retryPolicy;
        private final ClientLogger logger;
        private final AtomicInteger retryAttempts = new AtomicInteger();
        private final CoreSubscriber<? super Message> messageSubscriber;
        private Subscription upstream;
        private volatile long requested;
        @SuppressWarnings("rawtypes")
        private static final AtomicLongFieldUpdater<RecoverableReactorReceiver> REQUESTED
            = AtomicLongFieldUpdater.newUpdater(RecoverableReactorReceiver.class, "requested");
        private volatile int wip;
        @SuppressWarnings("rawtypes")
        private static final AtomicIntegerFieldUpdater<RecoverableReactorReceiver> WIP
            = AtomicIntegerFieldUpdater.newUpdater(RecoverableReactorReceiver.class, "wip");
        private volatile boolean done;
        private volatile boolean cancelled;
        private volatile Throwable error;
        @SuppressWarnings("rawtypes")
        private static final AtomicReferenceFieldUpdater<RecoverableReactorReceiver, Throwable> ERROR
            = AtomicReferenceFieldUpdater.newUpdater(RecoverableReactorReceiver.class, Throwable.class, "error");

        /**
         * Create a recoverable-receiver that supports the message-flux to stream messages from the receiver attached
         * to a messaging entity to the message-flux's downstream subscriber and recover from receiver termination.
         *
         * @param parent the parent message-flux.
         * @param messageSubscriber the message-flux's downstream subscriber to notify the events.
         * @param prefetch the number of messages that the operator should prefetch from the messaging entity
         *                 (for a less chatty network and faster message processing on the client).
         * @param creditFlowMode the mode indicating how to compute the credit and when to send it to the broker.
         * @param retryPolicy the retry policy to use to recover from receiver termination.
         */
        RecoverableReactorReceiver(MessageFlux parent, CoreSubscriber<? super Message> messageSubscriber, int prefetch,
            CreditFlowMode creditFlowMode, AmqpRetryPolicy retryPolicy) {
            this.parent = parent;
            this.messageSubscriber = messageSubscriber;
            this.prefetch = prefetch;
            this.creditFlowMode = creditFlowMode;
            this.retryPolicy = retryPolicy;
            this.logger = parent.logger;
        }

        /**
         * Invoked by the upstream in response to message-flux subscribing to it.
         *
         * @param s the subscription handle for requesting receivers from the upstream or terminating upstream
         *         through cancellation when it is no longer needed.
         */
        @Override
        public void onSubscribe(Subscription s) {
            if (Operators.validate(upstream, s)) {
                upstream = s;
                // Invoke the downstream 'onSubscribe' with the subscription handle enabling downstream to
                // request messages or cancellation.
                messageSubscriber.onSubscribe(this);
                // Request the first receiver from the upstream.
                s.request(1);
            }
        }

        /**
         * Invoked by the upstream to deliver new receiver.
         *
         * @param receiver the new receiver.
         */
        @Override
        public void onNext(AmqpReceiveLink receiver) {
            if (done) {
                receiver.closeAsync().subscribe();
                Operators.onNextDropped(receiver, messageSubscriber.currentContext());
                return;
            }

            // Create a new mediator to channel communication between the new receiver and the recoverable-receiver.
            final ReactorReceiverMediator mediator
                = new ReactorReceiverMediator(this, receiver, prefetch, creditFlowMode, logger);

            // Request MediatorHolder to set the new mediator as the current (for the drain-loop to pick)
            if (mediatorHolder.trySet(mediator)) {
                // The MediatorHolder accepted the mediator. Notify that this recoverable-receiver i.e. the parent of
                // the mediator is ready to use the mediator by invoking 'mediator.onParentReady()'. In-response,
                // the mediator notifies its readiness by invoking 'onMediatorReady(DispositionFunction)' of this
                // recoverable-receiver.
                mediator.onParentReady();
            } else {
                // the MediatorHolder rejected the mediator as holder was frozen due to operator termination.
                logger.atWarning()
                    .addKeyValue("oldLinkName", mediatorHolder.getLinkName())
                    .addKeyValue(LINK_NAME_KEY, receiver.getLinkName())
                    .addKeyValue(ENTITY_PATH_KEY, receiver.getEntityPath())
                    .log("Got a AmqpReceiveLink when the MessageFlux is already terminated.");
                receiver.closeAsync().subscribe();
                Operators.onDiscard(receiver, messageSubscriber.currentContext());
            }
        }

        /**
         * Signals operator termination with error.
         * <ul>
         *     <li>Invoked by the upstream when it errors, or</li>
         *     <li>Invoked by the drain-loop when it detects a 'non-retriable or retry exhaust' error, or </li>
         *     <li>Invoked by the drain-loop when it detects the receiver signaled terminal completion  and retry
         *     is disabled (i.e., NULL_RETRY_POLICY is set).</li>
         * </ul>
         *
         * @param e the error signaled.
         */
        @Override
        public void onError(Throwable e) {
            if (done) {
                Operators.onErrorDropped(e, messageSubscriber.currentContext());
                return;
            }

            // It is possible that -
            // 1. the error from upstream and 'non-retriable or retry exhaust' error from RetryLoop
            // 2. Or, the error from upstream and error from receiver when NULL_RETRY_POLICY is set
            // signals concurrently, if so, the ERROR will be a CompositeException storing both errors.
            if (Exceptions.addThrowable(ERROR, this, e)) {
                done = true;
                final String logMessage;
                if (retryPolicy == NULL_RETRY_POLICY) {
                    logMessage = "Terminal error signal from Upstream|Receiver arrived at MessageFlux.";
                } else {
                    logMessage = "Terminal error signal from Upstream|RetryLoop arrived at MessageFlux.";
                }
                mediatorHolder.withReceiverInfo(logger.atWarning()).log(logMessage, e);
                drain(null);
            } else {
                // Once the drain-loop processed the last error, then further errors dropped through the standard
                // Reactor channel. E.g., retry exhaust error happened and, as part of its processing, upstream
                // gets canceled, but if upstream still signals an error, then that error gets dropped.
                Operators.onErrorDropped(e, messageSubscriber.currentContext());
            }
        }

        /**
         * Signals operator termination with completion.
         * <ul>
         *     <li>Invoked by the upstream when it completes, or</li>
         *     <li>Invoked by the the drain-loop when it detects first receiver signaled terminal completion and retry
         *     is disabled (i.e., NULL_RETRY_POLICY is set).</li>
         * </ul>
         */
        @Override
        public void onComplete() {
            if (done) {
                return;
            }

            done = true;
            final String logMessage;
            if (retryPolicy == NULL_RETRY_POLICY) {
                logMessage = "Terminal completion signal from Upstream|Receiver arrived at MessageFlux.";
            } else {
                logMessage = "Terminal completion signal from Upstream arrived at MessageFlux.";
            }
            mediatorHolder.withReceiverInfo(logger.atWarning()).log(logMessage);
            drain(null);
        }

        /**
         * Invoked by the downstream message subscriber to signal the demand for messages. Whatever has been
         * requested can be sent downstream, so only signal the demand for what can be safely handled.
         * No messages will be sent downstream until the demand is signaled.
         *
         * @param n the number of messages to send to downstream.
         */
        @Override
        public void request(long n) {
            if (Operators.validate(n)) {
                Operators.addCap(REQUESTED, this, n);
                drain(null);
            }
        }

        /**
         * Invoked by downstream to signal termination by cancellation.
         */
        @Override
        public void cancel() {
            if (cancelled) {
                return;
            }

            cancelled = true;
            mediatorHolder.withReceiverInfo(logger.atWarning())
                .log("Downstream cancellation signal arrived at MessageFlux.");
            // By incrementing wip, indicate the active drain-loop that there is cancel signal to handle,
            if (WIP.getAndIncrement(this) == 0) {
                // but it is identified that there is no active drain-loop; hence immediately react to cancel.
                upstream.cancel();
                mediatorHolder.freeze();
                // wip increment also placed a tombstone on the drain-loop, so further drain(..) calls are no-op.
            }
        }

        /**
         * Invoked by the new mediator when it is ready to be used. A mediator constructed in 'onNext' moves
         * to ready state when its backing receiver is active.
         *
         * @param updateDispositionFunc the function to disposition messages from mediator's backing receiver.
         */
        void onMediatorReady(DispositionFunction updateDispositionFunc) {
            retryAttempts.set(0);
            parent.onNextUpdateDispositionFunction(updateDispositionFunc);
            // After invoking 'messageSubscriber.onSubscribe(this)' and before the readiness of the mediator,
            // there may be request signals for messages from the downstream, invoke drain(...) to process
            // those signals.
            drain(null);
        }

        /**
         * The serialized entry point to drain-loop.
         *
         * @param dataSignal the message to drop if the operator is terminated by cancellation.
         */
        void drain(Message dataSignal) {
            // By incrementing wip, indicate the drain-loop that there is a signal to handle
            // (signals are - the readiness of a new mediator, request for messages from the downstream,
            // the arrival of a message, operator termination by the upstream, current link terminated),
            if (WIP.getAndIncrement(this) != 0) {
                if (dataSignal != null && cancelled) {
                    // but it is identified that the tombstone is placed on the drain-loop, so drop the data
                    // signal, if any (e.g., message), through the standard Reactor channel.
                    Operators.onDiscard(dataSignal, messageSubscriber.currentContext());
                }
                return;
            }
            drainLoop();
        }

        /**
         * The serialized drain-loop (implementation patterns inspired from RxJava, Reactor Operators).
         * Reference: 'Operator Concurrency Primitives' series https://akarnokd.blogspot.com/2015/05/
         */
        private void drainLoop() {
            int missed = 1;
            CoreSubscriber<? super Message> downstream = messageSubscriber;
            // Begin: serialized drain-loop.
            for (;;) {
                boolean d = done;
                // Obtain the current mediator (backed by a receiver)
                ReactorReceiverMediator mediator = mediatorHolder.mediator;
                // the 'mediator' can be null only in two cases -
                // [1]. In RecoverableReactorReceiver::onSubscribe, it passes the 'Subscription' to downstream via
                // messageSubscriber.onSubscribe(..). Once this method returns RecoverableReactorReceiver request
                // a receiver to back the very first Mediator. But from inside messageSubscriber.onSubscribe(),
                // Subscription.request(n) could be called resulting execution of drain-loop. In this case,
                // the Mediator will be null since the request for the first receiver is yet to be made.
                // [2]. If the upstream signals operator termination without emitting the very first receiver, hence
                // no associated Mediator.
                boolean hasMediator = mediator != null;

                if (terminateIfCancelled(downstream, null)) {
                    // the 'return' from the drain-loop in response to 'true' from 'terminateIf*' methods
                    // places a tombstone on the drain-loop (by not reducing the wip counter).
                    // Once tombstone placed on the drain-loop, further drain(..) calls are no-op.
                    return;
                }

                if (terminateIfErrorOrCompletionSignaled(d, downstream, null)) {
                    return;
                }

                long r = requested;
                long emitted = 0L;
                boolean mediatorTerminatedAndDrained = false;

                if (r != 0L && hasMediator) {
                    // there is demand ('r' != 0) from the downstream; see if it can be satisfied.
                    Queue<Message> q = mediator.queue;
                    // Begin: emitter-loop.
                    // Emits up to 'r' (requested) messages to the downstream if available in the mediator's queue,
                    // i.e., emitter-loop emits min(r, queue.size) messages.
                    while (emitted != r) {
                        Message message = q.poll();

                        if (terminateIfCancelled(downstream, message)) {
                            return;
                        }

                        if (terminateIfErrorOrCompletionSignaled(done, downstream, message)) {
                            return;
                        }

                        boolean empty = message == null;
                        // check if a new mediator may be needed.
                        if (empty && mediator.done) {
                            // Emitted all messages from the current mediator, and its backing receiver termination
                            // terminated the mediator; we may obtain a new mediator with a new backing receiver.
                            mediatorTerminatedAndDrained = true;
                            break;
                        }

                        if (empty) {
                            // There were more requested, but no messages left in the mediator's queue.
                            break;
                        }

                        messageSubscriber.onNext(message);
                        emitted++;
                    }
                    // End: emitter-loop.

                    if (emitted == r) {
                        // The emitter-loop checks the need for a new mediator until 'r-1' emissions; let's check
                        // after the 'r'-th emission.
                        if (mediator.queue.isEmpty() && mediator.done) {
                            mediatorTerminatedAndDrained = true;
                        }
                    }

                    if (emitted != 0 && r != Long.MAX_VALUE) {
                        r = REQUESTED.addAndGet(this, -emitted);
                    }
                    mediator.update(r, emitted);
                }

                if (r == 0L && hasMediator) {
                    // Even if there was no downstream demand (i.e., 'r' == 0),

                    // there could be pending cancellation signal from the downstream for the operator termination
                    if (terminateIfCancelled(downstream, null)) {
                        return;
                    }

                    // or pending signal from the upstream to terminate the operator with error or completion
                    // or last drain-loop iteration detected 'non-retriable or retry exhaust' error needing
                    // operator termination,
                    if (terminateIfErrorOrCompletionSignaled(done, downstream, null)) {
                        return;
                    }

                    // or pending signal indicating the mediator termination. E.g., receiver detached without
                    // receiving a message.
                    if (mediator.queue.isEmpty() && mediator.done) {
                        mediatorTerminatedAndDrained = true;
                    }
                }

                // No need to check 'hasMediator' before accessing 'mediator.isRetryInitiated' since
                // 'mediatorTerminatedAndDrained'
                // being 'true' means 'mediator' is set.
                if (mediatorTerminatedAndDrained && !mediator.isRetryInitiated) {
                    mediator.isRetryInitiated = true;
                    // The current mediator's queue is drained, and the mediator is terminated, let's close it,
                    mediator.closeAsync().subscribe();
                    // and proceed with requesting a new mediator if MessageFlux is in a state to do so.
                    setTerminationSignalOrScheduleNextMediatorRequest(mediator.error, downstream, mediatorHolder);
                }

                missed = WIP.addAndGet(this, -missed);
                if (missed == 0) {
                    break;
                }
                // The next serialized drain-loop iteration to process missed signals arrived during last iteration.
            }
            // End: serialized drain-loop.
        }

        /**
         * CONTRACT: Never invoke from the outside of serialized drain-loop.
         * <p>
         * See if downstream signaled cancellation to terminate the operator, if so, react to the cancellation.
         * </p>
         * @param downstream the downstream.
         * @param messageDropped the message that gets dropped if cancellation was signaled.
         * @return true if canceled, false otherwise.
         */
        private boolean terminateIfCancelled(CoreSubscriber<? super Message> downstream, Message messageDropped) {
            if (cancelled) {
                Operators.onDiscard(messageDropped, downstream.currentContext());
                upstream.cancel();
                mediatorHolder.freeze();
                return true;
            }
            return false;
        }

        /**
         * CONTRACT: Never invoke from the outside of serialized drain-loop.
         * <p>
         * See if there is a pending signal for the operator termination with error or completion, if so, react to it
         * by terminating downstream.
         * </p>
         *
         * @param d indicate if the operator termination was signaled.
         * @param downstream the downstream.
         * @param messageDropped the message that gets dropped if termination happened.
         * @return true if terminated, false otherwise.
         */
        private boolean terminateIfErrorOrCompletionSignaled(boolean d, CoreSubscriber<? super Message> downstream,
            Message messageDropped) {
            if (d) {
                // true for 'd' means the operator termination was signaled.
                final LoggingEventBuilder logBuilder = mediatorHolder.withReceiverInfo(logger.atWarning());
                Throwable e = error;
                if (e != null && e != Exceptions.TERMINATED) {
                    // A non-null 'e' indicates either
                    // 1. upstream signaled termination with an error
                    // 2. Or, there is 'non-retriable or retry exhausted' error
                    // 3. Or, retry is disabled (i.e., NULL_RETRY_POLICY is set) and the receiver signaled terminal
                    // error.
                    //
                    // let's terminate the local resources and propagate the error to terminate the downstream.

                    // Freezing 'this.e' (by marking it as TERMINATED) to drop further error signals to 'onError'.
                    e = Exceptions.terminate(ERROR, this);
                    Operators.onDiscard(messageDropped, downstream.currentContext());
                    upstream.cancel();
                    mediatorHolder.freeze();
                    logBuilder.log("MessageFlux reached a terminal error-state, signaling it downstream.", e);
                    downstream.onError(e);
                    return true;
                }

                // The absence of error (e) indicates either
                // 1. upstream signaled termination with completion,
                // 2. Or, retry is disabled (i.e., NULL_RETRY_POLICY is set) and the first receiver signaled terminal
                // completion.
                Operators.onDiscard(messageDropped, downstream.currentContext());
                upstream.cancel();
                mediatorHolder.freeze();
                logBuilder.log("MessageFlux reached a terminal completion-state, signaling it downstream.");
                downstream.onComplete();
                return true;
            }
            return false;
        }

        /**
         *
         * CONTRACT: Never invoke from the outside of serialized drain-loop.
         * <p></p>
         * 1. When retry is enabled (i.e., NULL_RETRY_POLICY is not set) then schedule request (retry) for the next mediator if
         * <ul>
         *     <li>the operator is not in a termination signaled state,</li>
         *     <li> and
         *         <ul>
         *             <li>there is no error Or</li>
         *             <li>error is retriable and the retry is not exhausted.</li>
         *         </ul>
         *     </li>
         * </ul>
         * 2. If retry is enabled (i.e., NULL_RETRY_POLICY is not set) and there is 'non-retriable or retry exhaust'
         * error, then set an error signal for the drain-loop to terminate the operator.
         * <br>
         * 3. If retry is disabled (i.e., NULL_RETRY_POLICY is set), then set an error signal (if first receiver error-ed)
         * or completion signal (if first receiver completed) for the drain-loop to terminate the operator.
         *
         * @param error the error that leads to error-ed termination of the last mediator or {@code null}
         *              if terminated with completion.
         * @param downstream the downstream.
         * @param mediatorHolder the mediator holder.
         */
        private void setTerminationSignalOrScheduleNextMediatorRequest(Throwable error,
            CoreSubscriber<? super Message> downstream, MediatorHolder mediatorHolder) {
            final LoggingEventBuilder logBuilder = mediatorHolder.withReceiverInfo(logger.atWarning());
            if (cancelled || done) {
                // To terminate the operator, the downstream signaled cancellation Or upstream signaled error
                // or completion. The next drain-loop iteration as a result of that signalling will terminate
                // the operator through one of the 'terminateIf*' methods followed by placing tombstone on
                // the drain-loop.
                logBuilder.log("MessageFlux reached terminal-state [done:{}, cancelled:{}].", done, cancelled);
                return;
            }

            if (retryPolicy == NULL_RETRY_POLICY) {
                if (error == null) {
                    onComplete();
                } else {
                    onError(error);
                }
                return;
            }

            final Duration delay;
            if (error == null) {
                // Even if the broker sets no link error condition, it's good to back off before creating a new link.
                delay = Duration.ofSeconds(1);
                logBuilder.addKeyValue("retryAfter", delay.toMillis())
                    .log("Current mediator reached terminal completion-state (retriable:true).");
            } else {
                final int attempt = retryAttempts.incrementAndGet();
                delay = retryPolicy.calculateRetryDelay(error, attempt);
                if (delay != null) {
                    logBuilder.addKeyValue("attempt", attempt)
                        .addKeyValue("retryAfter", delay.toMillis())
                        .log("Current mediator reached terminal error-state (retriable:true).", error);
                } else {
                    logBuilder.addKeyValue("attempt", attempt)
                        .log(
                            "Current mediator reached terminal error-state (retriable:false) Or MessageFlux retries exhausted.",
                            error);
                    // Note: this method is (will be by contract) called from the drain-loop's current iteration.
                    // Invoke 'onError' to set an error signal for the next drain-loop iteration to terminate the
                    // operator.
                    onError(error);
                    // Once the control from this method 'return' to the caller i.e. to drain-loop, the next immediate
                    // drain-loop iteration (iteration guaranteed by the above error signaling) picks the error signal,
                    // terminates the operator and downstream through 'terminateIfErrored*' method and places tombstone
                    // on the drain-loop.
                    return;
                }
            }

            try {
                scheduleNextMediatorRequest(delay, mediatorHolder);
            } catch (RejectedExecutionException ree) {
                final RuntimeException e = Operators.onRejectedExecution(ree, downstream.currentContext());
                mediatorHolder.withReceiverInfo(logger.atWarning())
                    .log("Unable to schedule a request for a new mediator (retriable:false).", e);
                onError(e);
                // See the above Note on 'onError' about why the downstream termination is guaranteed immediately
                // after control 'return' to drain-loop.
            }
        }

        /**
         * Schedule a task to request a new mediator.
         *
         * @param delay the backoff duration before requesting the next mediator.
         * @param mediatorHolder the mediator holder.
         * @throws RejectedExecutionException if the scheduler is unable to schedule the task.
         */
        private void scheduleNextMediatorRequest(Duration delay, MediatorHolder mediatorHolder) {
            final Runnable task = () -> {
                final LoggingEventBuilder logBuilder = mediatorHolder.withReceiverInfo(logger.atWarning());
                if (cancelled || done) {
                    logBuilder.log("During the backoff, MessageFlux reached terminal-state [done:{}, cancelled:{}].",
                        done, cancelled);
                    return;
                }
                logBuilder.log("Requesting a new mediator.");
                upstream.request(1);
            };

            mediatorHolder.nextMediatorRequestDisposable
                = Schedulers.parallel().schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * The mediator that coordinates between {@link RecoverableReactorReceiver} and a receiver {@link AmqpReceiveLink}.
     */
    private static final class ReactorReceiverMediator
        implements AsyncCloseable, CoreSubscriber<Message>, Subscription {
        private static final Subscription CANCELLED_SUBSCRIPTION = Operators.cancelledSubscription();
        private final RecoverableReactorReceiver parent;
        private final AmqpReceiveLink receiver;
        private final int prefetch;
        private final CreditFlowMode creditFlowMode;
        private final ClientLogger logger;
        private final Disposable.Composite endpointStateDisposable = Disposables.composite();
        private CreditAccountingStrategy creditAccounting;
        private volatile boolean ready;
        private volatile Subscription s;
        @SuppressWarnings("rawtypes")
        private static final AtomicReferenceFieldUpdater<ReactorReceiverMediator, Subscription> S
            = AtomicReferenceFieldUpdater.newUpdater(ReactorReceiverMediator.class, Subscription.class, "s");
        volatile Throwable error;
        @SuppressWarnings("rawtypes")
        static final AtomicReferenceFieldUpdater<ReactorReceiverMediator, Throwable> ERROR
            = AtomicReferenceFieldUpdater.newUpdater(ReactorReceiverMediator.class, Throwable.class, "error");
        /**
         * The flag indicating if the mediator is terminated by completion or error.
         */
        volatile boolean done;
        /**
         * The drain loop iteration that first identifies the mediator as terminated (done == true) and
         * and drained (queue.isEmpty() == true) will initiate a retry to obtain the next mediator. While that retry
         * completion is pending, any request for messages from downstream may lead to further drain loop iterations;
         * the 'isRetryInitiated' flag ensures those drain loop iterations (those also see the mediator as terminated
         * and drained) will not initiate duplicate retries.
         */
        volatile boolean isRetryInitiated;
        /**
         * The queue holding messages from the backing receiver's message publisher, waiting to be drained by
         * the drain-loop iterations.
         */
        final Queue<Message> queue;

        /**
         * Create a mediator to channel events (messages, termination) from a receiver to recoverable-receiver.
         *
         * @param parent the recoverable-receiver (a.k.a. parent).
         * @param receiver the receiver backing the mediator.
         * @param prefetch the number of messages to prefetch using the receiver (for a less chatty network
         *                 and faster message processing on the client).
         * @param creditFlowMode the mode indicating how to compute the credit and when to send it to the broker.
         */
        ReactorReceiverMediator(RecoverableReactorReceiver parent, AmqpReceiveLink receiver, int prefetch,
            CreditFlowMode creditFlowMode, ClientLogger logger) {
            this.parent = parent;
            this.receiver = receiver;
            this.prefetch = prefetch;
            this.creditFlowMode = creditFlowMode;
            this.logger = logger;
            // Obtain a resizable single-producer & single-consumer queue (SpscLinkedArrayQueue).
            this.queue = Queues.<Message>get(Integer.MAX_VALUE).get();
        }

        /**
         * Invoked by the parent {@link RecoverableReactorReceiver} when it is ready to use this new mediator
         * (The mediator facilitate communication between the parent and the new receiver ({@link AmqpReceiveLink}) that
         * mediator wraps). In response, this mediator notifies the parent about its readiness by invoking
         * {@link RecoverableReactorReceiver#onMediatorReady(DispositionFunction)}.
         */
        void onParentReady() {
            updateLogWithReceiverId(logger.atWarning()).log("Setting next mediator and waiting for activation.");
            // 1. Subscribe for the messages on the Receiver (AmqpReceiveLink).
            receiver.receive().subscribe(this);
            // 2. Subscribe for the readiness (active) and terminal event on the Receiver (AmqpReceiveLink) endpoint.
            final Disposable endpointDisposable = receiver.getEndpointStates()
                .filter(s -> s == AmqpEndpointState.ACTIVE)
                // ^ Pass down only readiness (active) event and terminal events.
                .publishOn(ReceiversPumpingScheduler.instance())
                // ^ Offload any initial drain (upon readiness) and final drain (upon termination) to
                // ReceiversPumpingScheduler.
                .subscribe(state -> {
                    assert state == AmqpEndpointState.ACTIVE;
                    if (!ready) {
                        updateLogWithReceiverId(logger.atWarning()).log("The mediator is active.");
                        // Set the 'ready' flag to indicate AmqpReceiveLink's successful transition to the active
                        // state.
                        // Once this flag is set, further drain-loop can use 'CreditAccountingStrategy' contract to
                        // place credits (i.e., the flag ensures credit is placed on the Link only after it is
                        // active).
                        ready = true;
                        // Notify readiness to trigger drain.
                        parent.onMediatorReady(this::updateDisposition);
                    }
                }, e -> {
                    updateLogWithReceiverId(logger.atWarning()).log("Receiver emitted terminal error.", e);
                    // Notify terminal error there by trigger final drain if needed.
                    onLinkError(e);
                }, () -> {
                    updateLogWithReceiverId(logger.atWarning()).log("Receiver emitted terminal completion.");
                    // Notify terminal completion there by trigger final drain if needed.
                    onLinkComplete();
                });
            endpointStateDisposable.add(endpointDisposable);
        }

        /**
         * Invoked in response to the subscription to the receiver's message publisher.
         *
         * @param s the subscription to request messages from the receiver's message publisher and terminate
         *         that publisher through cancellation when it is no longer needed.
         */
        @Override
        public void onSubscribe(Subscription s) {
            if (Operators.setOnce(S, this, s)) {
                switch (creditFlowMode) {
                    case RequestDriven:
                        creditAccounting = new RequestDrivenCreditAccountingStrategy(receiver, s, prefetch, logger);
                        break;

                    case EmissionDriven:
                        creditAccounting = new EmissionDrivenCreditAccountingStrategy(receiver, s, prefetch, logger);
                        break;

                    default:
                        throw new IllegalArgumentException("Unknown CreditFlowMode " + creditFlowMode);
                }
            }
        }

        /**
         * CONTRACT: Never invoke from the outside of serialized drain-loop.
         * <p>
         * Notify the latest view of the downstream request and messages emitted by the emitter-loop during
         * the last drain-loop iteration.
         * </p>
         * @param request the latest view of the downstream request.
         * @param emitted the number of messages emitted by the latest emitter-loop run.
         */
        void update(long request, long emitted) {
            if (ready && !done) {
                creditAccounting.update(request, emitted);
            }
        }

        /**
         * Invoked by the receiver's message publisher to deliver a message.
         *
         * @param message the message.
         */
        @Override
        public void onNext(Message message) {
            if (done) {
                Operators.onNextDropped(message, parent.currentContext());
                return;
            }

            if (s == Operators.cancelledSubscription()) {
                Operators.onDiscard(message, parent.currentContext());
                return;
            }

            if (queue.offer(message)) {
                parent.drain(message);
            } else {
                Operators.onOperatorError(this, Exceptions.failWithOverflow(Exceptions.BACKPRESSURE_ERROR_QUEUE_FULL),
                    parent.messageSubscriber.currentContext());
                Operators.onDiscard(message, parent.messageSubscriber.currentContext());
                done = true;
                // At this point "this.s == canceled, isEmpty(this.queue) == true, this.done == true".
                // this.s == cancelled means upstream 'receiver' subscription is canceled. Even if the 'receiver'
                // invokes this.onNext(m) a few times, they are all discarded keeping queue empty. When drain-loop
                // iteration sees this.done && isEmpty(this.queue) == true, the mediator is closed and proceeds to
                // request the next mediator.
                parent.drain(message);
            }
        }

        @Override
        public void onError(Throwable e) {
            // NO-OP: The error signal to terminate the mediator arrives through onLinkError.
        }

        /**
         * Invoked by the receiver's endpoint publisher to signal mediator termination with an error.
         *
         * @param e the error signaled.
         */
        private void onLinkError(Throwable e) {
            if (done) {
                Operators.onErrorDropped(e, parent.messageSubscriber.currentContext());
                return;
            }

            if (ERROR.compareAndSet(this, null, e)) {
                done = true;
                parent.drain(null);
            } else {
                done = true;
                Operators.onErrorDropped(e, parent.messageSubscriber.currentContext());
            }
        }

        @Override
        public void onComplete() {
            // NO-OP: The completion signal to terminate the mediator arrives through onLinkComplete.
        }

        /**
         * Invoked by the receiver's endpoint publisher to signal mediator termination with completion.
         */
        private void onLinkComplete() {
            if (done) {
                return;
            }

            done = true;
            parent.drain(null);
        }

        @Override
        public void request(long n) {
            throw new IllegalStateException("The request accounting must be through update(,).");
        }

        @Override
        public void cancel() {
            // Remove (cancel) the Subscription to message publisher i.e. receiver.receive(),
            if (Operators.terminate(S, this)) {
                // discard the queue
                Operators.onDiscardQueueWithClear(queue, parent.currentContext(), null);
            }
            // and remove (dispose) the Subscriptions to endpointStates publisher i.e. receiver.getEndpointStates().
            endpointStateDisposable.dispose();
        }

        /**
         * Close the mediator. Closing is triggered in the following cases -
         * <ul>
         * <li>When {@link RecoverableReactorReceiver} switches to a new (i.e., next) mediator, it closes the current mediator.</li>
         * <li>When {@link RecoverableReactorReceiver} terminates (hence {@link MessageFlux}) due to
         *     <ul>
         *         <li>downstream cancellation or</li>
         *         <li>upstream termination with error or completion or</li>
         *         <li>retry-exhaust-error or non-retriable-error or</li>
         *         <li>termination of receiver with error or completion when NULL_RETRY_POLICY is set,</li>
         *     </ul>
         * it closes the current (i.e., last) mediator. </li>
         * </ul>
         */
        @Override
        public Mono<Void> closeAsync() {
            cancel();
            return receiver.closeAsync();
        }

        /**
         * Updates the disposition state of a message uniquely identified by the given delivery tag.
         *
         * @param deliveryTag delivery tag of message.
         * @param deliveryState Delivery state of message.
         *
         * @return A Mono that completes when the state is successfully updated and acknowledged by message broker.
         */
        private Mono<Void> updateDisposition(String deliveryTag, DeliveryState deliveryState) {
            if (done || s == CANCELLED_SUBSCRIPTION) {
                // [1]. 'done' is set to 'true' when the backing receiver signals completion or error.
                // [2]. 's' is set to 'canceled' when the upstream signals completion or error to the parent
                // RecoverableReactorReceiver, Or downstream cancels parent RecoverableReactorReceiver.
                // 1 & 2 means we don't have to read the 'volatile' variables 'parent.done', 'parent.cancelled'
                //
                final String state
                    = String.format("[link.done:%b link.cancelled:%b parent.done:%b parent.cancelled:%b]", done,
                        s == CANCELLED_SUBSCRIPTION, parent.done, parent.cancelled);

                final DeliveryNotOnLinkException dispositionError
                    = DeliveryNotOnLinkException.linkClosed(deliveryTag, deliveryState);
                final Throwable receiverError = error;
                if (receiverError != null) {
                    dispositionError.addSuppressed(receiverError);
                }
                final Throwable upstreamError = parent.error;
                if (upstreamError != null) {
                    dispositionError.addSuppressed(upstreamError);
                }
                return monoError(logger.atError()
                    .addKeyValue(DELIVERY_TAG_KEY, deliveryTag)
                    .addKeyValue(DELIVERY_STATE_KEY, deliveryState)
                    .addKeyValue("messageFluxState", state), dispositionError);
            }
            return receiver.updateDisposition(deliveryTag, deliveryState);
        }

        private LoggingEventBuilder updateLogWithReceiverId(LoggingEventBuilder builder) {
            return builder.addKeyValue(CONNECTION_ID_KEY, receiver.getConnectionId())
                .addKeyValue(LINK_NAME_KEY, receiver.getLinkName())
                .addKeyValue(ENTITY_PATH_KEY, receiver.getEntityPath());
        }
    }

    /**
     * A type that supports atomically setting a mediator and disposing of the last set mediator upon freezing.
     * Once frozen, further attempt to set the mediator will be rejected. The object of this type holds
     * the current mediator that the drain-loop access to read events from the receiver (backing the mediator).
     */
    private static final class MediatorHolder {
        private boolean isFrozen;
        volatile ReactorReceiverMediator mediator;
        // Holds the subscription to the task that, when executed, request the next mediator.
        volatile Disposable nextMediatorRequestDisposable;

        /**
         * Try to set the current mediator for the drain-loop.
         *
         * @param mediator the mediator.
         * @return true if the mediator is set successfully, false if the attempt to set is rejected due
         * to the holder in the frozen state.
         */
        boolean trySet(ReactorReceiverMediator mediator) {
            synchronized (this) {
                if (isFrozen) {
                    return false;
                }
                this.mediator = mediator;
                return true;
            }
        }

        /**
         * Freeze the holder to dispose of the current mediator and any resources it tracks; no further
         * mediator can be set once frozen. Freezing happens when the message-flux operator is terminated.
         */
        void freeze() {
            final Disposable d;
            final ReactorReceiverMediator m;
            synchronized (this) {
                if (isFrozen) {
                    return;
                }
                d = nextMediatorRequestDisposable;
                m = this.mediator;
                isFrozen = true;
            }

            if (d != null) {
                d.dispose();
            }
            if (m != null) {
                m.closeAsync().subscribe();
            }
        }

        String getLinkName() {
            final ReactorReceiverMediator m = mediator;
            return m != null ? m.receiver.getLinkName() : null;
        }

        /**
         * annotate the log builder with the receiver info (connectionId:linkName:entityPath) if the mediator has
         * receiver set, else no-op.
         *
         * @param builder the log builder to annotate.
         * @return the log builder annotated with receiver info.
         */
        LoggingEventBuilder withReceiverInfo(LoggingEventBuilder builder) {
            final ReactorReceiverMediator m = mediator;
            if (m != null) {
                return builder.addKeyValue(CONNECTION_ID_KEY, m.receiver.getConnectionId())
                    .addKeyValue(LINK_NAME_KEY, m.receiver.getLinkName())
                    .addKeyValue(ENTITY_PATH_KEY, m.receiver.getEntityPath());
            }
            return builder;
        }
    }
}
