/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.google.common.reflect.TypeToken;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseBuilder;
import com.microsoft.rest.ServiceResponseWithHeaders;
import com.microsoft.rest.protocol.ResponseBuilder;
import com.microsoft.rest.protocol.SerializerAdapter;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * The builder for building a {@link ServiceResponse} customized for Azure.
 *
 * @param <T> the return type from caller.
 * @param <E> the exception to throw in case of error.
 */
public final class AzureResponseBuilder<T, E extends RestException> implements ResponseBuilder<T, E> {
    /** The base response builder for handling most scenarios. */
    private ServiceResponseBuilder<T, E> baseBuilder;

    /**
     * Create a ServiceResponseBuilder instance.
     *
     * @param serializer the serialization utils to use for deserialization operations
     */
    private AzureResponseBuilder(SerializerAdapter<?> serializer) {
        baseBuilder = new ServiceResponseBuilder.Factory().newInstance(serializer);
    }

    @Override
    public ResponseBuilder<T, E> register(int statusCode, Type type) {
        baseBuilder.register(statusCode, type);
        return this;
    }

    @Override
    public ResponseBuilder<T, E> registerError(Class<? extends RestException> type) {
        baseBuilder.registerError(type);
        return this;
    }

    @Override
    public ServiceResponse<T> build(Response<ResponseBody> response) throws IOException {
        return baseBuilder.build(response);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceResponse<T> buildEmpty(Response<Void> response) throws E, IOException {
        int statusCode = response.code();
        if (baseBuilder.isSuccessful(statusCode)) {
            if (new TypeToken<T>(getClass()) { }.getRawType().isAssignableFrom(Boolean.class)) {
                return new ServiceResponse<T>(response).withBody((T) (Object) (statusCode / 100 == 2));
            } else {
                return new ServiceResponse<>(response);
            }
        } else {
            try {
                throw baseBuilder.exceptionType().getConstructor(String.class, Response.class)
                        .newInstance("Status code " + statusCode, response);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IOException("Invalid status code " + statusCode + ", but an instance of " + baseBuilder.exceptionType().getCanonicalName()
                        + " cannot be created.", e);
            }
        }
    }

    @Override
    public <THeader> ServiceResponseWithHeaders<T, THeader> buildWithHeaders(Response<ResponseBody> response, Class<THeader> headerType) throws IOException {
        return baseBuilder.buildWithHeaders(response, headerType);
    }

    @Override
    public <THeader> ServiceResponseWithHeaders<T, THeader> buildEmptyWithHeaders(Response<Void> response, Class<THeader> headerType) throws IOException {
        ServiceResponse<T> bodyResponse = buildEmpty(response);
        ServiceResponseWithHeaders<T, THeader> baseResponse = baseBuilder.buildEmptyWithHeaders(response, headerType);
        ServiceResponseWithHeaders<T, THeader> serviceResponse = new ServiceResponseWithHeaders<>(baseResponse.headers(), bodyResponse.headResponse());
        serviceResponse.withBody(bodyResponse.body());
        return serviceResponse;
    }

    /**
     * Specifies whether to throw on 404 responses from a GET call.
     * @param throwOnGet404 true if to throw; false to simply return null. Default is false.
     * @return the response builder itself
     */
    public AzureResponseBuilder<T, E> withThrowOnGet404(boolean throwOnGet404) {
        baseBuilder.withThrowOnGet404(throwOnGet404);
        return this;
    }

    /**
     * A factory to create an Azure response builder.
     */
    public static final class Factory implements ResponseBuilder.Factory {
        @Override
        public <T, E extends RestException> AzureResponseBuilder<T, E> newInstance(final SerializerAdapter<?> serializerAdapter) {
            return new AzureResponseBuilder<T, E>(serializerAdapter);
        }
    }
}
