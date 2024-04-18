// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class OpenAIServerSentEvents<T> {

    private static final List<String> STREAM_COMPLETION_EVENT = Arrays.asList("data: [DONE]", "data:[DONE]");
    private final Flux<ByteBuffer> source;
    private final Class<T> type;
    private ByteArrayOutputStream outStream;

    public OpenAIServerSentEvents(Flux<ByteBuffer> source, Class<T> type) {
        this.source = source;
        this.type = type;
        this.outStream = new ByteArrayOutputStream();
    }

    public Flux<T> getEvents() {
        return mapByteBuffersToEvents();
    }

    private Flux<T> mapByteBuffersToEvents() {
        return source
            .publishOn(Schedulers.boundedElastic())
            .concatMap(byteBuffer -> {
                List<T> values = new ArrayList<>();
                byte[] byteArray = byteBuffer.array();
                for (byte currentByte : byteArray) {
                    if (currentByte == 0xA || currentByte == 0xD) {
                        String currentLine;
                        try {
                            currentLine = outStream.toString(StandardCharsets.UTF_8.name());
                            handleCurrentLine(currentLine, values);
                        } catch (IOException e) {
                            return Flux.error(e);
                        }
                        outStream = new ByteArrayOutputStream();
                    } else {
                        outStream.write(currentByte);
                    }
                }
                try {
                    handleCurrentLine(outStream.toString(StandardCharsets.UTF_8.name()), values);
                    outStream = new ByteArrayOutputStream();
                } catch (IllegalStateException e) {
                    // return the values collected so far, as this could be because the server sent event is
                    // split across two byte buffers and the last line is incomplete and will be continued in
                    // the next byte buffer
                    return Flux.fromIterable(values);
                } catch (IOException e) {
                    return Flux.error(e);
                }
                return Flux.fromIterable(values);
            }).cache();
    }

    private void handleCurrentLine(String currentLine, List<T> values) throws IOException {
        if (currentLine.isEmpty() || STREAM_COMPLETION_EVENT.contains(currentLine)) {
            return;
        }

        // The expected line format of the server sent event is data: {...}
        String[] split = currentLine.split(":", 2);
        if (split.length != 2) {
            throw new IllegalStateException("Invalid data format " + currentLine);
        }

        String dataValue = split[1];
        if (split[1].startsWith(" ")) {
            dataValue = split[1].substring(1);
        }

//        T value = SERIALIZER.readValue(dataValue, type);


        JsonReader reader = JsonProviders.createReader(dataValue);

        T value = null;

        values.add(value);
    }
}
