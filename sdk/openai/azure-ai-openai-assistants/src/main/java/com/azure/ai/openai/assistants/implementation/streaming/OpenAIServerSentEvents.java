package com.azure.ai.openai.assistants.implementation.streaming;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class OpenAIServerSentEvents<T> {

    // Server sent events are divided by 2 line breaks, therefore we need to account for 4 bytes containing 2 CRLF characters.
    private static final int LINE_BREAK_CHAR_COUNT_THRESHOLD = 2;


    private final Flux<ByteBuffer> source;
    private ByteArrayOutputStream outStream;
    private final EventStringHandler<T> eventStringHandler;

    public OpenAIServerSentEvents(Flux<ByteBuffer> source) {
        this(source, (EventStringHandler<T>) new StreamTypeFactory());
    }

    public OpenAIServerSentEvents(Flux<ByteBuffer> source, EventStringHandler<T> eventStringHandler) {
        this.source = source;
        this.outStream = new ByteArrayOutputStream();
        this.eventStringHandler = eventStringHandler;
    }

    public Flux<T> getEvents() {
        return mapEventStream();
    }

    /**
     * Maps the byte buffer to a stream of server sent events.
     *
     * @return A stream of server sent events deserialized into StreamUpdates.
     */
    private Flux<T> mapEventStream() {
        return source
            .publishOn(Schedulers.boundedElastic())
            .concatMap(byteBuffer -> {
                List<T> values = new ArrayList<>();
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
                                eventStringHandler.handleCurrentEvent(currentLine, values);
                            } catch (IOException e) {
                                return Flux.error(e);
                            }
                            outStream = new ByteArrayOutputStream();
                        }
                    } else {
                        // In some cases line breaks can contain both the line feed and carriage return characters.
                        // We don't want to reset the line break count if we encounter a carriage return character.
                        // We are assuming that line feeds and carriage returns, if both present, are always paired.
                        // With this assumption, we are able to operate when carriage returns aren't present in the input also.
                        if (!isByteCarriageReturn(currentByte)) {
                            lineBreakCharsEncountered = 0;
                        }
                    }
                }

                try {
                    this.eventStringHandler.handleCurrentEvent(outStream.toString(StandardCharsets.UTF_8.name()), values);
                    outStream = new ByteArrayOutputStream();
                } catch (IllegalStateException | UncheckedIOException e) {
                    // Even split across different ByteBuffers, the next one will contain the rest of the event.
                    return Flux.fromIterable(values);
                } catch (IOException e) {
                    return Flux.error(e);
                }
                outStream = new ByteArrayOutputStream();
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
}
