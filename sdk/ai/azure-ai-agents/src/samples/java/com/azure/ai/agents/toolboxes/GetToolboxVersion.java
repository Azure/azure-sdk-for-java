// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ToolboxesClient;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.Tool;
import com.azure.ai.agents.models.ToolboxVersionDetails;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This sample demonstrates how to retrieve a specific version of a toolbox
 * using the ToolboxesClient.
 *
 * <p>The {@code getToolboxVersion} method returns the full version details
 * including the list of tools, description, metadata, and creation timestamp.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class GetToolboxVersion {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "toolbox_created_from_java";
        String version = "1"; // Replace with the desired version
        // Code sample for retrieving a specific toolbox version
        ToolboxesClient toolboxesClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildToolboxesClient();

        ToolboxVersionDetails toolboxVersion = toolboxesClient.getToolboxVersion(toolboxName, version);

        System.out.println("Toolbox Name: " + toolboxVersion.getName());
        System.out.println("Toolbox Version: " + toolboxVersion.getVersion());
        System.out.println("Description: " + toolboxVersion.getDescription());
        System.out.println("Created At: " + toolboxVersion.getCreatedAt());
        System.out.println("Tools:");
        for (Tool tool : toolboxVersion.getTools()) {
            if (tool instanceof McpTool) {
                McpTool mcpTool = (McpTool) tool;
                System.out.println("  - MCP '" + mcpTool.getServerLabel()
                        + "' require_approval: " + mcpTool.getRequireApprovalAsString());
            }
        }
    }
}
