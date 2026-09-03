// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.responses.ResponseCodeInterpreterToolCall;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import reactor.core.publisher.Mono;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;

import java.time.Duration;
import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with the Code Interpreter tool
 * for executing Python code, data analysis, and visualization using the async client.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 */
public class CodeInterpreterAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();

        // Create a CodeInterpreterTool with default auto container configuration
        CodeInterpreterTool tool = new CodeInterpreterTool();

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can execute Python code to solve problems. "
                + "When asked to perform calculations or data analysis, use the code interpreter to run Python code.")
            .setTools(Collections.singletonList(tool));

        // Create the agent version and pin the agent endpoint to it. The endpoint URL identifies the agent,
        // so responses.create(...) below does not need to send an agent_reference in its body.
        String agentName = "code-interpreter-agent";
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
                    return Mono.fromFuture(openAIAsyncClient.responses().create(ResponseCreateParams.builder()
                            .input("Calculate the first 10 prime numbers and show me the Python code you used.")
                            .build()))
                        .doOnNext(response -> {
                            for (ResponseOutputItem outputItem : response.output()) {
                                if (outputItem.message().isPresent()) {
                                    ResponseOutputMessage message = outputItem.message().get();
                                    message.content().forEach(content -> {
                                        content.outputText().ifPresent(text -> {
                                            System.out.println("Assistant: " + text.text());
                                        });
                                    });
                                }

                                if (outputItem.codeInterpreterCall().isPresent()) {
                                    ResponseCodeInterpreterToolCall codeCall = outputItem.codeInterpreterCall().get();
                                    System.out.println("\n--- Code Interpreter Execution ---");
                                    System.out.println("Call ID: " + codeCall.id());
                                    codeCall.code().ifPresent(code -> {
                                        System.out.println("Python Code Executed:\n" + code);
                                    });
                                    System.out.println("Status: " + codeCall.status());
                                }
                            }
                        });
                },
                agent -> agentsAsyncClient.deleteAgentVersion(agentName, agent.getVersion()))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .timeout(Duration.ofSeconds(300))
            .block();
    }
}
