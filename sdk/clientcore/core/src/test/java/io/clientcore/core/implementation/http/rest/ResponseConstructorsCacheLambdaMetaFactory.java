// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.ClientLogger;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
     *
     * @return identified constructor, null if there is no match
     */
    ResponseConstructor get(Class<? extends Response<?>> responseClass) {
        return this.cache.computeIfAbsent(responseClass, this::locateResponseConstructor);
    }

    /**
     * Identify the most specific constructor for the given response class.
     * <p>
     * The most specific constructor is looked up following order: 1. (httpRequest, statusCode, headers, body,
     * decodedHeaders) 2. (httpRequest, statusCode, headers, body) 3. (httpRequest, statusCode, headers)
     * <p>
     * Developer Note: This method logic can be easily replaced with Java.Stream and associated operators but we're
     * using basic sort and loop constructs here as this method is in hot path and Stream route is consuming a fair
     * amount of resources.
     *
     * @param responseClass the response class
     *
     * @return identified constructor, null if there is no match
     */
    private ResponseConstructor locateResponseConstructor(Class<?> responseClass) {
        Constructor<?>[] constructors = responseClass.getDeclaredConstructors();
        // Sort constructors in the "descending order" of parameter count.
        Arrays.sort(constructors, Comparator.comparing(Constructor::getParameterCount, (a, b) -> b - a));
        for (Constructor<?> constructor : constructors) {
            final int paramCount = constructor.getParameterCount();

            if (paramCount >= 3 && paramCount <= 4) {
                try {
                    if (paramCount == 3) {
                        MethodHandle ctrMethodHandle = LOOKUP.unreflectConstructor(constructor);
                        return new ResponseConstructor(3, LambdaMetafactory.metafactory(LOOKUP, "apply",
                                ResponseFunc3.METHOD_TYPE, ResponseFunc3.SIGNATURE, ctrMethodHandle, ctrMethodHandle.type())
                            .getTarget());
                    } else {
                        MethodHandle ctrMethodHandle = LOOKUP.unreflectConstructor(constructor);

                        return new ResponseConstructor(4, LambdaMetafactory.metafactory(LOOKUP, "apply",
                                ResponseFunc4.METHOD_TYPE, ResponseFunc4.SIGNATURE, ctrMethodHandle, ctrMethodHandle.type())
                            .getTarget());
                    }
                } catch (Throwable t) {
                    throw logger.logThrowableAsError(new RuntimeException(t));
                }
            }
        }

        return null;
    }

    /**
     * Type that represent a {@link Response} constructor and can be used to invoke the same constructor.
     */
    final class ResponseConstructor {
        private final int parameterCount;
        private final MethodHandle responseFunc;

        /**
         * Creates ResponseConstructor.
         *
         * @param parameterCount the constructor parameter count
         * @param responseFunc the functional interface which delegate its abstract method invocation to the invocation
         * of a {@link Response} constructor
         */
        private ResponseConstructor(int parameterCount, MethodHandle responseFunc) {
            this.parameterCount = parameterCount;
            this.responseFunc = responseFunc;
        }

        /**
         * Invoke the {@link Response} constructor this type represents.
         *
         * @param response the decoded http response
         * @param bodyAsObject the http response content
         *
         * @return an instance of a {@link Response} implementation
         */
        Response<?> invoke(final Response<?> response, final Object bodyAsObject) {
            final HttpRequest httpRequest = response.getRequest();
            final int responseStatusCode = response.getStatusCode();
            final HttpHeaders responseHeaders = response.getHeaders();

            try {
                switch (this.parameterCount) {
                    case 3:
                        return callMethodHandle(responseFunc, httpRequest, responseStatusCode, responseHeaders);
                    case 4:
                        return callMethodHandle(responseFunc, httpRequest, responseStatusCode, responseHeaders,
                            bodyAsObject);
                    default:
                        throw logger.logThrowableAsError(
                            new IllegalStateException("Response constructor with expected parameters not found."));
                }
            } catch (Throwable t) {
                throw logger.logThrowableAsError(new RuntimeException(t));
            }
        }
    }

    private static Response<?> callMethodHandle(MethodHandle methodHandle, Object... params) throws Throwable {
        return (Response<?>) methodHandle.invoke(params);
    }

    @FunctionalInterface
    private interface ResponseFunc3 {
        MethodType SIGNATURE = MethodType.methodType(Object.class, HttpRequest.class, int.class, HttpHeaders.class);
        MethodType METHOD_TYPE = MethodType.methodType(ResponseFunc3.class);

        Object apply(HttpRequest httpRequest, int responseStatusCode, HttpHeaders responseHeaders);
    }

    @FunctionalInterface
    private interface ResponseFunc4 {
        MethodType SIGNATURE = MethodType.methodType(Object.class, HttpRequest.class, int.class, HttpHeaders.class,
            Object.class);
        MethodType METHOD_TYPE = MethodType.methodType(ResponseFunc4.class);

        Object apply(HttpRequest httpRequest, int responseStatusCode, HttpHeaders responseHeaders, Object body);
    }
}
