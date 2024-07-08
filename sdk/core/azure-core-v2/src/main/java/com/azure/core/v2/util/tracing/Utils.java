// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.tracing;

import io.clientcore.core.util.Context;
import io.clientcore.core.util.ClientLogger;

import static com.azure.core.v2.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.v2.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.v2.util.tracing.Tracer.MESSAGE_ENQUEUED_TIME;

final class Utils {

    static final String SPAN_KIND_KEY = "span-kind";
    static final String START_TIME_KEY = "span-start-time";

    static final AutoCloseable NOOP_CLOSEABLE = () -> {
    };
    static final ClientLogger LOGGER = new ClientLogger(Utils.class);

    private Utils() {
    }

    @SuppressWarnings("deprecation")
    static void addMessagingAttributes(StartSpanOptions spanOptions, Context context) {
        String entityPath = getOrNull(context, ENTITY_PATH_KEY, String.class);
        if (entityPath != null) {
            spanOptions.setAttribute(ENTITY_PATH_KEY, entityPath);
        }
        String hostName = getOrNull(context, HOST_NAME_KEY, String.class);
        if (hostName != null) {
            spanOptions.setAttribute(HOST_NAME_KEY, hostName);
        }
        Long messageEnqueuedTime = getOrNull(context, MESSAGE_ENQUEUED_TIME, Long.class);
        if (messageEnqueuedTime != null) {
            spanOptions.setAttribute(MESSAGE_ENQUEUED_TIME, messageEnqueuedTime);
        }
    }

    /**
     * Returns the value of the specified key from the context.
     *
     * @param key The name of the attribute that needs to be extracted from the {@link Context}.
     * @param clazz clazz the type of raw class to find data for.
     * @param context The context containing the specified key.
     * @return The T type of raw class object
     */
    @SuppressWarnings("unchecked")
    static <T> T getOrNull(Context context, String key, Class<T> clazz) {
        final Object data = context.get(key);
        if (data != null && clazz.isAssignableFrom(data.getClass())) {
            return (T) data;
        }

        return null;
    }
}
