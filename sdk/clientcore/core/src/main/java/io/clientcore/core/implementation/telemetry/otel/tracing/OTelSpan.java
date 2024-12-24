// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.otel.OTelAttributeKey;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.Scope;
import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.SpanContext;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.STATUS_CODE_CLASS;

public class OTelSpan implements Span {
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpan.class);
    private static final Scope NOOP_SCOPE = new Scope() {
    };
    private static final ReflectiveInvoker SET_ATTRIBUTE_INVOKER;
    private static final ReflectiveInvoker SET_STATUS_INVOKER;
    private static final ReflectiveInvoker END_INVOKER;
    private static final ReflectiveInvoker GET_SPAN_CONTEXT_INVOKER;
    private static final ReflectiveInvoker IS_RECORDING_INVOKER;
    private static final ReflectiveInvoker MAKE_CURRENT_INVOKER;
    private static final Object ERROR_STATUS_CODE;
    private final Object otelSpan;

    static {
        ReflectiveInvoker setAttributeInvoker = null;
        ReflectiveInvoker setStatusInvoker = null;
        ReflectiveInvoker endInvoker = null;
        ReflectiveInvoker getSpanContextInvoker = null;
        ReflectiveInvoker isRecordingInvoker = null;
        ReflectiveInvoker makeCurrentInvoker = null;

        Object errorStatusCode = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                setAttributeInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("setAttribute", ATTRIBUTE_KEY_CLASS, Object.class));

                setStatusInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("setStatus", STATUS_CODE_CLASS, String.class));

                endInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("end"));

                isRecordingInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("isRecording"));

                getSpanContextInvoker
                    = ReflectionUtils.getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("getSpanContext"));

                makeCurrentInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("makeCurrent"));

                errorStatusCode = STATUS_CODE_CLASS.getField("ERROR").get(null);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        SET_ATTRIBUTE_INVOKER = setAttributeInvoker;
        SET_STATUS_INVOKER = setStatusInvoker;
        END_INVOKER = endInvoker;
        GET_SPAN_CONTEXT_INVOKER = getSpanContextInvoker;
        IS_RECORDING_INVOKER = isRecordingInvoker;
        MAKE_CURRENT_INVOKER = makeCurrentInvoker;
        ERROR_STATUS_CODE = errorStatusCode;
    }

    OTelSpan(Object otelSpan) {
        this.otelSpan = otelSpan;
    }

    public OTelSpan setAttribute(String key, Object value) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpan != null) {
            try {
                SET_ATTRIBUTE_INVOKER.invokeWithArguments(otelSpan, OTelAttributeKey.getKey(key, value),
                    OTelAttributeKey.castAttributeValue(value));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public Span setError(String errorType) {
        setAttribute("error.type", errorType);
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpan != null) {
            try {
                SET_STATUS_INVOKER.invokeWithArguments(otelSpan, ERROR_STATUS_CODE, null);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public Span setError(Throwable error) {
        setAttribute("error.type", error.getClass().getCanonicalName());
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpan != null) {
            try {
                SET_STATUS_INVOKER.invokeWithArguments(otelSpan, ERROR_STATUS_CODE, error.getMessage());
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public void end() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpan != null) {
            try {
                END_INVOKER.invokeWithArguments(otelSpan);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }
    }

    @Override
    public SpanContext getSpanContext() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpan != null) {
            try {
                return new OTelSpanContext(GET_SPAN_CONTEXT_INVOKER.invokeWithArguments(otelSpan));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return OTelSpanContext.getInvalid();
    }

    @Override
    public boolean isRecording() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpan != null) {
            try {
                return (boolean) IS_RECORDING_INVOKER.invokeWithArguments(otelSpan);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return false;
    }

    @Override
    public Scope makeCurrent() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpan != null) {
            try {
                Object scope = MAKE_CURRENT_INVOKER.invokeWithArguments(otelSpan);
                if (scope instanceof AutoCloseable) {
                    return new Scope() {
                        @Override
                        public void close() {
                            try {
                                ((AutoCloseable) scope).close();
                            } catch (Exception e) {
                                OTelInitializer.INSTANCE.runtimeError(LOGGER, e);
                            }
                        }
                    };
                } else {
                    OTelInitializer.INSTANCE.runtimeError(LOGGER, "makeCurrent returned non-AutoCloseable");
                }
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return NOOP_SCOPE;
    }
}
