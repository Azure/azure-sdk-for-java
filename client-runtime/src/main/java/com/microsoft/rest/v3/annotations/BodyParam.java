/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to annotate a parameter to send to a REST endpoint as HTTP Request content.
 *
 * If the parameter type extends InputStream, this payload is streamed to server through
 * "application/octet-stream". Otherwise, the body is serialized first and sent as
 * "application/json" or "application/xml", based on the serializer.
 *
 * Example 1: put JSON
 *
 *   {@literal @}PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}")
 *   VirtualMachine createOrUpdate(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") VirtualMachine vm);
 *
 * Example 2: stream
 *
 *   {@literal @}POST("formdata/stream/uploadfile")
 *   void uploadFileViaBody(@BodyParam("application/octet-stream") FileInputStream fileContent);
 *
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface BodyParam {
    /**
     * @return the Content-Type that the body should be treated as
     */
    String value();
}