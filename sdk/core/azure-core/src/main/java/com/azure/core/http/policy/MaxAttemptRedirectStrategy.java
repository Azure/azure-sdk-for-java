// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpMethod;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A default implementation of {@link RedirectStrategy} that uses the provided try count, and status code
 * to determine if request should be redirected.
 */
public class MaxAttemptRedirectStrategy implements RedirectStrategy {

    // Based on Stamp specific redirects design doc
    static final int MAX_REDIRECT_ATTEMPTS = 10;
    private static final String LOCATION_HEADER_NAME = "Location";

    private int maxAttempts;
    private final String locationHeader;
    private final Set<HttpMethod> redirectMethods;

    /**
     * Creates an instance of {@link MaxAttemptRedirectStrategy}.
     *
     * @param maxAttempts The max number of redirect attempts that can be made.
     * @param locationHeader The header name containing the redirect URL.
     * @param allowedMethods The set of {@link HttpMethod} that are allowed to be redirected.
     *
     * @throws NullPointerException if {@code locationHeader} is {@code null}.
     */
    public MaxAttemptRedirectStrategy(int maxAttempts, String locationHeader, Set<HttpMethod> allowedMethods) {
        this.maxAttempts = maxAttempts;
        this.locationHeader = Objects.requireNonNull(locationHeader, "'locationHeader' cannot be null.");
        this.redirectMethods = allowedMethods;
    }

    /**
     * Creates an instance of {@link MaxAttemptRedirectStrategy}.
     *
     * @param maxAttempts The max number of redirect attempts that can be made.
     */
    public MaxAttemptRedirectStrategy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        if (maxAttempts < 0){
            ClientLogger logger = new ClientLogger(MaxAttemptRedirectStrategy.class);
            throw logger.logExceptionAsError(new IllegalArgumentException("Max attempts cannot be less than 0."));
        }
        this.locationHeader = LOCATION_HEADER_NAME;
        this.redirectMethods = new HashSet<HttpMethod>() {
            {
                add(HttpMethod.GET);
                add(HttpMethod.HEAD);
            }
        };
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }

    @Override
    public boolean shouldAttemptRedirect(String redirectUrl, int tryCount, int maxRedirects,
                                         Set<String> attemptedRedirectUrls) {
        if (tryCount >= maxRedirects) {
            logger.error(String.format("Request has been redirected more than %d times.", MAX_REDIRECT_ATTEMPTS));
            return false;
        }

        if (attemptedRedirectUrls.contains(redirectUrl)) {
            logger.error(String.format("Request was redirected more than once to: %s", redirectUrl));
            return false;
        }
        return true;
    }

    @Override
    public String getLocationHeader() {
        return locationHeader;
    }

    @Override
    public Set<HttpMethod> getAllowedMethods() {
        return redirectMethods;
    }
}
