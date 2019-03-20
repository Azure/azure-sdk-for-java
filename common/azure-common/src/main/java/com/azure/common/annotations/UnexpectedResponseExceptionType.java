/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.annotations;

import com.azure.common.http.rest.RestException;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The error type that will be thrown or returned when an unexpected status code is returned from an REST API.
 *
 * <p><strong>Example:</strong></p>
 *
 * <pre>
 * {@literal @}UnexpectedResponseExceptionType(MyCustomException.class)
 * {@literal @}POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.CustomerInsights/hubs/{hubName}/images/getEntityTypeImageUploadUrl")
 *  void getUploadUrlForEntityType(@Path("resourceGroupName") String resourceGroupName, @Path("hubName") String hubName, @Path("subscriptionId") String subscriptionId, @Body GetImageUploadUrlInputInner parameters);
 * </pre>
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface UnexpectedResponseExceptionType {
    /**
     * The type of RestException that should be thrown/returned when the API returns an unrecognized
     * status code.
     * @return The type of RestException that should be thrown/returned.
     */
    Class<? extends RestException> value();
}