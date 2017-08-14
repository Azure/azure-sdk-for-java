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
 * List of static headers sent to the API.
 *
 * Headers are comma separated strings, with each in the format of
 * "header name: header value1,header value2".
 *
 * Example:
 *   {@literal @}Headers({ "Content-Type: application/json; charset=utf-8", "accept-language: en-US" })
 *   {@literal @}POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.CustomerInsights/hubs/{hubName}/images/getEntityTypeImageUploadUrl")
 *   void getUploadUrlForEntityType(@Path("resourceGroupName") String resourceGroupName, @Path("hubName") String hubName, @Path("subscriptionId") String subscriptionId, @Body GetImageUploadUrlInputInner parameters);
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Headers {
    /**
     * List of static headers.
     * @return List of static headers.
     */
    String[] value();
}