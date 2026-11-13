// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.jdk.httpclient.implementation.AzureJdkHttpRequest;
import com.azure.core.http.jdk.httpclient.implementation.ByteArrayTimeoutResponseSubscriber;
import com.azure.core.http.jdk.httpclient.implementation.JdkHttpResponseAsync;
import com.azure.core.http.jdk.httpclient.implementation.JdkHttpResponseSync;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.util.Context;
import com.azure.core.util.SharedExecutorService;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.http.jdk.httpclient.implementation.JdkHttpUtils.fromJdkHttpHeaders;

/**
 * HttpClient implementation for the JDK HttpClient.
 */
class JdkHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpClient.class);

    final java.net.http.HttpClient jdkHttpClient;

    private final Set<String> restrictedHeaders;

    private final Duration writeTimeout;
    private final Duration responseTimeout;
    private final Duration readTimeout;
    private final boolean hasReadTimeout;
    private final java.net.http.HttpClient.Version httpVersion;

    JdkHttpClient(java.net.http.HttpClient httpClient, Set<String> restrictedHeaders, Duration writeTimeout,
        Duration responseTimeout, Duration readTimeout, java.net.http.HttpClient.Version httpVersion) {
        this.jdkHttpClient = httpClient;
        this.httpVersion = httpVersion;
        int javaVersion = getJavaVersion();
        if (javaVersion <= 11) {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("JdkAsyncHttpClient is not supported in Java version 11 and below."));
        }

        this.restrictedHeaders = restrictedHeaders;
        LOGGER.verbose("Effective restricted headers: {}", restrictedHeaders);

        // Set the write and response timeouts to null if they are negative or zero.
        // The writeTimeout is used with 'Flux.timeout(Duration)' which uses thread switching, always. When the timeout
        // is zero or negative it's treated as an infinite timeout. So, setting this to null will prevent that thread
        // switching with the same runtime behavior.
        this.writeTimeout
            = (writeTimeout != null && !writeTimeout.isNegative() && !writeTimeout.isZero()) ? writeTimeout : null;

        // Nonpositive response timeouts disable both the continue-handshake and post-upload response deadlines.
        // Bodiless requests use the native JDK timeout, which also requires a strictly positive duration.
        this.responseTimeout = (responseTimeout != null && !responseTimeout.isNegative() && !responseTimeout.isZero())
            ? responseTimeout
            : null;
        this.readTimeout = readTimeout;
        this.hasReadTimeout = readTimeout != null && !readTimeout.isNegative() && !readTimeout.isZero();
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData(HttpUtils.AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        boolean ignoreResponseBody = (boolean) context.getData(HttpUtils.AZURE_IGNORE_RESPONSE_BODY).orElse(false);

        if (eagerlyReadResponse || ignoreResponseBody) {
            java.net.http.HttpResponse.BodyHandler<byte[]> bodyHandler = getResponseHandler(hasReadTimeout, readTimeout,
                java.net.http.HttpResponse.BodyHandlers::ofByteArray, ByteArrayTimeoutResponseSubscriber::new);

            return Mono.defer(() -> Mono.fromFuture(sendInternal(request, context, bodyHandler, jdkResponse -> {
                // For now, eagerlyReadResponse and ignoreResponseBody works the same.
                HttpHeaders headers = fromJdkHttpHeaders(jdkResponse.headers());
                int statusCode = jdkResponse.statusCode();

                return new JdkHttpResponseSync(request, statusCode, headers, jdkResponse.body());
            })));
        } else {
            // The way 'BodyHandlers.ofPublisher()' works is that it returns the 'CompletableFuture' as soon as the
            // response is received, before any values of the 'Flow.Publisher<List<ByteBuffer>>' are emitted.
            // Given this, we can hook into 'Flux.timeout(Duration)' when we convert the
            // 'Flow.Publisher<List<ByteBuffer>>' to 'Flux<ByteBuffer>' to track the read timeout.
            return Mono.defer(() -> Mono
                .fromFuture(sendInternal(request, context, java.net.http.HttpResponse.BodyHandlers.ofPublisher(),
                    jdkResponse -> new JdkHttpResponseAsync(request, readTimeout, hasReadTimeout, jdkResponse))));
        }
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        boolean eagerlyReadResponse = (boolean) context.getData(HttpUtils.AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        boolean ignoreResponseBody = (boolean) context.getData(HttpUtils.AZURE_IGNORE_RESPONSE_BODY).orElse(false);

        CompletableFuture<HttpResponse> responseFuture;
        if (eagerlyReadResponse || ignoreResponseBody) {
            java.net.http.HttpResponse.BodyHandler<byte[]> bodyHandler = getResponseHandler(hasReadTimeout, readTimeout,
                java.net.http.HttpResponse.BodyHandlers::ofByteArray, ByteArrayTimeoutResponseSubscriber::new);
            responseFuture = sendInternal(request, context, bodyHandler, response -> new JdkHttpResponseSync(request,
                response.statusCode(), fromJdkHttpHeaders(response.headers()), response.body()));
        } else {
            responseFuture = sendInternal(request, context, java.net.http.HttpResponse.BodyHandlers.ofInputStream(),
                response -> new JdkHttpResponseSync(request, response, readTimeout));
        }

        try {
            // The synchronous JDK API cannot cancel a phase deadline without interrupting the caller. Wait on the
            // cancellable exchange instead, keeping timeout handling identical to the asynchronous path.
            return responseFuture.get();
        } catch (ExecutionException e) {
            Throwable cause = unwrapCompletionException(e.getCause());
            if (cause instanceof Error) {
                throw LOGGER.logThrowableAsError((Error) cause);
            }
            // Match HttpClient.send(): failures from the asynchronous exchange, including request-body publisher
            // failures, are exposed as IOExceptions by the synchronous API.
            if (cause instanceof IOException) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException((IOException) cause));
            }
            throw LOGGER.logExceptionAsError(new UncheckedIOException(new IOException(cause.getMessage(), cause)));
        } catch (InterruptedException e) {
            responseFuture.cancel(true);
            Thread.currentThread().interrupt();
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private <T> CompletableFuture<HttpResponse> sendInternal(HttpRequest request, Context context,
        java.net.http.HttpResponse.BodyHandler<T> bodyHandler,
        Function<java.net.http.HttpResponse<T>, HttpResponse> responseFactory) {
        Duration timeout = (Duration) context.getData(HttpUtils.AZURE_RESPONSE_TIMEOUT)
            .filter(value -> value instanceof Duration)
            .orElse(responseTimeout);
        CompletableFuture<HttpResponse> result = new CompletableFuture<>();
        ResponseTimeout watcher = new ResponseTimeout(timeout, result);
        // A request-level preference avoids rewriting the version on the caller's mutable JDK builder.
        java.net.http.HttpRequest jdkRequest = new AzureJdkHttpRequest(request, context, restrictedHeaders, LOGGER,
            writeTimeout, responseTimeout, watcher::uploadStarted, watcher::uploadCompleted, httpVersion);
        CompletableFuture<java.net.http.HttpResponse<T>> exchange = jdkHttpClient.sendAsync(jdkRequest, info -> {
            watcher.responseReceived();
            return bodyHandler.apply(info);
        });
        if (jdkRequest.expectContinue()) {
            watcher.waitForContinue();
        }

        result.whenComplete((ignored, error) -> {
            watcher.close();
            if (error != null) {
                exchange.cancel(true);
            }
        });
        exchange.thenApply(responseFactory).whenComplete((response, error) -> {
            if (error != null) {
                result.completeExceptionally(unwrapCompletionException(error));
            } else if (!result.complete(response)) {
                // A timeout or cancellation may win the race with receipt of the response.
                response.close();
            }
        });
        return result;
    }

    private static Throwable unwrapCompletionException(Throwable error) {
        while (error instanceof CompletionException && error.getCause() != null) {
            error = error.getCause();
        }
        return error;
    }

    private static final class ResponseTimeout {
        private final Duration timeout;
        private final CompletableFuture<?> result;
        private ScheduledFuture<?> task;
        private long generation;
        private boolean uploadStarted;
        private boolean responseReceived;
        private boolean closed;

        private ResponseTimeout(Duration timeout, CompletableFuture<?> result) {
            this.timeout = timeout;
            this.result = result;
        }

        private synchronized void uploadCompleted() {
            // Start a fresh response deadline only after the publisher has finished the upload.
            schedule("Response");
        }

        private synchronized void waitForContinue() {
            // Subscription or an early final response can race with sendAsync returning its future.
            if (!uploadStarted) {
                schedule("Continue handshake");
            }
        }

        private synchronized void uploadStarted() {
            uploadStarted = true;
            // Body publication has its own write timeout. Do not count it against the continue-wait deadline.
            cancelTask();
        }

        private void schedule(String phase) {
            if (closed
                || responseReceived
                || result.isDone()
                || timeout == null
                || timeout.isZero()
                || timeout.isNegative()) {
                return;
            }
            cancelTask();
            long current = generation;
            task = SharedExecutorService.getInstance()
                .schedule(() -> timedOut(current, phase), Math.max(1, timeout.toMillis()), TimeUnit.MILLISECONDS);
        }

        private synchronized void responseReceived() {
            responseReceived = true;
            cancelTask();
        }

        private void timedOut(long current, String phase) {
            synchronized (this) {
                if (closed || responseReceived || current != generation) {
                    return;
                }
                closed = true;
                cancelTask();
            }
            // Completing the future can cancel the native exchange. Do not hold the timer lock across callbacks.
            result.completeExceptionally(new HttpTimeoutException(phase + " timed out after " + timeout + "."));
        }

        private synchronized void close() {
            closed = true;
            cancelTask();
        }

        private void cancelTask() {
            ++generation;
            if (task != null) {
                task.cancel(false);
                task = null;
            }
        }
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
