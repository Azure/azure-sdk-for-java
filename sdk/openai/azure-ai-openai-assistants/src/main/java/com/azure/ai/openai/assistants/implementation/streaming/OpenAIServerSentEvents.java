// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.assistants.implementation.streaming;

import com.azure.ai.openai.assistants.models.AssistantStreamEvent;
import com.azure.ai.openai.assistants.models.StreamUpdate;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.DONE;
import static com.azure.ai.openai.assistants.models.AssistantStreamEvent.ERROR;

/**
 * A class that handles the deserialization of server sent events.
 */
public final class OpenAIServerSentEvents {

    // Server sent events are divided by 2 CRLF or single LF character
    private static final int SSE_CHUNK_LINE_BREAK_COUNT_MARKER = 2;

    /**
     * A factory that determines into which type to deserialize the server sent events.
     */
    private final StreamTypeFactory eventDeserializer = new StreamTypeFactory();

    /**
     * The source of the server sent events.
     */
    private final Flux<ByteBuffer> source;

    /**
     * The output stream accumulating the server sent events.
     */
    private ByteArrayOutputStream outStream;
    private final ClientLogger LOGGER = new ClientLogger(OpenAIServerSentEvents.class);

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
    public Flux<StreamUpdate> getEvents() {
        return mapEventStream();
    }

    /**
     * Maps the byte buffer to a stream of server sent events.
     *
     * @return A stream of server sent events deserialized into StreamUpdates.
     */
    private Flux<StreamUpdate> mapEventStream() {
        return source.publishOn(Schedulers.boundedElastic()).concatMap(byteBuffer -> {
            List<StreamUpdate> values = new ArrayList<>();
            byte[] byteArray = byteBuffer.array();
            // We check whether we ended the last byteBuffer with a line feed or not, in case we need to close this
            // chunk soon after
            byte[] outByteArray = outStream.toByteArray();
            int lineBreakCharsEncountered
                = outByteArray.length > 0 && isByteLineFeed(outByteArray[outByteArray.length - 1]) ? 1 : 0;

            for (byte currentByte : byteArray) {
                outStream.write(currentByte);
                if (isByteLineFeed(currentByte)) {
                    lineBreakCharsEncountered++;

                    // We are looking for 2 line breaks to signify the end of a server sent event.
                    if (lineBreakCharsEncountered == SSE_CHUNK_LINE_BREAK_COUNT_MARKER) {
                        processCurrentEvent(values);
                        outStream = new ByteArrayOutputStream();
                    }
                } else if (!isByteCarriageReturn(currentByte)) {
                    lineBreakCharsEncountered = 0;
                }
            }

            processRemainingBytes(values);
            return Flux.fromIterable(values);
        }).cache();
    }

    private void processCurrentEvent(List<StreamUpdate> values) {
        try {
            String currentLine = outStream.toString(StandardCharsets.UTF_8.name());
            handleCurrentEvent(currentLine, values);
        } catch (IOException e) {
            LOGGER.error("Error occurred while processing the current event.", e);
        }
    }

    private void processRemainingBytes(List<StreamUpdate> values) {
        try {
            String remainingBytes = outStream.toString(StandardCharsets.UTF_8.name());
            if (remainingBytes.endsWith("\n\n") || remainingBytes.endsWith("\r\n\r\n")) {
                handleCurrentEvent(remainingBytes, values);
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred while processing the remaining bytes.", e);
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
    public void handleCurrentEvent(String currentEvent, List<StreamUpdate> outputValues) {
        if (currentEvent.isEmpty()) {
            return;
        }

        String[] lines = currentEvent.split("\n", 2);
        if (lines.length != 2 || lines[0].isEmpty() || lines[1].isEmpty()) {
            return;
        }

        String eventName = lines[0].substring(6).trim();
        String eventJson = lines[1].substring(5).trim();

        if (AssistantStreamEvent.DONE.equals(AssistantStreamEvent.fromString(eventName))) {
            return;
        }
        if (AssistantStreamEvent.ERROR.equals(AssistantStreamEvent.fromString(eventName))) {
            LOGGER.atError().log("Error event received: {}", eventJson);
            return;
        }

        outputValues.add(this.eventDeserializer.deserializeEvent(eventName, BinaryData.fromString(eventJson)));
    }
}
