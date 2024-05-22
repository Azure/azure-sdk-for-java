// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.annotation;

import io.clientcore.core.http.models.HttpMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * Annotation describing the relative path to a REST endpoint, the method to use when making requests to it and other
 * request-related information.
 *
 * <p>The path value can be either a relative path or an absolute path. When it's an absolute path, it must start
 * with a protocol or a parameterized segment (otherwise the parse cannot tell if it's absolute or relative).</p>
 *
 * <!-- TODO (vcolin7): Add samples. -->
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface HttpRequestInformation {
    /**
     * Get the HTTP method to use for the request.
     *
     * @return The HTTP method to use for the request.
     */
    HttpMethod method();
    /**
     * Get the relative path the request should point to.
     *
     * @return The relative path the request should point to.
     */
    String path() default "";

    /**
     * Get the list of static headers to send with the request.
     *
     * @return The list of static headers to send with the request.
     */
    String[] headers() default {};

    /**
     * Get expected the status code(s) to receive with a response.
     *
     * @return The expected status code(s) to receive with a response.
     */
    int[] expectedStatusCodes() default {};

    /**
     * Get the type of the response body sent over the wire.
     *
     * @return The type of the response body sent over the wire.
     */
    Class<?> returnValueWireType() default Void.class;

    /**
     * Get the query parameters to be appended to the request URL. Elements in the array shall be written in the format
     * of 'key=value'.
     *
     * @return The query parameters to be appended to the request URL.
     */
    String[] queryParams() default {};
}
