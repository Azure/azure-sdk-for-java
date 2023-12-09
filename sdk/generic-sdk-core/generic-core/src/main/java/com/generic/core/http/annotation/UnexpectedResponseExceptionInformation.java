// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.annotation;

import com.generic.core.http.exception.HttpExceptionType;
import com.generic.core.http.exception.HttpResponseException;
import com.generic.core.implementation.http.annotation.UnexpectedResponseExceptionInformationArray;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The {@link HttpExceptionType} that is thrown or returned when one of the status codes is returned from a REST API. Multiple
 * annotations can be used. When no codes are listed that exception is always thrown or returned if it is reached
 * during evaluation, this should be treated as a default case. If no default case is annotated the fall through
 * exception is {@link HttpResponseException}.
 *
 * <p><strong>Example:</strong></p>
 *
 * <!-- src_embed com.generic.core.http.annotation.UnexpectedResponseExceptionType.class -->
 * <!-- end com.generic.core.http.annotation.UnexpectedResponseExceptionType.class -->
 */
@Retention(RUNTIME)
@Target(METHOD)
@Repeatable(UnexpectedResponseExceptionInformationArray.class)
public @interface UnexpectedResponseExceptionInformation {
    /**
     * The name of the {@link HttpExceptionType} of an {@link HttpResponseException} that should be thrown/returned when
     * the API returns an unrecognized status code.
     *
     * @return The {@link HttpExceptionType} that should be thrown/returned, represented as a {@link String}.
     */
    String exceptionTypeName() default "";

    /**
     * HTTP status codes which trigger the {@link HttpResponseException} to be thrown or returned. If no status codes
     * are listed the exception is always thrown or returned.
     *
     * @return The HTTP status codes that trigger the exception to be thrown or returned.
     */
    int[] statusCode() default {};

    /**
     * The class to decode the body of the HTTP response that accompanies an {@link HttpResponseException} to be thrown
     * or returned.
     *
     * @return The class to decode the body of the HTTP response that accompanies an {@link HttpResponseException} to be
     * thrown or returned.
     */
    Class<?> exceptionBodyClass() default Object.class;
}
