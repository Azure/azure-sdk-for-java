// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;
import com.openai.client.OpenAIClientAsync;
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
 * This sample demonstrates (using the async client) how to create an agent with a Custom Code Interpreter
 * backed by an MCP tool pointing to a Container Apps code interpreter session.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>MCP_SERVER_URL - The MCP server URL for the custom code interpreter.</li>
 *   <li>MCP_PROJECT_CONNECTION_ID - The MCP project connection ID.</li>
 * </ul>
 */
public class CustomCodeInterpreterAsync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String mcpServerUrl = Configuration.getGlobalConfiguration().get("MCP_SERVER_URL");
        String connectionId = Configuration.getGlobalConfiguration().get("MCP_PROJECT_CONNECTION_ID");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();

        McpTool customCodeInterpreter = new McpTool("custom-code-interpreter")
            .setServerUrl(mcpServerUrl)
            .setProjectConnectionId(connectionId)
            .setRequireApproval("never");

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can run Python code to analyze data and solve problems.")
            .setTools(Collections.singletonList(customCodeInterpreter));

        String agentName = "CustomCodeInterpreterAgent";
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
                            .input("Calculate the factorial of 10 using Python.")
                            .build()))
                        .doOnNext(response -> {
                            System.out.println("Response: " + response.output());
                        });
                },
                agent -> agentsAsyncClient.deleteAgentVersion(agentName, agent.getVersion()))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .timeout(Duration.ofSeconds(300))
            .block();
    }
}
