// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import com.azure.core.annotation.ReturnType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
/**
 * Annotation given to all service client methods that perform network operations.
 * All methods with this annotation should be contained in class annotated with {@link ServiceClient}
 */
@Target({METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface ServiceMethod {

    /**
     * This represents the return type expected from this service method.
     * @return the return type of the method annotated with {@link ServiceMethod}
     */
    ReturnType returns();

}
