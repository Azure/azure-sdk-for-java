// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP OPTIONS method annotation describing the parameterized relative path to a REST endpoint for retrieving options.
 *
 * <p>
 * The required value can be either a relative path or an absolute path. When it's an absolute path, it must start
 * with a protocol or a parameterized segment (Otherwise the parse cannot tell if it's absolute or relative).
 * </p>
 *
 * <p>
 * <strong>Example 1: Relative path segments</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Options.class1 -->
 * <!-- end com.azure.core.annotation.Options.class1 -->
 *
 * <p>
 * <strong>Example 2: Absolute path segment</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Options.class2 -->
 * <!-- end com.azure.core.annotation.Options.class2 -->
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Options {
    /**
     * Get the relative path of the annotated method's OPTIONS URL.
     *
     * @return The relative path of the annotated method's OPTIONS URL.
     */
    String value();
}
