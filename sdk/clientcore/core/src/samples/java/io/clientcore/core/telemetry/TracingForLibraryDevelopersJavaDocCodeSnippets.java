// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.pipeline.HttpLoggingPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.http.pipeline.InstrumentationPolicy;
import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.SpanKind;
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.telemetry.tracing.TracingScope;

import java.util.Collections;
import java.util.Map;

/**
 * THESE CODE SNIPPETS ARE INTENDED FOR CLIENT LIBRARY DEVELOPERS ONLY.
 * <p>
 *
 * Application developers are expected to use OpenTelemetry API directly.
 * Check out {@link TelemetryJavaDocCodeSnippets} for application-level samples.
 */
public class TracingForLibraryDevelopersJavaDocCodeSnippets {
    private static final LibraryTelemetryOptions LIBRARY_OPTIONS = new LibraryTelemetryOptions("sample")
        .setLibraryVersion("1.0.0")
        .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

    public void createTracer() {

        // BEGIN: io.clientcore.core.telemetry.tracing.createtracer

        LibraryTelemetryOptions libraryOptions = new LibraryTelemetryOptions("sample")
            .setLibraryVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        TelemetryOptions<?> telemetryOptions = new TelemetryOptions<>();

        Tracer tracer = TelemetryProvider.getInstance().getTracer(telemetryOptions, libraryOptions);

        // END: io.clientcore.core.telemetry.tracing.createtracer
    }

    /**
     * This example shows minimal distributed tracing instrumentation.
     */
    public void traceCall() {

        Tracer tracer = TelemetryProvider.getInstance().getTracer(null, LIBRARY_OPTIONS);
        RequestOptions requestOptions = null;

        // BEGIN: io.clientcore.core.telemetry.tracing.tracecall

        Span span = tracer.spanBuilder("{operationName}", SpanKind.CLIENT, requestOptions)
            .startSpan();

        // we'll propagate context implicitly using span.makeCurrent() as shown later.
        // Libraries that write async code should propagate context explicitly in addition to implicit propagation.
        if (tracer.isEnabled()) {
            if (requestOptions == null) {
                requestOptions = new RequestOptions();
            }
            requestOptions.setContext(requestOptions.getContext().put(TelemetryProvider.TRACE_CONTEXT_KEY, span));
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
    public void traceWithAttributes() {

        Tracer tracer = TelemetryProvider.getInstance().getTracer(null, LIBRARY_OPTIONS);
        RequestOptions requestOptions = null;

        // BEGIN: io.clientcore.core.telemetry.tracing.tracewithattributes

        Span sendSpan = tracer.spanBuilder("send {queue-name}", SpanKind.PRODUCER, requestOptions)
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
        TelemetryOptions<?> telemetryOptions = new TelemetryOptions<>();
        HttpLogOptions logOptions = new HttpLogOptions();

        // BEGIN: io.clientcore.core.telemetry.tracing.instrumentationpolicy

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(
                new HttpRetryPolicy(),
                new InstrumentationPolicy(telemetryOptions, logOptions),
                new HttpLoggingPolicy(logOptions))
            .build();

        // END:  io.clientcore.core.telemetry.tracing.instrumentationpolicy
    }

    public void customizeInstrumentationPolicy() {
        TelemetryOptions<?> telemetryOptions = new TelemetryOptions<>();
        HttpLogOptions logOptions = new HttpLogOptions();

        // BEGIN: io.clientcore.core.telemetry.tracing.customizeinstrumentationpolicy

        // InstrumentationPolicy can capture custom headers from requests and responses - for example when the endpoint
        // supports legacy correlation headers.
        Map<HttpHeaderName, String> requestHeadersToRecord
            = Collections.singletonMap(HttpHeaderName.CLIENT_REQUEST_ID, "custom.request.id");
        Map<HttpHeaderName, String> responseHeadersToRecord
            = Collections.singletonMap(HttpHeaderName.REQUEST_ID, "custom.response.id");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(
                new HttpRetryPolicy(),
                new InstrumentationPolicy(telemetryOptions, logOptions, requestHeadersToRecord, responseHeadersToRecord),
                new HttpLoggingPolicy(logOptions))
            .build();

        // END:  io.clientcore.core.telemetry.tracing.customizeinstrumentationpolicy
    }

    private void clientCall(RequestOptions options) {
    }
}
