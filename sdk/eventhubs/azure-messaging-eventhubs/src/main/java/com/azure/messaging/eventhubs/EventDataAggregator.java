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
import reactor.core.Exceptions;
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
import java.util.function.BiConsumer;
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
    private final BiConsumer<EventData, Throwable> eventFailureReporter;

    /**
     * Build a {@link FluxOperator} wrapper around the passed parent {@link Publisher}
     *
     * @param source the {@link Publisher} to decorate
     */
    EventDataAggregator(Flux<? extends EventData> source, Supplier<EventDataBatch> batchSupplier, String namespace,
        BufferedProducerClientOptions options, String partitionId) {
        this(source, batchSupplier, namespace, options, partitionId, null);
    }

    EventDataAggregator(Flux<? extends EventData> source, Supplier<EventDataBatch> batchSupplier, String namespace,
        BufferedProducerClientOptions options, String partitionId,
        BiConsumer<EventData, Throwable> eventFailureReporter) {
        super(source);

        this.partitionId = partitionId;
        this.batchSupplier = batchSupplier;
        this.namespace = namespace;
        this.options = options;
        this.eventFailureReporter = eventFailureReporter;
    }

    /**
     * Subscribes to events from this operator.  Downstream subscribers invoke this method and subscribe to events from
     * it.
     *
     * @param actual Downstream subscriber.
     */
    @Override
    public void subscribe(CoreSubscriber<? super EventDataBatch> actual) {
        final EventDataAggregatorMain subscription = new EventDataAggregatorMain(actual, namespace, options,
            batchSupplier, partitionId, eventFailureReporter, LOGGER);

        if (DOWNSTREAM_SUBSCRIPTION.compareAndSet(this, null, subscription)) {
            source.subscribe(subscription);
        } else {
            subscription.disposeTimer();
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("Cannot resubscribe to multiple upstreams."));
        }
    }

    void requestNextEvent() {
        final EventDataAggregatorMain downstream = downstreamSubscription;
        if (downstream != null) {
            downstream.requestNextEvent();
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
        private final Disposable disposable;
        private final Scheduler timerScheduler;

        private final AtomicBoolean isCompleted = new AtomicBoolean(false);
        private final CoreSubscriber<? super EventDataBatch> downstream;

        private final String partitionId;
        private final ClientLogger logger;
        private final Supplier<EventDataBatch> batchSupplier;
        private final String namespace;
        private final BiConsumer<EventData, Throwable> eventFailureReporter;
        private final boolean deferFailedEventDemand;
        private final Object lock = new Object();

        private Subscription subscription;
        private EventDataBatch currentBatch;

        // Guarded by lock. A later event retries batch creation.
        private RuntimeException batchCreationError;

        EventDataAggregatorMain(CoreSubscriber<? super EventDataBatch> downstream, String namespace,
            BufferedProducerClientOptions options, Supplier<EventDataBatch> batchSupplier, String partitionId,
            BiConsumer<EventData, Throwable> eventFailureReporter, ClientLogger logger) {
            this.namespace = namespace;
            this.downstream = downstream;
            this.partitionId = partitionId;
            this.logger = logger;
            this.batchSupplier = batchSupplier;
            this.deferFailedEventDemand = eventFailureReporter != null;
            this.eventFailureReporter = eventFailureReporter != null ? eventFailureReporter : (event, error) -> {
                if (options.getSendFailedContext() != null) {
                    options.getSendFailedContext()
                        .accept(new SendBatchFailedContext(Collections.singletonList(event), partitionId, error));
                }
            };
            synchronized (lock) {
                getOrCreateBatch();
            }

            this.eventSink = Sinks.many().unicast().onBackpressureError();
            // Restarting an interval can interrupt its thread. Keep blocking batch creation on a separate scheduler.
            // This scheduler must also be separate from the producer scheduler that completes batch creation.
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

            try {
                updateOrPublishBatch(null, true);
                downstream.onComplete();
            } finally {
                disposeTimer();
            }
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
            if (isCompleted.get()) {
                return;
            }
            final boolean requestMore = updateOrPublishBatch(eventData, false);

            eventSink.emitNext(1L, Sinks.EmitFailureHandler.FAIL_FAST);

            // When an EventDataBatch is pushed downstream, we decrement REQUESTED. However, if REQUESTED is still > 0,
            // that means we did not publish the EventDataBatch (i.e. because it was not full). We request another
            // EventData upstream to try and fill this EventDataBatch and push it downstream.
            if (requestMore) {
                requestNextEvent();
            }
        }

        private void requestNextEvent() {
            if (!isCompleted.get() && REQUESTED.get(this) > 0) {
                subscription.request(1L);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!isCompleted.compareAndSet(false, true)) {
                Operators.onErrorDropped(t, downstream.currentContext());
                return;
            }

            try {
                updateOrPublishBatch(null, true);
                downstream.onError(t);
            } finally {
                disposeTimer();
            }
        }

        /**
         * Upstream signals a completion.
         */
        @Override
        public void onComplete() {
            if (isCompleted.compareAndSet(false, true)) {
                try {
                    updateOrPublishBatch(null, true);
                    downstream.onComplete();
                } finally {
                    disposeTimer();
                }
            }
        }

        private void disposeTimer() {
            disposable.dispose();
            // Let an active tick finish without interrupting a connection operation.
            timerScheduler.disposeGracefully()
                .subscribe(null, error -> logger.warning("Unable to dispose timer scheduler.", error));
        }

        /**
         * @param eventData EventData to add to or null if there are no events to add to the batch.
         * @param alwaysPublish {@code true} to always push batch downstream. {@code false}, otherwise.
         * @return Whether to request another event immediately. Failed events resume demand after their callback.
         */
        private boolean updateOrPublishBatch(EventData eventData, boolean alwaysPublish) {
            final boolean isFlush = isFlushSignal(eventData);
            if (alwaysPublish || isFlush) {
                publishDownstream(isFlush);
                return true;
            }
            if (eventData == null) {
                // EventData will be null in the case when options.maxWaitTime() has elapsed  and we want to push the
                // batch downstream.
                return true;
            }

            Throwable failure;
            synchronized (lock) {
                EventDataBatch batch = getOrCreateBatch();
                if (batch == null) {
                    failure = batchCreationError;
                } else {
                    failure = tryAdd(batch, eventData);
                    if (failure == null) {
                        return true;
                    }
                    publishDownstream(false);
                    batch = getOrCreateBatch();
                    if (batch == null) {
                        failure = batchCreationError;
                    } else {
                        failure = tryAdd(batch, eventData);
                        if (failure == null) {
                            return true;
                        }
                    }
                }
            }
            try {
                eventFailureReporter.accept(eventData, failure);
            } catch (Throwable error) {
                Exceptions.throwIfFatal(error);
                logger.warning("Unable to report a failed event.", error);
            }
            return !deferFailedEventDemand;
        }

        // Called with lock held.
        private EventDataBatch getOrCreateBatch() {
            if (currentBatch == null && !isCompleted.get()) {
                try {
                    currentBatch = batchSupplier.get();
                    batchCreationError = null;
                } catch (RuntimeException error) {
                    Exceptions.throwIfFatal(error);
                    batchCreationError = error;
                    logger.warning("Unable to create a batch. A later event will retry.", error);
                }
            }
            return currentBatch;
        }

        private Throwable tryAdd(EventDataBatch batch, EventData event) {
            try {
                if (batch.tryAdd(event)) {
                    return null;
                }
            } catch (AmqpException error) {
                return error;
            }
            return new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
                "EventData exceeded maximum size.", new AmqpErrorContext(namespace));
        }

        /**
         * Publishes batch downstream if there are events in the batch and updates it.
         */
        private void publishDownstream(boolean isFlush) {
            EventDataBatch previous = null;
            try {
                synchronized (lock) {
                    previous = this.currentBatch;
                    if (previous == null || previous.getEvents().isEmpty()) {
                        if (isFlush) {
                            // Even if the batch is empty, we'll push EMPTY on a flush signal.
                            // This ensures any flush related flags set at the call site are reset.
                            downstream.onNext(EventDataBatch.EMPTY);
                        }
                        return;
                    }

                    // Detach before publishing so a failed replacement cannot cause a duplicate send.
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
                        this.currentBatch = null;
                    }
                }
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
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
