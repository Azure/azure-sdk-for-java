// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Replaces the header with the value of its target. The value specified here replaces headers specified statically in
 * the {@link Headers}. If the parameter this annotation is attached to is a Map type, then this will be treated as a
 * header collection. In that case each of the entries in the argument's map will be individual header values that use
 * the value of this annotation as a prefix to their key/header name.
 *
 * <p><strong>Example 1:</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.HeaderParam.class1 -->
 * <!-- end com.generic.core.annotation.HeaderParam.class1 -->
 *
 * <p><strong>Example 2:</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.HeaderParam.class2 -->
 * <!-- end com.generic.core.annotation.HeaderParam.class2 -->
 *
 * <p><strong>Example 3:</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.HeaderParam.class3 -->
 * <!-- end com.generic.core.annotation.HeaderParam.class3 -->
 *
 * <p><strong>Example 4:</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.HeaderParam.class4 -->
 * <!-- end com.generic.core.annotation.HeaderParam.class4 -->
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface HeaderParam {
    /**
     * The name of the variable in the endpoint uri template which will be replaced with the value of the parameter
     * annotated with this annotation.
     *
     * @return The name of the variable in the endpoint uri template which will be replaced with the value of the
     * parameter annotated with this annotation.
     */
    String value();
}
