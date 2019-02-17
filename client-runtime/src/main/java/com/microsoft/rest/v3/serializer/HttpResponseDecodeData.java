/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.serializer;

import com.microsoft.rest.v3.http.HttpMethod;

import java.lang.reflect.Type;

/**
 * Type representing necessary data required to decode a Http response.
 */
public interface HttpResponseDecodeData {
    /**
     * Get the HTTP method that will be used to retrieve the response.
     *
     * @return the HTTP method that will be used to retrieve the response
     */
     HttpMethod httpMethod();

    /**
     * Get the expected HTTP response status codes.
     *
     * If the returned int[] is null, then all status codes less than 400 are allowed.
     *
     * @return the expected HTTP response status codes,if null then all status codes less than 400 are allowed
     */
     int[] expectedStatusCodes();

    /**
     * Get the type of the HTTP response content.
     *
     * When this method return non-null {@code Type} then the raw HTTP response
     * content will need to parsed to this {@code Type} then converted to actual
     * {@code returnType}.
     *
     * @return the type that the raw HTTP response content will be sent as
     */
    Type returnValueWireType();

    /**
     * Get the return type.
     *
     * @return the return type
     */
    Type returnType();

    /**
     * Get the type of body Object that a thrown {@link com.microsoft.rest.v3.RestException} will
     * contain if the HTTP response's status code is not one of the expected status codes.
     *
     * @return the type of body Object that a thrown RestException will contain if the HTTP
     * response's status code is not one of the expected status codes
     */
     Class<?> exceptionBodyType();
}
