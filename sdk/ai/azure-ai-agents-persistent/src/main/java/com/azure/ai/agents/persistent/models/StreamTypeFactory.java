// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent.models;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;

import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_CREATED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_MESSAGE_COMPLETED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_MESSAGE_CREATED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_MESSAGE_DELTA;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_MESSAGE_INCOMPLETE;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_MESSAGE_IN_PROGRESS;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_CANCELLED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_CANCELLING;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_COMPLETED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_CREATED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_EXPIRED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_FAILED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_IN_PROGRESS;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_QUEUED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_REQUIRES_ACTION;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_STEP_CANCELLED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_STEP_COMPLETED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_STEP_CREATED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_STEP_DELTA;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_STEP_EXPIRED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_STEP_FAILED;
import static com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent.THREAD_RUN_STEP_IN_PROGRESS;

/**
 * Deserializes the server sent event into the appropriate type. Which subtype of {@link StreamUpdate} is used, is
 * determined by the event {@link PersistentAgentStreamEvent}. The subtype is merely a wrapper around the actual deserialized
 * data.
 */
public final class StreamTypeFactory {
    private final ClientLogger logger = new ClientLogger(StreamTypeFactory.class);

    /**
     * Default constructor for creating a new instance of StreamTypeFactory.
     */
    public StreamTypeFactory() {
        // Default constructor
    }

    /**
     * Deserializes the server sent event into the appropriate type.
     *
     * @param eventName The name of the event.
     * @param eventJson The event data.
     * @return The deserialized event.
     * @throws IllegalArgumentException If the event type is unknown.
     */
    public StreamUpdate deserializeEvent(String eventName, BinaryData eventJson) throws IllegalArgumentException {
        PersistentAgentStreamEvent event = PersistentAgentStreamEvent.fromString(eventName);

        if (THREAD_CREATED.equals(event)) {
            return new StreamThreadCreation(eventJson.toObject(PersistentAgentStreamEvent.class), event);
        } else if (THREAD_RUN_CREATED.equals(event)
            || THREAD_RUN_QUEUED.equals(event)
            || THREAD_RUN_IN_PROGRESS.equals(event)
            || THREAD_RUN_COMPLETED.equals(event)
            || THREAD_RUN_CANCELLED.equals(event)
            || THREAD_RUN_CANCELLING.equals(event)
            || THREAD_RUN_FAILED.equals(event)
            || THREAD_RUN_EXPIRED.equals(event)) {
            return new StreamThreadRunCreation(eventJson.toObject(ThreadRun.class), event);
        } else if (THREAD_RUN_STEP_CREATED.equals(event)
            || THREAD_RUN_STEP_IN_PROGRESS.equals(event)
            || THREAD_RUN_STEP_COMPLETED.equals(event)
            || THREAD_RUN_STEP_FAILED.equals(event)
            || THREAD_RUN_STEP_CANCELLED.equals(event)
            || THREAD_RUN_STEP_EXPIRED.equals(event)) {
            return new StreamRunCreation(eventJson.toObject(RunStep.class), event);
        } else if (THREAD_MESSAGE_CREATED.equals(event)
            || THREAD_MESSAGE_IN_PROGRESS.equals(event)
            || THREAD_MESSAGE_COMPLETED.equals(event)
            || THREAD_MESSAGE_INCOMPLETE.equals(event)) {
            return new StreamMessageCreation(eventJson.toObject(ThreadMessage.class), event);
        } else if (THREAD_MESSAGE_DELTA.equals(event)) {
            return new StreamMessageUpdate(eventJson.toObject(MessageDeltaChunk.class), event);
        } else if (THREAD_RUN_STEP_DELTA.equals(event)) {
            return new StreamRunStepUpdate(eventJson.toObject(RunStepDeltaChunk.class), event);
        } else if (THREAD_RUN_REQUIRES_ACTION.equals(event)) {
            return new StreamRequiredAction(eventJson.toObject(ThreadRun.class), event);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException("Unknown event type: " + event));
        }
    }
}
