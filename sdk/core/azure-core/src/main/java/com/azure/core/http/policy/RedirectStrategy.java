// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;

import java.net.HttpURLConnection;
import java.util.Set;

/**
 * The interface for determining the retry strategy used in {@link RetryPolicy}.
 */
public interface RedirectStrategy {


    ClientLogger logger = new ClientLogger(RedirectStrategy.class);

    /**
     * Max number of retry attempts to be make.
     *
     * @return The max number of retry attempts.
     */
    int getMaxRetries();
    String getLocationHeader();
    Set<HttpMethod> getRedirectableMethods();

    /**
     *
     * @param
     *
     */
    /**
     * Determines if the url should be redirected between each retry.
     *
     * @param responseHeaders the ongoing request headers
     * @param tryCount redirect retries so far
     * @param attemptedRedirectLocations attempted redirect retries locations so far
     *
     * @return {@code true} if the request should be redirected, {@code false}
     * otherwise
     */
    boolean shouldAttemptRedirect(HttpHeaders responseHeaders, int tryCount, Set<String> attemptedRedirectLocations);

}
