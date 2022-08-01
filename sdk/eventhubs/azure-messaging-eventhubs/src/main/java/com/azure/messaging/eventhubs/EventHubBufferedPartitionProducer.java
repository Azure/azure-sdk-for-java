// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Keeps track of publishing events to a partition.
 */
class EventHubBufferedPartitionProducer implements Closeable {
    private final ClientLogger logger;
    private final EventHubProducerAsyncClient client;
    private final String partitionId;
    private final AmqpErrorContext errorContext;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Disposable publishSubscription;
    private final Sinks.Many<EventData> eventSink;
    private final CreateBatchOptions createBatchOptions;
    private final Queue<EventData> eventQueue;

    EventHubBufferedPartitionProducer(EventHubProducerAsyncClient client, String partitionId,
        BufferedProducerClientOptions options) {
        this.client = client;
        this.partitionId = partitionId;
        this.errorContext = new AmqpErrorContext(client.getFullyQualifiedNamespace());
        this.createBatchOptions = new CreateBatchOptions().setPartitionId(partitionId);

        this.logger = new ClientLogger(EventHubBufferedPartitionProducer.class + "-" + partitionId);

        final Supplier<Queue<EventData>> queueSupplier = Queues.get(options.getMaxEventBufferLengthPerPartition());
        this.eventQueue = queueSupplier.get();
        this.eventSink = Sinks.many().unicast().onBackpressureBuffer(eventQueue);

        final Flux<EventDataBatch> eventDataBatchFlux = new EventDataAggregator(eventSink.asFlux(),
            this::createNewBatch, client.getFullyQualifiedNamespace(), options, partitionId);

        final PublishResultSubscriber publishResultSubscriber = new PublishResultSubscriber(partitionId,
            options.getSendSucceededContext(), options.getSendFailedContext(), eventQueue, logger);

        this.publishSubscription = publishEvents(eventDataBatchFlux)
            .publishOn(Schedulers.boundedElastic(), 1)
            .subscribeWith(publishResultSubscriber);
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
        return Mono.create(sink -> {
            sink.onRequest(request -> {
                if (isClosed.get()) {
                    sink.error(new IllegalStateException(String.format(
                        "Partition publisher id[%s] is already closed. Cannot enqueue more events.", partitionId)));
                    return;
                }

                try {
                    eventSink.emitNext(eventData, (signalType, emitResult) -> {
                        // If the draining queue is slower than the publishing queue.
                        System.err.printf("[%s] Could not push event downstream. %s.", partitionId, signalType);
                        return emitResult == Sinks.EmitResult.FAIL_OVERFLOW;
                    });
                    sink.success();
                } catch (Exception e) {
                    sink.error(new AmqpException(false, "Unable to buffer message for partition: " + getPartitionId(), errorContext));
                }
            });
        });
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
        return Mono.empty();
    }

    @Override
    public void close() {
        if (isClosed.getAndSet(true)) {
            return;
        }

        publishSubscription.dispose();
        client.close();
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
     * @throws UncheckedInterruptedException If an exception occurred when trying to create a new batch.  It is
     *     possible when the thread is interrupted while creating the batch.
     */
    private EventDataBatch createNewBatch() {
        final Mono<EventDataBatch> batch = client.createBatch(createBatchOptions);
        try {
            return batch.toFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw logger.logThrowableAsError(new UncheckedInterruptedException(e));
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
        private final ClientLogger logger;

        PublishResultSubscriber(String partitionId, Consumer<SendBatchSucceededContext> onSucceed,
            Consumer<SendBatchFailedContext> onFailed, Queue<EventData> dataQueue, ClientLogger logger) {
            this.partitionId = partitionId;
            this.onSucceed = onSucceed;
            this.onFailed = onFailed;
            this.dataQueue = dataQueue;
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
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            logger.error("Publishing subscription completed and ended in an error.", throwable);
            onFailed.accept(new SendBatchFailedContext(null, partitionId, throwable));
        }

        @Override
        protected void hookOnComplete() {
            logger.info("Publishing subscription completed. Clearing rest of queue.");

            final List<EventData> events = new ArrayList<>(this.dataQueue);
            this.dataQueue.clear();

            onFailed.accept(new SendBatchFailedContext(events, partitionId, null));
        }
    }

    static class UncheckedInterruptedException extends RuntimeException {
        UncheckedInterruptedException(Throwable error) {
            super("Unable to fetch batch.", error);
        }
    }
}
