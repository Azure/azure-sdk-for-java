// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaToolboxesClient;
import com.azure.ai.agents.models.Tool;
import com.azure.ai.agents.models.ToolboxSearchPreviewTool;
import com.azure.ai.agents.models.ToolboxVersionDetails;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Collections;

/**
 * This sample demonstrates creating a toolbox version that includes the Toolbox Search preview tool.
 *
 * <p>Toolboxes are a preview feature. Before running, set {@code FOUNDRY_PROJECT_ENDPOINT} to your Azure AI Foundry
 * project endpoint.</p>
 */
public class ToolboxSearchToolboxSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "toolbox-search-tool-java";

        BetaToolboxesClient toolboxesClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .beta().buildBetaToolboxesClient();

        try {
            toolboxesClient.deleteToolbox(toolboxName);
        } catch (ResourceNotFoundException ignored) {
            // The sample toolbox does not already exist.
        }

        try {
            // BEGIN: com.azure.ai.agents.toolboxes.ToolboxSearchToolboxSample.createToolboxSearchToolbox

            ToolboxSearchPreviewTool toolboxSearchTool = new ToolboxSearchPreviewTool()
                .setName("search_tools")
                .setDescription("Search over available toolbox tools at runtime.");

            ToolboxVersionDetails version = toolboxesClient.createToolboxVersion(
                toolboxName,
                Collections.singletonList(toolboxSearchTool),
                "Toolbox version with a Toolbox Search preview tool.",
                null,
                null,
                null);

            System.out.printf("Created toolbox: %s%n", version.getName());
            System.out.printf("Toolbox version: %s%n", version.getVersion());
            for (Tool tool : version.getTools()) {
                System.out.printf("Tool type: %s%n", tool.getType());
            }

            // END: com.azure.ai.agents.toolboxes.ToolboxSearchToolboxSample.createToolboxSearchToolbox
        } finally {
            try {
                toolboxesClient.deleteToolbox(toolboxName);
                System.out.printf("Deleted toolbox: %s%n", toolboxName);
            } catch (ResourceNotFoundException ignored) {
                // The sample toolbox may not have been created.
            }
        }
    }
}
