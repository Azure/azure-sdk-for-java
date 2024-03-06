// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.ServerSentEvent;
import com.generic.core.http.models.ServerSentEventListener;
import com.generic.core.http.okhttp.implementation.OkHttpBufferedResponse;
import com.generic.core.http.okhttp.implementation.OkHttpFileRequestBody;
import com.generic.core.http.okhttp.implementation.OkHttpInputStreamRequestBody;
import com.generic.core.http.okhttp.implementation.OkHttpResponse;
import com.generic.core.http.models.ContentType;
import com.generic.core.implementation.util.ServerSentEventHelper;
import com.generic.core.models.BinaryData;
import com.generic.core.models.FileBinaryData;
import com.generic.core.models.Header;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
import com.generic.core.models.InputStreamBinaryData;
import com.generic.core.util.ClientLogger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * HttpClient implementation for OkHttp.
 */
class OkHttpHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(OkHttpHttpClient.class);
    private static final byte[] EMPTY_BODY = new byte[0];
    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(EMPTY_BODY);
    final OkHttpClient httpClient;
    private static final String LAST_EVENT_ID = "Last-Event-Id";
    private static final String DEFAULT_EVENT = "message";
    private static final Pattern DIGITS_ONLY = Pattern.compile("^[\\d]*$");

    OkHttpHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpResponse send(HttpRequest request) {
        boolean eagerlyConvertHeaders = request.getMetadata().isEagerlyConvertHeaders();
        boolean eagerlyReadResponse = request.getMetadata().isEagerlyReadResponse();
        boolean ignoreResponseBody = request.getMetadata().isIgnoreResponseBody();

        Request okHttpRequest = toOkHttpRequest(request);
        try {
            Response okHttpResponse = httpClient.newCall(okHttpRequest).execute();
            return toHttpResponse(request, okHttpResponse, eagerlyReadResponse, ignoreResponseBody,
                eagerlyConvertHeaders);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Converts the given generic-core request to okhttp request.
     *
     * @param request the generic-core request.
     *
     * @return Th eOkHttp request.
     */
    private okhttp3.Request toOkHttpRequest(HttpRequest request) {
        Request.Builder requestBuilder = new Request.Builder()
            .url(request.getUrl());

        if (request.getHeaders() != null) {
            for (Header hdr : request.getHeaders()) {
                // OkHttp allows for headers with multiple values, but it treats them as separate headers,
                // therefore, we must call rb.addHeader for each value, using the same key for all of them
                hdr.getValuesList().forEach(value -> requestBuilder.addHeader(hdr.getName(), value));
            }
        }

        if (request.getHttpMethod() == HttpMethod.GET) {
            return requestBuilder.get().build();
        } else if (request.getHttpMethod() == HttpMethod.HEAD) {
            return requestBuilder.head().build();
        }

        RequestBody okHttpRequestBody = toOkHttpRequestBody(request.getBody(), request.getHeaders());

        return requestBuilder.method(request.getHttpMethod().toString(), okHttpRequestBody).build();
    }

    /**
     * Create a Mono of okhttp3.RequestBody from the given BinaryData.
     *
     * @param bodyContent The request body content
     * @param headers the headers associated with the original request
     *
     * @return The Mono emitting okhttp request
     */
    private RequestBody toOkHttpRequestBody(BinaryData bodyContent, Headers headers) {
        if (bodyContent == null) {
            return EMPTY_REQUEST_BODY;
        }

        String contentType = headers.getValue(HeaderName.CONTENT_TYPE);
        MediaType mediaType = (contentType == null) ? null : MediaType.parse(contentType);

        if (bodyContent instanceof InputStreamBinaryData) {
            long effectiveContentLength = getRequestContentLength(bodyContent, headers);

            // The OkHttpInputStreamRequestBody doesn't read bytes until it's triggered by OkHttp dispatcher.
            return new OkHttpInputStreamRequestBody((InputStreamBinaryData) bodyContent, effectiveContentLength,
                mediaType);
        } else if (bodyContent instanceof FileBinaryData) {
            long effectiveContentLength = getRequestContentLength(bodyContent, headers);

            // The OkHttpFileRequestBody doesn't read bytes until it's triggered by OkHttp dispatcher.
            return new OkHttpFileRequestBody((FileBinaryData) bodyContent, effectiveContentLength, mediaType);
        } else {
            return RequestBody.create(bodyContent.toBytes(), mediaType);
        }
    }

    private static long getRequestContentLength(BinaryData content, Headers headers) {
        Long contentLength = content.getLength();

        if (contentLength == null) {
            String contentLengthHeaderValue = headers.getValue(HeaderName.CONTENT_LENGTH);

            if (contentLengthHeaderValue != null) {
                contentLength = Long.parseLong(contentLengthHeaderValue);
            } else {
                // -1 means that content length is unknown.
                contentLength = -1L;
            }
        }

        return contentLength;
    }

    private HttpResponse toHttpResponse(HttpRequest request, okhttp3.Response response,
                                               boolean eagerlyReadResponse, boolean ignoreResponseBody,
                                               boolean eagerlyConvertHeaders) throws IOException {
        okhttp3.Headers responseHeaders = response.headers();

        if (isTextEventStream(responseHeaders)) {
            ServerSentEventListener listener = request.getServerSentEventListener();
            if (listener != null && response.body() != null) {
                processTextEventStream(request, response.body().byteStream(), listener);
            } else {
                LOGGER.atInfo().log(() -> "No listener attached to the server sent event http request. "
                    + "Treating response as regular response.");
            }
            return new OkHttpBufferedResponse(response, request, EMPTY_BODY, eagerlyConvertHeaders);
        } else {
            if (eagerlyReadResponse || ignoreResponseBody) {
                try (ResponseBody body = response.body()) {
                    byte[] bytes = (body != null) ? body.bytes() : EMPTY_BODY;
                    return new OkHttpBufferedResponse(response, request, bytes, eagerlyConvertHeaders);
                }
            } else {
                return new OkHttpResponse(response, request, eagerlyConvertHeaders);
            }
        }
    }

    private void processTextEventStream(HttpRequest httpRequest, InputStream inputStream, ServerSentEventListener listener) {
        RetrySSEResult retrySSEResult;
        try (BufferedReader reader
                 = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            retrySSEResult = processBuffer(reader, listener);
            if (retrySSEResult != null) {
                retryExceptionForSSE(retrySSEResult, listener, httpRequest);
            }
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Processes the sse buffer and dispatches the event
     *
     * @param reader The BufferedReader object
     * @param listener The listener object attached with the httpRequest
     */
    private RetrySSEResult processBuffer(BufferedReader reader, ServerSentEventListener listener) {
        StringBuilder collectedData = new StringBuilder();
        ServerSentEvent event = null;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                collectedData.append(line).append("\n");
                if (isEndOfBlock(collectedData)) {
                    event = processLines(collectedData.toString().split("\n"));
                    if (!Objects.equals(event.getEvent(), DEFAULT_EVENT) || event.getData() != null) {
                        listener.onEvent(event);
                    }
                    collectedData = new StringBuilder(); // clear the collected data
                }
            }
            listener.onClose();
        } catch (IOException e) {
            return new RetrySSEResult(e,
                event != null ? event.getId() : -1,
                event != null ? ServerSentEventHelper.getRetryAfter(event) : null);
        }
        return null;
    }

    private boolean isEndOfBlock(StringBuilder sb) {
        // blocks of data are separated by double newlines
        // add more end of blocks here if needed
        return sb.indexOf("\n\n") >= 0;
    }

    private ServerSentEvent processLines(String[] lines) {
        List<String> eventData = null;
        ServerSentEvent event = new ServerSentEvent();

        for (String line : lines) {
            int idx = line.indexOf(':');
            if (idx == 0) {
                ServerSentEventHelper.setComment(event, line.substring(1).trim());
                continue;
            }
            String field = line.substring(0, idx < 0 ? lines.length : idx).trim().toLowerCase();
            String value = idx < 0 ? "" : line.substring(idx + 1).trim();

            switch (field) {
                case "event":
                    ServerSentEventHelper.setEvent(event, value);
                    break;
                case "data":
                    if(eventData == null) {
                        eventData = new ArrayList<>();
                    }
                    eventData.add(value);
                    break;
                case "id":
                    if (!value.isEmpty()) {
                        ServerSentEventHelper.setId(event, Long.parseLong(value));
                    }
                    break;
                case "retry":
                    if (!value.isEmpty() && DIGITS_ONLY.matcher(value).matches()) {
                        ServerSentEventHelper.setRetryAfter(event, Duration.ofMillis(Long.parseLong(value)));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid data received from server");
            }
        }

        if (event.getEvent() == null) {
            ServerSentEventHelper.setEvent(event, DEFAULT_EVENT);
        }
        if (eventData != null) {
            ServerSentEventHelper.setData(event, eventData);
        }

        return event;
    }

    /**
     * Retries the request if the listener allows it
     *
     * @param retrySSEResult  the result of the retry
     * @param listener The listener object attached with the httpRequest
     * @param httpRequest the HTTP Request being sent
     */
    private void retryExceptionForSSE(RetrySSEResult retrySSEResult, ServerSentEventListener listener, HttpRequest httpRequest) {
        if (Thread.currentThread().isInterrupted()
            || !listener.shouldRetry(retrySSEResult.getException(),
            retrySSEResult.getRetryAfter(),
            retrySSEResult.getLastEventId())) {
            listener.onError(retrySSEResult.getException());
            return;
        }

        if (retrySSEResult.getLastEventId() != -1) {
            httpRequest.getHeaders()
                .add(HeaderName.fromString(LAST_EVENT_ID), String.valueOf(retrySSEResult.getLastEventId()));
        }

        try {
            if (retrySSEResult.getRetryAfter() != null) {
                Thread.sleep(retrySSEResult.getRetryAfter().toMillis());
            }
        } catch (InterruptedException ignored) {
            return;
        }

        if (!Thread.currentThread().isInterrupted()) {
            this.send(httpRequest);
        }
    }

    private static boolean isTextEventStream(okhttp3.Headers responseHeaders) {
        return responseHeaders != null && responseHeaders.get(HeaderName.CONTENT_TYPE.toString()) != null &&
                Objects.equals(responseHeaders.get(HeaderName.CONTENT_TYPE.toString()), ContentType.TEXT_EVENT_STREAM);
    }

    /**
     * Inner class to hold the result for a retry of an SSE request
     */
    private static class RetrySSEResult {
        private final long lastEventId;
        private final Duration retryAfter;
        private final IOException ioException;

        public RetrySSEResult(IOException e, long lastEventId, Duration retryAfter) {
            this.ioException = e;
            this.lastEventId = lastEventId;
            this.retryAfter = retryAfter;
        }

        public long getLastEventId() {
            return lastEventId;
        }

        public Duration getRetryAfter() {
            return retryAfter;
        }

        public IOException getException() {
            return ioException;
        }
    }

}
