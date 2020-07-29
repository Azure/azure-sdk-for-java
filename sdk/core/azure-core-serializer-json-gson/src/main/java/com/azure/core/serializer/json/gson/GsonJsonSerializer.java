// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.experimental.serializer.JsonNode;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.experimental.serializer.PropertyNameSerializer;
import com.azure.core.experimental.serializer.TypeReference;
import com.azure.core.util.CoreUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

/**
 * GSON based implementation of the {@link JsonSerializer} interface.
 */
public final class GsonJsonSerializer implements JsonSerializer, PropertyNameSerializer {
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
    public <T> Mono<T> deserialize(InputStream stream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
            typeReference.getJavaType()));
    }

    @Override
    public <T> Mono<T> deserializeTree(JsonNode jsonNode, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> gson.fromJson(JsonNodeUtils.toGsonElement(jsonNode),
            typeReference.getJavaType()));
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
    public String getSerializerMemberName(Member member) {
        if (Modifier.isTransient(member.getModifiers())) {
            return null;
        }
        if (member instanceof Field) {
            Field f = (Field) member;
            if (gson.excluder().excludeField(f, true)) {
                return null;
            }
            if (!f.isAnnotationPresent(SerializedName.class)) {
                return member.getName();
            }
            String propertyName = f.getDeclaredAnnotation(SerializedName.class).value();
            return CoreUtils.isNullOrEmpty(propertyName) ? f.getName() : propertyName;
        } else if (member instanceof Method) {
            return member.getName();
        }
        return null;
    }

}
