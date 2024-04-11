// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.jdk.httpclient.implementation;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.util.ClientLogger;

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
public final class JdkHttpRequest extends HttpRequest {
    private final BodyPublisher bodyPublisher;
    private final String method;
    private final URI uri;
    private final HttpHeaders headers;
    private final Optional<Duration> responseTimeout;

    /**
     * Creates a new instance of the JDK HttpRequest.
     *
     * @param coreRequest The Client Core request to create the JDK HttpRequest from.
     * @param restrictedHeaders The set of restricted headers.
     * @param logger The logger to log warnings to.
     * @param writeTimeout The write timeout of the request.
     * @param responseTimeout The response timeout of the request.
     */
    public JdkHttpRequest(io.clientcore.core.http.models.HttpRequest coreRequest, Set<String> restrictedHeaders,
        ClientLogger logger, Duration writeTimeout, Duration responseTimeout) {
        HttpMethod method = coreRequest.getHttpMethod();

        this.method = method.toString();
        this.bodyPublisher = (method == HttpMethod.GET || method == HttpMethod.HEAD)
            ? noBody()
            : BodyPublisherUtils.toBodyPublisher(coreRequest, writeTimeout);

        try {
            uri = coreRequest.getUrl().toURI();
        } catch (URISyntaxException e) {
            throw logger.logThrowableAsError(new RuntimeException(e));
        }

        this.headers = HttpHeaders.of(new HeaderFilteringMap(coreRequest.getHeaders(), restrictedHeaders, logger),
            (ignored1, ignored2) -> true);
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
