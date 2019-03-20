/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation.serializer;

import com.azure.common.http.rest.RestResponseBase;
import com.azure.common.annotations.HeaderCollection;
import com.azure.common.implementation.util.TypeUtil;
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
    Type returnType();

    /**
     * Get the type of the entity to be used to deserialize 'Matching' headers.
     *
     * The 'header entity' is optional and client can choose it when a strongly typed model is needed for headers.
     *
     * 'Matching' headers are the HTTP response headers those with:
     *   1. header names same as name of a properties in the 'header entity'.
     *   2. header names start with value of {@link HeaderCollection} annotation applied to the properties in the 'header entity'.
     *
     * @return headers entity type
     */
     default Type headersType() {
        Type token = this.returnType();
        Type headersType = null;

        if (TypeUtil.isTypeOrSubTypeOf(token, Mono.class)) {
            token = TypeUtil.getTypeArgument(token);
        }

        // Only the RestResponseBase class supports a custom header type. All other RestResponse subclasses do not.
        if (TypeUtil.isTypeOrSubTypeOf(token, RestResponseBase.class)) {
            headersType = TypeUtil.getTypeArguments(TypeUtil.getSuperType(token, RestResponseBase.class))[0];
        }

        return headersType;
    }

    /**
     * Get the expected HTTP response status codes.
     *
     * 1. If the returned int[] is null, then all 2XX status codes are considered as success code.
     * 2. If the returned int[] is not-null, only the codes in the array are considered as success code.
     *
     * @return the expected HTTP response status codes
     */
    default int[] expectedStatusCodes() {
         return null;
    }

    /**
     * Get the type of the 'entity' in HTTP response content.
     *
     * When this method return non-null {@code java.lang.reflect.Type} then the raw HTTP response
     * content will need to parsed to this {@code java.lang.reflect.Type} then converted to actual
     * {@code returnType}.
     *
     * @return the type that the raw HTTP response content will be sent as
     */
     default Type returnValueWireType() {
        return null;
    }

    /**
     * Get the type of error body Object to be used for deserializing body when HTTP response status
     * code is not one of the expected status codes.
     *
     * expected status codes are the codes returned by {@code expectedStatusCodes()},
     * when {@code expectedStatusCodes()} returns null then 2XX are considered as expected status codes
     *
     * @return the type of error body Object to be used for deserializing body when HTTP response
     * status code is not one of the expected status codes
     */
    default Class<?> exceptionBodyType() {
         return Object.class;
    }
}
