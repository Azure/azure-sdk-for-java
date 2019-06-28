// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation given to all service client classes.
 */
@Target({TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface ServiceClient {

    /**
     * The builder class that can construct an instance of this class.
     * All service clients are instantiated using a builder and this is a required field.
     * Also, builders should be annotated with {@link ServiceClientBuilder}
     */
    Class<?> builder();

    /**
     * Represents whether the network IO methods on this client will be performed asynchronously or
     * synchronously (i.e. blocking).
     */
    boolean isAsync() default false;
}
