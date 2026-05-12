// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A generic helper that parses {@code text/event-stream} byte streams into typed events.
 *
 * @param <T> The type of event emitted by the stream.
 */
public final class ServerSentEvents<T> {
    private static final int SSE_CHUNK_LINE_BREAK_COUNT_MARKER = 2;
    private static final String DATA_FIELD = "data";
    private static final String EVENT_FIELD = "event";
    private static final String STREAM_COMPLETION_DATA = "[DONE]";
    private static final ClientLogger LOGGER = new ClientLogger(ServerSentEvents.class);

    private final EventDeserializer<T> eventDeserializer;
    private final Flux<T> events;
    private ByteArrayOutputStream outStream;
    private int lineBreakCharsEncountered;

    /**
     * Creates a new instance that deserializes each SSE {@code data:} payload into {@code type}.
     *
     * @param source The raw SSE byte stream.
     * @param type The model type to deserialize each {@code data:} payload into.
     */
    public ServerSentEvents(Flux<ByteBuffer> source, Class<T> type) {
        this(source, dataDeserializer(type));
    }

    /**
     * Creates a new instance with a custom deserializer that receives each SSE frame's {@code event:} name and
     * {@code data:} payload.
     *
     * @param source The raw SSE byte stream.
     * @param eventDeserializer The deserializer to convert parsed SSE frames into typed events.
     */
    public ServerSentEvents(Flux<ByteBuffer> source, EventDeserializer<T> eventDeserializer) {
        Objects.requireNonNull(source, "'source' cannot be null.");
        this.eventDeserializer = Objects.requireNonNull(eventDeserializer, "'eventDeserializer' cannot be null.");
        this.outStream = new ByteArrayOutputStream();
        this.events = source.publishOn(Schedulers.boundedElastic())
            .concatMap(this::processByteBuffer)
            .concatWith(Flux.defer(this::processRemainingBytes))
            .cache();
    }

    /**
     * Creates a new instance that synthesizes a JSON object containing the SSE {@code event:} and {@code data:} values,
     * then deserializes that JSON object into {@code type}.
     *
     * @param source The raw SSE byte stream.
     * @param type The model type to deserialize the synthesized {@code event}/{@code data} envelope into.
     * @param <T> The type of event emitted by the stream.
     * @return A {@link ServerSentEvents} instance that emits typed events.
     */
    public static <T> ServerSentEvents<T> fromEventAndData(Flux<ByteBuffer> source, Class<T> type) {
        return new ServerSentEvents<>(source, eventAndDataDeserializer(type));
    }

    /**
     * Gets the stream of parsed server-sent events.
     *
     * @return A stream of parsed server-sent events.
     */
    public Flux<T> getEvents() {
        return events;
    }

    private Flux<T> processByteBuffer(ByteBuffer byteBuffer) {
        List<T> values = new ArrayList<>();
        try {
            for (byte currentByte : toByteArray(byteBuffer)) {
                outStream.write(currentByte);
                if (isByteLineFeed(currentByte)) {
                    lineBreakCharsEncountered++;
                    if (lineBreakCharsEncountered == SSE_CHUNK_LINE_BREAK_COUNT_MARKER) {
                        processCurrentEvent(values);
                        resetCurrentEvent();
                    }
                } else if (!isByteCarriageReturn(currentByte)) {
                    lineBreakCharsEncountered = 0;
                }
            }
        } catch (IOException e) {
            return Flux.error(LOGGER.atError().log(e));
        } catch (RuntimeException e) {
            return Flux.error(LOGGER.atError().log(e));
        }

        return Flux.fromIterable(values);
    }

    private Flux<T> processRemainingBytes() {
        if (outStream.size() == 0) {
            return Flux.empty();
        }

        List<T> values = new ArrayList<>();
        try {
            processCurrentEvent(values);
            resetCurrentEvent();
        } catch (IOException e) {
            return Flux.error(LOGGER.atError().log(e));
        } catch (RuntimeException e) {
            return Flux.error(LOGGER.atError().log(e));
        }

        return Flux.fromIterable(values);
    }

    private void processCurrentEvent(List<T> values) throws IOException {
        String currentEvent = outStream.toString(StandardCharsets.UTF_8.name());
        handleCurrentEvent(currentEvent, values);
    }

    private void resetCurrentEvent() {
        outStream = new ByteArrayOutputStream();
        lineBreakCharsEncountered = 0;
    }

    private void handleCurrentEvent(String currentEvent, List<T> outputValues) throws IOException {
        if (currentEvent.isEmpty()) {
            return;
        }

        String eventName = null;
        StringBuilder dataBuilder = new StringBuilder();
        boolean dataLineSeen = false;

        for (String line : currentEvent.split("\n")) {
            String currentLine = removeTrailingCarriageReturn(line);
            if (currentLine.isEmpty() || currentLine.startsWith(":")) {
                continue;
            }

            int delimiterIndex = currentLine.indexOf(':');
            String fieldName = delimiterIndex < 0 ? currentLine : currentLine.substring(0, delimiterIndex);
            String fieldValue = getFieldValue(currentLine, delimiterIndex);

            if (EVENT_FIELD.equals(fieldName)) {
                eventName = fieldValue;
            } else if (DATA_FIELD.equals(fieldName)) {
                if (dataLineSeen) {
                    dataBuilder.append('\n');
                }
                dataBuilder.append(fieldValue);
                dataLineSeen = true;
            }
        }

        if (!dataLineSeen) {
            return;
        }

        String data = dataBuilder.toString();
        if (isStreamCompletionEvent(data)) {
            return;
        }

        T value = eventDeserializer.deserialize(eventName, data);
        if (value != null) {
            outputValues.add(value);
        }
    }

    private static String removeTrailingCarriageReturn(String line) {
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }

    private static String getFieldValue(String line, int delimiterIndex) {
        if (delimiterIndex < 0) {
            return "";
        }

        String value = line.substring(delimiterIndex + 1);
        return value.startsWith(" ") ? value.substring(1) : value;
    }

    private static boolean isStreamCompletionEvent(String data) {
        return STREAM_COMPLETION_DATA.equals(data.trim());
    }

    private static boolean isByteLineFeed(byte character) {
        return character == 0xA;
    }

    private static boolean isByteCarriageReturn(byte character) {
        return character == 0xD;
    }

    private static byte[] toByteArray(ByteBuffer byteBuffer) {
        ByteBuffer duplicate = byteBuffer.asReadOnlyBuffer();
        byte[] byteArray = new byte[duplicate.remaining()];
        duplicate.get(byteArray);
        return byteArray;
    }

    private static <T> EventDeserializer<T> dataDeserializer(Class<T> type) {
        Objects.requireNonNull(type, "'type' cannot be null.");
        return (eventName, data) -> BinaryData.fromString(data).toObject(type);
    }

    private static <T> EventDeserializer<T> eventAndDataDeserializer(Class<T> type) {
        Objects.requireNonNull(type, "'type' cannot be null.");
        return (eventName, data) -> deserializeEventAndData(eventName, data, type);
    }

    private static <T> T deserializeEventAndData(String eventName, String data, Class<T> type) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField(EVENT_FIELD, eventName);
            jsonWriter.writeStringField(DATA_FIELD, data);
            jsonWriter.writeEndObject();
        }
        return BinaryData.fromBytes(outputStream.toByteArray()).toObject(type);
    }

    /**
     * Deserializes a parsed server-sent event.
     *
     * @param <T> The type of event emitted by the stream.
     */
    @FunctionalInterface
    public interface EventDeserializer<T> {
        /**
         * Deserializes a parsed SSE frame.
         *
         * @param eventName The value of the SSE {@code event:} field, or {@code null} if the frame didn't include one.
         * @param data The combined value of the SSE {@code data:} field(s).
         * @return The deserialized event, or {@code null} to skip the frame.
         * @throws IOException If the event cannot be deserialized.
         */
        T deserialize(String eventName, String data) throws IOException;
    }
}
