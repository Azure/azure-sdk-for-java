package com.azure.ai.projects.models.streaming;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.ai.projects.models.AgentStreamEvent;
import com.azure.ai.projects.models.MessageDeltaChunk;
import com.azure.ai.projects.models.RunStep;
import com.azure.ai.projects.models.RunStepDeltaChunk;
import com.azure.ai.projects.models.ThreadMessage;
import com.azure.ai.projects.models.ThreadRun;
import com.azure.core.util.BinaryData;

import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_CREATED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_MESSAGE_COMPLETED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_MESSAGE_CREATED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_MESSAGE_DELTA;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_MESSAGE_INCOMPLETE;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_MESSAGE_IN_PROGRESS;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_CANCELLED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_CANCELLING;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_COMPLETED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_CREATED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_EXPIRED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_FAILED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_IN_PROGRESS;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_QUEUED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_REQUIRES_ACTION;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_STEP_CANCELLED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_STEP_COMPLETED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_STEP_CREATED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_STEP_DELTA;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_STEP_EXPIRED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_STEP_FAILED;
import static com.azure.ai.projects.models.AgentStreamEvent.THREAD_RUN_STEP_IN_PROGRESS;

/**
 * Deserializes the server sent event into the appropriate type. Which subtype of {@link StreamUpdate} is used, is
 * determined by the event {@link AgentStreamEvent}. The subtype is merely a wrapper around the actual deserialized
 * data.
 */
public final class StreamTypeFactory {

    /**
     * Deserializes the server sent event into the appropriate type.
     *
     * @param eventName The name of the event.
     * @param eventJson The event data.
     * @return The deserialized event.
     * @throws IllegalArgumentException If the event type is unknown.
     */
    public StreamUpdate deserializeEvent(String eventName, BinaryData eventJson) throws IllegalArgumentException {
        AgentStreamEvent event = AgentStreamEvent.fromString(eventName);

        if (THREAD_CREATED.equals(event)) {
            return new StreamThreadCreation(eventJson.toObject(AgentStreamEvent.class), event);
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
            throw new IllegalArgumentException("Unknown event type: " + event);
        }
    }
}
