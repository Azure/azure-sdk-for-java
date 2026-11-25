// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.agentinsights;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.AgentInsightMonitorsClient;
import com.azure.ai.projects.models.AgentInsight;
import com.azure.ai.projects.models.AgentInsightMonitor;
import com.azure.ai.projects.models.AgentInsightRun;
import com.azure.ai.projects.models.AgentInsightRunResult;
import com.azure.ai.projects.models.ListAgentInsightsOptions;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.UUID;

import static com.azure.ai.projects.agentinsights.AgentInsightsSampleUtils.deleteMonitor;
import static com.azure.ai.projects.agentinsights.AgentInsightsSampleUtils.requiredSetting;

/**
 * Analyzes the last three hours of traces for an existing agent and lists its insights.
 * The project must have connected Application Insights, a GPT-5-or-later model deployment, and
 * an agent with ingested traces. Use an agent without an existing Insights monitor.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT}, {@code FOUNDRY_AGENT_NAME}, and {@code FOUNDRY_MODEL_NAME}.
 */
public class AgentInsightsOnDemandSample {
    public static void main(String[] args) {
        AgentInsightMonitorsClient monitors = new AIProjectClientBuilder()
            .endpoint(requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAgentInsightMonitorsClient();

        AgentInsightMonitor monitor = monitors.createAgentInsightMonitor(
            requiredSetting("FOUNDRY_AGENT_NAME"), requiredSetting("FOUNDRY_MODEL_NAME"), false, null, null);
        System.out.printf("Created monitor: %s%n", monitor.getId());
        try {
            SyncPoller<AgentInsightRun, AgentInsightRunResult> poller
                = monitors.beginCreateAgentInsightRun(monitor.getId(), UUID.randomUUID().toString(), 3.0);
            AgentInsightRunResult result = poller.getFinalResult();
            System.out.printf("Traces analyzed: %d; insights created: %d%n",
                result.getTracesAnalyzed(), result.getInsightsCreated());

            for (AgentInsight insight : monitors.listAgentInsights(new ListAgentInsightsOptions(monitor.getId()))) {
                System.out.printf("Insight: %s (%s)%n", insight.getTitle(), insight.getStatus());
                AgentInsight details = monitors.getAgentInsight(monitor.getId(), insight.getId(), true);
                System.out.printf("Diagnosis: %s%n", details.getDescription());
            }
        } finally {
            deleteMonitor(monitors, monitor.getId());
        }
    }
}
