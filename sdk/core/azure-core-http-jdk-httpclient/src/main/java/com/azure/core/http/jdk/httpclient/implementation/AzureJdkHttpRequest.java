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
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Flow;

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
    private final Optional<HttpClient.Version> httpVersion;

    /**
     * Creates a request with body publication callbacks and an optional protocol preference.
     *
     * @param azureCoreRequest The Azure Core request.
     * @param context The request context.
     * @param restrictedHeaders The restricted header names.
     * @param logger The logger.
     * @param writeTimeout The write timeout.
     * @param responseTimeout The response timeout.
     * @param writeStartedCallback Callback when the body is subscribed, or null.
     * @param writeCompletedCallback Callback after publishing the request body, or null to use the native request
     * timeout.
     * @param httpVersion The protocol preference, or null to use the JDK client's preference.
     */
    public AzureJdkHttpRequest(com.azure.core.http.HttpRequest azureCoreRequest, Context context,
        Set<String> restrictedHeaders, ClientLogger logger, Duration writeTimeout, Duration responseTimeout,
        Runnable writeStartedCallback, Runnable writeCompletedCallback, HttpClient.Version httpVersion) {
        HttpMethod method = azureCoreRequest.getHttpMethod();
        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();
        responseTimeout = (Duration) context.getData(HttpUtils.AZURE_RESPONSE_TIMEOUT)
            .filter(timeoutDuration -> timeoutDuration instanceof Duration)
            .orElse(responseTimeout);

        this.method = method.toString();
        BodyPublisher publisher = (method == HttpMethod.GET || method == HttpMethod.HEAD)
            ? noBody()
            : BodyPublisherUtils.toBodyPublisher(azureCoreRequest, writeTimeout, progressReporter);
        boolean hasBody = publisher.contentLength() != 0;
        this.bodyPublisher = (hasBody && writeCompletedCallback != null)
            ? new CompletionTrackingPublisher(publisher, writeStartedCallback, writeCompletedCallback)
            : publisher;

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
        // The JDK does not subscribe to a zero-length publisher, so keep its native deadline for bodiless requests.
        // Non-empty uploads use the completion callback instead; a native deadline would also time out the upload.
        this.responseTimeout = (writeCompletedCallback == null || !hasBody)
            && responseTimeout != null
            && !responseTimeout.isZero()
            && !responseTimeout.isNegative() ? Optional.of(responseTimeout) : Optional.empty();
        this.httpVersion = Optional.ofNullable(httpVersion);
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
        return httpVersion;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    private static final class CompletionTrackingPublisher implements BodyPublisher {
        private final BodyPublisher publisher;
        private final Runnable writeStarted;
        private final Runnable writeCompleted;

        private CompletionTrackingPublisher(BodyPublisher publisher, Runnable writeStarted, Runnable writeCompleted) {
            this.publisher = publisher;
            this.writeStarted = writeStarted;
            this.writeCompleted = writeCompleted;
        }

        @Override
        public long contentLength() {
            return publisher.contentLength();
        }

        @Override
        public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
            if (writeStarted != null) {
                writeStarted.run();
            }
            publisher.subscribe(new Flow.Subscriber<ByteBuffer>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscriber.onSubscribe(subscription);
                }

                @Override
                public void onNext(ByteBuffer item) {
                    subscriber.onNext(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                }

                @Override
                public void onComplete() {
                    subscriber.onComplete();
                    writeCompleted.run();
                }
            });
        }
    }
}
