// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.vertx.implementation.BufferedVertxHttpResponse;
import com.azure.core.http.vertx.implementation.VertxHttpResponse;
import com.azure.core.http.vertx.implementation.VertxRequestWriteSubscriber;
import com.azure.core.http.vertx.implementation.VertxUtils;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.implementation.ReflectiveInvoker;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.ByteBufferContent;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.implementation.util.SerializableContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.SharedExecutorService;
import com.azure.core.util.logging.ClientLogger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.net.HostAndPort;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.http.vertx.implementation.VertxUtils.resetRequest;

/**
 * {@link HttpClient} implementation for the Vert.x {@link io.vertx.core.http.HttpClient}.
 */
class VertxHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(VertxHttpClient.class);

    // How long to wait for an interim 100 Continue before sending the body anyway, matching the defaults used by
    // other HTTP stacks. RFC 9110 requires a client not to wait indefinitely.
    private static final Duration EXPECT_CONTINUE_TIMEOUT = Duration.ofSeconds(1);

    private static final ReflectiveInvoker REQUEST_WRITE_HEAD;
    static {
        try {
            // Vert.x 5.1 introduced writeHead() and deprecated sendHead() for removal. Resolve the appropriate method
            // at runtime so the transport can still be compiled and used with Vert.x 4.5 and 5.0.
            Method writeHeadMethod;
            try {
                writeHeadMethod = HttpClientRequest.class.getMethod("writeHead");
            } catch (NoSuchMethodException ex) {
                writeHeadMethod = HttpClientRequest.class.getMethod("sendHead");
            }
            REQUEST_WRITE_HEAD = ReflectionUtils.getMethodInvoker(HttpClientRequest.class, writeHeadMethod, false);
        } catch (Exception ex) {
            // Fail here as the method isn't available and this HttpClient wouldn't be usable anyway.
            throw LOGGER.logExceptionAsError(new IllegalStateException(ex));
        }
    }

    final io.vertx.core.http.HttpClient client;
    final HttpClientOptions buildOptions;
    private final Duration responseTimeout;
    private final Duration writeTimeout;
    private final ScheduledExecutorService writeTimeoutExecutor;

    /**
     * Constructs a {@link VertxHttpClient}.
     *
     * @param client The Vert.x {@link io.vertx.core.http.HttpClient}
     */
    VertxHttpClient(io.vertx.core.http.HttpClient client, HttpClientOptions buildOptions, Duration responseTimeout) {
        this(client, buildOptions, responseTimeout, Duration.ZERO);
    }

    VertxHttpClient(io.vertx.core.http.HttpClient client, HttpClientOptions buildOptions, Duration responseTimeout,
        Duration writeTimeout) {
        this(client, buildOptions, responseTimeout, writeTimeout, SharedExecutorService.getInstance());
    }

    VertxHttpClient(io.vertx.core.http.HttpClient client, HttpClientOptions buildOptions, Duration responseTimeout,
        Duration writeTimeout, ScheduledExecutorService writeTimeoutExecutor) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
        this.buildOptions = buildOptions;
        this.responseTimeout = responseTimeout;
        this.writeTimeout = writeTimeout;
        this.writeTimeoutExecutor = Objects.requireNonNull(writeTimeoutExecutor, "writeTimeoutExecutor cannot be null");
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return Mono.deferContextual(contextView -> Mono.fromFuture(sendInternal(request, context, contextView)))
            .onErrorMap(VertxUtils::wrapVertxException);
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        try {
            return sendInternal(request, context, reactor.util.context.Context.empty()).get();
        } catch (Exception e) {
            Throwable mapped = e;
            if (e instanceof ExecutionException) {
                mapped = e.getCause();
            }

            mapped = VertxUtils.wrapVertxException(mapped);
            if (mapped instanceof Error) {
                throw LOGGER.logThrowableAsError((Error) mapped);
            } else if (mapped instanceof IOException) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException((IOException) mapped));
            } else if (mapped instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) mapped);
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(mapped));
            }
        }
    }

    private CompletableFuture<HttpResponse> sendInternal(HttpRequest request, Context context,
        ContextView contextView) {
        boolean eagerlyReadResponse = (boolean) context.getData(HttpUtils.AZURE_EAGERLY_READ_RESPONSE).orElse(false);
        boolean ignoreResponseBody = (boolean) context.getData(HttpUtils.AZURE_IGNORE_RESPONSE_BODY).orElse(false);
        Duration perCallTimeout = (Duration) context.getData(HttpUtils.AZURE_RESPONSE_TIMEOUT)
            .filter(timeoutDuration -> timeoutDuration instanceof Duration)
            .orElse(responseTimeout);

        ProgressReporter progressReporter = Contexts.with(context).getHttpRequestProgressReporter();

        RequestOptions options = new RequestOptions().setMethod(HttpMethod.valueOf(request.getHttpMethod().name()))
            .setAbsoluteURI(request.getUrl());

        // This is the shared design for sending a request and receiving a response in Vert.x. The design relies on
        // using a Vert.x Promise and the handler methods to control the flow of the request-response cycle. The Promise
        // will be completed upon failure during the request-response cycle or upon completion of the response. This can
        // be shared between sync and async as Reactor can use Mono.fromCompletionStage to convert the Promise to a Mono
        // and the sync path can convert the Promise to a Future and block it.

        // Create the Promise that will be returned. Promise.promise() is an uncompleted Promise.
        Promise<HttpResponse> promise = Promise.promise();
        CompletableFuture<HttpResponse> requestFuture = new CompletableFuture<>();
        promise.future().onComplete(result -> {
            if (result.failed()) {
                requestFuture.completeExceptionally(result.cause());
            } else if (!requestFuture.complete(result.result())) {
                // Cancellation may win the race with a successful response. Do not leave that response unconsumed.
                result.result().close();
            }
        });
        requestFuture.whenComplete((response, error) -> {
            if (requestFuture.isCancelled()) {
                // Fail the Vert.x promise too, including when cancellation occurs before request acquisition.
                promise.tryFail(new CancellationException("HTTP request was cancelled."));
            }
        });

        client.request(options).andThen(requestResult -> {
            if (requestResult.failed()) {
                promise.tryFail(requestResult.cause());
                return;
            }

            HttpClientRequest vertxRequest = requestResult.result();
            // A timed-out or canceled response future does not abort the underlying HTTP exchange on its own.
            promise.future().onFailure(cause -> resetRequest(vertxRequest, 0L, cause));
            if (promise.future().failed()) {
                return;
            }
            WriteTimeout writeWatcher = new WriteTimeout(writeTimeout, promise, writeTimeoutExecutor);
            promise.future().onComplete(ignored -> writeWatcher.close());

            for (HttpHeader header : request.getHeaders()) {
                // Potential optimization here would be creating a MultiMap wrapper around azure-core's
                // HttpHeaders and using RequestOptions.setHeaders(MultiMap)
                if (vertxRequest.version() == HttpVersion.HTTP_2
                    && HttpHeaderName.HOST.getCaseSensitiveName().equalsIgnoreCase(header.getName())) {
                    HostAndPort authority = parseHostHeader(header);
                    if (authority == null || authority.host().isEmpty()) {
                        promise.tryFail(LOGGER.logExceptionAsError(new IllegalArgumentException(
                            "The Host header must contain exactly one valid host and optional port.")));
                        return;
                    }
                    // Override :authority without sending a conflicting Host or changing the connection destination.
                    vertxRequest.authority(authority);
                } else {
                    vertxRequest.putHeader(header.getName(), header.getValuesList());
                }
            }
            if (request.getHeaders().get(HttpHeaderName.CONTENT_LENGTH) == null) {
                vertxRequest.setChunked(true);
            }

            Future<HttpClientResponse> responseFuture;
            if (!perCallTimeout.isZero() && !perCallTimeout.isNegative()) {
                responseFuture = vertxRequest.response().timeout(perCallTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } else {
                responseFuture = vertxRequest.response();
            }

            responseFuture = responseFuture.onFailure(promise::tryFail);

            if (eagerlyReadResponse || ignoreResponseBody) {
                responseFuture.andThen(responseResult -> {
                    if (responseResult.failed()) {
                        promise.tryFail(responseResult.cause());
                        return;
                    }

                    HttpClientResponse vertxHttpResponse = responseResult.result();
                    vertxHttpResponse.body().andThen(bodyResult -> {
                        if (bodyResult.succeeded()) {
                            promise.tryComplete(
                                new BufferedVertxHttpResponse(request, vertxHttpResponse, bodyResult.result()));
                        } else {
                            promise.tryFail(bodyResult.cause());
                        }
                    });
                });
            } else {
                responseFuture.andThen(responseResult -> {
                    if (responseResult.succeeded()) {
                        promise.tryComplete(new VertxHttpResponse(request, responseResult.result()));
                    } else {
                        promise.tryFail(responseResult.cause());
                    }
                });
            }

            sendBody(contextView, request, progressReporter, vertxRequest, promise, writeWatcher);
        });

        return requestFuture;
    }

    private static HostAndPort parseHostHeader(HttpHeader header) {
        if (header.getValuesList().size() != 1) {
            return null;
        }

        // HostAndPort.parseAuthority is not available in the minimum supported Vert.x 4.5 release.
        String value = header.getValue();
        try {
            URI uri = new URI("http://" + value);
            if (uri.getHost() == null
                || uri.getRawUserInfo() != null
                || !value.equals(uri.getRawAuthority())
                || uri.getPort() > 65535) {
                return null;
            }
            return HostAndPort.create(uri.getHost(), uri.getPort());
        } catch (URISyntaxException ignored) {
            return null;
        }
    }

    private void sendBody(ContextView contextView, HttpRequest azureRequest, ProgressReporter progressReporter,
        HttpClientRequest vertxRequest, Promise<HttpResponse> promise, WriteTimeout writeWatcher) {
        BinaryData body = azureRequest.getBodyAsBinaryData();

        if (body == null) {
            writeWatcher.start();
            vertxRequest.end().onSuccess(ignored -> writeWatcher.close()).onFailure(promise::tryFail);
            return;
        }

        if (expectsContinue(azureRequest.getHeaders().getValue(HttpHeaderName.EXPECT))) {
            // Send the headers and hold the body until the service accepts the expectation. If it answers with a
            // final status instead, that response completes the request and the body is never written.
            AtomicBoolean bodyWritten = new AtomicBoolean();
            Runnable writeOnce = () -> {
                if (bodyWritten.compareAndSet(false, true)) {
                    writeBody(contextView, azureRequest, progressReporter, vertxRequest, promise, body, writeWatcher);
                }
            };

            // A service may ignore the expectation entirely and wait for the body, so the request cannot depend on
            // the interim response arriving. RFC 9110 allows the content to be sent after a short wait.
            io.vertx.core.Context vertxContext = Vertx.currentContext();
            Long fallbackTimerId = vertxContext == null
                ? null
                : vertxContext.owner().setTimer(EXPECT_CONTINUE_TIMEOUT.toMillis(), ignored -> writeOnce.run());
            Runnable cancelFallback = () -> {
                if (fallbackTimerId != null && vertxContext != null) {
                    vertxContext.owner().cancelTimer(fallbackTimerId);
                }
            };

            // Once the exchange is terminal, for example the service rejected the headers outright or sending them
            // failed, there is nothing left to send. Claim the write so neither the fallback nor a late interim
            // response can subscribe the body onto a finished request.
            promise.future().onComplete(ignored -> {
                cancelFallback.run();
                bodyWritten.set(true);
            });

            vertxRequest.continueHandler(ignored -> {
                cancelFallback.run();
                writeOnce.run();
            });
            long headerWrite = writeWatcher.start();
            writeHead(vertxRequest).onSuccess(ignored -> writeWatcher.pause(headerWrite)).onFailure(promise::tryFail);
        } else {
            writeBody(contextView, azureRequest, progressReporter, vertxRequest, promise, body, writeWatcher);
        }
    }

    /*
     * Expect is a comma separated list of expectations and values may carry surrounding whitespace, so the header
     * cannot be compared as a whole.
     */
    private static boolean expectsContinue(String expectHeader) {
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

    private void writeBody(ContextView contextView, HttpRequest azureRequest, ProgressReporter progressReporter,
        HttpClientRequest vertxRequest, Promise<HttpResponse> promise, BinaryData body, WriteTimeout writeWatcher) {
        writeWatcher.start();
        BinaryDataContent bodyContent = BinaryDataHelper.getContent(body);
        if (bodyContent instanceof ByteArrayContent
            || bodyContent instanceof ByteBufferContent
            || bodyContent instanceof StringContent
            || bodyContent instanceof SerializableContent) {
            // This cannot produce a NullPointerException for the BinaryDataContent types that trigger this as they
            // are in-memory and have a length.
            long contentLength = bodyContent.getLength();
            Buffer buffer = Buffer.buffer(bodyContent.toBytes());
            // end() completes with the write, whereas send() completes only after receiving response headers.
            vertxRequest.end(buffer).onSuccess(ignored -> {
                writeWatcher.close();
                reportProgress(contentLength, progressReporter);
            }).onFailure(promise::tryFail);
        } else {
            // Right now both Flux<ByteBuffer> and InputStream bodies are being handled reactively.
            azureRequest.getBody()
                .subscribe(new VertxRequestWriteSubscriber(vertxRequest::exceptionHandler, vertxRequest::drainHandler,
                    buffer -> vertxRequest.write(buffer).onSuccess(ignored -> writeWatcher.progress()),
                    vertxRequest::writeQueueFull, (code, cause) -> resetRequest(vertxRequest, code, cause),
                    vertxRequest::end, promise, progressReporter, contextView));
        }
    }

    @SuppressWarnings("unchecked")
    private static Future<Void> writeHead(HttpClientRequest vertxRequest) {
        try {
            return (Future<Void>) REQUEST_WRITE_HEAD.invokeWithArguments(vertxRequest);
        } catch (Exception ex) {
            return Future.failedFuture(ex);
        }
    }

    private static void reportProgress(long progress, ProgressReporter progressReporter) {
        if (progressReporter != null) {
            progressReporter.reportProgress(progress);
        }
    }

    private static final class WriteTimeout {
        private final Duration timeout;
        private final Promise<HttpResponse> promise;
        private final ScheduledExecutorService executor;
        private final long timeoutNanos;
        private ScheduledFuture<?> task;
        private long generation;
        private long watchdogGeneration;
        private long lastProgressNanos;
        private boolean closed;

        private WriteTimeout(Duration timeout, Promise<HttpResponse> promise, ScheduledExecutorService executor) {
            this.timeout = timeout;
            this.promise = promise;
            this.executor = executor;
            this.timeoutNanos = timeout == null || timeout.isZero() || timeout.isNegative()
                ? 0
                : TimeUnit.MILLISECONDS.toNanos(Math.max(1, timeout.toMillis()));
        }

        private synchronized long start() {
            long current = ++generation;
            progress();
            return current;
        }

        private synchronized void progress() {
            if (!closed && !promise.future().isComplete() && timeoutNanos > 0) {
                // Buffers update a timestamp, not the scheduled task. The shared executor retains canceled tasks
                // until their deadlines, so replacing a task per buffer would retain the entire upload's history.
                lastProgressNanos = System.nanoTime();
                if (task == null) {
                    long watchdog = ++watchdogGeneration;
                    task = executor.schedule(() -> timedOut(watchdog), timeoutNanos, TimeUnit.NANOSECONDS);
                }
            }
        }

        private synchronized void pause(long current) {
            // A late header-write completion must not pause a body write that has already begun.
            if (current == generation) {
                ++generation;
                cancelTask();
            }
        }

        private void timedOut(long current) {
            synchronized (this) {
                if (closed || current != watchdogGeneration) {
                    return;
                }
                long remaining = timeoutNanos - (System.nanoTime() - lastProgressNanos);
                if (remaining > 0) {
                    // Progress updates only the timestamp. Schedule again after this watchdog has left the queue,
                    // rather than retaining a canceled timer for every streamed buffer.
                    task = executor.schedule(() -> timedOut(current), remaining, TimeUnit.NANOSECONDS);
                    return;
                }
                closed = true;
                cancelTask();
            }
            promise.tryFail(new TimeoutException("Request write timed out after " + timeout + "."));
        }

        private synchronized void close() {
            closed = true;
            ++generation;
            cancelTask();
        }

        private void cancelTask() {
            ++watchdogGeneration;
            if (task != null) {
                task.cancel(false);
                task = null;
            }
        }
    }
}
