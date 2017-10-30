/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A host parameter replaces the parametrized segments in a dynamic {@link Host}.
 *
 * You provide the value, which should be the same (case sensitive) with
 * the parametrized segments in '{}' in the host, unless there's only one
 * parametrized segment, then you can leave the value empty. This is extremely
 * useful when the designer of the API interface doesn't know about the named
 * parameters in the host.
 *
 * Example 1: named parameters
 *
 *   {@literal @}Host("{accountName}.{suffix}")
 *   interface DatalakeService {
 *     {@literal @}GET("jobs/{jobIdentity}")
 *     Job getJob(@HostParam("accountName") String accountName, @HostParam("suffix") String suffix, @PathParam("jobIdentity") jobIdentity);
 *   }
 *
 * Example 2: unnamed parameter
 *
 *    {@literal @}Host(KEY_VAULT_ENDPOINT)
 *    interface KeyVaultService {
 *       {@literal @}GET("secrets/{secretName}")
 *       Secret get(@HostParam String vaultName, @PathParam("secretName") String secretName);
 *    }
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface HostParam {
    /**
     * The name of the variable in the endpoint uri template which will be replaced with the value
     * of the parameter annotated with this annotation.
     * @return The name of the variable in the endpoint uri template which will be replaced with the
     * value of the parameter annotated with this annotation.
     */
    String value();
    /**
     * A value true for this argument indicates that value of {@link HostParam#value()} is already
     * encoded hence engine should not encode it, by default value will be encoded.
     * @return Whether or not this argument is already encoded.
     */
    boolean encoded() default false;
}