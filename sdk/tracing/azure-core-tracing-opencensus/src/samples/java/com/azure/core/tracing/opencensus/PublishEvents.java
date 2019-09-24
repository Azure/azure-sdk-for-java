// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opencensus;

import com.azure.core.util.Context;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncProducer;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;
import reactor.core.publisher.Flux;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to send a message to an Azure Event Hub with tracing support.
 */
public class PublishEvents {
    private static final String CONNECTION_STRING = System.getenv("AZURE_EVENTHUBS_CONNECTION_STRING");
    /**
     * Main method to invoke this demo on how to send a message to an Azure Event Hub with trace spans exported to
     * zipkin.
     *
     * Please refer to the  <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a zipkin exporter.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        ZipkinTraceExporter.createAndRegister("http://localhost:9411/api/v2/spans", "tracing-to-zipkin-service");

        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

        Tracer tracer = Tracing.getTracer();


        Scope scope = tracer.spanBuilder("opencensus-span").startScopedSpan();
        try {
            // The connection string value can be obtained by:
            // 1. Going to your Event Hubs namespace in Azure Portal.
            // 2. Creating an Event Hub instance.
            // 3. Creating a "Shared access policy" for your Event Hub instance.
            // 4. Copying the connection string from the policy's properties.

            // Instantiate a client that will be used to call the service.
            EventHubAsyncClient client = new EventHubClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildAsyncClient();

            Context context = new Context("opencensus-span", tracer.getCurrentSpan());
            final int count = 2;
            final byte[] body = "Hello World!".getBytes(UTF_8);
            final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
                final EventData data = new EventData(body, context);
                return Flux.just(data);
            });

            // Create a producer. This overload of `createProducer` does not accept any arguments. Consequently, events
            // sent from this producer are load balanced between all available partitions in the Event Hub instance.
            EventHubAsyncProducer producer = client.createProducer();

            // Send those events. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
            // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
            // the event.
            producer.send(testData).block();
            try {
                producer.close();
            } catch (IOException e) {
                System.err.println("Error encountered while closing producer: " + e.toString());
            }

            client.close();
        } finally {
            scope.close();
        }

        Tracing.getExportComponent().shutdown();
    }
}
