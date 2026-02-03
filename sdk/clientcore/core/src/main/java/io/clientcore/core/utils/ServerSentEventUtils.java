// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ServerSentEvent;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.implementation.utils.ServerSentEventHelper;
import io.clientcore.core.models.ServerSentResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for Server Sent Event handling.
 */
public final class ServerSentEventUtils {
    private static final String DEFAULT_EVENT = "message";
    private static final HttpHeaderName LAST_EVENT_ID = HttpHeaderName.fromString("Last-Event-Id");

    private ServerSentEventUtils() {
    }

    /**
     * Checks if the {@code Content-Type} is a text event stream.
     *
     * @param contentType The content type.
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
     * @return The non-null {@link ServerSentResult result} in case of an error.
     * @throws IOException If an error occurs while reading the data.
     */
    public static ServerSentResult processTextEventStream(InputStream inputStream, ServerSentEventListener listener)
        throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return processBuffer(reader, listener);
        }
    }

    /**
     * Retries reconnect to the {@link HttpRequest} if the {@link ServerSentResult#getRetryAfter()}
     * and {@link ServerSentResult#getLastEventId()} are not null.
     * <p>
     * The method will retry the request with the last event id set and after the retry time.
     * </p>
     *
     * @param serverSentResult The {@link ServerSentResult result} of the retry.
     * @param httpRequest The {@link HttpRequest} to send.
     * @return {@code true} if the request was retried; {@code false} otherwise.
     */
    public static boolean attemptRetry(ServerSentResult serverSentResult, HttpRequest httpRequest) {
        if (shouldDefaultRetry(serverSentResult.getRetryAfter())) {
            if (serverSentResult.getException() != null) {
                return false;
            }

            if (serverSentResult.getLastEventId() != null) {
                // Retry the request with the last event id set
                httpRequest.getHeaders().set(LAST_EVENT_ID, serverSentResult.getLastEventId());
            }

            // Retry the request after the retry time
            long millis = serverSentResult.getRetryAfter().toMillis();
            if (millis > 0) {
                try {
                    Thread.sleep(serverSentResult.getRetryAfter().toMillis());
                } catch (InterruptedException ignored) {
                    // ignored
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static boolean shouldDefaultRetry(Duration retryAfter) {
        return retryAfter != null;
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
    private static ServerSentResult processBuffer(BufferedReader reader, ServerSentEventListener listener) {
        ServerSentEvent event = null;
        List<String> allCollectedData = new ArrayList<>(); // List to store all collected data blocks

        try {
            StringBuilder collectedData = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                collectedData.append(line).append("\n");

                if (isEndOfBlock(collectedData)) {
                    String temp = collectedData.toString();
                    allCollectedData.add(temp.trim().replace("data: ", "")); // Add the collected data block to the list
                    event = processLines(collectedData.toString().split("\n"));

                    if (!Objects.equals(event.getEvent(), DEFAULT_EVENT) || event.getData() != null) {
                        listener.onEvent(event);
                    }

                    collectedData = new StringBuilder(); // clear the collected data
                }
            }

            listener.onClose();
        } catch (IOException e) {
            return new ServerSentResult(e, event != null ? event.getId() : null,
                event != null ? ServerSentEventHelper.getRetryAfter(event) : null, null);
        }

        return new ServerSentResult(null, event.getId(), ServerSentEventHelper.getRetryAfter(event), allCollectedData);
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
                        ServerSentEventHelper.setId(event, value);
                    }

                    break;

                case "retry":
                    if (!value.isEmpty() && isDigitsOnly(value)) {
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

    private static boolean isDigitsOnly(String str) {
        int length = str.length();

        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);

            if (c < '0' || c > '9') {
                return false;
            }
        }

        return true;
    }
}
