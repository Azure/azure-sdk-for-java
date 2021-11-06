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
 * <!-- src_embed com.azure.core.annotation.FormData.class -->
 * <pre>
 * &#64;Post&#40;&quot;spellcheck&quot;&#41;
 * Mono&lt;Response&lt;ResponseBody&gt;&gt; spellChecker&#40&#64;Header&#40&quot;X-BingApis-SDK&quot;&#41;
 *     String xBingApisSDK, &#64;QueryParam&#40&quot;UserId&quot;&#41; String userId,
 *     &#64;FormData&#40&quot;Text&quot;&#41; String text&#41;;</pre>
 * <!-- end com.azure.core.annotation.FormData.class -->
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface FormData {
    /**
     * @return The name of the key in a key value pair as part of the form data.
     */
    String value();

    /**
     * Optional value to be provided if {@link FormData#value()} represents a file's contents.
     *
     * @return The name of the file that {@link FormData#value()} represents.
     */
    String filename() default "";
}
