// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for query parameters to be appended to a REST API Request URI.
 *
 * <p><strong>Example 1:</strong></p>
 *
 * <pre>
 * {@literal @}GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/resources")
 * {@literal Single<RestResponseBase<Headers, Body>>} listByResourceGroup(@PathParam("resourceGroupName") String
 * resourceGroupName, @PathParam("subscriptionId") String subscriptionId, @QueryParam("$filter") String
 * filter, @QueryParam("$expand") String expand, @QueryParam("$top") Integer top, @QueryParam("api-version") String
 * apiVersion);</pre>
 *
 * <p>The value of parameters filter, expand, top, apiVersion will be encoded and encoded value will be used to replace
 * the corresponding path segment {$filter},
 * {$expand}, {$top}, {api-version} respectively.</p>
 *
 * <p><strong>Example 2:</strong> (A use case where PathParam.encoded=true will be used)</p>
 *
 * <p>It is possible that, a path segment variable can be used to represent sub path:</p>
 *
 * <pre>
 * {@literal @}GET("http://wq.com/foo/{subpath}/values")
 *  String getValues(@PathParam("subpath") String param, @QueryParam("connectionString") String connectionString);</pre>
 *
 * <p>In this case, if consumer pass "a=b" as the value for query then the resolved url looks like:
 * "<code>http://wq.com/foo/paramblah/values?connectionString=a%3Db</code>"</p>
 *
 * <p>For such cases the encoded attribute can be used:</p>
 *
 * <pre>
 * {@literal @}GET("http://wq.com/foo/{subpath}/values")
 *  String getValues(@PathParam("subpath") String param, @QueryParam("query", encoded = true) String query);</pre>
 *
 * <p>In this case, if consumer pass "a=b" as the value for param1 then the resolved url looks as expected:
 * "<code>http://wq.com/foo/paramblah/values?connectionString=a=b</code>"</p>
 * 
 * <p><strong>Example 3:</strong></p>
 *
 * <pre>
 * {@literal @}GET("http://wq.com/foo/multiple/params")
 *  String multipleParams(@QueryParam("avoid", multipleQueryParams = true) List&lt;String&gt; avoid);</pre>
 *
 * <p>The value of parameter avoid would look like this:
 * "<code>http://wq.com/foo/multiple/params?avoid%3Dtest1{@literal &}avoid%3Dtest2{@literal &}avoid%3Dtest3</code>"</p>
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
     * @return Whether or not this query parameter is already encoded.
     */
    boolean encoded() default false;
    /**
     * A value true for this argument indicates that value of {@link QueryParam#value()} should not be
     * converted to Json in case it is an array but instead sent as multiple values with same parameter
     * name.
     * @return Whether or not this query parameter list values should be sent as individual query
     * params or as a single Json.
     */
    boolean multipleQueryParams() default false;
}
