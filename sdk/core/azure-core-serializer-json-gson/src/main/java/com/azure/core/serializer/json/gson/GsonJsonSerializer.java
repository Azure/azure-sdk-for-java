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
    public <T> T deserializeSync(InputStream stream, TypeReference<T> typeReference) {
        return gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), typeReference.getJavaType());
    }

    @Override
    public <T> Mono<T> deserialize(InputStream stream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserializeSync(stream, typeReference));
    }

    @Override
    public <T> T deserializeTreeSync(JsonNode jsonNode, TypeReference<T> typeReference) {
        return gson.fromJson(JsonNodeUtils.toGsonElement(jsonNode), typeReference.getJavaType());
    }

    @Override
    public <T> Mono<T> deserializeTree(JsonNode jsonNode, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserializeTreeSync(jsonNode, typeReference));
    }

    @Override
    public <S extends OutputStream> S serializeSync(S stream, Object value) {
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
    public <S extends OutputStream> Mono<S> serialize(S stream, Object value) {
        return Mono.fromCallable(() -> serializeSync(stream, value));
    }

    @Override
    public <S extends OutputStream> S serializeTreeSync(S stream, JsonNode jsonNode) {
        return serializeSync(stream, JsonNodeUtils.toGsonElement(jsonNode));
    }

    @Override
    public <S extends OutputStream> Mono<S> serializeTree(S stream, JsonNode jsonNode) {
        return serialize(stream, JsonNodeUtils.toGsonElement(jsonNode));
    }

    @Override
    public JsonNode toTreeSync(InputStream stream) {
        return JsonNodeUtils.fromGsonElement(new JsonParser().parse(new InputStreamReader(stream, UTF_8)));
    }

    @Override
    public Mono<JsonNode> toTree(InputStream stream) {
        return Mono.fromCallable(() -> toTreeSync(stream));
    }

    @Override
    public JsonNode toTreeSync(Object value) {
        return JsonNodeUtils.fromGsonElement(gson.toJsonTree(value));
    }

    @Override
    public Mono<JsonNode> toTree(Object value) {
        return Mono.fromCallable(() -> toTreeSync(value));
    }
}
