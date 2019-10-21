// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation given to all service client methods that perform network operations.
 * All methods with this annotation should be contained in class annotated with {@link ServiceClient}
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface ServiceMethod {

    /**
     * This represents the return type expected from this service method.
     * @return the return type of the method annotated with {@link ServiceMethod}
     */
    ReturnType returns();

}
