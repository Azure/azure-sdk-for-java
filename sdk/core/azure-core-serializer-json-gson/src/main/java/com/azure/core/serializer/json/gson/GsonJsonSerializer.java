// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.experimental.serializer.JsonNode;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.experimental.serializer.TypeReference;
import com.azure.core.util.logging.ClientLogger;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * GSON based implementation of the {@link JsonSerializer} interface.
 */
public final class GsonJsonSerializer implements JsonSerializer {
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
    public <T> T deserializeTree(JsonNode jsonNode, TypeReference<T> typeReference) {
        return gson.fromJson(JsonNodeUtils.toGsonElement(jsonNode), typeReference.getJavaType());
    }

    @Override
    public <T> Mono<T> deserializeTreeAsync(JsonNode jsonNode, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserializeTree(jsonNode, typeReference));
    }

    @Override
    public <S extends OutputStream> S serialize(S stream, Object value) {
        Writer writer = new OutputStreamWriter(stream, UTF_8);
        gson.toJson(value, writer);

        try {
            writer.flush();
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }

        return stream;
    }

    @Override
    public <S extends OutputStream> Mono<S> serializeAsync(S stream, Object value) {
        return Mono.fromCallable(() -> serialize(stream, value));
    }

    @Override
    public <S extends OutputStream> S serializeTree(S stream, JsonNode jsonNode) {
        return serialize(stream, JsonNodeUtils.toGsonElement(jsonNode));
    }

    @Override
    public <S extends OutputStream> Mono<S> serializeTreeAsync(S stream, JsonNode jsonNode) {
        return serializeAsync(stream, JsonNodeUtils.toGsonElement(jsonNode));
    }

    @Override
    public JsonNode toTree(InputStream stream) {
        return JsonNodeUtils.fromGsonElement(new JsonParser().parse(new InputStreamReader(stream, UTF_8)));
    }

    @Override
    public Mono<JsonNode> toTreeAsync(InputStream stream) {
        return Mono.fromCallable(() -> toTree(stream));
    }

    @Override
    public JsonNode toTree(Object value) {
        return JsonNodeUtils.fromGsonElement(gson.toJsonTree(value));
    }

    @Override
    public Mono<JsonNode> toTreeAsync(Object value) {
        return Mono.fromCallable(() -> toTree(value));
    }
}
