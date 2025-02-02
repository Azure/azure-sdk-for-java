// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpInstrumentationOptions;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.pipeline.HttpInstrumentationPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.LongCounter;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * THESE CODE SNIPPETS ARE INTENDED FOR CLIENT LIBRARY DEVELOPERS ONLY.
 * <p>
 *
 * Application developers are expected to use OpenTelemetry API directly.
 * Check out {@code TelemetryJavaDocCodeSnippets} for application-level samples.
 */
public class TelemetryForLibraryDevelopersJavaDocCodeSnippets {
    private static final LibraryInstrumentationOptions LIBRARY_OPTIONS = new LibraryInstrumentationOptions("sample")
        .setLibraryVersion("1.0.0")
        .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");
    private static final HttpHeaderName CUSTOM_REQUEST_ID = HttpHeaderName.fromString("custom-request-id");

    public void createTracer() {

        // BEGIN: io.clientcore.core.instrumentation.createtracer

        LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions("sample")
            .setLibraryVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, libraryOptions);

        Tracer tracer = instrumentation.createTracer();

        // END: io.clientcore.core.instrumentation.createtracer
    }

    public void createMeter() {
        // BEGIN: io.clientcore.core.instrumentation.createmeter

        LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions("sample")
            .setLibraryVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, libraryOptions);
        instrumentation.createMeter();

        // END: io.clientcore.core.instrumentation.createmeter
    }

    public void histogram() {
        LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions("sample")
            .setLibraryVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, libraryOptions);
        Meter meter = instrumentation.createMeter();

        // BEGIN: io.clientcore.core.instrumentation.histogram

        List<Double> bucketBoundariesAdvice = Arrays.asList(0.005d, 0.01d, 0.025d, 0.05d, 0.075d,
            0.1d, 0.25d, 0.5d, 0.75d, 1d, 2.5d, 5d, 7.5d, 10d);
        DoubleHistogram histogram = meter.createDoubleHistogram("contoso.sample.client.operation.duration",
            "s",
            "Contoso sample client operation duration", bucketBoundariesAdvice);
        InstrumentationAttributes successAttributes  = instrumentation.createAttributes(
            Collections.singletonMap("operation.name", "{operationName}"));

        long startTime = System.nanoTime();
        String errorType = null;

        try {
            performOperation();
        } catch (Throwable t) {
            // make sure to report any exceptions including unchecked ones.
            errorType = getCause(t).getClass().getCanonicalName();
            throw t;
        } finally {
            InstrumentationAttributes attributes = errorType == null
                ? successAttributes
                : successAttributes.put("error.type", errorType);

            histogram.record((System.nanoTime() - startTime) / 1e9, attributes, null);
        }

        // END: io.clientcore.core.instrumentation.histogram
    }

    public void counter() {
        LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions("sample")
            .setLibraryVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, libraryOptions);
        Meter meter = instrumentation.createMeter();

        List<Object> batch = new ArrayList<>();

        // BEGIN: io.clientcore.core.instrumentation.counter
        LongCounter counter = meter.createLongCounter("sample.client.sent.messages",
            "Number of messages sent by the client library",
            "{message}");
        InstrumentationAttributes successAttributes  = instrumentation.createAttributes(
            Collections.singletonMap("operation.name", "sendBatch"));
        String errorType = null;
        try {
            sendBatch(batch);
        } catch (Throwable t) {
            // make sure to report any exceptions including unchecked ones.
            errorType = getCause(t).getClass().getCanonicalName();
            throw t;
        } finally {
            InstrumentationAttributes attributes = errorType == null
                ? successAttributes
                : successAttributes.put("error.type", errorType);

            counter.add(batch.size(), attributes, null);
        }

        // END: io.clientcore.core.instrumentation.counter
    }

    public void upDownCounter() {
        LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions("sample")
            .setLibraryVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, libraryOptions);
        Meter meter = instrumentation.createMeter();

        // BEGIN: io.clientcore.core.instrumentation.updowncounter
        LongCounter upDownCounter = meter.createLongUpDownCounter("sample.client.operation.active",
            "Number of operations in progress",
            "{operation}");
        InstrumentationAttributes successAttributes  = instrumentation.createAttributes(
            Collections.singletonMap("operation.name", "sendBatch"));
        try {
            upDownCounter.add(1, successAttributes, null);
            performOperation();
        } finally {
            upDownCounter.add(-1, successAttributes, null);
        }

        // END: io.clientcore.core.instrumentation.updowncounter
    }

    public void createAttributes() {
        // BEGIN: io.clientcore.core.instrumentation.createattributes

        LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions("sample")
            .setLibraryVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, libraryOptions);
        InstrumentationAttributes attributes = instrumentation
            .createAttributes(Collections.singletonMap("key1", "value1"));

        // END: io.clientcore.core.instrumentation.createattributes
    }

    /**
     * This example shows minimal distributed tracing instrumentation.
     */
    @SuppressWarnings("try")
    public void traceCall() {

        Tracer tracer = Instrumentation.create(null, LIBRARY_OPTIONS).createTracer();
        RequestOptions requestOptions = null;

        // BEGIN: io.clientcore.core.instrumentation.tracecall

        InstrumentationContext context = requestOptions == null ? null : requestOptions.getInstrumentationContext();
        Span span = tracer.spanBuilder("{operationName}", SpanKind.CLIENT, context)
            .startSpan();

        // we'll propagate context implicitly using span.makeCurrent() as shown later.
        // Libraries that write async code should propagate context explicitly in addition to implicit propagation.
        if (tracer.isEnabled()) {
            if (requestOptions == null) {
                requestOptions = new RequestOptions();
            }
            requestOptions.setInstrumentationContext(span.getInstrumentationContext());
        }

        try (TracingScope scope = span.makeCurrent()) {
            clientCall(requestOptions);
        } catch (Throwable t) {
            // make sure to report any exceptions including unchecked ones.
            span.end(getCause(t));
            throw t;
        } finally {
            // NOTE: closing the scope does not end the span, span should be ended explicitly.
            span.end();
        }

        // END:  io.clientcore.core.instrumentation.tracecall
    }

    /**
     * This example shows how to use generic operation instrumentation to trace call and record duration metric
     */
    public void instrumentCallWithMetricsAndTraces() {
        Instrumentation instrumentation = Instrumentation.create(null, LIBRARY_OPTIONS);
        URI serviceEndpoint = URI.create("https://example.com");
        final String durationMetricName = "sample.client.operation.duration";
        InstrumentedOperationDetails downloadDetails = new InstrumentedOperationDetails(durationMetricName, "downloadContent")
            .endpoint(serviceEndpoint);

        RequestOptions requestOptions = null;

        // BEGIN: io.clientcore.core.instrumentation.operation

        final OperationInstrumentation operationInstrumentation = instrumentation.createOperationInstrumentation(downloadDetails);

        if (!operationInstrumentation.shouldInstrument(requestOptions)) {
            clientCall(requestOptions);
            return;
        }

        if (requestOptions == null || requestOptions == RequestOptions.none()) {
            requestOptions = new RequestOptions();
        }

        OperationInstrumentation.Scope scope = operationInstrumentation.startScope(requestOptions);

        try {
            clientCall(requestOptions);
        } catch (Throwable t) {
            // make sure to report any exceptions including unchecked ones.
            scope.setError(getCause(t));
            throw t;
        } finally {
            scope.close();
        }

        // END: io.clientcore.core.instrumentation.operation
    }

    /**
     * This example shows how to enrich spans create by generic operation instrumentation with additional attributes.
     * Note: metrics enrichment is not supported yet.
     */
    public void enrichOperationInstrumentation() {
        Instrumentation instrumentation = Instrumentation.create(null, LIBRARY_OPTIONS);
        URI serviceEndpoint = URI.create("https://example.com");
        final String durationMetricName = "sample.client.operation.duration";
        InstrumentedOperationDetails downloadDetails = new InstrumentedOperationDetails(durationMetricName, "downloadContent")
            .endpoint(serviceEndpoint);

        RequestOptions requestOptions = null;

        final OperationInstrumentation operationInstrumentation = instrumentation.createOperationInstrumentation(downloadDetails);

        if (!operationInstrumentation.shouldInstrument(requestOptions)) {
            clientCall(requestOptions);
            return;
        }

        if (requestOptions == null || requestOptions == RequestOptions.none()) {
            requestOptions = new RequestOptions();
        }

        // BEGIN: io.clientcore.core.instrumentation.enrich
        OperationInstrumentation.Scope scope = operationInstrumentation.startScope(requestOptions);
        Span span = scope.getInstrumentationContext().getSpan();
        if (span.isRecording()) {
            span.setAttribute("sample.content.id", "{content-id}");
        }

        // END: io.clientcore.core.instrumentation.enrich

        try {
            clientCall(requestOptions);
        } catch (Throwable t) {
            // make sure to report any exceptions including unchecked ones.
            scope.setError(getCause(t));
            throw t;
        } finally {
            scope.close();
        }
    }

    private Throwable getCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    /**
     * This example shows full distributed tracing instrumentation that adds attributes.
     */
    @SuppressWarnings("try")
    public void traceWithAttributes() {

        Tracer tracer = Instrumentation.create(null, LIBRARY_OPTIONS).createTracer();
        RequestOptions requestOptions = null;

        // BEGIN: io.clientcore.core.instrumentation.tracewithattributes

        Span sendSpan = tracer.spanBuilder("send {queue-name}", SpanKind.PRODUCER, null)
            // Some of the attributes should be provided at the start time (as documented in semantic conventions) -
            // they can be used by client apps to sample spans.
            .setAttribute("messaging.system", "servicebus")
            .setAttribute("messaging.destination.name", "{queue-name}")
            .setAttribute("messaging.operations.name", "send")
            .startSpan();

        try (TracingScope scope = sendSpan.makeCurrent()) {
            if (sendSpan.isRecording()) {
                sendSpan.setAttribute("messaging.message.id", "{message-id}");
            }

            clientCall(requestOptions);
        } catch (Throwable t) {
            sendSpan.end(t);
            throw t;
        } finally {
            sendSpan.end();
        }

        // END:  io.clientcore.core.instrumentation.tracewithattributes
    }

    public void configureInstrumentationPolicy() {
        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions();

        // BEGIN: io.clientcore.core.instrumentation.instrumentationpolicy

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(
                new HttpRetryPolicy(),
                new HttpInstrumentationPolicy(instrumentationOptions))
            .build();

        // END:  io.clientcore.core.instrumentation.instrumentationpolicy
    }

    public void customizeInstrumentationPolicy() {
        // BEGIN: io.clientcore.core.instrumentation.customizeinstrumentationpolicy

        // You can configure URL sanitization to include additional query parameters to preserve
        // in `url.full` attribute.
        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions();
        instrumentationOptions.addAllowedQueryParamName("documentId");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(
                new HttpRetryPolicy(),
                new HttpInstrumentationPolicy(instrumentationOptions))
            .build();

        // END:  io.clientcore.core.instrumentation.customizeinstrumentationpolicy
    }

    public void enrichInstrumentationPolicySpans() {
        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions();

        // BEGIN: io.clientcore.core.instrumentation.enrichhttpspans

        HttpPipelinePolicy enrichingPolicy = (request, next) -> {
            Span span = request.getRequestOptions() == null
                ? Span.noop()
                : request.getRequestOptions().getInstrumentationContext().getSpan();
            if (span.isRecording()) {
                span.setAttribute("custom.request.id", request.getHeaders().getValue(CUSTOM_REQUEST_ID));
            }

            return next.process();
        };

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(
                new HttpRetryPolicy(),
                new HttpInstrumentationPolicy(instrumentationOptions),
                enrichingPolicy)
            .build();


        // END:  io.clientcore.core.instrumentation.enrichhttpspans
    }


    private void performOperation() {
    }

    private void clientCall(RequestOptions options) {
    }

    private void sendBatch(List<?> messages) {

    }
}
