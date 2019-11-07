// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
}
