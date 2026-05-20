// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.telemetry;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveClientBuilder;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputTextContentPart;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.SessionResponse;
import com.azure.ai.voicelive.models.SessionUpdateResponseDone;
import com.azure.ai.voicelive.models.UserMessageItem;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrating automatic tracing via {@code GlobalOpenTelemetry}.
 *
 * <p>Use this sample when you want to confirm that the VoiceLive client emits OpenTelemetry spans
 * automatically without adding manual tracing calls around each SDK operation.</p>
 *
 * <p>When you run it, the sample registers a simple console span exporter, opens a short text-only
 * VoiceLive session, sends a few prompts back-to-back, prints each server event as it arrives, and
 * then flushes the spans so you can inspect the emitted telemetry immediately.</p>
 *
 * <p>This sample registers a global OpenTelemetry instance with
 * {@code OpenTelemetrySdk.builder().buildAndRegisterGlobal()}. The VoiceLive client picks it
 * up automatically via {@code GlobalOpenTelemetry.getOrNoop()}.</p>
 *
 * <p>Alternatively, attach the OpenTelemetry Java agent ({@code -javaagent:opentelemetry-javaagent.jar})
 * which registers the global instance automatically — no code needed at all.</p>
 *
 * <p><strong>Environment Variables:</strong></p>
 * <ul>
 *     <li>{@code AZURE_VOICELIVE_ENDPOINT} — (Required) The VoiceLive service endpoint URL</li>
 * </ul>
 *
 * <p>This sample uses {@link DefaultAzureCredentialBuilder} (Entra ID, recommended). For an example
 * of API key authentication, see {@code AuthenticationMethodsSample}.</p>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.telemetry.GlobalTracingSample" -Dexec.classpathScope=test
 * }</pre>
 */
public final class GlobalTracingSample {

    public static void main(String[] args) throws InterruptedException {
        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");

        if (endpoint == null) {
            System.err.println("Please set AZURE_VOICELIVE_ENDPOINT environment variable");
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
        //    Authenticates using DefaultAzureCredential (Entra ID).
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setModalities(Arrays.asList(InteractionModality.TEXT))
            .setInstructions("You are a helpful assistant. Be concise.");

        System.out.println("Starting voice session (automatic tracing)...");

        // Multiple prompts → multiple round-trips → richer trace output.
        List<String> prompts = Arrays.asList(
            "Say hello in one short sentence.",
            "Now name one color.",
            "Now name one fruit."
        );

        CountDownLatch done = new CountDownLatch(1);

        // 3. Run a short text-mode conversation — all operations are traced automatically.
        //    Session lifetime is local to this reactive chain; the session is captured by the
        //    lambda passed to flatMapMany and then threaded into per-event handling via flatMap,
        //    so no instance field or shared holder is needed.
        client.startSession("gpt-realtime")
            .flatMapMany(session -> configureSession(session, sessionOptions, prompts)
                .thenMany(session.receiveEvents())
                .flatMap(GlobalTracingSample::handleServerEvent))
            .subscribe(
                ignored -> { },
                error -> {
                    System.err.println("Error: " + error.getMessage());
                    done.countDown();
                },
                done::countDown
            );

        done.await(60, TimeUnit.SECONDS);

        // 4. Flush remaining spans.
        tracerProvider.close();
    }

    /**
     * Send the session configuration followed by each prompt as a sequential chain.
     */
    private static Mono<Void> configureSession(
        VoiceLiveSessionAsyncClient session,
        VoiceLiveSessionOptions sessionOptions,
        List<String> prompts
    ) {
        Mono<Void> chain = session.sendEvent(new ClientEventSessionUpdate(sessionOptions));
        for (String prompt : prompts) {
            UserMessageItem message = new UserMessageItem(
                Collections.singletonList(new InputTextContentPart(prompt)));
            chain = chain
                .then(session.sendEvent(new ClientEventConversationItemCreate().setItem(message)))
                .then(session.sendEvent(new ClientEventResponseCreate()));
        }
        return chain;
    }

    /**
     * Handle a single server event. Returns {@link Mono#empty()} so per-event handling stays
     * inside the reactive chain (no nested subscribe). This sample doesn't send any follow-up
     * events from inside the handler.
     */
    private static Mono<Void> handleServerEvent(com.azure.ai.voicelive.models.SessionUpdate serverEvent) {
        System.out.println("Event: " + serverEvent.getType());
        if (serverEvent instanceof SessionUpdateResponseDone) {
            SessionResponse response = ((SessionUpdateResponseDone) serverEvent).getResponse();
            if (response.getUsage() != null) {
                System.out.println("  Total tokens: " + response.getUsage().getTotalTokens());
            }
        }
        return Mono.empty();
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
