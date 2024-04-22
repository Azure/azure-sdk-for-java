// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.ServerSentEventUtils;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.http.jdk.httpclient.implementation.InputStreamTimeoutResponseSubscriber;
import io.clientcore.http.jdk.httpclient.implementation.JdkHttpRequest;
import io.clientcore.http.jdk.httpclient.implementation.JdkHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.clientcore.core.http.models.ContentType.APPLICATION_OCTET_STREAM;
import static io.clientcore.core.http.models.HttpMethod.HEAD;
import static io.clientcore.core.http.models.ResponseBodyMode.BUFFER;
import static io.clientcore.core.http.models.ResponseBodyMode.IGNORE;
import static io.clientcore.core.http.models.ResponseBodyMode.STREAM;
import static io.clientcore.core.util.ServerSentEventUtils.processTextEventStream;
import static io.clientcore.http.jdk.httpclient.implementation.JdkHttpUtils.fromJdkHttpHeaders;

/**
 * HttpClient implementation for the JDK HttpClient.
 */
class JdkHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpClient.class);

    private final java.net.http.HttpClient jdkHttpClient;

    private final Set<String> restrictedHeaders;

    private final Duration writeTimeout;
    private final Duration responseTimeout;
    private final Duration readTimeout;
    private final boolean hasReadTimeout;

    JdkHttpClient(java.net.http.HttpClient httpClient, Set<String> restrictedHeaders, Duration writeTimeout,
        Duration responseTimeout, Duration readTimeout) {
        this.jdkHttpClient = httpClient;
        int javaVersion = getJavaVersion();
        if (javaVersion <= 11) {
            throw LOGGER.logThrowableAsError(
                new UnsupportedOperationException("JdkAsyncHttpClient is not supported in Java version 11 and below."));
        }

        this.restrictedHeaders = restrictedHeaders;
        LOGGER.atVerbose().addKeyValue("headers", restrictedHeaders).log("Effective restricted headers.");

        // Set the write and response timeouts to null if they are negative or zero.
        // The writeTimeout is used with 'Flux.timeout(Duration)' which uses thread switching, always. When the timeout
        // is zero or negative it's treated as an infinite timeout. So, setting this to null will prevent that thread
        // switching with the same runtime behavior.
        this.writeTimeout
            = (writeTimeout != null && !writeTimeout.isNegative() && !writeTimeout.isZero()) ? writeTimeout : null;

        // The responseTimeout is used by JDK 'HttpRequest.timeout()' which will throw an exception when the timeout
        // is non-null and is zero or negative. We treat zero or negative as an infinite timeout, so reset to null to
        // prevent the exception from being thrown and have the behavior we want.
        this.responseTimeout = (responseTimeout != null && !responseTimeout.isNegative() && !responseTimeout.isZero())
            ? responseTimeout
            : null;
        this.readTimeout = readTimeout;
        this.hasReadTimeout = readTimeout != null && !readTimeout.isNegative() && !readTimeout.isZero();
    }

    @Override
    public Response<?> send(HttpRequest request) throws IOException {
        java.net.http.HttpRequest jdkRequest = toJdkHttpRequest(request);
        try {
            // JDK HttpClient works differently than OkHttp and HttpUrlConnection where the response body handling has
            // to be determined when the request is being sent, rather than being something that can be determined after
            // the response has been received. Given that, we'll always consume the response body as an InputStream and
            // after receiving it we'll handle ignoring, buffering, or streaming appropriately based on either the
            // Content-Type header or the response body mode.
            HttpResponse.BodyHandler<InputStream> bodyHandler = getResponseHandler(hasReadTimeout, readTimeout,
                HttpResponse.BodyHandlers::ofInputStream, InputStreamTimeoutResponseSubscriber::new);

            java.net.http.HttpResponse<InputStream> jdKResponse = jdkHttpClient.send(jdkRequest, bodyHandler);
            return toResponse(request, jdKResponse);
        } catch (InterruptedException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    /**
     * Converts the given client-core request to the JDK HttpRequest type.
     *
     * @param request the client-core request
     * @return the HttpRequest
     */
    private java.net.http.HttpRequest toJdkHttpRequest(HttpRequest request) {
        return new JdkHttpRequest(request, restrictedHeaders, LOGGER, writeTimeout, responseTimeout);
    }

    /**
     * Get the java runtime major version.
     *
     * @return the java major version
     */
    private static int getJavaVersion() {
        return Runtime.version().feature();
    }

    /**
     * Gets the response body handler based on whether a read timeout is configured.
     * <p>
     * When a read timeout is configured our custom handler is used that tracks the time taken between each read
     * operation to pull the body from the network. If a timeout isn't configured the built-in JDK handler is used.
     *
     * @param hasReadTimeout Flag indicating if a read timeout is configured.
     * @param readTimeout The configured read timeout.
     * @param jdkBodyHandler The JDK body handler to use when no read timeout is configured.
     * @param timeoutSubscriber The supplier for the custom body subscriber to use when a read timeout is configured.
     * @return The response body handler to use.
     * @param <T> The type of the response body.
     */
    private static <T> HttpResponse.BodyHandler<T> getResponseHandler(boolean hasReadTimeout, Duration readTimeout,
        Supplier<HttpResponse.BodyHandler<T>> jdkBodyHandler,
        Function<Long, HttpResponse.BodySubscriber<T>> timeoutSubscriber) {
        return hasReadTimeout ? responseInfo -> timeoutSubscriber.apply(readTimeout.toMillis()) : jdkBodyHandler.get();
    }

    private Response<?> toResponse(HttpRequest request, HttpResponse<InputStream> response) throws IOException {
        HttpHeaders coreHeaders = fromJdkHttpHeaders(response.headers());

        String contentType = coreHeaders.getValue(HttpHeaderName.CONTENT_TYPE);
        if (ServerSentEventUtils.isTextEventStreamContentType(contentType)) {
            ServerSentEventListener listener = request.getServerSentEventListener();

            if (listener != null) {
                processTextEventStream(response.body(), listener);
            } else {
                throw LOGGER.logThrowableAsError(new RuntimeException(ServerSentEventUtils.NO_LISTENER_ERROR_MESSAGE));
            }

            return new JdkHttpResponse(request, response.statusCode(), coreHeaders, BinaryData.EMPTY);
        }

        return processResponse(request, response, coreHeaders, contentType);
    }

    private Response<?> processResponse(HttpRequest request, HttpResponse<InputStream> response,
        HttpHeaders coreHeaders, String contentType) throws IOException {
        RequestOptions options = request.getRequestOptions();
        ResponseBodyMode responseBodyMode = null;

        if (options != null) {
            responseBodyMode = options.getResponseBodyMode();
        }

        if (responseBodyMode == null) {
            if (request.getHttpMethod() == HEAD) {
                responseBodyMode = IGNORE;
            } else if (contentType != null
                && APPLICATION_OCTET_STREAM.regionMatches(true, 0, contentType, 0, APPLICATION_OCTET_STREAM.length())) {

                responseBodyMode = STREAM;
            } else {
                responseBodyMode = BUFFER;
            }
        }

        BinaryData body = null;

        switch (responseBodyMode) {
            case IGNORE:
                response.body().close();

                break;

            case STREAM:
                body = BinaryData.fromStream(response.body());

                break;

            case BUFFER:
            case DESERIALIZE: // Deserialization will occur at a later point in HttpResponseBodyDecoder.
            default:
                try (InputStream responseBody = response.body()) { // Use try-with-resources to close the stream.
                    body = BinaryData.fromBytes(responseBody.readAllBytes());
                }
        }

        return new JdkHttpResponse(request, response.statusCode(), coreHeaders, body == null ? BinaryData.EMPTY : body);
    }
}
