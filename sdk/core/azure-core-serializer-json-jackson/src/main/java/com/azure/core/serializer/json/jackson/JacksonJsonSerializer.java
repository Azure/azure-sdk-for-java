// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.experimental.serializer.JsonNode;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Jackson based implementation of the {@link JsonSerializer} interface.
 */
public final class JacksonJsonSerializer implements JsonSerializer {
    private final ObjectMapper mapper;

    /**
     * Constructs a {@link JsonSerializer} using the passed Jackson serializer.
     *
     * @param mapper Configured Jackson serializer.
     */
    JacksonJsonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> Mono<T> deserialize(InputStream stream, Type type) {
        return Mono.fromCallable(() -> {
            if (stream == null) {
                return null;
            }

            return mapper.readValue(stream, mapper.getTypeFactory().constructType(type));
        });
    }

    @Override
    public <T> Mono<T> deserializeTree(JsonNode jsonNode, Type type) {
        return Mono.fromCallable(() -> mapper.readerFor(mapper.getTypeFactory().constructType(type))
                .readValue(JsonNodeUtils.toJacksonNode(jsonNode)));
    }

    @Override
    public <S extends OutputStream> Mono<S> serialize(S stream, Object value) {
        return Mono.fromCallable(() -> {
            mapper.writeValue(stream, value);

            return stream;
        });
    }

    @Override
    public Mono<OutputStream> serializeTree(OutputStream stream, JsonNode jsonNode) {
        return serialize(stream, JsonNodeUtils.toJacksonNode(jsonNode));
    }

    @Override
    public Mono<JsonNode> toTree(InputStream stream) {
        return Mono.fromCallable(() -> JsonNodeUtils.fromJacksonNode(mapper.readTree(stream)));
    }

    @Override
    public Mono<JsonNode> toTree(Object value) {
        return Mono.fromCallable(() -> JsonNodeUtils.fromJacksonNode(mapper.valueToTree(value)));
    }
}
