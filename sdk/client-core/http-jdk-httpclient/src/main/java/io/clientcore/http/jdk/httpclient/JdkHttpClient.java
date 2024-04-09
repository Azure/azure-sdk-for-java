// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.http.jdk.httpclient.implementation.ByteArrayTimeoutResponseSubscriber;
import io.clientcore.http.jdk.httpclient.implementation.InputStreamTimeoutResponseSubscriber;
import io.clientcore.http.jdk.httpclient.implementation.JdkHttpRequest;
import io.clientcore.http.jdk.httpclient.implementation.JdkHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

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
        this.writeTimeout = (writeTimeout != null && !writeTimeout.isNegative() && !writeTimeout.isZero())
            ? writeTimeout
            : null;

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
    public Response<?> send(HttpRequest request) {
        java.net.http.HttpRequest jdkRequest = toJdkHttpRequest(request);
        try {
            // For now, eagerlyReadResponse and ignoreResponseBody works the same.
            // if (ignoreResponseBody) {
            // java.net.http.HttpResponse<Void> jdKResponse = jdkHttpClient.send(jdkRequest,
            // responseInfo -> new BodyIgnoringSubscriber(LOGGER));
            // return new JdkHttpResponseSync(request, jdKResponse.statusCode(),
            // fromJdkHttpHeaders(jdKResponse.headers()), IGNORED_BODY);
            // }
            ResponseBodyMode bodyMode = request.getMetadata().getResponseBodyMode();
            if (bodyMode == ResponseBodyMode.IGNORE
                || bodyMode == ResponseBodyMode.DESERIALIZE
                || bodyMode == ResponseBodyMode.BUFFER) {
                java.net.http.HttpResponse.BodyHandler<byte[]> bodyHandler
                    = getResponseHandler(hasReadTimeout, readTimeout,
                        java.net.http.HttpResponse.BodyHandlers::ofByteArray, ByteArrayTimeoutResponseSubscriber::new);

                java.net.http.HttpResponse<byte[]> jdKResponse = jdkHttpClient.send(jdkRequest, bodyHandler);
                return new JdkHttpResponse(request, jdKResponse.statusCode(),
                    fromJdkHttpHeaders(jdKResponse.headers()), BinaryData.fromBytes(jdKResponse.body()));
            } else {
                java.net.http.HttpResponse.BodyHandler<InputStream> bodyHandler = getResponseHandler(hasReadTimeout,
                    readTimeout, java.net.http.HttpResponse.BodyHandlers::ofInputStream,
                    InputStreamTimeoutResponseSubscriber::new);

                java.net.http.HttpResponse<InputStream> jdKResponse = jdkHttpClient.send(jdkRequest, bodyHandler);
                return new JdkHttpResponse(request, jdKResponse.statusCode(),
                    fromJdkHttpHeaders(jdKResponse.headers()), BinaryData.fromStream(jdKResponse.body()));
            }
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        } catch (InterruptedException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    /**
     * Converts the given azure-core request to the JDK HttpRequest type.
     *
     * @param request the azure-core request
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
    private static <T> java.net.http.HttpResponse.BodyHandler<T> getResponseHandler(boolean hasReadTimeout,
        Duration readTimeout, Supplier<java.net.http.HttpResponse.BodyHandler<T>> jdkBodyHandler,
        Function<Long, java.net.http.HttpResponse.BodySubscriber<T>> timeoutSubscriber) {
        return hasReadTimeout ? responseInfo -> timeoutSubscriber.apply(readTimeout.toMillis()) : jdkBodyHandler.get();
    }
}
