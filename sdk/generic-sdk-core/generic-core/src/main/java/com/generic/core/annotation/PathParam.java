// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to annotate replacement for a named path segment in REST endpoint URL.
 *
 * <p>A parameter that is annotated with PathParam will be ignored if the "uri template" does not contain a path
 * segment variable with name {@link PathParam#value()}.</p>
 *
 * <p><strong>Example 1:</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.PathParam.class1 -->
 * <!-- end com.generic.core.annotation.PathParam.class1 -->
 *
 * <p><strong>Example 2: (A use case where PathParam.encoded=true will be used)</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.PathParam.class2 -->
 * <!-- end com.generic.core.annotation.PathParam.class2 -->
 *
 * <!-- src_embed com.generic.core.annotation.PathParam.class3 -->
 * <!-- end com.generic.core.annotation.PathParam.class3 -->
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface PathParam {
    /**
     * The name of the variable in the endpoint uri template which will be replaced with the value
     * of the parameter annotated with this annotation.
     * @return The name of the variable in the endpoint uri template which will be replaced with the
     *     value of the parameter annotated with this annotation.
     */
    String value();
    /**
     * A value true for this argument indicates that value of {@link PathParam#value()} is already encoded
     * hence engine should not encode it, by default value will be encoded.
     * @return Whether or not this path parameter is already encoded.
     */
    boolean encoded() default false;
}
