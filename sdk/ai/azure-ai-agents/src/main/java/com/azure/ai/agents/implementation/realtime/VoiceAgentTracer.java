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
import io.opentelemetry.api.common.AttributesBuilder;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
    static final AttributeKey<String> GEN_AI_SYSTEM_INSTRUCTIONS = AttributeKey.stringKey("gen_ai.system_instructions");
    static final AttributeKey<String> GEN_AI_REQUEST_TEMPERATURE = AttributeKey.stringKey("gen_ai.request.temperature");
    static final AttributeKey<String> GEN_AI_REQUEST_MAX_OUTPUT_TOKENS
        = AttributeKey.stringKey("gen_ai.request.max_output_tokens");
    static final AttributeKey<String> GEN_AI_REQUEST_TOOLS = AttributeKey.stringKey("gen_ai.request.tools");
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
    static final AttributeKey<String> GEN_AI_VOICE_CALL_ID = AttributeKey.stringKey("gen_ai.voice.call_id");
    static final AttributeKey<String> GEN_AI_VOICE_ITEM_ID = AttributeKey.stringKey("gen_ai.voice.item_id");
    static final AttributeKey<String> GEN_AI_VOICE_PREVIOUS_ITEM_ID
        = AttributeKey.stringKey("gen_ai.voice.previous_item_id");
    static final AttributeKey<Long> GEN_AI_VOICE_OUTPUT_INDEX = AttributeKey.longKey("gen_ai.voice.output_index");
    static final AttributeKey<Long> GEN_AI_VOICE_INPUT_SAMPLE_RATE
        = AttributeKey.longKey("gen_ai.voice.input_sample_rate");
    static final AttributeKey<String> GEN_AI_VOICE_INPUT_AUDIO_FORMAT
        = AttributeKey.stringKey("gen_ai.voice.input_audio_format");
    static final AttributeKey<String> GEN_AI_VOICE_OUTPUT_AUDIO_FORMAT
        = AttributeKey.stringKey("gen_ai.voice.output_audio_format");
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
    private final ReentrantReadWriteLock lifecycleLock = new ReentrantReadWriteLock();
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
    private final AtomicReference<String> systemInstructions = new AtomicReference<>();
    private final AtomicReference<String> requestTemperature = new AtomicReference<>();
    private final AtomicReference<String> requestMaxOutputTokens = new AtomicReference<>();
    private final AtomicReference<String> requestTools = new AtomicReference<>();
    private final AtomicReference<String> inputAudioFormat = new AtomicReference<>();
    private final AtomicReference<String> outputAudioFormat = new AtomicReference<>();
    private final AtomicLong inputSampleRate = new AtomicLong(-1);

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
        lifecycleLock.writeLock().lock();
        try {
            Span span = baseSpan(OPERATION_CONNECT, OPERATION_CONNECT).startSpan();
            if (connectSpan.compareAndSet(null, span)) {
                connectContext.set(Context.current().with(span));
            } else {
                span.end();
            }
        } finally {
            lifecycleLock.writeLock().unlock();
        }
    }

    /** Ends the connect span and records accumulated session values. */
    public void endConnectSpan(Throwable error) {
        lifecycleLock.writeLock().lock();
        try {
            Span span = connectSpan.getAndSet(null);
            connectContext.set(null);
            if (span == null) {
                return;
            }
            setIfPresent(span, GEN_AI_VOICE_SESSION_ID, sessionId.get());
            setIfPresent(span, GEN_AI_REQUEST_MODEL, model.get());
            setIfPresent(span, GEN_AI_CONVERSATION_ID, conversationId.get());
            setIfPresent(span, GEN_AI_RESPONSE_ID, responseId.get());
            setIfPresent(span, GEN_AI_SYSTEM_INSTRUCTIONS, systemInstructions.get());
            setIfPresent(span, GEN_AI_REQUEST_TEMPERATURE, requestTemperature.get());
            setIfPresent(span, GEN_AI_REQUEST_MAX_OUTPUT_TOKENS, requestMaxOutputTokens.get());
            setIfPresent(span, GEN_AI_REQUEST_TOOLS, requestTools.get());
            setIfPresent(span, GEN_AI_VOICE_INPUT_AUDIO_FORMAT, inputAudioFormat.get());
            setIfPresent(span, GEN_AI_VOICE_OUTPUT_AUDIO_FORMAT, outputAudioFormat.get());
            if (inputSampleRate.get() >= 0) {
                span.setAttribute(GEN_AI_VOICE_INPUT_SAMPLE_RATE, inputSampleRate.get());
            }
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
        } finally {
            lifecycleLock.writeLock().unlock();
        }
    }

    /** Records a typed client event as a child send span. */
    public void traceSend(RealtimeClientEvent event, String payload) {
        if (!beginTrace()) {
            return;
        }
        try {
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
            trackSessionConfiguration(payload);
            trackResponseStart(eventType);
            traceEvent(OPERATION_SEND, eventType, payload, INPUT_EVENT, null);
        } finally {
            endTrace();
        }
    }

    /** Records an untyped client event as a child send span. */
    public void traceSendRaw(String payload) {
        if (!beginTrace()) {
            return;
        }
        try {
            String eventType = eventType(payload);
            trackSessionConfiguration(payload);
            trackResponseStart(eventType);
            traceEvent(OPERATION_SEND, eventType, payload, INPUT_EVENT, null);
        } finally {
            endTrace();
        }
    }

    /** Records a typed server event as a child receive span. */
    public void traceReceive(RealtimeServerEvent event, String payload) {
        if (!beginTrace()) {
            return;
        }
        try {
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
            trackSessionConfiguration(payload);
            trackFirstToken(eventType);
            if (isHighVolumeDelta(eventType)) {
                return;
            }
            traceEvent(OPERATION_RECV, eventType, payload, OUTPUT_EVENT, event);
        } finally {
            endTrace();
        }
    }

    /** Records an untyped server event as a child receive span. */
    public void traceReceiveRaw(String payload) {
        if (!beginTrace()) {
            return;
        }
        try {
            String eventType = eventType(payload);
            trackSessionConfiguration(payload);
            trackFirstToken(eventType);
            if (!isHighVolumeDelta(eventType)) {
                traceEvent(OPERATION_RECV, eventType, payload, OUTPUT_EVENT, null);
            }
        } finally {
            endTrace();
        }
    }

    /** Records a close operation as a child span. */
    public void traceClose() {
        if (!beginTrace()) {
            return;
        }
        try {
            if (!closeTraced.compareAndSet(false, true)) {
                return;
            }
            Span span = childSpan(OPERATION_CLOSE, OPERATION_CLOSE).startSpan();
            span.end();
        } finally {
            endTrace();
        }
    }

    private boolean beginTrace() {
        lifecycleLock.readLock().lock();
        if (connectContext.get() != null) {
            return true;
        }
        lifecycleLock.readLock().unlock();
        return false;
    }

    private void endTrace() {
        lifecycleLock.readLock().unlock();
    }

    private void traceEvent(String operation, String eventType, String payload, String contentEvent,
        RealtimeServerEvent serverEvent) {
        Span span = childSpan(operation + " " + eventType, operation).setAttribute(GEN_AI_VOICE_EVENT_TYPE, eventType)
            .setAttribute(GEN_AI_VOICE_MESSAGE_SIZE,
                payload == null ? 0 : payload.getBytes(StandardCharsets.UTF_8).length)
            .startSpan();
        AttributesBuilder eventAttributes
            = Attributes.builder().put(GEN_AI_SYSTEM, SYSTEM_VALUE).put(GEN_AI_VOICE_EVENT_TYPE, eventType);
        if (captureContent && payload != null) {
            eventAttributes.put(EVENT_CONTENT, payload);
        }
        span.addEvent(contentEvent, eventAttributes.build());
        trackCorrelationAttributes(span, payload);
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

    private void trackSessionConfiguration(String payload) {
        Map<?, ?> root = payloadObject(payload);
        if (root == null) {
            return;
        }
        String type = stringValue(root.get("type"));
        if (!RealtimeClientEventType.SESSION_UPDATE.toString().equals(type)
            && !RealtimeServerEventType.SESSION_CREATED.toString().equals(type)
            && !RealtimeServerEventType.SESSION_UPDATED.toString().equals(type)) {
            return;
        }
        Map<?, ?> session = objectMap(root.get("session"));
        if (session == null) {
            return;
        }
        if (session.containsKey("temperature")) {
            requestTemperature.set(stringValue(session.get("temperature")));
        }
        if (session.containsKey("max_output_tokens") || session.containsKey("max_response_output_tokens")) {
            Object maxTokens = session.get("max_output_tokens");
            if (maxTokens == null) {
                maxTokens = session.get("max_response_output_tokens");
            }
            requestMaxOutputTokens.set(stringValue(maxTokens));
        }
        if (captureContent) {
            if (session.containsKey("instructions")) {
                systemInstructions.set(stringValue(session.get("instructions")));
            }
            if (session.containsKey("tools")) {
                Object tools = session.get("tools");
                requestTools.set(tools == null ? null : BinaryData.fromObject(tools).toString());
            }
        }

        Map<?, ?> audio = objectMap(session.get("audio"));
        Map<?, ?> input = audio == null ? null : objectMap(audio.get("input"));
        Map<?, ?> output = audio == null ? null : objectMap(audio.get("output"));
        Map<?, ?> inputFormat = input == null ? null : objectMap(input.get("format"));
        Object inputFormatValue
            = inputFormat == null ? (input == null ? null : input.get("format")) : inputFormat.get("type");
        Map<?, ?> outputFormat = output == null ? null : objectMap(output.get("format"));
        Object outputFormatValue
            = outputFormat == null ? (output == null ? null : output.get("format")) : outputFormat.get("type");
        if (input != null && input.containsKey("format")) {
            inputAudioFormat.set(stringValue(inputFormatValue));
        }
        if (output != null && output.containsKey("format")) {
            outputAudioFormat.set(stringValue(outputFormatValue));
        }

        Object sampleRate = inputFormat == null ? null : inputFormat.get("rate");
        if (sampleRate == null) {
            sampleRate = session.get("input_audio_sampling_rate");
        }
        Long parsedSampleRate = longValue(sampleRate);
        if (parsedSampleRate != null) {
            inputSampleRate.set(parsedSampleRate);
        }
    }

    private void trackCorrelationAttributes(Span span, String payload) {
        Map<?, ?> root = payloadObject(payload);
        if (root == null) {
            return;
        }
        setIfPresent(span, GEN_AI_RESPONSE_ID, stringValue(root.get("response_id")));
        setIfPresent(span, GEN_AI_VOICE_ITEM_ID, stringValue(root.get("item_id")));
        setIfPresent(span, GEN_AI_VOICE_CALL_ID, stringValue(root.get("call_id")));
        setIfPresent(span, GEN_AI_VOICE_PREVIOUS_ITEM_ID, stringValue(root.get("previous_item_id")));
        Long outputIndex = longValue(root.get("output_index"));
        if (outputIndex != null) {
            span.setAttribute(GEN_AI_VOICE_OUTPUT_INDEX, outputIndex);
        }

        Map<?, ?> item = objectMap(root.get("item"));
        if (item != null) {
            setIfPresent(span, GEN_AI_VOICE_ITEM_ID, stringValue(item.get("id")));
            setIfPresent(span, GEN_AI_VOICE_CALL_ID, stringValue(item.get("call_id")));
        }
        Map<?, ?> response = objectMap(root.get("response"));
        if (response != null) {
            setIfPresent(span, GEN_AI_RESPONSE_ID, stringValue(response.get("id")));
            String responseConversationId = stringValue(response.get("conversation_id"));
            setIfPresent(span, GEN_AI_CONVERSATION_ID, responseConversationId);
            if (responseConversationId != null) {
                conversationId.set(responseConversationId);
            }
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
        Map<?, ?> root = payloadObject(payload);
        String type = root == null ? null : stringValue(root.get("type"));
        return type == null ? extractEventType(payload) : type;
    }

    private static String extractEventType(String payload) {
        if (payload == null) {
            return "unknown";
        }
        int typeIndex = payload.indexOf("\"type\":");
        if (typeIndex < 0) {
            return "unknown";
        }
        int startQuote = payload.indexOf('"', typeIndex + 7);
        if (startQuote < 0) {
            return "unknown";
        }
        int endQuote = payload.indexOf('"', startQuote + 1);
        return endQuote < 0 ? "unknown" : payload.substring(startQuote + 1, endQuote);
    }

    private static Map<?, ?> payloadObject(String payload) {
        if (payload == null) {
            return null;
        }
        try {
            return BinaryData.fromString(payload).toObject(Map.class);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Map<?, ?> objectMap(Object value) {
        return value instanceof Map ? (Map<?, ?>) value : null;
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static Long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? null : Long.valueOf(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
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
