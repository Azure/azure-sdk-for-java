// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation given to all service client builder classes.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ServiceClientBuilder {

    /**
     * An array of classes that this builder can build.
     *
     * @return An array of all classnames that this builder can create an instance of.
     */
    Class<?>[] serviceClients();

    /**
     * The {@link ServiceClientProtocol protocol} clients created from this builder will use to interact with the
     * service.
     *
     * @return The {@link ServiceClientProtocol}.
     */
    ServiceClientProtocol protocol() default ServiceClientProtocol.HTTP;
}
