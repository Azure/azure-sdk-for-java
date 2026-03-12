// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.streaming;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates how to stream a response from an agent configured with the
 * Azure-specific Code Interpreter tool using the asynchronous client. Code execution
 * progress events and text output are printed as they arrive.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 * </ul>
 */
public class CodeInterpreterStreamingAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        // Create a CodeInterpreterTool - an Azure-specific tool for executing Python code
        CodeInterpreterTool tool = new CodeInterpreterTool();

        // Create agent with Code Interpreter tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can execute Python code to solve problems. "
                + "When asked to perform calculations, use the code interpreter to run Python code.")
            .setTools(Collections.singletonList(tool));

        agentsAsyncClient.createAgentVersion("code-interpreter-streaming-async-agent", agentDefinition)
            .flatMap(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

                AgentReference agentReference = new AgentReference(agent.getName())
                    .setVersion(agent.getVersion());

                // BEGIN: com.azure.ai.agents.streaming.code_interpreter_async
                // Stream response asynchronously with Code Interpreter
                ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

                return responsesAsyncClient.createStreamingWithAgent(agentReference,
                        ResponseCreateParams.builder()
                            .input("Calculate the first 10 prime numbers using Python."))
                    .doOnNext(event -> {
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
                    .then(Mono.fromCallable(() -> {
                        System.out.println();

                        // Access the complete accumulated response
                        Response response = responseAccumulator.response();
                        System.out.println("\nResponse ID: " + response.id());
                        // END: com.azure.ai.agents.streaming.code_interpreter_async
                        return response;
                    }));
            })
            .then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                if (agent != null) {
                    return agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion())
                        .doOnSuccess(v -> System.out.println("Agent deleted"));
                }
                return Mono.empty();
            }))
            .block();
    }
}
