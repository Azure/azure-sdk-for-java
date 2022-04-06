// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.http.UnexpectedExceptionInformation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;

/**
 * Type representing necessary information required to decode a specific Http response.
 */
public interface HttpResponseDecodeData {
    /**
     * Get the type of the entity to deserialize the body.
     *
     * @return the return type
     */
    Type getReturnType();

    /**
     * Get the type that {@link HttpHeaders} will be deserialized to when returned.
     * <p>
     * {@code returnType} isn't required to have a headers type, in that case the {@link HttpHeaders} won't be
     * deserialized.
     * <p>
     * If the return type is a {@link Mono} its generic type will be inspected. Only return types that are or are a
     * subtype of {@link ResponseBase} will have a headers type.
     *
     * @return The {@code returnType} headers type if set, otherwise null.
     */
    default Type getHeadersType() {
        return TypeUtil.getHeadersType(getReturnType());
    }

    /**
     * This method returns {@code true} if the given {@code statusCode} is in the list of expected HTTP response
     * codes, {@code false} otherwise.
     *
     * @param statusCode The HTTP response status code to evaluate.
     * @return {@code true} if the given status code is expected.
     */
    boolean isExpectedResponseStatusCode(int statusCode);

    /**
     * Get the type of the 'entity' in HTTP response content.
     *
     * When this method return non-null {@code java.lang.reflect.Type} then the raw HTTP response
     * content will need to parsed to this {@code java.lang.reflect.Type} then converted to actual
     * {@code returnType}.
     *
     * @return the type that the raw HTTP response content will be sent as
     */
    default Type getReturnValueWireType() {
        return null;
    }

    /**
     * Get the {@link UnexpectedExceptionInformation} that will be used to generate a RestException if the HTTP response
     * status code is not one of the expected status codes.
     *
     * @param code Exception HTTP status code return from a REST API.
     * @return the UnexpectedExceptionInformation to generate an exception to throw or return.
     */
    default UnexpectedExceptionInformation getUnexpectedException(int code) {
        return new UnexpectedExceptionInformation(HttpResponseException.class);
    }

    /**
     * Checks if the {@link #getReturnType() return type} is a decode-able type.
     * <p>
     * Types that aren't decode-able are the following (including sub-types):
     * <ul>
     * <li>BinaryData</li>
     * <li>byte[]</li>
     * <li>ByteBuffer</li>
     * <li>InputStream</li>
     * <li>Void</li>
     * <li>void</li>
     * </ul>
     *
     * Reactive, {@link Mono} and {@link Flux}, and Response, {@link Response} and {@link ResponseBase}, generics are
     * cracked open and their generic types are inspected for being one of the types above.
     *
     * @return Flag indicating if the return type is decode-able.
     */
    default boolean isReturnTypeDecodable() {
        return TypeUtil.isReturnTypeDecodable(getReturnType());
    }

    /**
     * Gets the return entity type from the {@link #getReturnType() return type}.
     * <p>
     * The entity type is the {@link #getReturnType() return type} itself if it isn't {@code Mono<T>} or
     * {@code Response<T>}. Otherwise, if the return type is {@code Mono<T>} the {@code T} type will be extracted,
     * then if {@code T} is {@code Response<S>} the {@code S} type will be extracted and returned or if it isn't a
     * {@code Response<S>} {@code T} will be returned.
     *
     * @return The return type entity type.
     */
    default Type getReturnEntityType() {
        return TypeUtil.getReturnEntityType(getReturnType());
    }
}
