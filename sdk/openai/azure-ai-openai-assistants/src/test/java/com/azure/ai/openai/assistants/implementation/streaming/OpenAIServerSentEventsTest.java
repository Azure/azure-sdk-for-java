// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class OpenAIServerSentEventsTest {

    @Test
    public void fromFileDump() {
        BinaryData testFile = BinaryData.fromFile(AssistantsClientTestBase.openResourceFile("create_thread_run.dump"));
        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testFile.toFluxByteBuffer());

        // data: [DONE] is the last event in the file, but is not emitted by the Flux
        StepVerifier.create(
                openAIServerSentEvents.getEvents()
                    .doOnNext(AssistantsClientTestBase::assertStreamUpdate)
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
            BinaryData.fromString("{\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\","
                + "\"created_at\":1710548044,\"metadata\":{}}").toObject(AssistantThread.class);

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
            BinaryData.fromString("{\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\","
                + "\"created_at\":1710548044,\"metadata\":{}}").toObject(AssistantThread.class);

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
            BinaryData.fromString("{\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\","
                + "\"created_at\":1710548044,\"metadata\":{}}").toObject(AssistantThread.class);

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertInstanceOf(StreamThreadCreation.class, event);
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerSplitBetweenByteBuffers1CLRF() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: thread.created\r\n".getBytes()),
            ByteBuffer.wrap("data: {\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\",\"created_at\":1710548044,\"metadata\":{}}\r".getBytes()),
            ByteBuffer.wrap("\n\r\nevent: done\r\n".getBytes()),
            ByteBuffer.wrap("data: [DONE]\r\n\r\n".getBytes())
        );
        AssistantThread expectedThread =
            BinaryData.fromString("{\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\","
                + "\"created_at\":1710548044,\"metadata\":{}}").toObject(AssistantThread.class);

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertInstanceOf(StreamThreadCreation.class, event);
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerSplitBetweenByteBuffers2CLRF() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: thread.created\r\n".getBytes()),
            ByteBuffer.wrap("data: {\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\",\"created_at\":1710548044,\"metadata\":{}}\r\n".getBytes()),
            ByteBuffer.wrap("\r\nevent: done\r\n".getBytes()),
            ByteBuffer.wrap("data: [DONE]\r\n\r\n".getBytes())
        );
        AssistantThread expectedThread =
            BinaryData.fromString("{\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\","
                + "\"created_at\":1710548044,\"metadata\":{}}").toObject(AssistantThread.class);

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertInstanceOf(StreamThreadCreation.class, event);
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerSplitBetweenByteBuffers3CLRF() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: thread.created\r\n".getBytes()),
            ByteBuffer.wrap("data: {\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\",\"created_at\":1710548044,\"metadata\":{}}\r\n\r".getBytes()),
            ByteBuffer.wrap("\nevent: done\r\n".getBytes()),
            ByteBuffer.wrap("data: [DONE]\r\n\r\n".getBytes())
        );
        AssistantThread expectedThread =
            BinaryData.fromString("{\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\","
                + "\"created_at\":1710548044,\"metadata\":{}}").toObject(AssistantThread.class);

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertInstanceOf(StreamThreadCreation.class, event);
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    @Test
    public void eventChunkDividerSplitBetweenByteBuffers4CLRF() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: thread.created\n".getBytes()),
            ByteBuffer.wrap("data: {\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\",\"created_at\":1710548044,\"metadata\":{}}\n".getBytes()),
            ByteBuffer.wrap("\nevent: done\n".getBytes()),
            ByteBuffer.wrap("data: [DONE]\n\n".getBytes())
        );
        AssistantThread expectedThread =
            BinaryData.fromString("{\"id\":\"thread_yprSWEXT25cgpL8rCwsUchVC\",\"object\":\"thread\","
                + "\"created_at\":1710548044,\"metadata\":{}}").toObject(AssistantThread.class);

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertInstanceOf(StreamThreadCreation.class, event);
                assertAssistantThread(expectedThread, ((StreamThreadCreation) event).getMessage());
            }).verifyComplete();
    }

    private static void assertAssistantThread(AssistantThread expectedThread, AssistantThread actual) {
        assertEquals(expectedThread.getId(), actual.getId());
        assertEquals(expectedThread.getObject(), actual.getObject());
        assertEquals(expectedThread.getCreatedAt(), actual.getCreatedAt());
        assertEquals(expectedThread.getMetadata(), actual.getMetadata());
    }
}
