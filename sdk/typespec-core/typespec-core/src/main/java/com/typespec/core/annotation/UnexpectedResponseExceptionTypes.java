// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The {@link Repeatable} container annotation for {@link UnexpectedResponseExceptionType}. This allows methods to have
 * different exceptions to be thrown or returned based on the response status codes returned from a REST API.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface UnexpectedResponseExceptionTypes {
    /**
     * Gets an array of {@link UnexpectedResponseExceptionType} that annotate a method.
     *
     * @return array of {@link UnexpectedResponseExceptionType} that annotate a method.
     */
    UnexpectedResponseExceptionType[] value();
}
