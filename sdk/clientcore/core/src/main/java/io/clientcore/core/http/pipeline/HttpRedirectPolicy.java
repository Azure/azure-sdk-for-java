// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LoggingEvent;
import io.clientcore.core.models.binarydata.BinaryData;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static io.clientcore.core.implementation.UrlRedactionUtil.getRedactedUri;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_METHOD_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_RESEND_COUNT_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_RESPONSE_HEADER_LOCATION_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.RETRY_MAX_ATTEMPT_COUNT_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.RETRY_WAS_LAST_ATTEMPT_KEY;
import static io.clientcore.core.implementation.instrumentation.LoggingEventNames.HTTP_REDIRECT_EVENT_NAME;

/**
 * A {@link HttpPipelinePolicy} that redirects a {@link HttpRequest} when an HTTP Redirect is received as a
 * {@link Response response}.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class HttpRedirectPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HttpRedirectPolicy.class);
    private final int maxAttempts;
    private final Predicate<HttpRedirectCondition> shouldRedirectCondition;
    private static final int DEFAULT_MAX_REDIRECT_ATTEMPTS = 3;

    private static final EnumSet<HttpMethod> DEFAULT_REDIRECT_ALLOWED_METHODS
        = EnumSet.of(HttpMethod.GET, HttpMethod.HEAD);
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
    public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        // Reset the attemptedRedirectUris for each individual request.
        InstrumentationContext instrumentationContext = httpRequest.getContext().getInstrumentationContext();

        ClientLogger logger = getLogger(httpRequest);
        return attemptRedirect(logger, next, 0, new LinkedHashSet<>(), instrumentationContext);
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.REDIRECT;
    }

    /**
     * Function to process through the HTTP Response received in the pipeline and redirect sending the request with a
     * new redirect URI.
     */
    private Response<BinaryData> attemptRedirect(ClientLogger logger, final HttpPipelineNextPolicy next,
        final int redirectAttempt, LinkedHashSet<String> attemptedRedirectUris,
        InstrumentationContext instrumentationContext) {

        // Make sure the context is not modified during redirect, except for the URI
        Response<BinaryData> response = next.copy().process();

        HttpRedirectCondition requestRedirectCondition
            = new HttpRedirectCondition(response, redirectAttempt, attemptedRedirectUris);

        if ((shouldRedirectCondition != null && shouldRedirectCondition.test(requestRedirectCondition))
            || (shouldRedirectCondition == null
                && defaultShouldAttemptRedirect(logger, requestRedirectCondition, instrumentationContext))) {
            createRedirectRequest(response);
            return attemptRedirect(logger, next, redirectAttempt + 1, attemptedRedirectUris, instrumentationContext);
        }

        return response;
    }

    private boolean defaultShouldAttemptRedirect(ClientLogger logger, HttpRedirectCondition requestRedirectCondition,
        InstrumentationContext context) {
        Response<BinaryData> response = requestRedirectCondition.getResponse();
        int tryCount = requestRedirectCondition.getTryCount();
        Set<String> attemptedRedirectUris = requestRedirectCondition.getRedirectedUris();
        String redirectUri = response.getHeaders().getValue(this.locationHeader);

        if (isValidRedirectStatusCode(response.getStatusCode()) && redirectUri != null) {
            HttpMethod method = response.getRequest().getHttpMethod();
            if (tryCount >= this.maxAttempts - 1) {
                logRedirect(logger, true, redirectUri, tryCount, method, "Redirect attempts have been exhausted.",
                    context);
                return false;
            }

            if (!allowedRedirectHttpMethods.contains(response.getRequest().getHttpMethod())) {
                logRedirect(logger, true, redirectUri, tryCount, method,
                    "Request redirection is not enabled for this HTTP method.", context);
                return false;
            }

            if (attemptedRedirectUris.contains(redirectUri)) {
                logRedirect(logger, true, redirectUri, tryCount, method,
                    "Request was redirected more than once to the same URI.", context);
                return false;
            }

            logRedirect(logger, false, redirectUri, tryCount, method, null, context);

            attemptedRedirectUris.add(redirectUri);

            return true;
        }

        return false;
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

        redirectResponse.close();
    }

    private void logRedirect(ClientLogger logger, boolean lastAttempt, String redirectUri, int tryCount,
        HttpMethod method, String message, InstrumentationContext context) {
        LoggingEvent log = lastAttempt ? logger.atWarning() : logger.atVerbose();
        if (log.isEnabled()) {
            log.addKeyValue(HTTP_REQUEST_RESEND_COUNT_KEY, tryCount)
                .addKeyValue(RETRY_MAX_ATTEMPT_COUNT_KEY, maxAttempts)
                .addKeyValue(HTTP_REQUEST_METHOD_KEY, method)
                .addKeyValue(HTTP_RESPONSE_HEADER_LOCATION_KEY, redactUri(redirectUri))
                .addKeyValue(RETRY_WAS_LAST_ATTEMPT_KEY, lastAttempt)
                .setEventName(HTTP_REDIRECT_EVENT_NAME)
                .setInstrumentationContext(context)
                .log(message);
        }
    }

    private String redactUri(String location) {
        URI uri;
        try {
            uri = URI.create(location);
        } catch (IllegalArgumentException e) {
            return null;
        }
        // TODO: make it configurable? Or don't log URL?
        return getRedactedUri(uri, Collections.emptySet());
    }

    private ClientLogger getLogger(HttpRequest httpRequest) {
        ClientLogger logger = null;

        if (httpRequest.getContext() != null && httpRequest.getContext().getLogger() != null) {
            logger = httpRequest.getContext().getLogger();
        }

        return logger == null ? LOGGER : logger;
    }
}
