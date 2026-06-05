// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ToolboxesAsyncClient;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.Tool;
import com.azure.ai.agents.models.ToolboxDetails;
import com.azure.ai.agents.models.ToolboxVersionDetails;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * This sample demonstrates end-to-end asynchronous CRUD operations on toolboxes.
 *
 * <p>A toolbox stores reusable tool definitions that can be shared across agents.
 * Each call to {@code createToolboxVersion} creates a new immutable version. The
 * toolbox's default version can be changed with {@code updateToolbox}.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class ToolboxesAsyncSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "toolbox-with-mcp-tool-java";

        ToolboxesAsyncClient toolboxesAsyncClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildToolboxesAsyncClient();

        List<Tool> toolsWithMcpApprovalNever = Collections.singletonList(
            new McpTool("api_specs")
                .setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
                .setRequireApproval("never"));

        List<Tool> toolsWithMcpApprovalAlways = Collections.singletonList(
            new McpTool("api_specs")
                .setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
                .setRequireApproval("always"));

        Mono<Void> workflow = toolboxesAsyncClient.deleteToolbox(toolboxName)
            .doOnSuccess(unused -> System.out.printf("Toolbox `%s` deleted%n", toolboxName))
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(toolboxesAsyncClient.createToolboxVersion(toolboxName, toolsWithMcpApprovalNever,
                "Toolbox version with MCP require_approval set to 'never'.", null, null, null))
            .doOnNext(created -> System.out.printf(
                "Created toolbox: %s with MCP tools requiring approval 'never' in version %s%n",
                created.getName(), created.getVersion()))
            .then(toolboxesAsyncClient.createToolboxVersion(toolboxName, toolsWithMcpApprovalAlways,
                "Toolbox version with MCP require_approval set to 'always'.", null, null, null))
            .doOnNext(created -> System.out.printf(
                "Created toolbox: %s with MCP tools requiring approval 'always' in version %s%n",
                created.getName(), created.getVersion()))
            .then(toolboxesAsyncClient.updateToolbox(toolboxName, "2"))
            .flatMap(updated -> printFetchedDefaultToolboxVersion(toolboxesAsyncClient, updated))
            .then(toolboxesAsyncClient.updateToolbox(toolboxName, "1"))
            .flatMap(updated -> printFetchedDefaultToolboxVersion(toolboxesAsyncClient, updated))
            .then(Mono.fromRunnable(() -> System.out.println("Listing toolboxes...")))
            .thenMany(toolboxesAsyncClient.listToolboxes())
            .doOnNext(item -> System.out.printf("  - %s (%s)%n", item.getName(), item.getId()))
            .then(toolboxesAsyncClient.deleteToolbox(toolboxName))
            .doOnSuccess(unused -> System.out.println("Toolbox deleted"));

        workflow
            .onErrorResume(error -> toolboxesAsyncClient.deleteToolbox(toolboxName)
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
                .then(Mono.error(error)))
            .timeout(Duration.ofMinutes(5))
            .block();
    }

    private static Mono<ToolboxVersionDetails> printFetchedDefaultToolboxVersion(
        ToolboxesAsyncClient toolboxesAsyncClient, ToolboxDetails updated) {
        System.out.printf("Updated toolbox: %s default version is now %s%n", updated.getName(),
            updated.getDefaultVersion());

        return toolboxesAsyncClient.getToolbox(updated.getName())
            .doOnNext(fetched -> System.out.printf("Retrieved toolbox with default version: %s%n",
                fetched.getDefaultVersion()))
            .flatMap(fetched -> toolboxesAsyncClient.getToolboxVersion(fetched.getName(),
                fetched.getDefaultVersion()))
            .doOnNext(version -> printMcpRequireApproval(version.getTools()));
    }

    private static void printMcpRequireApproval(List<Tool> tools) {
        for (Tool tool : tools) {
            if (tool instanceof McpTool) {
                McpTool mcpTool = (McpTool) tool;
                System.out.printf("  - MCP `%s` require_approval: %s%n", mcpTool.getServerLabel(),
                    mcpTool.getRequireApprovalAsString());
            }
        }
    }
}
