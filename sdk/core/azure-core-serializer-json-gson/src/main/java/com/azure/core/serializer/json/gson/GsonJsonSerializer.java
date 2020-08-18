// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.MemberNameConverter;
import com.azure.core.util.serializer.TypeReference;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * GSON based implementation of the {@link JsonSerializer} and {@link MemberNameConverter} interfaces.
 */
public final class GsonJsonSerializer implements JsonSerializer, MemberNameConverter {
    private final ClientLogger logger = new ClientLogger(GsonJsonSerializer.class);

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
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        return gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), typeReference.getJavaType());
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserialize(stream, typeReference));
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        Writer writer = new OutputStreamWriter(stream, UTF_8);
        gson.toJson(value, writer);

        try {
            writer.flush();
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream stream, Object value) {
        return Mono.fromRunnable(() -> serialize(stream, value));
    }

    @Override
    public String convertMemberName(Member member) {
        if (Modifier.isTransient(member.getModifiers())) {
            return null;
        }
        if (member instanceof Field) {
            Field f = (Field) member;
            if (f.isAnnotationPresent(SerializedName.class)) {
                return f.getDeclaredAnnotation(SerializedName.class).value();
            }
            return member.getName();
        }
        return null;
    }
}
