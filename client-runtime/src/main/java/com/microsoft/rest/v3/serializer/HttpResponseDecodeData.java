/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.serializer;

import com.microsoft.rest.v3.RestResponse;
import com.microsoft.rest.v3.annotations.HeaderCollection;
import com.microsoft.rest.v3.http.HttpMethod;
import com.microsoft.rest.v3.util.TypeUtil;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;

/**
 * Type representing necessary data required to decode a specific Http response.
 */
public interface HttpResponseDecodeData {
    /**
     * Get the HTTP method that was used to retrieve the response.
     *
     * @return the HTTP method that was used to retrieve the response
     */
     HttpMethod httpMethod();

    /**
     * Get the return type.
     *
     * @return the return type
     */
    Type returnType();

    /**
     * Get the type of the entity to be used as the model to hold deserialized 'Matching' headers.
     *
     * The 'header entity' is optional and client can choose it when a strongly typed model is needed for headers.
     *
     * 'Matching' headers are the REST API returned headers those with:
     *      1. header names same as name of a properties in the entity.
     *      2. header names start with value of {@link HeaderCollection} annotation applied to the properties in the entity.
     *
     * @return headers entity type
     */
     default Type headersType() {
        Type token = this.returnType();
        Type headersType = null;
        //
        if (TypeUtil.isTypeOrSubTypeOf(token, Mono.class)) {
            token = TypeUtil.getTypeArgument(token);
        }
        if (TypeUtil.isTypeOrSubTypeOf(token, RestResponse.class)) {
            headersType = TypeUtil.getTypeArguments(TypeUtil.getSuperType(token, RestResponse.class))[0];
        }
        return headersType;
    }

    /**
     * Get the expected HTTP response status codes.
     *
     * If the returned int[] is null, then all status codes less than 400 are allowed.
     *
     * @return the expected HTTP response status codes,if null then all status codes less than 400 are allowed
     */
    default int[] expectedStatusCodes() {
         return null;
    }

    /**
     * Get the type of the 'entity' in HTTP response content.
     *
     * When this method return non-null {@code Type} then the raw HTTP response
     * content will need to parsed to this {@code Type} then converted to actual
     * {@code returnType}.
     *
     * @return the type that the raw HTTP response content will be sent as
     */
     default Type returnValueWireType() {
        return null;
    }

    /**
     * Get the type of body Object that a thrown {@link com.microsoft.rest.v3.RestException} will
     * contain if the HTTP response's status code is not one of the expected status codes.
     *
     * @return the type of body Object that a thrown RestException will contain if the HTTP
     * response's status code is not one of the expected status codes
     */
    default Class<?> exceptionBodyType() {
         return Object.class;
    }
}
