/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Defines the parametrized host name of a proxy.
 *
 * This is the 'host' field or 'x-ms-parameterized-host.hostTemplate' field in
 * a Swagger document. parameters are enclosed in {}s, e.g. {accountName}. An
 * HTTP client must accept the parametrized host as the base URL for the request,
 * replacing the parameters during runtime with the actual values users provide.
 *
 * For parametrized hosts, parameters annotated with {@link HostParam} must be
 * provided. See Java docs in {@link HostParam} for directions for host
 * parameters.
 *
 * The host's value must contain the scheme/protocol and the host. The host's value may contain the
 * port number.
 *
 * Example 1: Static annotation.
 *
 *   {@literal @}Host("https://management.azure.com")
 *   interface VirtualMachinesService {
 *     {@literal @}GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}")
 *     VirtualMachine getByResourceGroup(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName, @PathParam("subscriptionId") String subscriptionId);
 *   }
 *
 *  Example 2: Dynamic annotation.
 *
 *    {@literal @}Host("https://{vaultName}.vault.azure.net:443")
 *    interface KeyVaultService {
 *       {@literal @}GET("secrets/{secretName}")
 *       Secret get(@HostParam("vaultName") String vaultName, @PathParam("secretName") String secretName);
 *    }

 */
@Target(value = {TYPE})
@Retention(RetentionPolicy.RUNTIME)        // Record this annotation in the class file and make it available during runtime.
public @interface Host {
    /**
     * Get the protocol/scheme, host, and optional port number in a single string.
     * @return The protocol/scheme, host, and optional port number in a single string.
     */
    String value() default "";
}
