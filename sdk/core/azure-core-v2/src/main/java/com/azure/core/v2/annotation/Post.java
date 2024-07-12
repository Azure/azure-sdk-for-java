// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * HTTP POST method annotation describing the parameterized relative path to a REST endpoint for an action.
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
 * <!-- src_embed com.azure.core.annotation.Post.class1 -->
 * <!-- end com.azure.core.annotation.Post.class1 -->
 *
 * <p>
 * <strong>Example 2: Absolute path segment</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Post.class2 -->
 * <!-- end com.azure.core.annotation.Post.class2 -->
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Post {
    /**
     * Get the relative path of the annotated method's POST URL.
     * @return The relative path of the annotated method's POST URL.
     */
    String value();
}
