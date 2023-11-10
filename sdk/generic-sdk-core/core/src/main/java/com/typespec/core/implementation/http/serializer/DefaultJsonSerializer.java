// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.serializer;

import com.typespec.core.http.models.HttpHeaders;
import com.typespec.core.models.TypeReference;
import com.typespec.core.util.ClientLogger;
import com.typespec.core.util.serializer.JsonSerializer;
import com.typespec.json.JsonProviders;
import com.typespec.json.JsonReader;
import com.typespec.json.JsonSerializable;
import com.typespec.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

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
            if (JsonSerializable.class.isAssignableFrom(typeReference.getJavaClass())) {
                Class<T> clazz = typeReference.getJavaClass();

                return (T) clazz.getMethod("fromJson", JsonReader.class).invoke(null, jsonReader);
            } else {
                return (T) jsonReader.readUntyped();
            }
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
            if (JsonSerializable.class.isAssignableFrom(typeReference.getJavaClass())) {
                Class<T> clazz = typeReference.getJavaClass();

                return (T) clazz.getMethod("fromJson", JsonReader.class).invoke(null, jsonReader);
            } else {
                return (T) jsonReader.readUntyped();
            }
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    @Override
    public <T> T deserialize(HttpHeaders httpHeaders, Type type) throws IOException {
        return null;
    }

    @Override
    public byte[] serializeToBytes(Object value) {
        if (value == null) {
            return null;
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             JsonWriter jsonWriter = JsonProviders.createWriter(byteArrayOutputStream)) {

            jsonWriter.writeUntyped(value);
            jsonWriter.flush();

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        if (value == null) {
            return;
        }

        try (JsonWriter jsonWriter = JsonProviders.createWriter(stream)) {
            jsonWriter.writeUntyped(value);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }
}
