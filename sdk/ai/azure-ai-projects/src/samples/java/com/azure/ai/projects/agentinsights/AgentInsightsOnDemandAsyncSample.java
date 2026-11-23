// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.agentinsights;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.AgentInsightMonitorsAsyncClient;
import com.azure.ai.projects.models.AgentInsightMonitor;
import com.azure.ai.projects.models.ListAgentInsightsOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.azure.ai.projects.agentinsights.AgentInsightsSampleUtils.deleteMonitor;
import static com.azure.ai.projects.agentinsights.AgentInsightsSampleUtils.requiredSetting;

/**
 * Asynchronously analyzes the last three hours of traces for an existing agent.
 * Requires connected Application Insights, a GPT-5-or-later model deployment, an agent with ingested
 * traces and no existing Insights monitor. Set {@code FOUNDRY_PROJECT_ENDPOINT},
 * {@code FOUNDRY_AGENT_NAME}, and {@code FOUNDRY_MODEL_NAME}.
 */
public class AgentInsightsOnDemandAsyncSample {
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
                return analyze(monitors, monitor);
            },
            monitor -> deleteMonitor(monitors, monitor.getId())
        ).block();
    }

    private static Mono<Void> analyze(AgentInsightMonitorsAsyncClient monitors, AgentInsightMonitor monitor) {
        return monitors.beginCreateAgentInsightRun(monitor.getId(), UUID.randomUUID().toString(), 3.0)
            .last()
            .flatMap(response -> response.getFinalResult())
            .flatMap(result -> {
                System.out.printf("Traces analyzed: %d; insights created: %d%n",
                    result.getTracesAnalyzed(), result.getInsightsCreated());
                return monitors.listAgentInsights(new ListAgentInsightsOptions(monitor.getId()))
                    .concatMap(insight -> monitors.getAgentInsight(monitor.getId(), insight.getId(), true)
                        .doOnNext(details -> System.out.printf("Insight: %s (%s); diagnosis: %s%n",
                            details.getTitle(), details.getStatus(), details.getDescription())))
                    .then();
            });
    }
}
