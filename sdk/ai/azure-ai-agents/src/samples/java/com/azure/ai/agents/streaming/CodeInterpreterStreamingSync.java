// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.streaming;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.core.http.StreamResponse;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;
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

        // Create a CodeInterpreterTool - an Azure-specific tool for executing Python code
        CodeInterpreterTool tool = new CodeInterpreterTool();

        // Create agent with Code Interpreter tool
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can execute Python code to solve problems. "
                + "When asked to perform calculations, use the code interpreter to run Python code.")
            .setTools(Collections.singletonList(tool));

        String agentName = "code-interpreter-streaming-agent";
        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName, agentDefinition);
        try {
            agentsClient.updateAgentDetails(agentName, new UpdateAgentDetailsOptions().setAgentEndpoint(
                new AgentEndpointConfig()
                    .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                        new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion()))))
                    .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()))));


            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);

            // BEGIN: com.azure.ai.agents.streaming.code_interpreter_sync
            // Stream response with Code Interpreter - observe code execution events as they arrive
            ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

            try (StreamResponse<ResponseStreamEvent> events = openAIClient.responses().createStreaming(
                    ResponseCreateParams.builder()
                        .input("Calculate the first 10 prime numbers using Python.")
                        .build())) {

                events.stream().forEach(event -> {
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
                });
            }
            System.out.println();

            // Access the complete accumulated response
            Response response = responseAccumulator.response();
            System.out.println("\nResponse ID: " + response.id());
            // END: com.azure.ai.agents.streaming.code_interpreter_sync
        } finally {
            agentsClient.deleteAgentVersion(agentName, agent.getVersion());
        }
    }
}
