/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.annotations;

import com.azure.common.exception.ServiceRequestException;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;

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
@Repeatable(UnexpectedResponseExceptionTypes.class)
public @interface UnexpectedResponseExceptionType {
    /**
     * The type of ServiceRequestException that should be thrown/returned when the API returns an unrecognized
     * status code.
     * @return The type of RestException that should be thrown/returned.
     */
    Class<? extends ServiceRequestException> value();

    /**
     * HTTP status codes which trigger the ServiceRequestException to be thrown/returned.
     * @return The HTTP status code that trigger the ServiceRequestException.
     */
    int[] code() default {};
}
