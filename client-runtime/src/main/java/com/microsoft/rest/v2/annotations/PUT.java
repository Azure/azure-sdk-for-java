/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the parametrized relative path for an HTTP PUT method.
 *
 * The required value can be either a relative path or an absolute path. When it's
 * an absolute path, it must start with a protocol or a parametrized segment.
 * (Otherwise the parse cannot tell if it's absolute or relative)
 *
 * For more details on format of "value" field and associated rules refer {@link com.microsoft.rest.v2.annotations.Doc_Http_Verb_Annotation_Value_URI_Template}.
 *
 * Example 1: relative path segments
 *
 *  {@literal @}PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}")
 *  VirtualMachine createOrUpdate(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName, @PathParam("subscriptionId") String subscriptionId, @BodyParam VirtualMachine vm);
 *
 * Example 2: absolute path segment
 *
 *  {@literal @}PUT({vaultBaseUrl}/secrets/{secretName})
 *  Secret createOrUpdate(@PathParam("vaultBaseUrl" encoded = true) String vaultBaseUrl, @PathParam("secretName") String secretName, @BodyParam SecretCreateParameters secret);
 */
@Target({ElementType.METHOD})            // The context in which annotation is applicable i.e. this annotation (PUT) can be applied only to methods
@Retention(RetentionPolicy.RUNTIME)      // Record this annotation in the class file and make it available during runtime.
public @interface PUT {
    /**
     * Get the relative path of the annotated method's PUT URL.
     * @return The relative path of the annotated method's PUT URL.
     */
    String value();
}
