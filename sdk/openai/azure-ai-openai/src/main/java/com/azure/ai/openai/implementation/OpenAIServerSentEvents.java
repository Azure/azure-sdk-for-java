// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class OpenAIServerSentEvents<T> {

    private final Flux<ByteBuffer> source;
    private final Class<T> type;
    private AtomicReference<String> lastLine = new AtomicReference<>("");
    private AtomicBoolean expectEmptyLine = new AtomicBoolean();

    private static final ObjectMapper SERIALIZER = new ObjectMapper()
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
        .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);

    public OpenAIServerSentEvents(Flux<ByteBuffer> source, Class<T> type) {
        this.source = source;
        this.type = type;
    }

    public Flux<T> getEvents() {
        return source.concatMap(byteBuffer -> {
            try {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
                BufferedReader reader = new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
                String currentLine = reader.readLine();
                currentLine = lastLine.get() + currentLine;
                List<T> values = new ArrayList<>();
                while (currentLine != null) {
                    if ("data: [DONE]".equals(currentLine)) {
                        return Flux.fromIterable(values);
                    }

                    if (expectEmptyLine.get() && !currentLine.isEmpty()) {
                        return Flux.error(new UnsupportedOperationException("Multi-line data not supported " + currentLine));
                    }

                    if (!expectEmptyLine.get()) {
                        expectEmptyLine.set(true);
                        // The expected line format of the server sent event is data: {...}
                        String[] split = currentLine.split(":", 2);
                        if (split.length != 2) {
                            return Flux.error(new IllegalStateException("Invalid data format " + currentLine));
                        }

                        String dataValue = split[1];
                        if (split[1].startsWith(" ")) {
                            dataValue = split[1].substring(1);
                        }

                        if (!dataValue.isEmpty() && isValidJson(dataValue)) {
                            T value = SERIALIZER.readValue(dataValue, type);
                            values.add(value);
                            lastLine.set("");
                        } else {
                            lastLine.set(currentLine);
                            expectEmptyLine.set(false);
                        }
                    } else {
                        expectEmptyLine.set(false);
                    }
                    currentLine = reader.readLine();
                }
                return Flux.fromIterable(values);
            } catch (IOException e) {
                return Flux.error(e);
            }
        });
    }

    private static boolean isValidJson(String json) {
        try {
            SERIALIZER.readTree(json);
            return true;
        } catch (JacksonException exception) {
            // This can happen if the byte buffers are split resulting in a partial data line in this bytebuffer.
            // So, concatenating the last line of this bytebuffer with the first line of the
            // next bytebuffer should form a valid data event.
            return false;
        }
    }
}
