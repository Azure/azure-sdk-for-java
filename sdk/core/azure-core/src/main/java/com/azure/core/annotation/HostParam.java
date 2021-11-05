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
     * The name of the variable in the endpoint URI template which will be replaced with the value of the parameter
     * annotated with this annotation.
     *
     * @return The name of the variable in the endpoint URI template which will be replaced with the value of the
     * parameter annotated with this annotation.
     */
    String value();

    /**
     * If set to {@code true}, this argument indicates that {@link HostParam#value() the value of this host parameter}
     * is already encoded, meaning that it should not be encoded again by an external actor. The default value for this
     * property is {@code false}.
     *
     * @return Whether this host parameter is already encoded.
     */
    boolean encoded() default true;
}
