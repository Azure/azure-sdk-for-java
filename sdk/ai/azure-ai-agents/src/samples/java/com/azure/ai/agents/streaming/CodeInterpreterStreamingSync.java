// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.streaming;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;

import java.util.Collections;

/**
 * This sample demonstrates how to stream a response from an agent configured with the
 * Azure-specific Code Interpreter tool. Code execution progress events and text output
 * are printed as they arrive.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 */
public class CodeInterpreterStreamingSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        AgentVersionDetails agent = null;

        try {
            // Create a CodeInterpreterTool - an Azure-specific tool for executing Python code
            CodeInterpreterTool tool = new CodeInterpreterTool();

            // Create agent with Code Interpreter tool
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that can execute Python code to solve problems. "
                    + "When asked to perform calculations, use the code interpreter to run Python code.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("code-interpreter-streaming-agent", agentDefinition);
            System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            // BEGIN: com.azure.ai.agents.streaming.code_interpreter_sync
            // Stream response with Code Interpreter - observe code execution events as they arrive
            ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

            IterableStream<ResponseStreamEvent> events =
                responsesClient.createStreamingWithAgent(agentReference,
                    ResponseCreateParams.builder()
                        .input("Calculate the first 10 prime numbers using Python."));

            for (ResponseStreamEvent event : events) {
                responseAccumulator.accumulate(event);
                // Print text deltas as they stream in
                event.outputTextDelta().ifPresent(textEvent ->
                    System.out.print(textEvent.delta()));

                // Observe code interpreter progress events
                event.codeInterpreterCallInProgress().ifPresent(e ->
                    System.out.println("\n[Code interpreter running...]"));
                event.codeInterpreterCallCodeDelta().ifPresent(e ->
                    System.out.print(e.delta()));
                event.codeInterpreterCallCompleted().ifPresent(e ->
                    System.out.println("\n[Code interpreter completed]"));
            }
            System.out.println();

            // Access the complete accumulated response
            Response response = responseAccumulator.response();
            System.out.println("\nResponse ID: " + response.id());
            // END: com.azure.ai.agents.streaming.code_interpreter_sync
        } finally {
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted");
            }
        }
    }
}
