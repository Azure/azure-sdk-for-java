// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for query parameters to be appended to a REST API Request URI.
 *
 * <p><strong>Example 1:</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.QueryParam.class1 -->
 * <!-- end com.generic.core.annotation.QueryParam.class1 -->
 *
 * <p><strong>Example 2:</strong> (A use case where PathParam.encoded=true will be used)</p>
 *
 * <!-- src_embed com.generic.core.annotation.QueryParam.class2 -->
 * <!-- end com.generic.core.annotation.QueryParam.class2 -->
 *
 * <p>For such cases the encoded attribute can be used:</p>
 *
 * <!-- src_embed com.generic.core.annotation.QueryParam.class3 -->
 * <!-- end com.generic.core.annotation.QueryParam.class3 -->
 *
 * <p><strong>Example 3:</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.QueryParam.class4 -->
 * <!-- end com.generic.core.annotation.QueryParam.class4 -->
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface QueryParam {
    /**
     * The name of the variable in the endpoint uri template which will be replaced with the value
     * of the parameter annotated with this annotation.
     * @return The name of the variable in the endpoint uri template which will be replaced with the
     *     value of the parameter annotated with this annotation.
     */
    String value();
    /**
     * A value true for this argument indicates that value of {@link QueryParam#value()} is already encoded
     * hence engine should not encode it, by default value will be encoded.
     * @return Whether this query parameter is already encoded.
     */
    boolean encoded() default false;
    /**
     * A value true for this argument indicates that value of {@link QueryParam#value()} should not be
     * converted to Json in case it is an array but instead sent as multiple values with same parameter
     * name.
     * @return Whether this query parameter list values should be sent as individual query
     * params or as a single Json.
     */
    boolean multipleQueryParams() default false;
}
