// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.annotation;

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
 * <pre>
 * {@literal @}GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft
 * .Compute/virtualMachines/")
 *  VirtualMachine getByResourceGroup(@PathParam("subscriptionId") String subscriptionId, @PathParam
 *  ("resourceGroupName") String rgName, @PathParam("foo") String bar);</pre>
 *
 * <p>The value of parameters subscriptionId, resourceGroupName will be encoded and encoded value will be used to
 * replace the corresponding path segment <code>{subscriptionId}</code>, <code>{resourceGroupName}</code>
 * respectively.</p>
 *
 * <p><strong>Example 2: (A use case where PathParam.encoded=true will be used)</strong></p>
 *
 * <p>It is possible that, a path segment variable can be used to represent sub path:</p>
 *
 * <pre>
 * {@literal @}GET("http://wq.com/foo/{subpath}/values")
 *  String getValues(@PathParam("subpath") String param1);</pre>
 *
 * <p>In this case, if consumer pass "a/b" as the value for param1 then the resolved url looks like:
 * "<code>http://wq.com/foo/a%2Fb/values</code>".</p>
 *
 * <p>For such cases the encoded attribute can be used:</p>
 *
 * <pre>
 * {@literal @}GET("http://wq.com/foo/{subpath}/values")
 *  String getValues(@PathParam(value = "subpath", encoded = true) String param1);</pre>
 *
 * <p>In this case, if consumer pass "a/b" as the value for param1 then the resolved url looks as expected:
 * "<code>http://wq.com/foo/a/b/values</code>".</p>
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
