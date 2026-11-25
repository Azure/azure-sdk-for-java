// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.agentinsights;

import com.azure.ai.projects.AgentInsightMonitorsAsyncClient;
import com.azure.ai.projects.AgentInsightMonitorsClient;
import com.azure.ai.projects.models.AgentInsightRun;
import com.azure.ai.projects.models.JobStatus;
import com.azure.ai.projects.models.ListAgentInsightRunsOptions;
import com.azure.ai.projects.models.UpdateAgentInsightMonitorOptions;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.util.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

final class AgentInsightsSampleUtils {
    private static final int CLEANUP_ATTEMPTS = 12;

    private AgentInsightsSampleUtils() {
    }

    static String requiredSetting(String name) {
        return Objects.requireNonNull(Configuration.getGlobalConfiguration().get(name),
            "Set the " + name + " environment variable before running this sample.");
    }

    static void deleteMonitor(AgentInsightMonitorsClient monitors, String monitorId) {
        monitors.updateAgentInsightMonitor(monitorId, new UpdateAgentInsightMonitorOptions().setEnabled(false));
        for (int attempt = 0; attempt < CLEANUP_ATTEMPTS; attempt++) {
            boolean hasActiveRuns = false;
            for (AgentInsightRun run : monitors.listAgentInsightRuns(new ListAgentInsightRunsOptions(monitorId))) {
                if (isActive(run)) {
                    hasActiveRuns = true;
                    try {
                        monitors.cancelAgentInsightRun(monitorId, run.getId());
                    } catch (ResourceModifiedException ignored) {
                        // The run can finish between listing and cancellation.
                    }
                }
            }
            if (!hasActiveRuns) {
                try {
                    monitors.deleteAgentInsightMonitor(monitorId);
                    return;
                } catch (ResourceModifiedException ignored) {
                    // A scheduled run can start between listing and deletion.
                }
            }
            if (attempt < CLEANUP_ATTEMPTS - 1) {
                try {
                    Thread.sleep(Duration.ofSeconds(10).toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while cleaning up monitor " + monitorId, e);
                }
            }
        }
        throw new IllegalStateException("Monitor " + monitorId + " still has active runs; remove it manually.");
    }

    static Mono<Void> deleteMonitor(AgentInsightMonitorsAsyncClient monitors, String monitorId) {
        return monitors.updateAgentInsightMonitor(monitorId, new UpdateAgentInsightMonitorOptions().setEnabled(false))
            .then(deleteMonitor(monitors, monitorId, 0));
    }

    private static Mono<Void> deleteMonitor(AgentInsightMonitorsAsyncClient monitors, String monitorId, int attempt) {
        return monitors.listAgentInsightRuns(new ListAgentInsightRunsOptions(monitorId))
            .filter(AgentInsightsSampleUtils::isActive)
            .map(AgentInsightRun::getId)
            .collectList()
            .flatMap(runIds -> Flux.fromIterable(runIds)
                .concatMap(runId -> monitors.cancelAgentInsightRun(monitorId, runId)
                    .onErrorResume(ResourceModifiedException.class, ignored -> Mono.empty()))
                .then(runIds.isEmpty()
                    ? monitors.deleteAgentInsightMonitor(monitorId)
                        .onErrorResume(ResourceModifiedException.class, ignored -> retry(monitors, monitorId, attempt))
                    : retry(monitors, monitorId, attempt)));
    }

    private static Mono<Void> retry(AgentInsightMonitorsAsyncClient monitors, String monitorId, int attempt) {
        if (attempt >= CLEANUP_ATTEMPTS - 1) {
            return Mono.error(new IllegalStateException(
                "Monitor " + monitorId + " still has active runs; remove it manually."));
        }
        return Mono.delay(Duration.ofSeconds(10))
            .then(Mono.defer(() -> deleteMonitor(monitors, monitorId, attempt + 1)));
    }

    private static boolean isActive(AgentInsightRun run) {
        return JobStatus.QUEUED.equals(run.getStatus()) || JobStatus.IN_PROGRESS.equals(run.getStatus());
    }
}
