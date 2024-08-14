// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * The JSON Converter.
 */
public final class JsonConverterUtil {
    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(JsonConverterUtil.class.getName());

    /**
     * Stores the JSON cache.
     */
    private static final Map<Class<?>, ReflectiveInvoker> FROM_JSON_CACHE;

    static {
        FROM_JSON_CACHE = new ConcurrentHashMap<>();
    }

    /**
     * Deserializes the {@code json} as an instance of {@link JsonSerializable}.
     *
     * @param jsonSerializable The {@link JsonSerializable} represented by the {@code json}.
     * @param json The JSON being deserialized.
     *
     * @return An instance of {@code jsonSerializable} based on the {@code json}.
     *
     * @throws IOException If an error occurs during deserialization.
     * @throws IllegalStateException If the {@code jsonSerializable} does not have a static {@code fromJson} method.
     * @throws Error If an error occurs during deserialization.
     */
    public static Object fromJson(Class<?> jsonSerializable, String json) throws IOException {
        if (FROM_JSON_CACHE.size() >= 10000) {
            FROM_JSON_CACHE.clear();
        }

        ReflectiveInvoker readJson = FROM_JSON_CACHE.computeIfAbsent(jsonSerializable, clazz -> {
            try {
                return ReflectionUtils.getMethodInvoker(clazz,
                    jsonSerializable.getDeclaredMethod("fromJson", JsonReader.class));
            } catch (Exception e) {
                throw new IllegalStateException("Unable to find fromJson method", e);
            }
        });

        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return readJson.invokeStatic(jsonReader);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof Exception) {
                throw new IOException(e);
            } else {
                throw (Error) e;
            }
        }
    }

    /**
     * Serializes an object to a JSON string.
     *
     * @param object The object to serialize.
     */
    public static String toJson(JsonSerializable<?> object) {
        LOGGER.entering("JsonConverterUtil", "toJson", object);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            object.toJson(JsonProviders.createWriter(outputStream)).flush();

            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(WARNING, "Unable to convert to JSON", e);
        }

        LOGGER.exiting("JsonConverterUtil", "toJson");

        return null;
    }
}
