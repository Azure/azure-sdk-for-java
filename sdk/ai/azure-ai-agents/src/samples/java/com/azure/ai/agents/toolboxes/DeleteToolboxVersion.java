// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ToolboxesClient;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This sample demonstrates how to delete a specific version of a toolbox using the
 * ToolboxesClient.
 *
 * <p>The {@code deleteToolboxVersion} method removes only the specified version,
 * leaving other versions and the toolbox itself intact.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class DeleteToolboxVersion {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "toolbox_created_from_java";
        String version = "1"; // Replace with the version to delete
        // Code sample for deleting a specific toolbox version
        ToolboxesClient toolboxesClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildToolboxesClient();

        toolboxesClient.deleteToolboxVersion(toolboxName, version);

        System.out.println("Deleted toolbox version with the following details:");
        System.out.println("\tToolbox Name: " + toolboxName);
        System.out.println("\tVersion: " + version);
    }
}
