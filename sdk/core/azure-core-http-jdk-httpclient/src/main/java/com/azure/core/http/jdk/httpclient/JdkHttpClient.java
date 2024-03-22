// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.jdk.httpclient.implementation.AzureJdkHttpRequest;
import com.azure.core.http.jdk.httpclient.implementation.ByteArrayTimeoutResponseSubscriber;
import com.azure.core.http.jdk.httpclient.implementation.FlowableTimeoutResponseSubscriber;
import com.azure.core.http.jdk.httpclient.implementation.InputStreamTimeoutResponseSubscriber;
import com.azure.core.http.jdk.httpclient.implementation.JdkHttpResponseAsync;
import com.azure.core.http.jdk.httpclient.implementation.JdkHttpResponseSync;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.http.jdk.httpclient.implementation.JdkHttpUtils.fromJdkHttpHeaders;

/**
 * HttpClient implementation for the JDK HttpClient.
 */
class JdkHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpClient.class);
    private static final String AZURE_EAGERLY_READ_RESPONSE = "azure-eagerly-read-response";
    private static final String AZURE_IGNORE_RESPONSE_BODY = "azure-ignore-response-body";

    private final java.net.http.HttpClient jdkHttpClient;

    private final Set<String> restrictedHeaders;

    private final Duration writeTimeout;
    private final Duration responseTimeout;
    private final Duration readTimeout;

    JdkHttpClient(java.net.http.HttpClient httpClient, Set<String> restrictedHeaders, Duration writeTimeout,
        Duration responseTimeout, Duration readTimeout) {
        this.jdkHttpClient = httpClient;
        int javaVersion = getJavaVersion();
        if (javaVersion <= 11) {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("JdkAsyncHttpClient is not supported in Java version 11 and below."));
        }

        this.restrictedHeaders = restrictedHeaders;
        LOGGER.verbose("Effective restricted headers: {}", restrictedHeaders);

        this.writeTimeout = writeTimeout;
        this.responseTimeout = responseTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData(AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        boolean ignoreResponseBody = (boolean) context.getData(AZURE_IGNORE_RESPONSE_BODY).orElse(false);

        Mono<java.net.http.HttpRequest> jdkRequestMono = Mono.fromCallable(() -> toJdkHttpRequest(request, context));

        if (eagerlyReadResponse || ignoreResponseBody) {
            java.net.http.HttpResponse.BodyHandler<byte[]> bodyHandler = getResponseHandler(readTimeout,
                java.net.http.HttpResponse.BodyHandlers::ofByteArray, ByteArrayTimeoutResponseSubscriber::new);

            return jdkRequestMono
                .flatMap(jdkRequest -> Mono.fromCompletionStage(jdkHttpClient.sendAsync(jdkRequest, bodyHandler)))
                .map(jdkResponse -> {
                    // For now, eagerlyReadResponse and ignoreResponseBody works the same.
                    HttpHeaders headers = fromJdkHttpHeaders(jdkResponse.headers());
                    int statusCode = jdkResponse.statusCode();

                    return new JdkHttpResponseSync(request, statusCode, headers, jdkResponse.body());
                });
        } else {
            java.net.http.HttpResponse.BodyHandler<Flow.Publisher<List<ByteBuffer>>> bodyHandler
                = getResponseHandler(readTimeout, java.net.http.HttpResponse.BodyHandlers::ofPublisher,
                    FlowableTimeoutResponseSubscriber::new);

            return jdkRequestMono
                .flatMap(jdkRequest -> Mono.fromCompletionStage(jdkHttpClient.sendAsync(jdkRequest, bodyHandler)))
                .map(jdkResponse -> new JdkHttpResponseAsync(request, jdkResponse));
        }
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData(AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        boolean ignoreResponseBody = (boolean) context.getData(AZURE_IGNORE_RESPONSE_BODY).orElse(false);

        java.net.http.HttpRequest jdkRequest = toJdkHttpRequest(request, context);
        try {
            // For now, eagerlyReadResponse and ignoreResponseBody works the same.
            // if (ignoreResponseBody) {
            // java.net.http.HttpResponse<Void> jdKResponse = jdkHttpClient.send(jdkRequest,
            // responseInfo -> new BodyIgnoringSubscriber(LOGGER));
            // return new JdkHttpResponseSync(request, jdKResponse.statusCode(),
            // fromJdkHttpHeaders(jdKResponse.headers()), IGNORED_BODY);
            // }

            if (eagerlyReadResponse || ignoreResponseBody) {
                java.net.http.HttpResponse.BodyHandler<byte[]> bodyHandler = getResponseHandler(readTimeout,
                    java.net.http.HttpResponse.BodyHandlers::ofByteArray, ByteArrayTimeoutResponseSubscriber::new);

                java.net.http.HttpResponse<byte[]> jdKResponse = jdkHttpClient.send(jdkRequest, bodyHandler);
                return new JdkHttpResponseSync(request, jdKResponse.statusCode(),
                    fromJdkHttpHeaders(jdKResponse.headers()), jdKResponse.body());
            } else {
                java.net.http.HttpResponse.BodyHandler<InputStream> bodyHandler = getResponseHandler(readTimeout,
                    java.net.http.HttpResponse.BodyHandlers::ofInputStream, InputStreamTimeoutResponseSubscriber::new);

                return new JdkHttpResponseSync(request, jdkHttpClient.send(jdkRequest, bodyHandler));
            }
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        } catch (InterruptedException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Converts the given azure-core request to the JDK HttpRequest type.
     *
     * @param request the azure-core request
     * @return the HttpRequest
     */
    private java.net.http.HttpRequest toJdkHttpRequest(HttpRequest request, Context context) {
        return new AzureJdkHttpRequest(request, context, restrictedHeaders, LOGGER, writeTimeout, responseTimeout);
    }

    /**
     * Get the java runtime major version.
     *
     * @return the java major version
     */
    private static int getJavaVersion() {
        // java.version format:
        // 8 and lower: 1.7, 1.8.0
        // 9 and above: 12, 14.1.1
        String version = System.getProperty("java.version");
        if (CoreUtils.isNullOrEmpty(version)) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Can't find 'java.version' system property."));
        }
        if (version.startsWith("1.")) {
            if (version.length() < 3) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version));
            }
            try {
                return Integer.parseInt(version.substring(2, 3));
            } catch (Exception t) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version, t));
            }
        } else {
            int idx = version.indexOf(".");

            if (idx == -1) {
                return Integer.parseInt(version);
            }
            try {
                return Integer.parseInt(version.substring(0, idx));
            } catch (Exception t) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Can't parse 'java.version':" + version, t));
            }
        }
    }

    /**
     * Gets the response body handler based on whether a read timeout is configured.
     * <p>
     * When a read timeout is configured our custom handler is used that tracks the time taken between each read
     * operation to pull the body from the network. If a timeout isn't configured the built-in JDK handler is used.
     *
     * @param readTimeout The configured read timeout.
     * @param jdkBodyHandler The JDK body handler to use when no read timeout is configured.
     * @param timeoutSubscriber The supplier for the custom body subscriber to use when a read timeout is configured.
     * @return The response body handler to use.
     * @param <T> The type of the response body.
     */
    private static <T> java.net.http.HttpResponse.BodyHandler<T> getResponseHandler(Duration readTimeout,
        Supplier<java.net.http.HttpResponse.BodyHandler<T>> jdkBodyHandler,
        Function<Long, java.net.http.HttpResponse.BodySubscriber<T>> timeoutSubscriber) {
        return (readTimeout != null && !readTimeout.isNegative() && !readTimeout.isZero())
            ? responseInfo -> timeoutSubscriber.apply(readTimeout.toMillis())
            : jdkBodyHandler.get();
    }
}
