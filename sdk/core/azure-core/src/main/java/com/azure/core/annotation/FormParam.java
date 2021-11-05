// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for form parameters to be sent to a REST API Request URI.
 *
 * <p><strong>Example 1:</strong></p>
 *
 * <pre>
 * {@literal @}POST("spellcheck")
 * {@literal Observable<Response<ResponseBody>>} spellChecker(@Header("X-BingApis-SDK") String xBingApisSDK, @Query
 * ("UserId") String userId, @FormParam("Text") String text);</pre>
 *
 * <p>The value of parameter text will be encoded and encoded value will be added to the form data sent to the API.</p>
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface FormParam {
    /**
     * @return The name of the key in a key value pair as part of the form data.
     */
    String value();

    /**
     * If set to {@code true}, this argument indicates that {@link FormParam#value() the value of this form
     * parameter} is already encoded, meaning that it should not be encoded again by an external actor. The default
     * value for this property is {@code false}.
     *
     * @return Whether this form parameter is already encoded.
     */
    boolean encoded() default false;
}
