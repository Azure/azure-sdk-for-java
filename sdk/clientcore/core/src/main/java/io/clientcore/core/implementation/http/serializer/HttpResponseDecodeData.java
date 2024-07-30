// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.serializer;

import io.clientcore.core.http.exception.HttpResponseException;
import io.clientcore.core.implementation.http.UnexpectedExceptionInformation;
import io.clientcore.core.implementation.http.rest.SwaggerMethodParser;

import java.lang.reflect.Type;

/**
 * Type representing necessary information required to decode a specific Http response.
 */
public interface HttpResponseDecodeData {
    /**
     * Gets the generic return type of the response.
     *
     * @return The generic return type.
     */
    Type getReturnType();

    /**
     * Get the type of the entity to be used to deserialize 'Matching' headers.
     * <p>
     * The 'header entity' is optional and client can choose it when a strongly typed model is needed for headers.
     * <p>
     * 'Matching' headers are the HTTP response headers those with:
     * 1. header names same as name of a properties in the 'header entity'.
     * entity'.
     *
     * @return headers entity type
     */
    default Type getHeadersType() {
        Type token = this.getReturnType();
        Type headersType = null;


        return headersType;
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
     * <p>
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
     * Get the {@link UnexpectedExceptionInformation} that will be used to generate an {@link HttpResponseException} if
     * the HTTP response status code is not one of the expected status codes.
     *
     * @param code Exception HTTP status code return from a REST API.
     *
     * @return The {@link UnexpectedExceptionInformation} to generate an exception to throw or return.
     */
    default UnexpectedExceptionInformation getUnexpectedException(int code) {
        return new UnexpectedExceptionInformation(null, null);
    }

    /**
     * Whether the {@link #getReturnType() returnType} is a decodable type.
     * <p>
     * Types that aren't decodable are the following (including sub-types):
     * <ul>
     * <li>BinaryData</li>
     * <li>byte[]</li>
     * <li>ByteBuffer</li>
     * <li>InputStream</li>
     * <li>Void</li>
     * <li>void</li>
     * </ul>
     *
     * cracked open and their generic types are inspected for being one of the types above.
     *
     * @return Whether the return type is decodable.
     */
    default boolean isReturnTypeDecodable() {
        return SwaggerMethodParser.isReturnTypeDecodable(SwaggerMethodParser.unwrapReturnType(getReturnType()));
    }

    /**
     * Whether the return type contains strongly-typed headers.
     * <p>
     * If the response contains strongly-typed headers this is an indication to the HttpClient that the headers should
     * be eagerly converted from the header format used by the HttpClient implementation to Azure Core HttpHeaders.
     *
     * @return Whether the return type contains strongly-typed headers.
     */
    boolean isHeadersEagerlyConverted();
}
