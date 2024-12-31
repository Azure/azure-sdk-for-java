// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.FallbackInvoker;
import io.clientcore.core.util.ClientLogger;

import java.util.function.Consumer;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;

/**
 * Helper class to create OTel attribute keys.
 */
public class OTelAttributeKey {
    private static final ClientLogger LOGGER = new ClientLogger(OTelAttributeKey.class);
    private static final FallbackInvoker CREATE_STRING_KEY_INVOKER;
    private static final FallbackInvoker CREATE_BOOLEAN_KEY_INVOKER;
    private static final FallbackInvoker CREATE_LONG_KEY_INVOKER;
    private static final FallbackInvoker CREATE_DOUBLE_KEY_INVOKER;

    static {
        ReflectiveInvoker createStringKeyInvoker = null;
        ReflectiveInvoker createBooleanKeyInvoker = null;
        ReflectiveInvoker createLongKeyInvoker = null;
        ReflectiveInvoker createDoubleKeyInvoker = null;

        try {
            createStringKeyInvoker
                = getMethodInvoker(ATTRIBUTE_KEY_CLASS, ATTRIBUTE_KEY_CLASS.getMethod("stringKey", String.class));
            createBooleanKeyInvoker
                = getMethodInvoker(ATTRIBUTE_KEY_CLASS, ATTRIBUTE_KEY_CLASS.getMethod("booleanKey", String.class));
            createLongKeyInvoker
                = getMethodInvoker(ATTRIBUTE_KEY_CLASS, ATTRIBUTE_KEY_CLASS.getMethod("longKey", String.class));
            createDoubleKeyInvoker
                = getMethodInvoker(ATTRIBUTE_KEY_CLASS, ATTRIBUTE_KEY_CLASS.getMethod("doubleKey", String.class));
        } catch (Throwable t) {
            OTelInitializer.initError(LOGGER, t);
        }

        Consumer<Throwable> onError = t -> OTelInitializer.runtimeError(LOGGER, t);
        CREATE_STRING_KEY_INVOKER = new FallbackInvoker(createStringKeyInvoker, null, onError);
        CREATE_BOOLEAN_KEY_INVOKER = new FallbackInvoker(createBooleanKeyInvoker, null, onError);
        CREATE_LONG_KEY_INVOKER = new FallbackInvoker(createLongKeyInvoker, null, onError);
        CREATE_DOUBLE_KEY_INVOKER = new FallbackInvoker(createDoubleKeyInvoker, null, onError);
    }

    /**
     * Creates an OTel attribute key.
     *
     * @param key the key name
     * @param value the value
     * @return the OTel attribute key
     */
    public static Object getKey(String key, Object value) {
        if (OTelInitializer.isInitialized()) {
            if (value instanceof Boolean) {
                return CREATE_BOOLEAN_KEY_INVOKER.invoke(key);
            } else if (value instanceof String) {
                return CREATE_STRING_KEY_INVOKER.invoke(key);
            } else if (value instanceof Long) {
                return CREATE_LONG_KEY_INVOKER.invoke(key);
            } else if (value instanceof Integer) {
                return CREATE_LONG_KEY_INVOKER.invoke(key);
            } else if (value instanceof Double) {
                return CREATE_DOUBLE_KEY_INVOKER.invoke(key);
            } else {
                LOGGER.atVerbose()
                    .addKeyValue("key", key)
                    .addKeyValue("type", value.getClass().getName())
                    .log("Could not populate attribute. Type is not supported.");
                return null;
            }
        }

        return null;
    }

    /**
     * Casts the attribute value to the correct type.
     *
     * @param value the value
     * @return the casted value
     */
    public static Object castAttributeValue(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }

        return value;
    }
}
