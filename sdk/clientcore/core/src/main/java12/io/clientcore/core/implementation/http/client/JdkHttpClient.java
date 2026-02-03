// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.ServerSentResult;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.ServerSentEventUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.clientcore.core.http.models.HttpMethod.HEAD;
import static io.clientcore.core.implementation.http.client.JdkHttpUtils.fromJdkHttpHeaders;
import static io.clientcore.core.utils.ServerSentEventUtils.attemptRetry;
import static io.clientcore.core.utils.ServerSentEventUtils.isTextEventStreamContentType;
import static io.clientcore.core.utils.ServerSentEventUtils.processTextEventStream;

/**
 * HttpClient implementation using {@link HttpURLConnection} to send requests and receive responses.
 */
public final class JdkHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpClient.class);

    /**
     * Error message for when no {@link ServerSentEventListener} is attached to the {@link HttpRequest}.
     */
    private static final String NO_LISTENER_ERROR_MESSAGE
        = "No ServerSentEventListener attached to HttpRequest to handle the text/event-stream response";

    private final Set<String> restrictedHeaders;
    private final Duration writeTimeout;
    private final Duration responseTimeout;
    private final Duration readTimeout;
    private final boolean hasReadTimeout;

    final java.net.http.HttpClient jdkHttpClient;

    public JdkHttpClient(java.net.http.HttpClient httpClient, Set<String> restrictedHeaders, Duration writeTimeout,
        Duration responseTimeout, Duration readTimeout) {
        this.jdkHttpClient = httpClient;

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
    public Response<BinaryData> send(HttpRequest request) {
        java.net.http.HttpRequest jdkRequest = toJdkHttpRequest(request);
        try {
            // JDK HttpClient works differently than OkHttp and HttpUrlConnection where the response body handling has
            // to be determined when the request is being sent, rather than being something that can be determined after
            // the response has been received. Given that, we'll always consume the response body as an InputStream and
            // after receiving it we'll handle ignoring, buffering, or streaming appropriately based on either the
            // Content-Type header or the response body mode.
            java.net.http.HttpResponse.BodyHandler<InputStream> bodyHandler
                = getResponseHandler(hasReadTimeout, readTimeout,
                    java.net.http.HttpResponse.BodyHandlers::ofInputStream, InputStreamTimeoutResponseSubscriber::new);

            java.net.http.HttpResponse<InputStream> jdKResponse = jdkHttpClient.send(jdkRequest, bodyHandler);
            return toResponse(request, jdKResponse);
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        } catch (InterruptedException e) {
            throw LOGGER.throwableAtError().log(e, RuntimeException::new);
        }
    }

    /**
     * Converts the given clientcore request to the JDK HttpRequest type.
     *
     * @param request the clientcore request
     * @return the HttpRequest
     */
    private java.net.http.HttpRequest toJdkHttpRequest(HttpRequest request) {
        return new JdkHttpRequest(request, restrictedHeaders, LOGGER, writeTimeout, responseTimeout);
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

    private Response<BinaryData> toResponse(HttpRequest request, java.net.http.HttpResponse<InputStream> response) {
        HttpHeaders coreHeaders = fromJdkHttpHeaders(response.headers());
        ServerSentResult serverSentResult = null;

        String contentType = coreHeaders.getValue(HttpHeaderName.CONTENT_TYPE);
        if (ServerSentEventUtils.isTextEventStreamContentType(contentType)) {
            ServerSentEventListener listener = request.getServerSentEventListener();
            if (listener != null) {
                try {
                    serverSentResult = processTextEventStream(response.body(), listener);
                } catch (IOException e) {
                    throw LOGGER.throwableAtError().log(e, CoreException::from);
                }

                if (serverSentResult.getException() != null) {
                    // If an exception occurred while processing the text event stream, emit listener onError.
                    listener.onError(serverSentResult.getException());
                }

                // If an error occurred or we want to reconnect
                if (!Thread.currentThread().isInterrupted() && attemptRetry(serverSentResult, request)) {
                    return this.send(request);
                }
            } else {
                throw LOGGER.throwableAtError().log(NO_LISTENER_ERROR_MESSAGE, IllegalStateException::new);
            }
        }

        return processResponse(request, response, serverSentResult, coreHeaders, contentType);
    }

    private Response<BinaryData> processResponse(HttpRequest request, java.net.http.HttpResponse<InputStream> response,
        ServerSentResult serverSentResult, HttpHeaders coreHeaders, String contentType) {

        BinaryData body = null;
        BodyHandling bodyHandling = getBodyHandling(request, coreHeaders);

        switch (bodyHandling) {
            case IGNORE:
                try {
                    response.body().close();
                } catch (IOException e) {
                    throw LOGGER.throwableAtError().log(e, CoreException::from);
                }
                break;

            case STREAM:
                if (isTextEventStreamContentType(contentType)) {
                    body = createBodyFromServerSentResult(serverSentResult);
                } else {
                    body = BinaryData.fromStream(response.body());
                }

                break;

            case BUFFER:
                // Deserialization will occur at a later point in HttpResponseBodyDecoder.
                if (isTextEventStreamContentType(contentType)) {
                    body = createBodyFromServerSentResult(serverSentResult);
                } else {
                    body = createBodyFromResponse(response);
                }
                break;

            default:
                body = createBodyFromResponse(response);
                break;

        }

        return new Response<>(request, response.statusCode(), coreHeaders, body == null ? BinaryData.empty() : body);
    }

    private BodyHandling getBodyHandling(HttpRequest request, HttpHeaders responseHeaders) {
        String contentType = responseHeaders.getValue(HttpHeaderName.CONTENT_TYPE);

        if (request.getHttpMethod() == HEAD) {
            return BodyHandling.IGNORE;
        } else if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            return BodyHandling.STREAM;
        } else {
            return BodyHandling.BUFFER;
        }
    }

    private enum BodyHandling {
        IGNORE,
        STREAM,
        BUFFER
    }

    private BinaryData createBodyFromServerSentResult(ServerSentResult serverSentResult) {
        String bodyContent = (serverSentResult != null && serverSentResult.getData() != null)
            ? String.join("\n", serverSentResult.getData())
            : "";
        return BinaryData.fromString(bodyContent);
    }

    private BinaryData createBodyFromResponse(HttpResponse<InputStream> response) {
        try (InputStream responseBody = response.body()) { // Use try-with-resources to close the stream.
            return BinaryData.fromBytes(responseBody.readAllBytes());
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }
}
