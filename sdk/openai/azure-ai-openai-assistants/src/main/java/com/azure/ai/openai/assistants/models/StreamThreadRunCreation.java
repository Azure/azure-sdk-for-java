// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.assistants.models;

/**
 * Represents a stream update indicating a change of state in a thread run, e.g. creation, completion, etc.
 */
public final class StreamThreadRunCreation extends StreamUpdate {

    /**
     * The thread run with the update sent by the service.
     */
    private final ThreadRun message;

    /**
     * Creates a new instance of StreamThreadRunCreation.
     *
     * @param threadRun The {@link ThreadRun} with the update sent by the service.
     * @param kind The stream event type associated with this update.
     */
    public StreamThreadRunCreation(ThreadRun threadRun, AssistantStreamEvent kind) {
        super(kind);
        this.message = threadRun;
    }

    /**
     * Get the thread run with the update sent by the service.
     *
     * @return the thread run with the update sent by the service.
     */
    public ThreadRun getMessage() {
        return message;
    }
}
