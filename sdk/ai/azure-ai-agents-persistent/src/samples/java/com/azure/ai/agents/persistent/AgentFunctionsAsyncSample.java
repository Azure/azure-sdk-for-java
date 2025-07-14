// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.FunctionDefinition;
import com.azure.ai.agents.persistent.models.FunctionToolDefinition;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.RequiredFunctionToolCall;
import com.azure.ai.agents.persistent.models.RequiredToolCall;
import com.azure.ai.agents.persistent.models.RunStatus;
import com.azure.ai.agents.persistent.models.SubmitToolOutputsAction;
import com.azure.ai.agents.persistent.models.ToolOutput;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;

public class AgentFunctionsAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        
        // Define the functions
        Supplier<String> getUserFavoriteCity = () -> "Seattle, WA";
        FunctionToolDefinition getUserFavoriteCityTool = new FunctionToolDefinition(
            new FunctionDefinition(
                "getUserFavoriteCity",
                BinaryData.fromObject(new Object())
            )
        );

        Function<String, String> getCityNickname = location -> "The Emerald City";
        FunctionToolDefinition getCityNicknameTool = new FunctionToolDefinition(
            new FunctionDefinition(
                "getCityNickname",
                BinaryData.fromObject(
                    mapOf(
                        "type", "object",
                        "properties", mapOf(
                            "location",
                            mapOf(
                                "type", "string",
                                "description", "The city and state, e.g. San Francisco, CA")
                        ),
                        "required", new String[]{"location"}))
            ).setDescription("Get the nickname of a city")
        );

        Function<RequiredToolCall, ToolOutput> getResolvedToolOutput = toolCall -> {
            if (toolCall instanceof RequiredFunctionToolCall) {
                RequiredFunctionToolCall functionToolCall = (RequiredFunctionToolCall) toolCall;
                String functionName = functionToolCall.getFunction().getName();
                if (functionName.equals("getUserFavoriteCity")) {
                    return new ToolOutput().setToolCallId(functionToolCall.getId())
                        .setOutput(getUserFavoriteCity.get());
                } else if (functionName.equals("getCityNickname")) {
                    String arguments = functionToolCall.getFunction().getArguments();
                    try {
                        JsonNode root = new JsonMapper().readTree(arguments);
                        String location = String.valueOf(root.get("location").asText());
                        return new ToolOutput().setToolCallId(functionToolCall.getId())
                            .setOutput(getCityNickname.apply(location));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return null;
        };

        // Create full reactive chain
        String agentName = "functions_example_async";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a weather bot. Use the provided functions to help answer questions. "
                + "Customize your responses to the user's preferences as much as possible and use friendly "
                + "nicknames for cities whenever possible.")
            .setTools(Arrays.asList(getUserFavoriteCityTool, getCityNicknameTool));

        administrationAsyncClient.createAgent(createAgentOptions)
            .flatMap(agent -> {
                System.out.println("Created agent: " + agent.getId());
                agentId.set(agent.getId());
                
                return threadsAsyncClient.createThread();
            })
            .flatMap(thread -> {
                System.out.println("Created thread: " + thread.getId());
                threadId.set(thread.getId());
                
                return messagesAsyncClient.createMessage(
                    thread.getId(),
                    MessageRole.USER,
                    "What's the nickname of my favorite city?");
            })
            .flatMap(message -> {
                System.out.println("Created message");
                
                CreateRunOptions createRunOptions = new CreateRunOptions(threadId.get(), agentId.get())
                    .setAdditionalInstructions("");
                
                return runsAsyncClient.createRun(createRunOptions);
            })
            .flatMap(threadRun -> {
                System.out.println("Created run, monitoring for completion or required actions...");
                
                // Poll the run until it's completed
                return Mono.fromSupplier(() -> threadRun)
                    .expand(run -> {
                        if (run.getStatus() == RunStatus.QUEUED
                            || run.getStatus() == RunStatus.IN_PROGRESS) {
                            return Mono.delay(java.time.Duration.ofMillis(500))
                                .then(runsAsyncClient.getRun(threadId.get(), run.getId()));
                        } else if (run.getStatus() == RunStatus.REQUIRES_ACTION) {
                            // Handle function calls
                            if (run.getRequiredAction() instanceof SubmitToolOutputsAction) {
                                SubmitToolOutputsAction submitToolsOutputAction = 
                                    (SubmitToolOutputsAction) run.getRequiredAction();
                                
                                ArrayList<ToolOutput> toolOutputs = new ArrayList<>();
                                for (RequiredToolCall toolCall
                                    : submitToolsOutputAction.getSubmitToolOutputs().getToolCalls()) {
                                    toolOutputs.add(getResolvedToolOutput.apply(toolCall));
                                }
                                
                                return runsAsyncClient.submitToolOutputsToRun(
                                        threadId.get(), run.getId(), toolOutputs)
                                    .flatMap(updatedRun -> Mono.delay(java.time.Duration.ofMillis(500))
                                        .then(runsAsyncClient.getRun(threadId.get(), updatedRun.getId())));
                            }
                            return Mono.empty();
                        } else {
                            return Mono.empty();
                        }
                    })
                    .last();
            })
            .flatMap(completedRun -> {
                System.out.println("Run completed with status: " + completedRun.getStatus());
                
                if (completedRun.getStatus() == RunStatus.FAILED && completedRun.getLastError() != null) {
                    System.out.println("Run failed: " + completedRun.getLastError().getMessage());
                }
                
                return printRunMessagesAsync(messagesAsyncClient, threadId.get());
            })
            .doFinally(signalType -> {
                // Clean up resources
                if (threadId.get() != null) {
                    threadsAsyncClient.deleteThread(threadId.get()).block();
                    System.out.println("Thread deleted: " + threadId.get());
                }
                if (agentId.get() != null) {
                    administrationAsyncClient.deleteAgent(agentId.get()).block();
                    System.out.println("Agent deleted: " + agentId.get());
                }
            })
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
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
