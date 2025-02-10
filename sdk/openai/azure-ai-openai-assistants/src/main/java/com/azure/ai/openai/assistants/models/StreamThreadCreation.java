// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.assistants.models;

/**
 * Represents a stream update that indicating the creation of a new thread.
 */
public final class StreamThreadCreation extends StreamUpdate {

    /**
     * The thread data sent in the update by the service.
     */
    private final AssistantThread message;

    /**
     * Creates a new instance of StreamThreadCreation.
     *
     * @param thread The {@link AssistantThread} in the update sent by the service.
     * @param kind The stream event type associated with this update.
     */
    public StreamThreadCreation(AssistantThread thread, AssistantStreamEvent kind) {
        super(kind);
        this.message = thread;
    }

    /**
     * Get the thread data sent in the update by the service.
     *
     * @return the thread data sent in the update by the service.
     */
    public AssistantThread getMessage() {
        return message;
    }
}
