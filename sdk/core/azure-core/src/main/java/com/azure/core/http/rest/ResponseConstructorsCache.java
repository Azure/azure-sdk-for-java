// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.ReflectionUtilsApi;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A concurrent cache of {@link Response} {@link MethodHandle} constructors.
 */
final class ResponseConstructorsCache {
    private static final String THREE_PARAM_ERROR = "Failed to deserialize 3-parameter response.";
    private static final String FOUR_PARAM_ERROR = "Failed to deserialize 4-parameter response.";
    private static final String FIVE_PARAM_ERROR = "Failed to deserialize 5-parameter response.";
    private static final String INVALID_PARAM_COUNT = "Response constructor with expected parameters not found.";

    private static final Map<Class<?>, MethodHandle> CACHE = new ConcurrentHashMap<>();

    private final ClientLogger logger = new ClientLogger(ResponseConstructorsCache.class);

    /**
     * Identify the suitable {@link MethodHandle} to construct the given response class.
     *
     * @param responseClass The response class.
     * @return The {@link MethodHandle} that is capable of constructing an instance of the class or null if no handle is
     * found.
     */
    MethodHandle get(Class<? extends Response<?>> responseClass) {
        return CACHE.computeIfAbsent(responseClass, this::locateResponseConstructor);
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
    private MethodHandle locateResponseConstructor(Class<?> responseClass) {
        MethodHandles.Lookup lookupToUse;
        try {
            lookupToUse = ReflectionUtilsApi.INSTANCE.getLookupToUse(responseClass);
        } catch (Throwable t) {
            throw logger.logExceptionAsError(new RuntimeException(t));
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
                     * 2) Use Java 9+ Module class to add reads in com.azure.core and the SDK library exports to
                     * com.azure.core for implementation.
                     * 3) SDK libraries create an accessible MethodHandles.Lookup which com.azure.core can use to spoof
                     * as the SDK library.
                     */
                    return lookupToUse.unreflectConstructor(constructor);
                } catch (Throwable t) {
                    throw logger.logExceptionAsError(new RuntimeException(t));
                }
            }
        }
        return null;
    }

    /**
     * Invoke the {@link MethodHandle} to construct and instance of the response class.
     *
     * @param handle The {@link MethodHandle} capable of constructing an instance of the response class.
     * @param decodedResponse The decoded HTTP response.
     * @param bodyAsObject The HTTP response body.
     * @return An instance of the {@link Response} implementation.
     */
    Mono<Response<?>> invoke(final MethodHandle handle,
        final HttpResponseDecoder.HttpDecodedResponse decodedResponse, final Object bodyAsObject) {
        final HttpResponse httpResponse = decodedResponse.getSourceResponse();
        final HttpRequest httpRequest = httpResponse.getRequest();
        final int responseStatusCode = httpResponse.getStatusCode();
        final HttpHeaders responseHeaders = httpResponse.getHeaders();

        final int paramCount = handle.type().parameterCount();
        switch (paramCount) {
            case 3:
                return constructResponse(handle, THREE_PARAM_ERROR, logger, httpRequest, responseStatusCode,
                    responseHeaders);
            case 4:
                return constructResponse(handle, FOUR_PARAM_ERROR, logger, httpRequest, responseStatusCode,
                    responseHeaders, bodyAsObject);
            case 5:
                return constructResponse(handle, FIVE_PARAM_ERROR, logger, httpRequest, responseStatusCode,
                    responseHeaders, bodyAsObject, decodedResponse.getDecodedHeaders());
            default:
                return monoError(logger, new IllegalStateException(INVALID_PARAM_COUNT));
        }
    }

    private static Mono<Response<?>> constructResponse(MethodHandle handle, String exceptionMessage,
        ClientLogger logger, Object... params) {
        return Mono.defer(() -> {
            try {
                return Mono.just((Response<?>) handle.invokeWithArguments(params));
            } catch (Throwable throwable) {
                return monoError(logger, new RuntimeException(exceptionMessage, throwable));
            }
        });
    }
}
