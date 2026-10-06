// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.core.util.logging.ClientLogger;

/**
 * Wrapper class for labels. Supports String, int, double and boolean types.
 *
 * If multiple values are set only one value will be used with following precedence.
 *
 * 1. stringValue.
 * 2. intValue.
 * 3. doubleValue.
 * 4. boolValue.
 */
public class RouterValueAdapter {
    private static final ClientLogger LOGGER = new ClientLogger(RouterValueAdapter.class);

    public static Object getValue(RouterValue routerValue) {
        if (routerValue.getStringValue() != null) {
            return routerValue.getStringValue();
        }
        if (routerValue.getIntValue() != null) {
            return routerValue.getIntValue();
        }
        if (routerValue.getDoubleValue() != null) {
            return routerValue.getDoubleValue();
        }
        if (routerValue.getBooleanValue() != null) {
            return routerValue.getBooleanValue();
        }
        return null;
    }
}
