// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.ReflectionUtilsApi;
import com.azure.core.util.logging.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent cache of {@link HttpResponseException} {@link MethodHandle} constructors.
 */
final class ResponseExceptionConstructorCache {
    private static final Map<Class<? extends HttpResponseException>, MethodHandle> CACHE = new ConcurrentHashMap<>();
    private static final ClientLogger LOGGER = new ClientLogger(ResponseExceptionConstructorCache.class);

    /**
     * Identifies the suitable {@link MethodHandle} to construct the given exception class.
     *
     * @param exceptionClass The exception class.
     * @return The {@link MethodHandle} that is capable of constructing an instance of the class, or null if no handle
     * is found.
     */
    MethodHandle get(Class<? extends HttpResponseException> exceptionClass, Class<?> exceptionBodyType) {
        return CACHE.computeIfAbsent(exceptionClass, key -> locateExceptionConstructor(key, exceptionBodyType));
    }

    private static MethodHandle locateExceptionConstructor(Class<? extends HttpResponseException> exceptionClass,
        Class<?> exceptionBodyType) {
        try {
            MethodHandles.Lookup lookupToUse = ReflectionUtilsApi.INSTANCE.getLookupToUse(exceptionClass);
            Constructor<?> constructor = exceptionClass.getConstructor(String.class, HttpResponse.class,
                exceptionBodyType);

            return lookupToUse.unreflectConstructor(constructor);
        } catch (Throwable t) {
            throw LOGGER.logExceptionAsError(new RuntimeException(t));
        }
    }

    @SuppressWarnings("unchecked")
    <T extends HttpResponseException> T invoke(MethodHandle handle, String exceptionMessage, HttpResponse httpResponse,
        Object exceptionBody) {
        try {
            return (T) handle.invokeWithArguments(exceptionMessage, httpResponse, exceptionBody);
        } catch (Throwable t) {
            throw LOGGER.logExceptionAsError(new RuntimeException(t));
        }
    }
}
