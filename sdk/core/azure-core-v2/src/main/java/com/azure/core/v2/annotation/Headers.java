// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to annotate list of static headers sent to a REST endpoint.
 *
 * <p>
 * Headers are comma separated strings, with each in the format of "header name: header value1,header value2".
 * </p>
 *
 * <p>
 * <strong>Examples:</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Headers.class -->
 * <!-- end com.azure.core.annotation.Headers.class -->
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Headers {
    /**
     * List of static headers.
     *
     * @return List of static headers.
     */
    String[] value();
}
