// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.rest.Response;
import com.azure.core.v2.implementation.serializer.HttpResponseDecoder;
import io.clientcore.core.util.ClientLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

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
                    throw logger.logThrowableAsError(new RuntimeException(t));
                }
            }
        }
        return null;
    }

    Response<?>>

    invoke(final Constructor<? extends Response<?>> constructor,
        final HttpResponseDecoder.HttpDecodedResponse decodedResponse, final Object bodyAsObject) {
        final HttpResponse httpResponse = decodedResponse.getSourceResponse();
        final HttpRequest httpRequest = httpResponse.getRequest();
        final int responseStatusCode = httpResponse.getStatusCode();
        final HttpHeaders responseHeaders = httpResponse.getHeaders();

        final int paramCount = constructor.getParameterCount();
        switch (paramCount) {
            case 3:
                return constructResponse(constructor, httpRequest, responseStatusCode, responseHeaders);

            case 4:
                return constructResponse(constructor, httpRequest, responseStatusCode, responseHeaders, bodyAsObject);

            case 5:
                return constructResponse(constructor, httpRequest, responseStatusCode, responseHeaders, bodyAsObject,
                    decodedResponse.getDecodedHeaders());

            default:
                throw logger.logThrowableAsError(
                    new IllegalStateException("Response constructor with expected parameters not found."));
        }
    }

    private static Response<?>> constructResponse(Constructor<? extends Response<?>> constructor,
        Object... params) {
        try {
            return constructor.newInstance(params));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            throw Exceptions.propagate(ex);
        }
    }
}
