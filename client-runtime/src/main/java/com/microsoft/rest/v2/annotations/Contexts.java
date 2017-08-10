/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * List of context properties to be sent down the pipeline.
 *
 * Example:
 *   {@literal @}Headers({ "Content-Type: application/json; charset=utf-8", "accept-language: en-US" })
 *   {@literal @}Contexts({ "logging: com.microsoft.azure.management.customerinsights.Images getUploadUrlForEntityType" })
 *   {@literal @}POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.CustomerInsights/hubs/{hubName}/images/getEntityTypeImageUploadUrl")
 *   void getUploadUrlForEntityType(@Path("resourceGroupName") String resourceGroupName, @Path("hubName") String hubName, @Path("subscriptionId") String subscriptionId, @Body GetImageUploadUrlInputInner parameters);
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Contexts {
    /**
     * List of static contexts.
     */
    String[] value();
}