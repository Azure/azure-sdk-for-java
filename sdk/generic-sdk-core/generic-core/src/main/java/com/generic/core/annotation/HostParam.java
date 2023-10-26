// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to annotate replacement of parameterized segments in a dynamic {@link Host}.
 *
 * <p>You provide the value, which should be the same (case sensitive) with the parameterized segments in '{}' in the
 * host, unless there's only one parameterized segment, then you can leave the value empty. This is extremely useful
 * when the designer of the API interface doesn't know about the named parameters in the host.</p>
 *
 * <p><strong>Example 1: Named parameters</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.HostParam.class1 -->
 * <pre>
 * &#64;Host&#40;&quot;&#123;accountName&#125;.&#123;suffix&#125;&quot;&#41;
 * interface DatalakeService &#123;
 *     &#64;Get&#40;&quot;jobs&#47;&#123;jobIdentity&#125;&quot;&#41;
 *     Job getJob&#40;&#64;HostParam&#40;&quot;accountName&quot;&#41; String accountName,
 *         &#64;HostParam&#40;&quot;suffix&quot;&#41; String suffix,
 *         &#64;PathParam&#40;&quot;jobIdentity&quot;&#41; String jobIdentity&#41;;
 * &#125;
 * </pre>
 * <!-- end com.generic.core.annotation.HostParam.class1 -->
 *
 * <p><strong>Example 2: Unnamed parameter</strong></p>
 *
 * <!-- src_embed com.generic.core.annotation.HostParam.class2 -->
 * <pre>
 * String KEY_VAULT_ENDPOINT = &quot;&#123;vaultName&#125;&quot;;
 *
 * &#64;Host&#40;KEY_VAULT_ENDPOINT&#41;
 * interface KeyVaultService &#123;
 *     &#64;Get&#40;&quot;secrets&#47;&#123;secretName&#125;&quot;&#41;
 *     Secret get&#40;&#64;HostParam&#40;&quot;vaultName&quot;&#41; String vaultName, &#64;PathParam&#40;&quot;secretName&quot;&#41; String secretName&#41;;
 * &#125;
 * </pre>
 * <!-- end com.generic.core.annotation.HostParam.class2 -->
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface HostParam {
    /**
     * The name of the variable in the endpoint uri template which will be replaced with the value of the parameter
     * annotated with this annotation.
     *
     * @return The name of the variable in the endpoint uri template which will be replaced with the value of the
     * parameter annotated with this annotation.
     */
    String value();

    /**
     * A value true for this argument indicates that value of {@link HostParam#value()} is already encoded hence engine
     * should not encode it, by default value will be encoded.
     *
     * @return Whether this argument is already encoded.
     */
    boolean encoded() default true;
}
