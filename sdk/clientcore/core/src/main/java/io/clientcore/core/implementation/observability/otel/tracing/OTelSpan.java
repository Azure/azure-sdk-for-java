package io.clientcore.core.implementation.observability.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.observability.otel.OTelAttributeKey;
import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.observability.Attributes;
import io.clientcore.core.observability.Scope;
import io.clientcore.core.observability.tracing.Span;
import io.clientcore.core.observability.tracing.SpanContext;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.ATTRIBUTES_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.SPAN_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.STATUS_CODE_CLASS;

public class OTelSpan implements Span {
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpan.class);
    private static final Scope NOOP_SCOPE = new Scope() {};
    private final static OTelSpan INVALID;
    private final static ReflectiveInvoker FROM_CONTEXT_INVOKER;
    private final static ReflectiveInvoker SET_ATTRIBUTE_INVOKER;
    private final static ReflectiveInvoker SET_STATUS_INVOKER;
    private final static ReflectiveInvoker ADD_LINK_INVOKER;
    private final static ReflectiveInvoker END_INVOKER;
    private final static ReflectiveInvoker GET_SPAN_CONTEXT_INVOKER;
    private final static ReflectiveInvoker IS_RECORDING_INVOKER;
    private final static ReflectiveInvoker MAKE_CURRENT_INVOKER;

    private final Object otelSpan;
    static {
        ReflectiveInvoker fromContextInvoker = null;
        ReflectiveInvoker setAttributeInvoker = null;
        ReflectiveInvoker setStatusInvoker = null;
        ReflectiveInvoker addLinkInvoker = null;
        ReflectiveInvoker endInvoker = null;
        ReflectiveInvoker getSpanContextInvoker = null;
        ReflectiveInvoker isRecordingInvoker = null;
        ReflectiveInvoker makeCurrentInvoker = null;

        Object invalidInstance = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                fromContextInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("fromContext", CONTEXT_CLASS));

                ReflectiveInvoker getInvalidInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("getInvalid"));

                invalidInstance = getInvalidInvoker.invokeStatic();

                setAttributeInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("setAttribute", ATTRIBUTE_KEY_CLASS, Object.class));

                setStatusInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("setStatus", STATUS_CODE_CLASS, String.class));

                addLinkInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("addLink", SPAN_CONTEXT_CLASS, ATTRIBUTES_CLASS));

                endInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("end"));

                isRecordingInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("isRecording"));

                getSpanContextInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("getSpanContext"));

                makeCurrentInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("makeCurrent"));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        INVALID = new OTelSpan(invalidInstance);
        FROM_CONTEXT_INVOKER = fromContextInvoker;
        SET_ATTRIBUTE_INVOKER = setAttributeInvoker;
        SET_STATUS_INVOKER = setStatusInvoker;
        ADD_LINK_INVOKER = addLinkInvoker;
        END_INVOKER = endInvoker;
        GET_SPAN_CONTEXT_INVOKER = getSpanContextInvoker;
        IS_RECORDING_INVOKER = isRecordingInvoker;
        MAKE_CURRENT_INVOKER = makeCurrentInvoker;
    }

    OTelSpan(Object otelSpan) {
        this.otelSpan = otelSpan;
    }

    // do we need it?
    static OTelSpan fromContext(Context context) {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                return new OTelSpan(FROM_CONTEXT_INVOKER.invokeStatic(context));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return INVALID;
    }

    public OTelSpan setAttribute(String key, Object value) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpan != null) {
            try {
                SET_ATTRIBUTE_INVOKER.invokeWithArguments(otelSpan, OTelAttributeKey.getKey(key, value), OTelAttributeKey.castAttributeValue(value));
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
                SET_STATUS_INVOKER.invokeWithArguments(otelSpan, OTelStatusCode.getErrorStatusCode(), null);
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
                SET_STATUS_INVOKER.invokeWithArguments(otelSpan, OTelStatusCode.getErrorStatusCode(), error.getMessage());
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public OTelSpan addLink(SpanContext spanContext, Attributes attributes) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpan != null) {
            try {
                ADD_LINK_INVOKER.invokeWithArguments(otelSpan, spanContext, attributes);
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
                                ((AutoCloseable)scope).close();
                            } catch (Exception e) {
                                OTelInitializer.INSTANCE.runtimeError(LOGGER, e);
                            }
                        }
                    } ;
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
