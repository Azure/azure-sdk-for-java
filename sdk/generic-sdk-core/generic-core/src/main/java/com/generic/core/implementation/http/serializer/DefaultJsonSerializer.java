// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.serializer;

import com.generic.core.implementation.TypeUtil;
import com.generic.core.util.ClientLogger;
import com.generic.core.util.serializer.JsonSerializer;
import io.clientcore.json.JsonProviders;
import io.clientcore.json.JsonReader;
import io.clientcore.json.JsonSerializable;
import io.clientcore.json.JsonWriter;

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
    public <T> T deserializeFromBytes(byte[] bytes, Type type) {
        try (JsonReader jsonReader = JsonProviders.createReader(bytes)) {
            if (type instanceof Class<?> && JsonSerializable.class.isAssignableFrom(TypeUtil.getRawClass(type))) {
                Class<T> clazz = (Class<T>) type;

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
    public <T> T deserializeFromStream(InputStream stream, Type type) {
        try (JsonReader jsonReader = JsonProviders.createReader(stream)) {
            if (type instanceof Class<?> && JsonSerializable.class.isAssignableFrom(TypeUtil.getRawClass(type))) {
                Class<T> clazz = (Class<T>) type;

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
    public void serializeToStream(OutputStream stream, Object value) {
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
