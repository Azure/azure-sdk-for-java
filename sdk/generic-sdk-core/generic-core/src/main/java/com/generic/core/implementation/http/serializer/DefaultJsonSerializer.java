// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.serializer;

import com.generic.core.models.TypeReference;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.serializer.JsonSerializer;
import com.generic.json.JsonProviders;
import com.generic.json.JsonReader;
import com.generic.json.JsonSerializable;
import com.generic.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Default implementation of the {@link JsonSerializer}.
 */
public class DefaultJsonSerializer implements JsonSerializer {
    // DefaultJsonSerializer is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(DefaultJsonSerializer.class);

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserializeFromBytes(byte[] bytes, TypeReference<T> typeReference) {
        try (JsonReader jsonReader = JsonProviders.createReader(bytes)) {
            Class<T> clazz = typeReference.getJavaClass();

            return (T) clazz.getMethod("fromJson", clazz).invoke(null, jsonReader);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        try (JsonReader jsonReader = JsonProviders.createReader(stream)) {
            Class<T> clazz = typeReference.getJavaClass();

            return (T) clazz.getMethod("fromJson", clazz).invoke(null, jsonReader);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    @Override
    public byte[] serializeToBytes(Object value) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            JsonWriter jsonWriter = JsonProviders.createWriter(byteArrayOutputStream);

            JsonSerializable<?> jsonSerializable = (JsonSerializable<?>) value;

            jsonWriter.writeJson(jsonSerializable);
            jsonWriter.close();

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        try {
            JsonWriter jsonWriter = JsonProviders.createWriter(stream);

            JsonSerializable<?> jsonSerializable = (JsonSerializable<?>) value;

            jsonWriter.writeJson(jsonSerializable);
            jsonWriter.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }
}
