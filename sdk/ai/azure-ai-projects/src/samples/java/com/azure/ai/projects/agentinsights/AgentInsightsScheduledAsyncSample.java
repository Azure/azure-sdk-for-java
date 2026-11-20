// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.agentinsights;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.AgentInsightMonitorsAsyncClient;
import com.azure.ai.projects.models.UpdateAgentInsightMonitorOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import static com.azure.ai.projects.agentinsights.AgentInsightsSampleUtils.deleteMonitor;
import static com.azure.ai.projects.agentinsights.AgentInsightsSampleUtils.requiredSetting;

/**
 * Asynchronously enables six-hour recurring Agent Insights runs and reads the next scheduled time.
 * Requires connected Application Insights, a GPT-5-or-later model deployment, and an existing agent
 * without an Insights monitor. Set {@code FOUNDRY_PROJECT_ENDPOINT}, {@code FOUNDRY_AGENT_NAME},
 * and {@code FOUNDRY_MODEL_NAME}. This sample does not wait for a scheduled run.
 */
public class AgentInsightsScheduledAsyncSample {
    public static void main(String[] args) {
        AgentInsightMonitorsAsyncClient monitors = new AIProjectClientBuilder()
            .endpoint(requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAgentInsightMonitorsAsyncClient();

        Mono.usingWhen(
            monitors.createAgentInsightMonitor(
                requiredSetting("FOUNDRY_AGENT_NAME"), requiredSetting("FOUNDRY_MODEL_NAME"), false, null, null),
            monitor -> {
                System.out.printf("Created monitor: %s%n", monitor.getId());
                return monitors.updateAgentInsightMonitor(monitor.getId(),
                    new UpdateAgentInsightMonitorOptions().setRunIntervalHours(6.0).setEnabled(true))
                    .doOnNext(scheduled -> System.out.printf("Interval: %s hours; next run: %s%n",
                        scheduled.getRunIntervalHours(), scheduled.getNextScheduledRunAt()));
            },
            monitor -> deleteMonitor(monitors, monitor.getId())
        ).block();
    }
}
