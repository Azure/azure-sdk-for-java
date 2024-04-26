package com.azure.ai.openai.assistants.implementation.streaming;

import com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent;
import com.azure.ai.openai.assistants.models.StreamUpdate;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.DONE;
import static com.azure.ai.openai.assistants.implementation.models.AssistantStreamEvent.ERROR;

public final class OpenAIServerSentEvents {

    private final StreamTypeFactory streamTypeFactory = new StreamTypeFactory();

    // Server sent events are divided by 2 line breaks, therefore we need to account for 4 bytes containing 2 CRLF characters.
    private static final int LINE_BREAK_CHAR_COUNT_THRESHOLD = 2;


    private final Flux<ByteBuffer> source;
    private ByteArrayOutputStream outStream;

    public OpenAIServerSentEvents(Flux<ByteBuffer> source) {
        this.source = source;
        this.outStream = new ByteArrayOutputStream();
    }

    public Flux<StreamUpdate> getEvents() {
        return mapEventStream();
    }

    /**
     * Maps the byte buffer to a stream of server sent events.
     *
     * @return A stream of server sent events deserialized into StreamUpdates.
     */
    private Flux<StreamUpdate> mapEventStream() {
        return source
            .publishOn(Schedulers.boundedElastic())
            .concatMap(byteBuffer -> {
                List<StreamUpdate> values = new ArrayList<>();
                byte[] byteArray = byteBuffer.array();
                int lineBreakCharsEncountered = 0;

                for (byte currentByte : byteArray) {
                    outStream.write(currentByte);
                    if (isByteLineFeed(currentByte)) {
                        lineBreakCharsEncountered++;

                        // We are looking for 2 line breaks to signify the end of a server sent event.
                        if (lineBreakCharsEncountered == LINE_BREAK_CHAR_COUNT_THRESHOLD) {
                            String currentLine;
                            try {
                                currentLine = outStream.toString(StandardCharsets.UTF_8.name());
                                handleCurrentEvent(currentLine, values);
                            } catch (IOException e) {
                                return Flux.error(e);
                            }
                            outStream = new ByteArrayOutputStream();
                        }
                    } else {
                        if (!isByteCarriageReturn(currentByte)) {
                            lineBreakCharsEncountered = 0;
                        }
                    }
                }

                try {
                    handleCurrentEvent(outStream.toString(StandardCharsets.UTF_8.name()), values);
                    outStream = new ByteArrayOutputStream();
                } catch (IllegalStateException | UncheckedIOException e) {
                    // Even split across different ByteBuffers, the next one will contain the rest of the event.
                    return Flux.fromIterable(values);
                } catch (IOException e) {
                    return Flux.error(e);
                }
//                outStream = new ByteArrayOutputStream();
                return Flux.fromIterable(values);
            }).cache();
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
     * @param values The list of values to add the current line to.
     * @throws IllegalStateException If the current event contains a server side error.
     */
    private void handleCurrentEvent(String currentEvent, List<StreamUpdate> values) throws IllegalStateException, IOException {
        if (currentEvent.isEmpty()) {
            return;
        }

        // We split the event into the event name and the event data. We don't want to split on \n in the data body.
        String[] lines = currentEvent.split("\n", 2);

        String eventName = lines[0].substring(6).trim(); // removing "event:" prefix
        String eventJson = lines[1].substring(5).trim(); // removing "data:" prefix

        if (DONE.equals(AssistantStreamEvent.fromString(eventName))) {
            return;
        }

        if (ERROR.equals(AssistantStreamEvent.fromString(eventName))) {
            throw new IllegalStateException("Server sent event error occurred.");
        }

        values.add(streamTypeFactory.deserializeEvent(eventName, BinaryData.fromString(eventJson)));
    }
}
