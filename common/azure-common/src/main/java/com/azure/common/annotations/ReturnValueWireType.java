// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for the type that will be used to deserialize the return value of a REST API response.
 * Supported values are:
 * 1. {@link com.azure.common.implementation.Base64Url}
 * 2. {@link com.azure.common.implementation.DateTimeRfc1123}
 * 3. {@link com.azure.common.implementation.UnixTime}
 * 4. {@link com.azure.common.http.rest.Page}
 * 5. {@link java.util.List List<T>} where T can be one of the four values above.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ReturnValueWireType {
    /**
     * The type that the service interface method's return value will be converted from.
     *
     * @return The type that the service interface method's return value will be converted from.
     */
    Class<?> value();
}
