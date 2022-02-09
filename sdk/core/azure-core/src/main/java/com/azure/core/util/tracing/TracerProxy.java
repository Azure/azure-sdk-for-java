// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.tracing;

import com.azure.core.util.Context;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This class provides a means for all client libraries to augment the context information they have received from an
 * end user with additional distributed tracing information, that may then be passed on to a backend for analysis.
 *
 * @see Tracer
 */
public final class TracerProxy {
    /*
     * AutoCloseable implementation which performs a no-op when close() is called.
     */
    static final AutoCloseable NOOP_AUTOCLOSEABLE = () -> { };

    private static Tracer tracer;

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't/ depend on the
        // System classloader to load Tracer classes.
        ServiceLoader<Tracer> serviceLoader = ServiceLoader.load(Tracer.class, TracerProxy.class.getClassLoader());
        Iterator<?> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            tracer = serviceLoader.iterator().next();
        }
    }

    private TracerProxy() {
        // no-op
    }

    /**
     * A new tracing span with INTERNAL kind is created for each {@link Tracer tracer} plugged into the SDK.
     * <p>
     * The {@code context} will be checked for information about a parent span. If a parent span is found, the new span
     * will be added as a child. Otherwise, the parent span will be created and added to the {@code context} and any
     * downstream {@code start()} calls will use the created span as the parent.
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated {@link Context} object.
     */
    public static Context start(String methodName, Context context) {
        if (tracer == null) {
            return context;
        }
        return tracer.start(methodName, context);
    }

    /**
     * A new tracing span is created for each {@link Tracer tracer} plugged into the SDK.
     * <p>
     * The {@code context} will be checked for information about a parent span. If a parent span is found, the new span
     * will be added as a child. Otherwise, the parent span will be created and added to the {@code context} and any
     * downstream {@code start()} calls will use the created span as the parent.
     *
     * @param methodName Name of the method triggering the span creation.
     * @param spanOptions span creation options.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated {@link Context} object.
     */
    public static Context start(String methodName, StartSpanOptions spanOptions, Context context) {
        if (tracer == null) {
            return context;
        }
        return tracer.start(methodName, spanOptions, context);
    }

    /**
     * For the plugged in {@link Tracer tracer}, the key-value pair metadata is added to its current span. If the {@code
     * context} does not contain a span, then no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    public static void setAttribute(String key, String value, Context context) {
        if (tracer == null) {
            return;
        }
        tracer.setAttribute(key, value, context);
    }

    /**
     * For the plugged in {@link Tracer tracer}, its current tracing span is marked as completed.
     *
     * @param responseCode Response status code if the span is in an HTTP call context.
     * @param error {@link Throwable} that happened during the span or {@code null} if no exception occurred.
     * @param context Additional metadata that is passed through the call stack.
     */
    public static void end(int responseCode, Throwable error, Context context) {
        if (tracer == null) {
            return;
        }
        tracer.end(responseCode, error, context);
    }

    /**
     * Sets the span name for each {@link Tracer tracer} plugged into the SDK.
     *
     * @param spanName Name of the span.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated {@link Context} object.
     */
    public static Context setSpanName(String spanName, Context context) {
        if (tracer == null) {
            return context;
        }
        return tracer.setSpanName(spanName, context);
    }

    /**
     * Returns true if tracing is enabled.
     *
     * @return true if tracing is enabled.
     */
    public static boolean isTracingEnabled() {
        return tracer != null;
    }
}
