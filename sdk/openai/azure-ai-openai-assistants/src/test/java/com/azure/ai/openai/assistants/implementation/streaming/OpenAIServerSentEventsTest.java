package com.azure.ai.openai.assistants.implementation.streaming;

import com.azure.ai.openai.assistants.AssistantsClientTestBase;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.StreamThreadCreation;
import com.azure.ai.openai.assistants.models.StreamUpdate;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAIServerSentEventsTest {

    @Test
    public void fromDumpFile() {
        BinaryData testFile = BinaryData.fromFile(AssistantsClientTestBase.openResourceFile("create_thread_run.dump"));
        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testFile.toFluxByteBuffer());

        // data: [DONE] is the last event in the file, but is not emitted by the Flux
        StepVerifier.create(
                openAIServerSentEvents.getEvents()
                    .doOnNext(event -> {
                        assertFalse(BinaryData.fromObject(event).toString().isBlank());
                    })
            ).expectNextCount(30)
            .verifyComplete();
    }

    @Test
    public void eventTypeInDifferentByteBuffer() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: thread.created\n".getBytes()),
            ByteBuffer.wrap("data: {\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\",\"created_at\":1710548044,\"metadata\":{}}\n\n".getBytes()),
            ByteBuffer.wrap("event: done\n".getBytes()),
            ByteBuffer.wrap("data: [DONE]\n\n".getBytes())
        );
        AssistantThread expectedThread =
            BinaryData.fromString("""
                {"id":"thread_yprSWEXT25cgpL8rCwsUchVC","object":"thread","created_at":1710548044,"metadata":{}}
                """).toObject(AssistantThread.class);

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertInstanceOf(StreamThreadCreation.class, event);
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getThread());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerInSplitByteBuffers() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: thread.created\n".getBytes()),
            ByteBuffer.wrap("data: {\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\",\"created_at\":1710548044,\"metadata\":{}}\n".getBytes()),
            ByteBuffer.wrap("\nevent: done\n".getBytes()),
            ByteBuffer.wrap("data: [DONE]\n\n".getBytes())
        );
        AssistantThread expectedThread =
            BinaryData.fromString("""
                {"id":"thread_yprSWEXT25cgpL8rCwsUchVC","object":"thread","created_at":1710548044,"metadata":{}}
                """).toObject(AssistantThread.class);

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertInstanceOf(StreamThreadCreation.class, event);
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getThread());
            }).verifyComplete();
    }

    private static void assertAssistantThread(AssistantThread expectedThread, AssistantThread actual) {
        assertEquals(expectedThread.getId(), actual.getId());
        assertEquals(expectedThread.getObject(), actual.getObject());
        assertEquals(expectedThread.getCreatedAt(), actual.getCreatedAt());
        assertEquals(expectedThread.getMetadata(), actual.getMetadata());
    }
}
