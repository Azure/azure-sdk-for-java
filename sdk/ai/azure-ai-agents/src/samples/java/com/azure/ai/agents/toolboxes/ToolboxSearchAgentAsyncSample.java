// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.ToolboxesAsyncClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.McpToolboxTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.ToolSearchToolboxTool;
import com.azure.ai.agents.models.ToolboxTool;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.ResponseCreateParams;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates asynchronously creating a tool-search toolbox and invoking its MCP endpoint.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code MCP_PROJECT_CONNECTION_ID} - The project connection resource ID used by the inner MCP server.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class ToolboxSearchAgentAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "toolbox-search-async-java";
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        AgentsClientBuilder builder = new AgentsClientBuilder().credential(credential).endpoint(endpoint);
        ToolboxesAsyncClient toolboxesClient = builder.buildToolboxesAsyncClient();
        AgentsAsyncClient agentsClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesClient = builder.buildResponsesAsyncClient();
        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        McpToolboxTool innerMcp = new McpToolboxTool("github")
            .setServerUrl("https://api.githubcopilot.com/mcp")
            .setProjectConnectionId(configuration.get("MCP_PROJECT_CONNECTION_ID"))
            .setRequireApproval(BinaryData.fromString("\"never\""))
            .setDeferLoading(true);
        ToolSearchToolboxTool search = new ToolSearchToolboxTool();

        toolboxesClient.deleteToolbox(toolboxName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(toolboxesClient.createToolboxVersion(toolboxName,
                Arrays.<ToolboxTool>asList(innerMcp, search), "Tool-search toolbox", null, null, null))
            .zipWith(credential.getToken(new TokenRequestContext().addScopes("https://ai.azure.com/.default")))
            .flatMap(tuple -> {
                String toolboxUrl = endpoint + "/toolboxes/" + toolboxName + "/versions/"
                    + tuple.getT1().getVersion() + "/mcp?api-version=v1";
                McpTool toolboxMcp = new McpTool("search-tool")
                    .setServerUrl(toolboxUrl)
                    .setAuthorization(tuple.getT2().getToken())
                    .setRequireApproval("never");
                return agentsClient.createAgentVersion("toolbox-search-agent-async",
                    new CreateAgentVersionInput(new PromptAgentDefinition(configuration.get("FOUNDRY_MODEL_NAME"))
                        .setInstructions("Use tool_search to discover a tool, then call_tool to invoke it.")
                        .setTools(Collections.singletonList(toolboxMcp))));
            })
            .doOnNext(agentRef::set)
            .flatMap(agent -> responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(
                    new AgentReference(agent.getName()).setVersion(agent.getVersion())),
                ResponseCreateParams.builder().input("What is my GitHub profile username?")))
            .doOnNext(response -> System.out.println("Response: " + response.output()))
            .then(cleanup(agentsClient, toolboxesClient, toolboxName, agentRef))
            .onErrorResume(error -> cleanup(agentsClient, toolboxesClient, toolboxName, agentRef)
                .then(Mono.error(error)))
            .block();
    }

    private static Mono<Void> cleanup(AgentsAsyncClient agentsClient, ToolboxesAsyncClient toolboxesClient,
        String toolboxName, AtomicReference<AgentVersionDetails> agentRef) {
        AgentVersionDetails agent = agentRef.get();
        Mono<Void> deleteAgent = agent == null ? Mono.empty()
            : agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        return deleteAgent.then(toolboxesClient.deleteToolbox(toolboxName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty()));
    }
}
