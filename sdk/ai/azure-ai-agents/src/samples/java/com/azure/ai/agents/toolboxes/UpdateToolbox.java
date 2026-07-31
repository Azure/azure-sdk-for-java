// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaToolboxesClient;
import com.azure.ai.agents.models.ToolboxDetails;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This sample demonstrates how to update a toolbox using the BetaToolboxesClient.
 *
 * <p>The {@code updateToolbox} method changes the default version that the toolbox
 * points to. This is useful when you have multiple immutable versions and want to
 * switch which version is used by default.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class UpdateToolbox {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "toolbox_created_from_java";
        // Code sample for updating a toolbox's default version
        BetaToolboxesClient toolboxesClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .beta().buildBetaToolboxesClient();

        ToolboxDetails updatedToolbox = toolboxesClient.updateToolbox(toolboxName, "2");

        System.out.println("Updated Toolbox Name: " + updatedToolbox.getName());
        System.out.println("Updated Default Version: " + updatedToolbox.getDefaultVersion());
    }
}
