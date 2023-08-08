// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * HTTP PUT method annotation describing the parameterized relative path to a REST endpoint for resource creation or
 * update.
 *
 * <p>The required value can be either a relative path or an absolute path. When it's an absolute path, it must start
 * with a protocol or a parameterized segment (Otherwise the parse cannot tell if it's absolute or relative).</p>
 *
 * <p><strong>Example 1: Relative path segments</strong></p>
 *
 * <!-- src_embed com.azure.core.annotation.Put.class1 -->
 * <pre>
 * &#64;Put&#40;&quot;subscriptions&#47;&#123;subscriptionId&#125;&#47;resourceGroups&#47;&#123;resourceGroupName&#125;&#47;providers&#47;Microsoft.Compute&#47;&quot;
 *     + &quot;virtualMachines&#47;&#123;vmName&#125;&quot;&#41;
 * VirtualMachine createOrUpdate&#40;&#64;PathParam&#40;&quot;resourceGroupName&quot;&#41; String rgName,
 *     &#64;PathParam&#40;&quot;vmName&quot;&#41; String vmName,
 *     &#64;PathParam&#40;&quot;subscriptionId&quot;&#41; String subscriptionId,
 *     &#64;BodyParam&#40;&quot;application&#47;json&quot;&#41; VirtualMachine vm&#41;;
 * </pre>
 * <!-- end com.azure.core.annotation.Put.class1 -->
 *
 * <p><strong>Example 2: Absolute path segment</strong></p>
 *
 * <!-- src_embed com.azure.core.annotation.Put.class2 -->
 * <pre>
 * &#64;Put&#40;&quot;&#123;vaultBaseUrl&#125;&#47;secrets&#47;&#123;secretName&#125;&quot;&#41;
 * Secret createOrUpdate&#40;&#64;PathParam&#40;value = &quot;vaultBaseUrl&quot;, encoded = true&#41; String vaultBaseUrl,
 *     &#64;PathParam&#40;&quot;secretName&quot;&#41; String secretName,
 *     &#64;BodyParam&#40;&quot;application&#47;json&quot;&#41; Secret secret&#41;;
 * </pre>
 * <!-- end com.azure.core.annotation.Put.class2 -->
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Put {
    /**
     * Get the relative path of the annotated method's PUT URL.
     * @return The relative path of the annotated method's PUT URL.
     */
    String value();
}
