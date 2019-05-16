// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.tracing;

import com.azure.core.util.Context;

/**
 * Contract that all tracers must implement to be plug-able into the SDK.
 */
public interface Tracer {
    /**
     * Creates a new tracing span.
     *
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found the
     * new span will be added as a child, otherwise the span will be created and added to the context and any downstream
     * start calls will use the created span as the parent.
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    Context start(String methodName, Context context);

    /**
     * Completes the current tracing span.
     *
     * @param responseCode Response status code if the span is in a HTTP call context.
     * @param error Potential throwable that happened during the span.
     * @param context Additional metadata that is passed through the call stack.
     */
    void end(int responseCode, Throwable error, Context context);

    /**
     * Adds metadata to the current span. The {@code context} is checked for having span information, if no span
     * information is found in the context no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    void setAttribute(String key, String value, Context context);

    /*
     * Adds metadata to the current span. The {@code context} is checked for having span information, if no span
     * information is found in the context no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    // void setAttribute(String key, long value, Context context);

    /*
     * Adds metadata to the current span. The {@code context} is checked for having span information, if no span
     * information is found in the context no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    // void setAttribute(String key, double value, Context context);

    /*
     * Adds metadata to the current span. The {@code context} is checked for having span information, if no span
     * information is found in the context no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    // void setAttribute(String key, boolean value, Context context);
}
