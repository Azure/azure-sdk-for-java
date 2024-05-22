package com.azure.ai.openai.assistants.models;

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
     */
    public StreamRunStepUpdate(RunStepDeltaChunk runStepDelta, AssistantStreamEvent kind) {
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
