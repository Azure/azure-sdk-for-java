// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.models.SessionLogEvent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a {@code text/event-stream} byte stream into a {@link Flux} of {@link SessionLogEvent} instances.
 *
 * <p>Each frame has an {@code event:} and {@code data:} line which are extracted and synthesised into a
 * JSON object for {@link SessionLogEvent#fromJson}.</p>
 */
public final class SessionLogServerSentEvents {

    private static final int SSE_CHUNK_LINE_BREAK_COUNT_MARKER = 2;
    private static final ClientLogger LOGGER = new ClientLogger(SessionLogServerSentEvents.class);

    private final Flux<ByteBuffer> source;
    private ByteArrayOutputStream outStream;

    /**
     * Creates a new instance.
     *
     * @param source The raw SSE byte stream.
     */
    public SessionLogServerSentEvents(Flux<ByteBuffer> source) {
        this.source = source;
        this.outStream = new ByteArrayOutputStream();
    }

    /**
     * Returns a {@link Flux} of parsed {@link SessionLogEvent} instances.
     *
     * @return The stream of session log events.
     */
    public Flux<SessionLogEvent> getEvents() {
        return mapEventStream();
    }

    private Flux<SessionLogEvent> mapEventStream() {
        return source.publishOn(Schedulers.boundedElastic()).concatMap(byteBuffer -> {
            List<SessionLogEvent> values = new ArrayList<>();
            byte[] byteArray;
            if (byteBuffer.hasArray()) {
                byteArray = byteBuffer.array();
            } else {
                byteArray = new byte[byteBuffer.remaining()];
                byteBuffer.get(byteArray);
            }
            int lineBreakCharsEncountered = 0;
            try {
                for (byte currentByte : byteArray) {
                    outStream.write(currentByte);
                    if (isByteLineFeed(currentByte)) {
                        lineBreakCharsEncountered++;
                        if (lineBreakCharsEncountered == SSE_CHUNK_LINE_BREAK_COUNT_MARKER) {
                            processCurrentEvent(values);
                            outStream = new ByteArrayOutputStream();
                            lineBreakCharsEncountered = 0;
                        }
                    } else if (!isByteCarriageReturn(currentByte)) {
                        lineBreakCharsEncountered = 0;
                    }
                }
                processRemainingBytes(values);
            } catch (IOException e) {
                return Flux.error(LOGGER.atError().log(e));
            }
            return Flux.fromIterable(values);
        }).cache();
    }

    private void processCurrentEvent(List<SessionLogEvent> values) throws UnsupportedEncodingException {
        String currentLine = outStream.toString(StandardCharsets.UTF_8.name());
        handleCurrentEvent(currentLine, values);
    }

    private void processRemainingBytes(List<SessionLogEvent> values) throws UnsupportedEncodingException {
        String remainingBytes = outStream.toString(StandardCharsets.UTF_8.name());
        if (remainingBytes.endsWith("\n\n")) {
            handleCurrentEvent(remainingBytes, values);
        }
    }

    private static boolean isByteLineFeed(byte character) {
        return character == 0xA;
    }

    private static boolean isByteCarriageReturn(byte character) {
        return character == 0xD;
    }

    private static void handleCurrentEvent(String currentEvent, List<SessionLogEvent> outputValues) {
        if (currentEvent.isEmpty()) {
            return;
        }

        String[] eventBlocks = currentEvent.trim().split("\n\n");

        for (String block : eventBlocks) {
            if (block.isEmpty()) {
                continue;
            }

            String eventName = null;
            String dataPayload = null;

            for (String line : block.split("\n")) {
                if (line.startsWith("event:")) {
                    eventName = line.substring("event:".length()).trim();
                } else if (line.startsWith("data:")) {
                    dataPayload = line.substring("data:".length()).trim();
                }
            }

            if (eventName == null || dataPayload == null) {
                continue;
            }

            // Synthesise JSON from the SSE headers for SessionLogEvent.fromJson.
            try {
                StringWriter sw = new StringWriter();
                try (JsonWriter jw = JsonProviders.createWriter(sw)) {
                    jw.writeStartObject();
                    jw.writeStringField("event", eventName);
                    jw.writeStringField("data", dataPayload);
                    jw.writeEndObject();
                }
                outputValues.add(BinaryData.fromString(sw.toString()).toObject(SessionLogEvent.class));
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Failed to serialise SessionLogEvent JSON", e));
            }
        }
    }
}
