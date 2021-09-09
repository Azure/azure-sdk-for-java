// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A default implementation of {@link RedirectStrategy} that uses the provided maximum retry attempts,
 * header name to look up redirect url value for, http methods and a known set of
 * redirect status response codes (301, 302, 307, 308) to determine if request should be redirected.
 */
public final class DefaultRedirectStrategy implements RedirectStrategy {
    private final ClientLogger logger = new ClientLogger(DefaultRedirectStrategy.class);

    private static final int DEFAULT_MAX_REDIRECT_ATTEMPTS = 3;
    private static final String DEFAULT_REDIRECT_LOCATION_HEADER_NAME = "Location";
    private static final int PERMANENT_REDIRECT_STATUS_CODE = 308;
    private static final int TEMPORARY_REDIRECT_STATUS_CODE = 307;
    private static final Set<HttpMethod> DEFAULT_REDIRECT_ALLOWED_METHODS =
        new HashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.HEAD));

    private final int maxAttempts;
    private final String locationHeader;
    private final Set<HttpMethod> allowedRedirectHttpMethods;

    /**
     * Creates an instance of {@link DefaultRedirectStrategy} with a maximum number of redirect attempts 3,
     * header name "Location" to locate the redirect url in the response headers and {@link HttpMethod#GET}
     * and {@link HttpMethod#HEAD} as allowed methods for performing the redirect.
     */
    public DefaultRedirectStrategy() {
        this(DEFAULT_MAX_REDIRECT_ATTEMPTS, DEFAULT_REDIRECT_LOCATION_HEADER_NAME, DEFAULT_REDIRECT_ALLOWED_METHODS);
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
        this(maxAttempts, DEFAULT_REDIRECT_LOCATION_HEADER_NAME, DEFAULT_REDIRECT_ALLOWED_METHODS);
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
        if (maxAttempts < 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Max attempts cannot be less than 0."));
        }
        this.maxAttempts = maxAttempts;
        if (CoreUtils.isNullOrEmpty(locationHeader)) {
            logger.error("'locationHeader' provided as null will be defaulted to {}",
                DEFAULT_REDIRECT_LOCATION_HEADER_NAME);
            this.locationHeader = DEFAULT_REDIRECT_LOCATION_HEADER_NAME;
        } else {
            this.locationHeader = locationHeader;
        }
        if (CoreUtils.isNullOrEmpty(allowedMethods)) {
            logger.error("'allowedMethods' provided as null will be defaulted to {}", DEFAULT_REDIRECT_ALLOWED_METHODS);
            this.allowedRedirectHttpMethods = DEFAULT_REDIRECT_ALLOWED_METHODS;
        } else {
            this.allowedRedirectHttpMethods = allowedMethods;
        }
    }

    @Override
    public boolean shouldAttemptRedirect(HttpPipelineCallContext context,
                                         HttpResponse httpResponse, int tryCount,
                                         Set<String> attemptedRedirectUrls) {
        String redirectUrl = tryGetRedirectHeader(httpResponse.getHeaders(), getLocationHeader());

        if (isValidRedirectCount(tryCount)
            && redirectUrl != null
            && !alreadyAttemptedRedirectUrl(redirectUrl, attemptedRedirectUrls)
            && isValidRedirectStatusCode(httpResponse.getStatusCode())
            && isAllowedRedirectMethod(httpResponse.getRequest().getHttpMethod())) {
            logger.verbose("[Redirecting] Try count: {}, Attempted Redirect URLs: {}", tryCount,
                attemptedRedirectUrls.toString());
            attemptedRedirectUrls.add(redirectUrl);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public HttpRequest createRedirectRequest(HttpResponse httpResponse) {
        String responseLocation = tryGetRedirectHeader(httpResponse.getHeaders(), getLocationHeader());
        return httpResponse.getRequest().setUrl(responseLocation);
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /*
     * The header name to look up the value for the redirect url in response headers.
     *
     * @return the value of the header, or null if the header doesn't exist in the response.
     */
    String getLocationHeader() {
        return locationHeader;
    }

    /*
     * The {@link HttpMethod http methods} that are allowed to be redirected.
     *
     * @return the set of allowed redirect http methods.
     */
    Set<HttpMethod> getAllowedRedirectHttpMethods() {
        return allowedRedirectHttpMethods;
    }

    /**
     * Check if the redirect url provided in the response headers is already attempted.
     *
     * @param redirectUrl the redirect url provided in the response header.
     * @param attemptedRedirectUrls the set containing a list of attempted redirect locations.
     * @return {@code true} if the redirectUrl provided in the response header is already being attempted for redirect
     * , {@code false} otherwise.
     */
    private boolean alreadyAttemptedRedirectUrl(String redirectUrl,
                                                Set<String> attemptedRedirectUrls) {
        if (attemptedRedirectUrls.contains(redirectUrl)) {
            logger.error("Request was redirected more than once to: {}", redirectUrl);
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
            logger.error("Request has been redirected more than {} times.", getMaxAttempts());
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
        if (getAllowedRedirectHttpMethods().contains(httpMethod)) {
            return true;
        } else {
            logger.error("Request was redirected from an invalid redirect allowed method: {}", httpMethod);
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

    /**
     * Gets the redirect url from the response headers.
     *
     * @param headers the http response headers.
     * @param headerName the header name to look up value for.
     * @return the header value for the provided header name, {@code null} otherwise.
     */
    String tryGetRedirectHeader(HttpHeaders headers, String headerName) {
        String headerValue = headers.getValue(headerName);
        if (CoreUtils.isNullOrEmpty(headerValue)) {
            logger.error("Redirect url was null for header name: {}, request redirect was terminated.", headerName);
            return null;
        } else {
            return headerValue;
        }
    }
}
