// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.logging.LoggingKeys;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import java.net.HttpURLConnection;
import java.util.EnumSet;
import java.util.Set;

/**
 * The {@code DefaultRedirectStrategy} class is an implementation of the {@link RedirectStrategy} interface. This
 * strategy uses the provided maximum retry attempts, header name to look up redirect URL value for, HTTP methods and
 * a known set of redirect status response codes (301, 302, 307, 308) to determine if a request should be redirected.
 *
 * <p>This class is useful when you need to handle HTTP redirects. It ensures that the requests are redirected
 * correctly based on the response status code and the maximum number of redirect attempts.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code DefaultRedirectStrategy} is created with a maximum of 3 redirect attempts,
 * "Location" as the header name to locate the redirect URL, and GET and HEAD as the allowed methods for performing
 * the redirect. The strategy is then used in a {@code RedirectPolicy} which can be added to the pipeline. For a request
 * sent by the pipeline, if the server responds with a redirect status code and provides a "Location" header,
 * the request will be redirected up to 3 times as needed.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.DefaultRedirectStrategy.constructor -->
 * <pre>
 * DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy&#40;3, &quot;Location&quot;,
 *     EnumSet.of&#40;HttpMethod.GET, HttpMethod.HEAD&#41;&#41;;
 * RedirectPolicy redirectPolicy = new RedirectPolicy&#40;redirectStrategy&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.DefaultRedirectStrategy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.RedirectStrategy
 * @see com.azure.core.http.policy.RedirectPolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 */
public final class DefaultRedirectStrategy implements RedirectStrategy {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultRedirectStrategy.class);

    private static final int DEFAULT_MAX_REDIRECT_ATTEMPTS = 3;
    private static final int PERMANENT_REDIRECT_STATUS_CODE = 308;
    private static final int TEMPORARY_REDIRECT_STATUS_CODE = 307;
    private static final Set<HttpMethod> DEFAULT_REDIRECT_ALLOWED_METHODS = EnumSet.of(HttpMethod.GET, HttpMethod.HEAD);

    private static final String REDIRECT_URLS_KEY = "redirectUrls";

    private final int maxAttempts;
    private final HttpHeaderName locationHeader;
    private final Set<HttpMethod> allowedRedirectHttpMethods;

    /**
     * Creates an instance of {@link DefaultRedirectStrategy} with a maximum number of redirect attempts 3,
     * header name "Location" to locate the redirect url in the response headers and {@link HttpMethod#GET}
     * and {@link HttpMethod#HEAD} as allowed methods for performing the redirect.
     */
    public DefaultRedirectStrategy() {
        this(DEFAULT_MAX_REDIRECT_ATTEMPTS, HttpHeaderName.LOCATION, DEFAULT_REDIRECT_ALLOWED_METHODS);
    }

    /**
     * Creates an instance of {@link DefaultRedirectStrategy} with the provided number of redirect attempts and
     * default header name "Location" to locate the redirect url in the response headers and {@link HttpMethod#GET}
     * and {@link HttpMethod#HEAD} as allowed methods for performing the redirect.
     *
     * @param maxAttempts The max number of redirect attempts that can be made.
     * @throws IllegalArgumentException if {@code maxAttempts} is less than 0.
     */
    public DefaultRedirectStrategy(int maxAttempts) {
        this(maxAttempts, HttpHeaderName.LOCATION, DEFAULT_REDIRECT_ALLOWED_METHODS);
    }

    /**
     * Creates an instance of {@link DefaultRedirectStrategy}.
     *
     * @param maxAttempts The max number of redirect attempts that can be made.
     * @param locationHeader The header name containing the redirect URL.
     * @param allowedMethods The set of {@link HttpMethod} that are allowed to be redirected.
     * @throws IllegalArgumentException if {@code maxAttempts} is less than 0.
     */
    public DefaultRedirectStrategy(int maxAttempts, String locationHeader, Set<HttpMethod> allowedMethods) {
        this(maxAttempts, validateLocationHeader(locationHeader), validateAllowedMethods(allowedMethods));
    }

    private DefaultRedirectStrategy(int maxAttempts, HttpHeaderName locationHeader, Set<HttpMethod> allowedMethods) {
        if (maxAttempts < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Max attempts cannot be less than 0."));
        }
        this.maxAttempts = maxAttempts;
        this.locationHeader = locationHeader;
        this.allowedRedirectHttpMethods = allowedMethods;
    }

    private static HttpHeaderName validateLocationHeader(String locationHeader) {
        if (CoreUtils.isNullOrEmpty(locationHeader)) {
            LOGGER.log(LogLevel.INFORMATIONAL,
                () -> "'locationHeader' provided as null will be defaulted to " + HttpHeaderName.LOCATION);
            return HttpHeaderName.LOCATION;
        } else {
            return HttpHeaderName.fromString(locationHeader);
        }
    }

    private static Set<HttpMethod> validateAllowedMethods(Set<HttpMethod> allowedMethods) {
        if (CoreUtils.isNullOrEmpty(allowedMethods)) {
            LOGGER.log(LogLevel.INFORMATIONAL,
                () -> "'allowedMethods' provided as null will be defaulted to " + DEFAULT_REDIRECT_ALLOWED_METHODS);
            return DEFAULT_REDIRECT_ALLOWED_METHODS;
        } else {
            return EnumSet.copyOf(allowedMethods);
        }
    }

    @Override
    public boolean shouldAttemptRedirect(HttpPipelineCallContext context, HttpResponse httpResponse, int tryCount,
        Set<String> attemptedRedirectUrls) {

        if (isValidRedirectStatusCode(httpResponse.getStatusCode())
            && isValidRedirectCount(tryCount)
            && isAllowedRedirectMethod(httpResponse.getRequest().getHttpMethod())) {
            String redirectUrl = httpResponse.getHeaderValue(locationHeader);
            if (redirectUrl != null && !alreadyAttemptedRedirectUrl(redirectUrl, attemptedRedirectUrls)) {
                LOGGER.atVerbose()
                    .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
                    .addKeyValue(REDIRECT_URLS_KEY, attemptedRedirectUrls::toString)
                    .log("Redirecting.");
                attemptedRedirectUrls.add(redirectUrl);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public HttpRequest createRedirectRequest(HttpResponse httpResponse) {
        return httpResponse.getRequest().setUrl(httpResponse.getHeaderValue(locationHeader));
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Check if the redirect url provided in the response headers is already attempted.
     *
     * @param redirectUrl the redirect url provided in the response header.
     * @param attemptedRedirectUrls the set containing a list of attempted redirect locations.
     * @return {@code true} if the redirectUrl provided in the response header is already being attempted for redirect
     * , {@code false} otherwise.
     */
    private boolean alreadyAttemptedRedirectUrl(String redirectUrl, Set<String> attemptedRedirectUrls) {
        if (attemptedRedirectUrls.contains(redirectUrl)) {
            LOGGER.atError()
                .addKeyValue(LoggingKeys.REDIRECT_URL_KEY, redirectUrl)
                .log("Request was redirected more than once to the same URL.");

            return true;
        }
        return false;
    }

    /**
     * Check if the attempt count of the redirect is less than the {@code maxAttempts}
     *
     * @param tryCount the try count for the HTTP request associated to the HTTP response.
     * @return {@code true} if the {@code tryCount} is greater than the {@code maxAttempts}, {@code false} otherwise.
     */
    private boolean isValidRedirectCount(int tryCount) {
        if (tryCount >= getMaxAttempts()) {
            LOGGER.atError().addKeyValue("maxAttempts", getMaxAttempts()).log("Redirect attempts have been exhausted.");

            return false;
        }
        return true;
    }

    /**
     * Check if the request http method is a valid redirect method.
     *
     * @param httpMethod the http method of the request.
     * @return {@code true} if the request {@code httpMethod} is a valid http redirect method, {@code false} otherwise.
     */
    private boolean isAllowedRedirectMethod(HttpMethod httpMethod) {
        if (allowedRedirectHttpMethods.contains(httpMethod)) {
            return true;
        } else {
            LOGGER.atError()
                .addKeyValue(LoggingKeys.HTTP_METHOD_KEY, httpMethod)
                .log("Request was redirected from an invalid redirect allowed method.");

            return false;
        }
    }

    /**
     * Checks if the incoming request status code is a valid redirect status code.
     *
     * @param statusCode the status code of the incoming request.
     * @return {@code true} if the request {@code statusCode} is a valid http redirect method, {@code false} otherwise.
     */
    private boolean isValidRedirectStatusCode(int statusCode) {
        return statusCode == HttpURLConnection.HTTP_MOVED_TEMP
            || statusCode == HttpURLConnection.HTTP_MOVED_PERM
            || statusCode == PERMANENT_REDIRECT_STATUS_CODE
            || statusCode == TEMPORARY_REDIRECT_STATUS_CODE;
    }
}
