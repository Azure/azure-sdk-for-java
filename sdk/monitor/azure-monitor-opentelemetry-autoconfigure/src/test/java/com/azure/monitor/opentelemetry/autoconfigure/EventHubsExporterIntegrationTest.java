// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.localstorage.LocalStorageTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.TestUtils;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@LiveOnly
public class EventHubsExporterIntegrationTest extends MonitorExporterClientTestBase {

    private static final String CONNECTION_STRING = System.getenv("AZURE_EVENTHUBS_CONNECTION_STRING");
    private static final String STORAGE_CONNECTION_STRING = System.getenv("STORAGE_CONNECTION_STRING");
    private static final String CONTAINER_NAME = System.getenv("STORAGE_CONTAINER_NAME");

    private static final ClientLogger LOGGER = new ClientLogger(EventHubsExporterIntegrationTest.class);

    private TokenCredential credential;

    @Override
    public void beforeTest() {
        super.beforeTest();
        credential = TokenCredentialUtil.getTestTokenCredential(interceptorManager);
    }

    @Test
    @SuppressWarnings("try")
    public void producerTest() throws InterruptedException {
        String ehNamespace = Configuration.getGlobalConfiguration().get("AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME");
        String ehName = Configuration.getGlobalConfiguration().get("AZURE_EVENTHUBS_EVENT_HUB_NAME");

        CountDownLatch exporterCountDown = new CountDownLatch(2);
        String spanName = "event-hubs-producer-testing";
        HttpPipelinePolicy validationPolicy = new HttpPipelinePolicy() {
            @Override
            public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                checkTelemetry(context);
                return next.process();
            }

            @Override
            public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
                checkTelemetry(context);
                return next.processSync();
            }

            private void checkTelemetry(HttpPipelineCallContext context) {
                byte[] asyncBytes = LocalStorageTelemetryPipelineListener
                    .ungzip(context.getHttpRequest().getBodyAsBinaryData().toBytes());
                List<TelemetryItem> telemetryItems = TestUtils.deserialize(asyncBytes);

                for (TelemetryItem telemetryItem : telemetryItems) {
                    MonitorDomain monitorDomain = telemetryItem.getData().getBaseData();
                    RemoteDependencyData remoteDependencyData = TestUtils.toRemoteDependencyData(monitorDomain);
                    String remoteDependencyName = remoteDependencyData.getName();
                    if (remoteDependencyName.contains(spanName)) {
                        exporterCountDown.countDown();
                        LOGGER.info("Count down " + spanName);
                    } else if (("send " + ehName).equals(remoteDependencyName)) {
                        exporterCountDown.countDown();
                        LOGGER.info("Count down eventHubs send");
                    } else {
                        LOGGER.info("remoteDependencyName = " + remoteDependencyName);
                    }
                }
            }
        };

        Optional<OpenTelemetrySdk> optionalOpenTelemetry
            = TestUtils.createOpenTelemetrySdk(getHttpPipeline(validationPolicy));
        if (!optionalOpenTelemetry.isPresent()) {
            return;
        }
        OpenTelemetrySdk otel = optionalOpenTelemetry.get();

        Tracer tracer = otel.getTracer("Sample");

        try (EventHubProducerAsyncClient producer = new EventHubClientBuilder().credential(credential)
            .fullyQualifiedNamespace(ehNamespace)
            .eventHubName(ehName)
            .clientOptions(
                new ClientOptions().setTracingOptions(new OpenTelemetryTracingOptions().setOpenTelemetry(otel)))
            .buildAsyncProducerClient()) {

            Span span = tracer.spanBuilder(spanName).startSpan();
            try (Scope scope = span.makeCurrent()) {
                StepVerifier.create(producer.createBatch().flatMap(batch -> {
                    batch.tryAdd(new EventData("test event"));
                    return producer.send(batch);
                })).expectComplete().verify(Duration.ofSeconds(60));
            } finally {
                span.end();
            }
        }
        assertTrue(exporterCountDown.await(20, TimeUnit.SECONDS));
    }

    @Disabled("Processor integration tests require separate consumer group to not have partition contention in CI - https://github.com/Azure/azure-sdk-for-java/issues/23567")
    @Test
    public void processorTest() throws InterruptedException {
        CountDownLatch exporterCountDown = new CountDownLatch(3);
        EventHubProducerAsyncClient producer
            = new EventHubClientBuilder().connectionString(CONNECTION_STRING).buildAsyncProducerClient();

        HttpPipelinePolicy validationPolicy = (context, next) -> {
            Mono<String> asyncString = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
            asyncString.subscribe(value -> {
                // user span
                if (value.contains("event-hubs-consumer-testing")) {
                    exporterCountDown.countDown();
                }
                // process span
                if (value.contains("EventHubs.process")) {
                    exporterCountDown.countDown();
                }
                // Storage call
                if (value.contains("AzureBlobStorageBlobs.setMetadata")) {
                    exporterCountDown.countDown();
                }
            });
            return next.process();
        };
        Optional<OpenTelemetrySdk> optionalOpenTelemetry
            = TestUtils.createOpenTelemetrySdk(getHttpPipeline(validationPolicy));
        if (!optionalOpenTelemetry.isPresent()) {
            return;
        }
        OpenTelemetrySdk openTelemetrySdk = optionalOpenTelemetry.get();
        Tracer tracer = openTelemetrySdk.getTracer("Sample");

        CountDownLatch partitionOwned = new CountDownLatch(1);
        CountDownLatch eventCountDown = new CountDownLatch(1);
        BlobContainerAsyncClient blobContainerAsyncClient
            = new BlobContainerClientBuilder().connectionString(STORAGE_CONNECTION_STRING)
                .containerName(CONTAINER_NAME)
                .buildAsyncClient();
        EventProcessorClient processorClient
            = new EventProcessorClientBuilder().consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .connectionString(CONNECTION_STRING)
                .processPartitionInitialization(partition -> {
                    if (partition.getPartitionContext().getPartitionId().equals("0")) {
                        partitionOwned.countDown();
                    }
                })
                .processEvent(event -> {
                    event.updateCheckpoint();
                    eventCountDown.countDown();
                })
                .processError(error -> {
                })
                .loadBalancingStrategy(LoadBalancingStrategy.GREEDY)
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
                .buildEventProcessorClient();

        Span span = tracer.spanBuilder("event-hubs-consumer-testing").startSpan();
        Scope scope = span.makeCurrent();
        try {
            processorClient.start();
        } finally {
            span.end();
            scope.close();
        }
        partitionOwned.await(10, TimeUnit.SECONDS);

        // send an event after partition 0 is owned
        producer.createBatch(new CreateBatchOptions().setPartitionId("0")).flatMap(batch -> {
            batch.tryAdd(new EventData("test event "));
            return producer.send(batch);
        }).block();

        assertTrue(eventCountDown.await(10, TimeUnit.SECONDS));
        assertTrue(exporterCountDown.await(10, TimeUnit.SECONDS));
    }
}
