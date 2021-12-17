package com.azure.monitor.opentelemetry.exporter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AiDependencyOperationNameSpanProcessorTest {
    private static final Tracer TRACER = configureAzureMonitorExporter();

    private static Tracer configureAzureMonitorExporter() {
        AzureMonitorTraceExporter exporter = new AzureMonitorExporterBuilder()
            .connectionString("{connection-string}")
            .buildTraceExporter();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(new AiDependencyOperationNameSpanProcessor())
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build();

        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();

        return openTelemetrySdk.getTracer("Sample");
    }

    @Test
    public void operationNameFromParentTest() throws InterruptedException {


        Span parentSpan = TRACER.spanBuilder("parent-span").setAttribute(io.opentelemetry.semconv.trace.attributes.SemanticAttributes.HTTP_METHOD, "myop").startSpan();

        parentSpan.updateName(",...");

        parentSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "POST");


        final Scope parentScope = parentSpan.makeCurrent();
        Span span = TRACER.spanBuilder("child-span").startSpan();
        final Scope scope = span.makeCurrent();
        try {
            // Thread bound (sync) calls will automatically pick up the parent span and you don't need to pass it explicitly.

        } finally {
            span.end();
            scope.close();
            parentSpan.end();
            parentScope.close();
        }

    }
}
