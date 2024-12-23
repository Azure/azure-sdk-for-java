package io.clientcore.core.implementation.observability.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.TRACE_FLAGS_CLASS;

public class TraceFlags {

    private final static int HEX_LENGTH;
    private final static TraceFlags DEFAULT;
    private final static TraceFlags SAMPLED;

    private static final ClientLogger LOGGER = new ClientLogger(TraceFlags.class);
    private final static ReflectiveInvoker FROM_HEX_INVOKER;
    private final static ReflectiveInvoker FROM_BYTE_INVOKER;
    private final static ReflectiveInvoker IS_SAMPLED_INVOKER;
    private final static ReflectiveInvoker AS_HEX_INVOKER;
    private final static ReflectiveInvoker AS_BYTE_INVOKER;
    private final Object otelTraceFlags;

    static {
        int hexLength = 0;
        Object defaultInstance = null;
        Object sampledInstance = null;

        ReflectiveInvoker fromHexInvoker = null;
        ReflectiveInvoker fromByteInvoker = null;
        ReflectiveInvoker isSampledInvoker = null;
        ReflectiveInvoker asHexInvoker = null;
        ReflectiveInvoker asByteInvoker = null;

        try {
            //hexLength = otelTraceFlagsClass.getField("HEX_LENGTH").getInt(null);

            //defaultInstance = otelTraceFlagsClass.getField("DEFAULT").get(null);
            //sampledInstance = otelTraceFlagsClass.getField("SAMPLED").get(null);

            fromHexInvoker = ReflectionUtils.getMethodInvoker(TRACE_FLAGS_CLASS,
                TRACE_FLAGS_CLASS.getMethod("fromHex", CharSequence.class, int.class));
            fromByteInvoker = ReflectionUtils.getMethodInvoker(TRACE_FLAGS_CLASS,
                TRACE_FLAGS_CLASS.getMethod("fromByte", byte.class));
            isSampledInvoker = ReflectionUtils.getMethodInvoker(TRACE_FLAGS_CLASS,
                TRACE_FLAGS_CLASS.getMethod("isSampled"));
            asHexInvoker = ReflectionUtils.getMethodInvoker(TRACE_FLAGS_CLASS,
                TRACE_FLAGS_CLASS.getMethod("asHex"));
            asByteInvoker = ReflectionUtils.getMethodInvoker(TRACE_FLAGS_CLASS,
                TRACE_FLAGS_CLASS.getMethod("asByte"));
        } catch (Throwable t) {
            OTelInitializer.INSTANCE.initError(LOGGER, t);
        }

        HEX_LENGTH = hexLength;
        DEFAULT = new TraceFlags(defaultInstance);
        SAMPLED = new TraceFlags(sampledInstance);

        FROM_HEX_INVOKER = fromHexInvoker;
        FROM_BYTE_INVOKER = fromByteInvoker;
        IS_SAMPLED_INVOKER = isSampledInvoker;
        AS_HEX_INVOKER = asHexInvoker;
        AS_BYTE_INVOKER = asByteInvoker;
    }

    TraceFlags(Object otelTraceFlags) {
        this.otelTraceFlags = otelTraceFlags;
    }

    static int getLength() {
        return HEX_LENGTH;
    }

    static TraceFlags getDefault() {
        return DEFAULT;
    }

    static TraceFlags getSampled() {
        return SAMPLED;
    }

    static TraceFlags fromHex(CharSequence src, int srcOffset) {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                Object otelTraceFlags = FROM_HEX_INVOKER.invokeStatic(src, srcOffset);
                return new TraceFlags(otelTraceFlags);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return DEFAULT;
    }

    static TraceFlags fromByte(byte traceFlagsByte) {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                return new TraceFlags(FROM_BYTE_INVOKER.invokeStatic(traceFlagsByte));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return DEFAULT;
    }

    boolean isSampled() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelTraceFlags != null) {
            try {
                return (boolean) IS_SAMPLED_INVOKER.invokeWithArguments(otelTraceFlags);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return false;
    }

    String asHex() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelTraceFlags != null) {
            try {
                return (String) AS_HEX_INVOKER.invokeWithArguments(otelTraceFlags);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return null;
    }

    byte asByte() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelTraceFlags != null) {
            try {
                return (byte) AS_BYTE_INVOKER.invokeWithArguments(otelTraceFlags);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return 0;
    }
}
