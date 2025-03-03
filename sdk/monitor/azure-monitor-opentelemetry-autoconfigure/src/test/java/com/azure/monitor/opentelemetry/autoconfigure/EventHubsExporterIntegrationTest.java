// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.messaging.eventhubs.*;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
    public void producerTest() throws InterruptedException {
        CountDownLatch exporterCountDown = new CountDownLatch(2);
        String spanName = "event-hubs-producer-testing";
        HttpPipelinePolicy validationPolicy = (context, next) -> {
            Mono<byte[]> asyncBytes = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                .map(LocalStorageTelemetryPipelineListener::ungzip);
            asyncBytes.subscribe(value -> {
                List<TelemetryItem> telemetryItems = deserialize(value);
                for (TelemetryItem telemetryItem : telemetryItems) {
                    MonitorDomain monitorDomain = telemetryItem.getData().getBaseData();
                    RemoteDependencyData remoteDependencyData = toRemoteDependencyData(monitorDomain);
                    String remoteDependencyName = remoteDependencyData.getName();
                    if (remoteDependencyName.contains(spanName)) {
                        exporterCountDown.countDown();
                        System.out.println("Count down " + spanName);
                        LOGGER.info("Count down " + spanName);
                    } else if (remoteDependencyName.contains("EventHubs.send")) {
                        exporterCountDown.countDown();
                        LOGGER.info("Count down " + "EventHubs.send");
                    } else {
                        LOGGER.info("remoteDependencyName = " + remoteDependencyName);
                    }
                }
            });
            return next.process();
        };
        Tracer tracer = TestUtils.createOpenTelemetrySdk(getHttpPipeline(validationPolicy)).getTracer("Sample");
        EventHubProducerAsyncClient producer = new EventHubClientBuilder().credential(credential)
            .fullyQualifiedNamespace("namespace")
            .eventHubName("event-hub")
            .buildAsyncProducerClient();
        Span span = tracer.spanBuilder(spanName).startSpan();
        Scope scope = span.makeCurrent();
        try {
            producer.createBatch().flatMap(batch -> {
                batch.tryAdd(new EventData("test event"));
                return producer.send(batch);
            }).subscribe();
        } finally {
            span.end();
            scope.close();
        }
        assertTrue(exporterCountDown.await(60, TimeUnit.SECONDS));
    }

    // Copied from com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils.java
    // deserialize multiple TelemetryItem raw bytes with newline delimiters to a list of TelemetryItems
    private static List<TelemetryItem> deserialize(byte[] rawBytes) {
        try (JsonReader jsonReader = JsonProviders.createReader(rawBytes)) {
            JsonToken token = jsonReader.currentToken();
            if (token == null) {
                jsonReader.nextToken();
            }
            List<TelemetryItem> result = new ArrayList<>();
            do {
                result.add(TelemetryItem.fromJson(jsonReader));
            } while (jsonReader.nextToken() == JsonToken.START_OBJECT);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Copied from com.azure.monitor.opentelemetry.exporter.implementation.utils.TestUtils.java
    // azure-json doesn't deserialize subtypes yet, so need to convert the abstract MonitorDomain to RemoteDependencyData
    private static RemoteDependencyData toRemoteDependencyData(MonitorDomain baseData) {
        try (JsonReader jsonReader = JsonProviders.createReader(baseData.toJsonString())) {
            return RemoteDependencyData.fromJson(jsonReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        Tracer tracer = TestUtils.createOpenTelemetrySdk(getHttpPipeline(validationPolicy)).getTracer("Sample");

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
