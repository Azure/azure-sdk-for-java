// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.core.JsonValue;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.FunctionTool;
import com.openai.models.live.ResponseItemCreateEvent;
import com.openai.models.live.ServerEvent;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseStreamEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

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
            BetaVoiceAgentsLiveWebSocketSessionClient session = agent.builder.beta()
                .buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession(agent.name)) {
            runConversation(session,
                Collections.singletonList(args.length == 0 ? "What is the weather in Seattle?" : args[0]),
                GptLiveFunctionToolSample::toolResult);
        }
    }

    static void runConversation(BetaVoiceAgentsLiveWebSocketSessionClient session, List<String> prompts,
        Function<ResponseFunctionToolCall, ClientEvent> toolHandler) {
        Iterator<String> pending = prompts.iterator();
        List<ClientEvent> results = new ArrayList<>();
        boolean started = false;
        boolean awaitingResponse = false;
        boolean closing = false;
        int continuations = 0;
        for (ServerEvent event : session.receiveEvents(GptLiveSampleUtils.TIMEOUT)) {
            GptLiveSampleUtils.printEvent(event);
            boolean nextTurn = false;
            if (event.isSessionStarted()) {
                if (started) {
                    throw new IllegalStateException("Duplicate session.started event.");
                }
                started = true;
                nextTurn = true;
            } else if (event.isResponseEvent()) {
                ResponseStreamEvent response = GptLiveSampleUtils.backendEvent(event);
                if (response.isOutputItemDone() && response.asOutputItemDone().item().isFunctionCall()) {
                    ResponseFunctionToolCall call = response.asOutputItemDone().item().asFunctionCall();
                    results.add(toolHandler.apply(call));
                } else if (response.isCompleted()) {
                    if (!started || !awaitingResponse || closing) {
                        throw new IllegalStateException("Unexpected GPT Live response lifecycle event.");
                    }
                    if (!results.isEmpty()) {
                        if (++continuations > 8) {
                            throw new IllegalStateException("Sample tool continuation limit exceeded.");
                        }
                        for (ClientEvent result : results) {
                            session.sendEvent(result);
                        }
                        results.clear();
                        session.sendEvent(GptLiveSampleUtils.responseCreate());
                    } else {
                        continuations = 0;
                        awaitingResponse = false;
                        System.out.println();
                        nextTurn = true;
                    }
                }
            } else if (event.isSessionClosed()) {
                String reason = event.asSessionClosed().reason().toString();
                if (!(started && "remote_hangup".equals(reason))
                    && !(closing && "close_requested".equals(reason))) {
                    throw new IllegalStateException("GPT Live closed before all turns completed. Event: " + event
                        + "; transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason());
                }
                System.out.println("Live session closed (" + reason + ").");
                return;
            }
            if (nextTurn) {
                if (pending.hasNext()) {
                    String prompt = pending.next();
                    System.out.println("You: " + prompt);
                    System.out.print("Assistant: ");
                    awaitingResponse = true;
                    session.sendEvent(GptLiveSampleUtils.userMessage(prompt));
                    session.sendEvent(GptLiveSampleUtils.responseCreate());
                } else {
                    closing = true;
                    session.sendEvent(GptLiveSampleUtils.closeSession());
                }
            }
        }
        throw new IllegalStateException("GPT Live disconnected without session.closed.");
    }

    static FunctionTool weatherTool() {
        FunctionTool.Parameters schema = FunctionTool.Parameters.builder()
            .putAdditionalProperty("type", JsonValue.from("object"))
            .putAdditionalProperty("properties", JsonValue.from(
                Collections.singletonMap("city", Collections.singletonMap("type", "string"))))
            .putAdditionalProperty("required", JsonValue.from(Collections.singletonList("city")))
            .putAdditionalProperty("additionalProperties", JsonValue.from(false))
            .build();
        return FunctionTool.builder().name("get_weather")
            .description("Get simulated weather for a city. This sample returns demonstration data only.")
            .parameters(schema)
            .strict(true)
            .build();
    }

    static ClientEvent toolResult(ResponseFunctionToolCall call) {
        if (!"get_weather".equals(call.name())) {
            throw new IllegalArgumentException("Unsupported sample tool: " + call.name());
        }
        Map<?, ?> arguments = object(
            BinaryData.fromString(call.arguments()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("city", string(arguments, "city"));
        result.put("condition", "sunny");
        result.put("temperature_f", 72);
        result.put("simulated", true);
        return ClientEvent.ofResponseItemCreate(ResponseItemCreateEvent.builder()
            .item(ResponseInputItem.FunctionCallOutput.builder()
                .callId(call.callId())
                .output(BinaryData.fromObject(result).toString())
                .build())
            .build());
    }

    static Map<?, ?> object(BinaryData data) {
        return data.toObject(Map.class);
    }

    static Map<?, ?> object(Object value) {
        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Expected a native Live JSON object: " + value);
        }
        return (Map<?, ?>) value;
    }

    static String string(Map<?, ?> event, String field) {
        Object value = event.get(field);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Missing or invalid Live event field: " + field);
        }
        return (String) value;
    }

}
