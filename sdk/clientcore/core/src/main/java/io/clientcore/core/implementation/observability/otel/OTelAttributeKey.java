package io.clientcore.core.implementation.observability.otel;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;

public class OTelAttributeKey {
    private static final ClientLogger LOGGER = new ClientLogger(OTelAttributeKey.class);
    private final static ReflectiveInvoker CREATE_STRING_KEY_INVOKER;
    private final static ReflectiveInvoker CREATE_BOOLEAN_KEY_INVOKER;
    private final static ReflectiveInvoker CREATE_LONG_KEY_INVOKER;
    private final static ReflectiveInvoker CREATE_DOUBLE_KEY_INVOKER;

    static {
        ReflectiveInvoker createStringKeyInvoker = null;
        ReflectiveInvoker createBooleanKeyInvoker = null;
        ReflectiveInvoker createLongKeyInvoker = null;
        ReflectiveInvoker createDoubleKeyInvoker = null;

        try {
            createStringKeyInvoker = ReflectionUtils.getMethodInvoker(ATTRIBUTE_KEY_CLASS,
                ATTRIBUTE_KEY_CLASS.getMethod("stringKey", String.class));

            createBooleanKeyInvoker = ReflectionUtils.getMethodInvoker(ATTRIBUTE_KEY_CLASS,
                ATTRIBUTE_KEY_CLASS.getMethod("booleanKey", String.class));

            createLongKeyInvoker = ReflectionUtils.getMethodInvoker(ATTRIBUTE_KEY_CLASS,
                ATTRIBUTE_KEY_CLASS.getMethod("longKey", String.class));

            createDoubleKeyInvoker = ReflectionUtils.getMethodInvoker(ATTRIBUTE_KEY_CLASS,
                ATTRIBUTE_KEY_CLASS.getMethod("doubleKey", String.class));
        } catch (Throwable t) {
            OTelInitializer.INSTANCE.initError(LOGGER, t);
        }

        CREATE_STRING_KEY_INVOKER = createStringKeyInvoker;
        CREATE_BOOLEAN_KEY_INVOKER = createBooleanKeyInvoker;
        CREATE_LONG_KEY_INVOKER = createLongKeyInvoker;
        CREATE_DOUBLE_KEY_INVOKER = createDoubleKeyInvoker;
    }

    public static Object getKey(String key, Object value) {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                if (value instanceof Boolean) {
                    return CREATE_BOOLEAN_KEY_INVOKER.invokeStatic(key);
                } else if (value instanceof String) {
                    return CREATE_STRING_KEY_INVOKER.invokeStatic(key);
                } else if (value instanceof Long) {
                    return CREATE_LONG_KEY_INVOKER.invokeStatic(key);
                } else if (value instanceof Integer) {
                    return CREATE_LONG_KEY_INVOKER.invokeStatic(key);
                } else if (value instanceof Double) {
                    return CREATE_DOUBLE_KEY_INVOKER.invokeStatic(key);
                } else {
                    LOGGER.atVerbose().addKeyValue("key", key)
                        .addKeyValue("type", value.getClass().getName())
                        .log("Could not populate attribute. Type is not supported.");
                    return null;
                }
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return null;
    }

    public static Object castAttributeValue(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }

        return value;
    }
}
