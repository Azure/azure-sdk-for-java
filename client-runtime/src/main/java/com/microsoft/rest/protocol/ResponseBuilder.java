/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.protocol;

import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponse;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Defines an interface that can process a Retrofit 2 response
 * into a deserialized body or an exception, depending on the
 * status code registered.
 *
 * @param <T> the body type if the status code is considered successful
 * @param <E> the exception type if the status code is considered a failure
 */
public interface ResponseBuilder<T, E extends RestException> {
    /**
     * Register a mapping from a response status code to a response destination type.
     *
     * @param statusCode the status code.
     * @param type the type to deserialize.
     * @return the same builder instance.
     */
    ResponseBuilder<T, E> register(int statusCode, final Type type);

    /**
     * Register a destination type for errors with models.
     *
     * @param type the type to deserialize.
     * @return the same builder instance.
     */
    ResponseBuilder<T, E> registerError(final Class<? extends RestException> type);

    /**
     * Build a ServiceResponse instance from a REST call response and a
     * possible error.
     *
     * <p>
     *     If the status code in the response is registered, the response will
     *     be considered valid and deserialized into the specified destination
     *     type. If the status code is not registered, the response will be
     *     considered invalid and deserialized into the specified error type if
     *     there is one. An AutoRestException is also thrown.
     * </p>
     *
     * @param response the {@link Response} instance from REST call
     * @return a ServiceResponse instance of generic type {@link T}
     * @throws E exceptions from the REST call
     * @throws IOException exceptions from deserialization
     */
    ServiceResponse<T> build(Response<ResponseBody> response) throws IOException;

    /**
     * Build a ServiceResponse instance from a REST call response and a
     * possible error, which does not have a response body.
     *
     * <p>
     *     If the status code in the response is registered, the response will
     *     be considered valid. If the status code is not registered, the
     *     response will be considered invalid. An AutoRestException is also thrown.
     * </p>
     *
     * @param response the {@link Response} instance from REST call
     * @return a ServiceResponse instance of generic type {@link T}
     * @throws E exceptions from the REST call
     * @throws IOException exceptions from deserialization
     */
    ServiceResponse<T> buildEmpty(Response<Void> response) throws IOException;

    /**
     * A factory that creates a builder based on the return type and the exception type.
     */
    interface Factory {
        /**
         * Returns a response builder instance. This can be created new or cached.
         *
         * @param <T> the type of the return object
         * @param <E> the type of the exception
         * @return a response builder instance
         */
        <T, E extends RestException> ResponseBuilder<T, E> newInstance(final SerializerAdapter<?> serializerAdapter);
    }
}
