// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

class ResponseConstructorsNoCacheReflection {
    private final ClientLogger logger = new ClientLogger(ResponseConstructorsCacheLambdaMetaFactory.class);

    Constructor<? extends Response<?>> get(Class<? extends Response<?>> responseClass) {
        return locateResponseConstructor(responseClass);
    }

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
                    throw Exceptions.propagate(e);
                }
            case 4:
                try {
                    return Mono.just(constructor.newInstance(httpRequest,
                            responseStatusCode,
                            responseHeaders,
                            bodyAsObject));
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw Exceptions.propagate(e);
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
                                throw Exceptions.propagate(e);
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
                                throw Exceptions.propagate(e);
                            }
                        }));
            default:
                throw logger.logExceptionAsError(
                    new IllegalStateException("Response constructor with expected parameters not found."));
        }
    }
}
