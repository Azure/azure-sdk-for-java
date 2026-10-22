// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Operators;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;

/**
 * Aggregates {@link EventData} into {@link EventDataBatch} and pushes them downstream when:
 *
 * <ul>
 *     <li>{@link BufferedProducerClientOptions#getMaxWaitTime()} elapses between events.</li>
 *     <li>{@link EventDataBatch} cannot hold any more events.</li>
 * </ul>
 */
class EventDataAggregator extends FluxOperator<EventData, EventDataBatch> {
    private static final ClientLogger LOGGER = new ClientLogger(EventDataAggregator.class);

    private volatile EventDataAggregatorMain downstreamSubscription;
    private static final AtomicReferenceFieldUpdater<EventDataAggregator, EventDataAggregatorMain> DOWNSTREAM_SUBSCRIPTION
        = AtomicReferenceFieldUpdater.newUpdater(EventDataAggregator.class, EventDataAggregatorMain.class,
            "downstreamSubscription");

    private final Supplier<EventDataBatch> batchSupplier;
    private final String namespace;
    private final BufferedProducerClientOptions options;
    private final String partitionId;

    /**
     * Build a {@link FluxOperator} wrapper around the passed parent {@link Publisher}
     *
     * @param source the {@link Publisher} to decorate
     */
    EventDataAggregator(Flux<? extends EventData> source, Supplier<EventDataBatch> batchSupplier, String namespace,
        BufferedProducerClientOptions options, String partitionId) {
        super(source);

        this.partitionId = partitionId;
        this.batchSupplier = batchSupplier;
        this.namespace = namespace;
        this.options = options;
    }

    /**
     * Subscribes to events from this operator.  Downstream subscribers invoke this method and subscribe to events from
     * it.
     *
     * @param actual Downstream subscriber.
     */
    @Override
    public void subscribe(CoreSubscriber<? super EventDataBatch> actual) {
        final EventDataAggregatorMain subscription
            = new EventDataAggregatorMain(actual, namespace, options, batchSupplier, partitionId, LOGGER);

        if (DOWNSTREAM_SUBSCRIPTION.compareAndSet(this, null, subscription)) {
            source.subscribe(subscription);
        } else {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("Cannot resubscribe to multiple upstreams."));
        }
    }

    int getNumberOfEvents() {
        final EventDataAggregatorMain downstream = downstreamSubscription;
        if (downstream == null) {
            return 0;
        }

        return downstream.getNumberOfEventsInCurrentBatch();
    }

    /**
     * Main implementation class for subscribing to the upstream source and publishing events downstream.
     */
    static class EventDataAggregatorMain implements Subscription, CoreSubscriber<EventData> {
        /**
         * The number of requested EventDataBatches.
         */
        private volatile long requested;
        private static final AtomicLongFieldUpdater<EventDataAggregatorMain> REQUESTED
            = AtomicLongFieldUpdater.newUpdater(EventDataAggregatorMain.class, "requested");

        private final Sinks.Many<Long> eventSink;
        private final Scheduler timerScheduler;
        private final Disposable disposable;

        private final AtomicBoolean isCompleted = new AtomicBoolean(false);
        private final CoreSubscriber<? super EventDataBatch> downstream;

        private final String partitionId;
        private final ClientLogger logger;
        private final Supplier<EventDataBatch> batchSupplier;
        private final String namespace;
        private final Consumer<SendBatchFailedContext> onSendFailed;
        private final Object lock = new Object();

        private Subscription subscription;
        private EventDataBatch currentBatch;
        // Guarded by lock.
        private RuntimeException lastBatchCreationError;

        EventDataAggregatorMain(CoreSubscriber<? super EventDataBatch> downstream, String namespace,
            BufferedProducerClientOptions options, Supplier<EventDataBatch> batchSupplier, String partitionId,
            ClientLogger logger) {
            this.namespace = namespace;
            this.downstream = downstream;
            this.partitionId = partitionId;
            this.logger = logger;
            this.batchSupplier = batchSupplier;
            this.onSendFailed = options.getSendFailedContext();
            // If this fails, the batch is created lazily when the first event arrives.
            synchronized (lock) {
                getOrCreateBatch();
            }

            this.eventSink = Sinks.many().unicast().onBackpressureError();
            // Every new batch cancels the running interval, and cancelling it interrupts the interval's thread if a
            // tick is in progress. Handling ticks on another thread keeps that interrupt away from the batch
            // creation (which would fail) and the send (which would close the connection's IO pipe).
            // That thread is owned by this aggregator: a tick blocks while the next batch is created, and the
            // created batch is delivered on the producer's scheduler (boundedElastic by default). If ticks ran on
            // that scheduler, they could occupy all of its threads and wait forever for their own results.
            this.timerScheduler = Schedulers.newSingle("eventhubs-buffered-producer-timer-" + partitionId, true);
            this.disposable = Flux
                .switchOnNext(eventSink.asFlux()
                    .map(e -> Flux.interval(options.getMaxWaitTime()).takeUntil(index -> isCompleted.get())))
                .onBackpressureDrop()
                .publishOn(timerScheduler, 1)
                .subscribe(index -> {
                    logger.atVerbose()
                        .addKeyValue(PARTITION_ID_KEY, partitionId)
                        .log("Time elapsed. Attempt to publish downstream.");
                    updateOrPublishBatch(null, true);
                });
        }

        /**
         * The number of events in the current batch.
         *
         * @return Number of events in the current batch.
         */
        public int getNumberOfEventsInCurrentBatch() {
            final EventDataBatch b;
            synchronized (lock) {
                b = currentBatch;
            }

            return b != null ? b.getCount() : 0;

        }

        /**
         * Request a number of {@link EventDataBatch}.
         *
         * @param n Number of batches requested.
         */
        @Override
        public void request(long n) {
            if (!Operators.validate(n)) {
                return;
            }

            Operators.addCap(REQUESTED, this, n);
            subscription.request(n);
        }

        /**
         * Cancels the subscription upstream.
         */
        @Override
        public void cancel() {
            if (!isCompleted.compareAndSet(false, true)) {
                return;
            }

            // Do not keep requesting more events upstream
            logger.atVerbose().addKeyValue(PARTITION_ID_KEY, partitionId).log("Disposing of aggregator.");
            subscription.cancel();

            updateOrPublishBatch(null, true);
            downstream.onComplete();
            disposable.dispose();
            disposeTimerScheduler();
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (subscription != null) {
                logger.warning("Subscription was already set. Cancelling existing subscription.");
                subscription.cancel();
            } else {
                subscription = s;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(EventData eventData) {
            // maxWaitTime is measured from the first event in a batch. Restarting the timer on every event would
            // postpone the time-based publish for as long as events keep arriving faster than maxWaitTime, leaving a
            // partially full batch waiting until it fills up.
            if (updateOrPublishBatch(eventData, false)) {
                eventSink.emitNext(1L, Sinks.EmitFailureHandler.FAIL_FAST);
            }

            // When an EventDataBatch is pushed downstream, we decrement REQUESTED. However, if REQUESTED is still > 0,
            // that means we did not publish the EventDataBatch (i.e. because it was not full). We request another
            // EventData upstream to try and fill this EventDataBatch and push it downstream.
            final long left = REQUESTED.get(this);
            if (left > 0) {
                subscription.request(1L);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!isCompleted.compareAndSet(false, true)) {
                Operators.onErrorDropped(t, downstream.currentContext());
                return;
            }

            updateOrPublishBatch(null, true);
            downstream.onError(t);
            disposeTimerScheduler();
        }

        /**
         * Upstream signals a completion.
         */
        @Override
        public void onComplete() {
            if (isCompleted.compareAndSet(false, true)) {
                updateOrPublishBatch(null, true);
                downstream.onComplete();
                disposeTimerScheduler();
            }
        }

        /**
         * Shuts down the timer thread without interrupting a tick in progress. Further ticks are no-ops since the
         * aggregator is completed.
         */
        private void disposeTimerScheduler() {
            timerScheduler.disposeGracefully()
                .subscribe(null,
                    error -> logger.atVerbose()
                        .addKeyValue(PARTITION_ID_KEY, partitionId)
                        .log("Unable to dispose timer scheduler.", error));
        }

        /**
         * Adds {@code eventData} to the current batch, publishing the batch downstream first if it is full.
         * <p>
         * Failures that only affect this event (it is too large for a batch, or a new batch could not be created)
         * are reported through the send-failed handler instead of terminating the aggregator. Terminating it would
         * permanently stop publishing to this partition.
         *
         * @param eventData EventData to add to or null if there are no events to add to the batch.
         * @param alwaysPublish {@code true} to always push batch downstream. {@code false}, otherwise.
         * @return {@code true} if {@code eventData} was added as the first event of a batch.
         */
        private boolean updateOrPublishBatch(EventData eventData, boolean alwaysPublish) {
            final boolean isFlush = isFlushSignal(eventData);
            if (alwaysPublish || isFlush) {
                publishDownstream(isFlush);
                return false;
            }
            if (eventData == null) {
                // EventData will be null in the case when options.maxWaitTime() has elapsed  and we want to push the
                // batch downstream.
                return false;
            }

            Throwable failure;
            synchronized (lock) {
                EventDataBatch batch = getOrCreateBatch();
                if (batch != null) {
                    failure = tryAdd(batch, eventData);
                    if (failure == null) {
                        return batch.getCount() == 1;
                    }

                    publishDownstream(false);

                    batch = getOrCreateBatch();
                    if (batch != null) {
                        failure = tryAdd(batch, eventData);
                        if (failure == null) {
                            return batch.getCount() == 1;
                        }
                    } else {
                        failure = batchCreationError();
                    }
                } else {
                    failure = batchCreationError();
                }
            }

            reportFailedEvent(eventData, failure);
            return false;
        }

        /**
         * Must be called while holding {@link #lock}.
         *
         * @return The current batch, creating one if needed; {@code null} if the aggregator is completed or a new
         *     batch could not be created (the error is kept in {@link #lastBatchCreationError}).
         */
        private EventDataBatch getOrCreateBatch() {
            if (currentBatch == null && !isCompleted.get()) {
                try {
                    currentBatch = batchSupplier.get();
                    lastBatchCreationError = null;
                } catch (RuntimeException e) {
                    lastBatchCreationError = e;
                    logger.atWarning()
                        .addKeyValue(PARTITION_ID_KEY, partitionId)
                        .log("Unable to create a new batch. It will be retried on the next event.", e);
                }
            }
            return currentBatch;
        }

        private Throwable batchCreationError() {
            if (isCompleted.get()) {
                return new IllegalStateException("Partition publisher id[" + partitionId + "] is completed.");
            }
            final Throwable error = lastBatchCreationError;
            return error != null ? error : new IllegalStateException("Unable to create a new batch.");
        }

        /**
         * @return {@code null} if the event was added, otherwise the reason it could not be added.
         */
        private Throwable tryAdd(EventDataBatch batch, EventData eventData) {
            try {
                if (batch.tryAdd(eventData)) {
                    return null;
                }
            } catch (AmqpException e) {
                // Thrown when a single event exceeds the maximum size allowed for a batch.
                return e;
            }
            return new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
                "EventData exceeded maximum size.", new AmqpErrorContext(namespace));
        }

        private void reportFailedEvent(EventData eventData, Throwable error) {
            logger.atWarning()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .log("Unable to add event to a batch. Reporting it as failed.", error);

            if (onSendFailed == null) {
                return;
            }
            try {
                onSendFailed
                    .accept(new SendBatchFailedContext(Collections.singletonList(eventData), partitionId, error));
            } catch (Throwable e) {
                logger.atWarning()
                    .addKeyValue(PARTITION_ID_KEY, partitionId)
                    .log("onSendBatchFailed handler threw.", e);
            }
        }

        /**
         * Publishes batch downstream if there are events in the batch and prepares the next one.
         */
        private void publishDownstream(boolean isFlush) {
            EventDataBatch previous = null;
            try {
                synchronized (lock) {
                    previous = this.currentBatch;
                    if (previous == null || previous.getEvents().isEmpty()) {
                        if (isFlush) {
                            // Even if there is no batch or it is empty, we'll push EMPTY on a flush signal.
                            // This ensures any flush related flags set at the call site are reset.
                            downstream.onNext(EventDataBatch.EMPTY);
                        }
                        return;
                    }

                    // Detach before creating the next batch so that a failed creation can never cause this
                    // (already published) batch to be filled and sent again.
                    this.currentBatch = null;
                    downstream.onNext(previous);

                    final long batchesLeft = REQUESTED.updateAndGet(this, (v) -> {
                        if (v == Long.MAX_VALUE) {
                            return v;
                        } else {
                            return v - 1;
                        }
                    });

                    logger.verbose(previous + ": Batch published. Requested batches left: {}", batchesLeft);

                    if (!isCompleted.get()) {
                        getOrCreateBatch();
                    } else {
                        logger.verbose("Aggregator is completed. Not setting another batch.");
                    }
                }
            } catch (Throwable e) {
                final Throwable error = Operators.onNextError(previous, e, downstream.currentContext(), subscription);

                logger.warning("Unable to push batch downstream to publish.", error);

                if (error != null) {
                    onError(error);
                }
            }
        }

        private static boolean isFlushSignal(EventData eventData) {
            return eventData instanceof FlushSignal;
        }
    }
}
