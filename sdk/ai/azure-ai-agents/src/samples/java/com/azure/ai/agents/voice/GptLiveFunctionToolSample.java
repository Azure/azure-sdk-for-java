// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentLiveWebSocketSessionClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.ResponseItemCreateEvent;
import com.openai.models.responses.ResponseInputItem;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Executes a sample weather function requested by the GPT Live Responses backend.
 * Weather results are explicitly simulated, not a call to a weather service.
 * Uses response.event envelopes and native response.item.create function_call_output items.
 * This is Responses delegation, not Foundry-managed-agent tool-result routing.
 * The uniquely named agent is deleted unless FOUNDRY_KEEP_GPT_LIVE_AGENT=true.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_MODEL} - Optional. The GPT Live deployment name. Defaults to {@code gpt-live-1}.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_BACKEND_MODEL} - Optional. The Responses backend deployment name in the Live
 *   resource. Defaults to {@code gpt-5.6-terra}.</li>
 *   <li>{@code FOUNDRY_KEEP_GPT_LIVE_AGENT} - Optional. Set to {@code true} to keep the agent after the sample.
 *   Defaults to {@code false}.</li>
 * </ul>
 */
public class GptLiveFunctionToolSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_GPT_LIVE_MODEL");
        String backendModel = configuration.get("FOUNDRY_GPT_LIVE_BACKEND_MODEL");
        boolean keepAgent = Boolean.parseBoolean(configuration.get("FOUNDRY_KEEP_GPT_LIVE_AGENT", "false"));

        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_PROJECT_ENDPOINT before running this sample.");
        }
        if (model == null) {
            model = "gpt-live-1";
        } else if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_GPT_LIVE_MODEL before running this sample.");
        }
        if (backendModel == null) {
            backendModel = "gpt-5.6-terra";
        } else if (backendModel.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_GPT_LIVE_BACKEND_MODEL before running this sample.");
        }

        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .allowPreview(true);
        try (GptLiveSampleUtils.AgentResource agent
            = GptLiveSampleUtils.createAgent(builder, model, backendModel, "tools", true, keepAgent);
            BetaVoiceAgentLiveWebSocketSessionClient session = agent.builder.beta()
                .buildBetaVoiceAgentLiveWebSocketClient().openWebSocketSession(agent.name)) {
            GptLiveSampleUtils.runToolConversation(session,
                Collections.singletonList(args.length == 0 ? "What is the weather in Seattle?" : args[0]),
                GptLiveFunctionToolSample::toolResult);
        }
    }

    static BinaryData weatherTool() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Collections.singletonMap("city", Collections.singletonMap("type", "string")));
        schema.put("required", Collections.singletonList("city"));
        schema.put("additionalProperties", false);
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("type", "function");
        tool.put("name", "get_weather");
        tool.put("description", "Get simulated weather for a city. This sample returns demonstration data only.");
        tool.put("parameters", schema);
        tool.put("strict", true);
        return BinaryData.fromObject(tool);
    }

    static ClientEvent toolResult(Map<?, ?> call) {
        if (!"get_weather".equals(GptLiveSampleUtils.string(call, "name"))) {
            throw new IllegalArgumentException("Unsupported sample tool: " + call.get("name"));
        }
        Map<?, ?> arguments = GptLiveSampleUtils.object(
            BinaryData.fromString(GptLiveSampleUtils.string(call, "arguments")));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("city", GptLiveSampleUtils.string(arguments, "city"));
        result.put("condition", "sunny");
        result.put("temperature_f", 72);
        result.put("simulated", true);
        return ClientEvent.ofResponseItemCreate(ResponseItemCreateEvent.builder()
            .item(ResponseInputItem.FunctionCallOutput.builder()
                .callId(GptLiveSampleUtils.string(call, "call_id"))
                .output(BinaryData.fromObject(result).toString())
                .build())
            .build());
    }

}
