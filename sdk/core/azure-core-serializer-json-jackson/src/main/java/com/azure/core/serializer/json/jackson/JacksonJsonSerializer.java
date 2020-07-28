// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.experimental.serializer.JsonNode;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.experimental.serializer.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Jackson based implementation of the {@link JsonSerializer} interface.
 */
public final class JacksonJsonSerializer implements JsonSerializer {
    private final ObjectMapper mapper;
    private final TypeFactory typeFactory;

    /**
     * Constructs a {@link JsonSerializer} using the passed Jackson serializer.
     *
     * @param mapper Configured Jackson serializer.
     */
    JacksonJsonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
        this.typeFactory = mapper.getTypeFactory();
    }

    @Override
    public <T> Mono<T> deserialize(InputStream stream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> {
            if (stream == null) {
                return null;
            }

            return mapper.readValue(stream, typeFactory.constructType(typeReference.getJavaType()));
        });
    }

    @Override
    public <T> Mono<T> deserializeTree(JsonNode jsonNode, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> mapper.readerFor(typeFactory.constructType(typeReference.getJavaType()))
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
