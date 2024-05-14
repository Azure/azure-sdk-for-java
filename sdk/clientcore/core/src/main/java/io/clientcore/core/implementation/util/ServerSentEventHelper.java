// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

import io.clientcore.core.http.models.ServerSentEvent;

import java.time.Duration;
import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link ServerSentEvent} instance.
 */
public final class ServerSentEventHelper {
    private static ServerSentEventAccessor accessor;

    private ServerSentEventHelper() {
    }

    /**
     * The method called from {@link ServerSentEvent} to set it's accessor.
     *
     * @param serverSentEventAccessor The accessor.
     */
    public static void setAccessor(final ServerSentEventHelper.ServerSentEventAccessor serverSentEventAccessor) {
        accessor = serverSentEventAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ServerSentEvent} instance.
     */
    public interface ServerSentEventAccessor {
        void setId(ServerSentEvent serverSentEvent, String id);
        void setEvent(ServerSentEvent serverSentEvent, String event);
        void setData(ServerSentEvent serverSentEvent, List<String> data);
        void setComment(ServerSentEvent serverSentEvent, String comment);
        void setRetryAfter(ServerSentEvent serverSentEvent, Duration retryAfter);
        Duration getRetryAfter(ServerSentEvent serverSentEvent);
    }

    public static void setId(ServerSentEvent serverSentEvent, String id) {
        accessor.setId(serverSentEvent, id);
    }

    public static void setEvent(ServerSentEvent serverSentEvent, String event) {
        accessor.setEvent(serverSentEvent, event);
    }

    public static void setData(ServerSentEvent serverSentEvent, List<String> data) {
        accessor.setData(serverSentEvent, data);
    }

    public static void setComment(ServerSentEvent serverSentEvent, String comment) {
        accessor.setComment(serverSentEvent, comment);
    }

    public static void setRetryAfter(ServerSentEvent serverSentEvent, Duration retryAfter) {
        accessor.setRetryAfter(serverSentEvent, retryAfter);
    }

    public static Duration getRetryAfter(ServerSentEvent serverSentEvent) {
        return accessor.getRetryAfter(serverSentEvent);
    }
}

