// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.exception.HttpResponseException;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.implementation.ReflectionUtils;
import com.typespec.core.util.logging.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent cache of {@link HttpResponseException} {@link MethodHandle} constructors.
 */
public final class ResponseExceptionConstructorCache {
    private static final Map<Class<? extends HttpResponseException>, MethodHandle> CACHE = new ConcurrentHashMap<>();
    private static final ClientLogger LOGGER = new ClientLogger(ResponseExceptionConstructorCache.class);

    /**
     * Identifies the suitable {@link MethodHandle} to construct the given exception class.
     *
     * @param exceptionClass The exception class.
     * @return The {@link MethodHandle} that is capable of constructing an instance of the class, or null if no handle
     * is found.
     */
    public MethodHandle get(Class<? extends HttpResponseException> exceptionClass, Class<?> exceptionBodyType) {
        return CACHE.computeIfAbsent(exceptionClass, key -> locateExceptionConstructor(key, exceptionBodyType));
    }

    private static MethodHandle locateExceptionConstructor(Class<? extends HttpResponseException> exceptionClass,
        Class<?> exceptionBodyType) {
        try {
            MethodHandles.Lookup lookupToUse = ReflectionUtils.getLookupToUse(exceptionClass);
            Constructor<?> constructor = exceptionClass.getConstructor(String.class, HttpResponse.class,
                exceptionBodyType);

            return lookupToUse.unreflectConstructor(constructor);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) ex);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends HttpResponseException> T invoke(MethodHandle handle, String exceptionMessage,
        HttpResponse httpResponse, Object exceptionBody) {
        try {
            return (T) handle.invokeWithArguments(exceptionMessage, httpResponse, exceptionBody);
        } catch (Throwable throwable) {
            if (throwable instanceof Error) {
                throw (Error) throwable;
            }

            if (throwable instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) throwable);
            }

            throw LOGGER.logExceptionAsError(new IllegalStateException(exceptionMessage, throwable));
        }
    }
}
