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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Helper that parses hosted agent session log SSE streams into {@link SessionLogEvent} values.
 */
public final class SessionLogStreamHelper {
    private static final int SSE_CHUNK_LINE_BREAK_COUNT_MARKER = 2;
    private static final String DATA_FIELD = "data";
    private static final String EVENT_FIELD = "event";
    private static final ClientLogger LOGGER = new ClientLogger(SessionLogStreamHelper.class);

    private SessionLogStreamHelper() {
    }

    /**
     * Parses raw {@code text/event-stream} bytes from a hosted agent session log stream.
     *
     * @param source The raw SSE byte stream.
     * @return A stream of parsed session log events. Events are not cached or replayed by this helper.
     */
    public static Flux<SessionLogEvent> parse(Flux<ByteBuffer> source) {
        Objects.requireNonNull(source, "'source' cannot be null.");

        return Flux.defer(() -> {
            EventParser parser = new EventParser();
            return source.publishOn(Schedulers.boundedElastic())
                .concatMap(parser::processByteBuffer)
                .concatWith(Flux.defer(parser::processRemainingBytes));
        });
    }

    private static void handleCurrentEvent(String currentEvent, List<SessionLogEvent> outputValues) throws IOException {
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

        outputValues.add(toSessionLogEvent(eventName, dataBuilder.toString()));
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

    private static SessionLogEvent toSessionLogEvent(String eventName, String data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField(EVENT_FIELD, eventName);
            jsonWriter.writeStringField(DATA_FIELD, data);
            jsonWriter.writeEndObject();
        }
        return BinaryData.fromBytes(outputStream.toByteArray()).toObject(SessionLogEvent.class);
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

    private static final class EventParser {
        private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        private int lineBreakCharsEncountered;

        private Flux<SessionLogEvent> processByteBuffer(ByteBuffer byteBuffer) {
            List<SessionLogEvent> values = new ArrayList<>();
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

        private Flux<SessionLogEvent> processRemainingBytes() {
            if (outStream.size() == 0) {
                return Flux.empty();
            }

            List<SessionLogEvent> values = new ArrayList<>();
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

        private void processCurrentEvent(List<SessionLogEvent> values) throws IOException {
            String currentEvent = outStream.toString(StandardCharsets.UTF_8.name());
            handleCurrentEvent(currentEvent, values);
        }

        private void resetCurrentEvent() {
            outStream = new ByteArrayOutputStream();
            lineBreakCharsEncountered = 0;
        }
    }
}
