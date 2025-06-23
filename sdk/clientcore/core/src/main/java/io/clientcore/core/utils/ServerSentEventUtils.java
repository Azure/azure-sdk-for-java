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

import java.io.IOException;
import java.io.InputStream;
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
        try (InputStream is = inputStream) {
            return processBuffer(is, listener);
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
     * @param inputStream The {@link InputStream} to read data from.
     * @param listener The {@link ServerSentEventListener} object attached to the {@link HttpRequest}.
     */
    private static ServerSentResult processBuffer(InputStream inputStream, ServerSentEventListener listener) {
        ServerSentEvent event = null;
        List<String> allCollectedData = new ArrayList<>(); // List to store all collected data blocks

        try {
            StringBuilder collectedData = new StringBuilder();

            // Use a byte array to accumulate UTF-8 bytes and convert to string line by line
            List<Byte> lineBytes = new ArrayList<>();

            int b;
            while ((b = inputStream.read()) != -1) {
                byte currentByte = (byte) b;

                if (currentByte == '\n') {
                    // End of line found, convert accumulated bytes to string
                    String line = convertBytesToString(lineBytes);
                    collectedData.append(line).append("\n");
                    lineBytes.clear();

                    if (isEndOfBlock(collectedData)) {
                        String temp = collectedData.toString();
                        allCollectedData.add(temp.trim().replace("data: ", "")); // Add the collected data block to the list
                        event = processLines(collectedData.toString().split("\n"));

                        if (!Objects.equals(event.getEvent(), DEFAULT_EVENT) || event.getData() != null) {
                            listener.onEvent(event);
                        }

                        collectedData = new StringBuilder(); // clear the collected data
                    }
                } else if (currentByte == '\r') {
                    // Handle \r\n or standalone \r - peek ahead to see if \n follows
                    int next = inputStream.read();
                    if (next == -1) {
                        // End of stream, treat \r as line ending
                        String line = convertBytesToString(lineBytes);
                        collectedData.append(line).append("\n");
                        lineBytes.clear();

                        if (isEndOfBlock(collectedData)) {
                            String temp = collectedData.toString();
                            allCollectedData.add(temp.trim().replace("data: ", "")); // Add the collected data block to the list
                            event = processLines(collectedData.toString().split("\n"));

                            if (!Objects.equals(event.getEvent(), DEFAULT_EVENT) || event.getData() != null) {
                                listener.onEvent(event);
                            }

                            collectedData = new StringBuilder(); // clear the collected data
                        }
                        break;
                    } else if (next == '\n') {
                        // \r\n found, end of line
                        String line = convertBytesToString(lineBytes);
                        collectedData.append(line).append("\n");
                        lineBytes.clear();

                        if (isEndOfBlock(collectedData)) {
                            String temp = collectedData.toString();
                            allCollectedData.add(temp.trim().replace("data: ", "")); // Add the collected data block to the list
                            event = processLines(collectedData.toString().split("\n"));

                            if (!Objects.equals(event.getEvent(), DEFAULT_EVENT) || event.getData() != null) {
                                listener.onEvent(event);
                            }

                            collectedData = new StringBuilder(); // clear the collected data
                        }
                    } else {
                        // Standalone \r, treat as line ending and add the next byte to line bytes
                        String line = convertBytesToString(lineBytes);
                        collectedData.append(line).append("\n");
                        lineBytes.clear();
                        lineBytes.add((byte) next);

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
                } else {
                    // Regular byte, add to current line
                    lineBytes.add(currentByte);
                }
            }

            // Handle any remaining data that didn't end with a newline
            if (!lineBytes.isEmpty()) {
                String line = convertBytesToString(lineBytes);
                collectedData.append(line).append("\n");

                if (isEndOfBlock(collectedData)) {
                    String temp = collectedData.toString();
                    allCollectedData.add(temp.trim().replace("data: ", "")); // Add the collected data block to the list
                    event = processLines(collectedData.toString().split("\n"));

                    if (!Objects.equals(event.getEvent(), DEFAULT_EVENT) || event.getData() != null) {
                        listener.onEvent(event);
                    }
                }
            }

            listener.onClose();
        } catch (IOException e) {
            return new ServerSentResult(e, event != null ? event.getId() : null,
                event != null ? ServerSentEventHelper.getRetryAfter(event) : null, null);
        }

        return new ServerSentResult(null, event != null ? event.getId() : null,
            event != null ? ServerSentEventHelper.getRetryAfter(event) : null, allCollectedData);
    }

    /**
     * Converts a list of bytes to a UTF-8 string.
     */
    private static String convertBytesToString(List<Byte> bytes) {
        if (bytes.isEmpty()) {
            return "";
        }

        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            byteArray[i] = bytes.get(i);
        }

        return new String(byteArray, StandardCharsets.UTF_8);
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
