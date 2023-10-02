// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for query parameters to be appended to a REST API Request URI.
 *
 * <p><strong>Example 1:</strong></p>
 *
 * <!-- src_embed com.azure.core.annotation.QueryParam.class1 -->
 * <pre>
 * &#64;Get&#40;&quot;subscriptions&#47;&#123;subscriptionId&#125;&#47;resourceGroups&#47;&#123;resourceGroupName&#125;&#47;resources&quot;&#41;
 * Mono&lt;ResponseBase&lt;ResponseHeaders, ResponseBody&gt;&gt; listByResourceGroup&#40;
 *     &#64;PathParam&#40;&quot;resourceGroupName&quot;&#41; String resourceGroupName,
 *     &#64;PathParam&#40;&quot;subscriptionId&quot;&#41; String subscriptionId,
 *     &#64;QueryParam&#40;&quot;$filter&quot;&#41; String filter,
 *     &#64;QueryParam&#40;&quot;$expand&quot;&#41; String expand,
 *     &#64;QueryParam&#40;&quot;$top&quot;&#41; Integer top,
 *     &#64;QueryParam&#40;&quot;api-version&quot;&#41; String apiVersion&#41;;
 *
 * &#47;&#47; The value of parameters filter, expand, top, apiVersion will be encoded and will be used to set the query
 * &#47;&#47; parameters &#123;$filter&#125;, &#123;$expand&#125;, &#123;$top&#125;, &#123;api-version&#125; on the HTTP URL.
 * </pre>
 * <!-- end com.azure.core.annotation.QueryParam.class1 -->
 *
 * <p><strong>Example 2:</strong> (A use case where PathParam.encoded=true will be used)</p>
 *
 * <!-- src_embed com.azure.core.annotation.QueryParam.class2 -->
 * <pre>
 * &#47;&#47; It is possible that a query parameter will need to be encoded:
 * &#64;Get&#40;&quot;http:&#47;&#47;wq.com&#47;foo&#47;&#123;subpath&#125;&#47;value&quot;&#41;
 * String getValue&#40;&#64;PathParam&#40;&quot;subpath&quot;&#41; String param,
 *     &#64;QueryParam&#40;&quot;query&quot;&#41; String query&#41;;
 *
 * &#47;&#47; In this case, if consumer pass &quot;a=b&quot; as the value for 'query' then the resolved url looks like:
 * &#47;&#47; &quot;http:&#47;&#47;wq.com&#47;foo&#47;subpath&#47;value?query=a%3Db&quot;
 * </pre>
 * <!-- end com.azure.core.annotation.QueryParam.class2 -->
 *
 * <p>For such cases the encoded attribute can be used:</p>
 *
 * <!-- src_embed com.azure.core.annotation.QueryParam.class3 -->
 * <pre>
 * &#64;Get&#40;&quot;http:&#47;&#47;wq.com&#47;foo&#47;&#123;subpath&#125;&#47;values&quot;&#41;
 * List&lt;String&gt; getValues&#40;&#64;PathParam&#40;&quot;subpath&quot;&#41; String param,
 *     &#64;QueryParam&#40;value = &quot;query&quot;, encoded = true&#41; String query&#41;;
 *
 * &#47;&#47; In this case, if consumer pass &quot;a=b&quot; as the value for 'query' then the resolved url looks like:
 * &#47;&#47; &quot;http:&#47;&#47;wq.com&#47;foo&#47;paramblah&#47;values?connectionString=a=b&quot;
 * </pre>
 * <!-- end com.azure.core.annotation.QueryParam.class3 -->
 *
 * <p><strong>Example 3:</strong></p>
 *
 * <!-- src_embed com.azure.core.annotation.QueryParam.class4 -->
 * <pre>
 * &#64;Get&#40;&quot;http:&#47;&#47;wq.com&#47;foo&#47;multiple&#47;params&quot;&#41;
 * String multipleParams&#40;&#64;QueryParam&#40;value = &quot;query&quot;, multipleQueryParams = true&#41; List&lt;String&gt; query&#41;;
 *
 * &#47;&#47; The value of parameter avoid would look like this:
 * &#47;&#47; &quot;http:&#47;&#47;wq.com&#47;foo&#47;multiple&#47;params?avoid%3Dtest1&amp;avoid%3Dtest2&amp;avoid%3Dtest3&quot;
 * </pre>
 * <!-- end com.azure.core.annotation.QueryParam.class4 -->
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
