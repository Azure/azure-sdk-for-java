// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.annotation;

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
 * <!-- src_embed com.azure.core.annotation.PathParam.class1 -->
 * <pre>
 * &#64;Get&#40;&quot;subscriptions&#47;&#123;subscriptionId&#125;&#47;resourceGroups&#47;&#123;resourceGroupName&#125;&#47;providers&#47;Microsoft.Compute&#47;&quot;
 *     + &quot;virtualMachines&#47;&quot;&#41;
 * VirtualMachine getByResourceGroup&#40;&#64;PathParam&#40;&quot;subscriptionId&quot;&#41; String subscriptionId,
 *     &#64;PathParam&#40;&quot;resourceGroupName&quot;&#41; String rgName,
 *     &#64;PathParam&#40;&quot;foo&quot;&#41; String bar&#41;;
 *
 * &#47;&#47; The value of parameters subscriptionId, resourceGroupName will be encoded and used to replace the
 * &#47;&#47; corresponding path segments &#123;subscriptionId&#125;, &#123;resourceGroupName&#125; respectively.
 * </pre>
 * <!-- end com.azure.core.annotation.PathParam.class1 -->
 *
 * <p><strong>Example 2: (A use case where PathParam.encoded=true will be used)</strong></p>
 *
 * <!-- src_embed com.azure.core.annotation.PathParam.class2 -->
 * <pre>
 * &#47;&#47; It is possible that a path segment variable can be used to represent sub path:
 *
 * &#64;Get&#40;&quot;http:&#47;&#47;wq.com&#47;foo&#47;&#123;subpath&#125;&#47;value&quot;&#41;
 * String getValue&#40;&#64;PathParam&#40;&quot;subpath&quot;&#41; String param1&#41;;
 *
 * &#47;&#47; In this case, if consumer pass &quot;a&#47;b&quot; as the value for param1 then the resolved url looks like:
 * &#47;&#47; &quot;http:&#47;&#47;wq.com&#47;foo&#47;a%2Fb&#47;value&quot;.
 * </pre>
 * <!-- end com.azure.core.annotation.PathParam.class2 -->
 *
 * <!-- src_embed com.azure.core.annotation.PathParam.class3 -->
 * <pre>
 * &#47;&#47; For such cases the encoded attribute can be used:
 *
 * &#64;Get&#40;&quot;http:&#47;&#47;wq.com&#47;foo&#47;&#123;subpath&#125;&#47;values&quot;&#41;
 * List&lt;String&gt; getValues&#40;&#64;PathParam&#40;value = &quot;subpath&quot;, encoded = true&#41; String param1&#41;;
 *
 * &#47;&#47; In this case, if consumer pass &quot;a&#47;b&quot; as the value for param1 then the resolved url looks as expected:
 * &#47;&#47; &quot;http:&#47;&#47;wq.com&#47;foo&#47;a&#47;b&#47;values&quot;.
 * </pre>
 * <!-- end com.azure.core.annotation.PathParam.class3 -->
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
