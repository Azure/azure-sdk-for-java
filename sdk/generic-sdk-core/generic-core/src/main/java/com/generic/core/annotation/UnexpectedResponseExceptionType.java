// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.annotation;

import com.generic.core.exception.HttpResponseException;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The exception type that is thrown or returned when one of the status codes is returned from a REST API. Multiple
 * annotations can be used. When no codes are listed that exception is always thrown or returned if it is reached
 * during evaluation, this should be treated as a default case. If no default case is annotated the fall through
 * exception is {@link HttpResponseException}.
 *
 * <p><strong>Example:</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.UnexpectedResponseExceptionType.class -->
 * <!-- end com.generic.core.annotation.UnexpectedResponseExceptionType.class -->
 */
@Retention(RUNTIME)
@Target(METHOD)
@Repeatable(UnexpectedResponseExceptionTypes.class)
public @interface UnexpectedResponseExceptionType {
    /**
     * The type of HttpResponseException that should be thrown/returned when the API returns an unrecognized
     * status code.
     * @return The type of RestException that should be thrown/returned.
     */
    Class<? extends HttpResponseException> value();

    /**
     * HTTP status codes which trigger the exception to be thrown or returned, if not status codes are listed the
     * exception is always thrown or returned.
     * @return The HTTP status codes that trigger the exception to be thrown or returned.
     */
    int[] code() default {};
}
