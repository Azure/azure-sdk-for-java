// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * A redirect policy for Confidential Ledger that preserves the Authorization header on redirect.
 *
 * <p>The Confidential Ledger service uses a distributed network of nodes. Write operations (POST)
 * may be redirected from a secondary node to the primary node via HTTP 307/308 redirects. The
 * standard {@link com.azure.core.http.policy.RedirectPolicy RedirectPolicy} in azure-core strips
 * the Authorization header on redirect for security, and only allows GET/HEAD methods by default.
 * This policy addresses both issues for Confidential Ledger by:</p>
 *
 * <ul>
 *   <li>Following redirects for all HTTP methods including POST</li>
 *   <li>Preserving the Authorization header when the redirect stays within the same
 *       Confidential Ledger host (same-origin check)</li>
 * </ul>
 *
 * <p>The Authorization header is only preserved when the redirect target has the same host as the
 * original request. If the redirect goes to a different host, the Authorization header is stripped
 * for security.</p>
 *
 * <p>This class is intended to be used internally by the Confidential Ledger client builders.</p>
 */
public final class ConfidentialLedgerRedirectPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ConfidentialLedgerRedirectPolicy.class);
    private static final int MAX_REDIRECT_ATTEMPTS = 3;
    private static final int PERMANENT_REDIRECT_STATUS_CODE = 308;
    private static final int TEMPORARY_REDIRECT_STATUS_CODE = 307;

    /**
     * Creates an instance of {@link ConfidentialLedgerRedirectPolicy}.
     */
    public ConfidentialLedgerRedirectPolicy() {
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptRedirect(context, next, context.getHttpRequest(), 1, new HashSet<>());
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return attemptRedirectSync(context, next, context.getHttpRequest(), 1, new HashSet<>());
    }

    private Mono<HttpResponse> attemptRedirect(HttpPipelineCallContext context, HttpPipelineNextPolicy next,
        HttpRequest originalHttpRequest, int redirectAttempt, Set<String> attemptedRedirectUrls) {

        HttpRequest requestCopy = originalHttpRequest.copy();
        // Save the Authorization header before sending. Downstream policies or the HTTP client
        // may strip or modify it, so we must capture it here to re-add after redirect.
        String savedAuthHeader = requestCopy.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        context.setHttpRequest(requestCopy);

        return next.clone().process().flatMap(httpResponse -> {
            if (shouldRedirect(httpResponse, redirectAttempt, attemptedRedirectUrls)) {
                HttpRequest redirectRequest = createRedirectRequest(httpResponse, originalHttpRequest, savedAuthHeader);
                return attemptRedirect(context, next, redirectRequest, redirectAttempt + 1, attemptedRedirectUrls);
            }
            return Mono.just(httpResponse);
        });
    }

    private HttpResponse attemptRedirectSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next,
        HttpRequest originalHttpRequest, int redirectAttempt, Set<String> attemptedRedirectUrls) {

        HttpRequest requestCopy = originalHttpRequest.copy();
        // Save the Authorization header before sending. Downstream policies or the HTTP client
        // may strip or modify it, so we must capture it here to re-add after redirect.
        String savedAuthHeader = requestCopy.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        context.setHttpRequest(requestCopy);

        HttpResponse httpResponse = next.clone().processSync();

        if (shouldRedirect(httpResponse, redirectAttempt, attemptedRedirectUrls)) {
            HttpRequest redirectRequest = createRedirectRequest(httpResponse, originalHttpRequest, savedAuthHeader);
            return attemptRedirectSync(context, next, redirectRequest, redirectAttempt + 1, attemptedRedirectUrls);
        }

        return httpResponse;
    }

    /**
     * Determines whether the response is a redirect that should be followed.
     */
    private boolean shouldRedirect(HttpResponse httpResponse, int tryCount, Set<String> attemptedRedirectUrls) {
        if (!isRedirectStatusCode(httpResponse.getStatusCode())) {
            return false;
        }

        if (tryCount > MAX_REDIRECT_ATTEMPTS) {
            LOGGER.atError()
                .addKeyValue("maxAttempts", MAX_REDIRECT_ATTEMPTS)
                .log("Redirect attempts have been exhausted.");
            return false;
        }

        String redirectUrl = httpResponse.getHeaderValue(HttpHeaderName.LOCATION);
        if (redirectUrl == null) {
            LOGGER.atWarning().log("Redirect status code received but no Location header present.");
            return false;
        }

        if (attemptedRedirectUrls.contains(redirectUrl)) {
            LOGGER.atError()
                .addKeyValue("redirectUrl", redirectUrl)
                .log("Request was redirected more than once to the same URL.");
            return false;
        }

        LOGGER.atVerbose()
            .addKeyValue("tryCount", tryCount)
            .addKeyValue("redirectUrl", redirectUrl)
            .log("Following redirect.");

        attemptedRedirectUrls.add(redirectUrl);
        return true;
    }

    /**
     * Creates a redirect request, preserving the Authorization header if the redirect stays within
     * the same host (same-origin).
     *
     * @param httpResponse the redirect response containing the Location header
     * @param originalRequest the original request before it was sent downstream
     * @param savedAuthHeader the Authorization header value saved before the request was sent
     */
    private HttpRequest createRedirectRequest(HttpResponse httpResponse, HttpRequest originalRequest,
        String savedAuthHeader) {
        String redirectUrl = httpResponse.getHeaderValue(HttpHeaderName.LOCATION);

        // Build the redirect request from the original (pre-send) request to avoid any
        // modifications made by downstream policies or the HTTP client.
        HttpRequest redirectRequest = originalRequest.copy();
        redirectRequest.setUrl(redirectUrl);

        if (savedAuthHeader != null) {
            if (isSameOrigin(originalRequest.getUrl().toString(), redirectUrl)) {
                // Re-add the saved Authorization header for same-origin redirects.
                redirectRequest.getHeaders().set(HttpHeaderName.AUTHORIZATION, savedAuthHeader);
            } else {
                LOGGER.atVerbose().log("Redirect target is a different host; stripping Authorization header.");
                redirectRequest.getHeaders().remove(HttpHeaderName.AUTHORIZATION);
            }
        }

        httpResponse.close();
        return redirectRequest;
    }

    private static boolean isRedirectStatusCode(int statusCode) {
        return statusCode == HttpURLConnection.HTTP_MOVED_PERM
            || statusCode == HttpURLConnection.HTTP_MOVED_TEMP
            || statusCode == TEMPORARY_REDIRECT_STATUS_CODE
            || statusCode == PERMANENT_REDIRECT_STATUS_CODE;
    }

    /**
     * Checks if two URLs share the same scheme and host (same-origin check).
     */
    private static boolean isSameOrigin(String originalUrl, String redirectUrl) {
        try {
            URL original = new URL(originalUrl);
            URL redirect = new URL(redirectUrl);
            return original.getProtocol().equalsIgnoreCase(redirect.getProtocol())
                && original.getHost().equalsIgnoreCase(redirect.getHost())
                && getEffectivePort(original) == getEffectivePort(redirect);
        } catch (MalformedURLException e) {
            LOGGER.atWarning().log("Failed to parse URL for same-origin check; stripping Authorization header.");
            return false;
        }
    }

    private static int getEffectivePort(URL url) {
        int port = url.getPort();
        if (port == -1) {
            return url.getDefaultPort();
        }
        return port;
    }
}
