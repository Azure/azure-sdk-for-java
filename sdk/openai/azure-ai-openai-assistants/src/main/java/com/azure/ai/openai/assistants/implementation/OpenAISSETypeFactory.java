package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent;
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

    public <T extends JsonSerializable<T>> deserializeEvent(BinaryData event, AssistantStreamEvent eventType) {
        return switch (eventType) {
            case THREAD_CREATED -> event.toObject(ThreadRun.class);
            case THREAD_RUN_CREATED -> event.toObject(ThreadRun.class);
            case THEARD_RUN_QUEUED -> event.toObject(ThreadRun.class);
            case THREAD_RUN_IN_PROGRESS -> event.toObject(ThreadRun.class);
            case THREAD_RUN_STEP_CREATED -> event.toObject(RunStep.class);
            case THREAD_RUN_STEP_IN_PROGRESS -> event.toObject(RunStep.class);
            case THREAD_MESSAGE_CREATED -> event.toObject(ThreadMessage.class);
            case THREAD_MESSAGE_IN_PROGRESS -> event.toObject(ThreadMessage.class);
            case THREAD_MESSAGE_DELTA -> event.toObject(.class);
            case THREAD_MESSAGE_COMPLETED -> event.toObject(ThreadRunCompleted.class);
            case THREAD_RUN_COMPLETED -> event.toObject(ThreadRun.class);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
