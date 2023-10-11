// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.rest.Response;
import com.typespec.core.implementation.ReflectionUtils;
import com.typespec.core.implementation.serializer.HttpResponseDecoder;
import com.typespec.core.util.logging.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent cache of {@link Response} {@link MethodHandle} constructors.
 */
public final class ResponseConstructorsCache {
    private static final String THREE_PARAM_ERROR = "Failed to deserialize 3-parameter response.";
    private static final String FOUR_PARAM_ERROR = "Failed to deserialize 4-parameter response.";
    private static final String FIVE_PARAM_ERROR = "Failed to deserialize 5-parameter response.";
    private static final String INVALID_PARAM_COUNT = "Response constructor with expected parameters not found.";

    private static final Map<Class<?>, MethodHandle> CACHE = new ConcurrentHashMap<>();

    private static final ClientLogger LOGGER = new ClientLogger(ResponseConstructorsCache.class);

    /**
     * Identify the suitable {@link MethodHandle} to construct the given response class.
     *
     * @param responseClass The response class.
     * @return The {@link MethodHandle} that is capable of constructing an instance of the class or null if no handle is
     * found.
     */
    public MethodHandle get(Class<? extends Response<?>> responseClass) {
        return CACHE.computeIfAbsent(responseClass, ResponseConstructorsCache::locateResponseConstructor);
    }

    /**
     * Identify the most specific {@link MethodHandle} to construct the given response class.
     * <p>
     * Lookup is the following order:
     * <ol>
     * <li>(httpRequest, statusCode, headers, body, decodedHeaders)</li>
     * <li>(httpRequest, statusCode, headers, body)</li>
     * <li>(httpRequest, statusCode, headers)</li>
     * </ol>
     *
     * Developer Note: This method logic can be easily replaced with Java.Stream and associated operators but we're
     * using basic sort and loop constructs here as this method is in hot path and Stream route is consuming a fair
     * amount of resources.
     *
     * @param responseClass The response class.
     * @return The {@link MethodHandle} that is capable of constructing an instance of the class or null if no handle is
     * found.
     */
    private static MethodHandle locateResponseConstructor(Class<?> responseClass) {
        MethodHandles.Lookup lookupToUse;
        try {
            lookupToUse = ReflectionUtils.getLookupToUse(responseClass);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) ex);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(ex));
        }

        /*
         * Now that the MethodHandles.Lookup has been found to create the MethodHandle instance begin searching for
         * the most specific MethodHandle that can be used to create the response class (as mentioned in the method
         * Javadocs).
         */
        Constructor<?>[] constructors = responseClass.getDeclaredConstructors();
        // Sort constructors in the "descending order" of parameter count.
        Arrays.sort(constructors, Comparator.comparing(Constructor::getParameterCount, (a, b) -> b - a));
        for (Constructor<?> constructor : constructors) {
            final int paramCount = constructor.getParameterCount();
            if (paramCount >= 3 && paramCount <= 5) {
                try {
                    /*
                     * From here we have three, possibly more options, to resolve this.
                     *
                     * 1) setAccessible to true in the response class (requires doPrivilege).
                     * 2) Use Java 9+ Module class to add reads in com.typespec.core and the SDK library exports to
                     * com.typespec.core for implementation.
                     * 3) SDK libraries create an accessible MethodHandles.Lookup which com.typespec.core can use to spoof
                     * as the SDK library.
                     */
                    return lookupToUse.unreflectConstructor(constructor);
                } catch (IllegalAccessException ex) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(ex));
                }
            }
        }

        // Before this was returning null, but in all cases where null is returned from this method an exception would
        // be thrown later. Instead, just throw here to properly use computeIfAbsent by not inserting a null key-value
        // pair that would cause the computation to always be performed.
        throw LOGGER.logExceptionAsError(new RuntimeException("Cannot find suitable constructor for class "
            + responseClass));
    }

    /**
     * Invoke the {@link MethodHandle} to construct and instance of the response class.
     *
     * @param handle The {@link MethodHandle} capable of constructing an instance of the response class.
     * @param decodedResponse The decoded HTTP response.
     * @param bodyAsObject The HTTP response body.
     * @return An instance of the {@link Response} implementation.
     */
    public Response<?> invoke(final MethodHandle handle,
        final HttpResponseDecoder.HttpDecodedResponse decodedResponse, final Object bodyAsObject) {
        final HttpResponse httpResponse = decodedResponse.getSourceResponse();
        final HttpRequest httpRequest = httpResponse.getRequest();
        final int responseStatusCode = httpResponse.getStatusCode();
        final HttpHeaders responseHeaders = httpResponse.getHeaders();

        final int paramCount = handle.type().parameterCount();
        switch (paramCount) {
            case 3:
                return constructResponse(handle, THREE_PARAM_ERROR, httpRequest, responseStatusCode,
                    responseHeaders);
            case 4:
                return constructResponse(handle, FOUR_PARAM_ERROR, httpRequest, responseStatusCode,
                    responseHeaders, bodyAsObject);
            case 5:
                return constructResponse(handle, FIVE_PARAM_ERROR, httpRequest, responseStatusCode,
                    responseHeaders, bodyAsObject, decodedResponse.getDecodedHeaders());
            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException(INVALID_PARAM_COUNT));
        }
    }

    private static Response<?> constructResponse(MethodHandle handle, String exceptionMessage, Object... params) {
        try {
            return (Response<?>) handle.invokeWithArguments(params);
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
