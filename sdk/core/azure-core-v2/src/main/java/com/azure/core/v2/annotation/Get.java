// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * HTTP GET method annotation describing the parameterized relative path to a REST endpoint for resource retrieval.
 *
 * <p>
 * The required value can be either a relative path or an absolute path. When it's an absolute path, it must start
 * with a protocol or a parameterized segment (otherwise the parse cannot tell if it's absolute or relative).
 * </p>
 *
 * <p>
 * <strong>Example 1: Relative path segments</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Get.class1 -->
 * <pre>
 * &#64;Get&#40;&quot;subscriptions&#47;&#123;subscriptionId&#125;&#47;resourceGroups&#47;&#123;resourceGroupName&#125;&#47;providers&#47;Microsoft.Compute&#47;&quot;
 *     + &quot;virtualMachines&#47;&#123;vmName&#125;&quot;&#41;
 * VirtualMachine getByResourceGroup&#40;&#64;PathParam&#40;&quot;resourceGroupName&quot;&#41; String rgName,
 *     &#64;PathParam&#40;&quot;vmName&quot;&#41; String vmName,
 *     &#64;PathParam&#40;&quot;subscriptionId&quot;&#41; String subscriptionId&#41;;
 * </pre>
 * <!-- end com.azure.core.annotation.Get.class1 -->
 *
 * <p>
 * <strong>Example 2: Absolute path segment</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Get.class2 -->
 * <pre>
 * &#64;Get&#40;&quot;&#123;nextLink&#125;&quot;&#41;
 * List&lt;VirtualMachine&gt; listNext&#40;&#64;PathParam&#40;&quot;nextLink&quot;&#41; String nextLink&#41;;
 * </pre>
 * <!-- end com.azure.core.annotation.Get.class2 -->
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Get {
    /**
     * Get the relative path of the annotated method's GET URL.
     *
     * @return The relative path of the annotated method's GET URL.
     */
    String value();
}
