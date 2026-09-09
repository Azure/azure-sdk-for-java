// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ToolboxesClient;
import com.azure.ai.agents.models.ReminderPreviewToolboxTool;
import com.azure.ai.agents.models.ToolboxTool;
import com.azure.ai.agents.models.ToolboxVersionDetails;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Collections;

/**
 * This sample demonstrates creating a toolbox version with the Reminder (preview) tool.
 *
 * <p>The reminder tool is available only to hosted agents. Before running, set
 * {@code FOUNDRY_PROJECT_ENDPOINT} to your Azure AI Foundry project endpoint.</p>
 */
public class ReminderPreviewToolboxSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "reminder-toolbox-java";

        ToolboxesClient toolboxesClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildToolboxesClient();

        try {
            toolboxesClient.deleteToolbox(toolboxName);
        } catch (ResourceNotFoundException ignored) {
            // The sample toolbox does not already exist.
        }

        try {
            // BEGIN: com.azure.ai.agents.toolboxes.ReminderPreviewToolboxSample.createReminderToolbox

            ReminderPreviewToolboxTool reminderTool = new ReminderPreviewToolboxTool()
                .setName("schedule_reminder")
                .setDescription("Schedule a reminder that re-invokes this agent at a future time.");

            ToolboxVersionDetails version = toolboxesClient.createToolboxVersion(
                toolboxName,
                Collections.<ToolboxTool>singletonList(reminderTool),
                "Built-in reminder tool for a self-scheduling agent.",
                null,
                null,
                null);

            System.out.printf("Created toolbox: %s%n", version.getName());
            System.out.printf("Toolbox version: %s%n", version.getVersion());
            System.out.printf("Tool type: %s%n", version.getTools().get(0).getType());

            // END: com.azure.ai.agents.toolboxes.ReminderPreviewToolboxSample.createReminderToolbox
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
