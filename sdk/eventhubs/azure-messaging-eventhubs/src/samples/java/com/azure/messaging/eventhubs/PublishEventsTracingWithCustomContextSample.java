// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

/**
 * Demonstrates how to use OpenTelemtery to trace EventHubs calls and set trace context manually
 * on {@link EventData}. Note that in most cases (when you use Reactor or write synchronous code)
 * setting context manually should not be necessary.
 */
public class PublishEventsTracingWithCustomContextSample {

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
     * Configure the OpenTelemetry {@link SampleTraceExporter} to enable tracing.
     *
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static Tracer configureJaegerExporter() {
        // configure exporter to your tracing backend instead.
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(new SampleTraceExporter()))
                    .build())
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

        // BEGIN: sample-trace-context-manual-propagation
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
        // END: sample-trace-context-manual-propagation
        producer.close();
    }

    private static class SampleTraceExporter implements SpanExporter {
        @Override
        public CompletableResultCode export(Collection<SpanData> collection) {
            collection.stream().forEach(System.out::println);
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode flush() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }
    }
}
