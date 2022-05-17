// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.MetricsOptions;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.perf.test.core.PerfStressProgram;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the Event Hubs performance tests.
 */
public class App {
    /**
     * Starts running a performance test.
     *
     * @param args Unused command line arguments.
     * @throws RuntimeException If not able to load test classes.
     */
    public static void main(String[] args) throws InterruptedException {

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
            .setResource(Resource.builder().put("service.name", "demoapp").build())
            .registerMetricReader(PrometheusHttpServer.builder().setHost("localhost").setPort(9464).build())
            .build();
        OpenTelemetry sdk = OpenTelemetrySdk.builder()
            .setMeterProvider(sdkMeterProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();

        /*EventHubProducerClient client = new EventHubClientBuilder()
            .connectionString("Endpoint=sb://azmondemo.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=weJgpc2LQxVNny4nh0M8VMwXy9MfItm2x5XRv5S3J1U=", "testme")
            .clientOptions(new ClientOptions().setMetricsOptions(new MetricsOptions().enable(false)))
            .buildProducerClient();
        List<EventData> events = new ArrayList<>();
        for (int i = 0; i < 10; i ++) {
            events.add(new EventData("hello"));
        }

        for (int i = 0; i < 100; i ++) {
            client.send(events);
            Thread.sleep(1);
        }

        Instant start = Instant.now();
        long sentBatches = 0;
        while (Duration.between(start,  Instant.now()).toMillis() < 50000) {
            client.send(events);
            sentBatches ++;
            Thread.sleep(1);
        }
        Instant end = Instant.now();

        System.out.println(sentBatches);
        System.out.println(((double)sentBatches) / Duration.between(start, end).toMillis());
        System.out.println(sentBatches / Duration.between(start, end).toSeconds());*/

        final Class<?>[] testClasses = new Class<?>[]{
            ReceiveEventsTest.class,
            SendEventDataTest.class,
            SendEventDataBatchTest.class,
            EventProcessorTest.class,
            GetPartitionInformationTest.class,
            ReactorReceiveEventsTest.class
        };

        PerfStressProgram.run(testClasses, args);
    }
}
