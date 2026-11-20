// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.AgentInsight;
import com.azure.ai.projects.models.AgentInsightMonitor;
import com.azure.ai.projects.models.AgentInsightRun;
import com.azure.ai.projects.models.AgentInsightRunResult;
import com.azure.ai.projects.models.AgentInsightStatus;
import com.azure.ai.projects.models.JobStatus;
import com.azure.ai.projects.models.ListAgentInsightRunsOptions;
import com.azure.ai.projects.models.ListAgentInsightsOptions;
import com.azure.ai.projects.models.UpdateAgentInsightMonitorOptions;
import com.azure.ai.projects.models.UpdateAgentInsightOptions;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Iterator;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentInsightMonitorsClientTests extends ClientTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void monitorRunAndInsightLifecycle(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        AgentInsightMonitorsClient client
            = getClientBuilder(httpClient, serviceVersion).buildAgentInsightMonitorsClient();
        sanitizeInsightContent();
        String agentName = recordedSetting("FOUNDRY_AGENT_NAME", "recorded-agent");
        AgentInsightMonitor monitor = client.createAgentInsightMonitor(agentName,
            recordedSetting("FOUNDRY_MODEL_NAME", "recorded-model"), false, null, null);
        String id = monitor.getId();
        try {
            assertNotNull(id);
            assertEquals(agentName, client.getAgentInsightMonitor(id).getAgentName());
            assertTrue(client.listAgentInsightMonitors(null, null, null, null, agentName)
                .stream()
                .anyMatch(item -> id.equals(item.getId())));

            assertEquals(6.0,
                client.updateAgentInsightMonitor(id, new UpdateAgentInsightMonitorOptions().setRunIntervalHours(6.0))
                    .getRunIntervalHours());
            SyncPoller<AgentInsightRun, AgentInsightRunResult> poller
                = client.beginCreateAgentInsightRun(id, testResourceNamer.randomUuid(), 3.0);
            poller.setPollInterval(Duration.ofSeconds(5));
            poller.waitForCompletion(Duration.ofMinutes(8));
            AgentInsightRunResult result = poller.getFinalResult();
            assertNotNull(result);
            AgentInsightRun run = poller.poll().getValue();
            assertEquals(run.getId(), client.getAgentInsightRun(id, run.getId()).getId());
            assertTrue(client.listAgentInsightRuns(new ListAgentInsightRunsOptions(id))
                .stream()
                .anyMatch(item -> run.getId().equals(item.getId())));

            Iterator<AgentInsight> insights = client.listAgentInsights(new ListAgentInsightsOptions(id)).iterator();
            assertTrue(insights.hasNext(), "The test agent needs ingested traces that produce an insight.");
            AgentInsight insight = insights.next();
            assertEquals(insight.getId(), client.getAgentInsight(id, insight.getId(), true).getId());
            AgentInsightMonitorsAsyncClient asyncClient
                = getClientBuilder(httpClient, serviceVersion).buildAgentInsightMonitorsAsyncClient();
            assertTrue(asyncClient.listAgentInsights(new ListAgentInsightsOptions(id))
                .any(item -> insight.getId().equals(item.getId()))
                .block());
            assertEquals(insight.getId(), asyncClient.getAgentInsight(id, insight.getId(), true).block().getId());
            assertEquals(AgentInsightStatus.IGNORED,
                asyncClient
                    .updateAgentInsight(id, insight.getId(),
                        new UpdateAgentInsightOptions().setStatus(AgentInsightStatus.IGNORED))
                    .block()
                    .getStatus());
            assertEquals(AgentInsightStatus.RESOLVED,
                client
                    .updateAgentInsight(id, insight.getId(),
                        new UpdateAgentInsightOptions().setStatus(AgentInsightStatus.RESOLVED))
                    .getStatus());
            client.resetAgentInsightMonitor(id);
        } finally {
            deleteMonitor(client, id);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void cancelInsightRun(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        AgentInsightMonitorsClient client
            = getClientBuilder(httpClient, serviceVersion).buildAgentInsightMonitorsClient();
        String id
            = client
                .createAgentInsightMonitor(recordedSetting("FOUNDRY_AGENT_NAME", "recorded-agent"),
                    recordedSetting("FOUNDRY_MODEL_NAME", "recorded-model"), false, null, null)
                .getId();
        try {
            AgentInsightRun run
                = client.beginCreateAgentInsightRun(id, testResourceNamer.randomUuid(), 3.0).poll().getValue();
            assertNotNull(run);
            assertEquals(run.getId(), client.cancelAgentInsightRun(id, run.getId()).getId());
        } finally {
            deleteMonitor(client, id);
        }
    }

    private void deleteMonitor(AgentInsightMonitorsClient client, String id) {
        client.updateAgentInsightMonitor(id, new UpdateAgentInsightMonitorOptions().setEnabled(false));
        for (int attempt = 0; attempt < 12; attempt++) {
            boolean active = false;
            for (AgentInsightRun run : client.listAgentInsightRuns(new ListAgentInsightRunsOptions(id))) {
                if (JobStatus.QUEUED.equals(run.getStatus()) || JobStatus.IN_PROGRESS.equals(run.getStatus())) {
                    active = true;
                    try {
                        client.cancelAgentInsightRun(id, run.getId());
                    } catch (ResourceModifiedException ignored) {
                        // The run can finish between listing and cancellation.
                    }
                }
            }
            if (!active) {
                try {
                    client.deleteAgentInsightMonitor(id);
                    return;
                } catch (ResourceModifiedException ignored) {
                    // A run can start between listing and deletion.
                }
            }
            sleepIfRunningAgainstService(10000);
        }
        throw new AssertionError("Monitor still has active runs: " + id);
    }
}
