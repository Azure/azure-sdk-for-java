package com.azure.ai.openai.assistants.implementation.streaming;

public class OpenAISSETypeFactory {

//    public <T extends JsonSerializable<T>> T deserializeEvent(BinaryData event, AssistantStreamEvent eventType) {
//        if (THREAD_CREATED.equals(eventType) || THREAD_RUN_CREATED.equals(eventType) ||
//            THEARD_RUN_QUEUED.equals(eventType) || THREAD_RUN_IN_PROGRESS.equals(eventType) ||
//            THREAD_RUN_COMPLETED.equals(eventType)) {
//            return event.toObject(ThreadRun.class);
//        } else if (THREAD_RUN_STEP_CREATED.equals(eventType) || THREAD_RUN_STEP_IN_PROGRESS.equals(eventType)) {
//            return event.toObject(RunStep.class);
//        } else if (THREAD_MESSAGE_CREATED.equals(eventType) || THREAD_MESSAGE_IN_PROGRESS.equals(eventType)
//            || THREAD_MESSAGE_COMPLETED.equals(eventType)) {
//            return event.toObject(ThreadMessage.class);
//        } else if(THREAD_MESSAGE_DELTA.equals(eventType)) {
//            return event.toObject(MessageDeltaChunk.class);
//        } else {
//            throw new IllegalArgumentException("Unknown event type: " + eventType);
//        }
//    }
}
