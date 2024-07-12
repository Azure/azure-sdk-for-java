// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for parameterized host name targeting a REST service.
 *
 * <p>
 * This is the 'host' field or 'x-ms-parameterized-host.hostTemplate' field in a Swagger document. parameters are
 * enclosed in {}s, e.g. {accountName}. An HTTP client must accept the parameterized host as the base URL for the
 * request, replacing the parameters during runtime with the actual values users provide.
 * </p>
 *
 * <p>
 * For parameterized hosts, parameters annotated with {@link HostParam} must be provided. See Java docs in
 * {@link HostParam} for directions for host parameters.
 * </p>
 *
 * <p>
 * The host's value must contain the scheme/protocol and the host. The host's value may contain the
 * port number.
 * </p>
 *
 * <p>
 * <strong>Example 1: Static annotation</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Host.class1 -->
 * <!-- end com.azure.core.annotation.Host.class1 -->
 *
 * <p>
 * <strong>Example 2: Dynamic annotation</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Host.class2 -->
 * <!-- end com.azure.core.annotation.Host.class2 -->
 */
// Record this annotation in the class file and make it available during runtime.
@Retention(RUNTIME)
@Target(TYPE)
public @interface Host {
    /**
     * Get the protocol/scheme, host, and optional port number in a single string.
     *
     * @return The protocol/scheme, host, and optional port number in a single string.
     */
    String value() default "";
}
