// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.implementation.telemetry;

import io.opentelemetry.api.common.AttributeKey;

/**
 * OpenTelemetry attribute keys and semantic convention constants for VoiceLive tracing.
 */
public final class VoiceLiveTelemetryAttributeKeys {

    /** Attribute key for Azure namespace. */
    public static final AttributeKey<String> AZ_NAMESPACE = AttributeKey.stringKey("az.namespace");
    /** Azure namespace value for Cognitive Services. */
    public static final String AZ_NAMESPACE_VALUE = "Microsoft.CognitiveServices";

    /** Attribute key for the GenAI system identifier. */
    public static final AttributeKey<String> GEN_AI_SYSTEM = AttributeKey.stringKey("gen_ai.system");
    /** GenAI system value for VoiceLive. */
    public static final String GEN_AI_SYSTEM_VALUE = "az.ai.voicelive";

    /** Attribute key for the GenAI provider name. */
    public static final AttributeKey<String> GEN_AI_PROVIDER_NAME = AttributeKey.stringKey("gen_ai.provider.name");
    /** GenAI provider value. */
    public static final String GEN_AI_PROVIDER_VALUE = "microsoft.foundry";

    /** Attribute key for the GenAI operation name. */
    public static final AttributeKey<String> GEN_AI_OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");
    /** Attribute key for the GenAI request model. */
    public static final AttributeKey<String> GEN_AI_REQUEST_MODEL = AttributeKey.stringKey("gen_ai.request.model");
    /** Attribute key for the GenAI response ID. */
    public static final AttributeKey<String> GEN_AI_RESPONSE_ID = AttributeKey.stringKey("gen_ai.response.id");
    /** Attribute key for the GenAI response finish reasons. */
    public static final AttributeKey<String> GEN_AI_RESPONSE_FINISH_REASONS
        = AttributeKey.stringKey("gen_ai.response.finish_reasons");
    /** Attribute key for GenAI input token usage. */
    public static final AttributeKey<Long> GEN_AI_USAGE_INPUT_TOKENS
        = AttributeKey.longKey("gen_ai.usage.input_tokens");
    /** Attribute key for GenAI output token usage. */
    public static final AttributeKey<Long> GEN_AI_USAGE_OUTPUT_TOKENS
        = AttributeKey.longKey("gen_ai.usage.output_tokens");
    /** Attribute key for the GenAI conversation ID. */
    public static final AttributeKey<String> GEN_AI_CONVERSATION_ID = AttributeKey.stringKey("gen_ai.conversation.id");
    /** Attribute key for GenAI system instructions. */
    public static final AttributeKey<String> GEN_AI_SYSTEM_INSTRUCTIONS
        = AttributeKey.stringKey("gen_ai.system_instructions");
    /** Attribute key for GenAI request temperature. */
    public static final AttributeKey<String> GEN_AI_REQUEST_TEMPERATURE
        = AttributeKey.stringKey("gen_ai.request.temperature");
    /** Attribute key for GenAI request max output tokens. */
    public static final AttributeKey<String> GEN_AI_REQUEST_MAX_OUTPUT_TOKENS
        = AttributeKey.stringKey("gen_ai.request.max_output_tokens");
    /** Attribute key for GenAI request tools. */
    public static final AttributeKey<String> GEN_AI_REQUEST_TOOLS = AttributeKey.stringKey("gen_ai.request.tools");

    /** Attribute key for GenAI agent name. */
    public static final AttributeKey<String> GEN_AI_AGENT_NAME = AttributeKey.stringKey("gen_ai.agent.name");
    /** Attribute key for GenAI agent ID. */
    public static final AttributeKey<String> GEN_AI_AGENT_ID = AttributeKey.stringKey("gen_ai.agent.id");
    /** Attribute key for GenAI agent thread ID. */
    public static final AttributeKey<String> GEN_AI_AGENT_THREAD_ID = AttributeKey.stringKey("gen_ai.agent.thread_id");
    /** Attribute key for GenAI agent version. */
    public static final AttributeKey<String> GEN_AI_AGENT_VERSION = AttributeKey.stringKey("gen_ai.agent.version");
    /** Attribute key for GenAI agent project name. */
    public static final AttributeKey<String> GEN_AI_AGENT_PROJECT_NAME
        = AttributeKey.stringKey("gen_ai.agent.project_name");

    /** Attribute key for server address. */
    public static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");
    /** Attribute key for server port. */
    public static final AttributeKey<Long> SERVER_PORT = AttributeKey.longKey("server.port");

    /** Attribute key for voice session ID. */
    public static final AttributeKey<String> GEN_AI_VOICE_SESSION_ID
        = AttributeKey.stringKey("gen_ai.voice.session_id");
    /** Attribute key for voice call ID. */
    public static final AttributeKey<String> GEN_AI_VOICE_CALL_ID = AttributeKey.stringKey("gen_ai.voice.call_id");
    /** Attribute key for voice item ID. */
    public static final AttributeKey<String> GEN_AI_VOICE_ITEM_ID = AttributeKey.stringKey("gen_ai.voice.item_id");
    /** Attribute key for the previous voice item ID. */
    public static final AttributeKey<String> GEN_AI_VOICE_PREVIOUS_ITEM_ID
        = AttributeKey.stringKey("gen_ai.voice.previous_item_id");
    /** Attribute key for voice output index. */
    public static final AttributeKey<Long> GEN_AI_VOICE_OUTPUT_INDEX
        = AttributeKey.longKey("gen_ai.voice.output_index");
    /** Attribute key for voice input sample rate. */
    public static final AttributeKey<Long> GEN_AI_VOICE_INPUT_SAMPLE_RATE
        = AttributeKey.longKey("gen_ai.voice.input_sample_rate");
    /** Attribute key for voice input audio format. */
    public static final AttributeKey<String> GEN_AI_VOICE_INPUT_AUDIO_FORMAT
        = AttributeKey.stringKey("gen_ai.voice.input_audio_format");
    /** Attribute key for voice output audio format. */
    public static final AttributeKey<String> GEN_AI_VOICE_OUTPUT_AUDIO_FORMAT
        = AttributeKey.stringKey("gen_ai.voice.output_audio_format");
    /** Attribute key for voice turn count. */
    public static final AttributeKey<Long> GEN_AI_VOICE_TURN_COUNT = AttributeKey.longKey("gen_ai.voice.turn_count");
    /** Attribute key for voice interruption count. */
    public static final AttributeKey<Long> GEN_AI_VOICE_INTERRUPTION_COUNT
        = AttributeKey.longKey("gen_ai.voice.interruption_count");
    /** Attribute key for voice audio bytes sent. */
    public static final AttributeKey<Long> GEN_AI_VOICE_AUDIO_BYTES_SENT
        = AttributeKey.longKey("gen_ai.voice.audio_bytes_sent");
    /** Attribute key for voice audio bytes received. */
    public static final AttributeKey<Long> GEN_AI_VOICE_AUDIO_BYTES_RECEIVED
        = AttributeKey.longKey("gen_ai.voice.audio_bytes_received");
    /** Attribute key for voice first token latency in milliseconds. */
    public static final AttributeKey<Double> GEN_AI_VOICE_FIRST_TOKEN_LATENCY_MS
        = AttributeKey.doubleKey("gen_ai.voice.first_token_latency_ms");
    /** Attribute key for voice event type. */
    public static final AttributeKey<String> GEN_AI_VOICE_EVENT_TYPE
        = AttributeKey.stringKey("gen_ai.voice.event_type");
    /** Attribute key for voice message size. */
    public static final AttributeKey<Long> GEN_AI_VOICE_MESSAGE_SIZE
        = AttributeKey.longKey("gen_ai.voice.message_size");

    /** Attribute key for MCP server label. */
    public static final AttributeKey<String> GEN_AI_VOICE_MCP_SERVER_LABEL
        = AttributeKey.stringKey("gen_ai.voice.mcp.server_label");
    /** Attribute key for MCP tool name. */
    public static final AttributeKey<String> GEN_AI_VOICE_MCP_TOOL_NAME
        = AttributeKey.stringKey("gen_ai.voice.mcp.tool_name");
    /** Attribute key for MCP approval request ID. */
    public static final AttributeKey<String> GEN_AI_VOICE_MCP_APPROVAL_REQUEST_ID
        = AttributeKey.stringKey("gen_ai.voice.mcp.approval_request_id");
    /** Attribute key for MCP approval flag. */
    public static final AttributeKey<Boolean> GEN_AI_VOICE_MCP_APPROVE
        = AttributeKey.booleanKey("gen_ai.voice.mcp.approve");
    /** Attribute key for MCP call count. */
    public static final AttributeKey<Long> GEN_AI_VOICE_MCP_CALL_COUNT
        = AttributeKey.longKey("gen_ai.voice.mcp.call_count");
    /** Attribute key for MCP list tools count. */
    public static final AttributeKey<Long> GEN_AI_VOICE_MCP_LIST_TOOLS_COUNT
        = AttributeKey.longKey("gen_ai.voice.mcp.list_tools_count");

    /** Attribute key for error type. */
    public static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");
    /** Attribute key for error message. */
    public static final AttributeKey<String> ERROR_MESSAGE = AttributeKey.stringKey("error.message");
    /** Attribute key for error code. */
    public static final AttributeKey<String> ERROR_CODE = AttributeKey.stringKey("error.code");
    /** Attribute key for GenAI event content. */
    public static final AttributeKey<String> GEN_AI_EVENT_CONTENT = AttributeKey.stringKey("gen_ai.event.content");
    /** Attribute key for voice rate limits. */
    public static final AttributeKey<String> GEN_AI_VOICE_RATE_LIMITS
        = AttributeKey.stringKey("gen_ai.voice.rate_limits");
    /** Attribute key for GenAI token type. */
    public static final AttributeKey<String> GEN_AI_TOKEN_TYPE = AttributeKey.stringKey("gen_ai.token.type");

    /** Span event name for GenAI input messages. */
    public static final String GEN_AI_INPUT_MESSAGES = "gen_ai.input.messages";
    /** Span event name for GenAI output messages. */
    public static final String GEN_AI_OUTPUT_MESSAGES = "gen_ai.output.messages";
    /** Span event name for GenAI system instructions. */
    public static final String GEN_AI_SYSTEM_INSTRUCTIONS_EVENT = "gen_ai.system.instructions";
    /** Span event name for voice error. */
    public static final String GEN_AI_VOICE_ERROR = "gen_ai.voice.error";
    /** Span event name for voice rate limits updated. */
    public static final String GEN_AI_VOICE_RATE_LIMITS_UPDATED = "gen_ai.voice.rate_limits.updated";

    /** Metric name for GenAI client operation duration. */
    public static final String GEN_AI_CLIENT_OPERATION_DURATION = "gen_ai.client.operation.duration";
    /** Metric name for GenAI client token usage. */
    public static final String GEN_AI_CLIENT_TOKEN_USAGE = "gen_ai.client.token.usage";

    private VoiceLiveTelemetryAttributeKeys() {
    }
}
