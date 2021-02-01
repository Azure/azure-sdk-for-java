package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.core.util.FluxUtil;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
public class EventHubsExporterIntegrationTest extends AzureMonitorExporterTestBase {

    private static final String CONNECTION_STRING = "{connection-string}";

    @Test
    public void producerTest() throws InterruptedException {
        CountDownLatch exporterCountDown = new CountDownLatch(2);
        Tracer tracer = configureAzureMonitorExporter((context, next) -> {
            Mono<String> asyncString = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
            asyncString.subscribe(value -> {
                if (value.contains("event-hubs-producer-testing")) {
                    exporterCountDown.countDown();
                }
                if (value.contains("EventHubs.send")) {
                    exporterCountDown.countDown();
                }
            });
            return next.process();
        });
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildAsyncProducerClient();
        Span span = tracer.spanBuilder("event-hubs-producer-testing").startSpan();
        final Scope scope = span.makeCurrent();
        try {
            producer.createBatch()
                .flatMap(batch -> {
                    batch.tryAdd(new EventData("test event "));
                    return producer.send(batch);
                }).subscribe();
        } finally {
            span.end();
            scope.close();
        }
        assertTrue(exporterCountDown.await(5, TimeUnit.SECONDS));
    }


    @Test
    public void processorTest() throws InterruptedException {
        CountDownLatch exporterCountDown = new CountDownLatch(2);
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildAsyncProducerClient();

        Tracer tracer = configureAzureMonitorExporter((context, next) -> {
            Mono<String> asyncString = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
            asyncString.subscribe(value -> {
                if (value.contains("event-hubs-consumer-testing")) {
                    exporterCountDown.countDown();
                }
                if (value.contains("EventHubs.process")) {
                    exporterCountDown.countDown();
                }
            });
            return next.process();
        });

        CountDownLatch partitionOwned = new CountDownLatch(1);
        CountDownLatch eventCountDown = new CountDownLatch(1);
        EventProcessorClient processorClient = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(CONNECTION_STRING)
            .processPartitionInitialization(partition -> {
                if (partition.getPartitionContext().getPartitionId().equals("0")) {
                    partitionOwned.countDown();
                }
            })
            .processEvent(event -> eventCountDown.countDown())
            .processError(error -> { })
            .loadBalancingStrategy(LoadBalancingStrategy.GREEDY)
            .checkpointStore(new InMemoryCheckpointStore())
            .buildEventProcessorClient();

        Span span = tracer.spanBuilder("event-hubs-consumer-testing").startSpan();
        final Scope scope = span.makeCurrent();
        try {
            processorClient.start();
        } finally {
            span.end();
            scope.close();
        }
        partitionOwned.await(10, TimeUnit.SECONDS);

        // send an event after partition 0 is owned
        producer.createBatch(new CreateBatchOptions().setPartitionId("0"))
            .flatMap(batch -> {
                batch.tryAdd(new EventData("test event "));
                return producer.send(batch);
            }).block();

        assertTrue(eventCountDown.await(10, TimeUnit.SECONDS));
        assertTrue(exporterCountDown.await(10, TimeUnit.SECONDS));
    }
}
