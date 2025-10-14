// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.FunctionDefinition;
import com.azure.ai.agents.persistent.models.FunctionToolDefinition;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent;
import com.azure.ai.agents.persistent.models.RequiredFunctionToolCall;
import com.azure.ai.agents.persistent.models.RequiredToolCall;
import com.azure.ai.agents.persistent.models.RunStatus;
import com.azure.ai.agents.persistent.models.StreamMessageUpdate;
import com.azure.ai.agents.persistent.models.StreamRequiredAction;
import com.azure.ai.agents.persistent.models.StreamThreadRunCreation;
import com.azure.ai.agents.persistent.models.StreamUpdate;
import com.azure.ai.agents.persistent.models.SubmitToolOutputsAction;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.ai.agents.persistent.models.ToolOutput;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.ai.agents.persistent.SampleUtils.cleanUpResources;
import static com.azure.ai.agents.persistent.SampleUtils.printStreamUpdate;

public final class AgentFunctionsStreamingAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();

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
                    return unit.equals("f") ? "70f" : "21c";
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
                        String funcArgs = functionToolCall.getFunction().getArguments();

                        JsonNode root = new JsonMapper().readTree(funcArgs);
                        String location = String.valueOf(root.get("location").asText());
                        return new ToolOutput().setToolCallId(functionToolCall.getId())
                            .setOutput(getCityNickname.apply(location));

                    } else if (functionName.equals("getCurrentWeatherAtLocation")) {
                        String funcArgs = functionToolCall.getFunction().getArguments();

                        JsonNode root = new JsonMapper().readTree(funcArgs);
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

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();

        String agentName = "functions_streaming_async_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a weather bot. Use the provided functions to help answer questions. "
                + "Customize your responses to the user's preferences as much as possible and use friendly "
                + "nicknames for cities whenever possible.")
            .setTools(Arrays.asList(getUserFavoriteCityTool, getCityNicknameTool, getCurrentWeatherAtLocationTool));

        // Create a fully reactive chain
        administrationAsyncClient.createAgent(createAgentOptions)
            .flatMap(agent -> {
                System.out.println("Created agent: " + agent.getId());
                agentId.set(agent.getId());
                
                return threadsAsyncClient.createThread()
                    .flatMap(thread -> {
                        System.out.println("Created thread: " + thread.getId());
                        threadId.set(thread.getId());
                        
                        return messagesAsyncClient.createMessage(
                            thread.getId(),
                            MessageRole.USER,
                            "What's the weather like in my favorite city?")
                            .flatMap(message -> {
                                System.out.println("Created initial message");
                                
                                CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
                                    .setAdditionalInstructions("");
                                
                                System.out.println("----- Run started! -----");
                                return handleStreamingRun(runsAsyncClient
                                    .createRunStreaming(createRunOptions), runsAsyncClient, getResolvedToolOutput);
                            });
                    });
            })
            .doFinally(signalType -> cleanUpResources(threadId, threadsAsyncClient, agentId, administrationAsyncClient))
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }

    private static Mono<Void> handleStreamingRun(
            Flux<StreamUpdate> streamingUpdates,
            RunsAsyncClient runsAsyncClient,
            Function<RequiredToolCall, ToolOutput> toolOutputResolver) {
            
        AtomicReference<ThreadRun> currentRun = new AtomicReference<>();
        
        return streamingUpdates
            .flatMap(streamUpdate -> {
                if (streamUpdate instanceof StreamRequiredAction) {
                    StreamRequiredAction actionUpdate = (StreamRequiredAction) streamUpdate;
                    currentRun.set(actionUpdate.getMessage());
                    
                    if (currentRun.get().getStatus() == RunStatus.REQUIRES_ACTION) {
                        return handleToolActions(
                            currentRun.get(), 
                            runsAsyncClient, 
                            toolOutputResolver);
                    }
                } else if (streamUpdate instanceof StreamMessageUpdate) {
                    StreamMessageUpdate messageUpdate = (StreamMessageUpdate) streamUpdate;
                    printStreamUpdate(messageUpdate);
                } else if (streamUpdate.getKind() == PersistentAgentStreamEvent.THREAD_RUN_COMPLETED) {
                    StreamThreadRunCreation runCreation = (StreamThreadRunCreation) streamUpdate;
                    currentRun.set(runCreation.getMessage());
                    System.out.println("Run completed with status: " + currentRun.get().getStatus());
                }
                return Mono.empty();
            })
            .then();
    }
    
    private static Flux<StreamUpdate> handleToolActions(
            ThreadRun run, 
            RunsAsyncClient runsAsyncClient,
            Function<RequiredToolCall, ToolOutput> toolOutputResolver) {
        
        List<ToolOutput> toolOutputs = new ArrayList<>();
        SubmitToolOutputsAction submitAction = (SubmitToolOutputsAction) run.getRequiredAction();
        
        for (RequiredToolCall toolCall : submitAction.getSubmitToolOutputs().getToolCalls()) {
            toolOutputs.add(toolOutputResolver.apply(toolCall));
        }
        
        return runsAsyncClient.submitToolOutputsToRunStreaming(
            run.getThreadId(),
            run.getId(),
            toolOutputs);
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
