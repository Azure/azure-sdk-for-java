// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient.BufferedProducerClientOptions;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;
import reactor.util.retry.Retry;

import java.io.Closeable;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Keeps track of publishing events to a partition.
 */
class EventHubBufferedPartitionProducer implements Closeable {
    private final ClientLogger logger;
    private final EventHubProducerAsyncClient client;
    private final String partitionId;
    private final BufferedProducerClientOptions options;
    private final AmqpErrorContext errorContext;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Mono<Integer> linkSizeMono;
    private final Disposable publishSubscription;
    private final Sinks.Many<EventData> eventSink;

    EventHubBufferedPartitionProducer(EventHubProducerAsyncClient client, String partitionId,
        BufferedProducerClientOptions options) {
        this.client = client;
        this.partitionId = partitionId;
        this.options = options;
        this.errorContext = new AmqpErrorContext(client.getFullyQualifiedNamespace());
        this.linkSizeMono = client.createBatch().map(batch -> batch.getMaxSizeInBytes())
            .publishOn(Schedulers.boundedElastic())
            .cache(value -> Duration.ofMillis(Long.MAX_VALUE),
                error -> {
                return Duration.ZERO;
            }, () -> Duration.ZERO);

        this.logger = new ClientLogger(EventHubBufferedPartitionProducer.class + "-" + partitionId);

        final Supplier<Queue<EventData>> queueSupplier = Queues.get(options.getMaxEventBufferLengthPerPartition());
        this.eventSink = Sinks.many().unicast().onBackpressureBuffer(queueSupplier.get());


        final EventDataAggregator eventDataBatchFlux = new EventDataAggregator(eventSink.asFlux(),
            () -> client.createBatch().subscribeOn(Schedulers.boundedElastic()).block(),
            client.getFullyQualifiedNamespace(), options);

        this.publishSubscription = publishEvents(eventDataBatchFlux)
            .publishOn(Schedulers.boundedElastic())
            .subscribe(result -> {
                if (result.error == null) {
                    options.getSendSucceededContext().accept(new SendBatchSucceededContext(result.batch.getEvents(),
                        partitionId));
                } else {
                    options.getSendFailedContext().accept(new SendBatchFailedContext(result.batch.getEvents(),
                        partitionId, result.error));
                }
            }, error -> {
                logger.error("Publishing subscription completed and ended in an error.", error);
                options.getSendFailedContext().accept(new SendBatchFailedContext(null, partitionId, error));
            },
            () -> {
                logger.info("Publishing subscription completed.");
            });

    }

    Mono<Void> enqueueEvent(EventData eventData) {
        return Mono.create(sink -> {
            sink.onRequest(request -> {
                try {
                    eventSink.emitNext(eventData, (signalType, emitResult) -> {
                        // If the draining queue is slower than the publishing queue.
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
     * Publishes {@link EventDataBatch} and returns the result.
     *
     * @return A stream of published results.
     */
    Flux<PublishResult> publishEvents(Flux<EventDataBatch> upstream) {
        return Flux.defer(() -> {
            return upstream.flatMap(batch -> {
                return client.send(batch).thenReturn(new PublishResult(batch, null))
                    // Resuming on error because an error is a terminal signal, so we want to wrap that with a result,
                    // so it doesn't stop publishing.
                    .onErrorResume(error -> Mono.just(new PublishResult(null, error)));
            });
        }).retryWhen(Retry.from(signal -> {
            if (isClosed.get()) {
                return Mono.empty();
            } else {
                return Mono.just(true);
            }
        }));
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
     * Static class to hold results.
     */
    private static final class PublishResult {
        private final EventDataBatch batch;
        private final Throwable error;

        PublishResult(EventDataBatch batch, Throwable error) {
            this.batch = batch;
            this.error = error;
        }
    }
}
