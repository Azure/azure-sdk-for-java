// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.http.Response;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.implementation.http.serializer.HttpResponseDecoder;
import com.generic.core.models.Headers;
import com.generic.core.util.logging.ClientLogger;

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

            if (paramCount >= 3 && paramCount <= 4) {
                try {
                    return (Constructor<? extends Response<?>>) constructor;
                } catch (Throwable t) {
                    throw logger.logThrowableAsError(new RuntimeException(t));
                }
            }
        }

        return null;
    }

    Response<?> invoke(final Constructor<? extends Response<?>> constructor,
                       final HttpResponseDecoder.HttpDecodedResponse decodedResponse, final Object bodyAsObject) {
        final HttpResponse httpResponse = decodedResponse.getSourceResponse();
        final HttpRequest httpRequest = httpResponse.getRequest();
        final int responseStatusCode = httpResponse.getStatusCode();
        final Headers responseHeaders = httpResponse.getHeaders();
        final int paramCount = constructor.getParameterCount();

        try {
            switch (paramCount) {
                case 3:
                    return constructResponse(constructor, httpRequest, responseStatusCode, responseHeaders);
                case 4:
                    return constructResponse(constructor, httpRequest, responseStatusCode, responseHeaders,
                        bodyAsObject);
                default:
                    throw logger.logThrowableAsError(
                        new IllegalStateException("Response constructor with expected parameters not found."));
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw logger.logThrowableAsError(new RuntimeException(e));
        }
    }

    private static Response<?> constructResponse(Constructor<? extends Response<?>> constructor, Object... params)
        throws InvocationTargetException, InstantiationException, IllegalAccessException {

        return constructor.newInstance(params);
    }
}
