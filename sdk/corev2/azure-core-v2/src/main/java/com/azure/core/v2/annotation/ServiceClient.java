// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Annotation given to all service client classes.
 */
@Retention(CLASS)
@Target(TYPE)
public @interface ServiceClient {

    /**
     * The builder class that can construct an instance of this class. All service clients are instantiated using a
     * builder and this is a required field. Also, builders should be annotated with {@link ServiceClientBuilder}.
     *
     * @return the classname of the builder that can create an instance of this class.
     */
    Class<?> builder();

    /**
     * Represents whether the network IO methods on this client will be performed asynchronously or synchronously (i.e.
     * blocking).
     *
     * @return {@code true} is the Service Client is asynchronous.
     */
    boolean isAsync() default false;

    /**
     * Optional field to indicate all the services this service client interacts with. All classes mentioned in this
     * list should be annotated with {@link ServiceInterface}. Typically, there's one service associated with each
     * client. However, there could be zero to N services associated with a single client.
     *
     * @return An array of all services this service client interacts with
     */
    Class<?>[] serviceInterfaces() default { };
}
