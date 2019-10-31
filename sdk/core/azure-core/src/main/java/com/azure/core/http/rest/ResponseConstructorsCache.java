// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A concurrent cache of {@link Response} constructors.
 */
final class ResponseConstructorsCache {
    private final ClientLogger logger = new ClientLogger(ResponseConstructorsCache.class);
    private final Map<Class<?>, Constructor<? extends Response<?>>> cache = new ConcurrentHashMap<>();

    /**
     * Identify the suitable constructor for the given response class.
     *
     * @param responseClass the response class
     * @return identified constructor, null if there is no match
     */
    Constructor<? extends Response<?>> get(Class<? extends Response<?>> responseClass) {
        return this.cache.computeIfAbsent(responseClass, this::locateResponseConstructor);
    }

    /**
     * Identify the most specific constructor for the given response class.
     *
     * The most specific constructor is looked up following order:
     * 1. (httpRequest, statusCode, headers, body, decodedHeaders)
     * 2. (httpRequest, statusCode, headers, body)
     * 3. (httpRequest, statusCode, headers)
     *
     * Developer Note: This method logic can be easily replaced with Java.Stream
     * and associated operators but we're using basic sort and loop constructs
     * here as this method is in hot path and Stream route is consuming a fair
     * amount of resources.
     *
     * @param responseClass the response class
     * @return identified constructor, null if there is no match
     */
    @SuppressWarnings("unchecked")
    private Constructor<? extends Response<?>> locateResponseConstructor(Class<?> responseClass) {
        Constructor<?>[] constructors = responseClass.getDeclaredConstructors();
        // Sort constructors in the "descending order" of parameter count.
        Arrays.sort(constructors, Comparator.comparing(Constructor::getParameterCount, (a, b) -> b - a));
        for (Constructor<?> constructor : constructors) {
            final int paramCount = constructor.getParameterCount();
            if (paramCount >= 3 && paramCount <= 5) {
                try {
                    return (Constructor<? extends Response<?>>) constructor;
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
    Mono<Response<?>> invoke(final Constructor<? extends Response<?>> constructor,
                             final HttpResponseDecoder.HttpDecodedResponse decodedResponse,
                             final Object bodyAsObject) {
        final HttpResponse httpResponse = decodedResponse.getSourceResponse();
        final HttpRequest httpRequest = httpResponse.getRequest();
        final int responseStatusCode = httpResponse.getStatusCode();
        final HttpHeaders responseHeaders = httpResponse.getHeaders();

        final int paramCount = constructor.getParameterCount();
        switch (paramCount) {
            case 3:
                try {
                    return Mono.just(constructor.newInstance(httpRequest,
                        responseStatusCode,
                        responseHeaders));
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw logger.logExceptionAsError(new RuntimeException("Failed to deserialize 3-parameter"
                        + " response. ", e));
                }
            case 4:
                try {
                    return Mono.just(constructor.newInstance(httpRequest,
                        responseStatusCode,
                        responseHeaders,
                        bodyAsObject));
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw logger.logExceptionAsError(new RuntimeException("Failed to deserialize 4-parameter"
                        + " response. ", e));
                }
            case 5:
                return decodedResponse.getDecodedHeaders()
                    .map((Function<Object, Response<?>>) decodedHeaders -> {
                        try {
                            return constructor.newInstance(httpRequest,
                                responseStatusCode,
                                responseHeaders,
                                bodyAsObject,
                                decodedHeaders);
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            throw logger.logExceptionAsError(new RuntimeException("Failed to deserialize 5-parameter"
                                + " response with decoded headers. ", e));
                        }
                    })
                    .switchIfEmpty(Mono.defer((Supplier<Mono<Response<?>>>) () -> {
                        try {
                            return Mono.just(constructor.newInstance(httpRequest,
                                responseStatusCode,
                                responseHeaders,
                                bodyAsObject,
                                null));
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            throw logger.logExceptionAsError(new RuntimeException(
                                "Failed to deserialize 5-parameter response without decoded headers.", e));
                        }
                    }));
            default:
                throw logger.logExceptionAsError(
                    new IllegalStateException("Response constructor with expected parameters not found."));
        }
    }
}
