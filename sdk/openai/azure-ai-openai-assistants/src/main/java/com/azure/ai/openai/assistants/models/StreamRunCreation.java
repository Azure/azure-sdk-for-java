// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.assistants.models;

/**
 * Represents a stream update indicating a change of state in a run step, e.g. creation, completion, etc.
 */
public class StreamRunCreation extends StreamUpdate {
    /**
     * The update with the data about this run step sent by the service.
     */
    private final RunStep message;

    /**
     * Creates a new instance of StreamRunCreation.
     *
     * @param run The {@link RunStep} with the data about this run step sent by the service.
     * @param kind The stream event type associated with this update.
     */
    public StreamRunCreation(RunStep run, AssistantStreamEvent kind) {
        super(kind);
        this.message = run;
    }

    /**
     * Get the update with the data about this run step sent by the service.
     *
     * @return the update with the data about this run step sent by the service.
     */
    public RunStep getMessage() {
        return message;
    }
}
