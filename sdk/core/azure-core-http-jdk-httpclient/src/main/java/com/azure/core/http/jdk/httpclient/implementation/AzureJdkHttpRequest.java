// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.implementation.util.HttpHeadersAccessHelper;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import static java.net.http.HttpRequest.BodyPublishers.noBody;

/**
 * Implementation of the JDK {@link HttpRequest}.
 * <p>
 * Using this instead of {@link HttpRequest#newBuilder()} allows us to optimize some cases now allowed by the builder.
 * For example, setting headers requires each key-value for the same header to be set individually. This class allows
 * us to set all headers at once. And given that the headers are backed by a {@link TreeMap} it reduces the number of
 * String comparisons performed.
 */
public final class AzureJdkHttpRequest extends HttpRequest {
    private final boolean expectContinue;
    private final BodyPublisher bodyPublisher;
    private final String method;
    private final URI uri;
    private final HttpHeaders headers;
    private final Optional<Duration> responseTimeout;

    /**
     * Creates a new instance of the JDK HttpRequest.
     *
     * @param azureCoreRequest The Azure Core request to create the JDK HttpRequest from.
     * @param context The context of the request.
     * @param restrictedHeaders The set of restricted headers.
     * @param logger The logger to log warnings to.
     * @param writeTimeout The write timeout of the request.
     * @param responseTimeout The response timeout of the request.
     */
    public AzureJdkHttpRequest(com.azure.core.http.HttpRequest azureCoreRequest, Context context,
        Set<String> restrictedHeaders, ClientLogger logger, Duration writeTimeout, Duration responseTimeout) {
        HttpMethod method = azureCoreRequest.getHttpMethod();
        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();
        responseTimeout = (Duration) context.getData(HttpUtils.AZURE_RESPONSE_TIMEOUT)
            .filter(timeoutDuration -> timeoutDuration instanceof Duration)
            .orElse(responseTimeout);

        this.method = method.toString();
        this.bodyPublisher = (method == HttpMethod.GET || method == HttpMethod.HEAD)
            ? noBody()
            : BodyPublisherUtils.toBodyPublisher(azureCoreRequest, writeTimeout, progressReporter);

        // The JDK enters its wait path on this flag alone, so only opt in when there is content to withhold. A
        // zero length publisher has nothing to send; a negative length means the length is not known ahead of time,
        // which is the streaming case the handshake exists for, so it stays eligible.
        this.expectContinue = bodyPublisher.contentLength() != 0 && expectsContinue(azureCoreRequest);

        try {
            uri = azureCoreRequest.getUrl().toURI();
        } catch (URISyntaxException e) {
            throw logger.logExceptionAsError(Exceptions.propagate(e));
        }

        this.headers = HttpHeaders
            .of(new HeaderFilteringMap(HttpHeadersAccessHelper.getRawHeaderMap(azureCoreRequest.getHeaders()),
                restrictedHeaders, logger), (ignored1, ignored2) -> true);
        this.responseTimeout = Optional.ofNullable(responseTimeout);
    }

    @Override
    public Optional<BodyPublisher> bodyPublisher() {
        return Optional.ofNullable(bodyPublisher);
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public Optional<Duration> timeout() {
        return responseTimeout;
    }

    /*
     * Expect is a comma separated list of expectations and values may carry surrounding whitespace, so the
     * header cannot be compared as a whole.
     */
    private static boolean expectsContinue(com.azure.core.http.HttpRequest azureCoreRequest) {
        String expectHeader = azureCoreRequest.getHeaders().getValue(HttpHeaderName.EXPECT);
        if (expectHeader == null) {
            return false;
        }

        for (String expectation : expectHeader.split(",")) {
            if ("100-continue".equalsIgnoreCase(expectation.trim())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean expectContinue() {
        return expectContinue;
    }

    @Override
    public URI uri() {
        return uri;
    }

    @Override
    public Optional<HttpClient.Version> version() {
        return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }
}
