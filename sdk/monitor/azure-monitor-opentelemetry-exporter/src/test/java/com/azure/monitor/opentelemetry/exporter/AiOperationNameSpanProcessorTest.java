package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.FluxUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AiOperationNameSpanProcessorTest {


    private static Tracer configureAzureMonitorExporter(HttpPipelinePolicy validator) {
        String connectionStringTemplate = "InstrumentationKey=ikey;IngestionEndpoint=https://testendpoint.com";
        String connectionString = Configuration.getGlobalConfiguration()
            .get("APPLICATIONINSIGHTS_CONNECTION_STRING", connectionStringTemplate);
        AzureMonitorTraceExporter exporter = new AzureMonitorExporterBuilder()
            .connectionString(connectionString)
            .addPolicy(validator)
            .buildTraceExporter();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(new AiOperationNameSpanProcessor())
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build();

        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();

        return openTelemetrySdk.getTracer("Sample");
    }

    @Test
    public void operationNameFromParentTest() throws InterruptedException {
        CountDownLatch exporterCountDown = new CountDownLatch(1);
        final Tracer TRACER = configureAzureMonitorExporter(new ValidationPolicy(exporterCountDown,
            Arrays.asList("child-span","myop")));

        Span parentSpan = TRACER.spanBuilder("parent-span").setAttribute(AiOperationNameSpanProcessor.AI_OPERATION_NAME_KEY, "myop").startSpan();
        parentSpan.updateName("parent-span-changed");
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

        assertTrue(exporterCountDown.await(10, TimeUnit.SECONDS));

    }

    @Test
    public void operationNameEmptyFromParentTest() throws InterruptedException {
        CountDownLatch exporterCountDown = new CountDownLatch(1);
        final Tracer TRACER = configureAzureMonitorExporter(new ValidationPolicy(exporterCountDown,
            Arrays.asList("child-span","POST parent-span-changed")));
        Span parentSpan = TRACER.spanBuilder("parent-span").startSpan();
        parentSpan.updateName("parent-span-changed");
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

        assertTrue(exporterCountDown.await(10, TimeUnit.SECONDS));

    }

    @Test
    public void operationNameAsSpanNameTest() throws InterruptedException {
        CountDownLatch exporterCountDown = new CountDownLatch(1);
        final Tracer TRACER = configureAzureMonitorExporter(new ValidationPolicy(exporterCountDown,
            Arrays.asList("child-span","parent-span-changed")));
        Span parentSpan = TRACER.spanBuilder("parent-span").startSpan();
        parentSpan.updateName("parent-span-changed");
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

        assertTrue(exporterCountDown.await(10, TimeUnit.SECONDS));

    }

    static class ValidationPolicy implements HttpPipelinePolicy {

        private final CountDownLatch countDown;
        private final List<String> expectedValues;

        ValidationPolicy(CountDownLatch countDown, List<String> expectedValues) {
            this.countDown = countDown;
            this.expectedValues = expectedValues;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            Mono<String> asyncString = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
            asyncString.subscribe(value -> {
                for(String expectedName: expectedValues) {
                    if(!value.contains(expectedName)) {
                        return;
                    }
                }
                countDown.countDown();
            });
            return next.process();
        }
    }
}
