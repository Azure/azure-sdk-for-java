// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation given to all service client builder classes.
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceClientBuilder {

    /**
     * An array of classes that this builder can build.
     */
    Class<?>[] serviceClients();
}
