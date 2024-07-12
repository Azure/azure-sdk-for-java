// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Annotation given to all service client methods that perform network operations. All methods with this annotation
 * should be contained in class annotated with {@link ServiceClient}
 */
@Retention(CLASS)
@Target(METHOD)
public @interface ServiceMethod {

    /**
     * This represents the return type expected from this service method.
     *
     * @return the return type of the method annotated with {@link ServiceMethod}
     */
    ReturnType returns();

}
