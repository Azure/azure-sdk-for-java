package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.NoOpHttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.observability.Tracer;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstrumentationPolicyTests {
    @Test
    public void withInstrumentationPolicy() throws IOException {
        InMemorySpanExporter exporter = InMemorySpanExporter.create();
        try (SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build()) {

            Tracer tracer = new Tracer(tracerProvider.get("test"));
            HttpPipeline pipeline
                = new HttpPipelineBuilder().policies(new InstrumentationPolicy(tracer)).httpClient(new NoOpHttpClient() {
                @Override
                public Response<?> send(HttpRequest request) {
                    return new MockHttpResponse(request, 200);
                }
            }).build();

            Response<?> r = pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"));
            r.close();
            assertNotNull(exporter.getFinishedSpanItems());
            assertEquals(1, exporter.getFinishedSpanItems().size());
        }
    }
}
