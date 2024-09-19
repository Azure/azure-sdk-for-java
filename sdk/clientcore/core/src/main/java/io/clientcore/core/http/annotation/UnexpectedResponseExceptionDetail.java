// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.annotation;

import io.clientcore.core.http.exception.HttpExceptionType;
import io.clientcore.core.http.exception.HttpResponseException;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The {@link HttpExceptionType} that is thrown or returned when one of the status codes is returned from a REST API. Multiple
 * annotations can be used. When no codes are listed that exception is always thrown or returned if it is reached
 * during evaluation, this should be treated as a default case. If no default case is annotated the fall through
 * exception is {@link HttpResponseException}.
 *
 * <p><strong>Example:</strong></p>
 *
 * <!-- src_embed io.clientcore.core.http.annotation.UnexpectedResponseExceptionType.class -->
 * <pre>
 * &#47;&#47; Set it so that all response exceptions use a custom exception type.
 *
 * &#64;UnexpectedResponseExceptionDetail&#40;exceptionBodyClass = MyCustomExceptionBody.class&#41;
 * &#64;HttpRequestInformation&#40;method = HttpMethod.POST, path = &quot;subscriptions&#47;&#123;subscriptionId&#125;&#47;resourceGroups&#47;&quot;
 *     + &quot;&#123;resourceGroupName&#125;&#47;providers&#47;Microsoft.CustomerInsights&#47;hubs&#47;&#123;hubName&#125;&#47;images&#47;&quot;
 *     + &quot;getEntityTypeImageUploadUri&quot;,
 *     returnValueWireType = void.class&#41;
 * void singleExceptionType&#40;&#64;PathParam&#40;&quot;resourceGroupName&quot;&#41; String resourceGroupName,
 *     &#64;PathParam&#40;&quot;hubName&quot;&#41; String hubName, &#64;PathParam&#40;&quot;subscriptionId&quot;&#41; String subscriptionId,
 *     &#64;BodyParam&#40;&quot;application&#47;json&quot;&#41; RequestBody parameters&#41;;
 *
 *
 * &#47;&#47; Set it so 404 uses a specific exception type while others use a generic exception type.
 *
 * &#64;UnexpectedResponseExceptionDetail&#40;statusCode = &#123;404&#125;, exceptionBodyClass = MyCustomExceptionBody.class&#41;
 * &#64;HttpRequestInformation&#40;method = HttpMethod.POST, path = &quot;subscriptions&#47;&#123;subscriptionId&#125;&#47;resourceGroups&#47;&quot;
 *     + &quot;&#123;resourceGroupName&#125;&#47;providers&#47;Microsoft.CustomerInsights&#47;hubs&#47;&#123;hubName&#125;&#47;images&#47;&quot;
 *     + &quot;getEntityTypeImageUploadUri&quot;,
 *     returnValueWireType = void.class&#41;
 * void multipleExceptionTypes&#40;&#64;PathParam&#40;&quot;resourceGroupName&quot;&#41; String resourceGroupName,
 *     &#64;PathParam&#40;&quot;hubName&quot;&#41; String hubName,
 *     &#64;PathParam&#40;&quot;subscriptionId&quot;&#41; String subscriptionId,
 *     &#64;BodyParam&#40;&quot;application&#47;json&quot;&#41; RequestBody parameters&#41;;
 *
 * &#47;&#47; If multiple annotations share the same HTTP status code or there is multiple default annotations the
 * &#47;&#47; exception, the last annotation in the top to bottom order will be used &#40;so the bottom most annotation&#41;.
 * </pre>
 * <!-- end io.clientcore.core.http.annotation.UnexpectedResponseExceptionType.class -->
 */
@Retention(RUNTIME)
@Target(METHOD)
@Repeatable(UnexpectedResponseExceptionDetails.class)
public @interface UnexpectedResponseExceptionDetail {
    /**
     * The name of the {@link HttpExceptionType} of an {@link HttpResponseException} that should be thrown/returned when
     * the API returns an unrecognized status code.
     *
     * @return The {@link HttpExceptionType} that should be thrown/returned, represented as a {@link String}.
     */
    String exceptionTypeName() default "";

    /**
     * HTTP status codes which trigger the {@link HttpResponseException} to be thrown or returned. If no status codes
     * are listed the exception is always thrown or returned.
     *
     * @return The HTTP status codes that trigger the exception to be thrown or returned.
     */
    int[] statusCode() default {};

    /**
     * The class to decode the body of the HTTP response that accompanies an {@link HttpResponseException} to be thrown
     * or returned.
     *
     * @return The class to decode the body of the HTTP response that accompanies an {@link HttpResponseException} to be
     * thrown or returned.
     */
    Class<?> exceptionBodyClass() default Object.class;
}
