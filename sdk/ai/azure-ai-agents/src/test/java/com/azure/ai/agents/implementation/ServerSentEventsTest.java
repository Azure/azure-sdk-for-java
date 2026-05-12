// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.models.SessionLogEvent;
import com.azure.ai.agents.models.SessionLogEventType;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ServerSentEvents}.
 */
public class ServerSentEventsTest {
    @Test
    public void emptyFluxByteBuffer() {
        ServerSentEvents<SessionLogEvent> serverSentEvents
            = new ServerSentEvents<>(Flux.empty(), SessionLogEvent.class);

        StepVerifier.create(serverSentEvents.getEvents()).verifyComplete();
    }

    @Test
    public void singleDataEventWithoutTrailingDelimiter() {
        ServerSentEvents<SessionLogEvent> serverSentEvents = new ServerSentEvents<>(
            Flux.just(toByteBuffer("data: " + sessionLogEventJson("value"))), SessionLogEvent.class);

        StepVerifier.create(serverSentEvents.getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "value"))
            .verifyComplete();
    }

    @Test
    public void multipleDataEventsInSingleByteBuffer() {
        String sse = "data: " + sessionLogEventJson("value") + "\n\n" + "data: " + sessionLogEventJson("value2");
        ServerSentEvents<SessionLogEvent> serverSentEvents
            = new ServerSentEvents<>(Flux.just(toByteBuffer(sse)), SessionLogEvent.class);

        StepVerifier.create(serverSentEvents.getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "value"))
            .assertNext(event -> assertSessionLogEvent(event, "value2"))
            .verifyComplete();
    }

    @Test
    public void multipleDataEventsSplitAcrossByteBuffers() {
        ServerSentEvents<SessionLogEvent> serverSentEvents
            = new ServerSentEvents<>(Flux.just(toByteBuffer("data: " + sessionLogEventJson("value")),
                toByteBuffer("\n\ndata: " + sessionLogEventJson("value2"))), SessionLogEvent.class);

        StepVerifier.create(serverSentEvents.getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "value"))
            .assertNext(event -> assertSessionLogEvent(event, "value2"))
            .verifyComplete();
    }

    @Test
    public void dataEventJsonSplitAcrossByteBuffers() {
        String sse1 = "data: " + sessionLogEventJson("value");
        String sse2 = "data: " + sessionLogEventJson("value2");

        ServerSentEvents<SessionLogEvent> serverSentEvents = new ServerSentEvents<>(
            Flux.just(toByteBuffer(sse1 + "\n\n" + sse2.substring(0, 10)), toByteBuffer(sse2.substring(10))),
            SessionLogEvent.class);

        StepVerifier.create(serverSentEvents.getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "value"))
            .assertNext(event -> assertSessionLogEvent(event, "value2"))
            .verifyComplete();

        serverSentEvents = new ServerSentEvents<>(
            Flux.just(toByteBuffer(sse1 + "\n\n" + sse2.substring(0, 2)), toByteBuffer(sse2.substring(2))),
            SessionLogEvent.class);

        StepVerifier.create(serverSentEvents.getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "value"))
            .assertNext(event -> assertSessionLogEvent(event, "value2"))
            .verifyComplete();
    }

    @Test
    public void multiByteCharactersCanBeSplitAcrossByteBuffers() {
        String fullData = "data: " + sessionLogEventJson("罗杰·费德勒/瑞士") + "\n\n" + "data: "
            + sessionLogEventJson("foo2/value2") + "\r\n\r\n" + "data: " + sessionLogEventJson("罗杰·/Switzerland");

        byte[] fullDataBytes = fullData.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bb1 = ByteBuffer.wrap(Arrays.copyOfRange(fullDataBytes, 0, 47));
        ByteBuffer bb2 = ByteBuffer.wrap(Arrays.copyOfRange(fullDataBytes, 47, 100));
        ByteBuffer bb3 = ByteBuffer.wrap(Arrays.copyOfRange(fullDataBytes, 100, fullDataBytes.length));

        ServerSentEvents<SessionLogEvent> serverSentEvents
            = new ServerSentEvents<>(Flux.just(bb1, bb2, bb3), SessionLogEvent.class);

        StepVerifier.create(serverSentEvents.getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "罗杰·费德勒/瑞士"))
            .assertNext(event -> assertSessionLogEvent(event, "foo2/value2"))
            .assertNext(event -> assertSessionLogEvent(event, "罗杰·/Switzerland"))
            .verifyComplete();
    }

    @Test
    public void fromEventAndDataDeserializesSessionLogEvent() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer(
            "event: log\n" + "data: {\"timestamp\":\"2026-03-10T09:33:17.121Z\",\"message\":\"started\"}\n\n"));

        StepVerifier.create(ServerSentEvents.fromEventAndData(source, SessionLogEvent.class).getEvents())
            .assertNext(event -> {
                assertEquals(SessionLogEventType.LOG, event.getEvent());
                assertEquals("{\"timestamp\":\"2026-03-10T09:33:17.121Z\",\"message\":\"started\"}", event.getData());
            })
            .verifyComplete();
    }

    @Test
    public void fromEventAndDataHandlesEventTypeInDifferentByteBuffer() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer("event: log\n"), toByteBuffer("data: first\n\n"),
            toByteBuffer("event: done\n"), toByteBuffer("data: [DONE]\n\n"));

        StepVerifier.create(ServerSentEvents.fromEventAndData(source, SessionLogEvent.class).getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .verifyComplete();
    }

    @Test
    public void fromEventAndDataHandlesEventsSplitAcrossByteBuffers() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer("event: log\r\ndata: fir"), toByteBuffer("st\r\n"),
            toByteBuffer("\r\nevent: log\ndata: second\n"), toByteBuffer("\n"));

        StepVerifier.create(ServerSentEvents.fromEventAndData(source, SessionLogEvent.class).getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .assertNext(event -> assertSessionLogEvent(event, "second"))
            .verifyComplete();
    }

    @Test
    public void fromEventAndDataHandlesChunkDividerInNextByteBuffer() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer("event: log\n"), toByteBuffer("data: first"),
            toByteBuffer("\n\nevent: done\n"), toByteBuffer("data: [DONE]\n\n"));

        StepVerifier.create(ServerSentEvents.fromEventAndData(source, SessionLogEvent.class).getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .verifyComplete();
    }

    @Test
    public void fromEventAndDataHandlesChunkDividerSplitBetweenByteBuffers() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer("event: log\n"), toByteBuffer("data: first\n"),
            toByteBuffer("\nevent: done\n"), toByteBuffer("data: [DONE]\n\n"));

        StepVerifier.create(ServerSentEvents.fromEventAndData(source, SessionLogEvent.class).getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .verifyComplete();
    }

    @Test
    public void fromEventAndDataHandlesCrLfChunkDividerSplitAcrossByteBuffers() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer("event: log\r\n"), toByteBuffer("data: first\r"),
            toByteBuffer("\n\r\nevent: done\r\n"), toByteBuffer("data: [DONE]\r\n\r\n"));

        StepVerifier.create(ServerSentEvents.fromEventAndData(source, SessionLogEvent.class).getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .verifyComplete();
    }

    @Test
    public void fromEventAndDataHandlesCrLfChunkDividerSplitAfterLineFeed() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer("event: log\r\n"), toByteBuffer("data: first\r\n"),
            toByteBuffer("\r\nevent: done\r\n"), toByteBuffer("data: [DONE]\r\n\r\n"));

        StepVerifier.create(ServerSentEvents.fromEventAndData(source, SessionLogEvent.class).getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .verifyComplete();
    }

    @Test
    public void fromEventAndDataHandlesCrLfChunkDividerSplitAfterCarriageReturn() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer("event: log\r\n"), toByteBuffer("data: first\r\n\r"),
            toByteBuffer("\nevent: done\r\n"), toByteBuffer("data: [DONE]\r\n\r\n"));

        StepVerifier.create(ServerSentEvents.fromEventAndData(source, SessionLogEvent.class).getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .verifyComplete();
    }

    @Test
    public void fromEventAndDataCombinesMultilineData() {
        Flux<ByteBuffer> source
            = Flux.just(toByteBuffer("event: log\n" + "data: first line\n" + "data: second line\n\n"));

        StepVerifier.create(ServerSentEvents.fromEventAndData(source, SessionLogEvent.class).getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "first line\nsecond line"))
            .verifyComplete();
    }

    @Test
    public void dataDeserializerDeserializesDataPayloadDirectly() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer("data: " + sessionLogEventJson("payload") + "\n\n"));

        StepVerifier.create(new ServerSentEvents<>(source, SessionLogEvent.class).getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "payload"))
            .verifyComplete();
    }

    @Test
    public void skipsStreamCompletionEvent() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer("data: [DONE]\n\n"));

        StepVerifier.create(ServerSentEvents.fromEventAndData(source, SessionLogEvent.class).getEvents())
            .verifyComplete();
    }

    @Test
    public void supportsDirectByteBuffers() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(25);
        byteBuffer.put("event: log\ndata: first\n\n".getBytes(StandardCharsets.UTF_8));
        byteBuffer.flip();

        StepVerifier.create(ServerSentEvents.fromEventAndData(Flux.just(byteBuffer), SessionLogEvent.class).getEvents())
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .verifyComplete();
    }

    private static ByteBuffer toByteBuffer(String value) {
        return ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String sessionLogEventJson(String data) {
        return "{\"event\":\"log\",\"data\":\"" + data + "\"}";
    }

    private static void assertSessionLogEvent(SessionLogEvent event, String data) {
        assertEquals(SessionLogEventType.LOG, event.getEvent());
        assertEquals(data, event.getData());
    }
}
