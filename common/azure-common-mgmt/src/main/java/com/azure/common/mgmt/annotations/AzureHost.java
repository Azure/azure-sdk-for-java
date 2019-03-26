/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt.annotations;

import com.azure.common.AzureEnvironment;
import com.azure.common.AzureEnvironment.Endpoint;
import com.azure.common.annotations.Host;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * An extension to {@link Host}, allowing endpoints
 * of {@link AzureEnvironment} to be specified instead of string
 * host names. This allows self adaptive base URLs based on the environment the
 * client is running in.
 *
 * Example 1: Azure Resource Manager
 *
 *   {@literal @}AzureHost(AzureEnvironment.Endpoint.RESOURCE_MANAGER)
 *   interface VirtualMachinesService {
 *     {@literal @}GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}")
 *     VirtualMachine getByResourceGroup(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName, @PathParam("subscriptionId") String subscriptionId);
 *   }
 *
 * Example 2: Azure Key Vault
 *
 *   {@literal @}AzureHost(AzureEnvironment.Endpoint.KEY_VAULT)
 *   interface KeyVaultService {
 *     {@literal @}GET("secrets/{secretName}")
 *     Secret getSecret(@HostParam String vaultName, @PathParam("secretName") String secretName);
 *   }
 */
@Target(value = {TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AzureHost {
    /**
     * The endpoint that all REST APIs within the Swagger interface will send their requests to.
     * @return The endpoint that all REST APIs within the Swagger interface will send their requests
     *      to.
     */
    String value() default "";

    /**
     * The endpoint that all REST APIs within the Swagger interface will send their requests to.
     * @return The endpoint that all REST APIs within the Swagger interface will send their requests
     *      to.
     */
    AzureEnvironment.Endpoint endpoint() default Endpoint.RESOURCE_MANAGER;
}
