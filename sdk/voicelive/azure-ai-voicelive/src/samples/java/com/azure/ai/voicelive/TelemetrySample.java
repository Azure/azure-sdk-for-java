// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.SessionUpdateResponseDone;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.KeyCredential;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrating how to enable OpenTelemetry tracing for VoiceLive sessions.
 *
 * <p>This runnable sample shows how to configure OpenTelemetry tracing so that every
 * connect, send, recv, and close operation emits spans with voice-specific attributes.</p>
 *
 * <p><strong>Environment Variables Required:</strong></p>
 * <ul>
 *     <li>{@code AZURE_VOICELIVE_ENDPOINT} — The VoiceLive service endpoint URL</li>
 *     <li>{@code AZURE_VOICELIVE_API_KEY} — The API key for authentication</li>
 * </ul>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.TelemetrySample" -Dexec.classpathScope=test
 * }</pre>
 *
 * <p><strong>Span Structure:</strong></p>
 * When tracing is enabled, the following span hierarchy is emitted:
 * <pre>
 * connect gpt-4o-realtime-preview        ← session lifetime
 * ├── send session.update                ← send spans include event type
 * ├── send response.create
 * ├── recv session.created               ← recv spans include event type
 * ├── recv response.audio.delta
 * ├── recv response.done                 ← includes token usage
 * └── close
 * </pre>
 *
 * <p><strong>Session-level Attributes (on connect span):</strong></p>
 * <ul>
 *     <li>{@code gen_ai.voice.session_id} — Voice session ID</li>
 *     <li>{@code gen_ai.voice.turn_count} — Completed response turns</li>
 *     <li>{@code gen_ai.voice.interruption_count} — User interruptions</li>
 *     <li>{@code gen_ai.voice.audio_bytes_sent} — Total audio payload bytes sent</li>
 *     <li>{@code gen_ai.voice.audio_bytes_received} — Total audio payload bytes received</li>
 *     <li>{@code gen_ai.voice.first_token_latency_ms} — Time to first response</li>
 * </ul>
 *
 * <p><strong>Alternative: Automatic tracing via GlobalOpenTelemetry</strong></p>
 * <p>If the OpenTelemetry Java agent is attached or {@code GlobalOpenTelemetry} is configured,
 * tracing works automatically with no builder configuration needed. The client defaults to
 * {@code GlobalOpenTelemetry.getOrNoop()}, which is a zero-cost no-op when no SDK is present.</p>
 *
 * <p><strong>Alternative: Azure Monitor Integration</strong></p>
 * <pre>{@code
 * // Replace LoggingSpanExporter with azure-monitor-opentelemetry-exporter:
 * AzureMonitorExporterBuilder exporter = new AzureMonitorExporterBuilder()
 *     .connectionString(System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING"));
 * SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
 *     .addSpanProcessor(SimpleSpanProcessor.create(exporter.buildTraceExporter()))
 *     .build();
 * }</pre>
 */
public final class TelemetrySample {

    /**
     * Main method to run the telemetry sample.
     *
     * @param args Unused command line arguments
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public static void main(String[] args) throws InterruptedException {
        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");
        String apiKey = System.getenv("AZURE_VOICELIVE_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("Please set AZURE_VOICELIVE_ENDPOINT and AZURE_VOICELIVE_API_KEY environment variables");
            return;
        }

        // 1. Set up OpenTelemetry with a console exporter that prints spans to stdout.
        //    In production, replace LoggingSpanExporter with OtlpGrpcSpanExporter
        //    or the Azure Monitor exporter.
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .build();
        OpenTelemetry otel = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();

        // 2. Build client with the explicit OpenTelemetry instance.
        //    Alternatively, omit .openTelemetry() to use GlobalOpenTelemetry automatically.
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .openTelemetry(otel)
            .enableContentRecording(false) // Set true to capture JSON payloads in spans
            .buildAsyncClient();

        System.out.println("Starting traced voice session...");

        CountDownLatch done = new CountDownLatch(1);

        // 3. Run a short text-mode conversation — all operations are traced automatically.
        client.startSession("gpt-4o-realtime-preview")
            .flatMap(session -> {
                VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
                    .setModalities(Arrays.asList(InteractionModality.TEXT))
                    .setInstructions("You are a helpful assistant. Be concise.");

                session.receiveEvents()
                    .subscribe(
                        event -> {
                            System.out.println("Event: " + event.getType());
                            if (event instanceof SessionUpdateResponseDone) {
                                session.closeAsync().subscribe();
                                done.countDown();
                            }
                        },
                        error -> {
                            System.err.println("Error: " + error.getMessage());
                            done.countDown();
                        }
                    );

                return session.sendEvent(new ClientEventSessionUpdate(options))
                    .then(session.startResponse())
                    .then(Mono.empty());
            })
            .subscribe();

        done.await(30, TimeUnit.SECONDS);

        // 4. Shut down the tracer provider to flush remaining spans to console.
        tracerProvider.close();
    }

    private TelemetrySample() {
    }
}
