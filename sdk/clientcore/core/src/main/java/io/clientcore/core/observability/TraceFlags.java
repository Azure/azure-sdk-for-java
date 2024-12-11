package io.clientcore.core.observability;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;

public class TraceFlags {
    final static Class<?> OTEL_TRACE_FLAGS_CLASS;

    private final static int HEX_LENGTH;
    private final static TraceFlags DEFAULT;
    private final static TraceFlags SAMPLED;

    private final static ReflectiveInvoker FROM_HEX_INVOKER;
    private final static ReflectiveInvoker FROM_BYTE_INVOKER;
    private final static ReflectiveInvoker IS_SAMPLED_INVOKER;
    private final static ReflectiveInvoker AS_HEX_INVOKER;
    private final static ReflectiveInvoker AS_BYTE_INVOKER;

    private volatile Object otelTraceFlags;

    static {
        Class<?> otelTraceFlagsClass;

        int hexLength = 0;
        Object defaultInstance = null;
        Object sampledInstance = null;

        ReflectiveInvoker fromHexInvoker;
        ReflectiveInvoker fromByteInvoker;
        ReflectiveInvoker isSampledInvoker;
        ReflectiveInvoker asHexInvoker;
        ReflectiveInvoker asByteInvoker;

        try {
            otelTraceFlagsClass = Class.forName("io.opentelemetry.api.trace.TraceFlags", true, TraceFlags.class.getClassLoader());
            //hexLength = otelTraceFlagsClass.getField("HEX_LENGTH").getInt(null);

            //defaultInstance = otelTraceFlagsClass.getField("DEFAULT").get(null);
            //sampledInstance = otelTraceFlagsClass.getField("SAMPLED").get(null);

            fromHexInvoker = ReflectionUtils.getMethodInvoker(otelTraceFlagsClass,
                otelTraceFlagsClass.getMethod("fromHex", CharSequence.class, int.class));
            fromByteInvoker = ReflectionUtils.getMethodInvoker(otelTraceFlagsClass,
                otelTraceFlagsClass.getMethod("fromByte", byte.class));
            isSampledInvoker = ReflectionUtils.getMethodInvoker(otelTraceFlagsClass,
                otelTraceFlagsClass.getMethod("isSampled"));
            asHexInvoker = ReflectionUtils.getMethodInvoker(otelTraceFlagsClass,
                otelTraceFlagsClass.getMethod("asHex"));
            asByteInvoker = ReflectionUtils.getMethodInvoker(otelTraceFlagsClass,
                otelTraceFlagsClass.getMethod("asByte"));

        } catch (Exception e) {
            otelTraceFlagsClass = null;
            hexLength = 0;
            defaultInstance = null;
            sampledInstance = null;

            fromHexInvoker = null;
            fromByteInvoker = null;
            isSampledInvoker = null;
            asHexInvoker = null;
            asByteInvoker = null;
        }

        OTEL_TRACE_FLAGS_CLASS = otelTraceFlagsClass;
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
        if (FROM_HEX_INVOKER == null) {
            return DEFAULT;
        }

        try {
            Object otelTraceFlags = FROM_HEX_INVOKER.invokeStatic(src, srcOffset);
            return new TraceFlags(otelTraceFlags);
        } catch (Throwable e) {
            // TODO log

            return DEFAULT;
        }

    }
    static TraceFlags fromByte(byte traceFlagsByte) {
        if (FROM_BYTE_INVOKER == null) {
            return DEFAULT;
        }

        try {
            Object otelTraceFlags = FROM_BYTE_INVOKER.invokeStatic(traceFlagsByte);
            return new TraceFlags(otelTraceFlags);
        } catch (Throwable e) {
            // TODO log
            return DEFAULT;
        }
    }

    boolean isSampled() {
        if (otelTraceFlags == null || IS_SAMPLED_INVOKER == null) {
            return false;
        }

        try {
            return (boolean) IS_SAMPLED_INVOKER.invokeWithArguments(otelTraceFlags);
        } catch (Throwable e) {
            otelTraceFlags = null;
            // TODO log
            return false;
        }
    }

    String asHex() {
        if (otelTraceFlags == null || AS_HEX_INVOKER == null) {
            return null;
        }

        try {
            return (String) AS_HEX_INVOKER.invokeWithArguments(otelTraceFlags);
        } catch (Throwable e) {
            otelTraceFlags = null;
            // TODO log
            return null;
        }
    }

    byte asByte() {
        if (otelTraceFlags == null || AS_BYTE_INVOKER == null) {
            return 0;
        }

        try {
            return (byte) AS_BYTE_INVOKER.invokeWithArguments(otelTraceFlags);
        } catch (Throwable e) {
            otelTraceFlags = null;
            // TODO log
            return 0;
        }
    }
}
