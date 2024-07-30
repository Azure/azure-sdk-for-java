// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.assistants.models;

/**
 * Represents a stream update indicating that input from the user is required.
 */
public class StreamRequiredAction extends StreamUpdate {

    /**
     * The message detailing the action required by the service.
     */
    private final ThreadRun message;

    /**
     * Creates a new instance of StreamRequiredAction.
     *
     * @param action The {@link ThreadRun} with the action required by the service.
     * @param kind The stream event type associated with this update.
     */
    public StreamRequiredAction(ThreadRun action, AssistantStreamEvent kind) {
        super(kind);
        this.message = action;
    }

    /**
     * Get the message detailing the action required by the service.
     *
     * @return the message detailing the action required by the service.
     */
    public ThreadRun getMessage() {
        return message;
    }
}
