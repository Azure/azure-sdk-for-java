// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util;

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
    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d*$");
    private static final HttpHeaderName LAST_EVENT_ID = HttpHeaderName.fromString("Last-Event-Id");
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
     * <p>
     * The passed {@link InputStream} will be closed by this method.
     *
     * @param inputStream The {@link InputStream} to read data from.
     * @param listener The {@link ServerSentEventListener event listener} attached to the {@link HttpRequest}.
     * @return The non-null {@link RetryServerSentResult result} in case of an error.
     * @throws IOException If an error occurs while reading the data.
     */
    public static RetryServerSentResult processTextEventStream(InputStream inputStream,
        ServerSentEventListener listener) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return processBuffer(reader, listener);
        }
    }

    /**
     * Retries the {@link HttpRequest} if the listener allows it.
     *
     * @param retryServerSentResult The {@link RetryServerSentResult result} of the retry.
     * @param listener The listener object attached with the {@link HttpRequest}.
     * @param httpRequest The {@link HttpRequest} to send.
     * @return {@code true} if the request should be retried.
     */
    public static boolean shouldRetry(RetryServerSentResult retryServerSentResult, ServerSentEventListener listener,
        HttpRequest httpRequest) {
        if (Thread.currentThread().isInterrupted() || !listener.shouldRetry(retryServerSentResult.getException(),
            retryServerSentResult.getRetryAfter(), retryServerSentResult.getLastEventId())) {

            listener.onError(retryServerSentResult.getException());

            return true;
        }

        if (retryServerSentResult.getLastEventId() != -1) {
            httpRequest.getHeaders()
                .add(LAST_EVENT_ID, String.valueOf(retryServerSentResult.getLastEventId()));
        }

        try {
            if (retryServerSentResult.getRetryAfter() != null) {
                Thread.sleep(retryServerSentResult.getRetryAfter().toMillis());
            }
        } catch (InterruptedException ignored) {
            return true;
        }

        return false;
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
    private static RetryServerSentResult processBuffer(BufferedReader reader, ServerSentEventListener listener) {
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
            return new RetryServerSentResult(e, event != null ? event.getId() : -1,
                event != null ? ServerSentEventHelper.getRetryAfter(event) : null);
        }

        return null;
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
}
