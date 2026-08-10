// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.models.MemoryStoreUpdateStatus;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;

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

    private AgentsServicePollUtils() {
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
        } else if (MemoryStoreUpdateStatus.SUPERSEDED.toString().equalsIgnoreCase(name)) {
            return LongRunningOperationStatus.USER_CANCELLED;
        }
        return status;
    }
}
