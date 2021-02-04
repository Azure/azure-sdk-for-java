// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
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
import java.util.function.Function;

/**
 * A concurrent cache of {@link Response} constructors.
 */
final class ResponseConstructorsCache {
    private static final String THREE_PARAM_EXCEPTION = "Failed to deserialize 3-parameter response.";
    private static final String FOUR_PARAM_EXCEPTION = "Failed to deserialize 4-parameter response.";
    private static final String FIVE_PARAM_EXCEPTION_HEADERS = "Failed to deserialize 5-parameter response with "
        + "decoded headers.";
    private static final String FIVE_PARAM_EXCEPTION_NO_HEADERS = "Failed to deserialize 5-parameter response "
        + "without decoded headers.";
    private static final String INVALID_PARAMETER_COUNT = "Response constructor with expected parameters not found.";

    private final ClientLogger logger = new ClientLogger(ResponseConstructorsCache.class);
    private final Map<Class<?>, MethodHandle> cache = new ConcurrentHashMap<>();

    /**
     * Identify the suitable constructor for the given response class.
     *
     * @param responseClass the response class
     * @return identified constructor, null if there is no match
     */
    MethodHandle get(Class<? extends Response<?>> responseClass) {
        return this.cache.computeIfAbsent(responseClass, this::locateResponseConstructor);
    }

    /**
     * Identify the most specific constructor for the given response class.
     * <p>
     * The most specific constructor is looked up following order:
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
     * @param responseClass the response class
     * @return identified constructor, null if there is no match
     */
    private MethodHandle locateResponseConstructor(Class<?> responseClass) {
        Constructor<?>[] constructors = responseClass.getDeclaredConstructors();
        // Sort constructors in the "descending order" of parameter count.
        Arrays.sort(constructors, Comparator.comparing(Constructor::getParameterCount, (a, b) -> b - a));
        for (Constructor<?> constructor : constructors) {
            final int paramCount = constructor.getParameterCount();
            if (paramCount >= 3 && paramCount <= 5) {
                try {
                    return MethodHandles.publicLookup().unreflectConstructor(constructor);
//                    return MethodHandles.lookup().unreflectConstructor(constructor);
                } catch (Throwable t) {
                    throw logger.logExceptionAsError(new RuntimeException(t));
                }
            }
        }
        return null;
    }

    /**
     * Invoke the constructor this type represents.
     *
     * @param constructor the constructor type
     * @param decodedResponse the decoded http response
     * @param bodyAsObject the http response content
     * @return an instance of a {@link Response} implementation
     */
    Mono<Response<?>> invoke(final MethodHandle constructor,
        final HttpResponseDecoder.HttpDecodedResponse decodedResponse, final Object bodyAsObject) {
        final HttpResponse httpResponse = decodedResponse.getSourceResponse();
        final HttpRequest httpRequest = httpResponse.getRequest();
        final int responseStatusCode = httpResponse.getStatusCode();
        final HttpHeaders responseHeaders = httpResponse.getHeaders();

        final int paramCount = constructor.type().parameterCount();
        switch (paramCount) {
            case 3:
                try {
                    return Mono.just((Response<?>) constructor.invoke(httpRequest, responseStatusCode,
                        responseHeaders));
                } catch (Throwable e) {
                    throw logger.logExceptionAsError(new RuntimeException(THREE_PARAM_EXCEPTION, e));
                }
            case 4:
                try {
                    return Mono.just((Response<?>) constructor.invoke(httpRequest, responseStatusCode, responseHeaders,
                        bodyAsObject));
                } catch (Throwable e) {
                    throw logger.logExceptionAsError(new RuntimeException(FOUR_PARAM_EXCEPTION, e));
                }
            case 5:
                return decodedResponse.getDecodedHeaders()
                    .map((Function<Object, Response<?>>) decodedHeaders -> {
                        try {
                            return (Response<?>) constructor.invoke(httpRequest, responseStatusCode, responseHeaders,
                                bodyAsObject, decodedHeaders);
                        } catch (Throwable e) {
                            throw logger.logExceptionAsError(new RuntimeException(FIVE_PARAM_EXCEPTION_HEADERS, e));
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        try {
                            return Mono.just((Response<?>) constructor.invoke(httpRequest, responseStatusCode,
                                responseHeaders, bodyAsObject, null));
                        } catch (Throwable e) {
                            throw logger.logExceptionAsError(new RuntimeException(FIVE_PARAM_EXCEPTION_NO_HEADERS, e));
                        }
                    }));
            default:
                throw logger.logExceptionAsError(new IllegalStateException(INVALID_PARAMETER_COUNT));
        }
    }
}
