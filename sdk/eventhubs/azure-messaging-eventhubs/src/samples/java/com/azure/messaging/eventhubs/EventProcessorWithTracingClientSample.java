// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Sample code to demonstrate how to create per-message spans when processing batches with
 * {@link EventProcessorClient}.
 */
public class EventProcessorWithTracingClientSample {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessorWithTracingClientSample.class);
    private static final Tracer TRACER = initOpenTelemetry().getTracer("eventhubs-samples");
    private static final String AZURE_EVENT_HUBS_CONNECTION_STRING = System.getenv("AZURE_EVENT_HUBS_CONNECTION_STRING");

    // EventHubs SDKs uses https://www.w3.org/TR/trace-context/ to propagating context
    private static final W3CTraceContextPropagator PROPAGATOR = W3CTraceContextPropagator.getInstance();

    // Helper class to extract context from EventData
    private static final TextMapGetter<EventData> CONTEXT_GETTER = new TextMapGetter<EventData>() {
        @Override
        public Iterable<String> keys(EventData data) {
            return data.getProperties().keySet();
        }

        @Override
        public String get(EventData data, String key) {
            Object value = data.getProperties().get(key);
            return value instanceof String ? (String) value : null;
        }
    };

    @SuppressWarnings("try")
    private static void processBatch(EventBatchContext batchContext) {
        // here we have a Span.current() created by EventHubs SDK
        // it captures the duration/errors that happened when processing a batch of events
        // If you want to trace processing of each message, we'll need to create a new span for each message

        for (EventData eventData : batchContext.getEvents()) {
            // first, we extract the trace-context from the message
            Context remoteContext = PROPAGATOR.extract(Context.current(), eventData, CONTEXT_GETTER);

            // then we use it as a parent for the new span
            SpanBuilder spanBuilder = TRACER.spanBuilder("processMessage")
                .setParent(remoteContext);

            // Optional: if you want to preserve the relationship between span created
            // by EventHubs SDK for this batch and the new span created for each message,
            // you can set batch span as a link on a message span
            spanBuilder.addLink(Span.current().getSpanContext());

            Span span = spanBuilder.startSpan();
            // making span current enables correlation with any child spans created under it
            try (Scope s = span.makeCurrent()) {
                // we can also add custom attributes to the span
                span.setAttribute("myapp.custom.attribute", "custom value");
                // process message here
                Thread.sleep(100);
            } catch (Throwable t) {
                // if an error occurs, we can set span status to error
                span.setStatus(StatusCode.ERROR, t.getMessage());

                // we can also record the exception (note that exceptions are huge and may significantly
                // increase the volume of recorded telemetry)
                // it's a good practice to record only unhandled exceptions
                span.recordException(t);
                throw new RuntimeException(t);
            } finally {
                span.end();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Consumer<ErrorContext> processError = errorContext -> {
            LOGGER.error("Error while processing {}, {}, {}, {}", errorContext.getPartitionContext().getEventHubName(),
                    errorContext.getPartitionContext().getConsumerGroup(),
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable().getMessage());
        };

        EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(AZURE_EVENT_HUBS_CONNECTION_STRING)
            .initialPartitionEventPosition(p -> EventPosition.latest())
            .processEventBatch(EventProcessorWithTracingClientSample::processBatch, 1)
            .processError(processError)
            .checkpointStore(new SampleCheckpointStore());

        EventProcessorClient eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
        eventProcessorClient.start();

        // keep application alive.
        Thread.sleep(Duration.ofMinutes(1).toMillis());

        eventProcessorClient.stop();
    }

    private static OpenTelemetry initOpenTelemetry() {
        return OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                    .build())
            .buildAndRegisterGlobal();
    }
}
