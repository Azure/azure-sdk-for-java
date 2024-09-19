// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.http.pipeline.HttpRequestRedirectCondition;
import io.clientcore.core.util.ClientLogger;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * Options to configure the redirect policy's behavior.
 */
public final class HttpRedirectOptions {
    private static final ClientLogger LOGGER = new ClientLogger(HttpRedirectOptions.class);
    private final int maxAttempts;
    private final EnumSet<HttpMethod> allowedRedirectHttpMethods;
    private final HttpHeaderName locationHeader;
    private Predicate<HttpRequestRedirectCondition> shouldRedirectCondition;

    /**
     * Creates an instance of {@link HttpRedirectOptions}.
     *
     * @param maxAttempts The maximum number of redirect attempts to be made.
     * @param allowedRedirectHttpMethods The set of HTTP methods that are allowed to be redirected.
     * @param locationHeader The header name containing the redirect URI.
     * @throws IllegalArgumentException if {@code maxAttempts} is less than 0.
     */
    public HttpRedirectOptions(int maxAttempts, HttpHeaderName locationHeader, EnumSet<HttpMethod> allowedRedirectHttpMethods) {
        if (maxAttempts < 0) {
            throw LOGGER.atError().log(null,
                new IllegalArgumentException("Max attempts cannot be less than 0."));
        }
        this.maxAttempts = maxAttempts;
        this.allowedRedirectHttpMethods = allowedRedirectHttpMethods == null
            ? EnumSet.noneOf(HttpMethod.class)
            : allowedRedirectHttpMethods;
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
     * If null, the default behavior is to redirect HTTP responses with status response code (301, 302, 307, 308).
     *
     * @return The predicate that determines if a redirect should be attempted.
     */
    public Predicate<HttpRequestRedirectCondition> getShouldRedirectCondition() {
        return shouldRedirectCondition;
    }

    /**
     * Sets the predicate that determines if a redirect should be attempted.
     * <p>
     * If null, the default behavior is to redirect HTTP responses with status response code (301, 302, 307, 308).
     *
     * @param shouldRedirectCondition The predicate that determines if a redirect should be attempted for the given
     * {@link Response}.
     * @return The updated {@link HttpRedirectOptions} object.
     */
    public HttpRedirectOptions setShouldRedirectCondition(Predicate<HttpRequestRedirectCondition> shouldRedirectCondition) {
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
    public EnumSet<HttpMethod> getAllowedRedirectHttpMethods() {
        return allowedRedirectHttpMethods;
    }

    /**
     * Gets the header name containing the redirect URI.
     * <p>
     * If null, the default behavior is to use the "Location" header to locate the redirect URI in the response headers.
     *
     * @return The header name containing the redirect URI.
     */
    public HttpHeaderName getLocationHeader() {
        return locationHeader;
    }
}
