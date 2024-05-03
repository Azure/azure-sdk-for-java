package com.azure.ai.openai.assistants.implementation.streaming;

import com.azure.ai.openai.assistants.AssistantsClientTestBase;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.StreamThreadCreation;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAIServerSentEventsTest {

    @Test
    public void fromFileDump() {
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
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerInNextByteBuffer() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: thread.created\n".getBytes()),
            ByteBuffer.wrap("data: {\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\",\"created_at\":1710548044,\"metadata\":{}}".getBytes()),
            ByteBuffer.wrap("\n\nevent: done\n".getBytes()),
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
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerSplitBetweenByteBuffers() {
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
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerSplitBetweenByteBuffers1_CLRF() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: thread.created\r\n".getBytes()),
            ByteBuffer.wrap("data: {\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\",\"created_at\":1710548044,\"metadata\":{}}\r".getBytes()),
            ByteBuffer.wrap("\n\r\nevent: done\r\n".getBytes()),
            ByteBuffer.wrap("data: [DONE]\r\n\r\n".getBytes())
        );
        AssistantThread expectedThread =
            BinaryData.fromString("""
                {"id":"thread_yprSWEXT25cgpL8rCwsUchVC","object":"thread","created_at":1710548044,"metadata":{}}
                """).toObject(AssistantThread.class);

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertInstanceOf(StreamThreadCreation.class, event);
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerSplitBetweenByteBuffers2_CLRF() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: thread.created\r\n".getBytes()),
            ByteBuffer.wrap("data: {\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\",\"created_at\":1710548044,\"metadata\":{}}\r\n".getBytes()),
            ByteBuffer.wrap("\r\nevent: done\r\n".getBytes()),
            ByteBuffer.wrap("data: [DONE]\r\n\r\n".getBytes())
        );
        AssistantThread expectedThread =
            BinaryData.fromString("""
                {"id":"thread_yprSWEXT25cgpL8rCwsUchVC","object":"thread","created_at":1710548044,"metadata":{}}
                """).toObject(AssistantThread.class);

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertInstanceOf(StreamThreadCreation.class, event);
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerSplitBetweenByteBuffers3_CLRF() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: thread.created\r\n".getBytes()),
            ByteBuffer.wrap("data: {\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\",\"created_at\":1710548044,\"metadata\":{}}\r\n\r".getBytes()),
            ByteBuffer.wrap("\nevent: done\r\n".getBytes()),
            ByteBuffer.wrap("data: [DONE]\r\n\r\n".getBytes())
        );
        AssistantThread expectedThread =
            BinaryData.fromString("""
                {"id":"thread_yprSWEXT25cgpL8rCwsUchVC","object":"thread","created_at":1710548044,"metadata":{}}
                """).toObject(AssistantThread.class);

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertInstanceOf(StreamThreadCreation.class, event);
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerSplitBetweenByteBuffers4_CLRF() {
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
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void errorEvent() {
        String errorEvent = "event: error\n" +
            "data: {\"error\":{\"message\":\"The server had an error processing your request. Sorry about that! " +
            "You can retry your request, or contact us through our help center at help.openai.com if you keep " +
            "seeing this error. (Please include the request ID req_23c32c4251b3cab04bce519f14d4801f in your email.)\"," +
            "\"type\":\"server_error\",\"param\":null,\"code\":null}}\n\n";
    }

    private static void assertAssistantThread(AssistantThread expectedThread, AssistantThread actual) {
        assertEquals(expectedThread.getId(), actual.getId());
        assertEquals(expectedThread.getObject(), actual.getObject());
        assertEquals(expectedThread.getCreatedAt(), actual.getCreatedAt());
        assertEquals(expectedThread.getMetadata(), actual.getMetadata());
    }
}
