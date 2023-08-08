// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import com.azure.messaging.eventhubs.implementation.UncheckedExecutionException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Operators;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
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

    private final AtomicReference<EventDataAggregatorMain> downstreamSubscription = new AtomicReference<>();
    private final Supplier<EventDataBatch> batchSupplier;
    private final String namespace;
    private final BufferedProducerClientOptions options;
    private final String partitionId;

    /**
     * Build a {@link FluxOperator} wrapper around the passed parent {@link Publisher}
     *
     * @param source the {@link Publisher} to decorate
     */
    EventDataAggregator(Flux<? extends EventData> source, Supplier<EventDataBatch> batchSupplier,
        String namespace, BufferedProducerClientOptions options, String partitionId) {
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
        final EventDataAggregatorMain subscription = new EventDataAggregatorMain(actual, namespace, options,
            batchSupplier, partitionId, LOGGER);

        if (!downstreamSubscription.compareAndSet(null, subscription)) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException(
                "Cannot resubscribe to multiple upstreams."));
        }

        source.subscribe(subscription);
    }

    /**
     * Main implementation class for subscribing to the upstream source and publishing events downstream.
     */
    static class EventDataAggregatorMain implements Subscription, CoreSubscriber<EventData> {
        /**
         * The number of requested EventDataBatches.
         */
        private volatile long requested;
        private static final AtomicLongFieldUpdater<EventDataAggregatorMain> REQUESTED =
            AtomicLongFieldUpdater.newUpdater(EventDataAggregatorMain.class, "requested");

        private final Sinks.Many<Long> eventSink;
        private final Disposable disposable;

        private final AtomicBoolean isCompleted = new AtomicBoolean(false);
        private final CoreSubscriber<? super EventDataBatch> downstream;

        private final String partitionId;
        private final ClientLogger logger;
        private final Supplier<EventDataBatch> batchSupplier;
        private final String namespace;
        private final Object lock = new Object();

        private Subscription subscription;
        private EventDataBatch currentBatch;

        private volatile Throwable lastError;

        EventDataAggregatorMain(CoreSubscriber<? super EventDataBatch> downstream, String namespace,
            BufferedProducerClientOptions options, Supplier<EventDataBatch> batchSupplier, String partitionId,
            ClientLogger logger) {
            this.namespace = namespace;
            this.downstream = downstream;
            this.partitionId = partitionId;
            this.logger = logger;
            this.batchSupplier = batchSupplier;
            this.currentBatch = batchSupplier.get();

            this.eventSink = Sinks.many().unicast().onBackpressureError();
            this.disposable = Flux.switchOnNext(eventSink.asFlux().map(e -> Flux.interval(options.getMaxWaitTime())
                    .takeUntil(index -> isCompleted.get())))
                .subscribe(index -> {
                    logger.atVerbose()
                        .addKeyValue(PARTITION_ID_KEY, partitionId)
                        .log("Time elapsed. Attempt to publish downstream.");
                    updateOrPublishBatch(null, true);
                });
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
            logger.atVerbose()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .log("Disposing of aggregator.");
            subscription.cancel();

            updateOrPublishBatch(null, true);
            downstream.onComplete();
            disposable.dispose();
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
            updateOrPublishBatch(eventData, false);

            eventSink.emitNext(1L, Sinks.EmitFailureHandler.FAIL_FAST);

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
        }

        /**
         * Upstream signals a completion.
         */
        @Override
        public void onComplete() {
            if (isCompleted.compareAndSet(false, true)) {
                updateOrPublishBatch(null, true);
                downstream.onComplete();
            }
        }

        /**
         * @param eventData EventData to add to or null if there are no events to add to the batch.
         * @param alwaysPublish {@code true} to always push batch downstream. {@code false}, otherwise.
         */
        private void updateOrPublishBatch(EventData eventData, boolean alwaysPublish) {
            if (alwaysPublish) {
                publishDownstream();
                return;
            } else if (eventData == null) {
                // EventData will be null in the case when options.maxWaitTime() has elapsed  and we want to push the
                // batch downstream.
                return;
            }

            boolean added;
            synchronized (lock) {
                added = currentBatch.tryAdd(eventData);

                if (added) {
                    return;
                }

                publishDownstream();
                added = currentBatch.tryAdd(eventData);
            }

            if (!added) {
                final AmqpException error = new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
                    "EventData exceeded maximum size.", new AmqpErrorContext(namespace));

                onError(error);
            }
        }

        /**
         * Publishes batch downstream if there are events in the batch and updates it.
         */
        private void publishDownstream() {
            EventDataBatch previous = null;

            try {
                synchronized (lock) {
                    previous = this.currentBatch;

                    if (previous == null) {
                        logger.warning("Batch should not be null, setting a new batch.");

                        this.currentBatch = batchSupplier.get();
                        return;
                    } else if (previous.getEvents().isEmpty()) {
                        return;
                    }

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
                        this.currentBatch = batchSupplier.get();
                    } else {
                        logger.verbose("Aggregator is completed. Not setting another batch.");
                        this.currentBatch = null;
                    }
                }
            } catch (UncheckedExecutionException exception) {
                logger.info("An exception occurred while trying to create a new batch.", exception);

                if (this.lastError != null) {
                    logger.info("Exception has been set already, terminating EventDataAggregator.");

                    final Throwable error = Operators.onNextError(previous, exception, downstream.currentContext(),
                        subscription);

                    if (error != null) {
                        onError(error);
                    }
                } else {
                    this.lastError = exception;
                }

            } catch (Throwable e) {
                final Throwable error = Operators.onNextError(previous, e, downstream.currentContext(), subscription);

                logger.warning("Unable to push batch downstream to publish.", error);

                if (error != null) {
                    onError(error);
                }
            }
        }
    }
}
