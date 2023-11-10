// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * sn
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ReturnValueWireType {
    /**
     * The type that the service interface method's return value will be converted from.
     *
     * @return The type that the service interface method's return value will be converted from.
     */
    Class<?> value();
}
