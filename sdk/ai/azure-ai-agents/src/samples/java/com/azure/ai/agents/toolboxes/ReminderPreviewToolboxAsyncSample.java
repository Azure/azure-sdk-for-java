// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.toolboxes;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ToolboxesAsyncClient;
import com.azure.ai.agents.models.ReminderPreviewToolboxTool;
import com.azure.ai.agents.models.ToolboxTool;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;

/**
 * This sample demonstrates asynchronously creating a toolbox version with the Reminder (preview) tool.
 *
 * <p>The reminder tool is available only to hosted agents. Before running, set
 * {@code FOUNDRY_PROJECT_ENDPOINT} to your Azure AI Foundry project endpoint.</p>
 */
public class ReminderPreviewToolboxAsyncSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String toolboxName = "reminder-toolbox-java-async";

        ToolboxesAsyncClient toolboxesAsyncClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildToolboxesAsyncClient();

        ReminderPreviewToolboxTool reminderTool = new ReminderPreviewToolboxTool()
            .setName("schedule_reminder")
            .setDescription("Schedule a reminder that re-invokes this agent at a future time.");

        Mono<Void> workflow = toolboxesAsyncClient.deleteToolbox(toolboxName)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(toolboxesAsyncClient.createToolboxVersion(
                toolboxName,
                Collections.<ToolboxTool>singletonList(reminderTool),
                "Built-in reminder tool for a self-scheduling agent.",
                null,
                null,
                null))
            .doOnNext(version -> {
                System.out.printf("Created toolbox: %s%n", version.getName());
                System.out.printf("Toolbox version: %s%n", version.getVersion());
                System.out.printf("Tool type: %s%n", version.getTools().get(0).getType());
            })
            .then(toolboxesAsyncClient.deleteToolbox(toolboxName))
            .onErrorResume(error -> toolboxesAsyncClient.deleteToolbox(toolboxName)
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
                .then(Mono.error(error)))
            .timeout(Duration.ofMinutes(5));

        workflow.block();
        System.out.printf("Deleted toolbox: %s%n", toolboxName);
    }
}
