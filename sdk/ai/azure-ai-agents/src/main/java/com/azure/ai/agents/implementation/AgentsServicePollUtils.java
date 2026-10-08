// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.models.MemoryStoreUpdateStatus;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.serializer.TypeReference;
import java.util.Map;

/**
 * Shared polling helpers for the Agents SDK.
 *
 * <p>The generated {@code OperationLocationPollingStrategy} / {@code SyncOperationLocationPollingStrategy}
 * delegate here so that the two strategies stay in sync and only minimal edits are needed in the
 * generated files.</p>
 *
 * <p>This class is package-private; it is <b>not</b> part of the public API.</p>
 */
final class AgentsServicePollUtils {
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE
        = new TypeReference<Map<String, Object>>() {
        };

    private AgentsServicePollUtils() {
    }

    static String getOptimizationPollingUrl(Object initialResponse, String endpoint) {
        if (!(initialResponse instanceof BinaryData)) {
            throw new IllegalStateException("The optimization create response did not contain a response body.");
        }
        Map<String, Object> job = ((BinaryData) initialResponse).toObject(MAP_TYPE_REFERENCE);
        Object jobId = job.get("id");
        if (jobId == null || jobId.toString().isEmpty()) {
            throw new IllegalStateException("The optimization create response did not contain a job ID.");
        }
        return endpoint + (endpoint.endsWith("/") ? "" : "/") + "agent_optimization_jobs/" + jobId;
    }

    /**
     * Remaps a {@link PollResponse} whose status may contain a custom service terminal state
     * ({@code "completed"}, {@code "superseded"}) that the base {@code OperationResourcePollingStrategy}
     * cannot recognize.  If no remapping is needed the original response is returned as-is.
     *
     * <p>The Memory Stores service defines:</p>
     * <ul>
     *   <li>{@code "completed"}  {@link LongRunningOperationStatus#SUCCESSFULLY_COMPLETED}</li>
     *   <li>{@code "superseded"} {@link LongRunningOperationStatus#USER_CANCELLED}</li>
     * </ul>
     */
    static <T> PollResponse<T> remapStatus(PollResponse<T> response) {
        LongRunningOperationStatus status = response.getStatus();
        LongRunningOperationStatus mapped = mapCustomStatus(status);
        if (mapped == status) {
            return response;
        }
        return new PollResponse<>(mapped, response.getValue(), response.getRetryAfter());
    }

    private static LongRunningOperationStatus mapCustomStatus(LongRunningOperationStatus status) {
        // Standard statuses (Succeeded, Failed, Canceled, InProgress, NotStarted) are already
        // mapped correctly by the parent's PollResult; only remap the custom ones.
        String name = status.toString();
        if (MemoryStoreUpdateStatus.COMPLETED.toString().equalsIgnoreCase(name)) {
            return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (MemoryStoreUpdateStatus.SUPERSEDED.toString().equalsIgnoreCase(name)
            // Optimization jobs and telephony use "cancelled"; MemoryStoreUpdateStatus intentionally has no CANCELLED.
            || "cancelled".equalsIgnoreCase(name)) {
            return LongRunningOperationStatus.USER_CANCELLED;
        }
        return status;
    }
}
