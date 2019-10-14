// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * HTTP PATCH method annotation describing the parameterized relative path to a REST endpoint for resource update.
 *
 * <p>The required value can be either a relative path or an absolute path. When it's an absolute path, it must start
 * with a protocol or a parameterized segment (Otherwise the parse cannot tell if it's absolute or relative).</p>
 *
 * <p><strong>Example 1: Relative path segments</strong></p>
 *
 * <pre>
 * {@literal @}Patch("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/
 * Microsoft.Compute/virtualMachines/{vmName}")
 *  VirtualMachine patch(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String
 *  vmName, @PathParam("subscriptionId") String subscriptionId, @BodyParam VirtualMachineUpdateParameters
 *  updateParameters); </pre>
 *
 * <p><strong>Example 2: Absolute path segment</strong></p>
 *
 * <pre>
 * {@literal @}Patch({vaultBaseUrl}/secrets/{secretName})
 *  Secret patch(@PathParam("vaultBaseUrl" encoded = true) String vaultBaseUrl, @PathParam("secretName") String
 *  secretName, @BodyParam SecretUpdateParameters updateParameters); </pre>
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Patch {
    /**
     * Get the relative path of the annotated method's PATCH URL.
     * @return The relative path of the annotated method's PATCH URL.
     */
    String value();
}
