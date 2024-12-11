package io.clientcore.core.observability;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;

public class AttributeKey<T> {

    static final Class<?> OTEL_ATTRIBUTE_KEY_CLASS;

    private final static ReflectiveInvoker GET_KEY_INVOKER;
    private final static ReflectiveInvoker GET_TYPE_INVOKER;
    private final static ReflectiveInvoker CREATE_STRING_KEY_INVOKER;
    private final static ReflectiveInvoker CREATE_BOOLEAN_KEY_INVOKER;
    private final static ReflectiveInvoker CREATE_LONG_KEY_INVOKER;
    private final static ReflectiveInvoker CREATE_DOUBLE_KEY_INVOKER;

    private volatile Object otelAttributeKey;
    static {
        Class<?> otelAttributeKeyClass;

        ReflectiveInvoker getKeyInvoker;
        ReflectiveInvoker getTypeInvoker;
        ReflectiveInvoker createStringKeyInvoker;
        ReflectiveInvoker createBooleanKeyInvoker;
        ReflectiveInvoker createLongKeyInvoker;
        ReflectiveInvoker createDoubleKeyInvoker;

        try {
            otelAttributeKeyClass = Class.forName("io.opentelemetry.api.common.AttributeKey", true, Span.class.getClassLoader());

            getKeyInvoker = ReflectionUtils.getMethodInvoker(otelAttributeKeyClass,
                otelAttributeKeyClass.getMethod("getKey"));

            getTypeInvoker = ReflectionUtils.getMethodInvoker(otelAttributeKeyClass,
                otelAttributeKeyClass.getMethod("getType"));

            createStringKeyInvoker = ReflectionUtils.getMethodInvoker(otelAttributeKeyClass,
                otelAttributeKeyClass.getMethod("stringKey", String.class));

            createBooleanKeyInvoker = ReflectionUtils.getMethodInvoker(otelAttributeKeyClass,
                otelAttributeKeyClass.getMethod("booleanKey", String.class));

            createLongKeyInvoker = ReflectionUtils.getMethodInvoker(otelAttributeKeyClass,
                otelAttributeKeyClass.getMethod("longKey", String.class));

            createDoubleKeyInvoker = ReflectionUtils.getMethodInvoker(otelAttributeKeyClass,
                otelAttributeKeyClass.getMethod("doubleKey", String.class));

        } catch (Exception e) {
            otelAttributeKeyClass = null;

            getKeyInvoker = null;
            getTypeInvoker = null;
            createStringKeyInvoker = null;
            createBooleanKeyInvoker = null;
            createLongKeyInvoker = null;
            createDoubleKeyInvoker = null;
            // TODO
        }

        OTEL_ATTRIBUTE_KEY_CLASS = otelAttributeKeyClass;

        GET_KEY_INVOKER = getKeyInvoker;
        GET_TYPE_INVOKER = getTypeInvoker;

        CREATE_STRING_KEY_INVOKER = createStringKeyInvoker;
        CREATE_BOOLEAN_KEY_INVOKER = createBooleanKeyInvoker;
        CREATE_LONG_KEY_INVOKER = createLongKeyInvoker;
        CREATE_DOUBLE_KEY_INVOKER = createDoubleKeyInvoker;
    }

    AttributeKey(Object otelAttributeKey) {
        this.otelAttributeKey = otelAttributeKey;
    }

    String getKey() {
        if (otelAttributeKey == null || GET_KEY_INVOKER == null) {
            return null;
        }

        try {
            return (String) GET_KEY_INVOKER.invokeWithArguments(otelAttributeKey);
        } catch (Exception e) {
            // TODO
            otelAttributeKey = null;
            return null;
        }
    }

    AttributeType getType() {
        if (otelAttributeKey == null || GET_TYPE_INVOKER == null) {
            return null;
        }

        try {
            return AttributeType.valueOf((String) GET_TYPE_INVOKER.invokeWithArguments(otelAttributeKey));
        } catch (Exception e) {
            // TODO
            otelAttributeKey = null;
            return null;
        }
    }

    Object otelKey() {
        return otelAttributeKey;
    }

    public static AttributeKey<String> stringKey(String key) {
        if (OTEL_ATTRIBUTE_KEY_CLASS == null || CREATE_STRING_KEY_INVOKER == null) {
            return new AttributeKey<>(null);
        }

        try {
            Object otelAttributeKey = CREATE_STRING_KEY_INVOKER.invokeStatic(key);
            return new AttributeKey<>(otelAttributeKey);
        } catch (Exception e) {
            // TODO
            return new AttributeKey<>(null);
        }
    }

    /** Returns a new AttributeKey for Boolean valued attributes. */
    static AttributeKey<Boolean> booleanKey(String key) {
        if (OTEL_ATTRIBUTE_KEY_CLASS == null || CREATE_BOOLEAN_KEY_INVOKER == null) {
            return new AttributeKey<>(null);
        }

        try {
            Object otelAttributeKey = CREATE_BOOLEAN_KEY_INVOKER.invokeStatic(key);
            return new AttributeKey<>(otelAttributeKey);
        } catch (Exception e) {
            // TODO
            return new AttributeKey<>(null);
        }
    }

    /** Returns a new AttributeKey for Long valued attributes. */
    public static AttributeKey<Long> longKey(String key) {
        if (OTEL_ATTRIBUTE_KEY_CLASS == null || CREATE_LONG_KEY_INVOKER == null) {
            return new AttributeKey<>(null);
        }

        try {
            Object otelAttributeKey = CREATE_LONG_KEY_INVOKER.invokeStatic(key);
            return new AttributeKey<>(otelAttributeKey);
        } catch (Exception e) {
            // TODO
            return new AttributeKey<>(null);
        }

    }

    /** Returns a new AttributeKey for Double valued attributes. */
    static AttributeKey<Double> doubleKey(String key) {
        if (OTEL_ATTRIBUTE_KEY_CLASS == null || CREATE_DOUBLE_KEY_INVOKER == null) {
            return new AttributeKey<>(null);
        }

        try {
            Object otelAttributeKey = CREATE_DOUBLE_KEY_INVOKER.invokeStatic(key);
            return new AttributeKey<>(otelAttributeKey);
        } catch (Exception e) {
            // TODO
            return new AttributeKey<>(null);
        }
    }
}
