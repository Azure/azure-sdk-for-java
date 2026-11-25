// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.agentinsights;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.AgentInsightMonitorsClient;
import com.azure.ai.projects.models.AgentInsightMonitor;
import com.azure.ai.projects.models.UpdateAgentInsightMonitorOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;

import static com.azure.ai.projects.agentinsights.AgentInsightsSampleUtils.deleteMonitor;
import static com.azure.ai.projects.agentinsights.AgentInsightsSampleUtils.requiredSetting;

/**
 * Enables six-hour recurring Agent Insights runs and reads the next scheduled time.
 * Requires connected Application Insights, a GPT-5-or-later model deployment, and an existing agent
 * without an Insights monitor. Set {@code FOUNDRY_PROJECT_ENDPOINT}, {@code FOUNDRY_AGENT_NAME},
 * and {@code FOUNDRY_MODEL_NAME}. This sample does not wait for a scheduled run.
 */
public class AgentInsightsScheduledSample {
    public static void main(String[] args) {
        AgentInsightMonitorsClient monitors = new AIProjectClientBuilder()
            .endpoint(requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAgentInsightMonitorsClient();

        AgentInsightMonitor monitor = monitors.createAgentInsightMonitor(
            requiredSetting("FOUNDRY_AGENT_NAME"), requiredSetting("FOUNDRY_MODEL_NAME"), false, null, null);
        System.out.printf("Created monitor: %s%n", monitor.getId());
        try {
            AgentInsightMonitor scheduled = monitors.updateAgentInsightMonitor(monitor.getId(),
                new UpdateAgentInsightMonitorOptions().setRunIntervalHours(6.0).setEnabled(true));
            System.out.printf("Interval: %s hours; next run: %s%n",
                scheduled.getRunIntervalHours(), scheduled.getNextScheduledRunAt());
        } finally {
            deleteMonitor(monitors, monitor.getId());
        }
    }
}
