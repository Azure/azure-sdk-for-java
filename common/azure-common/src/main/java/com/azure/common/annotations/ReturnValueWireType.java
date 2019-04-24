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
 *
 * <ol>
 *     <li>{@link com.azure.common.implementation.Base64Url}</li>
 *     <li>{@link com.azure.common.implementation.DateTimeRfc1123}</li>
 *     <li>{@link com.azure.common.implementation.UnixTime}</li>
 *     <li>{@link com.azure.common.http.rest.Page}</li>
 *     <li>{@link java.util.List List&lt;T&gt;} where {@code T} can be one of the four values above.</li>
 * </ol>
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ReturnValueWireType {
    /**
     * The type that the service interface method's return value will be converted from.
     * @return The type that the service interface method's return value will be converted from.
     */
    Class<?> value();
}
