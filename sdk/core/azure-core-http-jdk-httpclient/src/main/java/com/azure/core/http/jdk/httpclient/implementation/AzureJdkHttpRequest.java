// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.implementation.util.HttpHeadersAccessHelper;
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
    private final BodyPublisher bodyPublisher;
    private final String method;
    private final URI uri;
    private final HttpHeaders headers;

    /**
     * Creates a new instance of the JDK HttpRequest.
     *
     * @param azureCoreRequest The Azure Core request to create the JDK HttpRequest from.
     * @param context The context of the request.
     * @param restrictedHeaders The set of restricted headers.
     * @param logger The logger to log warnings to.
     */
    public AzureJdkHttpRequest(com.azure.core.http.HttpRequest azureCoreRequest, Context context,
        Set<String> restrictedHeaders, ClientLogger logger) {
        this.method = azureCoreRequest.getHttpMethod().toString();

        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        final java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder();
        try {
            uri = azureCoreRequest.getUrl().toURI();
        } catch (URISyntaxException e) {
            throw logger.logExceptionAsError(Exceptions.propagate(e));
        }

        this.headers = HttpHeaders.of(new HeaderFilteringMap(
            HttpHeadersAccessHelper.getRawHeaderMap(azureCoreRequest.getHeaders()), restrictedHeaders, logger),
            (ignored1, ignored2) -> true);

        switch (azureCoreRequest.getHttpMethod()) {
            case GET:
                this.bodyPublisher = null;
                break;

            case HEAD:
                this.bodyPublisher = noBody();
                break;

            default:
                this.bodyPublisher = BodyPublisherUtils.toBodyPublisher(azureCoreRequest, progressReporter);
                break;
        }
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
        return Optional.empty();
    }

    @Override
    public boolean expectContinue() {
        return false;
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
