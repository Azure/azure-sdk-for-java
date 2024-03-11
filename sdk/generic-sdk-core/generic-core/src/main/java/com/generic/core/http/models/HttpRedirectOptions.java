// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.http.Response;
import com.generic.core.http.policy.RequestRedirectCondition;
import com.generic.core.models.HeaderName;
import com.generic.core.util.ClientLogger;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Options to configure the redirect policy's behavior.
 */
public final class HttpRedirectOptions {
    private static final ClientLogger LOGGER = new ClientLogger(HttpRedirectOptions.class);
    private final int maxAttempts;
    private final Set<HttpMethod> allowedRedirectHttpMethods;
    private final HeaderName locationHeader;
    private Predicate<RequestRedirectCondition> shouldRedirectCondition;

    /**
     * Creates an instance of {@link HttpRedirectOptions} with values for {@code maxAttempts}
     * defaulting to 3, if not specified, and the default set of allowed HTTP methods {@link HttpMethod#GET}
     * and {@link HttpMethod#HEAD} and the default header name "Location" to locate the redirect url in the response
     * headers.
     *
     * @param maxAttempts The maximum number of redirect attempts to be made.
     * @param allowedRedirectHttpMethods The set of HTTP methods that are allowed to be redirected.
     * @param locationHeader The header name containing the redirect URL.
     * @throws IllegalArgumentException if {@code maxAttempts} is less than 0.
     */
    public HttpRedirectOptions(int maxAttempts, HeaderName locationHeader, Set<HttpMethod> allowedRedirectHttpMethods) {
        if (maxAttempts < 0) {
            LOGGER.atVerbose()
                .log(() -> "Max attempts cannot be less than 0. Using 3 redirect attempts as the maximum.");
            maxAttempts = 3;
        }
        this.maxAttempts = maxAttempts;
        this.allowedRedirectHttpMethods = allowedRedirectHttpMethods == null
            ? Collections.emptySet()
            : Collections.unmodifiableSet(allowedRedirectHttpMethods);
        this.locationHeader = locationHeader;
    }

    /**
     * Get the maximum number of redirect attempts to be made.
     * @return the max redirect attempts.
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Gets the predicate that determines if a redirect should be attempted.
     * <p>
     * If null, the default behavior is to retry HTTP responses with status codes 408, 429, and any 500 status code that
     * isn't 501 or 505. And to retry any {@link Exception}.
     *
     * @return The predicate that determines if a retry should be attempted.
     */
    public Predicate<RequestRedirectCondition> getShouldRedirectCondition() {
        return shouldRedirectCondition;
    }

    /**
     * Sets the predicate that determines if a retry should be attempted.
     * <p>
     * If null, the default behavior is to retry HTTP responses with status codes 408, 429, and any 500 status code that
     * isn't 501 or 505. And to retry any {@link Exception}.
     *
     * @param shouldRedirectCondition The predicate that determines if a retry should be attempted for the given
     * {@link Response}.
     * @return The updated {@link HttpRedirectOptions} object.
     */
    public HttpRedirectOptions setShouldRedirectCondition(Predicate<RequestRedirectCondition> shouldRedirectCondition) {
        this.shouldRedirectCondition = shouldRedirectCondition;
        return this;
    }

    /**
     * Gets the set of HTTP methods that are allowed to be redirected.
     * <p>
     * If null, the default behavior is to allow GET and HEAD HTTP methods to be redirected.
     *
     * @return The set of HTTP methods that are allowed to be redirected.
     */
    public Set<HttpMethod> getAllowedRedirectHttpMethods() {
        return allowedRedirectHttpMethods;
    }

    /**
     * Gets the header name containing the redirect URL.
     *
     * @return The header name containing the redirect URL.
     */
    public HeaderName getLocationHeader() {
        return locationHeader;
    }
}
