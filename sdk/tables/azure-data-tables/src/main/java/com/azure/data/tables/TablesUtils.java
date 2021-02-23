// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;

import static com.azure.core.util.FluxUtil.monoError;

final class TablesUtils {
    private TablesUtils() {
        throw new UnsupportedOperationException("TablesUtils should not be instantiated.");
    }

    /**
     * Deserializes a given HTTP response including headers to a given class.
     *
     * @param statusCode The status code with which the exception will be swallowed.
     * @param httpResponseException Exception to be swallowed.
     * @param responseClass Class of the response object to deserialize from the HTTP response.
     * @param headersClass Class of the headers object to deserialize from the HTTP response.
     * @param serializerAdapter Serializer adapter to deserialize the given HTTP response.
     * @param logger Logger that will be used to record the exception.
     * @param <E> Response exception class.
     * @param <R> Deserialized response class.
     * @param <H> Deserialized headers class.
     * @return A Mono that contains the deserialized response.
     */
    static <E extends HttpResponseException, R extends ResponseBase<H, Void>, H> Mono<R> swallowExceptionForStatusCode(
        int statusCode, E httpResponseException, Class<R> responseClass, Class<H> headersClass,
        SerializerAdapter serializerAdapter, ClientLogger logger) {

        HttpResponse httpResponse = httpResponseException.getResponse();

        if (httpResponse.getStatusCode() == statusCode) {
            H deserializedHeaders;

            try {
                deserializedHeaders = serializerAdapter.deserialize(httpResponse.getHeaders(),
                    headersClass);
            } catch (IOException ioException) {
                return monoError(logger, new UncheckedIOException(ioException));
            }

            try {
                Constructor<R> constructor = responseClass.getConstructor(HttpRequest.class, int.class, HttpHeaders.class, Void.class,
                    headersClass);

                R response = constructor.newInstance(httpResponse.getRequest(),
                    httpResponse.getStatusCode(), httpResponse.getHeaders(), null, deserializedHeaders);

                return Mono.just(response);
            } catch (Exception e) {
                return monoError(logger, httpResponseException);
            }
        }

        return monoError(logger, httpResponseException);
    }
}
