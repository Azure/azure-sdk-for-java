// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

/**
 * Constants for GenAI semantic convention attribute names and operation names.
 * <p>
 * These follow the OpenTelemetry GenAI semantic conventions:
 * <a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/">GenAI Semantic Conventions</a>
 */
final class GenAiConstants {

    private GenAiConstants() {
        // utility class
    }

    // --- Operation names ---
    static final String OPERATION_CREATE_AGENT = "create_agent";
    static final String OPERATION_INVOKE_AGENT = "invoke_agent";
    static final String OPERATION_CHAT = "chat";
    static final String OPERATION_CREATE_CONVERSATION = "create_conversation";

    // --- System / Provider ---
    static final String GEN_AI_SYSTEM = "gen_ai.system";
    static final String GEN_AI_SYSTEM_VALUE = "az.ai.agents";
    static final String GEN_AI_PROVIDER_NAME = "gen_ai.provider.name";
    static final String GEN_AI_PROVIDER_NAME_VALUE = "microsoft.foundry";
    static final String AZ_NAMESPACE = "az.namespace";
    static final String AZ_NAMESPACE_VALUE = "Microsoft.CognitiveServices";

    // --- Operation ---
    static final String GEN_AI_OPERATION_NAME = "gen_ai.operation.name";

    // --- Agent attributes ---
    static final String GEN_AI_AGENT_ID = "gen_ai.agent.id";
    static final String GEN_AI_AGENT_NAME = "gen_ai.agent.name";
    static final String GEN_AI_AGENT_DESCRIPTION = "gen_ai.agent.description";
    static final String GEN_AI_AGENT_VERSION = "gen_ai.agent.version";
    static final String GEN_AI_AGENT_TYPE = "gen_ai.agent.type";

    // --- Hosted agent attributes ---
    static final String GEN_AI_AGENT_HOSTED_CPU = "gen_ai.agent.hosted.cpu";
    static final String GEN_AI_AGENT_HOSTED_MEMORY = "gen_ai.agent.hosted.memory";
    static final String GEN_AI_AGENT_HOSTED_IMAGE = "gen_ai.agent.hosted.image";
    static final String GEN_AI_AGENT_HOSTED_PROTOCOL = "gen_ai.agent.hosted.protocol";
    static final String GEN_AI_AGENT_HOSTED_PROTOCOL_VERSION = "gen_ai.agent.hosted.protocol_version";

    // --- Request parameters ---
    static final String GEN_AI_REQUEST_MODEL = "gen_ai.request.model";
    static final String GEN_AI_REQUEST_TEMPERATURE = "gen_ai.request.temperature";
    static final String GEN_AI_REQUEST_TOP_P = "gen_ai.request.top_p";
    static final String GEN_AI_REQUEST_MAX_INPUT_TOKENS = "gen_ai.request.max_input_tokens";
    static final String GEN_AI_REQUEST_MAX_OUTPUT_TOKENS = "gen_ai.request.max_output_tokens";
    static final String GEN_AI_REQUEST_REASONING_EFFORT = "gen_ai.request.reasoning.effort";
    static final String GEN_AI_REQUEST_TOOLS = "gen_ai.request.tools";

    // --- Response attributes ---
    static final String GEN_AI_RESPONSE_MODEL = "gen_ai.response.model";
    static final String GEN_AI_RESPONSE_ID = "gen_ai.response.id";
    static final String GEN_AI_RESPONSE_FINISH_REASONS = "gen_ai.response.finish_reasons";

    // --- Token usage ---
    static final String GEN_AI_USAGE_INPUT_TOKENS = "gen_ai.usage.input_tokens";
    static final String GEN_AI_USAGE_OUTPUT_TOKENS = "gen_ai.usage.output_tokens";

    // --- Messages (content-gated) ---
    static final String GEN_AI_SYSTEM_INSTRUCTIONS = "gen_ai.system_instructions";
    static final String GEN_AI_INPUT_MESSAGES = "gen_ai.input.messages";
    static final String GEN_AI_OUTPUT_MESSAGES = "gen_ai.output.messages";

    // --- Conversation ---
    static final String GEN_AI_CONVERSATION_ID = "gen_ai.conversation.id";

    // --- Server ---
    static final String SERVER_ADDRESS = "server.address";
    static final String SERVER_PORT = "server.port";

    // --- Error ---
    static final String ERROR_TYPE = "error.type";

    // --- Events ---
    static final String GEN_AI_AGENT_WORKFLOW = "gen_ai.agent.workflow";
    static final String GEN_AI_EVENT_CONTENT = "gen_ai.event.content";
    static final String GEN_AI_WORKFLOW_ACTION = "gen_ai.workflow.action";

    // --- Token type (for metrics) ---
    static final String GEN_AI_TOKEN_TYPE = "gen_ai.token.type";
    static final String TOKEN_TYPE_INPUT = "input";
    static final String TOKEN_TYPE_OUTPUT = "output";

    // --- Metric names ---
    static final String METRIC_OPERATION_DURATION = "gen_ai.client.operation.duration";
    static final String METRIC_TOKEN_USAGE = "gen_ai.client.token.usage";

    // --- Metric units ---
    static final String METRIC_UNIT_SECONDS = "s";
    static final String METRIC_UNIT_TOKENS = "{token}";

    // --- Agent type values ---
    static final String AGENT_TYPE_PROMPT = "prompt";
    static final String AGENT_TYPE_HOSTED = "hosted";
    static final String AGENT_TYPE_WORKFLOW = "workflow";
    static final String AGENT_TYPE_UNKNOWN = "unknown";

    // --- Default port (HTTPS) ---
    static final int DEFAULT_HTTPS_PORT = 443;
}
