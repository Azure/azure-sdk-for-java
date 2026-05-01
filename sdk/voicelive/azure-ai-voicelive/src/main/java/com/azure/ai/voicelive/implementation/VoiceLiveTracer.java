// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.implementation;

import java.net.URI;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.azure.ai.voicelive.models.ClientEvent;
import com.azure.ai.voicelive.models.ClientEventInputAudioBufferAppend;
import com.azure.ai.voicelive.models.ClientEventResponseCancel;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.ClientEventType;
import com.azure.ai.voicelive.models.ResponseTokenStatistics;
import com.azure.ai.voicelive.models.SessionResponse;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemCreated;
import com.azure.ai.voicelive.models.SessionUpdateError;
import com.azure.ai.voicelive.models.SessionUpdateErrorDetails;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioTranscriptDelta;
import com.azure.ai.voicelive.models.SessionUpdateResponseCreated;
import com.azure.ai.voicelive.models.SessionUpdateResponseDone;
import com.azure.ai.voicelive.models.SessionUpdateResponseFunctionCallArgumentsDelta;
import com.azure.ai.voicelive.models.SessionUpdateResponseFunctionCallArgumentsDone;
import com.azure.ai.voicelive.models.SessionUpdateResponseOutputItemAdded;
import com.azure.ai.voicelive.models.SessionUpdateResponseOutputItemDone;
import com.azure.ai.voicelive.models.SessionUpdateResponseTextDelta;
import com.azure.ai.voicelive.models.SessionUpdateSessionCreated;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.ai.voicelive.models.VoiceLiveSessionResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.logging.ClientLogger;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

/**
 * Tracer for VoiceLive WebSocket sessions using the OpenTelemetry API.
 * <p>
 * Manages a parent "connect" span for the session lifetime, with child spans for
 * send, recv, and close operations. Tracks session-level counters for audio bytes,
 * turn counts, interruptions, and first-token latency.
 * </p>
 */
public final class VoiceLiveTracer {

    private static final String SDK_NAME = "azure-ai-voicelive";

    /**
     * Creates a {@link VoiceLiveTracer} backed by the global OpenTelemetry instance.
     * Returns null if no real OpenTelemetry is configured (noop).
     *
     * @param endpoint The WebSocket endpoint URI.
     * @param model The model name, used for tracing span names.
     * @param enableContentRecording Whether to record content in traces.
     * @return A new VoiceLiveTracer instance, or null if tracing is not configured.
     */
    public static VoiceLiveTracer create(URI endpoint, String model, Boolean enableContentRecording) {
        OpenTelemetry otel = GlobalOpenTelemetry.getOrNoop();
        if (otel == null || otel == OpenTelemetry.noop()) {
            return null;
        }
        Tracer tracer = otel.getTracer(SDK_NAME);
        io.opentelemetry.api.metrics.Meter meter = otel.getMeter(SDK_NAME);
        return new VoiceLiveTracer(tracer, meter, endpoint, model, enableContentRecording);
    }

    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveTracer.class);

    // GenAI semantic convention attribute keys
    static final AttributeKey<String> GEN_AI_SYSTEM = AttributeKey.stringKey("gen_ai.system");
    static final String GEN_AI_SYSTEM_VALUE = "az.ai.voicelive";
    static final AttributeKey<String> GEN_AI_OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");
    static final AttributeKey<String> GEN_AI_REQUEST_MODEL = AttributeKey.stringKey("gen_ai.request.model");
    static final AttributeKey<String> GEN_AI_PROVIDER_NAME = AttributeKey.stringKey("gen_ai.provider.name");
    static final String GEN_AI_PROVIDER_NAME_VALUE = "microsoft.foundry";
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
    static final AttributeKey<Double> GEN_AI_VOICE_FIRST_TOKEN_LATENCY_MS
        = AttributeKey.doubleKey("gen_ai.voice.first_token_latency_ms");
    static final AttributeKey<String> GEN_AI_VOICE_EVENT_TYPE = AttributeKey.stringKey("gen_ai.voice.event_type");
    static final AttributeKey<Long> GEN_AI_VOICE_MESSAGE_SIZE = AttributeKey.longKey("gen_ai.voice.message_size");
    static final AttributeKey<Long> GEN_AI_USAGE_INPUT_TOKENS = AttributeKey.longKey("gen_ai.usage.input_tokens");
    static final AttributeKey<Long> GEN_AI_USAGE_OUTPUT_TOKENS = AttributeKey.longKey("gen_ai.usage.output_tokens");
    static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");

    // Function call attribute keys
    static final AttributeKey<String> GEN_AI_VOICE_CALL_ID = AttributeKey.stringKey("gen_ai.voice.call_id");
    static final AttributeKey<String> GEN_AI_VOICE_PREVIOUS_ITEM_ID
        = AttributeKey.stringKey("gen_ai.voice.previous_item_id");
    static final AttributeKey<String> GEN_AI_VOICE_ITEM_ID = AttributeKey.stringKey("gen_ai.voice.item_id");
    static final AttributeKey<Long> GEN_AI_VOICE_OUTPUT_INDEX = AttributeKey.longKey("gen_ai.voice.output_index");

    // Agent attribute keys
    static final AttributeKey<String> GEN_AI_AGENT_NAME = AttributeKey.stringKey("gen_ai.agent.name");
    static final AttributeKey<String> GEN_AI_AGENT_VERSION = AttributeKey.stringKey("gen_ai.agent.version");
    static final AttributeKey<String> GEN_AI_AGENT_PROJECT_NAME = AttributeKey.stringKey("gen_ai.agent.project_name");
    static final AttributeKey<String> GEN_AI_AGENT_ID = AttributeKey.stringKey("gen_ai.agent.id");
    static final AttributeKey<String> GEN_AI_AGENT_THREAD_ID = AttributeKey.stringKey("gen_ai.agent.thread_id");
    static final AttributeKey<String> GEN_AI_CONVERSATION_ID = AttributeKey.stringKey("gen_ai.conversation.id");

    // Session config attribute keys (tracked on connect span)
    static final AttributeKey<String> GEN_AI_SYSTEM_INSTRUCTIONS = AttributeKey.stringKey("gen_ai.system_instructions");
    static final AttributeKey<String> GEN_AI_REQUEST_TEMPERATURE = AttributeKey.stringKey("gen_ai.request.temperature");
    static final AttributeKey<String> GEN_AI_REQUEST_MAX_OUTPUT_TOKENS
        = AttributeKey.stringKey("gen_ai.request.max_output_tokens");
    static final AttributeKey<Long> GEN_AI_VOICE_INPUT_SAMPLE_RATE
        = AttributeKey.longKey("gen_ai.voice.input_sample_rate");
    static final AttributeKey<String> GEN_AI_REQUEST_TOOLS = AttributeKey.stringKey("gen_ai.request.tools");

    // Response attribute keys
    static final AttributeKey<String> GEN_AI_RESPONSE_ID = AttributeKey.stringKey("gen_ai.response.id");
    static final AttributeKey<String> GEN_AI_RESPONSE_FINISH_REASONS
        = AttributeKey.stringKey("gen_ai.response.finish_reasons");

    // Rate limit event keys
    static final AttributeKey<String> GEN_AI_VOICE_RATE_LIMITS = AttributeKey.stringKey("gen_ai.voice.rate_limits");
    static final String GEN_AI_VOICE_RATE_LIMITS_UPDATED = "gen_ai.voice.rate_limits.updated";

    // Span event names
    static final String GEN_AI_INPUT_MESSAGES = "gen_ai.input.messages";
    static final String GEN_AI_OUTPUT_MESSAGES = "gen_ai.output.messages";
    static final String GEN_AI_VOICE_ERROR = "gen_ai.voice.error";

    // Event attribute keys
    static final AttributeKey<String> EVENT_CONTENT = AttributeKey.stringKey("gen_ai.event.content");
    static final AttributeKey<String> ERROR_CODE = AttributeKey.stringKey("error.code");
    static final AttributeKey<String> ERROR_MESSAGE = AttributeKey.stringKey("error.message");

    // Operation name values
    static final String OPERATION_CONNECT = "connect";
    static final String OPERATION_SEND = "send";
    static final String OPERATION_RECV = "recv";
    static final String OPERATION_CLOSE = "close";

    // Raw event type constants
    static final String EVENT_TYPE_UNKNOWN = "unknown";
    static final String EVENT_TYPE_SESSION_UPDATE = ClientEventType.SESSION_UPDATE.toString();
    static final String EVENT_TYPE_RATE_LIMITS_UPDATED = "rate_limits.updated";

    // Content recording configuration
    private static final ConfigurationProperty<Boolean> CAPTURE_MESSAGE_CONTENT
        = ConfigurationPropertyBuilder.ofBoolean("azure.tracing.gen_ai.content_recording_enabled")
            .environmentVariableName("AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED")
            .systemPropertyName("azure.tracing.gen_ai.content_recording_enabled")
            .shared(true)
            .defaultValue(false)
            .build();

    private final Tracer tracer;
    private final io.opentelemetry.api.metrics.Meter meter;
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

    // Agent attributes - from AgentSessionConfig + session.created response
    private volatile String agentName;
    private volatile String agentVersion;
    private volatile String agentProjectName;
    private volatile String clientConversationId; // from AgentSessionConfig
    private final AtomicReference<String> agentId = new AtomicReference<>();
    private final AtomicReference<String> agentThreadId = new AtomicReference<>();
    private final AtomicReference<String> conversationId = new AtomicReference<>(); // from server

    // Session config attributes - tracked from session.update events
    private volatile String systemInstructions;
    private volatile String requestTemperature;
    private volatile String requestMaxOutputTokens;
    private volatile Long inputAudioSamplingRate;
    private volatile String requestTools;

    // Last response metadata - accumulated for connect span (Python parity)
    private final AtomicReference<String> lastResponseId = new AtomicReference<>();
    private final AtomicReference<String> lastFinishReasons = new AtomicReference<>();

    /**
     * Creates a VoiceLiveTracer.
     *
     * @param tracer The OpenTelemetry Tracer instance (may be a no-op tracer).
     * @param meter The OpenTelemetry Meter instance (may be a no-op meter).
     * @param endpoint The WebSocket endpoint URI.
     * @param model The model name.
     * @param captureContentOverride Optional override for content recording (null = use env var).
     */
    VoiceLiveTracer(Tracer tracer, io.opentelemetry.api.metrics.Meter meter, URI endpoint, String model,
        Boolean captureContentOverride) {
        this.tracer = tracer;
        this.meter = meter;
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
    public void startConnectSpan() {
        String spanName = model != null ? "connect " + model : "connect";

        SpanBuilder spanBuilder = tracer.spanBuilder(spanName)
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
            .setAttribute(GEN_AI_OPERATION_NAME, OPERATION_CONNECT)
            .setAttribute(AZ_NAMESPACE, AZ_NAMESPACE_VALUE)
            .setAttribute(GEN_AI_PROVIDER_NAME, GEN_AI_PROVIDER_NAME_VALUE);

        if (model != null) {
            spanBuilder.setAttribute(GEN_AI_REQUEST_MODEL, model);
        }
        if (serverAddress != null) {
            spanBuilder.setAttribute(SERVER_ADDRESS, serverAddress);
            if (serverPort != -1) {
                spanBuilder.setAttribute(SERVER_PORT, (long) serverPort);
            }
        }

        Span span = spanBuilder.startSpan();
        Context ctx = Context.current().with(span);
        connectSpan.set(span);
        connectContext.set(ctx);
    }

    /**
     * Starts the parent "connect" span for the session lifetime with agent configuration.
     *
     * @param config The agent session configuration.
     */
    public void startConnectSpan(com.azure.ai.voicelive.models.AgentSessionConfig config) {
        // Store agent config for apply to connect span on close
        if (config != null) {
            this.agentName = config.getAgentName();
            this.agentVersion = config.getAgentVersion();
            this.agentProjectName = config.getProjectName();
            this.clientConversationId = config.getConversationId();
        }

        // Start the base connect span
        startConnectSpan();
    }

    /**
     * Ends the connect span, flushing session-level counters as attributes.
     *
     * @param error The error that caused the session to close, or null.
     */
    public void endConnectSpan(Throwable error) {
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
            span.setAttribute(GEN_AI_VOICE_FIRST_TOKEN_LATENCY_MS, (double) latency);
        }

        // Flush agent attributes
        if (agentName != null) {
            span.setAttribute(GEN_AI_AGENT_NAME, agentName);
        }
        if (agentVersion != null) {
            span.setAttribute(GEN_AI_AGENT_VERSION, agentVersion);
        }
        if (agentProjectName != null) {
            span.setAttribute(GEN_AI_AGENT_PROJECT_NAME, agentProjectName);
        }
        String aid = agentId.get();
        if (aid != null) {
            span.setAttribute(GEN_AI_AGENT_ID, aid);
        }
        String tid = agentThreadId.get();
        if (tid != null) {
            span.setAttribute(GEN_AI_AGENT_THREAD_ID, tid);
        }
        String cid = conversationId.get();
        if (cid == null) {
            cid = clientConversationId;
        }
        if (cid != null) {
            span.setAttribute(GEN_AI_CONVERSATION_ID, cid);
        }

        // Flush session config attributes
        if (systemInstructions != null) {
            span.setAttribute(GEN_AI_SYSTEM_INSTRUCTIONS, systemInstructions);
        }
        if (requestTemperature != null) {
            span.setAttribute(GEN_AI_REQUEST_TEMPERATURE, requestTemperature);
        }
        if (requestMaxOutputTokens != null) {
            span.setAttribute(GEN_AI_REQUEST_MAX_OUTPUT_TOKENS, requestMaxOutputTokens);
        }
        if (inputAudioSamplingRate != null) {
            span.setAttribute(GEN_AI_VOICE_INPUT_SAMPLE_RATE, inputAudioSamplingRate);
        }
        if (requestTools != null) {
            span.setAttribute(GEN_AI_REQUEST_TOOLS, requestTools);
        }
        String rid = lastResponseId.get();
        if (rid != null) {
            span.setAttribute(GEN_AI_RESPONSE_ID, rid);
        }
        String fr = lastFinishReasons.get();
        if (fr != null) {
            span.setAttribute(GEN_AI_RESPONSE_FINISH_REASONS, fr);
        }

        if (error != null) {
            span.setStatus(StatusCode.ERROR, error.getMessage());
            span.recordException(error);
            span.setAttribute(ERROR_TYPE, error.getClass().getCanonicalName());
        }

        span.end();
    }

    // ============================================================================
    // Common child span builder
    // ============================================================================

    /**
     * Creates a SpanBuilder with all common attributes for child spans, matching Python SDK parity:
     * gen_ai.system, gen_ai.operation.name, az.namespace, gen_ai.provider.name,
     * server.address, server.port, gen_ai.request.model, gen_ai.voice.session_id,
     * gen_ai.conversation.id (when available).
     */
    private SpanBuilder childSpanBuilder(String spanName, String operationName) {
        SpanBuilder builder = tracer.spanBuilder(spanName)
            .setSpanKind(SpanKind.CLIENT)
            .setParent(connectContext.get())
            .setAttribute(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
            .setAttribute(GEN_AI_OPERATION_NAME, operationName)
            .setAttribute(AZ_NAMESPACE, AZ_NAMESPACE_VALUE)
            .setAttribute(GEN_AI_PROVIDER_NAME, GEN_AI_PROVIDER_NAME_VALUE);

        if (model != null) {
            builder.setAttribute(GEN_AI_REQUEST_MODEL, model);
        }
        if (serverAddress != null) {
            builder.setAttribute(SERVER_ADDRESS, serverAddress);
            if (serverPort != -1) {
                builder.setAttribute(SERVER_PORT, (long) serverPort);
            }
        }
        String sid = sessionId.get();
        if (sid != null) {
            builder.setAttribute(GEN_AI_VOICE_SESSION_ID, sid);
        }
        String cid = conversationId.get();
        if (cid == null) {
            cid = clientConversationId;
        }
        if (cid != null) {
            builder.setAttribute(GEN_AI_CONVERSATION_ID, cid);
        }
        return builder;
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
    public void traceSend(ClientEvent event, String jsonPayload) {
        Context parentCtx = connectContext.get();
        if (parentCtx == null) {
            return;
        }

        String eventType = event.getType() != null ? event.getType().toString() : EVENT_TYPE_UNKNOWN;
        String spanName = OPERATION_SEND + " " + eventType;

        Span span
            = childSpanBuilder(spanName, OPERATION_SEND).setAttribute(GEN_AI_VOICE_EVENT_TYPE, eventType).startSpan();

        try {
            if (span.isRecording()) {
                if (jsonPayload != null) {
                    span.setAttribute(GEN_AI_VOICE_MESSAGE_SIZE, (long) jsonPayload.length());
                }

                // Track function call output attributes on the span
                if (event instanceof com.azure.ai.voicelive.models.ClientEventConversationItemCreate) {
                    com.azure.ai.voicelive.models.ClientEventConversationItemCreate createEvent
                        = (com.azure.ai.voicelive.models.ClientEventConversationItemCreate) event;
                    String prevItemId = createEvent.getPreviousItemId();
                    if (prevItemId != null) {
                        span.setAttribute(GEN_AI_VOICE_PREVIOUS_ITEM_ID, prevItemId);
                    }
                    com.azure.ai.voicelive.models.ConversationRequestItem item = createEvent.getItem();
                    if (item instanceof com.azure.ai.voicelive.models.FunctionCallOutputItem) {
                        String callId = ((com.azure.ai.voicelive.models.FunctionCallOutputItem) item).getCallId();
                        if (callId != null) {
                            span.setAttribute(GEN_AI_VOICE_CALL_ID, callId);
                        }
                    }
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
                trackSendCounters(event, jsonPayload);
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
    public void traceRecv(SessionUpdate update, String rawPayload) {
        Context parentCtx = connectContext.get();
        if (parentCtx == null) {
            return;
        }

        // Skip high-volume text/transcript delta events to reduce telemetry noise.
        // These carry incremental fragments with no counters to track.
        // Matches Python SDK's _DELTA_SKIP_EVENT_TYPES.
        if (update instanceof SessionUpdateResponseTextDelta
            || update instanceof SessionUpdateResponseAudioTranscriptDelta) {
            return;
        }

        String eventType = update.getType() != null ? update.getType().toString() : EVENT_TYPE_UNKNOWN;
        String spanName = OPERATION_RECV + " " + eventType;

        Span span
            = childSpanBuilder(spanName, OPERATION_RECV).setAttribute(GEN_AI_VOICE_EVENT_TYPE, eventType).startSpan();

        try {
            if (span.isRecording()) {
                if (rawPayload != null) {
                    span.setAttribute(GEN_AI_VOICE_MESSAGE_SIZE, (long) rawPayload.length());
                }

                // Track per-message token usage from response.done
                trackRecvTokenUsage(update, span);

                // Track response metadata (id, conversation_id, finish_reasons)
                trackResponseMetadata(update, span);

                // Track item_id, call_id, output_index, response_id on recv spans
                trackRecvItemAttributes(update, span);

                // Add span event for output messages
                AttributesBuilder eventAttrs = Attributes.builder()
                    .put(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
                    .put(GEN_AI_VOICE_EVENT_TYPE, eventType);
                if (captureContent && rawPayload != null) {
                    eventAttrs.put(EVENT_CONTENT, rawPayload);
                }
                span.addEvent(GEN_AI_OUTPUT_MESSAGES, eventAttrs.build());

                // Track error events — add event but don't set error status
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
    public void traceClose() {
        Context parentCtx = connectContext.get();
        if (parentCtx == null) {
            return;
        }

        Span span = childSpanBuilder(OPERATION_CLOSE, OPERATION_CLOSE).startSpan();
        span.end();
    }

    /**
     * Traces a raw receive operation. Used when a message fails to parse or for raw events.
     *
     * @param rawPayload The raw JSON payload string.
     */
    public void traceRecvRaw(String rawPayload) {
        Context parentCtx = connectContext.get();
        if (parentCtx == null) {
            return;
        }

        // Try to extract event type from raw JSON
        String eventType = extractEventType(rawPayload);

        Span span = childSpanBuilder(OPERATION_RECV + " " + eventType, OPERATION_RECV)
            .setAttribute(GEN_AI_VOICE_EVENT_TYPE, eventType)
            .startSpan();

        try {
            if (span.isRecording()) {
                if (rawPayload != null) {
                    span.setAttribute(GEN_AI_VOICE_MESSAGE_SIZE, (long) rawPayload.length());
                    if (captureContent) {
                        AttributesBuilder eventAttrs = Attributes.builder()
                            .put(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
                            .put(GEN_AI_VOICE_EVENT_TYPE, eventType);
                        eventAttrs.put(EVENT_CONTENT, rawPayload);
                        span.addEvent(GEN_AI_OUTPUT_MESSAGES, eventAttrs.build());
                    }

                    // Handle rate_limits.updated events - add a special span event with rate limit info
                    if (EVENT_TYPE_RATE_LIMITS_UPDATED.equals(eventType)) {
                        String rateLimitsJson = extractRateLimits(rawPayload);
                        if (rateLimitsJson != null) {
                            Attributes rateLimitAttrs = Attributes.of(GEN_AI_VOICE_RATE_LIMITS, rateLimitsJson);
                            span.addEvent(GEN_AI_VOICE_RATE_LIMITS_UPDATED, rateLimitAttrs);
                        }
                    }
                }
            }
        } finally {
            span.end();
        }
    }

    /**
     * Extracts the event type from a raw JSON payload.
     */
    private static String extractEventType(String rawPayload) {
        if (rawPayload == null) {
            return EVENT_TYPE_UNKNOWN;
        }
        // Simple extraction: find "type":"..." in JSON
        int typeIndex = rawPayload.indexOf("\"type\":");
        if (typeIndex < 0) {
            return EVENT_TYPE_UNKNOWN;
        }
        int startQuote = rawPayload.indexOf('"', typeIndex + 7);
        if (startQuote < 0) {
            return EVENT_TYPE_UNKNOWN;
        }
        int endQuote = rawPayload.indexOf('"', startQuote + 1);
        if (endQuote < 0) {
            return "unknown";
        }
        return rawPayload.substring(startQuote + 1, endQuote);
    }

    /**
     * Extracts the rate_limits array JSON string from a rate_limits.updated payload.
     */
    private static String extractRateLimits(String rawPayload) {
        if (rawPayload == null) {
            return null;
        }
        int rateLimitsKey = rawPayload.indexOf("\"rate_limits\":");
        if (rateLimitsKey < 0) {
            return null;
        }
        int arrayStart = rawPayload.indexOf('[', rateLimitsKey);
        if (arrayStart < 0) {
            return null;
        }
        // Find matching closing bracket
        int depth = 0;
        for (int i = arrayStart; i < rawPayload.length(); i++) {
            char c = rawPayload.charAt(i);
            if (c == '[' || c == '{') {
                depth++;
            } else if (c == ']' || c == '}') {
                depth--;
                if (depth == 0) {
                    return rawPayload.substring(arrayStart, i + 1);
                }
            }
        }
        return rawPayload.substring(arrayStart);
    }

    // ============================================================================
    // Counter Tracking
    // ============================================================================

    private void trackSendCounters(ClientEvent event, String jsonPayload) {
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

        // Track audio format from session.update (from typed object)
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
                if (session.getInputAudioSamplingRate() != null) {
                    inputAudioSamplingRate = session.getInputAudioSamplingRate().longValue();
                }
            }
        }

        // Parse session config from session.update JSON for tracking on connect span
        if (jsonPayload != null && EVENT_TYPE_SESSION_UPDATE.equals(extractEventType(jsonPayload))) {
            parseAndTrackSessionUpdateConfig(jsonPayload);
        }
    }

    /**
     * Parses session.update JSON payload and tracks config attributes on the connect span.
     * These are accumulated and applied when the connect span ends.
     */
    private void parseAndTrackSessionUpdateConfig(String jsonPayload) {
        if (jsonPayload == null) {
            return;
        }
        // Extract instruction
        String instructions = extractJsonStringField(jsonPayload, "instructions");
        if (instructions != null) {
            this.systemInstructions = instructions;
        }
        // Extract temperature
        String temperature = extractJsonNumberField(jsonPayload, "temperature");
        if (temperature != null) {
            this.requestTemperature = temperature;
        }
        // Extract max_response_output_tokens
        String maxTokens = extractJsonNumberField(jsonPayload, "max_response_output_tokens");
        if (maxTokens != null) {
            this.requestMaxOutputTokens = maxTokens;
        }
        // Extract input_audio_sampling_rate
        String samplingRate = extractJsonNumberField(jsonPayload, "input_audio_sampling_rate");
        if (samplingRate != null) {
            try {
                this.inputAudioSamplingRate = Long.parseLong(samplingRate.split("\\.")[0]);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        // Extract input_audio_format
        String inAudioFormat = extractJsonStringField(jsonPayload, "input_audio_format");
        if (inAudioFormat != null) {
            this.inputAudioFormat.set(inAudioFormat);
        }
        // Extract output_audio_format
        String outAudioFormat = extractJsonStringField(jsonPayload, "output_audio_format");
        if (outAudioFormat != null) {
            this.outputAudioFormat.set(outAudioFormat);
        }
        // Extract tools array
        int toolsIndex = jsonPayload.indexOf("\"tools\":");
        if (toolsIndex >= 0) {
            int arrayStart = jsonPayload.indexOf('[', toolsIndex);
            if (arrayStart >= 0) {
                int depth = 0;
                for (int i = arrayStart; i < jsonPayload.length(); i++) {
                    char c = jsonPayload.charAt(i);
                    if (c == '[' || c == '{') {
                        depth++;
                    } else if (c == ']' || c == '}') {
                        depth--;
                        if (depth == 0) {
                            this.requestTools = jsonPayload.substring(arrayStart, i + 1);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Extracts a string field value from a JSON string without fully parsing it.
     */
    private static String extractJsonStringField(String json, String fieldName) {
        String key = "\"" + fieldName + "\":\"";
        int idx = json.indexOf(key);
        if (idx < 0) {
            return null;
        }
        int start = idx + key.length();
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '\\') {
                end += 2; // skip escaped char
            } else if (c == '"') {
                break;
            } else {
                end++;
            }
        }
        if (end > start) {
            return json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return null;
    }

    /**
     * Extracts a numeric field value from a JSON string without fully parsing it.
     */
    private static String extractJsonNumberField(String json, String fieldName) {
        String key = "\"" + fieldName + "\":";
        int idx = json.indexOf(key);
        if (idx < 0) {
            return null;
        }
        int start = idx + key.length();
        // Skip whitespace
        while (start < json.length() && json.charAt(start) == ' ') {
            start++;
        }
        if (start >= json.length()) {
            return null;
        }
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (Character.isDigit(c) || c == '.' || c == '-') {
                end++;
            } else {
                break;
            }
        }
        if (end > start) {
            return json.substring(start, end);
        }
        return null;
    }

    private void trackRecvCounters(SessionUpdate update) {
        // Track session ID from session.created / session.updated
        if (update instanceof SessionUpdateSessionCreated) {
            VoiceLiveSessionResponse session = ((SessionUpdateSessionCreated) update).getSession();
            if (session != null && session.getId() != null) {
                sessionId.set(session.getId());
            }
            // Track agent info if present
            if (session != null && session.getAgent() != null) {
                com.azure.ai.voicelive.models.RespondingAgentOptions agent = session.getAgent();
                if (agent.getAgentId() != null) {
                    agentId.set(agent.getAgentId());
                }
                if (agent.getThreadId() != null) {
                    agentThreadId.set(agent.getThreadId());
                }
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

            // Track conversation_id from response.done
            SessionResponse response = ((SessionUpdateResponseDone) update).getResponse();
            if (response != null && response.getConversationId() != null) {
                conversationId.set(response.getConversationId());
            }
        }

        // Track conversation_id from response.created
        if (update instanceof SessionUpdateResponseCreated) {
            SessionResponse response = ((SessionUpdateResponseCreated) update).getResponse();
            if (response != null && response.getConversationId() != null) {
                conversationId.set(response.getConversationId());
            }
        }
    }

    /**
     * Tracks response metadata (id, conversation_id, finish_reasons) on the recv span.
     */
    private void trackResponseMetadata(SessionUpdate update, Span span) {
        if (update instanceof SessionUpdateResponseDone) {
            SessionResponse response = ((SessionUpdateResponseDone) update).getResponse();
            if (response != null) {
                if (response.getId() != null) {
                    span.setAttribute(GEN_AI_RESPONSE_ID, response.getId());
                }
                if (response.getConversationId() != null) {
                    span.setAttribute(GEN_AI_CONVERSATION_ID, response.getConversationId());
                }
                if (response.getStatus() != null) {
                    String fr = "[\"" + response.getStatus().toString() + "\"]";
                    span.setAttribute(GEN_AI_RESPONSE_FINISH_REASONS, fr);
                    lastFinishReasons.set(fr);
                }
                // Accumulate last response.id for connect span
                lastResponseId.set(response.getId());
            }
        }
        if (update instanceof SessionUpdateResponseCreated) {
            SessionResponse response = ((SessionUpdateResponseCreated) update).getResponse();
            if (response != null) {
                if (response.getId() != null) {
                    span.setAttribute(GEN_AI_RESPONSE_ID, response.getId());
                }
                if (response.getConversationId() != null) {
                    span.setAttribute(GEN_AI_CONVERSATION_ID, response.getConversationId());
                }
            }
        }
    }

    /**
     * Tracks item_id, call_id, output_index, and response_id on recv spans for
     * conversation.item.created, response.output_item.added/done,
     * and response.function_call_arguments.delta/done events.
     */
    private void trackRecvItemAttributes(SessionUpdate update, Span span) {
        // conversation.item.created -> item_id
        if (update instanceof SessionUpdateConversationItemCreated) {
            SessionUpdateConversationItemCreated itemCreated = (SessionUpdateConversationItemCreated) update;
            if (itemCreated.getItem() != null && itemCreated.getItem().getId() != null) {
                span.setAttribute(GEN_AI_VOICE_ITEM_ID, itemCreated.getItem().getId());
            }
        }
        // response.output_item.added -> item_id, response_id, output_index, conversation_id
        if (update instanceof SessionUpdateResponseOutputItemAdded) {
            SessionUpdateResponseOutputItemAdded outputAdded = (SessionUpdateResponseOutputItemAdded) update;
            if (outputAdded.getItem() != null && outputAdded.getItem().getId() != null) {
                span.setAttribute(GEN_AI_VOICE_ITEM_ID, outputAdded.getItem().getId());
            }
            if (outputAdded.getResponseId() != null) {
                span.setAttribute(GEN_AI_RESPONSE_ID, outputAdded.getResponseId());
            }
            span.setAttribute(GEN_AI_VOICE_OUTPUT_INDEX, (long) outputAdded.getOutputIndex());
        }
        // response.output_item.done -> item_id, response_id, output_index
        if (update instanceof SessionUpdateResponseOutputItemDone) {
            SessionUpdateResponseOutputItemDone outputDone = (SessionUpdateResponseOutputItemDone) update;
            if (outputDone.getItem() != null && outputDone.getItem().getId() != null) {
                span.setAttribute(GEN_AI_VOICE_ITEM_ID, outputDone.getItem().getId());
            }
            if (outputDone.getResponseId() != null) {
                span.setAttribute(GEN_AI_RESPONSE_ID, outputDone.getResponseId());
            }
            span.setAttribute(GEN_AI_VOICE_OUTPUT_INDEX, (long) outputDone.getOutputIndex());
        }
        // response.function_call_arguments.delta -> item_id, response_id, call_id, output_index
        if (update instanceof SessionUpdateResponseFunctionCallArgumentsDelta) {
            SessionUpdateResponseFunctionCallArgumentsDelta delta
                = (SessionUpdateResponseFunctionCallArgumentsDelta) update;
            if (delta.getItemId() != null) {
                span.setAttribute(GEN_AI_VOICE_ITEM_ID, delta.getItemId());
            }
            if (delta.getResponseId() != null) {
                span.setAttribute(GEN_AI_RESPONSE_ID, delta.getResponseId());
            }
            if (delta.getCallId() != null) {
                span.setAttribute(GEN_AI_VOICE_CALL_ID, delta.getCallId());
            }
            span.setAttribute(GEN_AI_VOICE_OUTPUT_INDEX, (long) delta.getOutputIndex());
        }
        // response.function_call_arguments.done -> item_id, response_id, call_id, output_index
        if (update instanceof SessionUpdateResponseFunctionCallArgumentsDone) {
            SessionUpdateResponseFunctionCallArgumentsDone done
                = (SessionUpdateResponseFunctionCallArgumentsDone) update;
            if (done.getItemId() != null) {
                span.setAttribute(GEN_AI_VOICE_ITEM_ID, done.getItemId());
            }
            if (done.getResponseId() != null) {
                span.setAttribute(GEN_AI_RESPONSE_ID, done.getResponseId());
            }
            if (done.getCallId() != null) {
                span.setAttribute(GEN_AI_VOICE_CALL_ID, done.getCallId());
            }
            span.setAttribute(GEN_AI_VOICE_OUTPUT_INDEX, (long) done.getOutputIndex());
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
                // Note: We add an error event but do NOT set error status on the span.
                // Error status is only set on endConnectSpan() when passed a Throwable.
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
        if (session.getInputAudioSamplingRate() != null) {
            inputAudioSamplingRate = session.getInputAudioSamplingRate().longValue();
        }
    }
}
