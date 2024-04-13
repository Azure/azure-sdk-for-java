// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ContentType;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ServerSentEvent;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.implementation.util.ServerSentEventHelper;

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
 * Utility class for Server Sent Event handling.
 */
public final class ServerSentEventUtils {
    private static final String DEFAULT_EVENT = "message";
    private static final Pattern DIGITS_ONLY = Pattern.compile("^[\\d]*$");
    private static final String LAST_EVENT_ID = "Last-Event-Id";
    public static final String NO_LISTENER_ERROR_MESSAGE = "No ServerSentEventListener attached to HttpRequest to "
        + "handle the text/event-stream response";

    private ServerSentEventUtils() {
    }

    /**
     * Checks if the {@code Content-Type} is a text event stream.
     *
     * @param contentType The content type.
     *
     * @return {@code true} if the content type is a text event stream.
     */
    public static boolean isTextEventStreamContentType(String contentType) {
        if (contentType != null) {
            return ContentType.TEXT_EVENT_STREAM.regionMatches(true, 0, contentType, 0,
                ContentType.TEXT_EVENT_STREAM.length());
        } else {
            return false;
        }
    }

    /**
     * Processes the text event stream.
     *
     * @param httpRequest The {@link HttpRequest} to send.
     * @param httpClient The {@link HttpClient} to send the {@code httpRequest} through.
     * @param inputStream The {@link InputStream} to read data from.
     * @param listener The {@link ServerSentEventListener event listener} attached to the {@link HttpRequest}.
     * @param logger The {@link ClientLogger} object to log errors with.
     */
    public static void processTextEventStream(HttpRequest httpRequest, HttpClient httpClient,
                                              InputStream inputStream, ServerSentEventListener listener,
                                              ClientLogger logger) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            RetrySSEResult retrySSEResult = processBuffer(reader, listener);

            if (retrySSEResult != null && !retryExceptionForSSE(retrySSEResult, listener, httpRequest)
                && !Thread.currentThread().isInterrupted()) {

                httpClient.send(httpRequest);
            }
        } catch (IOException e) {
            throw logger.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private static boolean isEndOfBlock(StringBuilder sb) {
        // Add more end of blocks here if needed. Blocks of data are separated by double newlines.
        return sb.lastIndexOf("\n\n") >= 0;
    }

    /**
     * Processes the SSE buffer and dispatches the event.
     *
     * @param reader The {@link BufferedReader} to read data from.
     * @param listener The {@link ServerSentEventListener} object attached to the {@link HttpRequest}.
     */
    private static RetrySSEResult processBuffer(BufferedReader reader, ServerSentEventListener listener) {
        ServerSentEvent event = null;

        try {
            StringBuilder collectedData = new StringBuilder();
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
            return new RetrySSEResult(e, event != null ? event.getId() : -1,
                event != null ? ServerSentEventHelper.getRetryAfter(event) : null);
        }

        return null;
    }

    /**
     * Retries the {@link HttpRequest} if the listener allows it.
     *
     * @param retrySSEResult The {@link RetrySSEResult result} of the retry.
     * @param listener The listener object attached with the {@link HttpRequest}.
     * @param httpRequest The {@link HttpRequest} to send.
     */
    private static boolean retryExceptionForSSE(RetrySSEResult retrySSEResult, ServerSentEventListener listener,
                                                HttpRequest httpRequest) {
        if (Thread.currentThread().isInterrupted() || !listener.shouldRetry(retrySSEResult.getException(),
            retrySSEResult.getRetryAfter(), retrySSEResult.getLastEventId())) {

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
                    // Ignore unknown fields.
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
     * Inner class to hold the result for a retry of an SSE request.
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
