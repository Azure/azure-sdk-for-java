// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class OpenAIServerSentEvents<T> {

    private static final JsonSerializer JSON_SERIALIZER = JsonSerializerProviders.createInstance(true);
    private final Flux<ByteBuffer> source;
    private final Class<T> type;
    private AtomicReference<String> lastLine = new AtomicReference<>("");
    private AtomicBoolean expectEmptyLine = new AtomicBoolean();

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
                        String[] split = currentLine.split(":", 2);
                        if (split.length != 2) {
                            return Flux.error(new IllegalStateException("Invalid data format " + currentLine));
                        }

                        String completionJson = split[1];
                        if (split[1].startsWith(" ")) {
                            completionJson = split[1].substring(1);
                        }

                        try {
                            T value = JSON_SERIALIZER.deserializeFromBytes(completionJson.getBytes(StandardCharsets.UTF_8), TypeReference.createInstance(type));
                            values.add(value);
                            lastLine.set("");
                        } catch (UncheckedIOException exception) {
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


}
