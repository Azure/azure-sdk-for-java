// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.Invoker;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent cache of {@link HttpResponseException} {@link Invoker} constructors.
 */
public final class ResponseExceptionConstructorCache {
    private static final Map<Class<? extends HttpResponseException>, Invoker> CACHE = new ConcurrentHashMap<>();
    private static final ClientLogger LOGGER = new ClientLogger(ResponseExceptionConstructorCache.class);

    /**
     * Identifies the suitable {@link Invoker} to construct the given exception class.
     *
     * @param exceptionClass The exception class.
     * @return The {@link Invoker} that is capable of constructing an instance of the class, or null if no handle
     * is found.
     */
    public Invoker get(Class<? extends HttpResponseException> exceptionClass, Class<?> exceptionBodyType) {
        return CACHE.computeIfAbsent(exceptionClass, key -> locateExceptionConstructor(key, exceptionBodyType));
    }

    private static Invoker locateExceptionConstructor(Class<? extends HttpResponseException> exceptionClass,
        Class<?> exceptionBodyType) {
        try {
            return ReflectionUtils.getConstructorInvoker(exceptionClass,
                exceptionClass.getConstructor(String.class, HttpResponse.class, exceptionBodyType));
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) ex);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends HttpResponseException> T invoke(Invoker invoker, String exceptionMessage,
        HttpResponse httpResponse, Object exceptionBody) {
        try {
            return (T) invoker.invokeWithArguments(exceptionMessage, httpResponse, exceptionBody);
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
