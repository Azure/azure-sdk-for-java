// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ToolboxesClient;
import com.azure.ai.agents.models.ToolboxVersionDetails;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This sample demonstrates how to list all versions of a toolbox using the
 * ToolboxesClient.
 *
 * <p>The {@code listToolboxVersions} method returns a paginated list of all
 * immutable versions that have been created for a given toolbox.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class ListToolboxVersions {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "toolbox_created_from_java";
        // Code sample for listing all versions of a toolbox
        ToolboxesClient toolboxesClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildToolboxesClient();

        System.out.println("Listing all versions of toolbox '" + toolboxName + "':");
        for (ToolboxVersionDetails version : toolboxesClient.listToolboxVersions(toolboxName)) {
            System.out.println("Version: " + version.getVersion());
            System.out.println("Version ID: " + version.getId());
            System.out.println("Description: " + version.getDescription());
            System.out.println("Created At: " + version.getCreatedAt());
            System.out.println("---");
        }
    }
}
