// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.telemetry;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveClientBuilder;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.SessionUpdateResponseDone;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.KeyCredential;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrating automatic tracing via {@code GlobalOpenTelemetry}.
 *
 * <p>This sample registers a global OpenTelemetry instance with
 * {@code OpenTelemetrySdk.builder().buildAndRegisterGlobal()}. The VoiceLive client picks it
 * up automatically via {@code GlobalOpenTelemetry.getOrNoop()}.</p>
 *
 * <p>Alternatively, attach the OpenTelemetry Java agent ({@code -javaagent:opentelemetry-javaagent.jar})
 * which registers the global instance automatically — no code needed at all.</p>
 *
 * <p><strong>Environment Variables Required:</strong></p>
 * <ul>
 *     <li>{@code AZURE_VOICELIVE_ENDPOINT} — The VoiceLive service endpoint URL</li>
 *     <li>{@code AZURE_VOICELIVE_API_KEY} — The API key for authentication</li>
 * </ul>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.telemetry.GlobalTracingSample" -Dexec.classpathScope=test
 * }</pre>
 */
public final class GlobalTracingSample {

    public static void main(String[] args) throws InterruptedException {
        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");
        String apiKey = System.getenv("AZURE_VOICELIVE_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("Please set AZURE_VOICELIVE_ENDPOINT and AZURE_VOICELIVE_API_KEY environment variables");
            return;
        }

        // 1. Register a global OpenTelemetry instance BEFORE building any client.
        //    In production, you'd use OtlpGrpcSpanExporter or Azure Monitor instead of ConsoleSpanExporter.
        //    Alternatively, attach the OpenTelemetry Java agent (-javaagent:opentelemetry-javaagent.jar)
        //    which does this automatically — no code needed at all.
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(new ConsoleSpanExporter()))
            .build();
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();  // <-- registers into GlobalOpenTelemetry
        System.out.println("GlobalOpenTelemetry registered (console exporter)");

        // 2. Build client — it picks up GlobalOpenTelemetry automatically.
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildAsyncClient();

        System.out.println("Starting voice session (automatic tracing)...");

        CountDownLatch done = new CountDownLatch(1);

        // 3. Run a short text-mode conversation — all operations are traced automatically.
        client.startSession("gpt-realtime")
            .flatMap(session -> {
                VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
                    .setModalities(Arrays.asList(InteractionModality.TEXT))
                    .setInstructions("You are a helpful assistant. Be concise.");

                // Configure the session, trigger a response, then wait for response.done.
                // Uses a single reactive chain: send config → start response → wait for done → close.
                return session.sendEvent(new ClientEventSessionUpdate(options))
                    .then(session.startResponse())
                    .thenMany(session.receiveEvents()
                        .doOnNext(event -> System.out.println("Event: " + event.getType()))
                        .filter(event -> event instanceof SessionUpdateResponseDone)
                        .take(1))
                    .flatMap(event -> session.closeAsync())
                    .doOnError(error -> System.err.println("Error: " + error.getMessage()))
                    .onErrorComplete()
                    .then();
            })
            .subscribe(
                v -> {},
                error -> done.countDown(),
                () -> done.countDown()
            );

        done.await(30, TimeUnit.SECONDS);

        // 4. Flush remaining spans.
        tracerProvider.close();
    }

    private GlobalTracingSample() {
    }

    /**
     * Minimal console exporter that prints span names, attributes, and events.
     */
    private static final class ConsoleSpanExporter implements SpanExporter {

        @Override
        public CompletableResultCode export(Collection<SpanData> spans) {
            for (SpanData span : spans) {
                System.out.printf("'%s' : %s%n", span.getName(), span.getAttributes());
                for (EventData event : span.getEvents()) {
                    System.out.printf("  Event '%s': %s%n", event.getName(), event.getAttributes());
                }
            }
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
