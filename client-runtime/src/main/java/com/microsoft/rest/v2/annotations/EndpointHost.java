/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

@Target(value={PARAMETER,METHOD, TYPE})    // The context in which annotation is applicable i.e. this annotation (EndpointHost) can be applied to method, method parameter and interface.
@Retention(RetentionPolicy.RUNTIME)        // Record this annotation in the class file and make it available during runtime.
public @interface EndpointHost {
    String value() default "";
}

/**
 * The applicability of this annotation is limited to variable with name {host} in the URI template.
 *
 *   When this annotation is applied to a method or interface level, value must be defined statically.
 *   When this annotation is applied to a method parameter level, the parameter value will be it's value
 *
 * TODO: Decide whether we need all three of below or subset of it.
 *
 *  [1] Annotation applied to interface level.
 *
 *     @EndpointHost("manage.windowsazure.com")
 *     interface VirtualMachinesService {
 *        @GET("https://{host}/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}")
 *        VirtualMachine getByResourceGroup(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName, @PathParam("subscriptionId") String subscriptionId);
 *     }
 *
 *  [2] Annotation applied to method level.
 *
 *    interface VirtualMachines {
 *        @EndpointHost("manage.windowsazure.com")
 *        @GET("https://{host}/subscriptions/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}")
 *        VirtualMachine getByResourceGroup(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName, @PathParam("subscriptionId") String subscriptionId);
 *    }
 *
 *  [3] Annotation applied to method parameter level.
 *
 *    interface VirtualMachines {
 *        @GET("https://{host}/subscriptions/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}")
 *        VirtualMachine getByResourceGroup(@EndpointHost String host, @PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName, @PathParam("subscriptionId") String subscriptionId);
 *   }
 *
 * Note:
 *   When this annotation is present in multiple levels, the precedence is method_parameter_level > method_level > interface_level
 *   Annotation parsing engine will throw error if this annotation is applied to interface and/or method level and the static value is null or empty string.
 *   If URI template does not contain {host} this annotation is ignored (e.g. in the third case if parameter host has a value but {host} absent in the URI template then parameter is ignored)
 */
