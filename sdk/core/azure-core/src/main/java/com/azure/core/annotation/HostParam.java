// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to annotate replacement of parameterized segments in a dynamic {@link Host}.
 *
 * <p>You provide the value, which should be the same (case sensitive) with the parameterized segments in '{}' in the
 * host, unless there's only one parameterized segment, then you can leave the value empty. This is extremely
 * useful when the designer of the API interface doesn't know about the named parameters in the host.</p>
 *
 * <p><strong>Example 1: Named parameters</strong></p>
 *
 * <pre>
 * {@literal @}Host("{accountName}.{suffix}")
 *  interface DatalakeService {
 *   {@literal @}GET("jobs/{jobIdentity}")
 *    Job getJob(@HostParam("accountName") String accountName, @HostParam("suffix") String suffix, @PathParam
 *    ("jobIdentity") jobIdentity);
 *  }</pre>
 *
 * <p><strong>Example 2: Unnamed parameter</strong></p>
 *
 * <pre>
 * {@literal @}Host(KEY_VAULT_ENDPOINT)
 *  interface KeyVaultService {
 *   {@literal @}GET("secrets/{secretName}")
 *    Secret get(@HostParam String vaultName, @PathParam("secretName") String secretName);
 *  }</pre>
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface HostParam {
    /**
     * The name of the variable in the endpoint uri template which will be replaced with the value
     * of the parameter annotated with this annotation.
     * @return The name of the variable in the endpoint uri template which will be replaced with the
     *     value of the parameter annotated with this annotation.
     */
    String value();
    /**
     * A value true for this argument indicates that value of {@link HostParam#value()} is already
     * encoded hence engine should not encode it, by default value will be encoded.
     * @return Whether or not this argument is already encoded.
     */
    boolean encoded() default true;
}
