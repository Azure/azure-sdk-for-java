// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

/***
 * Custom serializer sample for the `CustomLogData` class.
 * Only the `serialize` method is implemented as we are not expected to deserialize the data here.
 */
public class CustomLogSerializer implements ObjectSerializer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ssZZZ");
    private final JsonFactory jsonFactory;

    public CustomLogSerializer() {
        jsonFactory = JsonFactory.builder().build();
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        // This method will never be called
        throw new UnsupportedOperationException("Deserialize called on custom serializer. Which should not happen.");
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserialize(stream, typeReference));
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        if (!(value instanceof CustomLogData)) {
            throw new RuntimeException("Unknown object type passed to custom serializer");
        }

        final JsonGenerator gen;
        final CustomLogData data = (CustomLogData) value;
        try {
            gen = jsonFactory.createGenerator(stream, JsonEncoding.UTF8);
            gen.writeStartObject();
            gen.writeStringField("logTime", FORMATTER.format(data.getTime()));
            gen.writeStringField("extendedColumn", data.getExtendedColumn());
            gen.writeStringField("additionalContext", data.getAdditionalContext());
            gen.writeEndObject();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IO exception.", e);
        }
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream stream, Object value) {
        return Mono.fromRunnable(() -> serialize(stream, value));
    }
}
