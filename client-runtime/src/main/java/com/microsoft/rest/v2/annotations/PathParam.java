/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Named replacement in a URL path segment.
 *
 * A parameter that is annotated with PathParam will be ignored if the "uri template"
 * does not contain a path segment variable with name {@link PathParam#value()}.
 *
 * Example#1:
 *
 *   {@literal @}GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/")
 *   VirtualMachine getByResourceGroup(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String rgName, @PathParam("foo") String bar);
 *
 *   The value of parameters subscriptionId, resourceGroupName will be encoded and encoded value will be used to replace the corresponding path segment {subscriptionId},
 *   {resourceGroupName} respectively.
 *
 * Example#2 (A use case where PathParam.encoded=true will be used)
 *
 *   It is possible that, a path segment variable can be used to represent sub path:
 *
 *   {@literal @}GET("http://wq.com/foo/{subpath}/values")
 *   String getValues(@PathParam("subpath") String param1);
 *
 *   In this case, if consumer pass "a/b" as the value for param1 then the resolved url looks like: "http://wq.com/foo/a%2Fb/values"
 *
 *   For such cases the encoded attribute can be used:
 *
 *   {@literal @}GET("http://wq.com/foo/{subpath}/values")
 *   String getValues(@PathParam(value = "subpath", encoded = true) String param1);
 *
 *   In this case, if consumer pass "a/b" as the value for param1 then the resolved url looks as expected: "http://wq.com/foo/a/b/values"
 *
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface PathParam {
    /**
     * The name of the variable in the endpoint uri template which will be replaced with the value
     * of the parameter annotated with this annotation.
     * @return The name of the variable in the endpoint uri template which will be replaced with the
     * value of the parameter annotated with this annotation.
     */
    String value();
    /**
     * A value true for this argument indicates that value of {@link PathParam#value()} is already encoded
     * hence engine should not encode it, by default value will be encoded.
     * @return Whether or not this path parameter is already encoded.
     */
    boolean encoded() default false;
}