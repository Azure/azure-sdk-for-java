// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import com.azure.messaging.eventhubs.implementation.UncheckedExecutionException;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.core.amqp.implementation.RetryUtil.withRetry;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.EMIT_RESULT_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;

/**
 * Keeps track of publishing events to a partition.
 */
class EventHubBufferedPartitionProducer implements Closeable {
    private final ClientLogger logger;

    private final AmqpRetryOptions retryOptions;
    private final EventHubProducerAsyncClient client;
    private final String partitionId;
    private final AmqpErrorContext errorContext;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Disposable publishSubscription;
    private final Sinks.Many<EventData> eventSink;
    private final CreateBatchOptions createBatchOptions;
    private final PublishResultSubscriber publishResultSubscriber;
    private final EventHubsTracer tracer;
    private final EventDataAggregator eventDataAggregator;

    EventHubBufferedPartitionProducer(EventHubProducerAsyncClient client, String partitionId,
        BufferedProducerClientOptions options, AmqpRetryOptions retryOptions, Sinks.Many<EventData> eventSink,
        Tracer tracer) {

        final HashMap<String, Object> logContext = new HashMap<>();
        logContext.put(PARTITION_ID_KEY, partitionId);
        this.logger = new ClientLogger(EventHubBufferedPartitionProducer.class, logContext);

        this.client = client;
        this.partitionId = partitionId;
        this.errorContext = new AmqpErrorContext(client.getFullyQualifiedNamespace());
        this.createBatchOptions = new CreateBatchOptions().setPartitionId(partitionId);
        this.retryOptions = retryOptions;
        this.eventSink = eventSink;

        this.eventDataAggregator = new EventDataAggregator(this.eventSink.asFlux(), this::createNewBatch,
            client.getFullyQualifiedNamespace(), options, partitionId);

        this.publishResultSubscriber = new PublishResultSubscriber(partitionId, this.eventSink,
            options.getSendSucceededContext(), options.getSendFailedContext(), retryOptions.getTryTimeout(), logger);

        this.publishSubscription = publishEvents(eventDataAggregator).publishOn(Schedulers.boundedElastic(), 1)
            .subscribeWith(publishResultSubscriber);

        this.tracer = new EventHubsTracer(tracer, client.getFullyQualifiedNamespace(), client.getEventHubName(), null);
    }

    /**
     * Enqueues an event into the queue.
     *
     * @param eventData Event to enqueue.
     *
     * @return A mono that completes when it is in the queue.
     *
     * @throws IllegalStateException if the partition processor is already closed when trying to enqueue another
     *     event.
     */
    Mono<Void> enqueueEvent(EventData eventData) {
        final Mono<Void> enqueueOperation = Mono.create(sink -> {
            if (isClosed.get()) {
                sink.error(new IllegalStateException(String
                    .format("Partition publisher id[%s] is already closed. Cannot enqueue more events.", partitionId)));
                return;
            }

            final TimeoutException e = publishResultSubscriber.awaitPendingFlush();
            if (e != null) {
                sink.error(e);
                return;
            }

            if (isClosed.get()) {
                sink.error(new IllegalStateException(String.format("Partition publisher id[%s] was "
                    + "closed between flushing events and now. Cannot enqueue events.", partitionId)));
                return;
            }

            tracer.reportMessageSpan(eventData, eventData.getContext());

            //TODO (conniey): Is this the right time to emit success. Or should we wait until it is added to batch?
            final Sinks.EmitResult emitResult = eventSink.tryEmitNext(eventData);
            if (emitResult.isSuccess()) {
                sink.success();
                return;
            }

            if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED || emitResult == Sinks.EmitResult.FAIL_OVERFLOW) {
                // If the draining queue is slower than the publishing queue.
                logger.atInfo()
                    .addKeyValue(EMIT_RESULT_KEY, emitResult)
                    .log("Event could not be published downstream. Applying retry.");

                sink.error(new AmqpException(true, emitResult + " occurred.", errorContext));
            } else {
                logger.atWarning()
                    .addKeyValue(EMIT_RESULT_KEY, emitResult)
                    .log("Event could not be published downstream. Not retrying.", emitResult);

                sink.error(new AmqpException(false, "Unable to buffer message for partition: " + getPartitionId(),
                    errorContext));
            }
        });

        return withRetry(enqueueOperation, retryOptions, "Timed out trying to enqueue event data.", true).onErrorMap(
            IllegalStateException.class, error -> new AmqpException(true, "Retries exhausted.", error, errorContext));
    }

    /**
     * Gets the partition id that this producer is publishing events to.
     *
     * @return The partition id that this producer is publishing events to.
     */
    String getPartitionId() {
        return partitionId;
    }

    /**
     * Gets the number of events in queue.
     *
     * @return the number of events in the queue.
     */
    int getBufferedEventCount() {
        // The number of events waiting to be pushed downstream to the EventDataAggregator.
        final int value = eventSink.scanOrDefault(Scannable.Attr.BUFFERED, 0);

        // The number of events in the current batch.
        final int currentBatch = eventDataAggregator.getNumberOfEvents();
        return value + currentBatch;
    }

    /**
     * Flushes all the events in the queue. Does not allow for any additional events to be enqueued as it is being
     * flushed.
     *
     * @return A Mono that completes when all events are flushed.
     */
    Mono<Void> flush() {
        return publishResultSubscriber.startFlush();
    }

    @Override
    public void close() {
        if (isClosed.getAndSet(true)) {
            return;
        }

        try {
            publishResultSubscriber.startFlush().block(retryOptions.getTryTimeout());
        } catch (IllegalStateException e) {
            logger.info("Timed out waiting for flush to complete.", e);
        } finally {
            publishSubscription.dispose();
            client.close();
        }
    }

    /**
     * Publishes {@link EventDataBatch} and returns the result.
     *
     * @return A stream of published results.
     */
    private Flux<PublishResult> publishEvents(Flux<EventDataBatch> upstream) {
        return upstream.flatMap(batch -> {
            if (batch == EventDataBatch.EMPTY) {
                return Mono.just(PublishResult.EMPTY);
            }
            return client.send(batch)
                .thenReturn(new PublishResult(batch.getEvents(), null))
                // Resuming on error because an error is a terminal signal, so we want to wrap that with a result,
                // so it doesn't stop publishing.
                .onErrorResume(error -> Mono.just(new PublishResult(batch.getEvents(), error)));
        }, 1, 1);
    }

    /**
     * Creates a new batch.
     *
     * @return A new EventDataBatch
     *
     * @throws UncheckedExecutionException If an exception occurred when trying to create a new batch.  It is
     *     possible when the thread is interrupted while creating the batch.
     */
    private EventDataBatch createNewBatch() {
        final Mono<EventDataBatch> batch = client.createBatch(createBatchOptions);
        try {
            return batch.toFuture().get();
        } catch (InterruptedException e) {
            throw logger.logExceptionAsError(new UncheckedExecutionException(e));
        } catch (ExecutionException e) {
            throw logger.logExceptionAsError(new UncheckedExecutionException(e));
        }
    }

    /**
     * Static class to hold results.
     */
    private static class PublishResult {
        private static final PublishResult EMPTY = new PublishResult();
        private final List<EventData> events;
        private final Throwable error;

        PublishResult(List<EventData> events, Throwable error) {
            this.events = Objects.requireNonNull(events);
            this.error = error;
        }

        private PublishResult() {
            this.events = null;
            this.error = null;
        }
    }

    /**
     * Subscribes to the results of a send {@link EventDataBatch} operation.
     */
    private static class PublishResultSubscriber extends BaseSubscriber<PublishResult> {
        private final String partitionId;
        private final Sinks.Many<EventData> eventSink;
        private final Consumer<SendBatchSucceededContext> onSucceed;
        private final Consumer<SendBatchFailedContext> onFailed;
        private final Duration operationTimeout;
        private final ClientLogger logger;

        private final AtomicReference<FlushSignal> pendingFlushSignal = new AtomicReference<>(null);
        private final Semaphore flushSemaphore = new Semaphore(1);
        private final AtomicBoolean terminated = new AtomicBoolean(false);

        PublishResultSubscriber(String partitionId, Sinks.Many<EventData> eventSink,
            Consumer<SendBatchSucceededContext> onSucceed, Consumer<SendBatchFailedContext> onFailed,
            Duration operationTimeout, ClientLogger logger) {
            this.partitionId = partitionId;
            this.eventSink = eventSink;
            this.onSucceed = onSucceed;
            this.onFailed = onFailed;
            this.operationTimeout = operationTimeout;
            this.logger = logger;
        }

        @Override
        protected void hookOnSubscribe(Subscription s) {
            requestUnbounded();
        }

        @Override
        protected void hookOnNext(PublishResult result) {
            if (result != PublishResult.EMPTY) {
                if (result.error == null) {
                    onSucceed.accept(new SendBatchSucceededContext(result.events, partitionId));
                } else {
                    onFailed.accept(new SendBatchFailedContext(result.events, partitionId, result.error));
                }
            }
            final FlushCompletionOrigin origin
                = (result == PublishResult.EMPTY) ? FlushCompletionOrigin.ON_NEXT_EMPTY : FlushCompletionOrigin.ON_NEXT;
            tryCompleteFlush(origin);
        }

        @Override
        protected void hookOnError(Throwable t) {
            if (terminated.getAndSet(true)) {
                return;
            }
            logger.atError().log("Publishing-subscription terminated with an error.", t);
            onFailed.accept(new SendBatchFailedContext(null, partitionId, t));
            tryCompleteFlush(FlushCompletionOrigin.TERMINAL_ERROR);
        }

        @Override
        protected void hookOnComplete() {
            if (terminated.getAndSet(true)) {
                return;
            }
            logger.atInfo().log("Publishing-subscription terminated.");
            tryCompleteFlush(FlushCompletionOrigin.TERMINAL_COMPLETION);
        }

        TimeoutException awaitPendingFlush() {
            if (pendingFlushSignal.get() == null) {
                return null;
            }
            final boolean acquired;
            try {
                acquired = flushSemaphore.tryAcquire(operationTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Unsure whether this is recoverable by trying again? Maybe, since this could be scheduled on
                // another thread.
                return new TimeoutException("Unable to acquire flush-semaphore due to interrupted exception.");
            }
            if (!acquired) {
                return new TimeoutException("Timed out waiting for flush operation to complete.");
            }
            // Release the permit obtained upon the successful flush await completion (later flush do not await for
            // any 'enqueueEvent`).
            flushSemaphore.release();
            return null;
        }

        /**
         * Flushes the queue. Releases semaphore when it is complete.
         *
         * @throws NullPointerException if {@code semaphore} or {@code sink} is null.
         */
        Mono<Void> startFlush() {
            return Mono.create(sink -> {
                if (terminated.get()) {
                    logger.atInfo().log("Nothing to flush as publishing-subscription is terminated.");
                    sink.success();
                    return;
                }

                final FlushSignal flushSignal = new FlushSignal(sink);
                if (!pendingFlushSignal.compareAndSet(null, flushSignal)) {
                    logger.atInfo().log("Another flush operation is already in progress.");
                    sink.success();
                    return;
                }

                final boolean acquired;
                try {
                    acquired = flushSemaphore.tryAcquire(operationTimeout.toMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    pendingFlushSignal.set(null);
                    logger.atWarning().log("Unable to acquire flush-semaphore.");
                    sink.error(e);
                    return;
                }

                if (!acquired) {
                    pendingFlushSignal.set(null);
                    sink.error(new TimeoutException("timout waiting for acquiring flush-semaphore."));
                    return;
                }

                logger.atVerbose()
                    .addKeyValue("signal-id", flushSignal.getId())
                    .addKeyValue("permits", flushSemaphore.availablePermits())
                    .log("Enqueuing flush.");
                final Sinks.EmitResult emitResult = eventSink.tryEmitNext(flushSignal);
                if (emitResult != Sinks.EmitResult.OK) {
                    pendingFlushSignal.set(null);
                    flushSemaphore.release();
                    sink.error(new RuntimeException(
                        "Unable to enqueue flush: id" + flushSignal.getId() + " (" + emitResult + ")"));
                    return;
                }
            });
        }

        private void tryCompleteFlush(FlushCompletionOrigin origin) {
            final FlushSignal flushSignal = pendingFlushSignal.getAndSet(null);
            if (flushSignal != null) {
                logger.atVerbose()
                    .addKeyValue("signal-id", flushSignal.getId())
                    .addKeyValue("permits", flushSemaphore.availablePermits())
                    .addKeyValue("completion-origin", origin)
                    .log("Completing flush.");
                flushSemaphore.release();
                flushSignal.flushed();
            }
        }

        private enum FlushCompletionOrigin {
            TERMINAL_COMPLETION,
            TERMINAL_ERROR,
            // indicate that by the time the flush signal was processed there was no batch or batch had no event.
            ON_NEXT_EMPTY,
            ON_NEXT,
        }
    }

}
