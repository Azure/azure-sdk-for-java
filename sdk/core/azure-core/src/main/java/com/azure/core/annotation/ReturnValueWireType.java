// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import com.azure.core.http.rest.Page;
import com.azure.core.implementation.DateTimeRfc1123;
import com.azure.core.implementation.UnixTime;
import com.azure.core.implementation.Base64Url;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for the type that will be used to deserialize the return value of a REST API response.
 * Supported values are:
 *
 * <ol>
 *     <li>{@link Base64Url}</li>
 *     <li>{@link DateTimeRfc1123}</li>
 *     <li>{@link UnixTime}</li>
 *     <li>{@link Page}</li>
 *     <li>{@link List List&lt;T&gt;} where {@code T} can be one of the four values above.</li>
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
