// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import com.azure.core.v2.http.rest.Page;
import io.clientcore.core.implementation.util.Base64Uri;
import io.clientcore.core.implementation.util.DateTimeRfc1123;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for the type that will be used to deserialize the return value of a REST API response. Supported values
 * are:
 *
 * <ol>
 * <li>{@link Base64Uri}</li>
 * <li>{@link DateTimeRfc1123}</li>
 * <li>{@link Page}</li>
 * <li>{@link List List&lt;T&gt;} where {@code T} can be one of the four values above.</li>
 * </ol>
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
