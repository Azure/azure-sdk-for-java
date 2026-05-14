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

import java.util.Arrays;
import java.util.List;

/**
 * This sample demonstrates how to create a toolbox version using the ToolboxesClient.
 *
 * <p>A toolbox stores reusable tool definitions that can be shared across agents.
 * Each call to {@code createToolboxVersion} creates a new immutable version. If the
 * toolbox does not yet exist, it is created automatically.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class CreateToolboxVersion {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        // Code sample for creating a toolbox version
        ToolboxesClient toolboxesClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildToolboxesClient();

        List<Tool> tools = Arrays.asList(
                new McpTool("api_specs")
                        .setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
                        .setRequireApproval("never")
        );

        ToolboxVersionDetails toolboxVersion = toolboxesClient.createToolboxVersion(
                "toolbox_created_from_java", tools,
                "Toolbox with MCP tool requiring approval 'never'.", null, null);

        System.out.println("Toolbox Name: " + toolboxVersion.getName());
        System.out.println("Toolbox Version: " + toolboxVersion.getVersion());
        System.out.println("Toolbox ID: " + toolboxVersion.getId());
        System.out.println("Description: " + toolboxVersion.getDescription());
    }
}
