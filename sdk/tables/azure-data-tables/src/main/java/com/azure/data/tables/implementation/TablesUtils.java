// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;

public final class TablesUtils {
    private TablesUtils() {
        // TablesUtils should not be instantiated.
    }

    /**
     * Deserializes a given HTTP response including headers to a given class.
     *
     * @param statusCode The status code with which the exception will be swallowed.
     * @param httpResponseException Exception to be swallowed.
     * @param logger Logger that will be used to record the exception.
     * @param <E> Response exception class.
     * @return A Mono that contains the deserialized response.
     */
    public static <E extends HttpResponseException> Mono<Response<Void>> swallowExceptionForStatusCode(int statusCode,
        E httpResponseException, ClientLogger logger) {

        HttpResponse httpResponse = httpResponseException.getResponse();

        if (httpResponse.getStatusCode() == statusCode) {
            return Mono.just(new SimpleResponse<>(httpResponseException.getResponse().getRequest(),
                httpResponseException.getResponse().getStatusCode(),
                httpResponseException.getResponse().getHeaders(), null));
        }

        return monoError(logger, httpResponseException);
    }
}
