// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * HTTP GET method annotation describing the parameterized relative path to a REST endpoint for resource retrieval.
 *
 * <p>The required value can be either a relative path or an absolute path. When it's an absolute path, it must start
 * with a protocol or a parameterized segment (otherwise the parse cannot tell if it's absolute or relative).</p>
 *
 * <p><strong>Example 1: Relative path segments</strong></p>
 *
 * <pre>
 * {@literal @}Get("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft
 * .Compute/virtualMachines/{vmName}")
 *  VirtualMachine getByResourceGroup(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String
 *  vmName, @PathParam("subscriptionId") String subscriptionId);</pre>
 *
 * <p><strong>Example 2: Absolute path segment</strong></p>
 *
 * <pre>
 * {@literal @}Get({nextLink})
 * {@literal List<VirtualMachine>} listNext(@PathParam("nextLink") String nextLink);</pre>
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Get {
    /**
     * Get the relative path of the annotated method's GET URL.
     * @return The relative path of the annotated method's GET URL.
     */
    String value();
}
