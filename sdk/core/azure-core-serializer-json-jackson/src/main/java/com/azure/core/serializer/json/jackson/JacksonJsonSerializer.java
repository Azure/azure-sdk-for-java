// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.experimental.serializer.JsonNode;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map;

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
    public <T> Mono<T> deserialize(InputStream stream, Class<T> clazz) {
        return Mono.fromCallable(() -> {
            if (stream == null) {
                return null;
            }

            return mapper.readValue(stream, clazz);
        });
    }

    @Override
    public Mono<Map<Object, Object>> deserializeToMap(InputStream stream) {
        return Mono.fromCallable(() -> {
            if (stream == null) {
                return null;
            }

            return mapper.readValue(stream, new TypeReference<Map<Object, Object>>() { });
        });
    }

    @Override
    public <T> Mono<T> deserializeTree(JsonNode jsonNode, Class<T> clazz) {
        return Mono.fromCallable(() -> mapper.treeToValue(JsonNodeUtils.toJacksonNode(jsonNode), clazz));
    }

    @Override
    public Mono<Map<Object, Object>> deserializeTreeToMap(JsonNode jsonNode) {
        return Mono.fromCallable(() -> mapper.convertValue(JsonNodeUtils.toJacksonNode(jsonNode),
            new TypeReference<Map<Object, Object>>() { }));
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

    @Override
    public Mono<String> getSerializerMemberName(Field field) {
        return Mono.fromCallable(() -> {
            if (!field.isAnnotationPresent(JsonProperty.class)) {
                return field.getName();
            }
            String propertyName = field.getDeclaredAnnotation(JsonProperty.class).value();
            return CoreUtils.isNullOrEmpty(propertyName) ? field.getName() : propertyName;
        });
    }
}
