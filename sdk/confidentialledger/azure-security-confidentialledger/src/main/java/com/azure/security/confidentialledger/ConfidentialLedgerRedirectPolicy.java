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
 * A redirect policy for Confidential Ledger that preserves the Authorization header on trusted
 * redirects while enforcing a strict redirect destination policy.
 *
 * <p>The Confidential Ledger service uses a distributed network of nodes. Write operations (POST)
 * may be redirected from the load-balanced endpoint to a specific node via HTTP 307/308 redirects.
 * The standard {@link com.azure.core.http.policy.RedirectPolicy RedirectPolicy} in azure-core strips
 * the Authorization header on redirect for security, and only allows GET/HEAD methods by default.
 * This policy addresses both issues for Confidential Ledger by:</p>
 *
 * <ul>
 *   <li>Following redirects for all HTTP methods including POST</li>
 *   <li>Preserving the Authorization header when the redirect target is a trusted Confidential
 *       Ledger destination</li>
 * </ul>
 *
 * <p><strong>Redirect destination policy.</strong> A redirect is only followed when the target uses
 * the same scheme and the target host is the original ledger host or one of its subdomains (e.g.,
 * {@code accledger-2.myledger.confidential-ledger.azure.com} is a subdomain of
 * {@code myledger.confidential-ledger.azure.com}). The host comparison is case-insensitive and
 * ignores the port. Redirects to sibling ledgers, parent domains, unrelated hosts, or look-alike
 * suffix domains are refused: they are logged at the warning level and never followed, so the
 * sensitive Authorization header is never forwarded to an unintended destination. This prevents a
 * misconfigured or malicious load balancer from redirecting a request — along with its sensitive
 * headers — to a destination outside the original ledger.</p>
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
        // Capture the pristine request URL so that every redirect hop is validated against the
        // original ledger endpoint rather than the previous (already-redirected) target.
        String originalRequestUrl = context.getHttpRequest().getUrl().toString();
        return attemptRedirect(context, next, context.getHttpRequest(), 1, new HashSet<>(), originalRequestUrl);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        // Capture the pristine request URL so that every redirect hop is validated against the
        // original ledger endpoint rather than the previous (already-redirected) target.
        String originalRequestUrl = context.getHttpRequest().getUrl().toString();
        return attemptRedirectSync(context, next, context.getHttpRequest(), 1, new HashSet<>(), originalRequestUrl);
    }

    private Mono<HttpResponse> attemptRedirect(HttpPipelineCallContext context, HttpPipelineNextPolicy next,
        HttpRequest originalHttpRequest, int redirectAttempt, Set<String> attemptedRedirectUrls,
        String originalRequestUrl) {

        HttpRequest requestCopy = originalHttpRequest.copy();
        // Save the Authorization header before sending. Downstream policies or the HTTP client
        // may strip or modify it, so we must capture it here to re-add after redirect.
        String savedAuthHeader = requestCopy.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        context.setHttpRequest(requestCopy);

        return next.clone().process().flatMap(httpResponse -> {
            if (shouldRedirect(httpResponse, redirectAttempt, attemptedRedirectUrls, originalRequestUrl)) {
                HttpRequest redirectRequest = createRedirectRequest(httpResponse, originalHttpRequest, savedAuthHeader);
                return attemptRedirect(context, next, redirectRequest, redirectAttempt + 1, attemptedRedirectUrls,
                    originalRequestUrl);
            }
            return Mono.just(httpResponse);
        });
    }

    private HttpResponse attemptRedirectSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next,
        HttpRequest originalHttpRequest, int redirectAttempt, Set<String> attemptedRedirectUrls,
        String originalRequestUrl) {

        HttpRequest requestCopy = originalHttpRequest.copy();
        // Save the Authorization header before sending. Downstream policies or the HTTP client
        // may strip or modify it, so we must capture it here to re-add after redirect.
        String savedAuthHeader = requestCopy.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        context.setHttpRequest(requestCopy);

        HttpResponse httpResponse = next.clone().processSync();

        if (shouldRedirect(httpResponse, redirectAttempt, attemptedRedirectUrls, originalRequestUrl)) {
            HttpRequest redirectRequest = createRedirectRequest(httpResponse, originalHttpRequest, savedAuthHeader);
            return attemptRedirectSync(context, next, redirectRequest, redirectAttempt + 1, attemptedRedirectUrls,
                originalRequestUrl);
        }

        return httpResponse;
    }

    /**
     * Determines whether the response is a redirect that should be followed.
     */
    private boolean shouldRedirect(HttpResponse httpResponse, int tryCount, Set<String> attemptedRedirectUrls,
        String originalRequestUrl) {
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

        // Enforce the redirect destination policy: only follow redirects whose target is the
        // original ledger host (or one of its subdomains) using the same scheme. Anything else —
        // sibling ledgers, parent domains, unrelated hosts, or look-alike suffix domains — is
        // refused and never followed, so the Authorization header is not forwarded to it.
        if (!isAllowedRedirectTarget(originalRequestUrl, redirectUrl)) {
            LOGGER.atWarning()
                .addKeyValue("redirectUrl", redirectUrl)
                .log("Refusing to follow redirect to disallowed target.");
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
     * Creates a redirect request from the original (pre-send) request, re-adding the saved
     * Authorization header.
     *
     * <p>The redirect target has already been validated as an allowed Confidential Ledger
     * destination by {@link #shouldRedirect}, so the Authorization header is safe to forward.</p>
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
            redirectRequest.getHeaders().set(HttpHeaderName.AUTHORIZATION, savedAuthHeader);
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
     * Checks whether the redirect target is an allowed Confidential Ledger destination.
     *
     * <p>Confidential Ledger redirects from the load-balanced endpoint
     * (e.g., {@code myledger.confidential-ledger.azure.com}) to a specific node
     * (e.g., {@code accledger-2.myledger.confidential-ledger.azure.com:16385}).
     * The node hostname is a subdomain of the original host, so a simple host equality check
     * would incorrectly reject this legitimate redirect. A redirect target is allowed only when it
     * shares the same scheme and the redirect host is either the same as, or a subdomain of, the
     * original host. The comparison is case-insensitive and ignores the port. Sibling ledgers,
     * parent domains, unrelated hosts, and look-alike suffix domains are rejected. If either URL
     * cannot be parsed, or either host is empty, the redirect is rejected (fail safe).</p>
     */
    private static boolean isAllowedRedirectTarget(String originalUrl, String redirectUrl) {
        try {
            URL original = new URL(originalUrl);
            URL redirect = new URL(redirectUrl);

            if (!original.getProtocol().equalsIgnoreCase(redirect.getProtocol())) {
                return false;
            }

            String originalHost = original.getHost().toLowerCase(java.util.Locale.ROOT);
            String redirectHost = redirect.getHost().toLowerCase(java.util.Locale.ROOT);

            // Fail safe: if either host cannot be determined, do not follow the redirect.
            if (originalHost.isEmpty() || redirectHost.isEmpty()) {
                return false;
            }

            // Exact host match or the redirect host is a subdomain of the original host.
            // e.g., accledger-2.myledger.confidential-ledger.azure.com is a subdomain of
            //        myledger.confidential-ledger.azure.com
            return redirectHost.equals(originalHost) || redirectHost.endsWith("." + originalHost);
        } catch (MalformedURLException e) {
            LOGGER.atWarning().log("Failed to parse URL for redirect destination check; refusing to follow redirect.");
            return false;
        }
    }
}
