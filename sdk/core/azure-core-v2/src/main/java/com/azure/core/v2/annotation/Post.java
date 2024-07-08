// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * HTTP POST method annotation describing the parameterized relative path to a REST endpoint for an action.
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
 * <!-- src_embed com.azure.core.annotation.Post.class1 -->
 * <pre>
 * &#64;Post&#40;&quot;subscriptions&#47;&#123;subscriptionId&#125;&#47;resourceGroups&#47;&#123;resourceGroupName&#125;&#47;providers&#47;Microsoft.Compute&#47;&quot;
 *     + &quot;virtualMachines&#47;&#123;vmName&#125;&#47;restart&quot;&#41;
 * void restart&#40;&#64;PathParam&#40;&quot;resourceGroupName&quot;&#41; String rgName,
 *     &#64;PathParam&#40;&quot;vmName&quot;&#41; String vmName,
 *     &#64;PathParam&#40;&quot;subscriptionId&quot;&#41; String subscriptionId&#41;;
 * </pre>
 * <!-- end com.azure.core.annotation.Post.class1 -->
 *
 * <p>
 * <strong>Example 2: Absolute path segment</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Post.class2 -->
 * <pre>
 * &#64;Post&#40;&quot;https:&#47;&#47;&#123;functionApp&#125;.azurewebsites.net&#47;admin&#47;functions&#47;&#123;name&#125;&#47;keys&#47;&#123;keyName&#125;&quot;&#41;
 * KeyValuePair generateFunctionKey&#40;&#64;PathParam&#40;&quot;functionApp&quot;&#41; String functionApp,
 *     &#64;PathParam&#40;&quot;name&quot;&#41; String name,
 *     &#64;PathParam&#40;&quot;keyName&quot;&#41; String keyName&#41;;
 * </pre>
 * <!-- end com.azure.core.annotation.Post.class2 -->
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Post {
    /**
     * Get the relative path of the annotated method's POST URL.
     * @return The relative path of the annotated method's POST URL.
     */
    String value();
}
