// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to annotate list of static headers sent to a REST endpoint.
 *
 * <p>
 * Headers are comma separated strings, with each in the format of "header name: header value1,header value2".
 * </p>
 *
 * <p>
 * <strong>Examples:</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.annotation.Headers.class -->
 * <pre>
 * &#64;Headers&#40;&#123;&quot;Content-Type: application&#47;json; charset=utf-8&quot;, &quot;accept-language: en-US&quot;&#125;&#41;
 * &#64;Post&#40;&quot;subscriptions&#47;&#123;subscriptionId&#125;&#47;resourceGroups&#47;&#123;resourceGroupName&#125;&#47;providers&#47;Microsoft.CustomerInsights&#47;&quot;
 *     + &quot;hubs&#47;&#123;hubName&#125;&#47;images&#47;getEntityTypeImageUploadUrl&quot;&#41;
 * void getUploadUrlForEntityType&#40;&#64;PathParam&#40;&quot;resourceGroupName&quot;&#41; String resourceGroupName,
 *     &#64;PathParam&#40;&quot;hubName&quot;&#41; String hubName,
 *     &#64;PathParam&#40;&quot;subscriptionId&quot;&#41; String subscriptionId,
 *     &#64;BodyParam&#40;&quot;application&#47;json&quot;&#41; RequestBody parameters&#41;;
 * </pre>
 * <!-- end com.azure.core.annotation.Headers.class -->
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Headers {
    /**
     * List of static headers.
     *
     * @return List of static headers.
     */
    String[] value();
}
