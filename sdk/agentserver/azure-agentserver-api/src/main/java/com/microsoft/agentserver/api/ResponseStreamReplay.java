// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import java.util.List;

/**
 * The result of replaying the stored Server-Sent Events for a background
 * streaming response.
 * <p>
 * Each {@link ReplayEvent} carries the SSE event name and the JSON payload exactly
 * as it was emitted (including the {@code sequence_number} field). The adapter
 * writes them to the wire as {@code event: {name}\ndata: {data}\n\n}.
 *
 * @param events the ordered list of replayable events (already filtered by the
 *               {@code starting_after} cursor when provided)
 */
public record ResponseStreamReplay(List<ReplayEvent> events) {

    /**
     * A single replayable SSE event.
     *
     * @param sequenceNumber the 0-based monotonically increasing sequence number
     * @param eventName      the SSE event name, e.g. {@code "response.created"}
     * @param data           the JSON payload (includes {@code sequence_number})
     */
    public record ReplayEvent(long sequenceNumber, String eventName, String data) {
    }
}

