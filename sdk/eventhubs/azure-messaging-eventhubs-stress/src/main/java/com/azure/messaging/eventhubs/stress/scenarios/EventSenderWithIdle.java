package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

import static com.azure.messaging.eventhubs.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.eventhubs.stress.util.TestUtils.createMessagePayload;
import static com.azure.messaging.eventhubs.stress.util.TestUtils.getBuilder;

/**
 * Verifies that after periods of idle-ness the {@link com.azure.messaging.eventhubs.EventHubProducerAsyncClient}
 * continues to publish events.
 */
@Component("EventSenderWithIdle")
public class EventSenderWithIdle extends EventHubsScenario {
    private final ClientLogger logger = new ClientLogger(EventSenderWithIdle.class);

    @Value("${BATCH_SIZE:15}")
    private int batchSize;

    @Value("${SEND_CONCURRENCY:1}")
    private int sendConcurrency;

    @Value("${PARTITION_KEY:#{null}}")
    private String partitionKey;

    private BinaryData messagePayload;

    @Override
    public void run() {
        messagePayload = createMessagePayload(options.getMessageSize());

        final CreateBatchOptions batchOptions;
        if (partitionKey != null) {
            batchOptions = new CreateBatchOptions().setPartitionKey(partitionKey);
        } else {
            batchOptions = new CreateBatchOptions();
        }

        toClose(Mono.just(1)
            .repeat()
            .flatMap(i -> {
                final Duration idleDuration = options.getIdleDuration();
                if (idleDuration.isZero()) {
                    return singleRun(batchOptions);
                } else {
                    return singleRun(batchOptions).then(Mono.delay(idleDuration));
                }
            }, sendConcurrency)
            .parallel(sendConcurrency, 1)
            .runOn(Schedulers.boundedElastic())
            .subscribe());

        blockingWait(options.getTestDuration().plusSeconds(1));
    }

    @Override
    public void recordRunOptions(Span span) {
        super.recordRunOptions(span);
        span.setAttribute(AttributeKey.longKey("sendConcurrency"), sendConcurrency);
        span.setAttribute(AttributeKey.longKey("batchSize"), batchSize);
        span.setAttribute(AttributeKey.longKey("idleDuration"), options.getIdleDuration().toMinutes());
        span.setAttribute(AttributeKey.longKey("testDuration"), options.getTestDuration().toMinutes());
    }

    private Mono<Void> singleRun(CreateBatchOptions createBatchOptions) {
        return Mono.using(
            () -> getBuilder(options).buildAsyncProducerClient(),
            client -> singleIteration(client, createBatchOptions),
            EventHubProducerAsyncClient::close);
    }

    private Mono<Void> singleIteration(EventHubProducerAsyncClient client, CreateBatchOptions createBatchOptions) {
        final String partitionId = createBatchOptions.getPartitionId();

        return client.createBatch(createBatchOptions)
            .map(batch -> {
                for (int i = 0; i < batchSize; i++) {
                    final EventData eventData = new EventData(messagePayload);

                    if (!batch.tryAdd(eventData)) {
                        telemetryHelper.recordError("batch is full", "createBatch", partitionId);

                        logger.atError()
                            .addKeyValue("numberOfEvents", batch.getCount())
                            .addKeyValue("size", batch.getSizeInBytes())
                            .addKeyValue("maxSize", batch.getMaxSizeInBytes())
                            .addKeyValue("body", eventData.getBodyAsString())
                            .log("Could not add event.");

                        break;
                    }
                }

                return batch;
            })
            .flatMap(batch -> client.send(batch))
            .onErrorResume(e -> {
                telemetryHelper.recordError(e, "Create and send batch", partitionId);
                return Mono.empty();
            })
            .doOnCancel(() -> telemetryHelper.recordError("Cancelled", "create and send batch",
                partitionId));
    }
}
