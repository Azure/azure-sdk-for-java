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
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.azure.core.amqp.implementation.RetryUtil.withRetry;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.EMIT_RESULT_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;

/**
 * Keeps track of publishing events to a partition.
 */
class EventHubBufferedPartitionProducer implements Closeable {
    private static final ClientLogger LOGGER = new ClientLogger(EventHubBufferedPartitionProducer.class);

    private final AmqpRetryOptions retryOptions;
    private final EventHubProducerAsyncClient client;
    private final String partitionId;
    private final AmqpErrorContext errorContext;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Disposable publishSubscription;
    private final Sinks.Many<EventData> eventSink;
    private final CreateBatchOptions createBatchOptions;
    private final Queue<EventData> eventQueue;
    private final AtomicBoolean isFlushing = new AtomicBoolean(false);
    private final Semaphore flushSemaphore = new Semaphore(1);
    private final PublishResultSubscriber publishResultSubscriber;
    private final EventHubsTracer tracer;

    EventHubBufferedPartitionProducer(EventHubProducerAsyncClient client, String partitionId,
        BufferedProducerClientOptions options, AmqpRetryOptions retryOptions, Sinks.Many<EventData> eventSink,
        Queue<EventData> eventQueue, Tracer tracer) {

        this.client = client;
        this.partitionId = partitionId;
        this.errorContext = new AmqpErrorContext(client.getFullyQualifiedNamespace());
        this.createBatchOptions = new CreateBatchOptions().setPartitionId(partitionId);
        this.retryOptions = retryOptions;
        this.eventSink = eventSink;
        this.eventQueue = eventQueue;

        final Flux<EventDataBatch> eventDataBatchFlux = new EventDataAggregator(eventSink.asFlux(),
            this::createNewBatch, client.getFullyQualifiedNamespace(), options, partitionId);

        this.publishResultSubscriber = new PublishResultSubscriber(partitionId,
            options.getSendSucceededContext(), options.getSendFailedContext(), eventQueue, flushSemaphore, isFlushing,
            retryOptions.getTryTimeout(), LOGGER);

        this.publishSubscription = publishEvents(eventDataBatchFlux)
            .publishOn(Schedulers.boundedElastic(), 1)
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
                sink.error(new IllegalStateException(String.format(
                    "Partition publisher id[%s] is already closed. Cannot enqueue more events.", partitionId)));
                return;
            }

            try {
                if (isFlushing.get()
                    && !flushSemaphore.tryAcquire(retryOptions.getTryTimeout().toMillis(), TimeUnit.MILLISECONDS)) {

                    sink.error(new TimeoutException("Timed out waiting for flush operation to complete."));
                    return;
                }
            } catch (InterruptedException e) {
                // Unsure whether this is recoverable by trying again? Maybe, since this could be scheduled on
                // another thread.
                sink.error(new TimeoutException("Unable to acquire flush semaphore due to interrupted exception."));
                return;
            }

            if (isClosed.get()) {
                sink.error(new IllegalStateException(String.format("Partition publisher id[%s] was "
                    + "closed between flushing events and now. Cannot enqueue events.", partitionId)));
                return;
            }

            tracer.reportMessageSpan(eventData, eventData.getContext());
            final Sinks.EmitResult emitResult = eventSink.tryEmitNext(eventData);
            if (emitResult.isSuccess()) {
                sink.success();
                return;
            }

            if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED || emitResult == Sinks.EmitResult.FAIL_OVERFLOW) {
                // If the draining queue is slower than the publishing queue.
                LOGGER.atInfo()
                    .addKeyValue(PARTITION_ID_KEY, partitionId)
                    .addKeyValue(EMIT_RESULT_KEY, emitResult)
                    .log("Event could not be published downstream. Applying retry.");

                sink.error(new AmqpException(true, emitResult + " occurred.", errorContext));
            } else {
                LOGGER.atWarning().addKeyValue(EMIT_RESULT_KEY, emitResult)
                    .log("Event could not be published downstream. Not retrying.", emitResult);

                sink.error(new AmqpException(false, "Unable to buffer message for partition: " + getPartitionId(),
                    errorContext));
            }
        });

        return withRetry(enqueueOperation, retryOptions, "Timed out trying to enqueue event data.", true)
            .onErrorMap(IllegalStateException.class, error -> new AmqpException(true, "Retries exhausted.", error, errorContext));
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
        return eventQueue.size();
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
            LOGGER.info("Timed out waiting for flush to complete.", e);
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
            return client.send(batch).thenReturn(new PublishResult(batch, null))
                // Resuming on error because an error is a terminal signal, so we want to wrap that with a result,
                // so it doesn't stop publishing.
                .onErrorResume(error -> Mono.just(new PublishResult(batch, error)));
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
            throw LOGGER.logExceptionAsError(new UncheckedExecutionException(e));
        } catch (ExecutionException e) {
            throw LOGGER.logExceptionAsError(new UncheckedExecutionException(e));
        }
    }

    /**
     * Static class to hold results.
     */
    private static class PublishResult {
        private final EventDataBatch batch;
        private final Throwable error;

        PublishResult(EventDataBatch batch, Throwable error) {
            this.batch = batch;
            this.error = error;
        }
    }

    private static class PublishResultSubscriber extends BaseSubscriber<PublishResult> {
        private final String partitionId;
        private final Consumer<SendBatchSucceededContext> onSucceed;
        private final Consumer<SendBatchFailedContext> onFailed;
        private final Queue<EventData> dataQueue;
        private final Duration operationTimeout;
        private final ClientLogger logger;

        private final AtomicBoolean isFlushing;
        private final Semaphore flushSemaphore;
        private MonoSink<Void> flushSink;

        PublishResultSubscriber(String partitionId, Consumer<SendBatchSucceededContext> onSucceed,
            Consumer<SendBatchFailedContext> onFailed, Queue<EventData> dataQueue, Semaphore flushSemaphore,
            AtomicBoolean flush, Duration operationTimeout, ClientLogger logger) {
            this.partitionId = partitionId;
            this.onSucceed = onSucceed;
            this.onFailed = onFailed;
            this.dataQueue = dataQueue;
            this.flushSemaphore = flushSemaphore;
            this.isFlushing = flush;
            this.operationTimeout = operationTimeout;
            this.logger = logger;
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            requestUnbounded();
        }

        @Override
        protected void hookOnNext(PublishResult result) {
            if (result.error == null) {
                onSucceed.accept(new SendBatchSucceededContext(result.batch.getEvents(), partitionId));
            } else {
                onFailed.accept(new SendBatchFailedContext(result.batch.getEvents(), partitionId, result.error));
            }

            tryCompleteFlush();
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            logger.atError()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .log("Publishing subscription completed and ended in an error.", throwable);

            onFailed.accept(new SendBatchFailedContext(null, partitionId, throwable));

            tryCompleteFlush();
        }

        @Override
        protected void hookOnComplete() {
            logger.atInfo()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .log("Publishing subscription completed. Clearing rest of queue.");

            final List<EventData> events = new ArrayList<>(this.dataQueue);
            this.dataQueue.clear();

            onFailed.accept(new SendBatchFailedContext(events, partitionId, null));

            tryCompleteFlush();
        }

        /**
         * Flushes the queue. Releases semaphore when it is complete.
         *
         * @throws NullPointerException if {@code semaphore} or {@code sink} is null.
         */
        Mono<Void> startFlush() {
            return Mono.create(sink -> {
                if (!isFlushing.compareAndSet(false, true)) {
                    logger.atInfo()
                        .addKeyValue(PARTITION_ID_KEY, partitionId)
                        .log("Flush operation already in progress.");
                    sink.success();
                    return;
                }

                this.flushSink = sink;
                try {
                    if (!flushSemaphore.tryAcquire(operationTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                        sink.error(new TimeoutException("Unable to acquire flush semaphore to begin timeout operation."));
                    }

                    tryCompleteFlush();
                } catch (InterruptedException e) {
                    logger.atWarning()
                        .addKeyValue(PARTITION_ID_KEY, partitionId)
                        .log("Unable to acquire flush semaphore.");

                    sink.error(e);
                }
            });
        }

        /**
         * Checks whether data queue is empty, if it is, completes the flush.
         */
        private void tryCompleteFlush() {
            if (!isFlushing.get()) {
                return;
            }

            if (!dataQueue.isEmpty()) {
                logger.atVerbose()
                    .addKeyValue(PARTITION_ID_KEY, partitionId)
                    .log("Data queue is not empty. Not completing flush.");
                return;
            }

            logger.atVerbose()
                .addKeyValue(PARTITION_ID_KEY, partitionId)
                .log("Completing flush operation.");

            if (flushSemaphore != null) {
                flushSemaphore.release();
            }

            isFlushing.compareAndSet(true, false);
            flushSink.success();
        }
    }

}
