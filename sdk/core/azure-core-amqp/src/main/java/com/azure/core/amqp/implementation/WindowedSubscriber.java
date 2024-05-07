// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A subscriber that split its upstream sequence into a series of windows.
 * <p>
 * A window can be requested and an {@link IterableStream} instance streaming sub-sequence belongs to that window
 * can be obtained using the API {@link #enqueueRequest(int, Duration)}.
 * </p>
 *
 * @param <T> the type of items in the window.
 */
public final class WindowedSubscriber<T> extends BaseSubscriber<T> {
    private static final String WORK_ID_KEY = "workId";
    private static final String UPSTREAM_REQUESTED_KEY = "requested";
    private static final String DIFFERENCE_KEY = "difference";

    private final Map<String, Object> loggingContext;
    private final String terminatedMessage;
    private final Duration nextItemTimout;
    private final Consumer<T> releaser;
    private final Function<Flux<T>, Flux<T>> windowDecorator;
    private final boolean cleanCloseStreamingWindowOnTerminate;
    private final ClientLogger logger;
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final ConcurrentLinkedDeque<T> queue = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedQueue<WindowWork<T>> workQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedDeque<WindowWork<T>> timedOutOrCanceledWorkQueue = new ConcurrentLinkedDeque<>();

    private volatile Subscription s;
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<WindowedSubscriber, Subscription> S
        = AtomicReferenceFieldUpdater.newUpdater(WindowedSubscriber.class, Subscription.class, "s");
    private volatile long requested;
    @SuppressWarnings("rawtypes")
    private static final AtomicLongFieldUpdater<WindowedSubscriber> REQUESTED
        = AtomicLongFieldUpdater.newUpdater(WindowedSubscriber.class, "requested");
    private volatile int wip;
    @SuppressWarnings("rawtypes")
    private static final AtomicIntegerFieldUpdater<WindowedSubscriber> WIP
        = AtomicIntegerFieldUpdater.newUpdater(WindowedSubscriber.class, "wip");
    private volatile boolean done;
    private volatile Throwable error;
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<WindowedSubscriber, Throwable> ERROR
        = AtomicReferenceFieldUpdater.newUpdater(WindowedSubscriber.class, Throwable.class, "error");

    /**
     * Creates the subscriber to split upstream sequence into a series of windows.
     *
     * @param loggingContext the context for logging.
     * @param terminatedMessage the message to include in the error when closing a window after the subscriber termination.
     * @param options the optional configurations for the subscriber.
     */
    public WindowedSubscriber(Map<String, Object> loggingContext, String terminatedMessage,
        WindowedSubscriberOptions<T> options) {
        this.loggingContext = Objects.requireNonNull(loggingContext, "'loggingContext' cannot be null.");
        Objects.requireNonNull(terminatedMessage, "'terminatedMessage' cannot be null.");
        this.terminatedMessage = terminatedMessage + " (Reason: %s)";
        Objects.requireNonNull(options, "'options' cannot be null.");
        this.nextItemTimout = options.getNextItemTimout();   // can be null.
        this.releaser = options.getReleaser();               // can be null.
        this.windowDecorator = options.getWindowDecorator(); // can be null.
        this.cleanCloseStreamingWindowOnTerminate = options.shouldCleanCloseStreamingWindowOnTerminate(); // default: false.
        this.logger = new ClientLogger(WindowedSubscriber.class, loggingContext);
    }

    /**
     * Enqueue a request for a window with up to {@code windowSize} number of items.
     *
     * <p>
     *  TODO (anu) – move to azure-core-amqp.
     *  The WindowedSubscriber can be later moved to azure-core-amqp for both Event Hubs and Service Bus use.
     *  For now keeping it in Service Bus to unblock immediate needs.
     * </p>
     *
     * @param windowSize the upper bound for the number of items to include in the window before closing it.
     * @param windowTimeout the maximum {@link Duration} since the window was opened before closing it. The window is
     *     opened when the request gets dequeued to process.
     *
     * @return the {@link IterableStream} that streams window events (items and termination signal).
     */
    public IterableStream<T> enqueueRequest(int windowSize, Duration windowTimeout) {
        final EnqueueResult<T> r = enqueueRequestImpl(windowSize, windowTimeout);
        return r.getWindowIterable();
    }

    // Opened in Package-private scope for testing purposes, any library need will use 'enqueueRequest' API.
    EnqueueResult<T> enqueueRequestImpl(int windowSize, Duration windowTimeout) {
        if (windowSize < 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'windowSize' must be strictly positive."));
        }
        if (Objects.isNull(windowTimeout)) {
            throw logger.logExceptionAsError(new NullPointerException("'windowTimeout' cannot be null."));
        }
        if (windowTimeout.isNegative() || windowTimeout.isZero()) {
            throw logger
                .logExceptionAsError(new IllegalArgumentException("'windowTimeout' period must be strictly positive."));
        }

        final long workId = idGenerator.getAndIncrement();
        final WindowWork<T> w = new WindowWork<T>(this, workId, windowSize, windowTimeout);
        if (isDoneOrCanceled()) {
            w.terminate(WorkTerminalState.PARENT_TERMINAL);
            return new EnqueueResult<T>(w, w.windowFlux(false));
        }

        workQueue.add(w);
        drain();
        return new EnqueueResult<T>(w, w.windowFlux(true));
    }

    @Override
    protected void hookOnSubscribe(Subscription s) {
        if (Operators.setOnce(S, this, s)) {
            drain();
        }
    }

    @Override
    protected void hookOnNext(T item) {
        if (done) {
            Operators.onNextDropped(item, super.currentContext());
            return;
        }

        if (s == Operators.cancelledSubscription()) {
            Operators.onDiscard(item, super.currentContext());
            return;
        }

        if (queue.offer(item)) {
            drain();
        } else {
            // The item 'queue' is 'unbounded' so this code block won't be hit, still follow guideline.
            final RuntimeException e = Exceptions.failWithOverflow(Exceptions.BACKPRESSURE_ERROR_QUEUE_FULL);
            Operators.onOperatorError(this, e, super.currentContext());
            Operators.onDiscard(item, super.currentContext());
            if (ERROR.compareAndSet(this, null, e)) {
                done = true;
                drain();
            } else {
                done = true;
                Operators.onErrorDropped(e, super.currentContext());
            }
        }
    }

    @Override
    protected void hookOnError(Throwable t) {
        if (done) {
            Operators.onErrorDropped(t, super.currentContext());
            return;
        }

        final RuntimeException e = new RuntimeException(String.format(terminatedMessage, "upstream-error"), t);
        if (ERROR.compareAndSet(this, null, e)) {
            done = true;
            drain();
        } else {
            done = true;
            Operators.onErrorDropped(t, super.currentContext());
        }
    }

    @Override
    protected void hookOnComplete() {
        if (done) {
            return;
        }

        done = true;
        drain();
    }

    @Override
    protected void hookOnCancel() {
        if (Operators.terminate(S, this)) {
            Operators.onDiscardQueueWithClear(queue, this.currentContext(), null);
            drain();
        }
    }

    private void drain() {
        if (WIP.getAndIncrement(this) != 0) {
            return;
        }
        drainLoop();
    }

    private void drainLoop() {
        int missed = 1;
        // Begin: drain-loop
        for (;;) {
            if (isDoneOrCanceled()) {
                if (cleanCloseStreamingWindowOnTerminate) {
                    WindowWork<T> w0 = workQueue.peek();
                    if (w0 != null && w0.isStreaming()) {
                        workQueue.poll();
                        w0.terminate(WorkTerminalState.PARENT_TERMINAL_CLEAN_CLOSE);
                    }
                }
                WindowWork<T> w1;
                while ((w1 = workQueue.poll()) != null) {
                    w1.terminate(WorkTerminalState.PARENT_TERMINAL);
                }
            } else {
                // Stage_1: Process work termination needs (timeout, cancel) detected and posted from outside the drain-loop.
                WindowWork<T> w0;
                while ((w0 = timedOutOrCanceledWorkQueue.poll()) != null) {
                    if (workQueue.remove(w0)) {
                        if (w0.hasTimedOut()) {
                            w0.terminate(WorkTerminalState.TIMED_OUT);
                        } else if (w0.isCanceled()) {
                            w0.terminate(WorkTerminalState.CANCELED);
                        } else {
                            throw w0.getLogger()
                                .log(new IllegalStateException("work with unexpected state in timeout-cancel queue."));
                        }
                    }
                }

                // Stage_2:
                //
                WindowWork<T> w = workQueue.peek();
                final boolean hasWork = w != null;
                if (hasWork && s != null) {
                    // Stage_2.1.A: Process the current work 'w' (initialize once and emit items).
                    //
                    initWorkOnce(w);
                    long r = requested;
                    if (r != 0L) {
                        long emitted = 0L;
                        EmitNextResult emitterLoopResult = EmitNextResult.OK;
                        // Begin: emitter-loop
                        while (emitted != r) {
                            final T item = queue.poll();
                            if (item == null || isDoneOrCanceled()) {
                                // If isDoneOrCanceled is true, a guaranteed drain-loop will terminate 'w' and works in work-queue.
                                break;
                            }
                            emitterLoopResult = w.tryEmitNext(item);
                            if (emitterLoopResult != EmitNextResult.OK) {
                                queue.addFirst(item);
                                break;
                            }
                            emitted++;
                        }
                        // End: emitter-loop

                        if (emitted != 0 && r != Long.MAX_VALUE) {
                            REQUESTED.addAndGet(this, -emitted);
                        }

                        // Stage_2.1.B: Process current work ('w') termination needs detected from inside the drain-loop.
                        // (If 'w' is eligible to terminate then remove it from the work-queue and terminate it).
                        //
                        if (w.hasReceivedDemanded()) {
                            workQueue.poll();
                            w.terminate(WorkTerminalState.RECEIVED_DEMANDED);
                            continue;
                        }
                        if (emitterLoopResult == EmitNextResult.CONSUMER_ERROR) {
                            workQueue.poll();
                            w.terminate(WorkTerminalState.CONSUMER_ERROR);
                            continue;
                        }
                        if (emitterLoopResult == EmitNextResult.SINK_ERROR) {
                            workQueue.poll();
                            w.terminate(WorkTerminalState.SINK_ERROR);
                            continue;
                        }
                        //
                        // Above, 'continue' after the terminate/removal of 'w' enforces one more drain-loop iteration to
                        // pick next work whose drain signal would have already consumed. A flow leads to such consumption
                        // needing 'continue' is -
                        //
                        // 1. The subscriber is waiting for upstream subscription, the application calls enqueueRequest()
                        //    few times to queue w1, w2, w3. Each enqueueRequest() calls drain() [WIP==3].
                        // 2. Since there is no upstream subscription, drain-loop exits after 2 iterations [WIP=0].
                        // 3. The upstream subscription arrives and calls drain() [WIP==1]. The drain-loop
                        //      3.1. init the work w1 requesting its demand (e.g, 1 item) to upstream, then,
                        //      3.2. runs the emitter-loop, which exit seeing item 'queue' as empty, [WIP==0] and drain-loop exits.
                        // 4. Later, one item w1 requested arrived. It call drain() [WIP==1], drain-loop deliver it to w1
                        //    and terminate w1 (since w1.hasReceivedDemanded == true).
                        // 5. Without 'continue' - [WIP=0] and drain-loop exit. The work w2 (and w3) will never be picked.
                        // 6. With 'continue'    - drain-loop repeats, starting w2 and sending its demand to the upstream,
                        //                         [WIP=0] and drain-loop exit.
                        //      6.1. When w2's item arrives, it trigger drain().
                        //      6.2. If no item arrives, the timeout of w2 will trigger drain() picking w3.
                    }
                }

                if (!hasWork && releaser != null) {
                    // Stage_2.2: drop the items when there is no active work.
                    long r = requested;
                    if (r != 0L) {
                        long released = 0L;
                        // Begin: releaser-loop
                        while (released != r) {
                            final boolean workArrived = !workQueue.isEmpty();
                            if (workArrived) {
                                break;
                            }
                            final T item = queue.poll();
                            if (item == null || isDoneOrCanceled()) {
                                break;
                            }
                            try {
                                releaser.accept(item);
                            } catch (Throwable e) {
                                this.logger.atError().log("Unexpected: 'releaser' thrown error.", e);
                            }
                            released++;
                        }
                        // End: releaser-loop

                        if (released != 0 && r != Long.MAX_VALUE) {
                            REQUESTED.addAndGet(this, -released);
                        }
                    }
                }
            }

            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
                break;
            }
            // The next serialized drain-loop iteration to process missed signals arrived during last iteration.
        }
        // End: drain-loop
    }

    /**
     * Check if upstream terminated (with error or completion) or downstream canceled the subscription.
     *
     * @return true if this subscriber is terminated.
     */
    private boolean isDoneOrCanceled() {
        return done || s == Operators.cancelledSubscription();
    }

    /**
     * CONTRACT: Never invoke from the outside of serialized drain-loop.
     * <p>
     * initialize the work and request the items needed to meet its window demand.
     * </p>
     *
     * @param w the work.
     */
    private void initWorkOnce(WindowWork<T> w) {
        if (!w.init()) {
            return;
        }

        // 'requested' is the overall outstanding demand yet to be satisfied by the upstream.
        final long requested = REQUESTED.get(this);
        final long workDemand = w.getDemand();
        final long difference = workDemand - requested;

        final LoggingEventBuilder logger
            = w.getLogger().addKeyValue(UPSTREAM_REQUESTED_KEY, requested).addKeyValue(DIFFERENCE_KEY, difference);

        if (difference > 0) {
            Operators.addCap(REQUESTED, this, difference);
            logger.log("Initialized: request-upstream:true.");
            s.request(difference);
        } else {
            logger.log("Initialized: request-upstream:false.");
        }
    }

    /**
     * Post a timed-out or canceled work to the drain-loop to process.
     *
     * @param w canceled or timed-out work.
     */
    private void postTimedOutOrCanceledWork(WindowWork<T> w) {
        timedOutOrCanceledWorkQueue.add(w);
        drain();
    }

    /**
     * Gets the error describing the reason for subscriber (parent) termination.
     *
     * @return the subscriber (parent) termination reason.
     */
    private Throwable getTerminalError() {
        assert isDoneOrCanceled();
        if (done) {
            final Throwable e = this.error;
            return e != null ? e : new RuntimeException(String.format(terminatedMessage, "upstream-completion"));
        }
        return new RuntimeException(String.format(terminatedMessage, "downstream-cancel"));
    }

    /**
     * Optional configurations for {@link WindowedSubscriber}.
     *
     * @param <T> the type of items in the windows that {@link WindowedSubscriber} produces.
     */
    public static final class WindowedSubscriberOptions<T> {
        private Consumer<T> releaser;
        private Duration nextItemTimout;
        private Function<Flux<T>, Flux<T>> windowDecorator;
        private boolean cleanCloseStreamingWindowOnTerminate;

        /**
         * Creates the optional configurations for {@link WindowedSubscriber}.
         */
        public WindowedSubscriberOptions() {
            this.releaser = null;
            this.nextItemTimout = null;
            this.windowDecorator = null;
            this.cleanCloseStreamingWindowOnTerminate = false;
        }

        /**
         * Gets the callback to drop the upstream items when there is no active or pending window to deliver those.
         *
         * @return the callback to drop the items in the absence of a window.
         */
        private Consumer<T> getReleaser() {
            return releaser;
        }

        /**
         * Determines how long to wait for the next item in the window after the previous item. If the next item
         * does not arrive within the wait time, then the window will be closed.
         *
         * @return the next window item timeout.
         */
        private Duration getNextItemTimout() {
            return nextItemTimout;
        }

        /**
         * Gets the decorator that add hooks to inspect the window events (items and termination).
         *
         * @return the window decorator.
         */
        private Function<Flux<T>, Flux<T>> getWindowDecorator() {
            return windowDecorator;
        }

        /**
         * Indicate that if the window in 'streaming state' (aka 'streaming window') should be closed without error when
         * the {@link WindowedSubscriber} terminates.
         *
         * @see WindowedSubscriberOptions#cleanCloseStreamingWindowOnTerminate() documentation for more details.
         *
         * @return true to close 'streaming window' normally when the subscriber terminates, false to close with error.
         */
        private boolean shouldCleanCloseStreamingWindowOnTerminate() {
            return cleanCloseStreamingWindowOnTerminate;
        }

        /**
         * Sets the callback to drop the upstream items when there is no active or pending window to deliver those.
         * <p>
         * By default, without a releaser, the items will be buffered for future windows.
         * </p>
         *
         * @param releaser the callback to drop the items in the absence of a window.
         * @return the updated {@link WindowedSubscriberOptions} object.
         */
        public WindowedSubscriberOptions<T> setReleaser(Consumer<T> releaser) {
            this.releaser = Objects.requireNonNull(releaser, "'releaser' cannot be null.");
            return this;
        }

        /**
         * Sets the duration to wait for the next item in the window after the previous item. If the next item does not
         * arrive within the wait time, then the window will be closed.
         * <p>
         * By default, the next item timeout is disabled.
         * </p>
         *
         *  @param nextItemTimout the next window item timeout.
         * @return the updated {@link WindowedSubscriberOptions} object.
         */
        public WindowedSubscriberOptions<T> setNextItemTimeout(Duration nextItemTimout) {
            this.nextItemTimout = Objects.requireNonNull(nextItemTimout, "'nextItemTimout' cannot be null.");
            return this;
        }

        /**
         * Sets the decorator to register hooks to inspect the window events (items and termination).
         *
         * @param windowDecorator the window decorator.
         * @return the updated {@link WindowedSubscriberOptions} object.
         */
        public WindowedSubscriberOptions<T> setWindowDecorator(Function<Flux<T>, Flux<T>> windowDecorator) {
            this.windowDecorator = Objects.requireNonNull(windowDecorator, "'windowDecorator' cannot be null.");
            return this;
        }

        /**
         * Indicate that the window in 'streaming state' should be closed without error when the {@link WindowedSubscriber}
         * terminates.
         * <p>
         * The window is called 'streaming window' or in 'streaming state', if it has sent at least one item but still
         * needs more upstream items to meet its demand.
         * </p>
         * <p>
         * By default, on subscriber termination, the 'streaming window' will be closed with subscriber's terminal error.
         * </p>
         * <p>
         * Once the subscriber is terminated, any request for windows after the 'streaming window' will be always closed
         * with subscriber's terminal error. I.e., this flag impacts only ONE window, the current one that is happens
         * to be in 'streaming state' when the subscriber terminal signal arrived.
         * </p>
         * <p>
         * Enable this flag if subscriber termination does not impact user's ability to process the items. E.g.,
         * <ul>
         *     <li>
         *         In the case of Event Hubs, subscriber termination means backing client terminated, but checkpointing
         *         does not require backing client. Service Bus RECEIVE_AND_DELETE is similar. In such cases, useful to
         *         close 'streaming window' without error (clean close).
         *     </li>
         *     <li>
         *         In Service Bus PEEK_MODE, healthy subscriber (i.e., healthy backing client) is crucial for user to
         *         process (disposition) the window items, so, useful to close 'streaming window' with error.
         *     </li>
         * </ul>
         * </p>
         *
         * @return the updated {@link WindowedSubscriberOptions} object.
         */
        public WindowedSubscriberOptions<T> cleanCloseStreamingWindowOnTerminate() {
            this.cleanCloseStreamingWindowOnTerminate = true;
            return this;
        }
    }

    /**
     * Type representing the work to produce a window of items.
     *
     * @param <T> the type of items in the window.
     */
    static final class WindowWork<T> {
        private static final String DEMAND_KEY = "demand";
        private static final String PENDING_KEY = "pending";
        public static final String SIGNAL_TYPE_KEY = "signalType";
        public static final String EMIT_RESULT_KEY = "emitResult";
        private static final String TERMINATING_WORK = "Terminating the work.";

        private final AtomicBoolean isInitialized = new AtomicBoolean(false);
        private final AtomicBoolean isCanceled = new AtomicBoolean(false);
        private final AtomicBoolean isTerminated = new AtomicBoolean(false);
        private final AtomicReference<TimeoutReason> timeoutReason = new AtomicReference<>(null);
        private final AtomicReference<Throwable> consumerError = new AtomicReference<>(null);
        private final ClientLogger logger;
        private final WindowedSubscriber<T> parent;
        private final int demand;
        private final Duration timeout;
        private final Sinks.Many<T> sink;
        private final AtomicInteger pending;
        private final Disposable.Composite timers;

        /**
         * Create a work to produce a window of items.
         *
         * @param parent the parent subscriber that deliveries items for the window.
         * @param id an identifier for the work.
         * @param demand the upper bound for the number of items to include in the window.
         * @param timeout the maximum {@link Duration} since the window was opened before closing it.
         */
        private WindowWork(WindowedSubscriber<T> parent, long id, int demand, Duration timeout) {
            this.logger = createLogger(parent.loggingContext, id, demand);
            this.parent = parent;
            this.demand = demand;
            this.pending = new AtomicInteger(demand);
            this.timeout = timeout;
            this.sink = createSink();
            this.timers = Disposables.composite();
        }

        /**
         * Check if the window was canceled from "outside WindowedSubscriber".
         *
         * @see WindowWork#windowFlux(boolean)
         *
         * @return true if the window was canceled externally.
         */
        boolean isCanceled() {
            return isCanceled.get();
        }

        /**
         * Check if the window has received the number of items it demanded.
         *
         * @return true if demanded number of items are received.
         */
        boolean hasReceivedDemanded() {
            return pending.get() <= 0;
        }

        /**
         * Check if the window has timeout or there was a failure while scheduling or waiting for timeout.
         *
         * @see TimeoutReason
         *
         * @return true if the window has timed out or there was a failure while scheduling or waiting for timeout.
         */
        boolean hasTimedOut() {
            return timeoutReason.get() != null;
        }

        /**
         * The number of items so far received by the window.
         *
         * @return the number of items received by the window.
         */
        int getPending() {
            return pending.get();
        }

        /**
         * The desired number of items to include in the window.
         *
         * @return the demanded window size.
         */
        private long getDemand() {
            return demand;
        }

        /**
         * Check if consumer unexpectedly thrown an error while handling an item in the window.
         *
         * @return true if consumer thrown an error while handling an item.
         */
        private boolean hasConsumerError() {
            return consumerError.get() != null;
        }

        /**
         * Check if the window is in 'streaming state'.
         * <p>
         * The window is called 'streaming window' or in 'streaming state', if it has sent at least one item but still
         * needs more upstream items to meet its demand.
         * </p>
         *
         * @see WindowedSubscriberOptions#cleanCloseStreamingWindowOnTerminate()
         *
         * @return true if the window is in 'streaming state', false otherwise.
         */
        private boolean isStreaming() {
            final int pending = getPending();
            return pending > 0 && pending < demand;
        }

        /**
         * CONTRACT: Never invoke from the outside of serialized drain-loop.
         * <p>
         * Perform one time initialization of the work to open its window.
         * </p>
         * @return true if the work is initialized for the first time; false, if it is already initialized.
         */
        private boolean init() {
            if (isInitialized.getAndSet(true)) {
                return false;
            }
            this.timers.add(beginTimeoutTimer());
            this.timers.add(beginNextItemTimeoutTimer());
            return true;
        }

        /**
         * Get the flux that streams the window events (items and termination) to it's downstream.
         * <p>
         * The downstream is the {@link IterableStream} that users uses to consume the window events synchronously.
         * </p>
         * <p>
         * The {@code drainOnCancel} enables registering for a drain loop run when the window termination gets triggered
         * from "outside WindowedSubscriber", which will be the case if window flux gets canceled. In all cases
         * other than cancel, the window termination (completion, error) is triggered from "within the WindowedSubscriber".
         * The WindowedSubscriber needs to control or to be aware of the window termination, so that it can pick work for
         * the next window.
         * </p>
         *
         * @param drainOnCancel true if the drain loop needs to be run when the flux is canceled.
         * @return the flux streaming window events.
         */
        private Flux<T> windowFlux(boolean drainOnCancel) {
            final Function<Flux<T>, Flux<T>> decorator = parent.windowDecorator;
            final Flux<T> flux = decorator != null ? decorator.apply(sink.asFlux()) : sink.asFlux();
            if (drainOnCancel) {
                return flux.doFinally(s -> {
                    if (s == SignalType.CANCEL) {
                        isCanceled.set(true);
                        final WindowWork<T> w = this;
                        // It's very likely that the cancel signaling happened from application (user) thread.
                        // Offload the responsibility of drain-loop run (for rolling to next work) to worker thread,
                        // and free up the application thread.
                        Schedulers.boundedElastic().schedule(() -> parent.postTimedOutOrCanceledWork(w));
                    }
                });
            } else {
                return flux;
            }
        }

        /**
         * CONTRACT: Never invoke from the outside of serialized drain-loop.
         * <p>
         * Attempt to deliver the next item to the work's window.
         * </p>
         * @param item the item to emit.
         * @return the result of the emission attempt.
         */
        private EmitNextResult tryEmitNext(T item) {
            final int c = pending.getAndDecrement();
            if (c <= 0) {
                if (c < 0) {
                    withPendingKey(logger.atWarning()).log("Unexpected emit-next attempt when no more demand.");
                }
                return EmitNextResult.RECEIVED_DEMANDED;
            }

            final Sinks.EmitResult emitResult;
            try {
                emitResult = sink.tryEmitNext(item);
            } catch (Throwable e) {
                // Normally operator(s) applied to the 'sink' catches and bubbles down the consumer thrown error,
                // rather than bubbling up to here. If error happens to arrive here, we'll need to close the window.
                consumerError.set(e);
                withPendingKey(logger.atError()).log("Unexpected consumer error occurred while emitting.", e);
                return EmitNextResult.CONSUMER_ERROR;
            }

            if (emitResult == Sinks.EmitResult.OK) {
                return EmitNextResult.OK;
            } else {
                withPendingKey(logger.atError()).addKeyValue(EMIT_RESULT_KEY, emitResult).log("Could not emit-next.");
                return EmitNextResult.SINK_ERROR;
            }
        }

        /**
         * CONTRACT: Never invoke from the outside of serialized drain-loop.
         * <p>
         * Terminate the work to close the window it represents.
         * </p>
         * @param terminalState the terminal state of the work.
         */
        private void terminate(WorkTerminalState terminalState) {
            if (isTerminated.getAndSet(true)) {
                return;
            }

            try {
                timers.dispose();
            } finally {
                if (terminalState == WorkTerminalState.SINK_ERROR) {
                    withPendingKey(logger.atWarning()).addKeyValue("reason", "sink-error").log(TERMINATING_WORK);
                    return;
                }

                if (terminalState == WorkTerminalState.CANCELED) {
                    assertCondition(isCanceled(), terminalState);
                    withPendingKey(logger.atWarning()).addKeyValue("reason", "sink-canceled").log(TERMINATING_WORK);
                    return;
                }

                if (terminalState == WorkTerminalState.RECEIVED_DEMANDED) {
                    assertCondition(hasReceivedDemanded(), terminalState);
                    withPendingKey(logger.atVerbose()).log(TERMINATING_WORK);
                    closeWindow();
                    return;
                }

                if (terminalState == WorkTerminalState.CONSUMER_ERROR) {
                    assertCondition(hasConsumerError(), terminalState);
                    final Throwable e = consumerError.get();
                    withPendingKey(logger.atWarning()).log(e.getMessage(), e);
                    closeWindow(e);
                    return;
                }

                if (terminalState == WorkTerminalState.TIMED_OUT) {
                    assertCondition(hasTimedOut(), terminalState);
                    final TimeoutReason reason = timeoutReason.get();
                    final Throwable e = reason.getError();
                    if (e != null) {
                        withPendingKey(logger.atWarning()).addKeyValue("reason", reason.getMessage())
                            .log(TERMINATING_WORK, e);
                        closeWindow(e);
                    } else {
                        withPendingKey(logger.atVerbose()).addKeyValue("reason", reason.getMessage())
                            .log(TERMINATING_WORK);
                        closeWindow();
                    }
                    return;
                }

                if (terminalState == WorkTerminalState.PARENT_TERMINAL) {
                    assertCondition(parent.isDoneOrCanceled(), terminalState);
                    final Throwable e = parent.getTerminalError();
                    withPendingKey(logger.atWarning()).log(e.getMessage(), e);
                    closeWindow(e);
                    return;
                }

                if (terminalState == WorkTerminalState.PARENT_TERMINAL_CLEAN_CLOSE) {
                    assertCondition(parent.isDoneOrCanceled() && isStreaming(), terminalState);
                    withPendingKey(logger.atWarning()).addKeyValue("reason", "terminal-clean-close")
                        .log(TERMINATING_WORK);
                    closeWindow();
                    return;
                }
            }
            throw logger.atError().log(new IllegalStateException("Unknown work terminal state." + terminalState));
        }

        /**
         * Starts a timer that upon expiration trigger the window close signal.
         *
         * @return {@link Disposable} to cancel the timer.
         */
        private Disposable beginTimeoutTimer() {
            final Disposable disposable = Mono.delay(timeout)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(__ -> onTimeout(TimeoutReason.TIMEOUT), e -> onTimeout(TimeoutReason.timeoutErrored(e)));
            return disposable;
        }

        /**
         * Start the timer to trigger the window close signal if the next item does not arrive within timeout.
         *
         * @return {@link Disposable} to cancel the timer.
         */
        private Disposable beginNextItemTimeoutTimer() {
            final Duration nextItemTimout = parent.nextItemTimout;
            if (nextItemTimout == null) {
                return Disposables.disposed();
            }
            final Flux<Mono<Long>> nextItemTimer = sink.asFlux().map(__ -> Mono.delay(nextItemTimout));
            final Disposable disposable = Flux.switchOnNext(nextItemTimer)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(__ -> onTimeout(TimeoutReason.TIMEOUT_NEXT_ITEM),
                    e -> onTimeout(TimeoutReason.timeoutNextItemErrored(e)));
            return disposable;
        }

        /**
         * Signal the window close by timeout.
         *
         * @param reason the timeout reason.
         */
        private void onTimeout(TimeoutReason reason) {
            // The call sites invokes 'onTimeout' in a worker (boundedElastic) thread, to offload the drain-loop
            // run responsibility to the worker. This saves parallel thread (triggering timeout) from running drain.
            if (timeoutReason.compareAndSet(null, reason)) {
                final WindowWork<T> w = this;
                parent.postTimedOutOrCanceledWork(w);
            }
        }

        /**
         * Assert the condition expected to be met for a given terminal state.
         *
         * @param condition the condition
         * @param terminalState the work terminal state.
         *
         * @throws IllegalStateException if the condition is not met.
         */
        private void assertCondition(boolean condition, WorkTerminalState terminalState) {
            if (condition) {
                return;
            }
            final String message = String.format("Illegal invocation of terminate(%s).", terminalState);
            throw withPendingKey(logger.atError()).log(new IllegalStateException(message));
        }

        /**
         * CONTRACT: The call site must originate from serialized drain-loop i.e.,
         * drainLoop() -> terminate(terminalState) -> closeWindow().
         * <p>
         * Attempt successful closure of the window.
         */
        private void closeWindow() {
            sink.emitComplete((signalType, emitResult) -> {
                logger.atError()
                    .addKeyValue(SIGNAL_TYPE_KEY, signalType)
                    .addKeyValue(EMIT_RESULT_KEY, emitResult)
                    .log("Could not close window.");
                return false;
            });
        }

        /**
         * CONTRACT: The call site must originate from serialized drain-loop i.e.,
         * drainLoop() -> terminate(terminalState) -> closeWindow(e).
         * <p>
         * Attempt to close the window with error.
         *
         * @param e the error to close the window with.
         */
        private void closeWindow(Throwable e) {
            sink.emitError(e, (signalType, emitResult) -> {
                logger.atError()
                    .addKeyValue(SIGNAL_TYPE_KEY, signalType)
                    .addKeyValue(EMIT_RESULT_KEY, emitResult)
                    .log("Could not closed window with error.");
                return false;
            });
        }

        /**
         * Annotate the logger with current number of items pending to meet the window demand.
         *
         * @param logger the logger to annotate.
         * @return the annotated logger.
         */
        private LoggingEventBuilder withPendingKey(LoggingEventBuilder logger) {
            return logger.addKeyValue(PENDING_KEY, pending.get());
        }

        /**
         * Gets the logger (used by parent subscriber when initializing this work).
         *
         * @return the logger.
         */
        private LoggingEventBuilder getLogger() {
            return withPendingKey(logger.atVerbose());
        }

        /**
         * Creates the logger for the work.
         *
         * @param parentLogContext the parent logging context.
         * @param id the identifier for the work.
         * @param demand the demand for the window this work represents.
         * @return the logger for the work.
         */
        private static ClientLogger createLogger(Map<String, Object> parentLogContext, long id, int demand) {
            final Map<String, Object> loggingContext = new HashMap<>(parentLogContext.size() + 5);
            loggingContext.putAll(parentLogContext);
            loggingContext.put(WORK_ID_KEY, id);
            loggingContext.put(DEMAND_KEY, demand);
            return new ClientLogger(WindowWork.class, loggingContext);
        }

        /**
         * Creates the sink to signal window events.
         * <p>
         * Events signaling (items, termination) to this sink will be serialized by the parent's drain-loop,
         * <ul>
         *    <li>items signaling {@link WindowWork#tryEmitNext(Object)}</li>
         *    <li>termination signaling {@link WindowWork#terminate(WorkTerminalState)}</li>
         * </ul>
         * </p>
         * <p>
         * There will be two subscribers to this sink,
         * <ul>
         *     <li>subscription for next item timeout.</li>
         *     <li>subscription from IterableStream.</li>
         * </ul>
         * to support this multi-subscription broadcasting use case, the sink is replay-able.
         * </p>
         * <p>
         * This sink has an internal unbounded queue acting as a buffer between the drain-loop and the consumer of the
         * window. This allows drain-loop to enqueue the window items as fast as it could and move to processing next
         * window work. The window size will be the cap for the number of items that gets enqueued.
         * </p>
         *
         * @return the sink to signal window events (items and termination).
         * @param <T> type of items in the sink.
         */
        private static <T> Sinks.Many<T> createSink() {
            return Sinks.many().replay().all();
        }

        /**
         * A type describing a successful completion of timeout or an error while scheduling or waiting for timeout.
         */
        private static final class TimeoutReason {
            static final TimeoutReason TIMEOUT = new TimeoutReason("Timeout occurred.", null);
            static final TimeoutReason TIMEOUT_NEXT_ITEM
                = new TimeoutReason("Timeout between the messages occurred.", null);

            private final String message;
            private final Throwable error;

            /**
             * Create reason describing the error while scheduling or waiting for timeout.
             *
             * @param error indicates anything internal to Reactor failing timeout attempt. E.g., error in scheduling
             *     timeout task when internal Scheduler throws {@link RejectedExecutionException}.
             * @return the reason.
             */
            static TimeoutReason timeoutErrored(Throwable error) {
                return new TimeoutReason("Error while scheduling or waiting for timeout.", error);
            }

            /**
             * Create reason describing the error while scheduling or waiting for timeout between items.
             *
             * @param error indicates anything internal to Reactor failing timeout attempt. E.g., error in scheduling
             *     timeout task when internal Scheduler throws {@link RejectedExecutionException}.
             * @return the reason.
             */
            static TimeoutReason timeoutNextItemErrored(Throwable error) {
                return new TimeoutReason("Error while scheduling or waiting for timeout between the messages.", error);
            }

            private TimeoutReason(String message, Throwable error) {
                this.message = message;
                this.error = error;
            }

            String getMessage() {
                return message;
            }

            Throwable getError() {
                return error;
            }
        }
    }

    /**
     * Represents the result of an item emission attempt to a window.
     */
    private enum EmitNextResult {
        /**
         * Indicate that the emission attempt succeeded.
         */
        OK,
        /**
         * Indicate that the consumer thrown unexpected error when handling the item.
         */
        CONSUMER_ERROR,
        /**
         * Indicate that the emission attempt was rejected as the demand for the window was already satisfied.
         */
        RECEIVED_DEMANDED,
        /**
         * Indicate that the emission attempt was rejected as there was a failure in delivering the item to the window sink.
         */
        SINK_ERROR
    }

    /**
     * Represents the terminal state of the work that indicates need for window closure.
     */
    private enum WorkTerminalState {
        /**
         * Indicate that work's window should be closed with error due to parent {@link WindowedSubscriber} subscriber
         * termination (triggered by upstream termination or downstream cancel).
         */
        PARENT_TERMINAL,
        /**
         * Indicate that work's window is in 'streaming state' and parent {@link WindowedSubscriber} subscriber is
         * terminated, close the window without error (i.e, ignore the parent terminal error).
         * <p>
         * The window is called 'streaming window' or in 'streaming state', if it has sent at least one item but still
         * needs more upstream items to meet its demand.
         * </p>
         *
         * @see WindowedSubscriberOptions#cleanCloseStreamingWindowOnTerminate()
         */
        PARENT_TERMINAL_CLEAN_CLOSE,
        /**
         * Indicate that work's window should be closed either without error (when timeout occur) or with error
         * (if there was a failure while scheduling or waiting for timeout).
         *
         * @see WindowWork.TimeoutReason
         */
        TIMED_OUT,
        /**
         * Indicate that work's window should be closed with error due to unexpected consumer error while consuming item.
         */
        CONSUMER_ERROR,
        /**
         * Indicate that work's window should be closed without error given it received demanded number of items.
         */
        RECEIVED_DEMANDED,
        /**
         * Indicate that the last emit-next operation on window sink was errored, hence any future operations on the
         * sink may fail as well, consider window was broken, hence no explicit window closure needed.
         */
        SINK_ERROR,
        /**
         * Indicate that downstream dropped the window by signaling cancellation, from the perspective of the work, it is
         * as if the window got closed, hence no explicit window closure needed.
         */
        CANCELED,
    }

    /**
     * Holds result of a window request enqueued to {@link WindowedSubscriber#enqueueRequestImpl(int, Duration)}.
     * <p>
     * Package-private type to give access to window streams and internal {@link WindowWork} for testing purposes.
     * </p>
     * @param <T> the type of items in the window.
     */
    static final class EnqueueResult<T> {
        private final WindowWork<T> work;
        private final Flux<T> windowFlux;
        private final IterableStream<T> windowIterable;

        private EnqueueResult(WindowWork<T> work, Flux<T> windowFlux) {
            this.work = work;
            this.windowFlux = windowFlux;
            this.windowIterable = new IterableStream<>(windowFlux);
        }

        WindowWork<T> getInnerWork() {
            return work;
        }

        Flux<T> getWindowFlux() {
            return windowFlux;
        }

        IterableStream<T> getWindowIterable() {
            return windowIterable;
        }
    }
}
