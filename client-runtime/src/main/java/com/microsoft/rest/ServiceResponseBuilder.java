/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.microsoft.rest.protocol.ResponseBuilder;
import com.microsoft.rest.protocol.SerializerAdapter;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * The builder for building a {@link ServiceResponse}.
 *
 * @param <T> The return type the caller expects from the REST response.
 * @param <E> the exception to throw in case of error.
 */
public final class ServiceResponseBuilder<T, E extends RestException> implements ResponseBuilder<T, E> {
    /**
     * A mapping of HTTP status codes and their corresponding return types.
     */
    private final Map<Integer, Type> responseTypes;

    /**
     * The exception type to thrown in case of error.
     */
    private Class<? extends RestException> exceptionType;

    /**
     * The mapperAdapter used for deserializing the response.
     */
    private final SerializerAdapter<?> serializerAdapter;

    private boolean throwOnGet404;

    /**
     * Create a ServiceResponseBuilder instance.
     *
     * @param serializerAdapter the serialization utils to use for deserialization operations
     */
    private ServiceResponseBuilder(SerializerAdapter<?> serializerAdapter) {
        this.serializerAdapter = serializerAdapter;
        this.responseTypes = new HashMap<>();
        this.exceptionType = RestException.class;
        this.responseTypes.put(0, Object.class);
        this.throwOnGet404 = false;
    }

    @Override
    public ServiceResponseBuilder<T, E> register(int statusCode, final Type type) {
        this.responseTypes.put(statusCode, type);
        return this;
    }


    @Override
    public ServiceResponseBuilder<T, E> registerError(final Class<? extends RestException> type) {
        this.exceptionType = type;
        try {
            Method f = type.getDeclaredMethod("body");
            this.responseTypes.put(0, f.getReturnType());
        } catch (NoSuchMethodException e) {
            // AutoRestException always has a body. Register Object as a fallback plan.
            this.responseTypes.put(0, Object.class);
        }
        return this;
    }

    /**
     * Register all the mappings from a response status code to a response
     * destination type stored in a {@link Map}.
     *
     * @param responseTypes the mapping from response status codes to response types.
     * @return the same builder instance.
     */
    public ServiceResponseBuilder<T, E> registerAll(Map<Integer, Type> responseTypes) {
        this.responseTypes.putAll(responseTypes);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceResponse<T> build(Response<ResponseBody> response) throws IOException {
        if (response == null) {
            return null;
        }

        int statusCode = response.code();
        ResponseBody responseBody;
        if (response.isSuccessful()) {
            responseBody = response.body();
        } else {
            responseBody = response.errorBody();
        }

        if (responseTypes.containsKey(statusCode)) {
            return new ServiceResponse<>((T) buildBody(statusCode, responseBody), response);
        } else if (response.isSuccessful() && responseTypes.size() == 1) {
            return new ServiceResponse<>((T) buildBody(statusCode, responseBody), response);
        } else if (!throwOnGet404 && "GET".equals(response.raw().request().method()) && statusCode == 404) {
            return new ServiceResponse<>(null, response);
        } else {
            try {
                String responseContent = "";
                if (responseBody != null) {
                    responseContent = responseBody.source().buffer().clone().readUtf8();
                }
                throw exceptionType.getConstructor(String.class, Response.class, (Class<?>) responseTypes.get(0))
                        .newInstance("Status code " + statusCode + ", " + responseContent, response, buildBody(statusCode, responseBody));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IOException("Status code " + statusCode + ", but an instance of " + exceptionType.getCanonicalName()
                    + " cannot be created.", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceResponse<T> buildEmpty(Response<Void> response) throws IOException {
        int statusCode = response.code();
        if (responseTypes.containsKey(statusCode)) {
            return new ServiceResponse<>(response);
        } else if (response.isSuccessful() && responseTypes.size() == 1) {
            return new ServiceResponse<>(response);
        } else {
            try {
                throw exceptionType.getConstructor(String.class, Response.class)
                        .newInstance("Status code " + statusCode, response);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IOException("Status code " + statusCode + ", but an instance of " + exceptionType.getCanonicalName()
                        + " cannot be created.", e);
            }
        }
    }

    @Override
    public <THeader> ServiceResponseWithHeaders<T, THeader> buildWithHeaders(final Response<ResponseBody> response, Class<THeader> headerType) throws IOException {
        ServiceResponse<T> bodyResponse = build(response);
        THeader headers = serializerAdapter.deserialize(
                serializerAdapter.serialize(Maps.asMap(response.headers().names(), new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return response.headers().get(s);
                    }
                })),
                headerType);
        return new ServiceResponseWithHeaders<>(bodyResponse.body(), headers, bodyResponse.response());
    }

    @Override
    public <THeader> ServiceResponseWithHeaders<T, THeader> buildEmptyWithHeaders(final Response<Void> response, Class<THeader> headerType) throws IOException {
        ServiceResponse<T> bodyResponse = buildEmpty(response);
        THeader headers = serializerAdapter.deserialize(
                serializerAdapter.serialize(Maps.asMap(response.headers().names(), new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return response.headers().get(s);
                    }
                })),
                headerType);
        ServiceResponseWithHeaders<T, THeader> serviceResponse = new ServiceResponseWithHeaders<>(headers, bodyResponse.headResponse());
        serviceResponse.withBody(bodyResponse.body());
        return serviceResponse;
    }

    /**
     * Builds the body object from the HTTP status code and returned response
     * body undeserialized and wrapped in {@link ResponseBody}.
     *
     * @param statusCode the HTTP status code
     * @param responseBody the response body
     * @return the response body, deserialized
     * @throws IOException thrown for any deserialization errors
     */
    private Object buildBody(int statusCode, ResponseBody responseBody) throws IOException {
        if (responseBody == null) {
            return null;
        }

        Type type;
        if (responseTypes.containsKey(statusCode)) {
            type = responseTypes.get(statusCode);
        } else if (responseTypes.get(0) != Object.class) {
            type = responseTypes.get(0);
        } else {
            type = new TypeReference<T>() { }.getType();
        }

        // Void response
        if (type == Void.class) {
            return null;
        }
        // Return raw response if InputStream is the target type
        else if (type == InputStream.class) {
            return responseBody.byteStream();
        }
        // Deserialize
        else {
            String responseContent = responseBody.source().buffer().readUtf8();
            if (responseContent.length() <= 0) {
                return null;
            }
            return serializerAdapter.deserialize(responseContent, type);
        }
    }

    /**
     * @return the exception type to thrown in case of error.
     */
    public Class<? extends RestException> exceptionType() {
        return exceptionType;
    }

    /**
     * Check if the returned status code will be considered a success for
     * this builder.
     *
     * @param statusCode the status code to check
     * @return true if it's a success, false otherwise.
     */
    public boolean isSuccessful(int statusCode) {
        return responseTypes != null && responseTypes.containsKey(statusCode);
    }

    /**
     * Specifies whether to throw on 404 responses from a GET call.
     * @param throwOnGet404 true if to throw; false to simply return null. Default is false.
     * @return the response builder itself
     */
    public ServiceResponseBuilder withThrowOnGet404(boolean throwOnGet404) {
        this.throwOnGet404 = throwOnGet404;
        return this;
    }

    /**
     * A factory to create a service response builder.
     */
    public static final class Factory implements ResponseBuilder.Factory {
        @Override
        public <T, E extends RestException> ServiceResponseBuilder<T, E> newInstance(final SerializerAdapter<?> serializerAdapter) {
            return new ServiceResponseBuilder<T, E>(serializerAdapter);
        }
    }
}
