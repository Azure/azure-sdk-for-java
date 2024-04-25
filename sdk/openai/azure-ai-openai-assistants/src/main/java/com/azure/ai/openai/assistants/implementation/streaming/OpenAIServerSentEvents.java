package com.azure.ai.openai.assistants.implementation.streaming;

import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class OpenAIServerSentEvents<T> {

    private static final List<String> STREAM_COMPLETION_EVENT = List.of("data: [DONE]", "data:[DONE]");
    private static final List<String> STREAM_ERROR_EVENT = List.of("data: [ERROR]", "data:[ERROR]");

    // Server sent events are divided by 2 line breaks, therefore we need to account for 4 bytes containing 2 CRLF characters.
    private static final int LINE_BREAK_CHAR_COUNT_THRESHOLD = 4;


    private static final String STREAM_EVENT_DELTA_NAME = "event:";
    private final Flux<ByteBuffer> source;
    private ByteArrayOutputStream outStream;

    public OpenAIServerSentEvents(Flux<ByteBuffer> source) {
        this.source = source;
        this.outStream = new ByteArrayOutputStream();
    }

    public Flux<T> getEvents() {
        return source
            .publishOn(Schedulers.boundedElastic())
            .concatMap(byteBuffer ->{
                List<T> values = new ArrayList<>();
                byte[] byteArray = byteBuffer.array();
                int lineBreakCharsEncountered = 0;

                for (byte currentByte : byteArray) {
                    outStream.write(currentByte);
                    if (isLineBreakCharacter(currentByte)) {
                        lineBreakCharsEncountered++;

                        // We are looking for 2 line breaks to signify the end of a server sent event.
                        if(lineBreakCharsEncountered == LINE_BREAK_CHAR_COUNT_THRESHOLD) {
                            String currentLine;
                            try {
                                currentLine = outStream.toString(StandardCharsets.UTF_8);
                                handleCurrentEvent(currentLine, values);
                            } catch (IllegalStateException | JsonProcessingException e) {
                                return Flux.error(e);
                            }
                            outStream = new ByteArrayOutputStream();
                        }
                    } else {
                        lineBreakCharsEncountered = 0;
                    }
                }
                return Flux.fromIterable(values);
            }).cache();
    }

    /**
     * Determines if character is either a line feed (0xA) or carriage return (0xD).
     *
     * @param character The character to check.
     * @return True if character is a line break character, false otherwise.
     */
    private boolean isLineBreakCharacter(byte character) {
        return character == 0xA || character == 0xD;
    }

    /**
     * Handles a collected event from the byte buffer which is formated as a UTF_8 string.
     *
     * @param currentEvent The current line of the server sent event.
     * @param values The list of values to add the current line to.
     * @throws JsonProcessingException If the current line cannot be processed.
     * @throws IllegalStateException If the current event contains a server side error.
     */
    private void handleCurrentEvent(String currentEvent, List<T> values) throws JsonProcessingException, IllegalStateException {
        String[] lines = currentEvent.split("");
        if (currentEvent.isEmpty() || STREAM_COMPLETION_EVENT.contains(currentEvent)) {
            return;
        }

        if (STREAM_ERROR_EVENT.contains(currentEvent)) {
            throw new IllegalStateException("Server sent event error occurred.");
        }


        values.add((T) currentEvent);
    }
}
