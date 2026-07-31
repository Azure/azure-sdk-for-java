// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaToolboxesClient;
import com.azure.ai.agents.models.ToolboxDetails;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This sample demonstrates how to list all toolboxes using the BetaToolboxesClient.
 *
 * <p>The {@code listToolboxes} method returns a paginated list of all toolboxes
 * in the project.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class ListToolboxes {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        // Code sample for listing all toolboxes
        BetaToolboxesClient toolboxesClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .beta().buildBetaToolboxesClient();

        System.out.println("Listing all toolboxes:");
        for (ToolboxDetails toolbox : toolboxesClient.listToolboxes()) {
            System.out.println("Toolbox ID: " + toolbox.getId());
            System.out.println("Toolbox Name: " + toolbox.getName());
            System.out.println("Default Version: " + toolbox.getDefaultVersion());
            System.out.println("---");
        }
    }
}
