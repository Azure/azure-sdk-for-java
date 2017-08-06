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

@Retention(RUNTIME)
@Target(PARAMETER)
public @interface PathParam {
    /**
     * The name of the variable in the endpoint uri template which will be replaced with the value
     * of the parameter annotated with this annotation.
     */
    String value();
    /**
     * A value true for this argument indicates that value of {@link PathParam#value()} is already encoded
     * hence engine should not encode it, by default value will be encoded.
     */
    boolean encoded() default false;
}

/**
 *  A parameter that is annotated with PathParam will be ignored if the "uri template" (see {@link com.microsoft.rest.Doc_Http_Verb_Annotation_Value_URI_Template})
 *  does not contain a path segment variable with name {@link PathParam#value()}.
 *
 * Example#1:
 *
 *    @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/")
 *    VirtualMachine getByResourceGroup(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String rgName, @PathParam("foo") String bar);
 *
 *    The value of parameters subscriptionId, resourceGroupName will be encoded and encoded value will be used to replace the corresponding path segment {subscriptionId},
 *    {resourceGroupName} respectively.
 *
 *  Example#2 (A use case where PathParam.encoded=true will be used)
 *
 *    It is possible that, a path segment variable can be used to represent sub path:
 *
 *    @GET("http://wq.com/foo/{subpath}/values")
 *    String getValues(@PathParam("subpath") String param1);
 *
 *    In this case, if consumer pass "a/b" as the value for param1 then the resolved url looks like: "http://wq.com/foo/a%2Fb/values"
 *
 *    For such cases the encoded attribute can be used:
 *
 *    @GET("http://wq.com/foo/{subpath}/values")
 *    String getValues(@PathParam(value = "subpath", encoded = true) String param1);
 *
 *    In this case, if consumer pass "a/b" as the value for param1 then the resolved url looks as expected: "http://wq.com/foo/a/b/values"
 *
 *  Example#3:
 *
 *    If the "uri template" is relative then engine will resolve full url using the base url. If base URL contains path segment variables then
 *    that also should be resolved using the parameters annotated with PathParam.
 *
 *    @GET("/foo/{name2}/values")
 *    String getValues(@PathParam("name1") String param1, @PathParam("name2") String param2);
 *
 *    Here name2 is not exists in the uri segment but after resolving the full path URI may looks like:
 *
 *    http://foo.com/sd/{name1}/foo/{name2}/values
 *
 *    Apply replacement rule in the resolved path.
 *
 *    NOTE for us: Understand how encoding needs to be applied to path segments, it seems there is slight difference in encoding path versus query.
 *    ===========
 **/
