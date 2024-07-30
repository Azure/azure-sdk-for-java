// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.assistants.implementation.streaming;

import com.azure.ai.openai.assistants.models.AssistantStreamEvent;
import com.azure.ai.openai.assistants.models.MessageDeltaChunk;
import com.azure.ai.openai.assistants.models.RunStep;
import com.azure.ai.openai.assistants.models.RunStepDeltaChunk;
import com.azure.ai.openai.assistants.models.StreamMessageCreation;
import com.azure.ai.openai.assistants.models.StreamMessageUpdate;
import com.azure.ai.openai.assistants.models.StreamRequiredAction;
import com.azure.ai.openai.assistants.models.StreamRunCreation;
import com.azure.ai.openai.assistants.models.StreamRunStepUpdate;
import com.azure.ai.openai.assistants.models.StreamThreadCreation;
import com.azure.ai.openai.assistants.models.StreamThreadRunCreation;
import com.azure.ai.openai.assistants.models.StreamUpdate;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.core.util.BinaryData;

import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_CREATED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_MESSAGE_COMPLETED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_MESSAGE_CREATED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_MESSAGE_DELTA;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_MESSAGE_INCOMPLETE;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_MESSAGE_IN_PROGRESS;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_CANCELLED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_CANCELLING;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_COMPLETED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_CREATED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_EXPIRED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_FAILED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_IN_PROGRESS;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_QUEUED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_REQUIRES_ACTION;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_STEP_CANCELLED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_STEP_COMPLETED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_STEP_CREATED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_STEP_DELTA;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_STEP_EXPIRED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_STEP_FAILED;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.THREAD_RUN_STEP_IN_PROGRESS;

/**
 * Deserializes the server sent event into the appropriate type. Which subtype of {@link StreamUpdate} is used, is
 * determined by the event {@link AssistantStreamEvent}. The subtype is merely a wrapper around the actual deserialized
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
        AssistantStreamEvent event = AssistantStreamEvent.fromString(eventName);

        if (THREAD_CREATED.equals(event)) {
            return new StreamThreadCreation(eventJson.toObject(AssistantThread.class), event);
        } else if  (THREAD_RUN_CREATED.equals(event) ||  THREAD_RUN_QUEUED.equals(event)
            || THREAD_RUN_IN_PROGRESS.equals(event) || THREAD_RUN_COMPLETED.equals(event)
            || THREAD_RUN_CANCELLED.equals(event) || THREAD_RUN_CANCELLING.equals(event)
            || THREAD_RUN_FAILED.equals(event) || THREAD_RUN_EXPIRED.equals(event)) {
            return new StreamThreadRunCreation(eventJson.toObject(ThreadRun.class), event);
        } else if (THREAD_RUN_STEP_CREATED.equals(event) || THREAD_RUN_STEP_IN_PROGRESS.equals(event)
            || THREAD_RUN_STEP_COMPLETED.equals(event) || THREAD_RUN_STEP_FAILED.equals(event)
            || THREAD_RUN_STEP_CANCELLED.equals(event) || THREAD_RUN_STEP_EXPIRED.equals(event)) {
            return new StreamRunCreation(eventJson.toObject(RunStep.class), event);
        } else if (THREAD_MESSAGE_CREATED.equals(event) || THREAD_MESSAGE_IN_PROGRESS.equals(event)
            || THREAD_MESSAGE_COMPLETED.equals(event) || THREAD_MESSAGE_INCOMPLETE.equals(event)) {
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
