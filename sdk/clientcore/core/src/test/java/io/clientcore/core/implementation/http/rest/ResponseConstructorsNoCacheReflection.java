// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
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

    Response<?> invoke(final Constructor<? extends Response<?>> constructor, final Response<?> response,
                       final Object bodyAsObject) {
        final HttpRequest httpRequest = response.getRequest();
        final int responseStatusCode = response.getStatusCode();
        final HttpHeaders responseHeaders = response.getHeaders();
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
