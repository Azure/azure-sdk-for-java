// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEvent;
import com.azure.ai.voicelive.models.ClientEventInputAudioBufferAppend;
import com.azure.ai.voicelive.models.ClientEventResponseCancel;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.ResponseTokenStatistics;
import com.azure.ai.voicelive.models.SessionResponse;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateError;
import com.azure.ai.voicelive.models.SessionUpdateErrorDetails;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateResponseDone;
import com.azure.ai.voicelive.models.SessionUpdateSessionCreated;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.ai.voicelive.models.VoiceLiveSessionResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

import java.net.URI;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracer for VoiceLive WebSocket sessions using the OpenTelemetry API.
 * <p>
 * Manages a parent "connect" span for the session lifetime, with child spans for
 * send, recv, and close operations. Tracks session-level counters for audio bytes,
 * turn counts, interruptions, and first-token latency.
 * </p>
 */
final class VoiceLiveTracer {

    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveTracer.class);

    // GenAI semantic convention attribute keys
    static final AttributeKey<String> GEN_AI_SYSTEM = AttributeKey.stringKey("gen_ai.system");
    static final String GEN_AI_SYSTEM_VALUE = "az.ai.voicelive";
    static final AttributeKey<String> GEN_AI_OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");
    static final AttributeKey<String> GEN_AI_REQUEST_MODEL = AttributeKey.stringKey("gen_ai.request.model");
    static final AttributeKey<String> AZ_NAMESPACE = AttributeKey.stringKey("az.namespace");
    static final String AZ_NAMESPACE_VALUE = "Microsoft.CognitiveServices";
    static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");
    static final AttributeKey<Long> SERVER_PORT = AttributeKey.longKey("server.port");

    // Voice-specific attribute keys
    static final AttributeKey<String> GEN_AI_VOICE_SESSION_ID = AttributeKey.stringKey("gen_ai.voice.session_id");
    static final AttributeKey<String> GEN_AI_VOICE_INPUT_AUDIO_FORMAT
        = AttributeKey.stringKey("gen_ai.voice.input_audio_format");
    static final AttributeKey<String> GEN_AI_VOICE_OUTPUT_AUDIO_FORMAT
        = AttributeKey.stringKey("gen_ai.voice.output_audio_format");
    static final AttributeKey<Long> GEN_AI_VOICE_TURN_COUNT = AttributeKey.longKey("gen_ai.voice.turn_count");
    static final AttributeKey<Long> GEN_AI_VOICE_INTERRUPTION_COUNT
        = AttributeKey.longKey("gen_ai.voice.interruption_count");
    static final AttributeKey<Long> GEN_AI_VOICE_AUDIO_BYTES_SENT
        = AttributeKey.longKey("gen_ai.voice.audio_bytes_sent");
    static final AttributeKey<Long> GEN_AI_VOICE_AUDIO_BYTES_RECEIVED
        = AttributeKey.longKey("gen_ai.voice.audio_bytes_received");
    static final AttributeKey<Long> GEN_AI_VOICE_FIRST_TOKEN_LATENCY_MS
        = AttributeKey.longKey("gen_ai.voice.first_token_latency_ms");
    static final AttributeKey<String> GEN_AI_VOICE_EVENT_TYPE = AttributeKey.stringKey("gen_ai.voice.event_type");
    static final AttributeKey<Long> GEN_AI_VOICE_MESSAGE_SIZE = AttributeKey.longKey("gen_ai.voice.message_size");
    static final AttributeKey<Long> GEN_AI_USAGE_INPUT_TOKENS = AttributeKey.longKey("gen_ai.usage.input_tokens");
    static final AttributeKey<Long> GEN_AI_USAGE_OUTPUT_TOKENS = AttributeKey.longKey("gen_ai.usage.output_tokens");
    static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");

    // Span event names
    static final String GEN_AI_INPUT_MESSAGES = "gen_ai.input.messages";
    static final String GEN_AI_OUTPUT_MESSAGES = "gen_ai.output.messages";
    static final String GEN_AI_VOICE_ERROR = "gen_ai.voice.error";

    // Event attribute keys
    private static final AttributeKey<String> EVENT_CONTENT = AttributeKey.stringKey("gen_ai.event.content");
    private static final AttributeKey<String> ERROR_CODE = AttributeKey.stringKey("error.code");
    private static final AttributeKey<String> ERROR_MESSAGE = AttributeKey.stringKey("error.message");

    // Content recording configuration
    private static final ConfigurationProperty<Boolean> CAPTURE_MESSAGE_CONTENT
        = ConfigurationPropertyBuilder.ofBoolean("azure.tracing.gen_ai.content_recording_enabled")
            .environmentVariableName("AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED")
            .systemPropertyName("azure.tracing.gen_ai.content_recording_enabled")
            .shared(true)
            .defaultValue(false)
            .build();

    private final Tracer tracer;
    private final boolean captureContent;
    private final String serverAddress;
    private final int serverPort;
    private final String model;

    // Session lifetime span and its OTel context (for parenting child spans)
    private final AtomicReference<Span> connectSpan = new AtomicReference<>();
    private final AtomicReference<Context> connectContext = new AtomicReference<>();

    // Session-level counters (thread-safe)
    private final AtomicLong turnCount = new AtomicLong(0);
    private final AtomicLong interruptionCount = new AtomicLong(0);
    private final AtomicLong audioBytesSent = new AtomicLong(0);
    private final AtomicLong audioBytesReceived = new AtomicLong(0);

    // First-token latency tracking
    private final AtomicLong responseCreateTimestampNanos = new AtomicLong(0);
    private final AtomicLong firstTokenLatencyMs = new AtomicLong(-1);

    // Session attributes discovered during the session
    private final AtomicReference<String> sessionId = new AtomicReference<>();
    private final AtomicReference<String> inputAudioFormat = new AtomicReference<>();
    private final AtomicReference<String> outputAudioFormat = new AtomicReference<>();

    /**
     * Creates a VoiceLiveTracer.
     *
     * @param tracer The OpenTelemetry Tracer instance (may be a no-op tracer).
     * @param endpoint The WebSocket endpoint URI.
     * @param model The model name.
     * @param captureContentOverride Optional override for content recording (null = use env var).
     */
    VoiceLiveTracer(Tracer tracer, URI endpoint, String model, Boolean captureContentOverride) {
        this.tracer = tracer;
        this.model = model;

        if (endpoint != null) {
            this.serverAddress = endpoint.getHost();
            this.serverPort
                = endpoint.getPort() == -1 ? ("wss".equals(endpoint.getScheme()) ? 443 : 80) : endpoint.getPort();
        } else {
            this.serverAddress = null;
            this.serverPort = -1;
        }

        if (captureContentOverride != null) {
            this.captureContent = captureContentOverride;
        } else {
            this.captureContent = Configuration.getGlobalConfiguration().get(CAPTURE_MESSAGE_CONTENT);
        }
    }

    // ============================================================================
    // Connect Span (session lifetime)
    // ============================================================================

    /**
     * Starts the parent "connect" span for the session lifetime.
     */
    void startConnectSpan() {
        String spanName = model != null ? "connect " + model : "connect";

        SpanBuilder spanBuilder = tracer.spanBuilder(spanName)
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
            .setAttribute(GEN_AI_OPERATION_NAME, "connect")
            .setAttribute(AZ_NAMESPACE, AZ_NAMESPACE_VALUE);

        if (model != null) {
            spanBuilder.setAttribute(GEN_AI_REQUEST_MODEL, model);
        }
        if (serverAddress != null) {
            spanBuilder.setAttribute(SERVER_ADDRESS, serverAddress);
            if (serverPort != 443 && serverPort != -1) {
                spanBuilder.setAttribute(SERVER_PORT, (long) serverPort);
            }
        }

        Span span = spanBuilder.startSpan();
        Context ctx = Context.current().with(span);
        connectSpan.set(span);
        connectContext.set(ctx);
    }

    /**
     * Ends the connect span, flushing session-level counters as attributes.
     *
     * @param error The error that caused the session to close, or null.
     */
    void endConnectSpan(Throwable error) {
        Span span = connectSpan.getAndSet(null);
        connectContext.set(null);
        if (span == null) {
            return;
        }

        // Flush session-level counters
        String sid = sessionId.get();
        if (sid != null) {
            span.setAttribute(GEN_AI_VOICE_SESSION_ID, sid);
        }
        String inFormat = inputAudioFormat.get();
        if (inFormat != null) {
            span.setAttribute(GEN_AI_VOICE_INPUT_AUDIO_FORMAT, inFormat);
        }
        String outFormat = outputAudioFormat.get();
        if (outFormat != null) {
            span.setAttribute(GEN_AI_VOICE_OUTPUT_AUDIO_FORMAT, outFormat);
        }
        span.setAttribute(GEN_AI_VOICE_TURN_COUNT, turnCount.get());
        span.setAttribute(GEN_AI_VOICE_INTERRUPTION_COUNT, interruptionCount.get());
        span.setAttribute(GEN_AI_VOICE_AUDIO_BYTES_SENT, audioBytesSent.get());
        span.setAttribute(GEN_AI_VOICE_AUDIO_BYTES_RECEIVED, audioBytesReceived.get());

        long latency = firstTokenLatencyMs.get();
        if (latency >= 0) {
            span.setAttribute(GEN_AI_VOICE_FIRST_TOKEN_LATENCY_MS, latency);
        }

        if (error != null) {
            span.setStatus(StatusCode.ERROR, error.getMessage());
            span.recordException(error);
            span.setAttribute(ERROR_TYPE, error.getClass().getCanonicalName());
        }

        span.end();
    }

    // ============================================================================
    // Send Span
    // ============================================================================

    /**
     * Traces a send operation. Creates a child span of the connect span.
     *
     * @param event The client event being sent.
     * @param jsonPayload The serialized JSON payload.
     */
    void traceSend(ClientEvent event, String jsonPayload) {
        Context parentCtx = connectContext.get();
        if (parentCtx == null) {
            return;
        }

        String eventType = event.getType() != null ? event.getType().toString() : "unknown";
        String spanName = "send " + eventType;

        Span span = tracer.spanBuilder(spanName)
            .setSpanKind(SpanKind.CLIENT)
            .setParent(parentCtx)
            .setAttribute(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
            .setAttribute(GEN_AI_OPERATION_NAME, "send")
            .setAttribute(AZ_NAMESPACE, AZ_NAMESPACE_VALUE)
            .setAttribute(GEN_AI_VOICE_EVENT_TYPE, eventType)
            .startSpan();

        try {
            if (span.isRecording()) {
                if (jsonPayload != null) {
                    span.setAttribute(GEN_AI_VOICE_MESSAGE_SIZE, (long) jsonPayload.length());
                }

                // Add span event
                AttributesBuilder eventAttrs = Attributes.builder()
                    .put(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
                    .put(GEN_AI_VOICE_EVENT_TYPE, eventType);
                if (captureContent && jsonPayload != null) {
                    eventAttrs.put(EVENT_CONTENT, jsonPayload);
                }
                span.addEvent(GEN_AI_INPUT_MESSAGES, eventAttrs.build());

                // Track session-level counters from sent events
                trackSendCounters(event);
            }
        } finally {
            span.end();
        }
    }

    // ============================================================================
    // Recv Span
    // ============================================================================

    /**
     * Traces a recv operation. Creates a child span of the connect span.
     *
     * @param update The parsed session update event.
     * @param rawPayload The raw JSON payload string (for message size and content recording).
     */
    void traceRecv(SessionUpdate update, String rawPayload) {
        Context parentCtx = connectContext.get();
        if (parentCtx == null) {
            return;
        }

        String eventType = update.getType() != null ? update.getType().toString() : "unknown";
        String spanName = "recv " + eventType;

        Span span = tracer.spanBuilder(spanName)
            .setSpanKind(SpanKind.CLIENT)
            .setParent(parentCtx)
            .setAttribute(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
            .setAttribute(GEN_AI_OPERATION_NAME, "recv")
            .setAttribute(AZ_NAMESPACE, AZ_NAMESPACE_VALUE)
            .setAttribute(GEN_AI_VOICE_EVENT_TYPE, eventType)
            .startSpan();

        try {
            if (span.isRecording()) {
                if (rawPayload != null) {
                    span.setAttribute(GEN_AI_VOICE_MESSAGE_SIZE, (long) rawPayload.length());
                }

                // Track per-message token usage from response.done
                trackRecvTokenUsage(update, span);

                // Add span event for output messages
                AttributesBuilder eventAttrs = Attributes.builder()
                    .put(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
                    .put(GEN_AI_VOICE_EVENT_TYPE, eventType);
                if (captureContent && rawPayload != null) {
                    eventAttrs.put(EVENT_CONTENT, rawPayload);
                }
                span.addEvent(GEN_AI_OUTPUT_MESSAGES, eventAttrs.build());

                // Track error events — set error status on span
                trackErrorEvents(update, span);

                // Track session-level counters from received events
                trackRecvCounters(update);
            }
        } finally {
            span.end();
        }
    }

    // ============================================================================
    // Close Span
    // ============================================================================

    /**
     * Traces the close operation.
     */
    void traceClose() {
        Context parentCtx = connectContext.get();
        if (parentCtx == null) {
            return;
        }

        Span span = tracer.spanBuilder("close")
            .setSpanKind(SpanKind.CLIENT)
            .setParent(parentCtx)
            .setAttribute(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
            .setAttribute(GEN_AI_OPERATION_NAME, "close")
            .setAttribute(AZ_NAMESPACE, AZ_NAMESPACE_VALUE)
            .startSpan();
        span.end();
    }

    // ============================================================================
    // Counter Tracking
    // ============================================================================

    private void trackSendCounters(ClientEvent event) {
        // Track audio bytes sent from input_audio_buffer.append
        if (event instanceof ClientEventInputAudioBufferAppend) {
            ClientEventInputAudioBufferAppend appendEvent = (ClientEventInputAudioBufferAppend) event;
            String audio = appendEvent.getAudio();
            if (audio != null) {
                try {
                    byte[] decoded = Base64.getDecoder().decode(audio);
                    audioBytesSent.addAndGet(decoded.length);
                } catch (IllegalArgumentException e) {
                    LOGGER.atVerbose().log("Failed to decode audio for byte counting", e);
                }
            }
        }

        // Track response.create for first-token latency
        if (event instanceof ClientEventResponseCreate) {
            responseCreateTimestampNanos.set(System.nanoTime());
            firstTokenLatencyMs.set(-1); // Reset for this response
        }

        // Track interruptions from response.cancel
        if (event instanceof ClientEventResponseCancel) {
            interruptionCount.incrementAndGet();
        }

        // Track audio format from session.update
        if (event instanceof ClientEventSessionUpdate) {
            ClientEventSessionUpdate sessionUpdate = (ClientEventSessionUpdate) event;
            VoiceLiveSessionOptions session = sessionUpdate.getSession();
            if (session != null) {
                if (session.getInputAudioFormat() != null) {
                    inputAudioFormat.set(session.getInputAudioFormat().toString());
                }
                if (session.getOutputAudioFormat() != null) {
                    outputAudioFormat.set(session.getOutputAudioFormat().toString());
                }
            }
        }
    }

    private void trackRecvCounters(SessionUpdate update) {
        // Track session ID from session.created / session.updated
        if (update instanceof SessionUpdateSessionCreated) {
            VoiceLiveSessionResponse session = ((SessionUpdateSessionCreated) update).getSession();
            if (session != null && session.getId() != null) {
                sessionId.set(session.getId());
            }
        }
        if (update instanceof SessionUpdateSessionUpdated) {
            VoiceLiveSessionResponse session = ((SessionUpdateSessionUpdated) update).getSession();
            if (session != null && session.getId() != null) {
                sessionId.set(session.getId());
            }
        }

        // Track audio format from session.created / session.updated responses
        if (update instanceof SessionUpdateSessionCreated) {
            updateAudioFormatsFromResponse(((SessionUpdateSessionCreated) update).getSession());
        }
        if (update instanceof SessionUpdateSessionUpdated) {
            updateAudioFormatsFromResponse(((SessionUpdateSessionUpdated) update).getSession());
        }

        // Track audio bytes received from response.audio.delta
        if (update instanceof SessionUpdateResponseAudioDelta) {
            SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) update;
            byte[] delta = audioDelta.getDelta();
            if (delta != null) {
                audioBytesReceived.addAndGet(delta.length);
            }

            // First-token latency: measure from response.create to first audio delta
            long createTs = responseCreateTimestampNanos.get();
            if (createTs > 0 && firstTokenLatencyMs.get() < 0) {
                long elapsed = (System.nanoTime() - createTs) / 1_000_000;
                firstTokenLatencyMs.compareAndSet(-1, elapsed);
            }
        }

        // Track turn count from response.done
        if (update instanceof SessionUpdateResponseDone) {
            turnCount.incrementAndGet();
        }
    }

    private void trackRecvTokenUsage(SessionUpdate update, Span span) {
        if (update instanceof SessionUpdateResponseDone) {
            SessionResponse response = ((SessionUpdateResponseDone) update).getResponse();
            if (response != null) {
                ResponseTokenStatistics usage = response.getUsage();
                if (usage != null) {
                    span.setAttribute(GEN_AI_USAGE_INPUT_TOKENS, (long) usage.getInputTokens());
                    span.setAttribute(GEN_AI_USAGE_OUTPUT_TOKENS, (long) usage.getOutputTokens());
                }
            }
        }
    }

    private void trackErrorEvents(SessionUpdate update, Span span) {
        if (update instanceof SessionUpdateError) {
            SessionUpdateError errorUpdate = (SessionUpdateError) update;
            SessionUpdateErrorDetails errorDetails = errorUpdate.getError();
            if (errorDetails != null) {
                // Set error status on the span
                String message = errorDetails.getMessage() != null ? errorDetails.getMessage() : "Unknown error";
                span.setStatus(StatusCode.ERROR, message);
                span.setAttribute(ERROR_TYPE, errorDetails.getType() != null ? errorDetails.getType() : "server_error");

                // Add error event with details
                AttributesBuilder errorAttrs = Attributes.builder().put(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE);
                if (errorDetails.getCode() != null) {
                    errorAttrs.put(ERROR_CODE, errorDetails.getCode());
                }
                if (errorDetails.getMessage() != null) {
                    errorAttrs.put(ERROR_MESSAGE, errorDetails.getMessage());
                }
                span.addEvent(GEN_AI_VOICE_ERROR, errorAttrs.build());
            }
        }
    }

    private void updateAudioFormatsFromResponse(VoiceLiveSessionResponse session) {
        if (session == null) {
            return;
        }
        if (session.getInputAudioFormat() != null) {
            inputAudioFormat.set(session.getInputAudioFormat().toString());
        }
        if (session.getOutputAudioFormat() != null) {
            outputAudioFormat.set(session.getOutputAudioFormat().toString());
        }
    }
}
