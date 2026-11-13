// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.AgentInsightMonitor;
import com.azure.ai.projects.models.AgentInsightRun;
import com.azure.ai.projects.models.AgentInsightRunResult;
import com.azure.ai.projects.models.JobStatus;
import com.azure.ai.projects.models.ListAgentInsightRunsOptions;
import com.azure.ai.projects.models.ListAgentInsightsOptions;
import com.azure.ai.projects.models.UpdateAgentInsightMonitorOptions;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentInsightMonitorsAsyncClientTests extends ClientTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void monitorRunAndInsightLifecycle(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        AgentInsightMonitorsAsyncClient client
            = getClientBuilder(httpClient, serviceVersion).buildAgentInsightMonitorsAsyncClient();
        sanitizeInsightContent();
        String agentName = recordedSetting("FOUNDRY_AGENT_NAME", "recorded-agent");
        AgentInsightMonitor monitor
            = client
                .createAgentInsightMonitor(agentName, recordedSetting("FOUNDRY_MODEL_NAME", "recorded-model"), false,
                    null, null)
                .block();
        String id = monitor.getId();
        try {
            assertNotNull(id);
            assertEquals(agentName, client.getAgentInsightMonitor(id).block().getAgentName());
            assertTrue(client.listAgentInsightMonitors(null, null, null, null, agentName)
                .any(item -> id.equals(item.getId()))
                .block());

            assertEquals(6.0,
                client.updateAgentInsightMonitor(id, new UpdateAgentInsightMonitorOptions().setRunIntervalHours(6.0))
                    .block()
                    .getRunIntervalHours());
            PollerFlux<AgentInsightRun, AgentInsightRunResult> poller
                = client.beginCreateAgentInsightRun(id, testResourceNamer.randomUuid(), 3.0)
                    .setPollInterval(Duration.ofSeconds(5));
            AsyncPollResponse<AgentInsightRun, AgentInsightRunResult> completed
                = poller.last().block(Duration.ofMinutes(8));
            AgentInsightRun run = completed.getValue();
            assertNotNull(completed.getFinalResult().block());
            assertEquals(run.getId(), client.getAgentInsightRun(id, run.getId()).block().getId());
            assertTrue(client.listAgentInsightRuns(new ListAgentInsightRunsOptions(id))
                .any(item -> run.getId().equals(item.getId()))
                .block());
            assertNotNull(client.listAgentInsights(new ListAgentInsightsOptions(id)).collectList().block());
            client.resetAgentInsightMonitor(id).block();
        } finally {
            deleteMonitor(client, id);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void cancelInsightRun(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        AgentInsightMonitorsAsyncClient client
            = getClientBuilder(httpClient, serviceVersion).buildAgentInsightMonitorsAsyncClient();
        String id
            = client
                .createAgentInsightMonitor(recordedSetting("FOUNDRY_AGENT_NAME", "recorded-agent"),
                    recordedSetting("FOUNDRY_MODEL_NAME", "recorded-model"), false, null, null)
                .block()
                .getId();
        try {
            AgentInsightRun run = client.beginCreateAgentInsightRun(id, testResourceNamer.randomUuid(), 3.0)
                .next()
                .block(Duration.ofMinutes(2))
                .getValue();
            assertNotNull(run);
            assertEquals(run.getId(), client.cancelAgentInsightRun(id, run.getId()).block().getId());
        } finally {
            deleteMonitor(client, id);
        }
    }

    private void deleteMonitor(AgentInsightMonitorsAsyncClient client, String id) {
        client.updateAgentInsightMonitor(id, new UpdateAgentInsightMonitorOptions().setEnabled(false)).block();
        for (int attempt = 0; attempt < 12; attempt++) {
            boolean active = false;
            for (AgentInsightRun run : client.listAgentInsightRuns(new ListAgentInsightRunsOptions(id))
                .collectList()
                .block()) {
                if (JobStatus.QUEUED.equals(run.getStatus()) || JobStatus.IN_PROGRESS.equals(run.getStatus())) {
                    active = true;
                    try {
                        client.cancelAgentInsightRun(id, run.getId()).block();
                    } catch (ResourceModifiedException ignored) {
                        // The run can finish between listing and cancellation.
                    }
                }
            }
            if (!active) {
                try {
                    client.deleteAgentInsightMonitor(id).block();
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
