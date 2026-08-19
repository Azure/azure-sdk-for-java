// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.InvokeAgentResponsesApiRoutineAction;
import com.azure.ai.projects.models.RoutineAction;
import com.azure.ai.projects.models.RoutineRun;
import com.azure.ai.projects.models.RoutineRunPhase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Shared helpers for the routines samples: building an agent action, polling for a completed
 * {@link RoutineRun}, and cleaning up routines.
 */
final class RoutinesSampleUtils {

    private RoutinesSampleUtils() {
    }

    /**
     * Builds an action that dispatches the routine to the supplied hosted agent through the responses API.
     *
     * @param agentName the hosted agent name.
     * @return the routine action.
     */
    static RoutineAction agentAction(String agentName) {
        return new InvokeAgentResponsesApiRoutineAction().setAgentName(agentName);
    }

    /**
     * Returns {@code true} when the run has reached a terminal phase or status.
     *
     * @param status the run status.
     * @return whether the status is terminal.
     */
    static boolean isTerminalStatus(String status) {
        return "finished".equalsIgnoreCase(status)
            || "completed".equalsIgnoreCase(status)
            || "failed".equalsIgnoreCase(status)
            || "killed".equalsIgnoreCase(status);
    }

    private static boolean isTerminalPhase(RoutineRunPhase phase) {
        return phase != null
            && ("completed".equalsIgnoreCase(phase.toString()) || "failed".equalsIgnoreCase(phase.toString()));
    }

    private static boolean isTerminalRun(RoutineRun run) {
        return isTerminalPhase(run.getPhase()) || isTerminalStatus(run.getStatus());
    }

    private static boolean isFailedRun(RoutineRun run) {
        return (run.getPhase() != null && "failed".equalsIgnoreCase(run.getPhase().toString()))
            || "failed".equalsIgnoreCase(run.getStatus())
            || "killed".equalsIgnoreCase(run.getStatus());
    }

    private static void printRun(RoutineRun run) {
        System.out.printf("    - run ID %s, status: %s, phase: %s, trigger type: %s, dispatch ID: %s, "
                + "triggered at: %s, ended at: %s%n",
            run.getId(), run.getStatus(), run.getPhase(), run.getTriggerType(), run.getDispatchId(),
            run.getTriggeredAt() == null ? "<not triggered yet>" : run.getTriggeredAt(),
            run.getEndedAt() == null ? "<not ended yet>" : run.getEndedAt());
    }

    /**
     * Synchronously polls the routine's runs until one reaches a terminal phase or the timeout elapses.
     *
     * @param routinesClient the routines client.
     * @param routineName the routine name.
     * @param timeout the maximum time to wait.
     * @return the completed run, or {@code null} if none completed before the timeout.
     */
    static RoutineRun waitForCompletedRun(BetaRoutinesClient routinesClient, String routineName, Duration timeout) {
        return waitForCompletedRun(routinesClient, routineName, null, timeout);
    }

    /**
     * Synchronously polls a specific dispatched run until it reaches a terminal phase or the timeout elapses.
     *
     * @param routinesClient the routines client.
     * @param routineName the routine name.
     * @param dispatchId the dispatch identifier to follow.
     * @param timeout the maximum time to wait.
     * @return the completed run, or {@code null} if it did not complete before the timeout.
     */
    static RoutineRun waitForCompletedRun(BetaRoutinesClient routinesClient, String routineName, String dispatchId,
        Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            RoutineRun completed = null;
            for (RoutineRun run : routinesClient.listRoutineRuns(routineName)) {
                printRun(run);
                if ((dispatchId == null || dispatchId.equals(run.getDispatchId())) && isTerminalRun(run)) {
                    completed = run;
                }
            }
            if (completed != null) {
                return completed;
            }
            try {
                Thread.sleep(Duration.ofSeconds(10).toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for the routine run.", e);
            }
        }
        return null;
    }

    /**
     * Asynchronously polls the routine's runs until one reaches a terminal phase or the timeout elapses.
     *
     * @param routinesAsyncClient the asynchronous routines client.
     * @param routineName the routine name.
     * @param timeout the maximum time to wait.
     * @return a {@link Mono} that emits the completed run, or completes empty if none completed before the timeout.
     */
    static Mono<RoutineRun> waitForCompletedRunAsync(BetaRoutinesAsyncClient routinesAsyncClient, String routineName,
        Duration timeout) {
        return waitForCompletedRunAsync(routinesAsyncClient, routineName, null, timeout);
    }

    /**
     * Asynchronously polls a specific dispatched run until it reaches a terminal phase or the timeout elapses.
     *
     * @param routinesAsyncClient the asynchronous routines client.
     * @param routineName the routine name.
     * @param dispatchId the dispatch identifier to follow.
     * @param timeout the maximum time to wait.
     * @return a {@link Mono} that emits the completed run, or completes empty if it did not complete before the
     * timeout.
     */
    static Mono<RoutineRun> waitForCompletedRunAsync(BetaRoutinesAsyncClient routinesAsyncClient, String routineName,
        String dispatchId, Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        return Flux.interval(Duration.ZERO, Duration.ofSeconds(10))
            .takeWhile(tick -> Instant.now().isBefore(deadline))
            .concatMap(tick -> routinesAsyncClient.listRoutineRuns(routineName)
                .doOnNext(RoutinesSampleUtils::printRun)
                .filter(run -> (dispatchId == null || dispatchId.equals(run.getDispatchId())) && isTerminalRun(run))
                .next())
            .next();
    }

    static void reportRun(RoutineRun completedRun, Duration timeout) {
        if (completedRun == null) {
            System.out.printf("The run did not complete within %d seconds.%n", timeout.getSeconds());
            return;
        }
        if (isFailedRun(completedRun)) {
            System.out.printf("The run failed. Type: %s Message: %s%n",
                completedRun.getErrorType(), completedRun.getErrorMessage());
            return;
        }
        System.out.printf("The response Id is %s%n", completedRun.getResponseId());
    }
}
