package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent;
import com.azure.ai.openai.assistants.models.MessageDeltaChunk;
import com.azure.ai.openai.assistants.models.MessageDeltaContent;
import com.azure.ai.openai.assistants.models.RunStep;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonSerializable;

import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THEARD_RUN_QUEUED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_CREATED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_MESSAGE_COMPLETED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_MESSAGE_CREATED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_MESSAGE_DELTA;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_MESSAGE_IN_PROGRESS;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_COMPLETED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_CREATED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_IN_PROGRESS;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_STEP_CREATED;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.THREAD_RUN_STEP_IN_PROGRESS;

public class OpenAISSETypeFactory {

    public <T extends JsonSerializable<T>> T deserializeEvent(BinaryData event, AssistantStreamEvent eventType) {
        if (THREAD_CREATED.equals(eventType) || THREAD_RUN_CREATED.equals(eventType) ||
            THEARD_RUN_QUEUED.equals(eventType) || THREAD_RUN_IN_PROGRESS.equals(eventType) ||
            THREAD_RUN_COMPLETED.equals(eventType)) {
            return event.toObject(ThreadRun.class);
        } else if (THREAD_RUN_STEP_CREATED.equals(eventType) || THREAD_RUN_STEP_IN_PROGRESS.equals(eventType)) {
            return event.toObject(RunStep.class);
        } else if (THREAD_MESSAGE_CREATED.equals(eventType) || THREAD_MESSAGE_IN_PROGRESS.equals(eventType)
            || THREAD_MESSAGE_COMPLETED.equals(eventType)) {
            return event.toObject(ThreadMessage.class);
        } else if(THREAD_MESSAGE_DELTA.equals(eventType)) {
            return event.toObject(MessageDeltaChunk.class);
        } else {
            throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
    }
}
