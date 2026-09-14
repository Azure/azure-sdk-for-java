// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentWebSocketClient;
import com.azure.ai.agents.VoiceAgentWebSocketSessionClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.RealtimeConversationItem;
import com.azure.ai.agents.models.RealtimeConversationItemType;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.RealtimeServerEventRealtimeServerEventError;
import com.azure.ai.agents.models.RealtimeServerEventResponseDone;
import com.azure.ai.agents.models.RealtimeServerEventResponseFunctionCallArgumentsDone;
import com.azure.ai.agents.models.RealtimeServerEventResponseTextDone;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.ai.agents.models.VoiceAgentFunctionTool;
import com.azure.ai.agents.models.VoiceAgentTool;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.ai.agents.models.VoiceOutputModality;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Demonstrates executing a client-side function tool during a live voice-agent session.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - Optional. The voice agent name. Defaults to
 *   {@code sample-voice-agent-function-tool-java}.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL} - Optional. The voice model. Defaults to {@code gpt-realtime}.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL_TYPE} - Optional. The voice model type. Defaults to {@code managed}.</li>
 * </ul>
 */
public class VoiceAgentLiveFunctionToolSample {
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(45);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME",
            "sample-voice-agent-function-tool-java");
        String model = configuration.get("FOUNDRY_VOICE_MODEL", "gpt-realtime");
        VoiceModelType modelType = VoiceModelType.fromString(configuration.get(
            "FOUNDRY_VOICE_MODEL_TYPE", VoiceModelType.MANAGED.toString()));

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true);
        AgentsClient agents = builder.buildAgentsClient();
        BetaVoiceAgentWebSocketClient realtime = builder.buildBetaVoiceAgentWebSocketClient();

        Map<String, Object> cityProperty = new LinkedHashMap<>();
        cityProperty.put("type", "string");
        cityProperty.put("description", "City name, for example Seattle.");
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("city", cityProperty);
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", Collections.singletonList("city"));

        VoiceAgentFunctionTool weatherTool = new VoiceAgentFunctionTool("get_weather")
            .setDescription("Get the current weather for a city.")
            .setParameters(BinaryData.fromObject(schema));
        VoiceAgentDefinition definition = new VoiceAgentDefinition()
            .setModelType(modelType)
            .setModel(model)
            .setInstructions("Use the get_weather tool when asked about weather, then answer using its result.")
            .setOutputModalities(Collections.singletonList(VoiceOutputModality.TEXT))
            .setTools(Collections.<VoiceAgentTool>singletonList(weatherTool));

        try {
            agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition));
            System.out.println("Created voice agent: " + agentName);
            try (VoiceAgentWebSocketSessionClient session = realtime.connect(agentName)) {
                ExecutorService receiver = Executors.newSingleThreadExecutor();
                Future<?> response = receiver.submit(() -> receiveResponse(session));
                try {
                    session.sendText("What's the weather like in Seattle right now?");
                    session.createResponse();
                    response.get(RESPONSE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
                } catch (TimeoutException error) {
                    System.out.println("Timed out waiting for the agent's reply; cancelling the active response.");
                    session.cancelResponse();
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                } catch (java.util.concurrent.ExecutionException error) {
                    throw new IllegalStateException("The realtime receive loop failed.", error.getCause());
                } finally {
                    response.cancel(true);
                    receiver.shutdownNow();
                }
            }
        } finally {
            agents.deleteAgent(agentName);
            System.out.println("Deleted voice agent: " + agentName);
        }
    }

    private static void receiveResponse(VoiceAgentWebSocketSessionClient session) {
        for (RealtimeServerEvent event : session.receiveEvents()) {
            if (event instanceof RealtimeServerEventResponseFunctionCallArgumentsDone) {
                RealtimeServerEventResponseFunctionCallArgumentsDone call
                    = (RealtimeServerEventResponseFunctionCallArgumentsDone) event;
                session.sendFunctionCallOutput(call.getCallId(), executeTool(call));
            } else if (event instanceof RealtimeServerEventResponseTextDone) {
                System.out.println("Agent: " + ((RealtimeServerEventResponseTextDone) event).getText());
            } else if (event instanceof RealtimeServerEventResponseDone) {
                if (!containsFunctionCall((RealtimeServerEventResponseDone) event)) {
                    return;
                }
            } else if (event instanceof RealtimeServerEventRealtimeServerEventError) {
                RealtimeServerEventRealtimeServerEventError error
                    = (RealtimeServerEventRealtimeServerEventError) event;
                System.out.println("Session error: " + error.getError().getMessage());
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static String executeTool(RealtimeServerEventResponseFunctionCallArgumentsDone call) {
        Map<String, Object> arguments = BinaryData.fromString(call.getArguments()).toObject(Map.class);
        System.out.printf("Tool call: %s(%s)%n", call.getName(), arguments);
        Map<String, Object> result = new LinkedHashMap<>();
        if ("get_weather".equals(call.getName())) {
            result.put("city", arguments.get("city"));
            result.put("condition", "sunny");
            result.put("temperature_f", 72);
        } else {
            result.put("error", "Unknown tool: " + call.getName());
        }
        return BinaryData.fromObject(result).toString();
    }

    private static boolean containsFunctionCall(RealtimeServerEventResponseDone event) {
        List<RealtimeConversationItem> output = event.getResponse().getOutput();
        if (output == null) {
            return false;
        }
        for (RealtimeConversationItem item : output) {
            if (item.getType() == RealtimeConversationItemType.FUNCTION_CALL) {
                return true;
            }
        }
        return false;
    }
}
