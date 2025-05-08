// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent.models.streaming;

import com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent;
import com.azure.ai.agents.persistent.models.RunStepDeltaChunk;

/**
 * Represents a stream update indicating a change of state in a run step, e.g. creation, completion, etc.
 */
public class StreamRunStepUpdate extends StreamUpdate {

    /**
     * The incremental update sent by the service.
     */
    private final RunStepDeltaChunk message;

    /**
     * Creates a new instance of StreamRunStepUpdate.
     *
     * @param runStepDelta The {@link RunStepDeltaChunk} with the update sent by the service.
     * @param kind The stream event type associated with this update.
     */
    public StreamRunStepUpdate(RunStepDeltaChunk runStepDelta, PersistentAgentStreamEvent kind) {
        super(kind);
        this.message = runStepDelta;
    }

    /**
     * Get the incremental update sent by the service.
     *
     * @return the incremental update sent by the service.
     */
    public RunStepDeltaChunk getMessage() {
        return message;
    }
}
