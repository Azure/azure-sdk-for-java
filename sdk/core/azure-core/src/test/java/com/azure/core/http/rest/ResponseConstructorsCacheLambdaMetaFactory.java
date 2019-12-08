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

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A concurrent cache of {@link Response} constructors.
 */
final class ResponseConstructorsCacheLambdaMetaFactory {
    private final ClientLogger logger = new ClientLogger(ResponseConstructorsCache.class);
    private final Map<Class<?>, ResponseConstructor> cache = new ConcurrentHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Identify the suitable constructor for the given response class.
     *
     * @param responseClass the response class
     * @return identified constructor, null if there is no match
     */
    ResponseConstructor get(Class<? extends Response<?>> responseClass) {
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
    private ResponseConstructor locateResponseConstructor(Class<?> responseClass) {
        Constructor<?>[] constructors = responseClass.getDeclaredConstructors();
        // Sort constructors in the "descending order" of parameter count.
        Arrays.sort(constructors, Comparator.comparing(Constructor::getParameterCount, (a, b) -> b - a));
        for (Constructor<?> constructor : constructors) {
            final int paramCount = constructor.getParameterCount();
            if (paramCount >= 3 && paramCount <= 5) {
                try {
                    if (paramCount == 3) {
                        MethodHandle ctrMethodHandle = LOOKUP.unreflectConstructor(constructor);
                        return new ResponseConstructor(3, LambdaMetafactory.metafactory(LOOKUP,
                            "apply",
                            ResponseFunc3.METHOD_TYPE,
                            ResponseFunc3.SIGNATURE,
                            ctrMethodHandle,
                            ctrMethodHandle.type()).getTarget().invoke());
                    } else if (paramCount == 4) {
                        MethodHandle ctrMethodHandle = LOOKUP.unreflectConstructor(constructor);
                        return new ResponseConstructor(4, LambdaMetafactory.metafactory(LOOKUP,
                            "apply",
                            ResponseFunc4.METHOD_TYPE,
                            ResponseFunc4.SIGNATURE,
                            ctrMethodHandle,
                            ctrMethodHandle.type()).getTarget().invoke());
                    } else {
                        // paramCount == 5
                        MethodHandle ctrMethodHandle = LOOKUP.unreflectConstructor(constructor);
                        return new ResponseConstructor(5, LambdaMetafactory.metafactory(LOOKUP,
                            "apply",
                            ResponseFunc5.METHOD_TYPE,
                            ResponseFunc5.SIGNATURE,
                            ctrMethodHandle,
                            ctrMethodHandle.type())
                            .getTarget().invoke());
                    }
                } catch (Throwable t) {
                    throw logger.logExceptionAsError(new RuntimeException(t));
                }
            }
        }
        return null;
    }

    /**
     * Type that represent a {@link Response} constructor and can be used to invoke
     * the same constructor.
     */
    static final class ResponseConstructor {
        private final int parameterCount;
        private final Object responseFunc;

        /**
         * Creates ResponseConstructor.
         *
         * @param parameterCount the constructor parameter count
         * @param responseFunc the functional interface which delegate its abstract method
         *                 invocation to the invocation of a {@link Response} constructor
         */
        private ResponseConstructor(int parameterCount, Object responseFunc) {
            this.parameterCount = parameterCount;
            this.responseFunc = responseFunc;
        }

        /**
         * Invoke the {@link Response} constructor this type represents.
         *
         * @param decodedResponse the decoded http response
         * @param bodyAsObject the http response content
         * @return an instance of a {@link Response} implementation
         */
        @SuppressWarnings("unchecked")
        Mono<Response<?>> invoke(final HttpResponseDecoder.HttpDecodedResponse decodedResponse,
                                 final Object bodyAsObject) {
            final HttpResponse httpResponse = decodedResponse.getSourceResponse();
            final HttpRequest httpRequest = httpResponse.getRequest();
            final int responseStatusCode = httpResponse.getStatusCode();
            final HttpHeaders responseHeaders = httpResponse.getHeaders();
            switch (this.parameterCount) {
                case 3:
                    try {
                        return Mono.just((Response<?>) ((ResponseFunc3) this.responseFunc).apply(httpRequest,
                            responseStatusCode,
                            responseHeaders));
                    } catch (Throwable t) {
                        throw Exceptions.propagate(t);
                    }
                case 4:
                    try {
                        return Mono.just((Response<?>) ((ResponseFunc4) this.responseFunc).apply(httpRequest,
                            responseStatusCode,
                            responseHeaders,
                            bodyAsObject));
                    } catch (Throwable t) {
                        throw Exceptions.propagate(t);
                    }
                case 5:
                    return decodedResponse.getDecodedHeaders()
                        .map((Function<Object, Response<?>>) decodedHeaders -> {
                            try {
                                return (Response<?>) ((ResponseFunc5) this.responseFunc).apply(httpRequest,
                                    responseStatusCode,
                                    responseHeaders,
                                    bodyAsObject,
                                    decodedHeaders);
                            } catch (Throwable t) {
                                throw Exceptions.propagate(t);
                            }
                        })
                        .switchIfEmpty(Mono.defer((Supplier<Mono<Response<?>>>) () -> {
                            try {
                                return Mono.just((Response<?>) ((ResponseFunc5) this.responseFunc)
                                    .apply(httpRequest,
                                        responseStatusCode,
                                        responseHeaders,
                                        bodyAsObject,
                                        null));
                            } catch (Throwable t) {
                                throw Exceptions.propagate(t);
                            }
                        }));
                default:
                    return Mono.error(new IllegalStateException(
                        "Response constructor with expected parameters not found."));
            }
        }
    }

    @FunctionalInterface
    private interface ResponseFunc3 {
        MethodType SIGNATURE = MethodType.methodType(Object.class,
            HttpRequest.class,
            int.class,
            HttpHeaders.class);
        MethodType METHOD_TYPE = MethodType.methodType(ResponseFunc3.class);

        Object apply(HttpRequest httpRequest,
                     int responseStatusCode,
                     HttpHeaders responseHeaders);
    }

    @FunctionalInterface
    private interface ResponseFunc4 {
        MethodType SIGNATURE = MethodType.methodType(Object.class,
            HttpRequest.class,
            int.class,
            HttpHeaders.class,
            Object.class);
        MethodType METHOD_TYPE = MethodType.methodType(ResponseFunc4.class);

        Object apply(HttpRequest httpRequest,
                     int responseStatusCode,
                     HttpHeaders responseHeaders,
                     Object body);
    }

    @FunctionalInterface
    private interface ResponseFunc5 {
        MethodType SIGNATURE = MethodType.methodType(Object.class,
            HttpRequest.class,
            int.class,
            HttpHeaders.class,
            Object.class,
            Object.class);
        MethodType METHOD_TYPE = MethodType.methodType(ResponseFunc5.class);

        Object apply(HttpRequest httpRequest,
                     int responseStatusCode,
                     HttpHeaders responseHeaders,
                     Object body,
                     Object decodedHeaders);
    }
}
