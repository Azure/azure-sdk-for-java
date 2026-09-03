// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.streaming;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;

import java.util.Collections;

/**
 * This sample demonstrates how to stream a response from an agent configured with the
 * Azure-specific Code Interpreter tool using the asynchronous client. Code execution
 * progress events and text output are printed as they arrive.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 */
public class CodeInterpreterStreamingAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();

        // Create a CodeInterpreterTool - an Azure-specific tool for executing Python code
        CodeInterpreterTool tool = new CodeInterpreterTool();

        // Create agent with Code Interpreter tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can execute Python code to solve problems. "
                + "When asked to perform calculations, use the code interpreter to run Python code.")
            .setTools(Collections.singletonList(tool));

        // Create the agent version and pin the agent endpoint to it. The endpoint URL identifies the agent,
        // so responses.createStreaming(...) below does not need to send an agent_reference in its body.
        String agentName = "code-interpreter-streaming-async-agent";
        Mono.usingWhen(
                agentsAsyncClient.createAgentVersion(agentName, agentDefinition)
                    .flatMap(agent -> agentsAsyncClient.updateAgentDetails(agentName,
                            new UpdateAgentDetailsOptions().setAgentEndpoint(
                                new AgentEndpointConfig()
                                    .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                                        new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion()))))
                                    .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()))))
                        .thenReturn(agent)),
                agent -> {
                    OpenAIClientAsync openAIAsyncClient
                        = builder.buildAgentScopedOpenAIAsyncClient(agentName);

                    // BEGIN: com.azure.ai.agents.streaming.code_interpreter_async
                    // Stream response asynchronously with Code Interpreter
                    ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

                    return Mono.fromFuture(openAIAsyncClient.responses()
                        .createStreaming(ResponseCreateParams.builder()
                            .input("Calculate the first 10 prime numbers using Python.")
                            .build())
                        .subscribe(event -> {
                            responseAccumulator.accumulate(event);
                            // Print text deltas as they arrive
                            event.outputTextDelta()
                                .ifPresent(textEvent -> System.out.print(textEvent.delta()));
                            // Observe code interpreter progress events
                            event.codeInterpreterCallInProgress()
                                .ifPresent(e -> System.out.println("\n[Code interpreter running...]"));
                            event.codeInterpreterCallCodeDelta()
                                .ifPresent(e -> System.out.print(e.delta()));
                            event.codeInterpreterCallCompleted()
                                .ifPresent(e -> System.out.println("\n[Code interpreter completed]"));
                        })
                        .onCompleteFuture())
                        .doOnSuccess(unused -> {
                            System.out.println();

                            // Access the complete accumulated response
                            Response response = responseAccumulator.response();
                            System.out.println("\nResponse ID: " + response.id());
                        });
                    // END: com.azure.ai.agents.streaming.code_interpreter_async
                },
                agent -> agentsAsyncClient.deleteAgentVersion(agentName, agent.getVersion()))
            .block();
    }
}
