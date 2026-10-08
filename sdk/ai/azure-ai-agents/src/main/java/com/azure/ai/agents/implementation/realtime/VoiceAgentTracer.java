// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import com.azure.ai.agents.models.RealtimeClientEvent;
import com.azure.ai.agents.models.RealtimeClientEventType;
import com.azure.ai.agents.models.RealtimeErrorEvent;
import com.azure.ai.agents.models.RealtimeInputAudioBufferAppendEvent;
import com.azure.ai.agents.models.RealtimeResponseAudioDeltaEvent;
import com.azure.ai.agents.models.RealtimeResponseCancelEvent;
import com.azure.ai.agents.models.RealtimeResponseDoneEvent;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.RealtimeServerEventType;
import com.azure.ai.agents.models.RealtimeSessionCreatedEvent;
import com.azure.ai.agents.models.VoiceAgentRealtimeResponse;
import com.azure.ai.agents.models.VoiceAgentSessionResponseConfiguration;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import com.openai.models.realtime.RealtimeError;
import com.openai.models.realtime.RealtimeResponseUsage;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/** OpenTelemetry tracing for a voice-agent WebSocket session. */
public final class VoiceAgentTracer {
    static final AttributeKey<String> GEN_AI_SYSTEM = AttributeKey.stringKey("gen_ai.system");
    static final AttributeKey<String> GEN_AI_OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");
    static final AttributeKey<String> GEN_AI_PROVIDER_NAME = AttributeKey.stringKey("gen_ai.provider.name");
    static final AttributeKey<String> GEN_AI_AGENT_NAME = AttributeKey.stringKey("gen_ai.agent.name");
    static final AttributeKey<String> GEN_AI_REQUEST_MODEL = AttributeKey.stringKey("gen_ai.request.model");
    static final AttributeKey<String> GEN_AI_RESPONSE_ID = AttributeKey.stringKey("gen_ai.response.id");
    static final AttributeKey<List<String>> GEN_AI_RESPONSE_FINISH_REASONS
        = AttributeKey.stringArrayKey("gen_ai.response.finish_reasons");
    static final AttributeKey<Long> GEN_AI_USAGE_INPUT_TOKENS = AttributeKey.longKey("gen_ai.usage.input_tokens");
    static final AttributeKey<Long> GEN_AI_USAGE_OUTPUT_TOKENS = AttributeKey.longKey("gen_ai.usage.output_tokens");
    static final AttributeKey<String> GEN_AI_CONVERSATION_ID = AttributeKey.stringKey("gen_ai.conversation.id");
    static final AttributeKey<String> AZ_NAMESPACE = AttributeKey.stringKey("az.namespace");
    static final AttributeKey<String> GEN_AI_VOICE_SESSION_ID = AttributeKey.stringKey("gen_ai.voice.session_id");
    static final AttributeKey<String> GEN_AI_VOICE_EVENT_TYPE = AttributeKey.stringKey("gen_ai.voice.event_type");
    static final AttributeKey<Long> GEN_AI_VOICE_MESSAGE_SIZE = AttributeKey.longKey("gen_ai.voice.message_size");
    static final AttributeKey<Long> GEN_AI_VOICE_TURN_COUNT = AttributeKey.longKey("gen_ai.voice.turn_count");
    static final AttributeKey<Long> GEN_AI_VOICE_INTERRUPTION_COUNT
        = AttributeKey.longKey("gen_ai.voice.interruption_count");
    static final AttributeKey<Long> GEN_AI_VOICE_AUDIO_BYTES_SENT
        = AttributeKey.longKey("gen_ai.voice.audio_bytes_sent");
    static final AttributeKey<Long> GEN_AI_VOICE_AUDIO_BYTES_RECEIVED
        = AttributeKey.longKey("gen_ai.voice.audio_bytes_received");
    static final AttributeKey<Double> GEN_AI_VOICE_FIRST_TOKEN_LATENCY_MS
        = AttributeKey.doubleKey("gen_ai.voice.first_token_latency_ms");
    static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");
    static final AttributeKey<Long> SERVER_PORT = AttributeKey.longKey("server.port");
    static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");
    static final AttributeKey<String> ERROR_CODE = AttributeKey.stringKey("error.code");
    static final AttributeKey<String> ERROR_MESSAGE = AttributeKey.stringKey("error.message");
    static final AttributeKey<String> EVENT_CONTENT = AttributeKey.stringKey("gen_ai.event.content");
    static final AttributeKey<String> GEN_AI_VOICE_RATE_LIMITS = AttributeKey.stringKey("gen_ai.voice.rate_limits");

    static final String SYSTEM_VALUE = "az.ai.agents";
    static final String PROVIDER_VALUE = "microsoft.foundry";
    static final String AZ_NAMESPACE_VALUE = "Microsoft.CognitiveServices";
    static final String OPERATION_CONNECT = "connect";
    static final String OPERATION_SEND = "send";
    static final String OPERATION_RECV = "recv";
    static final String OPERATION_CLOSE = "close";
    static final String INPUT_EVENT = "gen_ai.input.messages";
    static final String OUTPUT_EVENT = "gen_ai.output.messages";
    static final String ERROR_EVENT = "gen_ai.voice.error";
    static final String RATE_LIMITS_EVENT = "gen_ai.voice.rate_limits.updated";

    private static final String SDK_NAME = "azure-ai-agents";
    private static final ConfigurationProperty<Boolean> OTEL_CONTENT_RECORDING
        = ConfigurationPropertyBuilder.ofBoolean("otel.instrumentation.genai.capture.message.content")
            .environmentVariableName("OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT")
            .systemPropertyName("otel.instrumentation.genai.capture.message.content")
            .shared(true)
            .build();
    private static final ConfigurationProperty<Boolean> AZURE_CONTENT_RECORDING
        = ConfigurationPropertyBuilder.ofBoolean("azure.tracing.gen_ai.content_recording_enabled")
            .environmentVariableName("AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED")
            .systemPropertyName("azure.tracing.gen_ai.content_recording_enabled")
            .shared(true)
            .defaultValue(false)
            .build();

    private final Tracer tracer;
    private final String agentName;
    private final String serverAddress;
    private final long serverPort;
    private final boolean captureContent;
    private final LongSupplier nanoTime;
    private final AtomicReference<Span> connectSpan = new AtomicReference<>();
    private final AtomicReference<Context> connectContext = new AtomicReference<>();
    private final AtomicReference<String> sessionId = new AtomicReference<>();
    private final AtomicReference<String> model = new AtomicReference<>();
    private final AtomicReference<String> conversationId = new AtomicReference<>();
    private final AtomicBoolean closeTraced = new AtomicBoolean();
    private final AtomicLong turnCount = new AtomicLong();
    private final AtomicLong interruptionCount = new AtomicLong();
    private final AtomicLong audioBytesSent = new AtomicLong();
    private final AtomicLong audioBytesReceived = new AtomicLong();
    private final AtomicLong responseCreateTimestampNanos = new AtomicLong();
    private final AtomicLong firstTokenLatencyMillis = new AtomicLong(-1);
    private final AtomicReference<String> responseId = new AtomicReference<>();
    private final AtomicReference<List<String>> finishReasons = new AtomicReference<>();

    /**
     * Creates tracing state backed by the globally registered OpenTelemetry instance.
     *
     * @param endpoint the WebSocket endpoint.
     * @param agentName the connected agent name.
     * @return the session tracer.
     */
    public static VoiceAgentTracer create(URI endpoint, String agentName) {
        return new VoiceAgentTracer(GlobalOpenTelemetry.getOrNoop().getTracer(SDK_NAME), endpoint, agentName,
            isContentRecordingEnabled());
    }

    VoiceAgentTracer(Tracer tracer, URI endpoint, String agentName, boolean captureContent) {
        this(tracer, endpoint, agentName, captureContent, System::nanoTime);
    }

    VoiceAgentTracer(Tracer tracer, URI endpoint, String agentName, boolean captureContent, LongSupplier nanoTime) {
        this.tracer = tracer;
        this.agentName = agentName;
        this.serverAddress = endpoint.getHost();
        this.serverPort = defaultPort(endpoint);
        this.captureContent = captureContent;
        this.nanoTime = nanoTime;
    }

    /** Starts the session-lifetime connect span. */
    public void startConnectSpan() {
        Span span = baseSpan(OPERATION_CONNECT, OPERATION_CONNECT).startSpan();
        if (connectSpan.compareAndSet(null, span)) {
            connectContext.set(Context.current().with(span));
        } else {
            span.end();
        }
    }

    /** Ends the connect span and records accumulated session values. */
    public void endConnectSpan(Throwable error) {
        Span span = connectSpan.getAndSet(null);
        connectContext.set(null);
        if (span == null) {
            return;
        }
        setIfPresent(span, GEN_AI_VOICE_SESSION_ID, sessionId.get());
        setIfPresent(span, GEN_AI_REQUEST_MODEL, model.get());
        setIfPresent(span, GEN_AI_CONVERSATION_ID, conversationId.get());
        setIfPresent(span, GEN_AI_RESPONSE_ID, responseId.get());
        List<String> responseFinishReasons = finishReasons.get();
        if (responseFinishReasons != null) {
            span.setAttribute(GEN_AI_RESPONSE_FINISH_REASONS, responseFinishReasons);
        }
        span.setAttribute(GEN_AI_VOICE_TURN_COUNT, turnCount.get());
        span.setAttribute(GEN_AI_VOICE_INTERRUPTION_COUNT, interruptionCount.get());
        span.setAttribute(GEN_AI_VOICE_AUDIO_BYTES_SENT, audioBytesSent.get());
        span.setAttribute(GEN_AI_VOICE_AUDIO_BYTES_RECEIVED, audioBytesReceived.get());
        long latencyMillis = firstTokenLatencyMillis.get();
        if (latencyMillis >= 0) {
            span.setAttribute(GEN_AI_VOICE_FIRST_TOKEN_LATENCY_MS, (double) latencyMillis);
        }
        if (error != null) {
            recordError(span, error);
        }
        span.end();
    }

    /** Records a typed client event as a child send span. */
    public void traceSend(RealtimeClientEvent event, String payload) {
        String eventType = event.getType() == null ? "unknown" : event.getType().toString();
        if (event instanceof RealtimeInputAudioBufferAppendEvent) {
            String audio = ((RealtimeInputAudioBufferAppendEvent) event).getAudio();
            if (audio != null) {
                try {
                    audioBytesSent.addAndGet(Base64.getDecoder().decode(audio).length);
                } catch (IllegalArgumentException ignored) {
                    // The service will report malformed base64; tracing must not change request behavior.
                }
            }
        } else if (event instanceof RealtimeResponseCancelEvent) {
            interruptionCount.incrementAndGet();
        }
        trackResponseStart(eventType);
        traceEvent(OPERATION_SEND, eventType, payload, INPUT_EVENT, null);
    }

    /** Records an untyped client event as a child send span. */
    public void traceSendRaw(String payload) {
        String eventType = eventType(payload);
        trackResponseStart(eventType);
        traceEvent(OPERATION_SEND, eventType, payload, INPUT_EVENT, null);
    }

    /** Records a typed server event as a child receive span. */
    public void traceReceive(RealtimeServerEvent event, String payload) {
        String eventType = event.getType() == null ? "unknown" : event.getType().toString();
        if (event instanceof RealtimeSessionCreatedEvent) {
            RealtimeSessionCreatedEvent created = (RealtimeSessionCreatedEvent) event;
            conversationId.compareAndSet(null, created.getConversationId());
            VoiceAgentSessionResponseConfiguration session
                = created.getSessionAsVoiceAgentSessionResponseConfiguration();
            if (session != null) {
                sessionId.compareAndSet(null, session.getId());
                model.compareAndSet(null, session.getModel());
            }
        } else if (event instanceof RealtimeResponseAudioDeltaEvent) {
            byte[] delta = ((RealtimeResponseAudioDeltaEvent) event).getDelta();
            if (delta != null) {
                audioBytesReceived.addAndGet(delta.length);
            }
        } else if (event instanceof RealtimeResponseDoneEvent) {
            turnCount.incrementAndGet();
        }
        trackFirstToken(eventType);
        if (isHighVolumeDelta(eventType)) {
            return;
        }
        traceEvent(OPERATION_RECV, eventType, payload, OUTPUT_EVENT, event);
    }

    /** Records an untyped server event as a child receive span. */
    public void traceReceiveRaw(String payload) {
        String eventType = eventType(payload);
        trackFirstToken(eventType);
        if (!isHighVolumeDelta(eventType)) {
            traceEvent(OPERATION_RECV, eventType, payload, OUTPUT_EVENT, null);
        }
    }

    /** Records a close operation as a child span. */
    public void traceClose() {
        if (!closeTraced.compareAndSet(false, true)) {
            return;
        }
        Span span = childSpan(OPERATION_CLOSE, OPERATION_CLOSE).startSpan();
        span.end();
    }

    private void traceEvent(String operation, String eventType, String payload, String contentEvent,
        RealtimeServerEvent serverEvent) {
        Span span = childSpan(operation + " " + eventType, operation).setAttribute(GEN_AI_VOICE_EVENT_TYPE, eventType)
            .setAttribute(GEN_AI_VOICE_MESSAGE_SIZE,
                payload == null ? 0 : payload.getBytes(StandardCharsets.UTF_8).length)
            .startSpan();
        if (captureContent && payload != null) {
            span.addEvent(contentEvent, Attributes.of(EVENT_CONTENT, payload));
        }
        if (serverEvent instanceof RealtimeErrorEvent) {
            recordServiceError(span, (RealtimeErrorEvent) serverEvent);
        } else if (serverEvent instanceof RealtimeResponseDoneEvent) {
            recordResponseDone(span, (RealtimeResponseDoneEvent) serverEvent);
        }
        if (OPERATION_RECV.equals(operation)
            && RealtimeServerEventType.RATE_LIMITS_UPDATED.toString().equals(eventType)) {
            String rateLimits = rateLimits(payload);
            if (rateLimits != null) {
                span.addEvent(RATE_LIMITS_EVENT, Attributes.of(GEN_AI_VOICE_RATE_LIMITS, rateLimits));
            }
        }
        span.end();
    }

    private SpanBuilder childSpan(String name, String operation) {
        SpanBuilder builder = baseSpan(name, operation);
        Context parent = connectContext.get();
        if (parent != null) {
            builder.setParent(parent);
        }
        setIfPresent(builder, GEN_AI_VOICE_SESSION_ID, sessionId.get());
        setIfPresent(builder, GEN_AI_REQUEST_MODEL, model.get());
        setIfPresent(builder, GEN_AI_CONVERSATION_ID, conversationId.get());
        return builder;
    }

    private SpanBuilder baseSpan(String name, String operation) {
        SpanBuilder builder = tracer.spanBuilder(name)
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute(GEN_AI_SYSTEM, SYSTEM_VALUE)
            .setAttribute(GEN_AI_OPERATION_NAME, operation)
            .setAttribute(GEN_AI_PROVIDER_NAME, PROVIDER_VALUE)
            .setAttribute(AZ_NAMESPACE, AZ_NAMESPACE_VALUE)
            .setAttribute(GEN_AI_AGENT_NAME, agentName)
            .setAttribute(SERVER_PORT, serverPort);
        if (serverAddress != null) {
            builder.setAttribute(SERVER_ADDRESS, serverAddress);
        }
        return builder;
    }

    private static void recordError(Span span, Throwable error) {
        span.setStatus(StatusCode.ERROR,
            error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
        span.setAttribute(ERROR_TYPE, error.getClass().getName());
        span.recordException(error);
    }

    private void recordServiceError(Span span, RealtimeErrorEvent errorEvent) {
        span.setStatus(StatusCode.ERROR);
        span.addEvent(ERROR_EVENT);
        RealtimeError error = errorEvent.getError();
        if (error == null) {
            return;
        }
        if (captureContent) {
            span.setStatus(StatusCode.ERROR, error.message());
            setIfPresent(span, ERROR_MESSAGE, error.message());
        }
        setIfPresent(span, ERROR_TYPE, error.type());
        error.code().ifPresent(code -> span.setAttribute(ERROR_CODE, code));
    }

    private void recordResponseDone(Span span, RealtimeResponseDoneEvent doneEvent) {
        VoiceAgentRealtimeResponse response = doneEvent.getResponse();
        if (response == null) {
            return;
        }
        setIfPresent(span, GEN_AI_RESPONSE_ID, response.getId());
        responseId.set(response.getId());
        if (response.getStatusDetails() != null && response.getStatusDetails().reason().isPresent()) {
            List<String> reasons
                = Collections.singletonList(response.getStatusDetails().reason().get().asString().toLowerCase());
            span.setAttribute(GEN_AI_RESPONSE_FINISH_REASONS, reasons);
            finishReasons.set(reasons);
        }
        RealtimeResponseUsage usage = response.getUsage();
        if (usage != null) {
            usage.inputTokens().ifPresent(tokens -> span.setAttribute(GEN_AI_USAGE_INPUT_TOKENS, tokens));
            usage.outputTokens().ifPresent(tokens -> span.setAttribute(GEN_AI_USAGE_OUTPUT_TOKENS, tokens));
        }
    }

    private void trackResponseStart(String eventType) {
        if (RealtimeClientEventType.RESPONSE_CREATE.toString().equals(eventType)) {
            responseCreateTimestampNanos.set(nanoTime.getAsLong());
            firstTokenLatencyMillis.set(-1);
        }
    }

    private void trackFirstToken(String eventType) {
        if (!RealtimeServerEventType.RESPONSE_OUTPUT_AUDIO_DELTA.toString().equals(eventType)
            && !RealtimeServerEventType.RESPONSE_OUTPUT_TEXT_DELTA.toString().equals(eventType)) {
            return;
        }
        long started = responseCreateTimestampNanos.get();
        if (started != 0 && firstTokenLatencyMillis.get() < 0) {
            long elapsedMillis = Math.max(0, (nanoTime.getAsLong() - started) / 1_000_000);
            firstTokenLatencyMillis.compareAndSet(-1, elapsedMillis);
        }
    }

    private static long defaultPort(URI endpoint) {
        if (endpoint.getPort() >= 0) {
            return endpoint.getPort();
        }
        String scheme = endpoint.getScheme();
        return "http".equalsIgnoreCase(scheme) || "ws".equalsIgnoreCase(scheme) ? 80 : 443;
    }

    private static String eventType(String payload) {
        try {
            Object type = BinaryData.fromString(payload).toObject(Map.class).get("type");
            return type == null ? "unknown" : type.toString();
        } catch (RuntimeException ignored) {
            return "unknown";
        }
    }

    private static String rateLimits(String payload) {
        try {
            Object rateLimits = BinaryData.fromString(payload).toObject(Map.class).get("rate_limits");
            return rateLimits == null ? null : BinaryData.fromObject(rateLimits).toString();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static boolean isHighVolumeDelta(String eventType) {
        return RealtimeServerEventType.CONVERSATION_ITEM_INPUT_AUDIO_TRANSCRIPTION_DELTA.toString().equals(eventType)
            || RealtimeServerEventType.RESPONSE_OUTPUT_AUDIO_DELTA.toString().equals(eventType)
            || RealtimeServerEventType.RESPONSE_OUTPUT_AUDIO_TRANSCRIPT_DELTA.toString().equals(eventType)
            || RealtimeServerEventType.RESPONSE_OUTPUT_TEXT_DELTA.toString().equals(eventType);
    }

    private static boolean isContentRecordingEnabled() {
        return isContentRecordingEnabled(Configuration.getGlobalConfiguration());
    }

    static boolean isContentRecordingEnabled(Configuration configuration) {
        Boolean standardValue = configuration.get(OTEL_CONTENT_RECORDING);
        if (standardValue != null) {
            return standardValue;
        }
        return configuration.get(AZURE_CONTENT_RECORDING);
    }

    private static void setIfPresent(Span span, AttributeKey<String> key, String value) {
        if (value != null) {
            span.setAttribute(key, value);
        }
    }

    private static void setIfPresent(SpanBuilder span, AttributeKey<String> key, String value) {
        if (value != null) {
            span.setAttribute(key, value);
        }
    }
}
