// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.util;

import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.ServerSentEvent;
import com.generic.core.http.models.ServerSentEventListener;
import com.generic.core.models.RetrySSEResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utility class for Server Sent Event handling
 */
public class ServerSentEventUtil {
    private static final String DEFAULT_EVENT = "message";
    private static final Pattern DIGITS_ONLY = Pattern.compile("^[\\d]*$");
    private static final String LAST_EVENT_ID = "Last-Event-Id";
    public static final String NO_LISTENER_LOG_MESSAGE = "No listener attached to the server sent event http request."
        + " Treating response as regular response.";
    private static boolean isEndOfBlock(StringBuilder sb) {
        // blocks of data are separated by double newlines
        // add more end of blocks here if needed
        return sb.indexOf("\n\n") >= 0;
    }

    /**
     * Processes the sse buffer and dispatches the event
     *
     * @param reader The BufferedReader object
     * @param listener The listener object attached with the httpRequest
     */
    public static RetrySSEResult processBuffer(BufferedReader reader, ServerSentEventListener listener) {
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
    public static boolean retryExceptionForSSE(RetrySSEResult retrySSEResult, ServerSentEventListener listener, HttpRequest httpRequest) {
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
}
