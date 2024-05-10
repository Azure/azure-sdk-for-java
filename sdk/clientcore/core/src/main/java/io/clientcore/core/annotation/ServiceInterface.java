// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.annotation;

import io.clientcore.core.http.annotation.HostParam;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to give the service interfaces a name that correlates to the service that is usable in a programmatic
 * way.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ServiceInterface {
    /**
     * Name of the service - this must be short and without spaces.
     *
     * @return The service name given to the interface.
     */
    String name();

    /**
     * Property that denotes a parameterized host name targeting a REST service.
     *
     * <p>This is the 'host' field in a Swagger document. Parameters are enclosed in {}s, e.g. {accountName}. An HTTP
     * client must accept the parameterized host as the base URL for the request, replacing the parameters during
     * runtime with the actual values users provide.</p>
     *
     * <p>For parameterized hosts, parameters annotated with {@link HostParam} must be provided. See Javadocs in
     * {@link HostParam} for directions for host parameters.</p>
     *
     * <p>The host's value must contain the scheme/protocol and the host. The host's value may contain the
     * port number.</p>
     *
     * @return The protocol/scheme, host, and optional port number in a single string.
     */

    String host() default "";
}
