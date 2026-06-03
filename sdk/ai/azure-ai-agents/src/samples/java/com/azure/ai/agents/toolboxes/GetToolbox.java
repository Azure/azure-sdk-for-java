// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaToolboxesClient;
import com.azure.ai.agents.models.ToolboxDetails;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This sample demonstrates how to retrieve a toolbox using the BetaToolboxesClient.
 *
 * <p>The {@code getToolbox} method returns the toolbox metadata including its name,
 * ID, and default version.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class GetToolbox {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "toolbox_created_from_java";
        // Code sample for retrieving a toolbox
        BetaToolboxesClient toolboxesClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .beta().buildBetaToolboxesClient();

        ToolboxDetails toolbox = toolboxesClient.getToolbox(toolboxName);

        System.out.println("Toolbox ID: " + toolbox.getId());
        System.out.println("Toolbox Name: " + toolbox.getName());
        System.out.println("Default Version: " + toolbox.getDefaultVersion());
    }
}
