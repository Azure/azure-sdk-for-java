// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRedirectOptions;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.util.LoggingKeys;
import io.clientcore.core.util.ClientLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A {@link HttpPipelinePolicy} that redirects a {@link HttpRequest} when an HTTP Redirect is received as a
 * {@link Response response}.
 */
public final class HttpRedirectPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HttpRedirectPolicy.class);
    private final int maxAttempts;
    private final Predicate<HttpRequestRedirectCondition> shouldRedirectCondition;
    private static final int DEFAULT_MAX_REDIRECT_ATTEMPTS = 3;
    private static final String REDIRECT_URIS_KEY = "redirectUris";
    private static final String ORIGINATING_REQUEST_URI_KEY = "originatingRequestUri";

    private static final EnumSet<HttpMethod> DEFAULT_REDIRECT_ALLOWED_METHODS = EnumSet.of(HttpMethod.GET, HttpMethod.HEAD);
    private static final int PERMANENT_REDIRECT_STATUS_CODE = 308;
    private static final int TEMPORARY_REDIRECT_STATUS_CODE = 307;
    private final EnumSet<HttpMethod> allowedRedirectHttpMethods;
    private final HttpHeaderName locationHeader;

    /**
     * Creates {@link HttpRedirectPolicy} with default with a maximum number of redirect attempts 3,
     * header name "Location" to locate the redirect uri in the response headers and {@link HttpMethod#GET}
     * and {@link HttpMethod#HEAD} as allowed methods for performing the redirect.
     * This redirect policy uses the redirect status response code (301, 302, 307, 308) to determine if this request
     * should be redirected.
     */
    public HttpRedirectPolicy() {
        this(new HttpRedirectOptions(DEFAULT_MAX_REDIRECT_ATTEMPTS, HttpHeaderName.LOCATION,
            DEFAULT_REDIRECT_ALLOWED_METHODS));
    }

    /**
     * Creates {@link HttpRedirectPolicy} with the provided {@code redirectOptions} to
     * determine if this request should be redirected.
     *
     * @param redirectOptions The configured {@link HttpRedirectOptions} to modify redirect policy behavior.
     *
     * @throws NullPointerException When {@code redirectOptions} is {@code null}.
     */
    public HttpRedirectPolicy(HttpRedirectOptions redirectOptions) {
        Objects.requireNonNull(redirectOptions, "'redirectOptions' cannot be null.");
        this.maxAttempts = redirectOptions.getMaxAttempts();
        this.shouldRedirectCondition = redirectOptions.getShouldRedirectCondition();
        this.allowedRedirectHttpMethods = redirectOptions.getAllowedRedirectHttpMethods().isEmpty()
            ? DEFAULT_REDIRECT_ALLOWED_METHODS
            : redirectOptions.getAllowedRedirectHttpMethods();
        this.locationHeader = redirectOptions.getLocationHeader() == null
            ? HttpHeaderName.LOCATION
            : redirectOptions.getLocationHeader();
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        // Reset the attemptedRedirectUris for each individual request.
        return attemptRedirect(next, 1, new LinkedHashSet<>());
    }

    /**
     * Function to process through the HTTP Response received in the pipeline and redirect sending the request with a
     * new redirect URI.
     */
    private Response<?> attemptRedirect(final HttpPipelineNextPolicy next,
                                        final int redirectAttempt, LinkedHashSet<String> attemptedRedirectUris) {
        // Make sure the context is not modified during redirect, except for the URI
        Response<?> response = next.clone().process();

        HttpRequestRedirectCondition requestRedirectCondition = new HttpRequestRedirectCondition(response, redirectAttempt, attemptedRedirectUris);
        if ((shouldRedirectCondition != null && shouldRedirectCondition.test(requestRedirectCondition))
            || (shouldRedirectCondition == null && defaultShouldAttemptRedirect(requestRedirectCondition))) {
            createRedirectRequest(response);
            return attemptRedirect(next, redirectAttempt + 1, attemptedRedirectUris);
        }

        return response;
    }

    private boolean defaultShouldAttemptRedirect(HttpRequestRedirectCondition requestRedirectCondition) {
        Response<?> response = requestRedirectCondition.getResponse();
        int tryCount = requestRedirectCondition.getTryCount();
        Set<String> attemptedRedirectUris = requestRedirectCondition.getRedirectedUris();
        String redirectUri = response.getHeaders().getValue(this.locationHeader);

        if (isValidRedirectStatusCode(response.getStatusCode())
            && isValidRedirectCount(tryCount)
            && isAllowedRedirectMethod(response.getRequest().getHttpMethod())
            && redirectUri != null
            && !alreadyAttemptedRedirectUri(redirectUri, attemptedRedirectUris)) {

            LOGGER.atVerbose()
                .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
                .addKeyValue(REDIRECT_URIS_KEY, attemptedRedirectUris::toString)
                .addKeyValue(ORIGINATING_REQUEST_URI_KEY, response.getRequest().getUri())
                .log("Redirecting.");

            attemptedRedirectUris.add(redirectUri);

            return true;
        }

        return false;
    }

    /**
     * Check if the attempt count of the redirect is less than the {@code maxAttempts}
     *
     * @param tryCount the try count for the HTTP request associated to the HTTP response.
     *
     * @return {@code true} if the {@code tryCount} is greater than the {@code maxAttempts}, {@code false} otherwise.
     */
    private boolean isValidRedirectCount(int tryCount) {
        if (tryCount >= this.maxAttempts) {
            LOGGER.atError()
                .addKeyValue("maxAttempts", this.maxAttempts)
                .log("Redirect attempts have been exhausted.");

            return false;
        }

        return true;
    }

    /**
     * Check if the redirect uri provided in the response headers is already attempted.
     *
     * @param redirectUri the redirect uri provided in the response header.
     * @param attemptedRedirectUris the set containing a list of attempted redirect locations.
     *
     * @return {@code true} if the redirectUri provided in the response header is already being attempted for redirect,
     * {@code false} otherwise.
     */
    private boolean alreadyAttemptedRedirectUri(String redirectUri, Set<String> attemptedRedirectUris) {
        if (attemptedRedirectUris.contains(redirectUri)) {
            LOGGER.atError()
                .addKeyValue(LoggingKeys.REDIRECT_URI_KEY, redirectUri)
                .log("Request was redirected more than once to the same URI.");

            return true;
        }

        return false;
    }


    /**
     * Check if the request http method is a valid redirect method.
     *
     * @param httpMethod the http method of the request.
     *
     * @return {@code true} if the request {@code httpMethod} is a valid http redirect method, {@code false} otherwise.
     */
    private boolean isAllowedRedirectMethod(HttpMethod httpMethod) {
        if (allowedRedirectHttpMethods.contains(httpMethod)) {
            return true;
        } else {
            LOGGER.atError()
                .addKeyValue(LoggingKeys.HTTP_METHOD_KEY, httpMethod)
                .log("Request redirection is not enabled for this HTTP method.");

            return false;
        }
    }

    /**
     * Checks if the incoming request status code is a valid redirect status code.
     *
     * @param statusCode the status code of the incoming request.
     *
     * @return {@code true} if the request {@code statusCode} is a valid http redirect method, {@code false} otherwise.
     */
    private boolean isValidRedirectStatusCode(int statusCode) {
        return statusCode == HttpURLConnection.HTTP_MOVED_TEMP
            || statusCode == HttpURLConnection.HTTP_MOVED_PERM
            || statusCode == PERMANENT_REDIRECT_STATUS_CODE
            || statusCode == TEMPORARY_REDIRECT_STATUS_CODE;
    }

    private void createRedirectRequest(Response<?> redirectResponse) {
        // Clear the authorization header to avoid the client to be redirected to an untrusted third party server
        // causing it to leak your authorization token to.
        redirectResponse.getRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
        redirectResponse.getRequest().setUri(redirectResponse.getHeaders().getValue(this.locationHeader));

        try {
            redirectResponse.close();
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }

    }
}
