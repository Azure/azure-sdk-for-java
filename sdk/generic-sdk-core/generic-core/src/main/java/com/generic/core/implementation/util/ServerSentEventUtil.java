// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.util;

import com.generic.core.http.models.ContentType;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.ServerSentEvent;
import com.generic.core.http.models.ServerSentEventListener;
import com.generic.core.util.ClientLogger;

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
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Utility class for Server Sent Event handling
 */
public final class ServerSentEventUtil {
    private static final String DEFAULT_EVENT = "message";
    private static final Pattern DIGITS_ONLY = Pattern.compile("^[\\d]*$");
    private static final String LAST_EVENT_ID = "Last-Event-Id";
    public static final String NO_LISTENER_ERROR_MESSAGE = "No ServerSentEventListener attached to HttpRequest to "
            + "handle the text/event-stream response";

    private ServerSentEventUtil() {
    }

    /**
     * Checks if the content type is a text event stream
     *
     * @param contentType The content type
     * @return True if the content type is a text event stream
     */
    public static boolean isTextEventStreamContentType(String contentType) {
        if (contentType != null) {
            return ContentType.TEXT_EVENT_STREAM.regionMatches(true, 0, contentType, 0, ContentType.TEXT_EVENT_STREAM.length());
        } else {
            return false;
        }
    }

    /**
     * Processes the text event stream
     *
     * @param httpRequest The HTTP Request
     * @param httpRequestConsumer The HTTP Request consumer
     * @param inputStream The input stream
     * @param listener The listener object attached with the httpRequest
     * @param logger The logger object
     */
    public static void processTextEventStream(HttpRequest httpRequest, Consumer<HttpRequest> httpRequestConsumer,
                                              InputStream inputStream, ServerSentEventListener listener, ClientLogger logger) {
        RetrySSEResult retrySSEResult;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            retrySSEResult = processBuffer(reader, listener);
            if (retrySSEResult != null && !retryExceptionForSSE(retrySSEResult, listener, httpRequest)
                    && !Thread.currentThread().isInterrupted()) {
                httpRequestConsumer.accept(httpRequest);
            }
        } catch (IOException e) {
            throw logger.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private static boolean isEndOfBlock(StringBuilder sb) {
        // blocks of data are separated by double newlines
        // add more end of blocks here if needed
        return sb.lastIndexOf("\n\n") >= 0;
    }

    /**
     * Processes the sse buffer and dispatches the event
     *
     * @param reader The BufferedReader object
     * @param listener The listener object attached with the httpRequest
     */
    private static RetrySSEResult processBuffer(BufferedReader reader, ServerSentEventListener listener) {
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

    /**
     * Retries the request if the listener allows it
     *
     * @param retrySSEResult  the result of the retry
     * @param listener The listener object attached with the httpRequest
     * @param httpRequest the HTTP Request being sent
     */
    private static boolean retryExceptionForSSE(RetrySSEResult retrySSEResult, ServerSentEventListener listener, HttpRequest httpRequest) {
        if (Thread.currentThread().isInterrupted()
                || !listener.shouldRetry(retrySSEResult.getException(),
                retrySSEResult.getRetryAfter(),
                retrySSEResult.getLastEventId())) {
            listener.onError(retrySSEResult.getException());
            return true;
        }

        if (retrySSEResult.getLastEventId() != -1) {
            httpRequest.getHeaders()
                    .add(HttpHeaderName.fromString(LAST_EVENT_ID), String.valueOf(retrySSEResult.getLastEventId()));
        }

        try {
            if (retrySSEResult.getRetryAfter() != null) {
                Thread.sleep(retrySSEResult.getRetryAfter().toMillis());
            }
        } catch (InterruptedException ignored) {
            return true;
        }
        return false;
    }

    private static ServerSentEvent processLines(String[] lines) {
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
                    if (eventData == null) {
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
                    // ignore unknown fields
                    break;
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
     * Inner Class to hold the result for a retry of an SSE request
     */
    private static class RetrySSEResult {
        private final long lastEventId;
        private final Duration retryAfter;
        private final IOException ioException;

        RetrySSEResult(IOException e, long lastEventId, Duration retryAfter) {
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
