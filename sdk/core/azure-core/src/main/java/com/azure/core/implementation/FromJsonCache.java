// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent cache of {@link JsonSerializable#fromJson(JsonReader) JsonCapable fromJson}
 * {@link MethodHandle MethodHandles}.
 */
public final class FromJsonCache {
    private static final ClientLogger LOGGER = new ClientLogger(FromJsonCache.class);
    private static final Map<Type, Boolean> IS_JSON_CAPABLE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> FROM_READER_CACHE =
        new ConcurrentHashMap<>();

    /**
     * Whether the {@link Type} implements {@link JsonSerializable}.
     *
     * @param type The type.
     * @return Whether the type implements {@link JsonSerializable}.
     */
    public static boolean isJsonCapable(Type type) {
        return IS_JSON_CAPABLE_CACHE.computeIfAbsent(type, t -> TypeUtil.typeImplementsInterface(t, JsonSerializable.class));
    }

    /**
     * Reads the {@link JsonReader} into the type of {@link JsonSerializable}.
     *
     * @param jsonCapable The {@link JsonSerializable} type.
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link JsonSerializable} object based on what was read from the {@link JsonReader}.
     */
    public static Object fromJson(Class<?> jsonCapable, JsonReader jsonReader) {
        MethodHandle fromJsonHandle = FROM_READER_CACHE.computeIfAbsent(jsonCapable, type -> {
            try {
                Method method = type.getMethod("fromJson", JsonReader.class);
                return ReflectionUtilsApi.INSTANCE.getLookupToUse(type).unreflect(method);
            } catch (Exception ex) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(ex));
            }
        });

        try {
            return fromJsonHandle.invoke(jsonReader);
        } catch (Throwable throwable) {
            if (throwable instanceof Error) {
                throw (Error) throwable;
            }

            if (throwable instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) throwable);
            }

            throw LOGGER.logExceptionAsError(new IllegalStateException(throwable));
        }
    }

    private FromJsonCache() {
    }
}
