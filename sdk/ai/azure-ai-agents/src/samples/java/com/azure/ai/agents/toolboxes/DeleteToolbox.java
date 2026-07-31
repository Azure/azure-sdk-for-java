// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaToolboxesClient;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This sample demonstrates how to delete a toolbox and all its versions using the
 * BetaToolboxesClient.
 *
 * <p>The {@code deleteToolbox} method removes the toolbox and every version
 * associated with it.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class DeleteToolbox {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "toolbox_created_from_java";
        // Code sample for deleting a toolbox
        BetaToolboxesClient toolboxesClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .beta().buildBetaToolboxesClient();

        toolboxesClient.deleteToolbox(toolboxName);

        System.out.println("Deleted toolbox with the following details:");
        System.out.println("\tToolbox Name: " + toolboxName);
    }
}
