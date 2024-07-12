// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

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
     * @return the service name given to the interface.
     */
    String name();
}
