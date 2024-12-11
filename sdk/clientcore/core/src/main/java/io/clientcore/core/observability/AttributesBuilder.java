package io.clientcore.core.observability;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;


public class AttributesBuilder {

    static final Class<?> OTEL_ATTRIBUTES_BUILDER_CLASS;

    private final static ReflectiveInvoker BUILD_INVOKER;
    private final static ReflectiveInvoker PUT_INT_INVOKER;
    private final static ReflectiveInvoker PUT_OBJECT_INVOKER;

    private volatile Object otelAttributesBuilder;
    static {
        Class<?> otelAttributesBuilderClass;

        ReflectiveInvoker buildInvoker;
        ReflectiveInvoker putIntInvoker;
        ReflectiveInvoker putObjectInvoker;

        try {
            otelAttributesBuilderClass = Class.forName("io.opentelemetry.api.common.AttributesBuilder", true, Span.class.getClassLoader());

            buildInvoker = ReflectionUtils.getMethodInvoker(otelAttributesBuilderClass,
                otelAttributesBuilderClass.getMethod("build"));

            putIntInvoker = ReflectionUtils.getMethodInvoker(otelAttributesBuilderClass,
                otelAttributesBuilderClass.getMethod("put", AttributeKey.OTEL_ATTRIBUTE_KEY_CLASS, int.class));

            putObjectInvoker = ReflectionUtils.getMethodInvoker(otelAttributesBuilderClass,
                otelAttributesBuilderClass.getMethod("put", AttributeKey.OTEL_ATTRIBUTE_KEY_CLASS, Object.class));

        } catch (Exception e) {
            otelAttributesBuilderClass = null;
            buildInvoker = null;
            putIntInvoker = null;
            putObjectInvoker = null;

            // TODO
        }

        OTEL_ATTRIBUTES_BUILDER_CLASS = otelAttributesBuilderClass;
        BUILD_INVOKER = buildInvoker;
        PUT_INT_INVOKER = putIntInvoker;
        PUT_OBJECT_INVOKER = putObjectInvoker;
    }

    AttributesBuilder(Object otelAttributesBuilder) {
        this.otelAttributesBuilder = otelAttributesBuilder;
    }

    Attributes build() {
        if (otelAttributesBuilder == null || BUILD_INVOKER == null) {
            return Attributes.empty();
        }

        try {
            Object otelAttributes = BUILD_INVOKER.invokeWithArguments(otelAttributesBuilder);
            return new Attributes(otelAttributes);
        } catch (Throwable e) {
            // TODO log
            otelAttributesBuilder = null;
            return Attributes.empty();
        }

    }

    <T> AttributesBuilder put(AttributeKey<Long> key, int value) {
        if (otelAttributesBuilder == null || PUT_INT_INVOKER == null) {
            return this;
        }

        try {
            PUT_INT_INVOKER.invokeWithArguments(otelAttributesBuilder, key.otelKey(), value);
        } catch (Throwable e) {
            // TODO log
            otelAttributesBuilder = null;
        }

        return this;
    }

    <T> AttributesBuilder put(AttributeKey<T> key, T value) {
        if (otelAttributesBuilder == null || PUT_OBJECT_INVOKER == null) {
            return this;
        }

        try {
            PUT_OBJECT_INVOKER.invokeWithArguments(otelAttributesBuilder, key.otelKey(), value);
        } catch (Throwable e) {
            // TODO log
            otelAttributesBuilder = null;
        }

        return this;
    }
}
