// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * A default implementation of {@link RedirectStrategy} that uses the status code to determine if to redirect
 * between each retry attempt.
 */
public class DefaultRedirectStrategy implements RedirectStrategy {

    // Based on Stamp specific redirects design doc
    static final int MAX_REDIRECT_RETRIES = 10;
    private static final String LOCATION_HEADER_NAME = "Location";

    private final int maxRetries;
    private final String locationHeader;
    private final Set<HttpMethod> redirectMethods;

    /**
     * Creates an instance of {@link DefaultRedirectStrategy}.
     *
     * @param maxRetries The max number of redirect attempts that can be made.
     */
    public DefaultRedirectStrategy(int maxRetries, String locationHeader, Set<HttpMethod> redirectableMethods) {
        this.maxRetries = maxRetries;
        this.locationHeader = locationHeader;
        this.redirectMethods = redirectableMethods;
    }

    /**
     * Creates an instance of {@link DefaultRedirectStrategy}.
     *
     * @param maxRetries The max number of redirect attempts that can be made.
     */
    public DefaultRedirectStrategy(int maxRetries) {
        if (maxRetries < 0) {
            ClientLogger logger = new ClientLogger(DefaultRedirectStrategy.class);
            throw logger.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        this.locationHeader = LOCATION_HEADER_NAME;
        this.redirectMethods = new HashSet<HttpMethod>() {
            {
                add(HttpMethod.GET);
                add(HttpMethod.HEAD);
            }
        };
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public boolean shouldAttemptRedirect(HttpHeaders responseHeaders, int tryCount,
                                         Set<String> attemptedRedirectLocations) {
        if (tryCount > MAX_REDIRECT_RETRIES) {
            logger.error(String.format("Request has been redirected more than %d times.", MAX_REDIRECT_RETRIES));
            return false;
        }
        if (attemptedRedirectLocations.contains(responseHeaders.get(LOCATION_HEADER_NAME))) {
            logger.error(String.format("Request was redirected more than once to: %s",
                responseHeaders.get(LOCATION_HEADER_NAME)));
            return false;
        }
        return true;
    }

    public String getLocationHeader() {
        return locationHeader;
    }

    public Set<HttpMethod> getRedirectableMethods() {
        return redirectMethods;
    }
}
