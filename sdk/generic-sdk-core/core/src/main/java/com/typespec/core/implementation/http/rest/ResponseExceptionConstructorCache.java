// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.http.exception.HttpResponseException;
import com.typespec.core.http.models.HttpResponse;
import com.typespec.core.implementation.ReflectiveInvoker;
import com.typespec.core.util.ClientLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent cache of {@link HttpResponseException} {@link ReflectiveInvoker} constructors.
 */
public final class ResponseExceptionConstructorCache {
    private static final Map<Class<? extends HttpResponseException>, ReflectiveInvoker> CACHE = new ConcurrentHashMap<>();
    private static final ClientLogger LOGGER = new ClientLogger(ResponseExceptionConstructorCache.class);

    /**
     * Identifies the suitable {@link ReflectiveInvoker} to construct the given exception class.
     *
     * @param exceptionClass The exception class.
     * @return The {@link ReflectiveInvoker} that is capable of constructing an instance of the class, or null if no handle
     * is found.
     */
    public ReflectiveInvoker get(Class<? extends HttpResponseException> exceptionClass, Class<?> exceptionBodyType) {
        return CACHE.computeIfAbsent(exceptionClass, key -> locateExceptionConstructor(key, exceptionBodyType));
    }

    private static ReflectiveInvoker locateExceptionConstructor(Class<? extends HttpResponseException> exceptionClass,
        Class<?> exceptionBodyType) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends HttpResponseException> T invoke(ReflectiveInvoker reflectiveInvoker, String exceptionMessage,
                                                             HttpResponse httpResponse, Object exceptionBody) {
        try {
            return (T) reflectiveInvoker.invokeWithArguments(exceptionMessage, httpResponse, exceptionBody);
        } catch (Exception exception) {
            if (exception instanceof RuntimeException) {
                throw LOGGER.logThrowableAsError((RuntimeException) exception);
            }

            throw LOGGER.logThrowableAsError(new IllegalStateException(exceptionMessage, exception));
        }
    }
}
