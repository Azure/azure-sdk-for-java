// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpInstrumentationPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePosition;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.LongCounter;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.clientcore.core.models.binarydata.BinaryData;

import java.io.IOException;
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
    private static final HttpHeaderName CUSTOM_REQUEST_ID = HttpHeaderName.fromString("custom-request-id");

    public void getTracer() {

        // BEGIN: io.clientcore.core.instrumentation.gettracer

        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("sample")
            .setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, sdkOptions);

        Tracer tracer = instrumentation.getTracer();

        // END: io.clientcore.core.instrumentation.gettracer
    }

    public void getMeter() {
        // BEGIN: io.clientcore.core.instrumentation.getmeter

        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("sample")
            .setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, sdkOptions);
        Meter meter = instrumentation.getMeter();

        // END: io.clientcore.core.instrumentation.getmeter
    }

    public void histogram() {
        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("sample")
            .setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, sdkOptions);
        Meter meter = instrumentation.getMeter();

        // BEGIN: io.clientcore.core.instrumentation.histogram

        List<Double> bucketBoundariesAdvice = Collections.unmodifiableList(Arrays.asList(0.005d, 0.01d, 0.025d, 0.05d, 0.075d,
            0.1d, 0.25d, 0.5d, 0.75d, 1d, 2.5d, 5d, 7.5d, 10d));
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
        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("sample")
            .setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, sdkOptions);
        Meter meter = instrumentation.getMeter();

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
        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("sample")
            .setSdkVersion("1.0.0")
            .setEndpoint("https://example.com")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, sdkOptions);
        Meter meter = instrumentation.getMeter();

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
        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("sample")
            .setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

        InstrumentationOptions instrumentationOptions = new InstrumentationOptions();

        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, sdkOptions);
        InstrumentationAttributes attributes = instrumentation
            .createAttributes(Collections.singletonMap("key1", "value1"));

        // END: io.clientcore.core.instrumentation.createattributes
    }

    /**
     * This example shows minimal distributed tracing instrumentation.
     */
    @SuppressWarnings("try")
    public void traceCall() throws IOException {
        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("sample")
            .setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0")
            .setEndpoint("https://example.com");

        Tracer tracer = Instrumentation.create(null, sdkOptions).getTracer();
        RequestContext context = RequestContext.none();

        // BEGIN: io.clientcore.core.instrumentation.tracecall

        if (!tracer.isEnabled()) {
            // tracing is disabled, so we don't need to create a span
            clientCall(context).close();
            return;
        }

        InstrumentationContext instrumentationContext = context.getInstrumentationContext();
        Span span = tracer.spanBuilder("{operationName}", SpanKind.CLIENT, instrumentationContext)
            .startSpan();

        RequestContext childContext = context.toBuilder()
            .setInstrumentationContext(span.getInstrumentationContext())
            .build();

        // we'll propagate context implicitly using span.makeCurrent() as shown later.
        // Libraries that write async code should propagate context explicitly in addition to implicit propagation.
        try (TracingScope scope = span.makeCurrent()) {
            clientCall(childContext).close();
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
        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("sample")
            .setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0")
            .setEndpoint("https://example.com");
        Instrumentation instrumentation = Instrumentation.create(null, sdkOptions);

        RequestContext requestContext = RequestContext.none();

        // BEGIN: io.clientcore.core.instrumentation.operation

        instrumentation.instrument("downloadContent", requestContext, this::clientCall);

        // END: io.clientcore.core.instrumentation.operation
    }

    /**
     * This example shows how to enrich spans create by generic operation instrumentation with additional attributes.
     * Note: metrics enrichment is not supported yet.
     */
    public void enrichOperationInstrumentation() {
        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("sample")
            .setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0")
            .setEndpoint("https://example.com");

        Instrumentation instrumentation = Instrumentation.create(null, sdkOptions);
        RequestContext requestContext = RequestContext.none();

        // BEGIN: io.clientcore.core.instrumentation.enrich
        instrumentation.instrumentWithResponse("downloadContent", requestContext, updatedContext -> {
            Span span = updatedContext.getInstrumentationContext().getSpan();
            if (span.isRecording()) {
                span.setAttribute("sample.content.id", "{content-id}");
            }

            return clientCall(updatedContext);
        });

        // END: io.clientcore.core.instrumentation.enrich
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
    public void traceWithAttributes() throws IOException {
        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("sample")
            .setSdkVersion("1.0.0")
            .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0")
            .setEndpoint("https://example.com");

        Tracer tracer = Instrumentation.create(null, sdkOptions).getTracer();
        RequestContext context = RequestContext.none();

        // BEGIN: io.clientcore.core.instrumentation.tracewithattributes

        Span sendSpan = tracer.spanBuilder("send {queue-name}", SpanKind.PRODUCER, context.getInstrumentationContext())
            // Some of the attributes should be provided at the start time (as documented in semantic conventions) -
            // they can be used by client apps to sample spans.
            .setAttribute("messaging.system", "servicebus")
            .setAttribute("messaging.destination.name", "{queue-name}")
            .setAttribute("messaging.operations.name", "send")
            .startSpan();

        RequestContext childContext = context.toBuilder()
            .setInstrumentationContext(sendSpan.getInstrumentationContext())
            .build();

        try (TracingScope scope = sendSpan.makeCurrent()) {
            if (sendSpan.isRecording()) {
                sendSpan.setAttribute("messaging.message.id", "{message-id}");
            }

            Response<?> response = clientCall(childContext);
            response.close();
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
            .addPolicy(new HttpRetryPolicy())
            .addPolicy(new HttpInstrumentationPolicy(instrumentationOptions))
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
            .addPolicy(new HttpRetryPolicy())
            .addPolicy(new HttpInstrumentationPolicy(instrumentationOptions))
            .build();

        // END:  io.clientcore.core.instrumentation.customizeinstrumentationpolicy
    }

    public void enrichInstrumentationPolicySpans() {
        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions();

        // BEGIN: io.clientcore.core.instrumentation.enrichhttpspans

        HttpPipelinePolicy enrichingPolicy = new HttpPipelinePolicy() {
            @Override
            public Response<BinaryData> process(HttpRequest request, HttpPipelineNextPolicy next) {
                Span span = request.getContext().getInstrumentationContext().getSpan();
                if (span.isRecording()) {
                    span.setAttribute("custom.request.id", request.getHeaders().getValue(CUSTOM_REQUEST_ID));
                }

                return next.process();
            }

            @Override
            public HttpPipelinePosition getPipelinePosition() {
                return HttpPipelinePosition.AFTER_INSTRUMENTATION;
            }
        };

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .addPolicy(new HttpRetryPolicy())
            .addPolicy(new HttpInstrumentationPolicy(instrumentationOptions))
            .addPolicy(enrichingPolicy)
            .build();

        // END:  io.clientcore.core.instrumentation.enrichhttpspans
    }


    private void performOperation() {
    }

    private Response<?> clientCall(RequestContext requestContext) {
        return null;
    }

    private void sendBatch(List<?> messages) {

    }
}
