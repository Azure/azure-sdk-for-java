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
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;

/**
 * THESE CODE SNIPPETS ARE INTENDED FOR CLIENT LIBRARY DEVELOPERS ONLY.
 * <p>
 *
 * Application developers are expected to use OpenTelemetry API directly.
 * Check out {@code TelemetryJavaDocCodeSnippets} for application-level samples.
 */
public class TracingForLibraryDevelopersJavaDocCodeSnippets {
    private static final LibraryInstrumentationOptions LIBRARY_OPTIONS = new LibraryInstrumentationOptions("sample")
        .setLibraryVersion("1.0.0")
        .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");
    private static final HttpHeaderName CUSTOM_REQUEST_ID = HttpHeaderName.fromString("custom-request-id");

    public void createTracer() {

        // BEGIN: io.clientcore.core.telemetry.tracing.createtracer

        LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions("sample")
            .setLibraryVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();

        Tracer tracer = Instrumentation.create(instrumentationOptions, libraryOptions).getTracer();

        // END: io.clientcore.core.telemetry.tracing.createtracer
    }

    /**
     * This example shows minimal distributed tracing instrumentation.
     */
    @SuppressWarnings("try")
    public void traceCall() {

        Tracer tracer = Instrumentation.create(null, LIBRARY_OPTIONS).getTracer();
        RequestOptions requestOptions = null;

        // BEGIN: io.clientcore.core.telemetry.tracing.tracecall

        Span span = tracer.spanBuilder("{operationName}", SpanKind.CLIENT, null)
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
            span.end(t);
            throw t;
        } finally {
            // NOTE: closing the scope does not end the span, span should be ended explicitly.
            span.end();
        }

        // END:  io.clientcore.core.telemetry.tracing.tracecall
    }

    /**
     * This example shows full distributed tracing instrumentation that adds attributes.
     */
    @SuppressWarnings("try")
    public void traceWithAttributes() {

        Tracer tracer = Instrumentation.create(null, LIBRARY_OPTIONS).getTracer();
        RequestOptions requestOptions = null;

        // BEGIN: io.clientcore.core.telemetry.tracing.tracewithattributes

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

        // END:  io.clientcore.core.telemetry.tracing.tracewithattributes
    }

    public void configureInstrumentationPolicy() {
        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions();

        // BEGIN: io.clientcore.core.telemetry.tracing.instrumentationpolicy

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(
                new HttpRetryPolicy(),
                new HttpInstrumentationPolicy(instrumentationOptions))
            .build();

        // END:  io.clientcore.core.telemetry.tracing.instrumentationpolicy
    }

    public void customizeInstrumentationPolicy() {
        // BEGIN: io.clientcore.core.telemetry.tracing.customizeinstrumentationpolicy

        // You can configure URL sanitization to include additional query parameters to preserve
        // in `url.full` attribute.
        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions();
        instrumentationOptions.addAllowedQueryParamName("documentId");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(
                new HttpRetryPolicy(),
                new HttpInstrumentationPolicy(instrumentationOptions))
            .build();

        // END:  io.clientcore.core.telemetry.tracing.customizeinstrumentationpolicy
    }

    public void enrichInstrumentationPolicySpans() {
        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions();

        // BEGIN: io.clientcore.core.telemetry.tracing.enrichhttpspans

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


        // END:  io.clientcore.core.telemetry.tracing.enrichhttpspans
    }

    private void clientCall(RequestOptions options) {
    }
}
