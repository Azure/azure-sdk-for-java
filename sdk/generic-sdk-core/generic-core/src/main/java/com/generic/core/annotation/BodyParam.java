// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to annotate a parameter to send to a REST endpoint as HTTP Request content.
 *
 * <p>If the parameter type extends <code>InputStream</code>, this payload is streamed to server through
 * "application/octet-stream". Otherwise, the body is serialized first and sent as "application/json" or
 * "application/xml", based on the serializer.
 * </p>
 *
 * <p><strong>Example 1: Put JSON</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.BodyParam.class1 -->
 * <!-- end com.generic.core.annotation.BodyParam.class1 -->
 *
 * <p><strong>Example 2: Stream</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.BodyParam.class2 -->
 * <!-- end com.generic.core.annotation.BodyParam.class2 -->
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface BodyParam {
    /**
     * Gets the Content-Type for the body.
     *
     * @return The Content-Type for the body.
     */
    String value();
}
