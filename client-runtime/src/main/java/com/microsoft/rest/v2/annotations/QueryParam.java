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
 * Named query parameters appended to a URL.
 *
 * Example#1:
 *
 *   {@literal @}GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/resources")
 *   {@literal Single<RestResponse<Headers, Body>>} listByResourceGroup(@PathParam("resourceGroupName") String resourceGroupName, @PathParam("subscriptionId") String subscriptionId, @QueryParam("$filter") String filter, @QueryParam("$expand") String expand, @QueryParam("$top") Integer top, @QueryParam("api-version") String apiVersion);
 *
 *   The value of parameters filter, expand, top, apiVersion will be encoded and encoded value will be used to replace the corresponding path segment {$filter},
 *   {$expand}, {$top}, {api-version} respectively.
 *
 * Example#2 (A use case where PathParam.encoded=true will be used)
 *
 *   It is possible that, a path segment variable can be used to represent sub path:
 *
 *   {@literal @}GET("http://wq.com/foo/{subpath}/values")
 *   String getValues(@PathParam("subpath") String param, @QueryParam("connectionString") String connectionString);
 *
 *   In this case, if consumer pass "a=b" as the value for query then the resolved url looks like: "http://wq.com/foo/paramblah/values?connectionString=a%3Db"
 *
 *   For such cases the encoded attribute can be used:
 *
 *   {@literal @}GET("http://wq.com/foo/{subpath}/values")
 *   String getValues(@PathParam("subpath") String param, @QueryParam("query", encoded = true) String query);
 *
 *   In this case, if consumer pass "a=b" as the value for param1 then the resolved url looks as expected: "http://wq.com/foo/paramblah/values?connectionString=a=b"
 *
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface QueryParam {
    /**
     * The name of the variable in the endpoint uri template which will be replaced with the value
     * of the parameter annotated with this annotation.
     * @return The name of the variable in the endpoint uri template which will be replaced with the
     * value of the parameter annotated with this annotation.
     */
    String value();
    /**
     * A value true for this argument indicates that value of {@link QueryParam#value()} is already encoded
     * hence engine should not encode it, by default value will be encoded.
     * @return Whether or not this query parameter is already encoded.
     */
    boolean encoded() default false;
}