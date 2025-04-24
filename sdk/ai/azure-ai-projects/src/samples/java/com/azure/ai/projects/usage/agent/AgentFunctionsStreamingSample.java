// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects.usage.agent;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.AgentsClient;
import com.azure.ai.projects.models.Agent;
import com.azure.ai.projects.models.AgentStreamEvent;
import com.azure.ai.projects.models.AgentThread;
import com.azure.ai.projects.models.CreateAgentOptions;
import com.azure.ai.projects.models.CreateRunOptions;
import com.azure.ai.projects.models.FunctionDefinition;
import com.azure.ai.projects.models.FunctionToolDefinition;
import com.azure.ai.projects.models.MessageDeltaImageFileContent;
import com.azure.ai.projects.models.MessageDeltaTextContent;
import com.azure.ai.projects.models.MessageRole;
import com.azure.ai.projects.models.RequiredFunctionToolCall;
import com.azure.ai.projects.models.RequiredToolCall;
import com.azure.ai.projects.models.RunStatus;
import com.azure.ai.projects.models.SubmitToolOutputsAction;
import com.azure.ai.projects.models.ThreadMessage;
import com.azure.ai.projects.models.ThreadRun;
import com.azure.ai.projects.models.ToolOutput;
import com.azure.ai.projects.models.streaming.StreamMessageUpdate;
import com.azure.ai.projects.models.streaming.StreamRequiredAction;
import com.azure.ai.projects.models.streaming.StreamThreadRunCreation;
import com.azure.ai.projects.models.streaming.StreamUpdate;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class AgentFunctionsStreamingSample {

    @Test
    void functionsStreamingExample() {
        AgentsClient agentsClient
            = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .subscriptionId(Configuration.getGlobalConfiguration().get("SUBSCRIPTIONID", "subscriptionid"))
            .resourceGroupName(Configuration.getGlobalConfiguration().get("RESOURCEGROUPNAME", "resourcegroupname"))
            .projectName(Configuration.getGlobalConfiguration().get("PROJECTNAME", "projectname"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAgentsClient();

        // function tool definitions
        FunctionToolDefinition getUserFavoriteCityTool = new FunctionToolDefinition(
            new FunctionDefinition(
                "getUserFavoriteCity",
                BinaryData.fromObject(
                    new Object()
                )).setDescription("Gets the user's favorite city.")
        );

        FunctionToolDefinition getCityNicknameTool = new FunctionToolDefinition(
            new FunctionDefinition(
                "getCityNickname",
                BinaryData.fromObject(
                    mapOf(
                        "type", "object",
                        "properties", mapOf(
                            "location", mapOf(
                                "type", "string",
                                "description", "The city and state, e.g. San Francisco, CA")
                        ),
                        "required", new String[]{"location"}))
            ).setDescription("Gets the nickname of a city, e.g. 'LA' for 'Los Angeles, CA'.")
        );

        FunctionToolDefinition getCurrentWeatherAtLocationTool = new FunctionToolDefinition(
            new FunctionDefinition(
                "getCurrentWeatherAtLocation",
                BinaryData.fromObject(
                    mapOf(
                        "type", "object",
                        "properties", mapOf(
                            "location", mapOf(
                                "type", "string",
                                "description", "The city and state, e.g. San Francisco, CA"),
                            "unit", mapOf(
                                "type", "string",
                                "description", "temperature unit as c or f",
                                "enum", new String[]{"c", "f"})),
                        "required", new String[]{"location", "unit"}))
            ).setDescription("Gets the current weather at a provided location.")
        );

        // actual functions
        Supplier<String> getUserFavoriteCity = () -> "Seattle, WA";

        Function<String, String> getCityNickname = (location) -> {
            switch (location) {
                case "Seattle, WA":
                    return "The Emerald city";
                default:
                    return "No nickname available";
            }
        };

        BiFunction<String, String, String> getCurrentWeatherAtLocation = (location, unit) -> {
            switch (location) {
                case "Seattle, WA":
                    return unit == "f" ? "70f" : "21c";
                default:
                    return "unknown";
            }
        };

        // function resolver
        Function<RequiredToolCall, ToolOutput> getResolvedToolOutput = toolCall -> {
            if (toolCall instanceof RequiredFunctionToolCall) {
                try {
                    RequiredFunctionToolCall functionToolCall = (RequiredFunctionToolCall) toolCall;
                    String functionName = functionToolCall.getFunction().getName();
                    if (functionName.equals("getUserFavoriteCity")) {
                        return new ToolOutput().setToolCallId(functionToolCall.getId())
                            .setOutput(getUserFavoriteCity.get());
                    } else if (functionName.equals("getCityNickname")) {
                        String args = functionToolCall.getFunction().getArguments();

                        JsonNode root = new JsonMapper().readTree(args);
                        String location = String.valueOf(root.get("location").asText());
                        return new ToolOutput().setToolCallId(functionToolCall.getId())
                            .setOutput(getCityNickname.apply(location));

                    } else if (functionName.equals("getCurrentWeatherAtLocation")) {
                        String args = functionToolCall.getFunction().getArguments();

                        JsonNode root = new JsonMapper().readTree(args);
                        String location = String.valueOf(root.get("location").asText());
                        String unit = String.valueOf(root.get("unit").asText());
                        return new ToolOutput().setToolCallId(functionToolCall.getId())
                            .setOutput(getCurrentWeatherAtLocation.apply(location, unit));
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        };

        String agentName = "functions_streaming_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a weather bot. Use the provided functions to help answer questions. "
                + "Customize your responses to the user's preferences as much as possible and use friendly "
                + "nicknames for cities whenever possible.")
            .setTools(Arrays.asList(getUserFavoriteCityTool, getCityNicknameTool, getCurrentWeatherAtLocationTool));
        Agent agent = agentsClient.createAgent(createAgentOptions);

        AgentThread thread = agentsClient.createThread();
        ThreadMessage createdMessage = agentsClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "What's the weather like in my favorite city?");

        //run agent
        CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
            .setAdditionalInstructions("");

        try {
            Flux<StreamUpdate> streamingUpdates = agentsClient.createRunStreaming(createRunOptions);


            streamingUpdates.doOnNext(
                streamUpdate -> {
                    if (streamUpdate.getKind() == AgentStreamEvent.THREAD_RUN_CREATED) {
                        System.out.println("----- Run started! -----");
                    } else if (streamUpdate instanceof StreamRequiredAction) {
                        StreamRequiredAction actionUpdate = (StreamRequiredAction) streamUpdate;
                        AtomicReference<ThreadRun> streamRun = new AtomicReference<>(actionUpdate.getMessage());

                        while (streamRun.get().getStatus() == RunStatus.REQUIRES_ACTION) {
                            List<ToolOutput> toolOutputs = new ArrayList<>();

                            SubmitToolOutputsAction submitToolsOutputAction = (SubmitToolOutputsAction) (streamRun.get().getRequiredAction());
                            for (RequiredToolCall toolCall : submitToolsOutputAction.getSubmitToolOutputs().getToolCalls()) {
                                toolOutputs.add(getResolvedToolOutput.apply(toolCall));
                            }

                            agentsClient.submitToolOutputsToRunStreaming(
                                streamRun.get().getThreadId(),
                                streamRun.get().getId(),
                                toolOutputs
                            ).doOnNext(update -> {
                                if (update instanceof StreamRequiredAction) {
                                    streamRun.set(((StreamRequiredAction) update).getMessage());
                                } else if (update instanceof StreamMessageUpdate) {
                                    StreamMessageUpdate messageUpdate = (StreamMessageUpdate) update;
                                    printStreamUpdate(messageUpdate);
                                } else if (update.getKind() == AgentStreamEvent.THREAD_RUN_COMPLETED) {
                                    streamRun.set(((StreamThreadRunCreation) update).getMessage());
                                }
                            }).blockLast();
                        }
                    } else if (streamUpdate instanceof StreamMessageUpdate) {
                        StreamMessageUpdate messageUpdate = (StreamMessageUpdate) streamUpdate;
                        printStreamUpdate(messageUpdate);
                    }
                }
            ).blockLast();

            System.out.println();
        } catch (Exception ex) {
            throw ex;
        } finally {
            //cleanup
            agentsClient.deleteThread(thread.getId());
            agentsClient.deleteAgent(agent.getId());
        }
    }

    void printStreamUpdate(StreamMessageUpdate messageUpdate) {
        messageUpdate.getMessage().getDelta().getContent().stream().forEach(delta -> {
            if (delta instanceof MessageDeltaImageFileContent) {
                MessageDeltaImageFileContent imgContent = (MessageDeltaImageFileContent) delta;
                System.out.println("Image fileId: " + imgContent.getImageFile().getFileId());
            } else if (delta instanceof MessageDeltaTextContent) {
                MessageDeltaTextContent textContent = (MessageDeltaTextContent) delta;
                System.out.print(textContent.getText().getValue());
            }
        });
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
