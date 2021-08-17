// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpMethod;
import com.azure.core.util.logging.ClientLogger;

import java.util.Set;

/**
 * The interface for determining the redirect strategy used in {@link RedirectPolicy}.
 */
public interface RedirectStrategy {
    ClientLogger logger = new ClientLogger(RedirectStrategy.class);

    /**
     * Max number of redirect attempts to be made.
     *
     * @return The max number of redirect attempts.
     */
    int getMaxAttempts();

    /**
     * @return the value of the header, or null if the header doesn't exist in the response.
     */
    String getLocationHeader();

    /**
     * @return the set of redirect allowed methods.
     */
    Set<HttpMethod> getAllowedMethods();

    /**
     * Determines if the url should be redirected between each try.
     *
     * @param redirectUrl the redirect url present in the response headers
     * @param tryCount redirect attempts so far
     * @param maxRedirects maximum number of redirects allowed
     * @param attemptedRedirectUrls attempted redirect locations so far
     *
     * @return {@code true} if the request should be redirected, {@code false}
     * otherwise
     */
    boolean shouldAttemptRedirect(String redirectUrl, int tryCount, int maxRedirects,
                                  Set<String> attemptedRedirectUrls);
}
