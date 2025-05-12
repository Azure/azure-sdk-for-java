// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.responses.implementation;

import com.azure.ai.openai.responses.models.ResponsesStreamEvent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that handles the deserialization of server sent events.
 */
public final class OpenAIServerSentEvents {

    // Server sent events are divided by 2 CRLF or single LF character
    private static final int SSE_CHUNK_LINE_BREAK_COUNT_MARKER = 2;

    /**
     * The source of the server sent events.
     */
    private final Flux<ByteBuffer> source;

    /**
     * The output stream accumulating the server sent events.
     */
    private ByteArrayOutputStream outStream;

    private static final ClientLogger LOGGER = new ClientLogger(OpenAIServerSentEvents.class);

    /**
     * Creates a new instance of OpenAIServerSentEvents.
     *
     * @param source The source of the server sent events.
     */
    public OpenAIServerSentEvents(Flux<ByteBuffer> source) {
        this.source = source;
        this.outStream = new ByteArrayOutputStream();
    }

    /**
     * Gets the stream of server sent events.
     *
     * @return A stream of server sent events.
     */
    public Flux<ResponsesStreamEvent> getEvents() {
        return mapEventStream();
    }

    /**
     * Maps the byte buffer to a stream of server sent events.
     *
     * @return A stream of server sent events deserialized into ResponsesStreamEvents.
     */
    private Flux<ResponsesStreamEvent> mapEventStream() {
        return source.publishOn(Schedulers.boundedElastic()).concatMap(byteBuffer -> {
            List<ResponsesStreamEvent> values = new ArrayList<>();
            byte[] byteArray = byteBuffer.array();
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
                        // If the current byte is not a CR, reset our counter as it is not part of a CRLF sequence.
                        lineBreakCharsEncountered = 0;
                    }
                }
                // Process any remaining bytes that might form a complete event.
                processRemainingBytes(values);
            } catch (IOException e) {
                return Flux.error(LOGGER.atError().log(e));
            }
            return Flux.fromIterable(values);
        }).cache();
    }

    private void processCurrentEvent(List<ResponsesStreamEvent> values) throws UnsupportedEncodingException {
        String currentLine = outStream.toString(StandardCharsets.UTF_8.name());
        handleCurrentEvent(currentLine, values);
    }

    private void processRemainingBytes(List<ResponsesStreamEvent> values) throws UnsupportedEncodingException {
        String remainingBytes = outStream.toString(StandardCharsets.UTF_8.name());
        if (remainingBytes.endsWith("\n\n")) {
            handleCurrentEvent(remainingBytes, values);
        }
    }

    /**
     * Determines if character is a line feed (0xA).
     *
     * @param character The character to check.
     * @return True if character is a line feed character, false otherwise.
     */
    private boolean isByteLineFeed(byte character) {
        return character == 0xA;
    }

    /**
     * Determines if character is a carriage return (0xD).
     *
     * @param character The character to check.
     * @return True if character is a carriage return character, false otherwise.
     */
    private boolean isByteCarriageReturn(byte character) {
        return character == 0xD;
    }

    /**
     * Handles a collected event from the byte buffer which is formated as a UTF_8 string.
     *
     * @param currentEvent The current line of the server sent event.
     * @param outputValues The list of values to add the current line to.
     */
    public void handleCurrentEvent(String currentEvent, List<ResponsesStreamEvent> outputValues) {
        if (currentEvent.isEmpty()) {
            return;
        }

        // The delimiter according to the spec is always `\n\n`
        // https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#event_stream_format
        String[] eventLines = currentEvent.trim().split("\n\n");

        for (String event : eventLines) {
            if (event.isEmpty()) {
                continue;
            }

            String[] lines = event.split("\n", 2);
            if (lines.length != 2 || lines[0].isEmpty() || lines[1].isEmpty()) {
                continue;
            }

            if (!lines[0].startsWith("event:") || !lines[1].startsWith("data:")) {
                continue;
            }

            // We don't need the event name, leaving this here for clarity.
            //        String eventName = lines[0].substring(6).trim();
            String eventJson = lines[1].substring(5).trim();
            outputValues.add(BinaryData.fromString(eventJson).toObject(ResponsesStreamEvent.class));
        }
    }
}
