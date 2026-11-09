// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.opentelemetry.api.common.AttributeKey;

/**
 * Helper class to create OTel attribute keys.
 */
public class OTelAttributeKey {
    private static final ClientLogger LOGGER = new ClientLogger(OTelAttributeKey.class);

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
                return FallbackInvoker.call(LOGGER, null, () -> AttributeKey.booleanKey(key));
            } else if (value instanceof String) {
                return FallbackInvoker.call(LOGGER, null, () -> AttributeKey.stringKey(key));
            } else if (value instanceof Long) {
                return FallbackInvoker.call(LOGGER, null, () -> AttributeKey.longKey(key));
            } else if (value instanceof Integer) {
                return FallbackInvoker.call(LOGGER, null, () -> AttributeKey.longKey(key));
            } else if (value instanceof Double) {
                return FallbackInvoker.call(LOGGER, null, () -> AttributeKey.doubleKey(key));
            } else if (value instanceof Float) {
                return FallbackInvoker.call(LOGGER, null, () -> AttributeKey.doubleKey(key));
            } else if (value == null) {
                return FallbackInvoker.call(LOGGER, null, () -> AttributeKey.stringKey(key));
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

        if (value instanceof Float) {
            return ((Float) value).doubleValue();
        }

        return value;
    }
}
