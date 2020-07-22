// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.experimental.serializer.JsonNode;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * GSON based implementation of the {@link JsonSerializer} interface.
 */
public final class GsonJsonSerializer implements JsonSerializer {
    private final Gson gson;

    /**
     * Constructs a {@link JsonSerializer} using the passed {@link Gson} serializer.
     *
     * @param gson Configured {@link Gson} serializer.
     */
    GsonJsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> Mono<T> deserialize(InputStream stream, Class<T> clazz) {
        return Mono.fromCallable(() -> gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), clazz));
    }

    @Override
    public Mono<Map<Object, Object>> deserializeToMap(InputStream stream) {
        return Mono.fromCallable(() -> gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
            new TypeToken<Map<Object, Object>>() { }.getType()));
    }

    @Override
    public <T> Mono<T> deserializeTree(JsonNode jsonNode, Class<T> clazz) {
        return Mono.fromCallable(() -> gson.fromJson(JsonNodeUtils.toGsonElement(jsonNode), clazz));
    }

    @Override
    public Mono<Map<Object, Object>> deserializeTreeToMap(JsonNode jsonNode) {
        return Mono.fromCallable(() -> gson.fromJson(JsonNodeUtils.toGsonElement(jsonNode),
            new TypeToken<Map<Object, Object>>() { }.getType()));
    }

    @Override
    public <S extends OutputStream> Mono<S> serialize(S stream, Object value) {
        return Mono.fromCallable(() -> {
            Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
            gson.toJson(value, writer);
            writer.flush();

            return stream;
        });
    }

    @Override
    public Mono<OutputStream> serializeTree(OutputStream stream, JsonNode jsonNode) {
        return serialize(stream, JsonNodeUtils.toGsonElement(jsonNode));
    }

    @Override
    public Mono<JsonNode> toTree(InputStream stream) {
        return Mono.fromCallable(() -> JsonNodeUtils.fromGsonElement(
            new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8))));
    }

    @Override
    public Mono<JsonNode> toTree(Object value) {
        return Mono.fromCallable(() -> JsonNodeUtils.fromGsonElement(gson.toJsonTree(value)));
    }

    @Override
    public Mono<String> getSerializerMemberName(Field field) {
        return Mono.fromCallable(() -> {
            if (!field.isAnnotationPresent(SerializedName.class)) {
                return field.getName();
            }
            return field.getDeclaredAnnotation(SerializedName.class).value();
        });
    }
}
