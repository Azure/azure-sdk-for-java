// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation to give the service interfaces a name that correlates to the service that is usable in a programmatic way.
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    /**
     * Name of the service.
     * @return the service name given to the interface.
     */
    String value() default "";
}
