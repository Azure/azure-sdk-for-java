// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

/**
 * Sample to demonstrate using {@link JaegerGrpcSpanExporter} to export telemetry events when publishing multiple events
 * to an eventhub instance using the {@link EventHubProducerAsyncClient}.
 */
public class PublishEventsJaegerExporterSample {

    private static final Tracer TRACER = configureJaegerExporter();
    private static final String CONNECTION_STRING = "<YOUR_CONNECTION_STRING>";

    /**
     * The main method to run the application.
     *
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        doClientWork();
    }

    /**
     * Configure the OpenTelemetry {@link JaegerGrpcSpanExporter} to enable tracing.
     *
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static Tracer configureJaegerExporter() {
        // Export traces to Jaeger
        JaegerGrpcSpanExporter jaegerExporter =
            JaegerGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:14250")
                .setTimeout(Duration.ofMinutes(30000))
                .build();

        // Set to process the spans by the Jaeger Exporter
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter)).build())
            .build();
        return openTelemetry.getSdkTracerProvider().get("Publish-Events-Eventhub-Sample");
    }

    /**
     * Send an iterable of events to specific event hub using the
     * {@link EventHubProducerClient} with distributed tracing enabled and using the Jaeger exporter to export
     * telemetry events.
     */
    private static void doClientWork() {
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(CONNECTION_STRING, "<eventHub Name>")
            .buildAsyncProducerClient();

        // BEGIN: readme-sample-context-manual-propagation-amqp
        Flux<EventData> events = Flux.just(
            new EventData("EventData Sample 1"),
            new EventData("EventData Sample 2"));

        // Create a batch to send the events.
        final AtomicReference<EventDataBatch> batchRef = new AtomicReference<>(
            producer.createBatch().block());

        final AtomicReference<io.opentelemetry.context.Context> traceContextRef = new AtomicReference<>(io.opentelemetry.context.Context.current());

        // when using async clients and instrumenting without ApplicationInsights or OpenTelemetry agent, context needs to be propagated manually
        // you would also want to propagate it manually when not making spans current.
        // we'll propagate context to events (to propagate it over to consumer)
        events.collect(batchRef::get, (b, e) ->
                b.tryAdd(e.addContext(PARENT_TRACE_CONTEXT_KEY, traceContextRef.get())))
            .flatMap(b -> producer.send(b))
            .doFinally(i -> Span.fromContext(traceContextRef.get()).end())
            .contextWrite(ctx -> {
                // this block is executed first, we'll create an outer span, which usually represents incoming request
                // or some logical operation
                Span span = TRACER.spanBuilder("my-span").startSpan();

                // and pass the new context with span to reactor for EventHubs producer client to pick it up.
                return ctx.put(PARENT_TRACE_CONTEXT_KEY, traceContextRef.updateAndGet(traceContext -> traceContext.with(span)));
            })
            .block();
        // END: readme-sample-context-manual-propagation-amqp
        producer.close();
    }
}
