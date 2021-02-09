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

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A concurrent cache of {@link Response} {@link MethodHandle} constructors.
 */
final class ResponseConstructorsCache {
    private static final String THREE_PARAMETER_MESSAGE = "Failed to deserialize 3-parameter response.";
    private static final String FOUR_PARAMETER_MESSAGE = "Failed to deserialize 4-parameter response.";
    private static final String FIVE_PARAMETER_HEADERS_MESSAGE = "Failed to deserialize 5-parameter response with "
        + "decoded headers.";
    private static final String FIVE_PARAMETER_NO_HEADERS_MESSAGE = "Failed to deserialize 5-parameter response "
        + "without decoded headers.";
    private static final String INVALID_HANDLE_MESSAGE = "Response constructor with expected parameters not found.";

    // This lookup is specific to the com.azure.core module, specifically this class.
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // Convenience pointer to the com.azure.core module.
    private static final Module CORE_MODULE = ResponseConstructorsCache.class.getModule();

    private final ClientLogger logger = new ClientLogger(ResponseConstructorsCache.class);
    private final Map<Class<?>, MethodHandle> cache = new ConcurrentHashMap<>();

    /**
     * Identify the suitable {@link MethodHandle} to construct the given response class.
     *
     * @param responseClass The response class.
     * @return The {@link MethodHandle} that is capable of constructing an instance of the class or null if no handle is
     * found.
     */
    MethodHandle get(Class<? extends Response<?>> responseClass) {
        return this.cache.computeIfAbsent(responseClass, this::locateResponseConstructor);
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
        /*
         * If we were able to write this using Java 9+ code.
         *
         * First check if the response class's module is exported to all unnamed modules. If it is we will use
         * MethodHandles.publicLookup() which is meant for creating MethodHandle instances for publicly accessible
         * classes.
         */
        MethodHandles.Lookup lookupToUse;
        if (responseClass.getModule().isExported("")) {
            lookupToUse = MethodHandles.publicLookup();
        } else {
            /*
             * Otherwise we use the MethodHandles.Lookup which is associated to this (com.azure.core) module, and more
             * specifically, is tied to this class (ResponseConstructorsCache). But, in order to use this lookup we
             * need to ensure that the com.azure.core module reads the response class's module as the lookup won't
             * have permissions necessary to create the MethodHandle instance without it.
             */
            lookupToUse = LOOKUP;

            if (!CORE_MODULE.canRead(responseClass.getModule())) {
                CORE_MODULE.addReads(responseClass.getModule());
            }
        }

        /*
         * Now that we have the MethodHandles.Lookup to create our method handle instance we will begin searching for
         * the most specific handle we can use the create the response class (as mentioned in the method Javadocs).
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
                try {
                    return Mono.just((Response<?>) handle.invoke(httpRequest, responseStatusCode, responseHeaders));
                } catch (Throwable e) {
                    return monoError(logger, new RuntimeException(THREE_PARAMETER_MESSAGE, e));
                }
            case 4:
                try {
                    return Mono.just((Response<?>) handle.invoke(httpRequest, responseStatusCode, responseHeaders,
                        bodyAsObject));
                } catch (Throwable e) {
                    return monoError(logger, new RuntimeException(FOUR_PARAMETER_MESSAGE, e));
                }
            case 5:
                return decodedResponse.getDecodedHeaders().<Response<?>>handle((decodedHeaders, sink) -> {
                    try {
                        sink.next((Response<?>) handle.invoke(httpRequest, responseStatusCode, responseHeaders,
                            bodyAsObject, decodedHeaders));
                        sink.complete();
                    } catch (Throwable e) {
                        sink.error(logger.logExceptionAsError(new RuntimeException(FIVE_PARAMETER_HEADERS_MESSAGE, e)));
                    }
                }).switchIfEmpty(Mono.defer(() -> {
                    try {
                        return Mono.just((Response<?>) handle.invoke(httpRequest, responseStatusCode, responseHeaders,
                            bodyAsObject, null));
                    } catch (Throwable e) {
                        return monoError(logger, new RuntimeException(FIVE_PARAMETER_NO_HEADERS_MESSAGE, e));
                    }
                }));
            default:
                return monoError(logger, new IllegalStateException(INVALID_HANDLE_MESSAGE));
        }
    }
}
