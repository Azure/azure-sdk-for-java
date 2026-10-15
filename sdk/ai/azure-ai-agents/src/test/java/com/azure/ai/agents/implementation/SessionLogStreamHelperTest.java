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
 * Unit tests for {@link SessionLogStreamHelper}.
 */
public class SessionLogStreamHelperTest {
    @Test
    public void emptyFluxByteBuffer() {
        StepVerifier.create(SessionLogStreamHelper.parse(Flux.empty())).verifyComplete();
    }

    @Test
    public void parsesSessionLogEvent() {
        String data = "{\"timestamp\":\"2026-03-10T09:33:17.121Z\",\"message\":\"started\"}";

        StepVerifier.create(SessionLogStreamHelper.parse(Flux.just(toByteBuffer(sessionLogSse(data)))))
            .assertNext(event -> assertSessionLogEvent(event, data))
            .verifyComplete();
    }

    @Test
    public void parsesMultipleEventsSplitAcrossByteBuffers() {
        Flux<ByteBuffer> source
            = Flux.just(toByteBuffer(sessionLogSse("first") + "\n\n" + sessionLogSse("sec")), toByteBuffer("ond\n\n"));

        StepVerifier.create(SessionLogStreamHelper.parse(source))
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .assertNext(event -> assertSessionLogEvent(event, "second"))
            .verifyComplete();
    }

    @Test
    public void handlesLfDelimiterSplitAcrossByteBuffers() {
        Flux<ByteBuffer> source
            = Flux.just(toByteBuffer("event: log\ndata: first\n"), toByteBuffer("\nevent: log\ndata: second\n\n"));

        StepVerifier.create(SessionLogStreamHelper.parse(source))
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .assertNext(event -> assertSessionLogEvent(event, "second"))
            .verifyComplete();
    }

    @Test
    public void handlesCrLfDelimiterSplitAcrossByteBuffers() {
        Flux<ByteBuffer> source = Flux.just(toByteBuffer("event: log\r\n"), toByteBuffer("data: first\r"),
            toByteBuffer("\n\r\nevent: log\r\n"), toByteBuffer("data: second\r\n\r\n"));

        StepVerifier.create(SessionLogStreamHelper.parse(source))
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .assertNext(event -> assertSessionLogEvent(event, "second"))
            .verifyComplete();
    }

    @Test
    public void combinesMultilineData() {
        Flux<ByteBuffer> source
            = Flux.just(toByteBuffer("event: log\n" + "data: first line\n" + "data: second line\n\n"));

        StepVerifier.create(SessionLogStreamHelper.parse(source))
            .assertNext(event -> assertSessionLogEvent(event, "first line\nsecond line"))
            .verifyComplete();
    }

    @Test
    public void ignoresCommentsAndUnknownFields() {
        Flux<ByteBuffer> source
            = Flux.just(toByteBuffer(": keep-alive\nid: 1\nretry: 1000\nevent: log\ndata: first\n\n"));

        StepVerifier.create(SessionLogStreamHelper.parse(source))
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .verifyComplete();
    }

    @Test
    public void treatsDoneAsSessionLogData() {
        StepVerifier.create(SessionLogStreamHelper.parse(Flux.just(toByteBuffer(sessionLogSse("[DONE]") + "\n\n"))))
            .assertNext(event -> assertSessionLogEvent(event, "[DONE]"))
            .verifyComplete();
    }

    @Test
    public void parseUsesParserStatePerSubscription() {
        Flux<SessionLogEvent> events = SessionLogStreamHelper.parse(
            Flux.just(toByteBuffer(sessionLogSse("first") + "\n\n"), toByteBuffer(sessionLogSse("second") + "\n\n")));

        StepVerifier.create(events)
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .assertNext(event -> assertSessionLogEvent(event, "second"))
            .verifyComplete();
        StepVerifier.create(events)
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .assertNext(event -> assertSessionLogEvent(event, "second"))
            .verifyComplete();
    }

    @Test
    public void supportsSlicedByteBuffers() {
        byte[] bytes = "prefixevent: log\ndata: first\n\nsuffix".getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.position(6);
        byteBuffer.limit(byteBuffer.limit() - 6);

        StepVerifier.create(SessionLogStreamHelper.parse(Flux.just(byteBuffer.slice())))
            .assertNext(event -> assertSessionLogEvent(event, "first"))
            .verifyComplete();
    }

    @Test
    public void multiByteCharactersCanBeSplitAcrossByteBuffers() {
        String fullData = sessionLogSse("罗杰·费德勒/瑞士") + "\n\n" + sessionLogSse("foo2/value2");
        byte[] fullDataBytes = fullData.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bb1 = ByteBuffer.wrap(Arrays.copyOfRange(fullDataBytes, 0, 47));
        ByteBuffer bb2 = ByteBuffer.wrap(Arrays.copyOfRange(fullDataBytes, 47, fullDataBytes.length));

        StepVerifier.create(SessionLogStreamHelper.parse(Flux.just(bb1, bb2)))
            .assertNext(event -> assertSessionLogEvent(event, "罗杰·费德勒/瑞士"))
            .assertNext(event -> assertSessionLogEvent(event, "foo2/value2"))
            .verifyComplete();
    }

    private static ByteBuffer toByteBuffer(String value) {
        return ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String sessionLogSse(String data) {
        return "event: log\ndata: " + data;
    }

    private static void assertSessionLogEvent(SessionLogEvent event, String data) {
        assertEquals(SessionLogEventType.LOG, event.getEvent());
        assertEquals(data, event.getData());
    }
}
