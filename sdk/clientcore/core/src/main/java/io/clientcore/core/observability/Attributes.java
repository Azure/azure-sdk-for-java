package io.clientcore.core.observability;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;

public class Attributes {

    static final Class<?> OTEL_ATTRIBUTES_CLASS;

    private static final Attributes EMPTY;

    private final static ReflectiveInvoker SIZE_INVOKER;
    private final static ReflectiveInvoker IS_EMPTY_INVOKER;
    private final static ReflectiveInvoker BUILDER_INVOKER;


    private volatile Object otelAttributes;
    static {
        Class<?> otelAttributesClass;

        ReflectiveInvoker sizeInvoker;
        ReflectiveInvoker isEmptyInvoker;
        ReflectiveInvoker builderInvoker;

        Object emptyInstance;
        try {
            otelAttributesClass = Class.forName("io.opentelemetry.api.common.Attributes", true, Span.class.getClassLoader());

            sizeInvoker = ReflectionUtils.getMethodInvoker(otelAttributesClass,
                otelAttributesClass.getMethod("size"));

            isEmptyInvoker = ReflectionUtils.getMethodInvoker(otelAttributesClass,
                otelAttributesClass.getMethod("isEmpty"));

            builderInvoker = ReflectionUtils.getMethodInvoker(otelAttributesClass,
                otelAttributesClass.getMethod("builder"));

            ReflectiveInvoker emptyInvoker = ReflectionUtils.getMethodInvoker(otelAttributesClass,
                otelAttributesClass.getMethod("empty"));

            emptyInstance = emptyInvoker.invokeStatic();

        } catch (Exception e) {
            otelAttributesClass = null;

            sizeInvoker = null;
            isEmptyInvoker = null;
            builderInvoker = null;

            emptyInstance = null;
            // TODO
        }

        OTEL_ATTRIBUTES_CLASS = otelAttributesClass;

        SIZE_INVOKER = sizeInvoker;
        IS_EMPTY_INVOKER = isEmptyInvoker;
        BUILDER_INVOKER = builderInvoker;

        EMPTY = new Attributes(emptyInstance);
    }

    Attributes(Object otelAttributes) {
        this.otelAttributes = otelAttributes;
    }


    int size() {
        if (otelAttributes == null || SIZE_INVOKER == null) {
            return 0;
        }

        try {
            return (int) SIZE_INVOKER.invokeWithArguments(otelAttributes);
        } catch (Throwable e) {
            // TODO log
            otelAttributes= null;
            return 0;
        }
    }

    boolean isEmpty() {
        if (otelAttributes == null || IS_EMPTY_INVOKER == null) {
            return true;
        }

        try {
            return (boolean) IS_EMPTY_INVOKER.invokeWithArguments(otelAttributes);
        } catch (Throwable e) {
            // TODO log
            otelAttributes = null;
            return true;
        }
    }

    static Attributes empty() {
        return EMPTY;
    }

    static AttributesBuilder builder() {
        if (OTEL_ATTRIBUTES_CLASS == null || BUILDER_INVOKER == null) {
            return new AttributesBuilder(null);
        }

        try {
            Object otelAttributesBuilder = BUILDER_INVOKER.invokeStatic();
            return new AttributesBuilder(otelAttributesBuilder);
        } catch (Throwable e) {
            // TODO log
            return new AttributesBuilder(null);
        }
    }
}
