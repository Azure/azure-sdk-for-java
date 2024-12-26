// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel;

import io.clientcore.core.util.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;

public class OTelAttributeKey {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    private static final ClientLogger LOGGER = new ClientLogger(OTelAttributeKey.class);
    private static final MethodHandle CREATE_STRING_KEY_INVOKER;
    private static final MethodHandle CREATE_BOOLEAN_KEY_INVOKER;
    private static final MethodHandle CREATE_LONG_KEY_INVOKER;
    private static final MethodHandle CREATE_DOUBLE_KEY_INVOKER;

    static {
        MethodHandle createStringKeyInvoker = null;
        MethodHandle createBooleanKeyInvoker = null;
        MethodHandle createLongKeyInvoker = null;
        MethodHandle createDoubleKeyInvoker = null;

        try {
            createStringKeyInvoker = LOOKUP.findStatic(ATTRIBUTE_KEY_CLASS, "stringKey",
                MethodType.methodType(ATTRIBUTE_KEY_CLASS, String.class));
            createBooleanKeyInvoker = LOOKUP.findStatic(ATTRIBUTE_KEY_CLASS, "booleanKey",
                MethodType.methodType(ATTRIBUTE_KEY_CLASS, String.class));
            createLongKeyInvoker = LOOKUP.findStatic(ATTRIBUTE_KEY_CLASS, "longKey",
                MethodType.methodType(ATTRIBUTE_KEY_CLASS, String.class));
            createDoubleKeyInvoker = LOOKUP.findStatic(ATTRIBUTE_KEY_CLASS, "doubleKey",
                MethodType.methodType(ATTRIBUTE_KEY_CLASS, String.class));
        } catch (Throwable t) {
            OTelInitializer.initError(LOGGER, t);
        }

        CREATE_STRING_KEY_INVOKER = createStringKeyInvoker;
        CREATE_BOOLEAN_KEY_INVOKER = createBooleanKeyInvoker;
        CREATE_LONG_KEY_INVOKER = createLongKeyInvoker;
        CREATE_DOUBLE_KEY_INVOKER = createDoubleKeyInvoker;
    }

    public static Object getKey(String key, Object value) {
        if (OTelInitializer.isInitialized()) {
            try {
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
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
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
