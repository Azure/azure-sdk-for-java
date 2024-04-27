package com.azure.ai.openai.assistants.implementation.streaming;

import com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent;
import com.azure.ai.openai.assistants.models.MessageDeltaChunk;
import com.azure.ai.openai.assistants.models.RunStep;
import com.azure.ai.openai.assistants.models.RunStepDeltaChunk;
import com.azure.ai.openai.assistants.models.StreamMessageCompletion;
import com.azure.ai.openai.assistants.models.StreamMessageCreation;
import com.azure.ai.openai.assistants.models.StreamMessageUpdate;
import com.azure.ai.openai.assistants.models.StreamRunCreation;
import com.azure.ai.openai.assistants.models.StreamRunStepUpdate;
import com.azure.ai.openai.assistants.models.StreamThreadCreation;
import com.azure.ai.openai.assistants.models.StreamThreadRunCreation;
import com.azure.ai.openai.assistants.models.StreamUpdate;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.core.util.BinaryData;

import java.util.List;

import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.DONE;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.ERROR;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THEARD_RUN_QUEUED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_CREATED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_MESSAGE_COMPLETED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_MESSAGE_CREATED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_MESSAGE_DELTA;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_MESSAGE_IN_PROGRESS;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_COMPLETED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_CREATED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_IN_PROGRESS;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_STEP_COMPLETED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_STEP_CREATED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_STEP_DELTA;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_STEP_IN_PROGRESS;

public final class StreamTypeFactory implements EventStringHandler<StreamUpdate> {

    public StreamUpdate deserializeEvent(String eventName, BinaryData eventJson) throws IllegalArgumentException {
        AssistantStreamEvent event = AssistantStreamEvent.fromString(eventName);

        if (THREAD_CREATED.equals(event)) {
            return new StreamThreadCreation(eventJson.toObject(AssistantThread.class));
        } else if  (THREAD_RUN_CREATED.equals(event) ||  THEARD_RUN_QUEUED.equals(event) ||
                THREAD_RUN_IN_PROGRESS.equals(event) || THREAD_RUN_COMPLETED.equals(event)) {
            return new StreamThreadRunCreation(eventJson.toObject(ThreadRun.class));
        } else if (THREAD_RUN_STEP_CREATED.equals(event) || THREAD_RUN_STEP_IN_PROGRESS.equals(event) ||
                THREAD_RUN_STEP_COMPLETED.equals(event)) {
            return new StreamRunCreation(eventJson.toObject(RunStep.class));
        } else if (THREAD_MESSAGE_CREATED.equals(event) || THREAD_MESSAGE_IN_PROGRESS.equals(event)) {
            return new StreamMessageCreation(eventJson.toObject(ThreadMessage.class));
        } else if (THREAD_MESSAGE_COMPLETED.equals(event)) {
            return new StreamMessageCompletion(eventJson.toObject(ThreadMessage.class));
        } else if (THREAD_MESSAGE_DELTA.equals(event)) {
            return new StreamMessageUpdate(eventJson.toObject(MessageDeltaChunk.class));
        } else if (THREAD_RUN_STEP_DELTA.equals(event)) {
            return new StreamRunStepUpdate(eventJson.toObject(RunStepDeltaChunk.class));

        } else {
            throw new IllegalArgumentException("Unknown event type: " + event);
        }
    }

    /**
     * Handles a collected event from the byte buffer which is formated as a UTF_8 string.
     *
     * @param currentEvent The current line of the server sent event.
     * @param outputValues The list of values to add the current line to.
     * @throws IllegalStateException If the current event contains a server side error.
     */
    @Override
    public void handleCurrentEvent(String currentEvent, List<StreamUpdate> outputValues) throws IllegalArgumentException {
        if (currentEvent.isEmpty()) {
            return;
        }

        // We split the event into the event name and the event data. We don't want to split on \n in the data body.
        String[] lines = currentEvent.split("\n", 2);

        if (lines.length < 2) {
            return;
        }

        String eventName = lines[0].substring(6).trim(); // removing "event:" prefix
        String eventJson = lines[1].substring(5).trim(); // removing "data:" prefix

        if (DONE.equals(AssistantStreamEvent.fromString(eventName))) {
            return;
        }

        if (ERROR.equals(AssistantStreamEvent.fromString(eventName))) {
            throw new IllegalStateException("Server sent event error occurred.");
        }

        outputValues.add(deserializeEvent(eventName, BinaryData.fromString(eventJson)));
    }
}
