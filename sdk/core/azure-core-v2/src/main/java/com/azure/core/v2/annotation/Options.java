// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP OPTIONS method annotation describing the parameterized relative path to a REST endpoint for retrieving options.
 *
 * <p>
 * The required value can be either a relative path or an absolute path. When it's an absolute path, it must start
 * with a protocol or a parameterized segment (Otherwise the parse cannot tell if it's absolute or relative).
 * </p>
 *
 * <p>
 * <strong>Example 1: Relative path segments</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Options.class1 -->
 * <pre>
 * &#64;Options&#40;&quot;subscriptions&#47;&#123;subscriptionId&#125;&#47;resourceGroups&#47;&#123;resourceGroupName&#125;&#47;providers&#47;Microsoft.Compute&#47;&quot;
 *     + &quot;virtualMachines&#47;&#123;vmName&#125;&quot;&#41;
 * ResponseBase&lt;ResponseHeaders, ResponseBody&gt; options&#40;&#64;PathParam&#40;&quot;resourceGroupName&quot;&#41; String rgName,
 *     &#64;PathParam&#40;&quot;vmName&quot;&#41; String vmName,
 *     &#64;PathParam&#40;&quot;subscriptionId&quot;&#41; String subscriptionId&#41;;
 * </pre>
 * <!-- end com.azure.core.annotation.Options.class1 -->
 *
 * <p>
 * <strong>Example 2: Absolute path segment</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Options.class2 -->
 * <pre>
 * &#64;Options&#40;&quot;&#123;vaultBaseUrl&#125;&#47;secrets&#47;&#123;secretName&#125;&quot;&#41;
 * ResponseBase&lt;ResponseHeaders, ResponseBody&gt; options&#40;
 *     &#64;PathParam&#40;value = &quot;vaultBaseUrl&quot;, encoded = true&#41; String vaultBaseUrl,
 *     &#64;PathParam&#40;&quot;secretName&quot;&#41; String secretName&#41;;
 * </pre>
 * <!-- end com.azure.core.annotation.Options.class2 -->
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Options {
    /**
     * Get the relative path of the annotated method's OPTIONS URL.
     *
     * @return The relative path of the annotated method's OPTIONS URL.
     */
    String value();
}
