// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.util.ClientLogger;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent cache of {@link Response} {@link ReflectiveInvoker} constructors.
 */
public final class ResponseConstructorsCache {
    private static final String THREE_PARAM_ERROR = "Failed to deserialize 3-parameter response.";
    private static final String FOUR_PARAM_ERROR = "Failed to deserialize 4-parameter response.";
    private static final String INVALID_PARAM_COUNT = "Response constructor with expected parameters not found.";

    private static final Map<Class<?>, ReflectiveInvoker> CACHE = new ConcurrentHashMap<>();

    private static final ClientLogger LOGGER = new ClientLogger(ResponseConstructorsCache.class);

    /**
     * Identify the suitable {@link ReflectiveInvoker} to construct the given response class.
     *
     * @param responseClass The response class.
     * @return The {@link ReflectiveInvoker} that is capable of constructing an instance of the class or null if no handle is
     * found.
     */
    public ReflectiveInvoker get(Class<? extends Response<?>> responseClass) {
        return CACHE.computeIfAbsent(responseClass, ResponseConstructorsCache::locateResponseConstructor);
    }

    /**
     * Identify the most specific {@link ReflectiveInvoker} to construct the given response class.
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
     * @return The {@link ReflectiveInvoker} that is capable of constructing an instance of the class or null if no handle is
     * found.
     */
    private static ReflectiveInvoker locateResponseConstructor(Class<?> responseClass) {
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
                     * 2) Use Java 9+ Module class to add reads in io.clientcore.core and the SDK library exports to
                     * io.clientcore.core for implementation.
                     * 3) SDK libraries create an accessible MethodHandles.Lookup which io.clientcore.core can use to spoof
                     * as the SDK library.
                     */
                    return ReflectionUtils.getConstructorInvoker(responseClass, constructor);
                } catch (Exception ex) {
                    if (ex instanceof RuntimeException) {
                        throw LOGGER.logThrowableAsError((RuntimeException) ex);
                    }

                    throw LOGGER.logThrowableAsError(new RuntimeException(ex));
                }
            }
        }

        // Before this was returning null, but in all cases where null is returned from this method an exception would
        // be thrown later. Instead, just throw here to properly use computeIfAbsent by not inserting a null key-value
        // pair that would cause the computation to always be performed.
        throw LOGGER.logThrowableAsError(new RuntimeException("Cannot find suitable constructor for class "
            + responseClass));
    }

    /**
     * Invoke the {@link ReflectiveInvoker} to construct and instance of the response class.
     *
     * @param reflectiveInvoker The {@link ReflectiveInvoker} capable of constructing an instance of the response class.
     * @param response The response.
     * @param bodyAsObject The HTTP response body.
     * @return An instance of the {@link Response} implementation.
     */
    public Response<?> invoke(ReflectiveInvoker reflectiveInvoker, Response<?> response, Object bodyAsObject) {
        final HttpRequest httpRequest = response.getRequest();
        final int responseStatusCode = response.getStatusCode();
        final HttpHeaders responseHeaders = response.getHeaders();

        final int paramCount = reflectiveInvoker.getParameterCount();
        switch (paramCount) {
            case 3:
                return constructResponse(reflectiveInvoker, THREE_PARAM_ERROR, httpRequest, responseStatusCode,
                    responseHeaders);
            case 4:
                return constructResponse(reflectiveInvoker, FOUR_PARAM_ERROR, httpRequest, responseStatusCode,
                    responseHeaders, bodyAsObject);
            default:
                throw LOGGER.logThrowableAsError(new IllegalStateException(INVALID_PARAM_COUNT));
        }
    }

    private static Response<?> constructResponse(ReflectiveInvoker reflectiveInvoker, String exceptionMessage, Object... params) {
        try {
            return (Response<?>) reflectiveInvoker.invokeStatic(params);
        } catch (Exception exception) {
            if (exception instanceof RuntimeException) {
                throw LOGGER.logThrowableAsError((RuntimeException) exception);
            }

            throw LOGGER.logThrowableAsError(new IllegalStateException(exceptionMessage, exception));
        }
    }
}
