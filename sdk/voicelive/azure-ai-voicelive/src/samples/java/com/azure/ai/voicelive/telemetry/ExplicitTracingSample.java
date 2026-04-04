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
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
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
 * # Basic (no tracing):
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.telemetry.ExplicitTracingSample" -Dexec.classpathScope=test
 *
 * # With OpenTelemetry tracing enabled:
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.telemetry.ExplicitTracingSample" -Dexec.classpathScope=test -Dexec.args="--enable-tracing"
 *
 * # With tracing + JSON payload content recording:
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.telemetry.ExplicitTracingSample" -Dexec.classpathScope=test -Dexec.args="--enable-tracing --enable-recording"
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
public final class ExplicitTracingSample {

    /**
     * Main method to run the telemetry sample.
     *
     * @param args Unused command line arguments
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public static void main(String[] args) throws InterruptedException {
        // Parse command line arguments
        boolean enableTracing = false;
        boolean enableRecording = false;
        for (String arg : args) {
            if ("--enable-tracing".equals(arg)) {
                enableTracing = true;
            } else if ("--enable-recording".equals(arg)) {
                enableRecording = true;
            }
        }

        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");
        String apiKey = System.getenv("AZURE_VOICELIVE_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("Please set AZURE_VOICELIVE_ENDPOINT and AZURE_VOICELIVE_API_KEY environment variables");
            System.err.println();
            System.err.println("Optional flags:");
            System.err.println("  --enable-tracing    Enable OpenTelemetry tracing (prints spans to console)");
            System.err.println("  --enable-recording  Also capture full JSON payloads in span events");
            return;
        }

        // 1. Set up OpenTelemetry tracing if enabled.
        //    This custom exporter prints both attributes AND span events (where content
        //    recording payloads appear). The built-in LoggingSpanExporter only prints
        //    attributes, so recorded content would not be visible with it.
        //    In production, replace with OtlpGrpcSpanExporter or the Azure Monitor exporter.
        SdkTracerProvider tracerProvider = null;
        OpenTelemetry otel = null;
        if (enableTracing) {
            tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(new ConsoleSpanExporter()))
                .build();
            otel = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
            System.out.println("OpenTelemetry tracing enabled (console exporter)");
            if (enableRecording) {
                System.out.println("Content recording enabled (JSON payloads will appear in span events)");
            }
        }

        // 2. Build client — optionally with tracing and content recording.
        //    Alternatively, omit .openTelemetry() to use GlobalOpenTelemetry automatically.
        VoiceLiveClientBuilder builder = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey));
        if (otel != null) {
            builder.openTelemetry(otel);
            builder.enableContentRecording(enableRecording);
        }
        VoiceLiveAsyncClient client = builder.buildAsyncClient();

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
        if (tracerProvider != null) {
            tracerProvider.close();
        }
    }

    private ExplicitTracingSample() {
    }

    /**
     * Custom span exporter that prints both span attributes and span events to the console.
     *
     * <p>The built-in {@code LoggingSpanExporter} only prints span attributes. When content
     * recording is enabled via {@code enableContentRecording(true)}, JSON payloads are captured
     * as span events (e.g., {@code gen_ai.input_messages}, {@code gen_ai.output_messages}).
     * This exporter makes those events visible in the console output.</p>
     */
    private static final class ConsoleSpanExporter implements SpanExporter {

        @Override
        public CompletableResultCode export(Collection<SpanData> spans) {
            for (SpanData span : spans) {
                System.out.printf("'%s' : %s %s %s [tracer: %s:%s] %s%n",
                    span.getName(),
                    span.getTraceId(),
                    span.getSpanId(),
                    span.getKind(),
                    span.getInstrumentationScopeInfo().getName(),
                    span.getInstrumentationScopeInfo().getVersion() != null
                        ? span.getInstrumentationScopeInfo().getVersion() : "",
                    span.getAttributes());

                // Print span events (content recording payloads appear here)
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
